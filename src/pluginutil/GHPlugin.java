package pluginutil;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.mod.Plugin;
import mindustry.net.NetConnection;
import mindustry.net.Packets;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.function.BiPredicate;

import static pluginutil.GHReadWrite.readFromFile;
import static pluginutil.GHReadWrite.writeToFile;
import static pluginutil.LogMode.info;
import static pluginutil.PluginUtil.sendLog;
import static pluginutil.PluginUtil.sendMsg;

@SuppressWarnings("unused")
public abstract class GHPlugin extends Plugin {

    protected String PLUGIN, CONFIG_DIR, VERSION;
    protected String[] adminOnlyCommands;

    protected GHPluginConfig cfg;

    protected LinkedHashMap<Class<?>, BiPredicate<NetConnection, Object>> piMap;

    public GHPlugin() {
        PLUGIN = this.getClass().getSimpleName();
        CONFIG_DIR = Vars.modDirectory + "/" + PLUGIN + ".json";
        VERSION = "1.0";

        adminOnlyCommands = new String[0];
        piMap = new LinkedHashMap<>();
    }

    // Called when game initializes
    public void init() {
        // Load values from file
        // If defConfig method is declared in the class && the class is not GHPlugin
        // Then read the data method to update list.
        try {
            if (getClass() == getClass().getDeclaredMethod("defConfig").getDeclaringClass()) {
                defConfig();
                read();
                cfg().softReset();
                write();

                log("Values loaded from file(s).");
            }
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            log(LogMode.warn, "An error has occurred while loading values. Plugin is turned off.");
            e.printStackTrace();
        }

        // If update method is declared in the class && the class is not GHPlugin
        // Then add its update method to update list.
        try {
            if (getClass() == getClass().getDeclaredMethod("update").getDeclaringClass()) {
                Core.app.addListener(new ApplicationListener() {
                    @Override
                    public void update() {
                        GHPlugin.this.update();
                    }
                });
                log("Update method implemented.");
            }
        } catch (NoSuchMethodException ignored) {
        }

        // If Enhance Help Command exists, register the admin only commands.
        if (adminOnlyCommands.length > 0) {
            Mods.LoadedMod ehc = Vars.mods.list().find(m -> m.main != null && m.main.getClass().getSimpleName().equals("EnhancedHelpCommand"));
            if (ehc != null) {
                Events.on(ehc.main.getClass(), e -> registerAdminOnlyCommands(ehc.main));
            } else
                log("Cannot find EnhancedHelpCommand, skipping admin only commands.");
        }

        // If Packet Interceptor exists, register the admin only commands.

        try {
            if (getClass() == getClass().getDeclaredMethod("onConnect", NetConnection.class, Object.class).getDeclaringClass())
                piMap.put(Packets.Connect.class, this::onConnect);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            if (getClass() == getClass().getDeclaredMethod("onDisconnect", NetConnection.class, Object.class).getDeclaringClass())
                piMap.put(Packets.Disconnect.class, this::onDisconnect);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            if (getClass() == getClass().getDeclaredMethod("onConnectPacket", NetConnection.class, Object.class).getDeclaringClass())
                piMap.put(Packets.ConnectPacket.class, this::onConnectPacket);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            if (getClass() == getClass().getDeclaredMethod("onInvokePacket", NetConnection.class, Object.class).getDeclaringClass())
                piMap.put(Packets.InvokePacket.class, this::onInvokePacket);
        } catch (NoSuchMethodException ignored) {
        }

        if (piMap.size() > 0) {
            Mods.LoadedMod pi = Vars.mods.list().find(m -> m.main != null && m.main.getClass().getSimpleName().equals("PacketInterceptor"));
            if (pi != null) {
                Events.on(pi.main.getClass(), e -> registerPacketInterceptors(pi.main));
            } else
                log("Cannot find PacketInterceptor, skipping packet interceptors.");
        }
    }

    // Register the admin only commands in EnhancedHelpCommand plugin if it exists.
    private void registerAdminOnlyCommands(Mod ehc) {
        try {
            Method getVersion = ehc.getClass().getMethod("getVersion");
            if (!getVersion.invoke(ehc).equals("1.1"))
                return;

            Method add = ehc.getClass().getDeclaredMethod("add", String[].class);
            add.invoke(ehc, (Object) adminOnlyCommands);
            log(adminOnlyCommands.length + " admin only command(s) registered.");
        } catch (Exception e) {
            log(LogMode.warn, "An error has occurred while registering admin only command(s).");
            e.printStackTrace();
        }
    }

    // Register the packet interceptors in PacketInterceptor plugin if it exists.
    private void registerPacketInterceptors(Mod pi) {
        try {
            Method getVersion = pi.getClass().getMethod("getVersion");
            if (!getVersion.invoke(pi).equals("1.1"))
                return;

            Method getListeners = pi.getClass().getDeclaredMethod("getListeners");
            Method addListener = pi.getClass().getDeclaredMethod("addListener", Class.class, Class.class, BiPredicate.class);

            int success = 0;
            for (Class<?> cls : (Class<?>[]) getListeners.invoke(pi)) {
                BiPredicate<NetConnection, Object> pred = piMap.get(cls);
                if (pred == null) continue;
                try {
                    addListener.invoke(pi, cls, getClass(), pred);
                    success++;
//                    log("on" + cls.getSimpleName() + " method implemented.");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            log(success + " packet interceptor(s) registered.");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log("Error occurred while implementing on packet methods.");
            e.printStackTrace();
        }
    }

    // Update, Override to use.
    protected void update() {
    }


    // Packet Interceptors
    protected boolean onConnect(NetConnection con, Object obj) {
        return false;
    }

    protected boolean onDisconnect(NetConnection con, Object obj) {
        return false;
    }

    protected boolean onConnectPacket(NetConnection con, Object obj) {
        return false;
    }

    protected boolean onInvokePacket(NetConnection con, Object obj) {
        return false;
    }


    // Return '/cmd' or 'cmd', depended by whether if it is used to print in chat or console.
    protected String cmd(boolean prefix, String cmd) {
        return (prefix ? "/" : "") + cmd;
    }

    // Send message to all players
    protected void msg(String msg) {
        msg(GHPal.clean, msg);
    }

    // Send message to all players
    protected void msg(String color, String msg) {
        msg(color, msg, null);
    }

    // Send message to certain player
    protected void msg(String msg, Player player) {
        msg(GHPal.clean, msg, player);
    }

    // Send message to certain player
    protected void msg(String color, String msg, Player player) {
        sendMsg(color, msg, player, PLUGIN);
    }

    // Send message to console
    protected void log(String msg) {
        log(info, msg);
    }

    protected void log(LogMode mode, String msg) {
        sendLog(mode, msg, PLUGIN);
    }

    // Send message to all players, certain player or console.
    protected void output(String msg) {
        output(msg, null, OutputMode.toAll);
    }

    protected void output(String msg, Player player) {
        output(msg, player, OutputMode.toAll);
    }

    protected void output(String msg, OutputMode server) {
        output(msg, null, server);
    }

    protected void output(String msg, Player player, OutputMode server) {
        output(info, msg, player, server);
    }

    protected void output(LogMode mode, String msg, Player player, OutputMode server) {
        output(mode, GHPal.clean, msg, player, server);
    }

    protected void output(String color, String msg, Player player, OutputMode server) {
        output(info, color, msg, player, server);
    }

    protected void output(LogMode mode, String color, String msg, Player player, OutputMode server) {
        PluginUtil.output(mode, color, msg, player, server, PLUGIN);
    }

    // Write File
    protected void write() {
        write(false);
    }

    protected void write(boolean silence) {
        try {
            writeToFile(CONFIG_DIR, cfg);
            if (!silence)
                log("Configs Saved To File.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read file
    protected void read() {
        read(false);
    }

    protected void read(boolean silence) {
        try {
            cfg = readFromFile(CONFIG_DIR, cfg);
            if (!silence)
                log("Configs Loaded From File.");
        } catch (FileNotFoundException fnfe) {
            defConfig();
            write();
        }
    }

    // Default configs here
    protected void defConfig() {
    }

    public String getVersion() {
        return VERSION;
    }

    protected <T extends GHPluginConfig> T cfg() {
        return null;
    }

    protected abstract static class GHPluginConfig {

        protected GHPluginConfig() {
            reset();
        }

        protected abstract void reset();

        protected abstract boolean softReset();
    }
}
