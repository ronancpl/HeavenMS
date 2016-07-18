package constants;

import java.io.FileInputStream;
import java.util.Properties;

public class ServerConstants {

    public static short VERSION = 83;
    public static String[] WORLD_NAMES = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};

    // Login Configuration
    public static final int CHANNEL_LOAD = 100;//Players per channel
    public static final long RANKING_INTERVAL = 60 * 60 * 1000;//60 minutes, 3600000
    public static final boolean ENABLE_PIC = false;
    //Event Configuration
    public static final boolean PERFECT_PITCH = true;  //for lvl 30 or above, each lvlup player gains 1 perfect pitch.
    // IP Configuration
    public static String HOST;
    //Database Configuration
    public static String DB_URL = "";
    public static String DB_USER = "";
    public static String DB_PASS = "";
    //Other Configuration
    public static boolean JAVA_8;
    public static boolean SHUTDOWNHOOK;
    //Gameplay Configurations
    public static final boolean USE_DEBUG = true;
    public static final boolean USE_MTS = false;
    public static final boolean USE_FAMILY_SYSTEM = false;
    public static final boolean USE_DUEY = true;
    public static final boolean USE_ITEM_SORT = true;
    public static final boolean USE_ITEM_SORT_BY_NAME = false;  //item sorting based on name rather than id.
    public static final boolean USE_PARTY_SEARCH = false;
    public static final boolean USE_AUTOBAN = false;            //commands the server to detect infractors automatically.
    public static final boolean USE_ANOTHER_AUTOASSIGN = true;  //based on distributing AP accordingly with higher secondary stat on equipments.
    public static final boolean USE_REFRESH_RANK_MOVE = true;
    
    public static final int MAX_AP = 999;
    public static final long BLOCK_DUEY_RACE_COND = (long)(0.5 * 1000);
    public static final long PET_LOOT_UPON_ATTACK = (long)(0.7 * 1000);            //time the pet must wait before trying to pick items up.
    
    //Some Gameplay Enhancing Configurations
    public static final boolean USE_PERFECT_SCROLLING = true;   //scrolls doesn't use slots upon failure.
    public static final boolean USE_ENHANCED_CHSCROLL = true;   //equips even more powerful with chaos upgrade
    public static final boolean USE_ENHANCED_CRAFTING = true;   //applys chaos scroll on every equip crafted.
    public static final boolean USE_ULTRA_NIMBLE_FEET = true;   //still needs some client editing to work =/
    public static final boolean USE_ULTRA_RECOVERY = true;      //huehue another client edit
    //public static final boolean USE_ULTRA_THREE_SNAILS = true;
    public static final boolean USE_ADD_SLOTS_BY_LEVEL = true;  //slots are added each 20 levels.
    public static final boolean USE_ADD_RATES_BY_LEVEL = true;  //rates are added each 20 levels.
    public static final int FAME_GAIN_BY_QUEST = 4;  //fame gain each N quest completes, set 0 to disable.
    public static final int SCROLL_CHANCE_RATE = 10;    //number of tries for success on a scroll, set 0 for default.
    //Rates
    public static final int EXP_RATE = 10;
    public static final int MESO_RATE = 10;
    public static final int DROP_RATE = 10;
    public static final int BOSS_DROP_RATE = 20;
    public static final int PARTY_EXPERIENCE_MOD = 1; // change for event stuff
    public static final double PQ_BONUS_EXP_MOD = 0.5;
	
    public static final long EVENT_END_TIMESTAMP = 1428897600000L;
    static {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("configuration.ini"));

            //SERVER
            ServerConstants.HOST = p.getProperty("HOST");

            //SQL DATABASE
            ServerConstants.DB_URL = p.getProperty("URL");
            ServerConstants.DB_USER = p.getProperty("DB_USER");
            ServerConstants.DB_PASS = p.getProperty("DB_PASS");

            //OTHER
            ServerConstants.JAVA_8 = p.getProperty("JAVA8").equalsIgnoreCase("TRUE");
            ServerConstants.SHUTDOWNHOOK = p.getProperty("SHUTDOWNHOOK").equalsIgnoreCase("true");

        } catch (Exception e) {
            System.out.println("Failed to load configuration.ini.");
            System.exit(0);
        }
    }
}
