package pluginutil;

import arc.graphics.Color;
import arc.graphics.Colors;
import arc.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class PluginUtil {
    public static boolean canParseBoolean(String str){
        return switch (str) {
            case "true", "1", "yes", "false", "0", "no" -> true;
            default -> false;
        };
    }
    public static boolean parseBoolean(String str){
        return parseBoolean(str, false);
    }
    public static boolean parseBoolean(String str, boolean def){
        return switch (str) {
            case "true", "1", "yes" -> true;
            case "false", "0", "no" -> false;
            default -> def;
        };
    }
    public static boolean canParseInt(String str){
        try{ Integer.parseInt(str); return true; }catch (Exception e) { return false; }
    }
    public static int parseInt(String str){
        return parseInt(str, Integer.MAX_VALUE);
    }
    public static int parseInt(String str, int def){
        try{ return Integer.parseInt(str); }catch (Exception e) { return def; }
    }
    public static boolean canParseFloat(String str){
        try{ Float.parseFloat(str); return true; }catch (Exception e) { return false; }
    }
    public static float parseFloat(String str){
        return parseFloat(str, Float.MAX_VALUE);
    }
    public static float parseFloat(String str, float def) {
        try{ return Float.parseFloat(str); }catch (Exception e) { return def; }
    }
    public static boolean canParseLong(String str, long def) {
        try{ Long.parseLong(str); return true; }catch (Exception e) { return false; }
    }
    public static long parseLong(String str){
        return parseLong(str, Long.MAX_VALUE);
    }
    public static long parseLong(String str, long def) {
        try{ return Long.parseLong(str); }catch (Exception e) { return def; }
    }

    public static String f(String str, Object... args){
        return String.format(str, args);
    }
    public static void w(FileWriter writer, String str) throws Exception {
        writer.write(str + "\n");
    }
    public static String r(Scanner reader) throws Exception {
        return reader.nextLine().split(": ")[1];
    }

    public static String colorRemove(String str){
        // Reference for This Holy Magic
        // NetServer.java: fixName(), checkColor()
        if(str.equals("[") || str.equals("]"))
            return "";

        for(int i = 0; i < str.length(); i++)
            if(str.charAt(i) == '[' && i != str.length() - 1 && str.charAt(i + 1) != '[' && (i == 0 || str.charAt(i - 1) != '[')){
                String prev = str.substring(0, i);
                String next = str.substring(i);
                String result = next;
                for(int j = 1; j < next.length(); j++)
                    if(next.charAt(j) == ']'){
                        String color = next.substring(1, j);
                        if(Colors.get(color.toUpperCase()) != null || Colors.get(color.toLowerCase()) != null){
                            result = next.substring(j + 1);
                            break;
                        }else
                            try{
                                Color.valueOf(color);
                                result = next.substring(j + 1);
                                break;
                            }catch(Exception e){
                                result = next;
                                break;
                            }
                    }

                str = prev + result;
            }
        // Holy Magic Ends Here.

        // Remove "[" at the end if any.
        if (str.endsWith("["))
            str = str.substring(0, str.length()-1);
        return str;
    }

    public void moveOldFile(File file, String dir, String plugin) throws Exception {
        file.renameTo(new File(dir + ".outdated"));
        FileWriter writer = new FileWriter(dir + ".outdated");
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine())
            writer.write(reader.nextLine() + "\n");
        writer.close();
        log(0, "Outdated Config File Found. Moved to Directory \n[" + file.getAbsolutePath() + "].\n" +
                "Outdated Config will be overwrite when a newer Outdated Config is detected.", plugin);
    }

    public void log(int mode, String msg, String plugin){
        switch (mode){
            case 0 -> Log.info(plugin + ": " + msg);
            case 1 -> Log.warn(plugin + ": " + msg);
        }
    }
}
