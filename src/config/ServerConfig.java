package config;

public class ServerConfig {
    //Thread Tracker Configuration
    private boolean USE_THREAD_TRACKER;

    //Database Configuration
    private String DB_URL;
    private String DB_USER;
    private String DB_PASS;
    private boolean DB_CONNECTION_POOL;

    //Login Configuration
    private int WLDLIST_SIZE;
    private int CHANNEL_SIZE;
    private int CHANNEL_LOAD;
    private int CHANNEL_LOCKS;

    private long RESPAWN_INTERVAL;
    private long PURGING_INTERVAL;
    private long RANKING_INTERVAL;
    private long  COUPON_INTERVAL;
    private long  UPDATE_INTERVAL;

    private boolean ENABLE_PIC;
    private boolean ENABLE_PIN;

    private int BYPASS_PIC_EXPIRATION;
    private int BYPASS_PIN_EXPIRATION;

    private boolean AUTOMATIC_REGISTER;
    private boolean BCRYPT_MIGRATION;
    private boolean COLLECTIVE_CHARSLOT;
    private boolean DETERRED_MULTICLIENT;

    //Besides blocking logging in with several client sessions on the same machine, this also blocks suspicious login attempts for players that tries to login on an account using several diferent remote addresses.

    //Multiclient Coordinator Configuration
    private int MAX_ALLOWED_ACCOUNT_HWID;
    private int MAX_ACCOUNT_LOGIN_ATTEMPT;
    private int LOGIN_ATTEMPT_DURATION;

    //Ip Configuration
    public String HOST;
    public boolean LOCALSERVER;

    //Other configuration
    public boolean SHUTDOWNHOOK;

    //Server Flags
    private boolean USE_CUSTOM_KEYSET;
    private boolean USE_DEBUG;
    private boolean USE_DEBUG_SHOW_INFO_EQPEXP;
    private boolean USE_DEBUG_SHOW_RCVD_PACKET;
    private boolean USE_DEBUG_SHOW_RCVD_MVLIFE;
    private boolean USE_DEBUG_SHOW_PACKET;
    private boolean USE_SUPPLY_RATE_COUPONS;
    private boolean USE_IP_VALIDATION;

    private boolean USE_MAXRANGE;
    private boolean USE_MAXRANGE_ECHO_OF_HERO;
    private boolean USE_MTS;
    private boolean USE_CPQ;
    private boolean USE_AUTOHIDE_GM;
    private boolean USE_BUYBACK_SYSTEM;
    private boolean USE_FIXED_RATIO_HPMP_UPDATE;
    private boolean USE_FAMILY_SYSTEM;
    private boolean USE_DUEY;
    private boolean USE_RANDOMIZE_HPMP_GAIN;
    private boolean USE_STORAGE_ITEM_SORT;
    private boolean USE_ITEM_SORT;
    private boolean USE_ITEM_SORT_BY_NAME;
    private boolean USE_PARTY_FOR_STARTERS;
    private boolean USE_AUTOASSIGN_STARTERS_AP;
    private boolean USE_AUTOASSIGN_SECONDARY_CAP;
    private boolean USE_STARTING_AP_4;
    private boolean USE_AUTOBAN;
    private boolean USE_AUTOBAN_LOG;
    private boolean USE_AUTOSAVE;
    private boolean USE_SERVER_AUTOASSIGNER;
    private boolean USE_REFRESH_RANK_MOVE;
    private boolean USE_ENFORCE_ADMIN_ACCOUNT;
    private boolean USE_ENFORCE_NOVICE_EXPRATE;
    private boolean USE_ENFORCE_HPMP_SWAP;
    private boolean USE_ENFORCE_MOB_LEVEL_RANGE;
    private boolean USE_ENFORCE_JOB_LEVEL_RANGE;
    private boolean USE_ENFORCE_JOB_SP_RANGE;
    private boolean USE_ENFORCE_ITEM_SUGGESTION;
    private boolean USE_ENFORCE_UNMERCHABLE_CASH;
    private boolean USE_ENFORCE_UNMERCHABLE_PET;
    private boolean USE_ENFORCE_MERCHANT_SAVE;
    private boolean USE_ENFORCE_MDOOR_POSITION;
    private boolean USE_SPAWN_CLEAN_MDOOR;
    private boolean USE_SPAWN_LOOT_ON_ANIMATION;
    private boolean USE_SPAWN_RELEVANT_LOOT;
    private boolean USE_ERASE_PERMIT_ON_OPENSHOP;
    private boolean USE_ERASE_UNTRADEABLE_DROP;
    private boolean USE_ERASE_PET_ON_EXPIRATION;
    private boolean USE_BUFF_MOST_SIGNIFICANT;
    private boolean USE_BUFF_EVERLASTING;
    private boolean USE_MULTIPLE_SAME_EQUIP_DROP;
    private boolean USE_BANISHABLE_TOWN_SCROLL;
    private boolean USE_ENABLE_FULL_RESPAWN;
    private boolean USE_ENABLE_CHAT_LOG;
    private boolean USE_REBIRTH_SYSTEM;
    private boolean USE_MAP_OWNERSHIP_SYSTEM;
    private boolean USE_FISHING_SYSTEM;
    private boolean USE_NPCS_SCRIPTABLE;

    //Events/PQs Configuration
    private boolean USE_OLD_GMS_STYLED_PQ_NPCS;
    private boolean USE_ENABLE_SOLO_EXPEDITIONS;
    private boolean USE_ENABLE_DAILY_EXPEDITIONS;
    private boolean USE_ENABLE_RECALL_EVENT;

    //Announcement Configuration
    private boolean USE_ANNOUNCE_SHOPITEMSOLD;
    private boolean USE_ANNOUNCE_CHANGEJOB;

    //Cash Shop Configuration
    private boolean USE_JOINT_CASHSHOP_INVENTORY;
    private boolean USE_CLEAR_OUTDATED_COUPONS;
    private boolean ALLOW_CASHSHOP_NAME_CHANGE;
    public boolean ALLOW_CASHSHOP_WORLD_TRANSFER;//Allows players to buy world transfers in the cash shop.

    //Maker Configuration
    private boolean USE_MAKER_PERMISSIVE_ATKUP;
    private boolean USE_MAKER_FEE_HEURISTICS;

    //Custom Configuration
    private boolean USE_ENABLE_CUSTOM_NPC_SCRIPT;
    private boolean USE_STARTER_MERGE;

    //Commands Configuration
    private boolean BLOCK_GENERATE_CASH_ITEM;
    private boolean USE_WHOLE_SERVER_RANKING;

    //Server Rates And Experience
    private int EXP_RATE;
    private int MESO_RATE;
    private int DROP_RATE;
    private int BOSS_DROP_RATE;
    private int QUEST_RATE;
    private int FISHING_RATE;
    private int TRAVEL_RATE;

    private double EQUIP_EXP_RATE;
    private double PQ_BONUS_EXP_RATE;

    private byte EXP_SPLIT_LEVEL_INTERVAL;
    private byte EXP_SPLIT_LEECH_INTERVAL;
    private float EXP_SPLIT_MVP_MOD;
    private float EXP_SPLIT_COMMON_MOD;
    private float PARTY_BONUS_EXP_RATE;

    //Miscellaneous Configuration
    private String TIMEZONE;
    private boolean USE_DISPLAY_NUMBERS_WITH_COMMA;
    private boolean USE_UNITPRICE_WITH_COMMA;
    private byte MAX_MONITORED_BUFFSTATS;
    private int MAX_AP;
    private int MAX_EVENT_LEVELS;
    private long BLOCK_NPC_RACE_CONDT;
    private long PET_LOOT_UPON_ATTACK;
    private int TOT_MOB_QUEST_REQUIREMENT;
    private int MOB_REACTOR_REFRESH_TIME;
    private int PARTY_SEARCH_REENTRY_LIMIT;
    private int NAME_CHANGE_COOLDOWN;
    public int WORLD_TRANSFER_COOLDOWN=NAME_CHANGE_COOLDOWN;//Cooldown for world tranfers, default is same as name change (30 days).
    private boolean INSTANT_NAME_CHANGE;

    //Dangling Items/Locks Configuration
    private int ITEM_EXPIRE_TIME ;
    private int KITE_EXPIRE_TIME ;
    private int ITEM_MONITOR_TIME;
    private int LOCK_MONITOR_TIME;

    //Map Monitor Configuration
    private int ITEM_EXPIRE_CHECK;
    private int ITEM_LIMIT_ON_MAP;
    private int MAP_VISITED_SIZE;
    private int MAP_DAMAGE_OVERTIME_INTERVAL;

    //Channel Mob Disease Monitor Configuration
    private int MOB_STATUS_MONITOR_PROC;
    private int MOB_STATUS_MONITOR_LIFE;
    private int MOB_STATUS_AGGRO_PERSISTENCE;
    private int MOB_STATUS_AGGRO_INTERVAL;

    //Some Gameplay Enhancing Configurations
    //Scroll Configuration
    private boolean USE_PERFECT_GM_SCROLL;
    private boolean USE_PERFECT_SCROLLING;
    private boolean USE_ENHANCED_CHSCROLL;
    private boolean USE_ENHANCED_CRAFTING;
    private boolean USE_ENHANCED_CLNSLATE;
    private int SCROLL_CHANCE_RATE;
    private int CHSCROLL_STAT_RATE;
    private int CHSCROLL_STAT_RANGE;

    //Beginner Skills Configuration
    private boolean USE_ULTRA_NIMBLE_FEET;
    private boolean USE_ULTRA_RECOVERY;
    private boolean USE_ULTRA_THREE_SNAILS;

    //Other Skills Configuration
    private boolean USE_FULL_ARAN_SKILLSET;
    private boolean USE_FAST_REUSE_HERO_WILL;
    private boolean USE_ANTI_IMMUNITY_CRASH;
    private boolean USE_UNDISPEL_HOLY_SHIELD;
    private boolean USE_FULL_HOLY_SYMBOL;

    //Character Configuration
    private boolean USE_ADD_SLOTS_BY_LEVEL;
    private boolean USE_ADD_RATES_BY_LEVEL;
    private boolean USE_STACK_COUPON_RATES;
    private boolean USE_PERFECT_PITCH;

    //Quest Configuration
    private boolean USE_QUEST_RATE;

    //Quest Points Configuration
    private int QUEST_POINT_REPEATABLE_INTERVAL;
    private int QUEST_POINT_REQUIREMENT;
    private int QUEST_POINT_PER_QUEST_COMPLETE;
    private int QUEST_POINT_PER_EVENT_CLEAR;

    //Guild Configuration
    private int CREATE_GUILD_MIN_PARTNERS;
    private int CREATE_GUILD_COST;
    private int CHANGE_EMBLEM_COST;
    private int EXPAND_GUILD_BASE_COST;
    private int EXPAND_GUILD_TIER_COST;
    private int EXPAND_GUILD_MAX_COST;

    //Family Configuration
    private int FAMILY_REP_PER_KILL;
    private int FAMILY_REP_PER_BOSS_KILL;
    private int FAMILY_REP_PER_LEVELUP;
    private int FAMILY_MAX_GENERATIONS;

    //Equipment Configuration
    private boolean USE_EQUIPMNT_LVLUP_SLOTS;
    private boolean USE_EQUIPMNT_LVLUP_POWER;
    private boolean USE_EQUIPMNT_LVLUP_CASH;
    private boolean USE_SPIKES_AVOID_BANISH;
    private int MAX_EQUIPMNT_LVLUP_STAT_UP;
    private int MAX_EQUIPMNT_STAT;
    private int USE_EQUIPMNT_LVLUP;

    //Map-Chair Configuration
    private boolean USE_CHAIR_EXTRAHEAL;
    private byte CHAIR_EXTRA_HEAL_MULTIPLIER;
    private int CHAIR_EXTRA_HEAL_MAX_DELAY;

    //Player NPC Configuration
    private int PLAYERNPC_INITIAL_X;
    private int PLAYERNPC_INITIAL_Y;
    private int PLAYERNPC_AREA_X;
    private int PLAYERNPC_AREA_Y;
    private int PLAYERNPC_AREA_STEPS;
    private boolean PLAYERNPC_ORGANIZE_AREA;
    private boolean PLAYERNPC_AUTODEPLOY;

    //Pet Auto-Pot Configuration
    private boolean USE_COMPULSORY_AUTOPOT;
    private boolean USE_EQUIPS_ON_AUTOPOT;
    private double PET_AUTOHP_RATIO;
    private double PET_AUTOMP_RATIO;

    //Pet & Mount Configuration
    private byte PET_EXHAUST_COUNT;
    private byte MOUNT_EXHAUST_COUNT;

    //Pet Hunger Configuration
    private boolean PETS_NEVER_HUNGRY;
    private boolean GM_PETS_NEVER_HUNGRY;

    //Event Configuration
    private int EVENT_MAX_GUILD_QUEUE;
    private long EVENT_LOBBY_DELAY;

    //Dojo Configuration
    private boolean USE_FAST_DOJO_UPGRADE;
    private boolean USE_DEADLY_DOJO;
    private int DOJO_ENERGY_ATK;
    private int DOJO_ENERGY_DMG;

    //Wedding Configuration
    private int WEDDING_RESERVATION_DELAY;
    private int WEDDING_RESERVATION_TIMEOUT;
    private int WEDDING_RESERVATION_INTERVAL;
    private int WEDDING_BLESS_EXP;
    private int WEDDING_GIFT_LIMIT;
    private boolean WEDDING_BLESSER_SHOWFX;

    //Buyback Configuration
    private boolean USE_BUYBACK_WITH_MESOS;
    private float BUYBACK_FEE;
    private float BUYBACK_LEVEL_STACK_FEE;
    private int BUYBACK_MESO_MULTIPLIER;
    private int BUYBACK_RETURN_MINUTES;
    private int BUYBACK_COOLDOWN_MINUTES;

    // Login timeout by shavit
    private long TIMEOUT_DURATION;

    //Event End Timestamp
    private long EVENT_END_TIMESTAMP;

    //Debug Variables
    private int DEBUG_VALUES[];
}
