package config;

public class ServerConfig {
    //Thread Tracker Configuration
    public boolean USE_THREAD_TRACKER;

    //Database Configuration
    public String DB_URL;
    public String DB_USER;
    public String DB_PASS;
    public boolean DB_CONNECTION_POOL;

    //Login Configuration
    public int WORLDS;
    public int WLDLIST_SIZE;
    public int CHANNEL_SIZE;
    public int CHANNEL_LOAD;
    public int CHANNEL_LOCKS;

    public long RESPAWN_INTERVAL;
    public long PURGING_INTERVAL;
    public long RANKING_INTERVAL;
    public long  COUPON_INTERVAL;
    public long  UPDATE_INTERVAL;

    public boolean ENABLE_PIC;
    public boolean ENABLE_PIN;

    public int BYPASS_PIC_EXPIRATION;
    public int BYPASS_PIN_EXPIRATION;

    public boolean AUTOMATIC_REGISTER;
    public boolean BCRYPT_MIGRATION;
    public boolean COLLECTIVE_CHARSLOT;
    public boolean DETERRED_MULTICLIENT;

    //Besides blocking logging in with several client sessions on the same machine, this also blocks suspicious login attempts for players that tries to login on an account using several diferent remote addresses.

    //Multiclient Coordinator Configuration
    public int MAX_ALLOWED_ACCOUNT_HWID;
    public int MAX_ACCOUNT_LOGIN_ATTEMPT;
    public int LOGIN_ATTEMPT_DURATION;

    //Ip Configuration
    public String HOST;
    public boolean LOCALSERVER;
    public boolean GMSERVER;

    //Other configuration
    public boolean SHUTDOWNHOOK;

    //Server Flags
    public boolean USE_CUSTOM_KEYSET;
    public boolean USE_DEBUG;
    public boolean USE_DEBUG_SHOW_INFO_EQPEXP;
    public boolean USE_DEBUG_SHOW_RCVD_PACKET;
    public boolean USE_DEBUG_SHOW_RCVD_MVLIFE;
    public boolean USE_DEBUG_SHOW_PACKET;
    public boolean USE_SUPPLY_RATE_COUPONS;
    public boolean USE_IP_VALIDATION;
    public boolean USE_CHARACTER_ACCOUNT_CHECK;

    public boolean USE_MAXRANGE;
    public boolean USE_MAXRANGE_ECHO_OF_HERO;
    public boolean USE_MTS;
    public boolean USE_CPQ;
    public boolean USE_AUTOHIDE_GM;
    public boolean USE_BUYBACK_SYSTEM;
    public boolean USE_FIXED_RATIO_HPMP_UPDATE;
    public boolean USE_FAMILY_SYSTEM;
    public boolean USE_DUEY;
    public boolean USE_RANDOMIZE_HPMP_GAIN;
    public boolean USE_STORAGE_ITEM_SORT;
    public boolean USE_ITEM_SORT;
    public boolean USE_ITEM_SORT_BY_NAME;
    public boolean USE_PARTY_FOR_STARTERS;
    public boolean USE_AUTOASSIGN_STARTERS_AP;
    public boolean USE_AUTOASSIGN_SECONDARY_CAP;
    public boolean USE_STARTING_AP_4;
    public boolean USE_AUTOBAN;
    public boolean USE_AUTOBAN_LOG;
    public boolean USE_AUTOSAVE;
    public boolean USE_SERVER_AUTOASSIGNER;
    public boolean USE_REFRESH_RANK_MOVE;
    public boolean USE_ENFORCE_ADMIN_ACCOUNT;
    public boolean USE_ENFORCE_NOVICE_EXPRATE;
    public boolean USE_ENFORCE_HPMP_SWAP;
    public boolean USE_ENFORCE_MOB_LEVEL_RANGE;
    public boolean USE_ENFORCE_JOB_LEVEL_RANGE;
    public boolean USE_ENFORCE_JOB_SP_RANGE;
    public boolean USE_ENFORCE_ITEM_SUGGESTION;
    public boolean USE_ENFORCE_UNMERCHABLE_CASH;
    public boolean USE_ENFORCE_UNMERCHABLE_PET;
    public boolean USE_ENFORCE_MERCHANT_SAVE;
    public boolean USE_ENFORCE_MDOOR_POSITION;
    public boolean USE_SPAWN_CLEAN_MDOOR;
    public boolean USE_SPAWN_LOOT_ON_ANIMATION;
    public boolean USE_SPAWN_RELEVANT_LOOT;
    public boolean USE_ERASE_PERMIT_ON_OPENSHOP;
    public boolean USE_ERASE_UNTRADEABLE_DROP;
    public boolean USE_ERASE_PET_ON_EXPIRATION;
    public boolean USE_BUFF_MOST_SIGNIFICANT;
    public boolean USE_BUFF_EVERLASTING;
    public boolean USE_MULTIPLE_SAME_EQUIP_DROP;
    public boolean USE_BANISHABLE_TOWN_SCROLL;
    public boolean USE_ENABLE_FULL_RESPAWN;
    public boolean USE_ENABLE_CHAT_LOG;
    public boolean USE_REBIRTH_SYSTEM;
    public boolean USE_MAP_OWNERSHIP_SYSTEM;
    public boolean USE_FISHING_SYSTEM;
    public boolean USE_NPCS_SCRIPTABLE;

    //Events/PQs Configuration
    public boolean USE_OLD_GMS_STYLED_PQ_NPCS;
    public boolean USE_ENABLE_SOLO_EXPEDITIONS;
    public boolean USE_ENABLE_DAILY_EXPEDITIONS;
    public boolean USE_ENABLE_RECALL_EVENT;

    //Announcement Configuration
    public boolean USE_ANNOUNCE_SHOPITEMSOLD;
    public boolean USE_ANNOUNCE_CHANGEJOB;

    //Cash Shop Configuration
    public boolean USE_JOINT_CASHSHOP_INVENTORY;
    public boolean USE_CLEAR_OUTDATED_COUPONS;
    public boolean ALLOW_CASHSHOP_NAME_CHANGE;
    public boolean ALLOW_CASHSHOP_WORLD_TRANSFER;//Allows players to buy world transfers in the cash shop.

    //Maker Configuration
    public boolean USE_MAKER_PERMISSIVE_ATKUP;
    public boolean USE_MAKER_FEE_HEURISTICS;

    //Custom Configuration
    public boolean USE_ENABLE_CUSTOM_NPC_SCRIPT;
    public boolean USE_STARTER_MERGE;

    //Commands Configuration
    public boolean BLOCK_GENERATE_CASH_ITEM;
    public boolean USE_WHOLE_SERVER_RANKING;

    public double EQUIP_EXP_RATE;
    public double PQ_BONUS_EXP_RATE;

    public byte EXP_SPLIT_LEVEL_INTERVAL;
    public byte EXP_SPLIT_LEECH_INTERVAL;
    public float EXP_SPLIT_MVP_MOD;
    public float EXP_SPLIT_COMMON_MOD;
    public float PARTY_BONUS_EXP_RATE;

    //Miscellaneous Configuration
    public String TIMEZONE;
    public boolean USE_DISPLAY_NUMBERS_WITH_COMMA;
    public boolean USE_UNITPRICE_WITH_COMMA;
    public byte MAX_MONITORED_BUFFSTATS;
    public int MAX_AP;
    public int MAX_EVENT_LEVELS;
    public long BLOCK_NPC_RACE_CONDT;
    public int TOT_MOB_QUEST_REQUIREMENT;
    public int MOB_REACTOR_REFRESH_TIME;
    public int PARTY_SEARCH_REENTRY_LIMIT;
    public long NAME_CHANGE_COOLDOWN;
    public long WORLD_TRANSFER_COOLDOWN=NAME_CHANGE_COOLDOWN;//Cooldown for world tranfers, default is same as name change (30 days).
    public boolean INSTANT_NAME_CHANGE;

    //Dangling Items/Locks Configuration
    public int ITEM_EXPIRE_TIME ;
    public int KITE_EXPIRE_TIME ;
    public int ITEM_MONITOR_TIME;
    public int LOCK_MONITOR_TIME;

    //Map Monitor Configuration
    public int ITEM_EXPIRE_CHECK;
    public int ITEM_LIMIT_ON_MAP;
    public int MAP_VISITED_SIZE;
    public int MAP_DAMAGE_OVERTIME_INTERVAL;

    //Channel Mob Disease Monitor Configuration
    public int MOB_STATUS_MONITOR_PROC;
    public int MOB_STATUS_MONITOR_LIFE;
    public int MOB_STATUS_AGGRO_PERSISTENCE;
    public int MOB_STATUS_AGGRO_INTERVAL;
    public boolean USE_AUTOAGGRO_NEARBY;

    //Some Gameplay Enhancing Configurations
    //Scroll Configuration
    public boolean USE_PERFECT_GM_SCROLL;
    public boolean USE_PERFECT_SCROLLING;
    public boolean USE_ENHANCED_CHSCROLL;
    public boolean USE_ENHANCED_CRAFTING;
    public boolean USE_ENHANCED_CLNSLATE;
    public int SCROLL_CHANCE_ROLLS;
    public int CHSCROLL_STAT_RATE;
    public int CHSCROLL_STAT_RANGE;

    //Beginner Skills Configuration
    public boolean USE_ULTRA_NIMBLE_FEET;
    public boolean USE_ULTRA_RECOVERY;
    public boolean USE_ULTRA_THREE_SNAILS;

    //Other Skills Configuration
    public boolean USE_FULL_ARAN_SKILLSET;
    public boolean USE_FAST_REUSE_HERO_WILL;
    public boolean USE_ANTI_IMMUNITY_CRASH;
    public boolean USE_UNDISPEL_HOLY_SHIELD;
    public boolean USE_FULL_HOLY_SYMBOL;

    //Character Configuration
    public boolean USE_ADD_SLOTS_BY_LEVEL;
    public boolean USE_ADD_RATES_BY_LEVEL;
    public boolean USE_STACK_COUPON_RATES;
    public boolean USE_PERFECT_PITCH;

    //Quest Configuration
    public boolean USE_QUEST_RATE;

    //Quest Points Configuration
    public int QUEST_POINT_REPEATABLE_INTERVAL;
    public int QUEST_POINT_REQUIREMENT;
    public int QUEST_POINT_PER_QUEST_COMPLETE;
    public int QUEST_POINT_PER_EVENT_CLEAR;

    //Guild Configuration
    public int CREATE_GUILD_MIN_PARTNERS;
    public int CREATE_GUILD_COST;
    public int CHANGE_EMBLEM_COST;
    public int EXPAND_GUILD_BASE_COST;
    public int EXPAND_GUILD_TIER_COST;
    public int EXPAND_GUILD_MAX_COST;

    //Family Configuration
    public int FAMILY_REP_PER_KILL;
    public int FAMILY_REP_PER_BOSS_KILL;
    public int FAMILY_REP_PER_LEVELUP;
    public int FAMILY_MAX_GENERATIONS;

    //Equipment Configuration
    public boolean USE_EQUIPMNT_LVLUP_SLOTS;
    public boolean USE_EQUIPMNT_LVLUP_POWER;
    public boolean USE_EQUIPMNT_LVLUP_CASH;
    public boolean USE_SPIKES_AVOID_BANISH;
    public int MAX_EQUIPMNT_LVLUP_STAT_UP;
    public int MAX_EQUIPMNT_STAT;
    public int USE_EQUIPMNT_LVLUP;

    //Map-Chair Configuration
    public boolean USE_CHAIR_EXTRAHEAL;
    public byte CHAIR_EXTRA_HEAL_MULTIPLIER;
    public int CHAIR_EXTRA_HEAL_MAX_DELAY;

    //Player NPC Configuration
    public int PLAYERNPC_INITIAL_X;
    public int PLAYERNPC_INITIAL_Y;
    public int PLAYERNPC_AREA_X;
    public int PLAYERNPC_AREA_Y;
    public int PLAYERNPC_AREA_STEPS;
    public boolean PLAYERNPC_ORGANIZE_AREA;
    public boolean PLAYERNPC_AUTODEPLOY;

    //Pet Auto-Pot Configuration
    public boolean USE_COMPULSORY_AUTOPOT;
    public boolean USE_EQUIPS_ON_AUTOPOT;
    public double PET_AUTOHP_RATIO;
    public double PET_AUTOMP_RATIO;

    //Pet & Mount Configuration
    public byte PET_EXHAUST_COUNT;
    public byte MOUNT_EXHAUST_COUNT;

    //Pet Hunger Configuration
    public boolean PETS_NEVER_HUNGRY;
    public boolean GM_PETS_NEVER_HUNGRY;

    //Event Configuration
    public int EVENT_MAX_GUILD_QUEUE;
    public long EVENT_LOBBY_DELAY;

    //Dojo Configuration
    public boolean USE_FAST_DOJO_UPGRADE;
    public boolean USE_DEADLY_DOJO;
    public int DOJO_ENERGY_ATK;
    public int DOJO_ENERGY_DMG;

    //Wedding Configuration
    public int WEDDING_RESERVATION_DELAY;
    public int WEDDING_RESERVATION_TIMEOUT;
    public int WEDDING_RESERVATION_INTERVAL;
    public int WEDDING_BLESS_EXP;
    public int WEDDING_GIFT_LIMIT;
    public boolean WEDDING_BLESSER_SHOWFX;

    //Buyback Configuration
    public boolean USE_BUYBACK_WITH_MESOS;
    public float BUYBACK_FEE;
    public float BUYBACK_LEVEL_STACK_FEE;
    public int BUYBACK_MESO_MULTIPLIER;
    public int BUYBACK_RETURN_MINUTES;
    public int BUYBACK_COOLDOWN_MINUTES;

    // Login timeout by shavit
    public long TIMEOUT_DURATION;

    //Event End Timestamp
    public long EVENT_END_TIMESTAMP;

}
