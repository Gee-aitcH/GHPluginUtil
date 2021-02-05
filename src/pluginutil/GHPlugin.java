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

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static pluginutil.GHReadWrite.readFromFile;
import static pluginutil.GHReadWrite.writeToFile;
import static pluginutil.PluginUtil.GHColors.clean;
import static pluginutil.PluginUtil.SendMode.info;
import static pluginutil.PluginUtil.SendMode.warn;
import static pluginutil.PluginUtil.*;

@SuppressWarnings("unused")
public class GHPlugin extends Plugin {

    protected String PLUGIN, CONFIG_DIR, VERSION;
    protected static String[] adminOnlyCommands;
    protected GHPluginConfig cfg;

    public GHPlugin() {
        PLUGIN = this.getClass().getSimpleName();
        CONFIG_DIR = Vars.modDirectory + "/" + PLUGIN + ".json";
        VERSION = "1.0";
        cfg = new GHPluginConfig();

        adminOnlyCommands = new String[0];
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
        try {
            if (getClass() == getClass().getDeclaringClass().getDeclaringClass() &&
                    getClass() != GHPlugin.class) {
                Core.app.addListener(new ApplicationListener() {
                    @Override
                    public void update() {
                        GHPlugin.this.update();
                    }
                });
                log(info, "Update method implemented.");
            }
            read();
            log(info, "Values loaded from file(s).");
        } catch (Exception e) {
            log(warn, "An error has occurred. Plugin is turned off.");
            e.printStackTrace();
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
                    Method add = mod.main.getClass().getDeclaredMethod("add", String[].class);
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
        try {
            Mods.LoadedMod mod = Vars.mods.list().find(m -> m.main != null && m.main.getClass().getSimpleName().equals("PacketInterceptor"));
            if (mod == null) return;
            Class<?> cls = mod.main.getClass();
            Method getPacketData = cls.getDeclaredMethod("getPacketData");
            Method setOverwrite = cls.getDeclaredMethod("setOverwrite", boolean.class);
            try {
                if (getClass() == getClass().getMethod("onConnectPacket", Reads.class, int.class, Player.class).getDeclaringClass() &&
                        getClass() != GHPlugin.class) {
                    Class<?>[] clses = cls.getDeclaredClasses();
                    for (Class<?> cls1 : clses) {
                        if (!cls1.getSimpleName().equals("PIConnect")) return;
                        Events.on(cls, e -> {
                            try {
                                Object[] objs = (Object[]) getPacketData.invoke(mod);
                                if (objs.length != 1 || objs[0] == null) {
                                    log(info, f("Malformed packet data, aborted. Data: %s", Arrays.toString(objs)));
                                    return;
                                }

                                if (onConnectPacket((String) objs[0]))
                                    setOverwrite.invoke(mod, true);
                            } catch (IllegalAccessException | InvocationTargetException eee) {
                                eee.printStackTrace();
                            }
                        });
                        log(info, "onConnectPacket method implemented.");
                        break;
                    }
                }
            } catch (NoSuchMethodException ignored) {
            }

            try {
                if (getClass() == getClass().getMethod("onDisconnectPacket", Method.class, Method.class, Mod.class).getDeclaringClass() &&
                        getClass() != GHPlugin.class) {
                    Class<?>[] clses = cls.getDeclaredClasses();
                    for (Class<?> cls1 : clses) {
                        if (!cls1.getSimpleName().equals("PIDisconnect")) return;
                        Events.on(cls, e -> {
                            try {
                                Object[] objs = (Object[]) getPacketData.invoke(mod);
                                if (objs.length != 1 || objs[0] == null) {
                                    log(info, f("Malformed packet data, aborted. Data: %s", Arrays.toString(objs)));
                                    return;
                                }

                                if (onDisconnectPacket((String) objs[0]))
                                    setOverwrite.invoke(mod, true);
                            } catch (IllegalAccessException | InvocationTargetException eee) {
                                eee.printStackTrace();
                            }
                        });
                        log(info, "onDisconnectPacket method implemented.");
                        break;
                    }
                }
            } catch (NoSuchMethodException ignored) {
            }

            try {
                if (getClass() == getClass().getMethod("onInvokePacket", Reads.class, int.class, Player.class).getDeclaringClass() &&
                        getClass() != GHPlugin.class) {
                    Class<?>[] clses = cls.getDeclaredClasses();
                    for (Class<?> cls1 : clses) {
                        if (!cls1.getSimpleName().equals("PIInvokePacket")) return;
                        Events.on(cls, e -> {
                            try {
                                Object[] objs = (Object[]) getPacketData.invoke(mod);
                                if (objs.length != 3 || objs[0] == null || (int) objs[1] == -1 || objs[2] == null) {
                                    log(info, f("Malformed packet data, aborted. Data: %s", Arrays.toString(objs)));
                                    return;
                                }

                                if (onInvokePacket((Reads) objs[0], (int) objs[1], (Player) objs[2]))
                                    setOverwrite.invoke(mod, true);
                            } catch (IllegalAccessException | InvocationTargetException eee) {
                                eee.printStackTrace();
                            }
                        });
                        log(info, "onInvokePacket method implemented.");
                        break;
                    }
                }
            } catch (NoSuchMethodException ignored) {
            }
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            log(info, "Something weird about packet interceptor related methods implemented.");
        }
    }


    // On Packet Intercept
    protected boolean onConnectPacket(String addressTCP) {
        return false;
    }

    protected boolean onDisconnectPacket(String reason) {
        return false;
    }

    protected boolean onInvokePacket(Reads read, int type, Player player) {
        return false;
    }

    // Update, Override to use.
    protected void update() {
    }

    // Return '/cmd' or 'cmd', depended by whether if it is used to print in chat or console.
    protected String cmd(boolean prefix, String cmd) {
        return (prefix ? "/" : "") + cmd;
    }

    // Send message to all players
    protected void msg(String msg) {
        sendMsg(clean, msg, null);
    }

    // Send message to all players
    protected void msg(String color, String msg) {
        sendMsg(color, msg, null);
    }

    // Send message to certain player
    protected void msg(String msg, Player player) {
        sendMsg(clean, msg, player, PLUGIN);
    }

    // Send message to certain player
    protected void msg(String color, String msg, Player player) {
        sendMsg(color, msg, player, PLUGIN);
    }

    // Send message to console
    protected void log(String msg) {
        sendLog(info, msg, PLUGIN);
    }

    protected void log(int mode, String msg) {
        sendLog(mode, msg, PLUGIN);
    }

    // Send message to all players, certain player or console.
    protected void output(String msg, Player player, Boolean server) {
        PluginUtil.output(info, clean, msg, player, server, PLUGIN);
    }

    protected void output(int mode, String msg, Player player, Boolean server) {
        PluginUtil.output(mode, clean, msg, player, server, PLUGIN);
    }

    protected void output(String color, String msg, Player player, Boolean server) {
        PluginUtil.output(info, color, msg, player, server, PLUGIN);
    }

    protected void output(int mode, String color, String msg, Player player, Boolean server) {
        PluginUtil.output(mode, color, msg, player, server, PLUGIN);
    }

    // Write File
    public void write() {
        try {
            writeToFile(CONFIG_DIR, cfg);
            log(info, "Configs Wrote To File.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read file
    public void read() {
        try {
            cfg = readFromFile(CONFIG_DIR, cfg);
            log(info, "Configs Read From File.");
        } catch (FileNotFoundException fnfe) {
            defConfig();
            write();
        }
    }

    // Default configs here
    protected void defConfig() {
        cfg = new GHPluginConfig();
    }

    public static class GHPluginConfig {

        public GHPluginConfig() {
            reset();
        }

        public void reset() {
        }
    }
}
