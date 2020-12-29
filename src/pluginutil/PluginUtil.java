package pluginutil;

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
    private String f(String str, Object... args){
        return String.format(str, args);
    }
    public static void w(FileWriter writer, String str) throws Exception {
        writer.write(str + "\n");
    }
    public static String r(Scanner reader) throws Exception {
        return reader.nextLine().split(": ")[1];
    }
}
