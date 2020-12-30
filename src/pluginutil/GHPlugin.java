package pluginutil;

import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.mod.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import static pluginutil.PluginUtil.*;

public class GHPlugin extends Plugin {

    protected String
            PLUGIN = "DiscordBot",
            CMD = "killall",
            DESC = "Vote for Killing All Troops.",
            CONFIG_DIR = Vars.modDirectory + "/" + PLUGIN + ".cfg",
            VERSION = "1.0";

    protected final String plgn = "[scarlet]", pass = "[green]", announce = "[orange]", accent = "[accent]", clean = "[white]", ignore = "[lightgray]";
    protected boolean mode;

    //called when game initializes
    public GHPlugin(){
        try {
            read();
        } catch (Exception e){
            mode = false;
            log(1, "An Error has occurred. Plugin is turned off.");
            e.printStackTrace();
        }
    }

    //register commands that run on the server
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

    protected void send(String color, String str){
        send(color, str, null);
    }
    protected void send(String color, String str, Player player) {
        if (player == null)
            Call.sendMessage(plgn + PLUGIN + ": " + color + str);
        else
            player.sendMessage(plgn + PLUGIN + ": " + color + str);
    }

    protected void log(int mode, String str){
        switch (mode){
            case 0 -> Log.info(PLUGIN + ": " + str);
            case 1 -> Log.warn(PLUGIN + ": " + str);
        }
    }

    // o Stands for output. Bad name, I know, whatever.
    protected void o(int mode, String color, String msg, Player player, boolean server){
        if (server)
            log(mode, msg);
        else
            send(color, msg, player);
    }
    // Util Stuff

    protected String cmd(boolean prefix){
        return (prefix ? "/" : "") + CMD;
    }

    protected void write() {
        try{
            FileWriter writer = new FileWriter(CONFIG_DIR);
            writer.write("Version: " + 1.0 + "\n");
            writer.write("Mode: " + mode + "\n");
            writer.close();
            log(0, "Configs Saved");
        }catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    protected void read() {
        try{
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
            mode = true;

            write();

            log(0, "Config File Created in Directory \n[" + file.getAbsolutePath() + "].");
        }catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}
