package pluginutil;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.util.io.Reads;
import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.mod.Plugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.function.BiPredicate;

import static pluginutil.GHReadWrite.*;
import static pluginutil.GHReadWrite.GHReadWriteException.NEW_FILE;
import static pluginutil.PluginUtil.SendMode.info;
import static pluginutil.PluginUtil.SendMode.warn;
import static pluginutil.PluginUtil.*;

@SuppressWarnings("unused")
public class GHPlugin extends Plugin {

    private static final LinkedHashMap<String, Field> config_map = new LinkedHashMap<>();

    protected static String[] configurables;
    protected static String[] adminOnlyCommands;
    protected static HashMap<Integer, HashSet<BiPredicate<Reads, Player>>> packetsInterceptorMap;

    protected String PLUGIN, CONFIG_DIR, VERSION;
    protected boolean mode;

    public GHPlugin() {
        PLUGIN = this.getClass().getSimpleName();
        CONFIG_DIR = Vars.modDirectory + "/" + PLUGIN + ".cfg";
        VERSION = "1.0";
        configurables = new String[]{"mode"};
        adminOnlyCommands = new String[0];
        packetsInterceptorMap = new HashMap<>();
    }

    // Called when game initializes
    public void init() {
        try {
            // If update method is declared in the class && the class is not GHPlugin
            // Then add its update method to update list.
            if (getClass() == getClass().getMethod("update").getDeclaringClass() &&
                    getClass() != GHPlugin.class) {
                Core.app.addListener(new ApplicationListener() {
                    @Override
                    public void update() {
                        GHPlugin.this.update();
                    }
                });
                log(info, "Update method implemented.");
            }
        } catch (NoSuchMethodException ignored) {
        }

        // Load values from file
        if (configurables.length > 0) {
            try {
                initMap(getClass(), config_map, configurables);
                read();
                log(info, "Values loaded from file(s).");
            } catch (Exception e) {
                mode = false;
                log(warn, "An error has occurred. Plugin is turned off.");
                e.printStackTrace();
            }
        }

        registerAdminOnlyCommands();
        registerPacketInterceptors();
    }

    // Register the admin only commands in EnhancedHelpCommand plugin if it exists.
    private void registerAdminOnlyCommands() {
        if (adminOnlyCommands.length > 0) {
            Mods.LoadedMod mod = Vars.mods.list().find(m -> m.main != null && m.main.getClass().getSimpleName().equals("EnhancedHelpCommand"));
            if (mod != null) {
                try {
                    Method add = mod.main.getClass().getDeclaredMethod("adminCommands", String[].class);
                    add.invoke(mod.main, (Object) adminOnlyCommands);
                    log(info, "Admin only command(s) registered.");
                } catch (Exception e) {
                    log(warn, "An error has occurred while registering admin only command(s).");
                    e.printStackTrace();
                }
            }
        }
    }

    // Register the packet interceptors in PacketInterceptor plugin if it exists.
    private void registerPacketInterceptors() {
        if (packetsInterceptorMap.size() > 0) {
            Mods.LoadedMod mod = Vars.mods.list().find(m -> m.main != null && m.main.getClass().getSimpleName().equals("PacketInterceptor"));
            if (mod != null) {
                try {
                    Class<?> cls = mod.main.getClass();
                    //Find the Getters & Setter of PacketInterceptor
                    Method getRead = cls.getDeclaredMethod("getRead");
                    Method getType = cls.getDeclaredMethod("getType");
                    Method getPlayer = cls.getDeclaredMethod("getPlayer");
                    Method setOverwrite = cls.getDeclaredMethod("setOverwrite", boolean.class);

                    // Register the onEvent Method of this plugin to the Event Handler.
                    Events.on(cls, e -> onPacketIntercept(getRead, getType, getPlayer, setOverwrite, mod.main));
                    log(info, "Packet interceptor(s) registered.");
                } catch (Exception e) {
                    log(warn, "An error has occurred while registering packet interceptor(s).");
                    e.printStackTrace();
                }
            }
        }
    }


    // On Packet Intercept
    protected void onPacketIntercept(Method getRead, Method getType, Method getPlayer, Method setOverwrite, Mod mod) {
        try {
            Reads read = (Reads) getRead.invoke(mod);
            int type = (int) getType.invoke(mod);
            Player player = (Player) getPlayer.invoke(mod);

            if (read == null || type == -1 || player == null) {
                log(info, f("Packet data missing, aborted. [%s, %s, %s]", read, type, player));
                return;
            }

            HashSet<BiPredicate<Reads, Player>> set = packetsInterceptorMap.get(type);

            boolean overwrite = false;
            for (BiPredicate<Reads, Player> entry : set)
                overwrite = entry.test(read, player) || overwrite;

            if (overwrite)
                setOverwrite.invoke(mod, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update, Override to use.
    protected void update() {
    }

    // Return '/cmd' or 'cmd', depended by whether if it is used to print in chat or console.
    protected String cmd(boolean prefix, String cmd) {
        return (prefix ? "/" : "") + cmd;
    }

    // Send message to all players
    protected void msg(String color, String msg) {
        sendMsg(color, msg, null);
    }

    // Send message to certain player
    protected void msg(String color, String msg, Player player) {
        sendMsg(color, msg, player, PLUGIN);
    }

    // Send message to console
    protected void log(int mode, String msg) {
        sendLog(mode, msg, PLUGIN);
    }

    // Send message to all players, certain player or console.
    protected void output(int mode, String color, String msg, Player player, Boolean server) {
        PluginUtil.output(mode, color, msg, player, server, PLUGIN);
    }

    // Write File
    protected void write() {
        try {
            writeToFile(CONFIG_DIR, config_map, this);
            log(info, "Configs Wrote To File.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read file
    protected void read() throws IOException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        try {
            readFromFile(CONFIG_DIR, config_map, this);
            log(info, "Configs Read From File.");
        } catch (GHReadWriteException ghe) {
            log(warn, f("Error Occurred While Reading: %s", ghe.getMessage()));
            if (ghe.type == NEW_FILE) {
                defConfig();
                write();
                log(warn, f("Config File Is Populated With Default Values"));
            }
        }
    }

    // Default configs here
    protected void defConfig() {
        mode = true;
    }
}
