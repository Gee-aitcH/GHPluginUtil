package pluginutil;

import arc.graphics.Color;
import arc.graphics.Colors;

import java.lang.reflect.Array;

@SuppressWarnings("unused")
public class GHParse {
    public static boolean canParseBoolean(String str) {
        return switch (str) {
            case "true", "1", "yes", "false", "0", "no" -> true;
            default -> false;
        };
    }

    public static boolean parseBoolean(String str) {
        return parseBoolean(str, false);
    }

    public static boolean parseBoolean(String str, boolean def) {
        return switch (str) {
            case "true", "1", "yes" -> true;
            case "false", "0", "no" -> false;
            default -> def;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean canParseByte(String str) {
        try {
            Byte.parseByte(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static byte parseByte(String str) {
        return parseByte(str, Byte.MAX_VALUE);
    }

    public static byte parseByte(String str, byte def) {
        try {
            return Byte.parseByte(str);
        } catch (Exception e) {
            return def;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean canParseShort(String str) {
        try {
            Short.parseShort(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static short parseShort(String str) {
        return parseShort(str, Short.MAX_VALUE);
    }

    public static short parseShort(String str, short def) {
        try {
            return Short.parseShort(str);
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean canParseInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int parseInt(String str) {
        return parseInt(str, Integer.MAX_VALUE);
    }

    public static int parseInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean canParseFloat(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static float parseFloat(String str) {
        return parseFloat(str, Float.MAX_VALUE);
    }

    public static float parseFloat(String str, float def) {
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean canParseDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static double parseDouble(String str) {
        return parseDouble(str, Double.MAX_VALUE);
    }

    public static double parseDouble(String str, double def) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean canParseLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static long parseLong(String str) {
        return parseLong(str, Long.MAX_VALUE);
    }

    public static long parseLong(String str, long def) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return def;
        }
    }

    public static Object parseSth(String str, Object obj) {
        if (obj == null) return null;
        Class<?> cls = obj.getClass();
        if (cls.isAssignableFrom(Boolean.class) && canParseBoolean(str)) return parseBoolean(str);
        if (cls.isAssignableFrom(Byte.class) && canParseByte(str)) return Byte.parseByte(str);
        if (cls.isAssignableFrom(Short.class) && canParseShort(str)) return Short.parseShort(str);
        if (cls.isAssignableFrom(Integer.class) && canParseInt(str)) return Integer.parseInt(str);
        if (cls.isAssignableFrom(Float.class) && canParseFloat(str)) return Float.parseFloat(str);
        if (cls.isAssignableFrom(Double.class) && canParseDouble(str)) return Double.parseDouble(str);
        if (cls.isAssignableFrom(Long.class) && canParseLong(str)) return Long.parseLong(str);
        if (cls.isAssignableFrom(Character.class)) return str.toCharArray()[0];
        return str;
    }

    public static Object[] parseArr(String str, Object obj) throws Exception {
        if (obj == null) return null;
        Class<?> cls = obj.getClass().componentType();
        String[] arr = str.substring(1, str.length() - 1).split(",");
        Object ele = cls.getDeclaredConstructor().newInstance();
        Object o = Array.newInstance(cls, arr.length);
        for (int i = 0; i < arr.length; i++)
            Array.set(o, i, parseSth(arr[i], ele));
        return (Object[]) o;
    }

    public static Object parseSthOrArr(String str, Object obj) throws Exception {
        if (obj == null) return null;
        if (obj.getClass().isArray())
            return parseArr(str, obj);
        return parseSth(str, obj);
    }

    public static String camelCase2SentenceCase(String str) {
        if (str == null)
            return null;
        str = str.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"),
                " ");
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String colorRemove(String str) {
        // Reference for This Holy Magic
        // NetServer.java: fixName(), checkColor()
        if (str == null) return null;
        if (str.equals("[") || str.equals("]"))
            return "";

        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '[' && i != str.length() - 1 && str.charAt(i + 1) != '[' && (i == 0 || str.charAt(i - 1) != '[')) {
                String prev = str.substring(0, i);
                String next = str.substring(i);
                String result = next;
                for (int j = 1; j < next.length(); j++)
                    if (next.charAt(j) == ']') {
                        String color = next.substring(1, j);
                        if (Colors.get(color.toUpperCase()) != null || Colors.get(color.toLowerCase()) != null) {
                            result = next.substring(j + 1);
                            break;
                        } else
                            try {
                                Color.valueOf(color);
                                result = next.substring(j + 1);
                                break;
                            } catch (Exception e) {
                                result = next;
                                break;
                            }
                    }

                str = prev + result;
            }
        // Holy Magic Ends Here.

        // Remove "[" at the end if any.
        if (str.endsWith("["))
            str = str.substring(0, str.length() - 1);
        return str;
    }
}
