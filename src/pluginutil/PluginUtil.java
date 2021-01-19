package pluginutil;

import arc.util.Log;
import mindustry.gen.Call;
import mindustry.gen.Player;

@SuppressWarnings({"unused", "SameParameterValue"})
public class PluginUtil {
    public static final String plgn = "[scarlet]", pass = "[green]", announce = "[orange]", accent = "[accent]", clean = "[white]", ignore = "[lightgray]";
    public static final int info = 0, warn = 1, err = 2, debug = 3;

    public static String f(String str, Object... args) {
        return String.format(str, args);
    }

    // Send message to all players
    protected static void sendMsg(String color, String msg, String plugin) {
        sendMsg(color, msg, null, plugin);
    }

    // Send message to certain player
    protected static void sendMsg(String color, String msg, Player player, String plugin) {
        String str = plgn + plugin + ": " + color + msg;
        if (player == null)
            Call.sendMessage(str);
        else
            player.sendMessage(str);
    }

    // Send message to console as Log
    public static void sendLog(int mode, String msg, String plugin) {
        String str = plugin + ": " + msg;
        switch (mode) {
            case 0 -> Log.info(str);
            case 1 -> Log.warn(str);
            case 2 -> Log.err(str);
            case 3 -> Log.debug(str);
        }
    }

    // Send message to certain player or  to console as Log based on factor.
    public static void output(int mode, String color, String msg, Player player, Boolean server, String plugin) {
        if (server == null || server)
            sendLog(mode, msg, plugin);
        if (server == null || !server)
            sendMsg(color, msg, player, plugin);
    }
}
