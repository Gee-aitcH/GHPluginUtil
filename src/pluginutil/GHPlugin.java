package pluginutil;

import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.mod.*;

public class GHPlugin extends Plugin {

    // Modify PLUGIN, CMD, DESC and you will be fine.
    protected String
            PLUGIN = "ExamplePluginChangeThisPlease",
            CMD = "example_cmd_change_this_please",
            DESC = "Example Description Change This Please",
            CONFIG_DIR = Vars.modDirectory + "/" + PLUGIN + ".cfg",
            VERSION = "1.0";

    protected final String plgn = "[scarlet]", pass = "[green]", announce = "[orange]", accent = "[accent]", clean = "[white]", ignore = "[lightgray]";
    protected boolean mode;

    //called when game initializes
    public void init(){
        try {
            read();
        } catch (Exception e){
            mode = false;
            log(1, "An Error has occurred. Plugin is turned off.");
            e.printStackTrace();
        }
    }

    //register commands... no need to override these if you will redirect all commands to commandHandler and use my format.
    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register(CMD, "[arg(s)...]", DESC, args -> commandHandler(args[0].split(" "), null, true));
    }
    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register(CMD, "[arg(s)...]", DESC, (args, player) -> commandHandler(args[0].split(" "), player, false));
    }

    protected void commandHandler(String[] arg, Player player, boolean s){
    }

    // Send message to all players
    protected void send(String color, String msg){
        send(color, msg, null);
    }
    // Send message to certain player
    protected void send(String color, String msg, Player player) {
        if (player == null)
            Call.sendMessage(plgn + PLUGIN + ": " + color + msg);
        else
            player.sendMessage(plgn + PLUGIN + ": " + color + msg);
    }

    // Send message to console, 0: info, 1: warn
    protected void log(int mode, String msg){
        switch (mode){
            case 0 -> Log.info(PLUGIN + ": " + msg);
            case 1 -> Log.warn(PLUGIN + ": " + msg);
        }
    }

    // o Stands for output. Bad name, I know, whatever.
    // Send message to all players, certain player or console.
    protected void o(int mode, String color, String msg, Player player, boolean server){
        if (server)
            log(mode, msg);
        else
            send(color, msg, player);
    }

    // Return '/cmd' or 'cmd', depended by whether if it is used to print in chat or console.
    protected String cmd(boolean prefix){
        return (prefix ? "/" : "") + CMD;
    }

    // Write File
    protected void write() {
        /*try{
            FileWriter writer = new FileWriter(CONFIG_DIR);
            writer.write("Version: " + 1.0 + "\n");
            writer.write("Mode: " + mode + "\n");
            writer.close();
            log(0, "Configs Saved");
        }catch (Exception ee) {
            ee.printStackTrace();
        }*/
    }

    // Read file
    protected void read() {
        /*try{
            File file = new File(CONFIG_DIR);
            boolean hasSave = !file.createNewFile();
            Scanner reader = new Scanner(file);
            // if has save and version of save match plugin version.
            if (hasSave && reader.nextLine().equals("Version: " + VERSION)){

                mode = parseBoolean(r(reader));

                reader.close();
                log(0, "Configs Loaded");
                return;
            }
            if(hasSave){
                file.renameTo(new File(CONFIG_DIR + ".outdated"));
                FileWriter writer = new FileWriter(CONFIG_DIR + ".outdated");
                reader = new Scanner(file);
                while (reader.hasNextLine())
                    writer.write(reader.nextLine() + "\n");
                writer.close();
                log(0, "Outdated Config File Found. Moved to Directory \n[" + file.getAbsolutePath() + "].\n" +
                        "Outdated Config will be overwrite when a newer Outdated Config is detected.");
            }

            // Default values
            defConfig();

            write();

            log(0, "Config File Created in Directory \n[" + file.getAbsolutePath() + "].");
        }catch (Exception ee) {
            defConfig();
            ee.printStackTrace();
        }*/
    }

    // Default configs here
    protected void defConfig(){
        //mode = true;
    }
}
