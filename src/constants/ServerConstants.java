package constants;

import java.io.FileInputStream;
import java.util.Properties;

public class ServerConstants {
    //Thread Tracker Configuration
    public static final boolean USE_THREAD_TRACKER = true;      //[SEVERE] This deadlock auditing thing will bloat the memory as fast as the time frame one takes to lose track of a raindrop on a tempesting day. Only for debugging purposes.
    
    //Database Configuration
    public static String DB_URL = "";
    public static String DB_USER = "";
    public static String DB_PASS = "";
    public static final boolean DB_CONNECTION_POOL = true;      //Installs a connection pool to hub DB connections. Set false to default.
    
    //Server Version
    public static short VERSION = 83;

    //Login Configuration
    public static final int WLDLIST_SIZE = 21;                  //Max possible worlds on the server.
    public static final int CHANNEL_SIZE = 20;                  //Max possible channels per world (which is 20, based on the channel list on login phase).
    public static final int CHANNEL_LOAD = 100;                 //Max players per channel (limit actually used to calculate the World server capacity).
    public static final int CHANNEL_LOCKS = 20;                 //Total number of structure management locks each channel has.
    
    public static final long RESPAWN_INTERVAL = 10 * 1000;	//10 seconds, 10000.
    public static final long PURGING_INTERVAL = 5 * 60 * 1000;
    public static final long RANKING_INTERVAL = 60 * 60 * 1000;	//60 minutes, 3600000.
    public static final long  COUPON_INTERVAL = 60 * 60 * 1000;	//60 minutes, 3600000.
    public static final long  UPDATE_INTERVAL = 777;            //Dictates the frequency on which the "centralized server time" is updated.
    
    public static final boolean ENABLE_PIC = false;             //Pick true/false to enable or disable Pic. Delete character requires PIC available.
    public static final boolean ENABLE_PIN = false;             //Pick true/false to enable or disable Pin.
    
    public static final int BYPASS_PIC_EXPIRATION = 20;         //Enables PIC bypass, which will remain active for that account by that client machine for N minutes. Set 0 to disable.
    public static final int BYPASS_PIN_EXPIRATION = 15;         //Enables PIN bypass, which will remain active for that account by that client machine for N minutes. Set 0 to disable.
    
    public static final boolean AUTOMATIC_REGISTER = true;      //Automatically register players when they login with a nonexistent username.
    public static final boolean BCRYPT_MIGRATION = true;        //Performs a migration from old SHA-1 and SHA-512 password to bcrypt.
    public static final boolean COLLECTIVE_CHARSLOT = false;    //Available character slots are contabilized globally rather than per world server.
    public static final boolean DETERRED_MULTICLIENT = false;   //Enables multi-client and suspicious remote IP detection on the login system.
    
    //Besides blocking logging in with several client sessions on the same machine, this also blocks suspicious login attempts for players that tries to login on an account using several diferent remote addresses.
    
    //Multiclient Coordinator Configuration
    public static final int MAX_ALLOWED_ACCOUNT_HWID = 4;       //Allows up to N concurrent HWID's for an account. HWID's remains linked to an account longer the more times it's used to login.
    public static final int MAX_ACCOUNT_LOGIN_ATTEMPT = 15;     //After N tries on an account, login on that account gets disabled for a short period.
    public static final int LOGIN_ATTEMPT_DURATION = 120;       //Period in seconds the login attempt remains registered on the system.
    
    //Ip Configuration
    public static String HOST;
    public static boolean LOCALSERVER;

    //Other Configuration
    public static boolean JAVA_8;
    public static boolean SHUTDOWNHOOK;
    // JAVA_8: every static function in AbstractPlayerInteraction are to be made non-static, and code comment sections uncommented after enabling this functionality.
    
    
    //Server Flags
    public static final boolean USE_CUSTOM_KEYSET = true;           //Enables auto-setup of the HeavenMS's custom keybindings when creating characters.
    public static final boolean USE_DEBUG = false;                  //Will enable some text prints on the client, oriented for debugging purposes.
    public static final boolean USE_DEBUG_SHOW_INFO_EQPEXP = false; //Prints on the cmd all equip exp gain info.
    public static       boolean USE_DEBUG_SHOW_RCVD_PACKET = false; //Prints on the cmd all received packet ids.
    public static       boolean USE_DEBUG_SHOW_RCVD_MVLIFE = false; //Prints on the cmd all received move life content.
    public static final boolean USE_DEBUG_SHOW_PACKET = false;
    public static       boolean USE_SUPPLY_RATE_COUPONS = true;     //Allows rate coupons to be sold through the Cash Shop.
    public static final boolean USE_IP_VALIDATION = true;           //Enables IP checking when logging in.
    
    public static final boolean USE_MAXRANGE = true;                //Will send and receive packets from all events on a map, rather than those of only view range.
    public static final boolean USE_MAXRANGE_ECHO_OF_HERO = true;
    public static final boolean USE_MTS = false;
    public static final boolean USE_CPQ = true;                     //Renders the CPQ available or not.
    public static final boolean USE_AUTOHIDE_GM = false;            //When enabled, GMs are automatically hidden when joining. Thanks to Steven Deblois (steven1152).
    public static final boolean USE_BUYBACK_SYSTEM = true;          //Enables the HeavenMS-builtin buyback system, to be used by dead players when clicking the MTS button.
    public static final boolean USE_FIXED_RATIO_HPMP_UPDATE = true; //Enables the HeavenMS-builtin HPMP update based on the current pool to max pool ratio.
    public static final boolean USE_FAMILY_SYSTEM = false;
    public static final boolean USE_DUEY = true;
    public static final boolean USE_RANDOMIZE_HPMP_GAIN = true;     //Enables randomizing on MaxHP/MaxMP gains and INT accounting for the MaxMP gain.
    public static final boolean USE_STORAGE_ITEM_SORT = true;       //Enables storage "Arrange Items" feature.
    public static final boolean USE_ITEM_SORT = true;               //Enables inventory "Item Sort/Merge" feature.
    public static final boolean USE_ITEM_SORT_BY_NAME = false;      //Item sorting based on name rather than id.
    public static final boolean USE_PARTY_SEARCH = false;
    public static final boolean USE_PARTY_FOR_STARTERS = true;      //Players level 10 or below can create/invite other players on the given level range.
    public static final boolean USE_AUTOASSIGN_STARTERS_AP = false; //Beginners level 10 or below have their AP autoassigned (they can't choose to levelup a stat). Set true ONLY if the localhost doesn't support AP assigning for beginners level 10 or below.
    public static final boolean USE_AUTOASSIGN_SECONDARY_CAP = true;//Prevents AP autoassign from spending on secondary stats after the player class' cap (defined on the autoassign handler) has been reached.
    public static final boolean USE_AUTOBAN = false;                //Commands the server to detect infractors automatically.
    public static final boolean USE_AUTOBAN_LOG = true;             //Log autoban related messages. Still logs even with USE_AUTOBAN disabled.
    public static final boolean USE_AUTOSAVE = true;                //Enables server autosaving feature (saves characters to DB each 1 hour).
    public static final boolean USE_SERVER_AUTOASSIGNER = true;     //HeavenMS-builtin autoassigner, uses algorithm based on distributing AP accordingly with required secondary stat on equipments.
    public static final boolean USE_REFRESH_RANK_MOVE = true;
    public static final boolean USE_ENFORCE_ADMIN_ACCOUNT = false;  //Forces accounts having GM characters to be treated as a "GM account" by the client (localhost). Some of the GM account perks is the ability to FLY, but unable to TRADE.
    public static final boolean USE_ENFORCE_NOVICE_EXPRATE = false; //Hardsets experience rate 1x for beginners level 10 or under. Ideal for roaming on novice areas without caring too much about losing some stats.
    public static final boolean USE_ENFORCE_HPMP_SWAP = false;      //Forces players to reuse stats (via AP Resetting) located on HP/MP pool only inside the HP/MP stats.
    public static final boolean USE_ENFORCE_MOB_LEVEL_RANGE = true; //Players N levels below the killed mob will gain no experience from defeating it.
    public static final boolean USE_ENFORCE_JOB_LEVEL_RANGE = false;//Caps the player level on the minimum required to advance their current jobs.
    public static final boolean USE_ENFORCE_JOB_SP_RANGE = false;   //Caps the player SP level on the total obtainable by their current jobs. After changing jobs, missing SP will be retrieved.
    public static final boolean USE_ENFORCE_ITEM_SUGGESTION = false;//Forces the Owl of Minerva and the Cash Shop to always display the defined item array instead of those featured by the players.
    public static final boolean USE_ENFORCE_UNMERCHABLE_CASH = true;//Forces players to not sell CASH items via merchants.
    public static final boolean USE_ENFORCE_UNMERCHABLE_PET = true; //Forces players to not sell pets via merchants. (since non-named pets gets dirty name and other possible DB-related issues)
    public static final boolean USE_ENFORCE_MERCHANT_SAVE = true;   //Forces automatic DB save on merchant owners, at every item movement on shop.
    public static final boolean USE_ENFORCE_MDOOR_POSITION = false; //Forces mystic door to be spawned near spawnpoints.
    public static final boolean USE_SPAWN_LOOT_ON_ANIMATION = false;//Makes loot appear some time after the mob has been killed (following the mob death animation, instead of instantly).
    public static final boolean USE_SPAWN_RELEVANT_LOOT = true;     //Forces to only spawn loots that are collectable by the player or any of their party members.
    public static final boolean USE_ERASE_PERMIT_ON_OPENSHOP = true;//Forces "shop permit" item to be consumed when player deploy his/her player shop.
    public static final boolean USE_ERASE_UNTRADEABLE_DROP = true;  //Forces flagged untradeable items to disappear when dropped.
    public static final boolean USE_ERASE_PET_ON_EXPIRATION = false;//Forces pets to be removed from inventory when expire time comes, rather than converting it to a doll.
    public static final boolean USE_BUFF_MOST_SIGNIFICANT = true;   //When applying buffs, the player will stick with the highest stat boost among the listed, rather than overwriting stats.
    public static final boolean USE_BUFF_EVERLASTING = false;       //Every applied buff on players holds expiration time so high it'd be considered permanent. Suggestion thanks to Vcoc.
    public static final boolean USE_MULTIPLE_SAME_EQUIP_DROP = true;//Enables multiple drops by mobs of the same equipment, number of possible drops based on the quantities provided at the drop data.
    public static final boolean USE_BANISHABLE_TOWN_SCROLL = true;  //Enables town scrolls to act as if it's a "player banish", rendering the antibanish scroll effect available.
    public static final boolean USE_ENABLE_FULL_RESPAWN = true;     //At respawn task, always respawn missing mobs when they're available. Spawn count doesn't depend on how many players are currently there.
    public static final boolean USE_ENABLE_CHAT_LOG = false;        //Write in-game chat to log
    public static final boolean USE_REBIRTH_SYSTEM = false;         //Flag to enable/disable rebirth system
    public static final boolean USE_MAP_OWNERSHIP_SYSTEM = true;    //Flag to enable/disable map ownership system
    public static final boolean USE_FISHING_SYSTEM = true;          //Flag to enable/disable fishing system
    public static final boolean USE_NPCS_SCRIPTABLE = true;         //Flag to enable/disable serverside predefined script NPCs.
    
    //Events/PQs Configuration
    public static final boolean USE_OLD_GMS_STYLED_PQ_NPCS = true;  //Enables PQ NPCs with similar behaviour to old GMS style, that skips info about the PQs and immediately tries to register the party in.
    public static final boolean USE_ENABLE_SOLO_EXPEDITIONS = true; //Enables start expeditions with any number of players. This will also bypass all the Zakum prequest.
    public static final boolean USE_ENABLE_RECALL_EVENT = true;     //Enables a disconnected player to reaccess the last event instance they were in before logging out. Recall only works if the event isn't cleared or disposed yet. Suggestion thanks to Alisson (Goukken).
    
    //Announcement Configuration
    public static final boolean USE_ANNOUNCE_SHOPITEMSOLD = false;  //Automatic message sent to owner when an item from the Player Shop or Hired Merchant is sold.
    public static final boolean USE_ANNOUNCE_CHANGEJOB = false;     //Automatic message sent to acquantainces when changing jobs.
    
    //Cash Shop Configuration
    public static final boolean USE_JOINT_CASHSHOP_INVENTORY = true;//Enables usage of a same cash shop inventory for explorers, cygnus and legends. Items from exclusive cash shop inventories won't show up on the shared inventory, though.
    public static final boolean USE_CLEAR_OUTDATED_COUPONS = true;  //Enables deletion of older code coupon registry from the DB, freeing so-long irrelevant data.
    
    //Maker Configuration
    public static final boolean USE_MAKER_PERMISSIVE_ATKUP = true;  //Allows players to use attack-based strengthening gems on non-weapon items.
    public static final boolean USE_MAKER_FEE_HEURISTICS = true;    //Apply compiled values for stimulants and reagents into the Maker fee calculations (max error revolves around 50k mesos). Set false to use basic constant values instead (results are never higher than requested by the client-side).
    
    //Custom Configuration
    public static final boolean USE_ENABLE_CUSTOM_NPC_SCRIPT = true;//Enables usage of custom HeavenMS NPC scripts (Agent E, Coco, etc). Will not disable Abdula (it's actually useful for the gameplay), quests or NPC shops.
    public static final boolean USE_STARTER_MERGE = false;          //Allows any players to use the Equipment Merge custom mechanic (as opposed to the high-level, Maker lv3 requisites).
    
    //Commands Configuration
    public static final boolean BLOCK_GENERATE_CASH_ITEM = false;   //Prevents creation of cash items with the item/drop command.
    public static final boolean USE_WHOLE_SERVER_RANKING = false;   //Enables a ranking pool made from every character registered on the server for the "ranks" command, instead of separated by worlds.
    
    //Server Rates And Experience
    public static final int EXP_RATE = 10;                          //NOTE: World-specific rates within "world.ini" OVERRIDES the default rates from here.
    public static final int MESO_RATE = 10;
    public static final int DROP_RATE = 10;
    public static final int BOSS_DROP_RATE = 10;                    //NOTE: Boss drop rate OVERRIDES common drop rate, for bosses-only.
    public static final int QUEST_RATE = 5;                         //Multiplier for Exp & Meso gains when completing a quest. Only available when USE_QUEST_RATE is true. Stacks with server Exp & Meso rates.
    public static final int FISHING_RATE = 10;                      //Multiplier for success likelihood on meso thrown during fishing.
    public static final int TRAVEL_RATE = 10;                       //Means of transportation rides/departs using 1/N of the default time.
    
    public static final double EQUIP_EXP_RATE = 1.0;                //Rate for equipment exp gain, grows linearly. Set 1.0 for default (about 100~200 same-level range mobs killed to pass equip from level 1 to 2).
    public static final float PARTY_BONUS_EXP_RATE = 1.0f;          //Rate for the party exp bonus reward.
    public static final double PQ_BONUS_EXP_RATE = 0.5;             //Rate for the PQ exp reward.
    
    //Miscellaneous Configuration
    public static String TIMEZONE = "GMT-3";
    public static boolean USE_DISPLAY_NUMBERS_WITH_COMMA = true;        //Enforce comma on displayed strings (use this when USE_UNITPRICE_WITH_COMMA is active and you still want to display comma-separated values).
    public static boolean USE_UNITPRICE_WITH_COMMA = true;              //Set this accordingly with the layout of the unitPrices on Item.wz XML's, whether it's using commas or dots to represent fractions.
    public static final byte MIN_UNDERLEVEL_TO_EXP_GAIN = 20;           //Characters are unable to get EXP from a mob if their level are under this threshold, only if "USE_ENFORCE_MOB_LEVEL_RANGE" is enabled. For bosses, this attribute is doubled.
    public static final byte MIN_RANGELEVEL_TO_EXP_LEECH = 40;          //Characters are unable to leech EXP from party member kills whose level difference are past this limit.
    public static final byte MAX_MONITORED_BUFFSTATS = 5;               //Limits accounting for "dormant" buff effects, that should take place when stronger stat buffs expires.
    public static final int MAX_AP = 32767;                             //Max AP allotted on the auto-assigner.
    public static final int MAX_EVENT_LEVELS = 8;                       //Event has different levels of rewarding system.
    public static final long BLOCK_NPC_RACE_CONDT = (long)(0.5 * 1000); //Time the player client must wait before reopening a conversation with an NPC.
    public static final long PET_LOOT_UPON_ATTACK = (long)(0.7 * 1000); //Time the pet must wait before trying to pick items up.
    public static final int TOT_MOB_QUEST_REQUIREMENT = 77;             //Overwrites old 999-mobs requirement for the ToT questline with new requirement value, set 0 for default.
    public static final int MOB_REACTOR_REFRESH_TIME = 30 * 1000;       //Overwrites refresh time for those reactors oriented to inflict damage to bosses (Ice Queen, Riche), set 0 for default.
    public static final int PARTY_SEARCH_REENTRY_LIMIT = 10;            //Max amount of times a party leader is allowed to persist on the Party Search before entry expiration (thus needing to manually restart the Party Search to be able to search for members).
    
    //Dangling Items/Locks Configuration
    public static final int ITEM_EXPIRE_TIME  = 3 * 60 * 1000;  //Time before items start disappearing. Recommended to be set up to 3 minutes.
    public static final int KITE_EXPIRE_TIME  = 60 * 60 * 1000; //Time before kites (cash item) disappears.
    public static final int ITEM_MONITOR_TIME = 5 * 60 * 1000;  //Interval between item monitoring tasks on maps, which checks for dangling (null) item objects on the map item history.
    public static final int LOCK_MONITOR_TIME = 30 * 1000;      //Waiting time for a lock to be released. If it reaches timeout, a critical server deadlock has made present.
    
    //Map Monitor Configuration
    public static final int ITEM_EXPIRE_CHECK = 10 * 1000;      //Interval between item expiring tasks on maps, which checks and makes disappear expired items.
    public static final int ITEM_LIMIT_ON_MAP = 200;            //Max number of items allowed on a map.
    public static final int MAP_VISITED_SIZE = 5;               //Max length for last mapids visited by a player. This is used to recover and update drops on these maps accordingly with player actions.
    public static final int MAP_DAMAGE_OVERTIME_INTERVAL = 5000;//Interval in milliseconds between map environment damage (e.g. El Nath and Aqua Road surrondings).
    
    //Channel Mob Disease Monitor Configuration
    public static final int MOB_STATUS_MONITOR_PROC = 200;      //Frequency in milliseconds between each proc on the mob disease monitor schedule.
    public static final int MOB_STATUS_MONITOR_LIFE = 84;       //Idle proc count the mob disease monitor is allowed to be there before closing it due to inactivity.
    public static final int MOB_STATUS_AGGRO_PERSISTENCE = 2;   //Idle proc count on aggro update for a mob to keep following the current controller, given him/her is the leading damage dealer.
    public static final int MOB_STATUS_AGGRO_INTERVAL = 5000;   //Interval in milliseconds between aggro logistics update.
    
    //Some Gameplay Enhancing Configurations
    //Scroll Configuration
    public static final boolean USE_PERFECT_GM_SCROLL = true;   //Scrolls from GMs never uses up slots nor fails.
    public static final boolean USE_PERFECT_SCROLLING = true;   //Scrolls doesn't use slots upon failure.
    public static final boolean USE_ENHANCED_CHSCROLL = true;   //Equips even more powerful with chaos upgrade.
    public static final boolean USE_ENHANCED_CRAFTING = true;   //Apply chaos scroll on every equip crafted.
    public static final boolean USE_ENHANCED_CLNSLATE = true;   //Clean slates can be applied to recover successfully used slots as well.
    public static final int SCROLL_CHANCE_RATE = 10;            //Number of rolls for success on a scroll, set 1 for default.
    public static final int CHSCROLL_STAT_RATE = 3;             //Number of rolls of stat upgrade on a successfully applied chaos scroll, set 1 for default.
    public static final int CHSCROLL_STAT_RANGE = 6;            //Stat upgrade range (-N, N) on chaos scrolls.
    
    //Beginner Skills Configuration
    public static final boolean USE_ULTRA_NIMBLE_FEET = true;   //Massive speed & jump upgrade.
    public static final boolean USE_ULTRA_RECOVERY = true;      //Massive recovery amounts overtime.
    public static final boolean USE_ULTRA_THREE_SNAILS = true;  //Massive damage on shell toss.
    
    //Other Skills Configuration
    public static final boolean USE_FULL_ARAN_SKILLSET = false; //Enables starter availability to all Aran job skills. Suggestion thanks to Masterrulax.
    public static final boolean USE_FAST_REUSE_HERO_WILL = true;//Greatly reduce cooldown on Hero's Will.
    public static final boolean USE_ANTI_IMMUNITY_CRASH = true; //Crash skills additionally removes the mob's invincibility buffs. Suggestion thanks to Celestial.
    public static final boolean USE_UNDISPEL_HOLY_SHIELD = true;//Holy shield buff also prevents players from suffering dispel from mobs.
    
    //Character Configuration
    public static final boolean USE_ADD_SLOTS_BY_LEVEL = true;  //Slots are added each 20 levels.
    public static final boolean USE_ADD_RATES_BY_LEVEL = true;  //Rates are added each 20 levels.
    public static final boolean USE_STACK_COUPON_RATES = false; //Multiple coupons effects builds up together.
    public static final boolean USE_PERFECT_PITCH = true;       //For lvl 30 or above, each lvlup grants player 1 perfect pitch.
    
    //Quest Configuration
    public static final boolean USE_QUEST_RATE = false;         //Exp/Meso gained by quests uses fixed server exp/meso rate times quest rate as multiplier, instead of player rates.
    
    //Quest Points Configuration
    public static final int QUEST_POINT_REPEATABLE_INTERVAL = 24;//Minimum interval between repeatable quest completions for quest points to be awarded.
    public static final int QUEST_POINT_REQUIREMENT = 16;        //Exchange factor between N quest points to +1 fame, set 0 to disable the entire quest point mechanism.
    public static final int QUEST_POINT_PER_QUEST_COMPLETE = 4;  //Each completed quest awards N quest points, set 0 to disable.
    public static final int QUEST_POINT_PER_EVENT_CLEAR = 1;     //Each completed event instance awards N quest points, set 0 to disable.
    
    //Guild Configuration
    public static final int CREATE_GUILD_MIN_PARTNERS = 6;       //Minimum number of members on Guild Headquarters to establish a new guild.
    public static final int CREATE_GUILD_COST = 1500000;
    public static final int CHANGE_EMBLEM_COST = 5000000;
    public static final int EXPAND_GUILD_BASE_COST = 500000;
    public static final int EXPAND_GUILD_TIER_COST = 1000000;
    public static final int EXPAND_GUILD_MAX_COST = 5000000;

    //Equipment Configuration
    public static final boolean USE_EQUIPMNT_LVLUP_SLOTS = true;//Equips can upgrade slots at level up.
    public static final boolean USE_EQUIPMNT_LVLUP_POWER = true;//Enable more powerful stat upgrades at equip level up.
    public static final boolean USE_EQUIPMNT_LVLUP_CASH = true; //Enable equip leveling up on cash equipments as well.
    public static final boolean USE_SPIKES_AVOID_BANISH = true; //Shoes equipped with spikes prevents mobs from banishing wearer.
    public static final int MAX_EQUIPMNT_LVLUP_STAT_UP = 10000; //Max stat upgrade an equipment can have on a levelup.
    public static final int MAX_EQUIPMNT_STAT = 32767;          //Max stat on an equipment by leveling up.
    public static final int USE_EQUIPMNT_LVLUP = 7;             //All equips lvlup at max level of N, set 1 to disable.
    
    //Map-Chair Configuration
    public static final boolean USE_CHAIR_EXTRAHEAL = true;     //Enable map chairs to further recover player's HP and MP (player must have the Chair Mastery skill).
    public static final byte CHAIR_EXTRA_HEAL_MULTIPLIER = 10;  //Due to only being able to be send up-to-255 heal values, values being actually updated is the one displayed times this.
    public static final int CHAIR_EXTRA_HEAL_MAX_DELAY = 21;    //Players are expected to recover fully after using this skill for N seconds.
    
    //Player NPC Configuration
    public static final int PLAYERNPC_INITIAL_X = 262;          //Map frame width for putting PlayerNPCs.
    public static final int PLAYERNPC_INITIAL_Y = 262;          //Map frame height for putting PlayerNPCs.
    public static final int PLAYERNPC_AREA_X = 320;             //Initial width gap between PlayerNPCs.
    public static final int PLAYERNPC_AREA_Y = 160;             //Initial height gap between PlayerNPCs.
    public static final int PLAYERNPC_AREA_STEPS = 4;           //Max number of times gap is shortened to comport PlayerNPCs.
    public static final boolean PLAYERNPC_ORGANIZE_AREA = true; //Automatically rearranges PlayerNPCs on the map if there is no space set the new NPC. Current distance gap between NPCs is decreased to solve this issue.
    public static final boolean PLAYERNPC_AUTODEPLOY = true;    //Makes PlayerNPC automatically deployed on the Hall of Fame at the instant one reaches max level. If false, eligible players must talk to 1st job instructor to deploy a NPC.
    
    //Pet Auto-Pot Configuration
    public static final boolean USE_COMPULSORY_AUTOPOT = true;  //Pets will consume as many potions as needed to fulfill the AUTOHP/MP ratio threshold.
    public static final boolean USE_EQUIPS_ON_AUTOPOT = true;   //Player MaxHP and MaxMP check values on autopot handler will be updated by the HP/MP bonuses on equipped items.
    public static final double PET_AUTOHP_RATIO = 0.99;         //Will automatically consume potions until given ratio of the MaxHP/MaxMP is reached.
    public static final double PET_AUTOMP_RATIO = 0.99;
    
    //Pet & Mount Configuration
    public static final byte PET_EXHAUST_COUNT = 3;             //Number of proc counts (1 per minute) on the exhaust schedule for fullness.
    public static final byte MOUNT_EXHAUST_COUNT = 1;           //Number of proc counts (1 per minute) on the exhaust schedule for tiredness.
    
    //Pet Hunger Configuration
    public static final boolean PETS_NEVER_HUNGRY = false;      //If true, pets and mounts will never grow hungry.
    public static final boolean GM_PETS_NEVER_HUNGRY = true;    //If true, pets and mounts owned by GMs will never grow hungry.
    
    //Event Configuration
    public static final int EVENT_MAX_GUILD_QUEUE = 10;         //Max number of guilds in queue for GPQ.
    public static final long EVENT_LOBBY_DELAY = 10;            //Cooldown duration in seconds before reopening an event lobby.
    
    //Dojo Configuration
    public static final boolean USE_FAST_DOJO_UPGRADE = true;   //Reduced Dojo training points amount required for a belt upgrade.
    public static final boolean USE_DEADLY_DOJO = false;        //Should bosses really use 1HP,1MP attacks in dojo?
    public static final int DOJO_ENERGY_ATK = 100;              //Dojo energy gain when deal attack
    public static final int DOJO_ENERGY_DMG =  20;              //Dojo energy gain when recv attack
    
    //Wedding Configuration
    public static final int WEDDING_RESERVATION_DELAY = 3;      //Minimum idle slots before processing a wedding reservation.
    public static final int WEDDING_RESERVATION_TIMEOUT = 10;   //Limit time in minutes for the couple to show up before cancelling the wedding reservation.
    public static final int WEDDING_RESERVATION_INTERVAL = 60;  //Time between wedding starts in minutes.
    public static final int WEDDING_BLESS_EXP = 30000;          //Exp gained per bless count.
    public static final int WEDDING_GIFT_LIMIT = 1;             //Max number of gifts per person to same wishlist on marriage instances.
    public static final boolean WEDDING_BLESSER_SHOWFX = true;  //Pops bubble sprite effect on players blessing the couple. Setting this false shows the blessing effect on the couple instead.

    //Buyback Configuration
    public static final boolean USE_BUYBACK_WITH_MESOS = true;  //Enables usage of either mesos or NX for the buyback fee.
    public static final float BUYBACK_FEE = 77.70f;             //Sets the base amount needed to buyback (level 30 or under will use the base value).
    public static final float BUYBACK_LEVEL_STACK_FEE = 85.47f; //Sets the level-stacking portion of the amount needed to buyback (fee will sum up linearly until level 120, when it reaches the peak).
    public static final int BUYBACK_MESO_MULTIPLIER = 1000;     //Sets a multiplier for the fee when using meso as the charge unit.
    public static final int BUYBACK_RETURN_MINUTES = 1;         //Sets the maximum amount of time the player can wait before decide to buyback.
    public static final int BUYBACK_COOLDOWN_MINUTES = 7;       //Sets the time the player must wait before using buyback again.
    
    //Event End Timestamp
    public static final long EVENT_END_TIMESTAMP = 1428897600000L;

    //Debug Variables
    public static int DEBUG_VALUES[] = new int[10];             // Field designed for packet testing purposes
    
    //Properties
    static {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("configuration.ini"));

            //Server Host
            ServerConstants.HOST = p.getProperty("HOST");
            ServerConstants.LOCALSERVER = ServerConstants.HOST.startsWith("127.") || ServerConstants.HOST.startsWith("localhost");

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
