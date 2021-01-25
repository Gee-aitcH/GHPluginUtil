package pluginutil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import static pluginutil.GHParse.camelCase2SentenceCase;
import static pluginutil.GHParse.parseSthOrArr;
import static pluginutil.PluginUtil.f;
import static pluginutil.PluginUtil.sendLog;

@SuppressWarnings("unused")
public class GHReadWrite {

    public static void initMap(Class<?> cls, LinkedHashMap<String, Field> map, String[] configs) {
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String cfgName = field.getName();
            cfgName = camelCase2SentenceCase(cfgName);
            for (String str : configs)
                if (str.equals(field.getName()))
                    map.put(cfgName, field);
        }
    }

    public static void writeToFile(String dir, LinkedHashMap<String, Field> map, Object instance) throws IOException, IllegalAccessException {
        File file = new File(dir);
        FileWriter writer = new FileWriter(file);

        for (Map.Entry<String, Field> entry : map.entrySet())
            writer.write(entry.getKey() + ": " + entry.getValue().get(instance) + "\n");

        writer.close();
    }

    public static void readFromFile(String dir, LinkedHashMap<String, Field> map, Object instance) throws IOException, GHReadWriteException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        File file = new File(dir);
        if (file.createNewFile())
            throw new GHReadWriteException(f("Config File Created at Directory [%s].", file.getAbsoluteFile()), 0);
        Scanner reader = new Scanner(file);
        ArrayList<String> lines = new ArrayList<>();
        while (reader.hasNextLine())
            lines.add(reader.nextLine());

        reader.close();

        if (lines.size() != map.size()) {
            boolean greater = lines.size() > map.size();
            throw new GHReadWriteException(f(
                    "Config File has %s line%s %s than required. [lines: %s, map size: %s]%n",
                    (lines.size() - map.size()) * (greater ? 1 : -1),
                    lines.size() - map.size() == 1 ? "" : "s",
                    greater ? "more" : "less",
                    lines.size(), map.size()), greater ? 1 : 2);
        }

        for (Map.Entry<String, Field> entry : map.entrySet()) {
            Field field = entry.getValue();
            String match = "";

            for (String line : lines) {
                String[] split = line.split(": ");

                if (!split[0].equals(entry.getKey())) continue;

                field.set(instance, parseSthOrArr(split[1], field.get(instance)));
                match = line;
                break;
            }

            if (!match.equals(""))
                lines.remove(match);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void moveOldFile(File file, String dir, String plugin) throws IOException {
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
