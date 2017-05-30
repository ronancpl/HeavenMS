package constants;

import java.io.FileInputStream;
import java.util.Properties;

public class ServerConstants {
	
    //World And Version
    public static short VERSION = 83;
    public static String[] WORLD_NAMES = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};

    //Login Configuration
    public static final int CHANNEL_LOAD = 100;                 //Max players per channel.
    
    public static final long PURGING_INTERVAL = 5 * 60 * 1000;
    public static final long RANKING_INTERVAL = 60 * 60 * 1000;	//60 minutes, 3600000.
    public static final long COUPON_INTERVAL = 60 * 60 * 1000;	//60 minutes, 3600000.
    public static final boolean ENABLE_PIC = false;             //Pick true/false to enable or disable Pic.
		
    //Ip Configuration
    public static String HOST;
	
    //Database Configuration
    public static String DB_URL = "";
    public static String DB_USER = "";
    public static String DB_PASS = "";
	
    //Other Configuration
    public static boolean JAVA_8;
    public static boolean SHUTDOWNHOOK;
	
    //Gameplay Configuration
    public static final boolean USE_CUSTOM_KEYSET = true;
    public static final boolean USE_MAXRANGE = true;        	//Will send and receive packets from all events of a map, rather than those of only view range.
    public static final boolean USE_DEBUG = true;           	//Will enable some text prints and new commands in the client oriented for debugging.
    public static final boolean USE_MTS = false;
    public static final boolean USE_FAMILY_SYSTEM = false;
    public static final boolean USE_DUEY = true;
    public static final boolean USE_ITEM_SORT = true;
    public static final boolean USE_ITEM_SORT_BY_NAME = false;  //Item sorting based on name rather than id.
    public static final boolean USE_PARTY_SEARCH = false;
    public static final boolean USE_AUTOBAN = false;            //Commands the server to detect infractors automatically.
    public static final boolean USE_ANOTHER_AUTOASSIGN = true;  //Based on distributing AP accordingly with higher secondary stat on equipments.
    public static final boolean USE_REFRESH_RANK_MOVE = true;
    public static final int MAX_AP = 999;
    public static final int MAX_EVENT_LEVELS = 8;               //Event has different levels of rewarding system.
    public static final long BLOCK_DUEY_RACE_COND = (long)(0.5 * 1000);
    public static final long PET_LOOT_UPON_ATTACK = (long)(0.7 * 1000); //Time the pet must wait before trying to pick items up.
    public static final boolean PERFECT_PITCH = true;	        //For lvl 30 or above, each lvlup player gains 1 perfect pitch.
    
    //Some Gameplay Enhancing Configuration
    public static final boolean USE_PERFECT_SCROLLING = true;   //Scrolls doesn't use slots upon failure.
    public static final boolean USE_ENHANCED_CHSCROLL = true;   //Equips even more powerful with chaos upgrade.
    public static final boolean USE_ENHANCED_CRAFTING = true;   //Applys chaos scroll on every equip crafted.
    public static final boolean USE_ULTRA_NIMBLE_FEET = true;   //Still needs some client editing to work.
    public static final boolean USE_ULTRA_RECOVERY = true;      //Huehue another client edit.
    //Public static final boolean USE_ULTRA_THREE_SNAILS = true;
    public static final boolean USE_ADD_SLOTS_BY_LEVEL = true;  //Slots are added each 20 levels.
    public static final boolean USE_ADD_RATES_BY_LEVEL = true;  //Rates are added each 20 levels.
    public static final boolean USE_STACK_COUPON_RATES = true;  //Multiple coupons effects builds up together.
    public static final int USE_EQUIPMNT_LVLUP = 7;             //Nope, not working yet. //all equips lvlup at max level as N, set 0 to disable.
    public static final int FAME_GAIN_BY_QUEST = 4;             //Fame gain each N quest completes, set 0 to disable.
    public static final int SCROLL_CHANCE_RATE = 10;            //Number of tries for success on a scroll, set 0 for default.
    
    //Pet Auto-Pot Recovery Rates
    public static final double PET_AUTOHP_RATIO = 0.99;         //Will automatically consume potions until given ratio of the MaxHP/MaxMP is reached.
    public static final double PET_AUTOMP_RATIO = 0.99;
    
    //Dojo Configuration
    public static final boolean USE_DEADLY_DOJO = false;        //Should bosses really use 1HP,1MP attacks in dojo?
	
    //Pet Hungry Configuration
    public static final boolean PETS_NEVER_HUNGRY = false;      //If true, pets will never grow hungry.
    public static final boolean GM_PETS_NEVER_HUNGRY = true;    //If true, pets of GMs will never grow hungry.
	
    //Rates And Experience
    public static final int EXP_RATE = 10;
    public static final int MESO_RATE = 10;
    public static final int DROP_RATE = 10;
    public static final int BOSS_DROP_RATE = 20;
    public static final int PARTY_EXPERIENCE_MOD = 1;           //Change for event stuff.
    public static final double PQ_BONUS_EXP_MOD = 0.5;
    
    //Event End Timestamp
    public static final long EVENT_END_TIMESTAMP = 1428897600000L;
	
    //Properties
    static {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("configuration.ini"));

            //Server Host
            ServerConstants.HOST = p.getProperty("HOST");

            //Sql Database
            ServerConstants.DB_URL = p.getProperty("URL");
            ServerConstants.DB_USER = p.getProperty("DB_USER");
            ServerConstants.DB_PASS = p.getProperty("DB_PASS");

            //java8 And Shutdownhook
            ServerConstants.JAVA_8 = p.getProperty("JAVA8").equalsIgnoreCase("TRUE");
            ServerConstants.SHUTDOWNHOOK = p.getProperty("SHUTDOWNHOOK").equalsIgnoreCase("true");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load configuration.ini.");
            System.exit(0);
        }
    }
}
