package pluginutil;

import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.mod.Plugin;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import static pluginutil.GHReadWrite.*;
import static pluginutil.PluginUtil.*;

@SuppressWarnings({"unused", "SameParameterValue"})
public class GHPlugin extends Plugin {

    private static final LinkedHashMap<String, Field> config_map = new LinkedHashMap<>();

    protected boolean mode;
    protected static String[] configurables;
    protected String PLUGIN, CONFIG_DIR, VERSION;

    public GHPlugin() {
        PLUGIN = this.getClass().getSimpleName();
        CONFIG_DIR = Vars.modDirectory + "/" + PLUGIN + ".cfg";
        VERSION = "1.0";
        configurables = new String[]{"mode"};
    }

    //called when game initializes
    public void init() {
        try {
            initMap(this.getClass(), config_map, configurables);
            read();
        } catch (Exception e) {
            mode = false;
            log(warn, "An Error has occurred. Plugin is turned off.");
            e.printStackTrace();
        }
    }

    // Send message to all players
    protected void msg(String color, String msg) {
        sendMsg(color, msg, null);
    }

    // Send message to certain player
    protected void msg(String color, String msg, Player player) {
        sendMsg(color, msg, player, PLUGIN);
    }

    // Send message to console, 0: info, 1: warn
    protected void log(int mode, String msg) {
        sendLog(mode, msg, PLUGIN);
    }

    // o Stands for output. Bad name, I know, whatever.
    // Send message to all players, certain player or console.
    protected void output(int mode, String color, String msg, Player player, Boolean server) {
        PluginUtil.output(mode, color, msg, player, server, PLUGIN);
    }

    // Return '/cmd' or 'cmd', depended by whether if it is used to print in chat or console.
    protected String cmd(boolean prefix, String cmd) {
        return (prefix ? "/" : "") + cmd;
    }

    // Write File
    protected void write() {
        try {
            writeToFile(CONFIG_DIR, config_map, this);
        } catch (GHReadWriteException ghe) {
            log(warn, f("Error Occurred While Writing: %s", ghe.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read file
    protected void read() {
        try {
            readFromFile(CONFIG_DIR, config_map, this);
        } catch (GHReadWriteException ghe) {
            log(warn, f("Error Occurred While Reading: %s", ghe.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Default configs here
    protected void defConfig() {
        mode = true;
    }
}
