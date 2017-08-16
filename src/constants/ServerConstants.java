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
    public static final boolean ENABLE_PIC = false;             //Pick true/false to enable or disable Pic. Delete character needs this feature ENABLED.
    public static final boolean ENABLE_PIN = false;             //Pick true/false to enable or disable Pin.
		
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
    public static final boolean USE_CUSTOM_KEYSET = true;           //Enables auto-setup of the MapleSolaxiaV2's custom keybindings when creating characters.
    public static final boolean USE_MAXRANGE_ECHO_OF_HERO = true;
    public static final boolean USE_MAXRANGE = true;                //Will send and receive packets from all events of a map, rather than those of only view range.
    public static final boolean USE_DEBUG = true;                  //Will enable some text prints on the client, oriented for debugging purposes.
    public static final boolean USE_DEBUG_SHOW_RCVD_PACKET = false; //Prints on the cmd all received packet ids.
    public static final boolean USE_DEBUG_SHOW_INFO_EQPEXP = true; //Prints on the cmd all equip exp gain info.
    public static final boolean USE_MTS = false;
    public static final boolean USE_FAMILY_SYSTEM = false;
    public static final boolean USE_DUEY = true;
    public static final boolean USE_ITEM_SORT = true;
    public static final boolean USE_ITEM_SORT_BY_NAME = false;      //Item sorting based on name rather than id.
    public static final boolean USE_PARTY_SEARCH = false;
    public static final boolean USE_AUTOBAN = false;                //Commands the server to detect infractors automatically.
    public static final boolean USE_ANOTHER_AUTOASSIGN = true;      //Based on distributing AP accordingly with higher secondary stat on equipments.
    public static final boolean USE_REFRESH_RANK_MOVE = true;
    public static final boolean USE_PERFECT_PITCH = true;	    //For lvl 30 or above, each lvlup player gains 1 perfect pitch.
    public static final boolean USE_PERMISSIVE_BUFFS = false;       //WARNING: Allows players that does not have increased certain buff-type skills to use it's effect. Used mainly on buff-cast commands, however making this active may generate a source for possible client-edited exploits.
    public static final boolean USE_ENFORCE_MDOOR_POSITION = true;  //Forces mystic door to be spawned near spawnpoints. (since things bugs out other way, and this helps players locate the door faster)
    public static final boolean USE_ERASE_UNTRADEABLE_DROP = true; //Forces flagged untradeable items to disappear when dropped.
    
    public static final int MAX_AP = 20000;                     //Max AP allotted on the auto-assigner.
    public static final int MAX_EVENT_LEVELS = 8;               //Event has different levels of rewarding system.
    public static final long BLOCK_NPC_RACE_CONDT = (long)(0.5 * 1000); //Time the player client must wait before reopening a conversation with an NPC.
    public static final long PET_LOOT_UPON_ATTACK = (long)(0.7 * 1000); //Time the pet must wait before trying to pick items up.
    
    //Dangling Items Configuration
    public static final int ITEM_EXPIRE_TIME  = 3 * 60 * 1000;  //Time before items start disappearing. Recommended to be set up to 3 minutes.
    public static final int ITEM_MONITOR_TIME = 5 * 60 * 1000;  //Interval between item monitoring task on maps, which checks for dangling item objects on the map item history.
    public static final int ITEM_LIMIT_ON_MAP = 777;            //Max number of items allowed on a map.
    
    //Some Gameplay Enhancing Configuration
    public static final boolean USE_PERFECT_SCROLLING = true;   //Scrolls doesn't use slots upon failure.
    public static final boolean USE_ENHANCED_CHSCROLL = true;   //Equips even more powerful with chaos upgrade.
    public static final boolean USE_ENHANCED_CRAFTING = true;   //Applys chaos scroll on every equip crafted.
    public static final boolean USE_ULTRA_NIMBLE_FEET = true;   //Haste-like speed & jump upgrade.
    public static final boolean USE_ULTRA_RECOVERY = true;      //Massive recovery amounts overtime.
    public static final boolean USE_ULTRA_THREE_SNAILS = true;  //Massive damage on shell toss.
    public static final boolean USE_ADD_SLOTS_BY_LEVEL = true;  //Slots are added each 20 levels.
    public static final boolean USE_ADD_RATES_BY_LEVEL = true;  //Rates are added each 20 levels.
    public static final boolean USE_STACK_COUPON_RATES = true;  //Multiple coupons effects builds up together.
    public static final boolean USE_EQUIPMNT_LVLUP_SLOTS = true;//Equips can upgrade slots at level up.
    public static final boolean USE_EQUIPMNT_LVLUP_POWER = true;//Enable more powerful stats upgrades at equip level up.
    public static final boolean USE_SPIKES_AVOID_BANISH = true; //Shoes equipped with spikes blocks mobs from banishing wearer.
    public static final boolean USE_CHAIR_EXTRAHEAL = true;     //Enable map chairs to futher recover player`s HP and MP.
    public static final int MAX_EQUIPMNT_LVLUP_STAT_GAIN = 10000; //Max stat upgrade an equipment can have on a levelup.
    public static final int MAX_EQUIPMNT_STAT = 32767;            //Max stat on an equipment by leveling up.
    public static final int USE_EQUIPMNT_LVLUP = 7;             //All equips lvlup at max level of N, set 1 to disable.
    public static final byte CHAIR_EXTRA_HEAL_HP = 70;          //Each extra heal proc increasing HP.
    public static final byte CHAIR_EXTRA_HEAL_MP = 42;          //Each extra heal proc increasing MP.
    public static final int FAME_GAIN_BY_QUEST = 4;             //Fame gain each N quest completes, set 0 to disable.
    public static final int SCROLL_CHANCE_RATE = 10;            //Number of tries for success on a scroll, set 0 for default.
    
    //Pet Auto-Pot Recovery Rates
    public static final double PET_AUTOHP_RATIO = 0.99;         //Will automatically consume potions until given ratio of the MaxHP/MaxMP is reached.
    public static final double PET_AUTOMP_RATIO = 0.99;
    
    //Dojo Configuration
    public static final boolean USE_DEADLY_DOJO = false;        //Should bosses really use 1HP,1MP attacks in dojo?
	
    //Pet Hungry Configuration
    public static final boolean PETS_NEVER_HUNGRY = false;      //If true, pets and mounts will never grow hungry.
    public static final boolean GM_PETS_NEVER_HUNGRY = true;    //If true, pets and mounts owned by GMs will never grow hungry.
	
    //Rates And Experience
    public static final int EXP_RATE = 10;
    public static final int MESO_RATE = 10;
    public static final int DROP_RATE = 10;
    public static final int BOSS_DROP_RATE = 20;
    public static final int PARTY_EXPERIENCE_MOD = 1;           //Change for event stuff.
    public static final double EQUIP_EXPERIENCE_MOD = 10.0;     //Rate for equipment exp needed, grows linearly. Set 1.0 for default (about 100~200 same-level range mobs killed to pass equip from level 1 to 2).
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
