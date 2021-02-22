package pluginutil;

import arc.util.Log;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static pluginutil.GHPal.plgn;

@SuppressWarnings({"unused", "SameParameterValue"})
public class PluginUtil {
    public static String f(String str, Object... args) {
        return String.format(str, args);
    }

    // Send message to certain player
    public static void sendMsg(String color, String msg, Player player, String plugin) {
        String str = plgn + plugin + ": " + color + msg;
        if (player == null)
            Call.sendMessage(str);
        else
            player.sendMessage(str);
    }

    // Send message to console as Log
    public static void sendLog(LogMode mode, String msg, String plugin) {
        String str = plugin + ": " + msg;
        switch (mode) {
            case info -> Log.info(str);
            case warn -> Log.warn(str);
            case err -> Log.err(str);
            case debug -> Log.debug(str);
        }
    }

    // Send message to certain player or  to console as Log based on factor.
    public static void output(LogMode mode, String color, String msg, Player player, OutputMode outputMode, String plugin) {
        if (outputMode == OutputMode.toAll || outputMode == OutputMode.toServer)
            sendLog(mode, msg, plugin);
        if (outputMode == OutputMode.toAll || outputMode == OutputMode.toClient)
            sendMsg(color, msg, player, plugin);
    }
}
