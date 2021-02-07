package pluginutil;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import static pluginutil.PluginUtil.sendLog;

@SuppressWarnings("unused cast ResultOfMethodCallIgnored")
public class GHReadWrite {

    public static void writeToFile(String dir, Object obj) throws IOException {
        new File(dir).createNewFile();
        FileWriter writer = new FileWriter(dir);
        writer.write(new Gson().toJson(obj));
        writer.close();
    }

    public static GHPlugin.GHPluginConfig readFromFile(String dir, GHPlugin.GHPluginConfig obj) throws FileNotFoundException {
        Scanner reader = new Scanner(new File(dir));
        StringBuilder sb = new StringBuilder();
        while (reader.hasNextLine())
            sb.append(reader.nextLine());
        obj = new Gson().fromJson(sb.toString(), obj.getClass());
        reader.close();
        return obj;
    }

    public static void moveOldFile(File file, String dir, String plugin, boolean silence) throws IOException {
        if (file == null)
            return;
        file.renameTo(new File(dir + ".outdated"));
        FileWriter writer = new FileWriter(dir + ".outdated");
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine())
            writer.write(reader.nextLine() + "\n");
        writer.close();
        sendLog(0, "Outdated Config File Found. Moved to Directory \n[" + file.getAbsolutePath() + "].\n" +
                "Outdated Config will be overwrite when a newer Outdated Config is detected.", plugin);
    }

    public static class GHReadWriteException extends Exception {
        public static final int NEW_FILE = 0, TOO_MANY_LINES = 1, TOO_FEW_LINES = 2;
        public int type;

        public GHReadWriteException(String message, int type) {
            super(message);
            this.type = type;
        }
    }
}
