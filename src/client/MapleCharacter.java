/* 
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program unader any cother version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import net.server.PlayerBuffValueHolder;
import net.server.PlayerCoolDownValueHolder;
import net.server.Server;
import net.server.channel.Channel;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.MapleMessenger;
import net.server.world.MapleMessengerCharacter;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;
import scripting.event.EventInstanceManager;
import server.CashShop;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePlayerShopItem;
import server.MaplePortal;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.TimerManager;
import server.events.MapleEvents;
import server.events.RescueGaga;
import server.events.gm.MapleFitness;
import server.events.gm.MapleOla;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.MapleHiredMerchant;
import server.maps.MapleDoor;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleMapEffect;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.maps.SavedLocation;
import server.maps.SavedLocationType;
import server.partyquest.MonsterCarnival;
import server.partyquest.MonsterCarnivalParty;
import server.partyquest.PartyQuest;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import client.autoban.AutobanManager;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleWeaponType;
import client.inventory.ModifyInventory;
import client.inventory.PetDataFactory;
import constants.ExpTable;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.Aran;
import constants.skills.Beginner;
import constants.skills.Bishop;
import constants.skills.BlazeWizard;
import constants.skills.Bowmaster;
import constants.skills.Buccaneer;
import constants.skills.Corsair;
import constants.skills.Crusader;
import constants.skills.DarkKnight;
import constants.skills.DawnWarrior;
import constants.skills.Evan;
import constants.skills.FPArchMage;
import constants.skills.GM;
import constants.skills.Hermit;
import constants.skills.Hero;
import constants.skills.ILArchMage;
import constants.skills.Legend;
import constants.skills.Magician;
import constants.skills.Marauder;
import constants.skills.Marksman;
import constants.skills.NightLord;
import constants.skills.Noblesse;
import constants.skills.Paladin;
import constants.skills.Priest;
import constants.skills.Ranger;
import constants.skills.Shadower;
import constants.skills.Sniper;
import constants.skills.Spearman;
import constants.skills.SuperGM;
import constants.skills.Swordsman;
import constants.skills.ThunderBreaker;
import net.server.channel.handlers.PartyOperationHandler;
import scripting.item.ItemScriptManager;
import server.maps.MapleMapItem;

public class MapleCharacter extends AbstractAnimatedMapleMapObject {
    private static NumberFormat nf = new DecimalFormat("#,###,###,###");
    private static MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    private static final String LEVEL_200 = "[Congrats] %s has reached Level 200! Congratulate %s on such an amazing achievement!";
    private static final String[] BLOCKED_NAMES = {"admin", "owner", "moderator", "intern", "donor", "administrator", "help", "helper", "alert", "notice", "maplestory", "Solaxia", "fuck", "wizet", "fucking", "negro", "fuk", "fuc", "penis", "pussy", "asshole", "gay",
        "nigger", "homo", "suck", "cum", "shit", "shitty", "condom", "security", "official", "rape", "nigga", "sex", "tit", "boner", "orgy", "clit", "asshole", "fatass", "bitch", "support", "gamemaster", "cock", "gaay", "gm",
        "operate", "master", "sysop", "party", "GameMaster", "community", "message", "event", "test", "meso", "Scania", "renewal", "yata", "AsiaSoft", "henesys"};
    
    private int world;
    private int accountid, id;
    private int rank, rankMove, jobRank, jobRankMove;
    private int level, str, dex, luk, int_, hp, maxhp, mp, maxmp;
    private int hpMpApUsed;
    private int hair;
    private int face;
    private int remainingAp;
    private int[] remainingSp = new int[10];
    private int quest_fame;
    private int fame;
    private int initialSpawnPoint;
    private int mapid;
    private int gender;
    private int currentPage, currentType = 0, currentTab = 1;
    private int itemEffect;
    private int guildid, guildRank, allianceRank;
    private int messengerposition = 4;
    private int slots = 0;
    private int energybar;
    private int gmLevel;
    private int ci = 0;
    private MapleFamily family;
    private int familyId;
    private int bookCover;
    private int markedMonster = 0;
    private int battleshipHp = 0;
    private int mesosTraded = 0;
    private int possibleReports = 10;
    private int dojoPoints, vanquisherStage, dojoStage, dojoEnergy, vanquisherKills;
    private int warpToId;
    private int expRate = 1, mesoRate = 1, dropRate = 1, expCoupon = 1, mesoCoupon = 1, dropCoupon = 1;
    private int omokwins, omokties, omoklosses, matchcardwins, matchcardties, matchcardlosses;
    private int owlSearch;
    private int married;
    private long lastfametime, lastUsedCashItem, lastHealed, lastMesoDrop = -1, jailExpiration = -1;
    private transient int localmaxhp, localmaxmp, localstr, localdex, localluk, localint_, magic, watk;
    private boolean hidden, canDoor = true, berserk, hasMerchant, whiteChat = false;
    private int linkedLevel = 0;
    private String linkedName = null;
    private boolean finishedDojoTutorial;
    private String name;
    private String chalktext;
    private String dataString;
    private String search = null;
    private AtomicBoolean mapTransitioning = new AtomicBoolean(true);  // player client is currently trying to change maps or log in the game map
    private AtomicBoolean awayFromWorld = new AtomicBoolean(true);  // player is online, but on cash shop or mts
    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger gachaexp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private AtomicInteger chair = new AtomicInteger();
    private int merchantmeso;
    private BuddyList buddylist;
    private EventInstanceManager eventInstance = null;
    private MapleHiredMerchant hiredMerchant = null;
    private MapleClient client;
    private MapleGuildCharacter mgc = null;
    private MaplePartyCharacter mpc = null;
    private MapleInventory[] inventory;
    private MapleJob job = MapleJob.BEGINNER;
    private MapleMap map;
    private MapleMessenger messenger = null;
    private MapleMiniGame miniGame;
    private MapleMount maplemount;
    private MapleParty party;
    private MaplePet[] pets = new MaplePet[3];
    private MaplePlayerShop playerShop = null;
    private MapleShop shop = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleStorage storage = null;
    private MapleTrade trade = null;
    private MonsterBook monsterbook;
    private MapleRing marriageRing;
    private CashShop cashshop;
    private SavedLocation savedLocations[];
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private List<Integer> lastmonthfameids;
    private final Map<Short, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<>();
    private Map<Integer, String> entered = new LinkedHashMap<>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<>();
    private Map<Skill, SkillEntry> skills = new LinkedHashMap<>();
    private Map<Integer, Integer> activeCoupons = new LinkedHashMap<>();
    private Map<Integer, Integer> activeCouponRates = new LinkedHashMap<>();
    private EnumMap<MapleBuffStat, MapleBuffStatValueHolder> effects = new EnumMap<>(MapleBuffStat.class);
    private Map<MapleBuffStat, Byte> buffEffectsCount = new LinkedHashMap<>();
    private Map<MapleDisease, Long> diseaseExpires = new LinkedHashMap<>();
    private Map<Integer, Map<MapleBuffStat, MapleBuffStatValueHolder>> buffEffects = new LinkedHashMap<>();
    private Map<Integer, Long> buffExpires = new LinkedHashMap<>();
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<>();
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<>();
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<>();
    private EnumMap<MapleDisease, MapleDiseaseValueHolder> diseases = new EnumMap<>(MapleDisease.class);
    private Map<Integer, MapleDoor> doors = new LinkedHashMap<>();
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule, beholderBuffSchedule, berserkSchedule;
    private ScheduledFuture<?> skillCooldownTask = null;
    private ScheduledFuture<?> buffExpireTask = null;
    private ScheduledFuture<?> itemExpireTask = null;
    private ScheduledFuture<?> diseaseExpireTask = null;
    private ScheduledFuture<?> recoveryTask = null;
    private ScheduledFuture<?> extraRecoveryTask = null;
    private ScheduledFuture<?> chairRecoveryTask = null;
    private ScheduledFuture<?> pendantOfSpirit = null; //1122017
    private List<ScheduledFuture<?>> timers = new ArrayList<>();
    private Lock chrLock = new ReentrantLock(true);
    private Lock effLock = new ReentrantLock(true);
    private Lock petLock = new ReentrantLock(true);
    private Lock prtLock = new ReentrantLock();
    private Map<Integer, Set<Integer>> excluded = new LinkedHashMap<>();
    private Set<Integer> excludedItems = new LinkedHashSet<>();
    private List<MapleRing> crushRings = new ArrayList<>();
    private List<MapleRing> friendshipRings = new ArrayList<>();
    private static String[] ariantroomleader = new String[3];
    private static int[] ariantroomslot = new int[3];
    private long portaldelay = 0, lastcombo = 0;
    private short combocounter = 0;
    private List<String> blockedPortals = new ArrayList<>();
    private Map<Short, String> area_info = new LinkedHashMap<>();
    private AutobanManager autoban;
    private boolean isbanned = false;
    private boolean blockCashShop = false;
    private byte pendantExp = 0, lastmobcount = 0, doorSlot = -1;
    private List<Integer> trockmaps = new ArrayList<>();
    private List<Integer> viptrockmaps = new ArrayList<>();
    private Map<String, MapleEvents> events = new LinkedHashMap<>();
    private PartyQuest partyQuest = null;
    private boolean loggedIn = false;
    private MapleDragon dragon = null;
    private boolean useCS;  //chaos scroll upon crafting item.
    private long npcCd;
    private long petLootCd;
    private long lastHpDec = 0;
    private int newWarpMap = -1;
    private boolean canWarpMap = true;  //only one "warp" must be used per call, and this will define the right one.
    private int canWarpCounter = 0;     //counts how many times "inner warps" have been called.
    private byte extraHpRec = 0, extraMpRec = 0;
    private short extraRecInterval;
    private int targetHpBarHash = 0;
    private long targetHpBarTime = 0;
    private long nextUnderlevelTime = 0;
    private int banishMap = -1;
    private int banishSp = -1;
    private long banishTime = 0;

    private MapleCharacter() {
        useCS = false;
        
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        savedLocations = new SavedLocation[SavedLocationType.values().length];
        for (int i = 0; i < remainingSp.length; i++) {
            remainingSp[i] = 0;
        }
        for (MapleInventoryType type : MapleInventoryType.values()) {
            byte b = 24;
            if (type == MapleInventoryType.CASH) {
                b = 96;
            }
            inventory[type.ordinal()] = new MapleInventory(this, type, (byte) b);
        }
        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = null;
        }
        quests = new LinkedHashMap<>();
        setPosition(new Point(0, 0));
        
        petLootCd = System.currentTimeMillis();
    }
    
    public MapleJob getJobStyle() {
        int jobtype = this.getJob().getId() / 100;
        
        if(jobtype == MapleJob.WARRIOR.getId() / 100 || jobtype == MapleJob.DAWNWARRIOR1.getId() / 100 || jobtype == MapleJob.ARAN1.getId() / 100) {
            return(MapleJob.WARRIOR);
        }
        
        else if(jobtype == MapleJob.MAGICIAN.getId() / 100 || jobtype == MapleJob.BLAZEWIZARD1.getId() / 100 || jobtype == MapleJob.EVAN1.getId() / 100) {
            return(MapleJob.MAGICIAN);
        }
        
        else if(jobtype == MapleJob.BOWMAN.getId() / 100 || jobtype == MapleJob.WINDARCHER1.getId() / 100) {
            if(this.getJob().getId() / 10 == MapleJob.CROSSBOWMAN.getId() / 10) return(MapleJob.CROSSBOWMAN);
            else return(MapleJob.BOWMAN);
        }
        
        else if(jobtype == MapleJob.THIEF.getId() / 100 || jobtype == MapleJob.NIGHTWALKER1.getId() / 100) {
            return(MapleJob.THIEF);
        }
        
        else if(jobtype == MapleJob.PIRATE.getId() / 100 || jobtype == MapleJob.THUNDERBREAKER1.getId() / 100) {
            if(this.getStr() > this.getDex()) return(MapleJob.BRAWLER);
            else return(MapleJob.GUNSLINGER);
        }
        
        return(MapleJob.BEGINNER);
    }

    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = c;
        ret.gmLevel = 0;
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.map = null;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountid = c.getAccID();
        ret.buddylist = new BuddyList(20);
        ret.maplemount = null;
        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.USE).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(24);
        
        // Select a keybinding method
        int[] selectedKey;
        int[] selectedType;
        int[] selectedAction;
        
        if(ServerConstants.USE_CUSTOM_KEYSET) {
            selectedKey = GameConstants.getCustomKey(true);
            selectedType = GameConstants.getCustomType(true);
            selectedAction = GameConstants.getCustomAction(true);
        }
        else {
            selectedKey = GameConstants.getCustomKey(false);
            selectedType = GameConstants.getCustomType(false);
            selectedAction = GameConstants.getCustomAction(false);
        }
                
        for (int i = 0; i < selectedKey.length; i++) {
            ret.keymap.put(selectedKey[i], new MapleKeyBinding(selectedType[i], selectedAction[i]));
        }
        
        
        //to fix the map 0 lol
        for (int i = 0; i < 5; i++) {
            ret.trockmaps.add(999999999);
        }
        for (int i = 0; i < 10; i++) {
            ret.viptrockmaps.add(999999999);
        }

        return ret;
    }
    
    public boolean getAwayFromWorld() {
        return awayFromWorld.get();
    }
    
    public void setAwayFromWorld(boolean away) {
        awayFromWorld.set(away);
    }
    
    public long getPetLootCd() {
        return petLootCd;
    }
    
    public void setPetLootCd(long cd) {
        petLootCd = cd;
    }
    
    public boolean getCS() {
        return useCS;
    }
    
    public void setCS(boolean cs) {
        useCS = cs;
    }
    
    public long getNpcCooldown() {
        return npcCd;
    }
    
    public void setNpcCooldown(long d) {
        npcCd = d;
    }
    
    public void setOwlSearch(int id) {
        owlSearch = id;
    }
    
    public int getOwlSearch() {
        return owlSearch;
    }

    public void addCooldown(int skillId, long startTime, long length) {
        effLock.lock();
        chrLock.lock();
        try {
            if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
                this.coolDowns.remove(Integer.valueOf(skillId));
            }
            this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length));
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public void addCrushRing(MapleRing r) {
        crushRings.add(r);
    }

    public MapleRing getRingById(int id) {
        for (MapleRing ring : getCrushRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        for (MapleRing ring : getFriendshipRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        if (getMarriageRing().getRingId() == id) {
            return getMarriageRing();
        }

        return null;
    }

    public int addDojoPointsByMap() {
        int pts = 0;
        if (dojoPoints < 17000) {
            pts = 1 + ((getMap().getId() - 1) / 100 % 100) / 6;
            if (!getDojoParty()) {
                pts++;
            }
            this.dojoPoints += pts;
        }
        return pts;
    }

    public void addDoor(Integer owner, MapleDoor door) {
        chrLock.lock();
        try {
            doors.put(owner, door);
        } finally {
            chrLock.unlock();
        }
    }
    
    public void removeDoor(Integer owner) {
        chrLock.lock();
        try {
            doors.remove(owner);
        } finally {
            chrLock.unlock();
        }
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addFriendshipRing(MapleRing r) {
        friendshipRings.add(r);
    }

    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
    }

    public void addMP(int delta) {
        setMp(mp + delta);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        setHp(hp + hpDiff);
        setMp(mp + mpDiff);
        updateSingleStat(MapleStat.HP, getHp());
        updateSingleStat(MapleStat.MP, getMp());
    }

    public void addPet(MaplePet pet) {
        petLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                if (pets[i] == null) {
                    pets[i] = pet;
                    return;
                }
            }
        } finally {
            petLock.unlock();
        }
    }

    public void addStat(int type, int up) {
        if (type == 1) {
            this.str += up;
            updateSingleStat(MapleStat.STR, str);
        } else if (type == 2) {
            this.dex += up;
            updateSingleStat(MapleStat.DEX, dex);
        } else if (type == 3) {
            this.int_ += up;
            updateSingleStat(MapleStat.INT, int_);
        } else if (type == 4) {
            this.luk += up;
            updateSingleStat(MapleStat.LUK, luk);
        }
        recalcLocalStats();
    }

    public int addHP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleJob jobtype = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpMpApUsed() > 9999 || MaxHP >= 30000) {
            return MaxHP;
        }
        if (jobtype.isA(MapleJob.BEGINNER)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.WARRIOR) || jobtype.isA(MapleJob.DAWNWARRIOR1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(10000000) : SkillFactory.getSkill(1000001)) > 0) {
                MaxHP += 20;
            } else {
                MaxHP += 8;
            }
        } else if (jobtype.isA(MapleJob.MAGICIAN) || jobtype.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 6;
        } else if (jobtype.isA(MapleJob.BOWMAN) || jobtype.isA(MapleJob.WINDARCHER1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.THIEF) || jobtype.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.PIRATE) || jobtype.isA(MapleJob.THUNDERBREAKER1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100000) : SkillFactory.getSkill(5100000)) > 0) {
                MaxHP += 18;
            } else {
                MaxHP += 8;
            }
        }
        return MaxHP;
    }

    public int addMP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        if (player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        if (player.getJob().isA(MapleJob.BEGINNER) || player.getJob().isA(MapleJob.NOBLESSE) || player.getJob().isA(MapleJob.LEGEND)) {
            MaxMP += 6;
        } else if (player.getJob().isA(MapleJob.WARRIOR) || player.getJob().isA(MapleJob.DAWNWARRIOR1) || player.getJob().isA(MapleJob.ARAN1)) {
            MaxMP += 2;
        } else if (player.getJob().isA(MapleJob.MAGICIAN) || player.getJob().isA(MapleJob.BLAZEWIZARD1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(12000000) : SkillFactory.getSkill(2000001)) > 0) {
                MaxMP += 18;
            } else {
                MaxMP += 14;
            }

        } else if (player.getJob().isA(MapleJob.BOWMAN) || player.getJob().isA(MapleJob.THIEF)) {
            MaxMP += 10;
        } else if (player.getJob().isA(MapleJob.PIRATE)) {
            MaxMP += 14;
        }

        return MaxMP;
    }
    
    public void addSummon(int id, MapleSummon summon) {
        summons.put(id, summon);
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void ban(String reason) {
        this.isbanned = true;
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")) {
                ps.setString(1, reason);
                ps.setInt(2, accountid);
                ps.executeUpdate();
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static boolean ban(String id, String reason, boolean accountId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }

            boolean ret = false;
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                    Connection con2 = DatabaseConnection.getConnection();

                    try (PreparedStatement psb = con2.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")) {
                            psb.setString(1, reason);
                            psb.setInt(2, rs.getInt(1));
                            psb.executeUpdate();
                    } finally {
                        con2.close();
                    }
                    ret = true;
            }
                        
            rs.close();
            ps.close();
            con.close();
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
		if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
        Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
        if (weapon_item != null) {
            MapleWeaponType weapon = ii.getWeaponType(weapon_item.getItemId());
            int mainstat, secondarystat;
            if (getJob().isA(MapleJob.THIEF) && weapon == MapleWeaponType.DAGGER_OTHER) {
                weapon = MapleWeaponType.DAGGER_THIEVES;
            }

            if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW || weapon == MapleWeaponType.GUN) {
                mainstat = localdex;
                secondarystat = localstr;
            } else if (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER_THIEVES) {
                mainstat = localluk;
                secondarystat = localdex + localstr;
            } else {
                mainstat = localstr;
                secondarystat = localdex;
            }
            maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
        } else {
            if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
                double weapMulti = 3;
                if (job.getId() % 100 != 0) {
                    weapMulti = 4.2;
                }

                int attack = (int) Math.min(Math.floor((2 * getLevel() + 31) / 3), 31);
                maxbasedamage = (int) (localstr * weapMulti + localdex) * attack / 100;
            } else {
                maxbasedamage = 1;
            }
        }
        return maxbasedamage;
    }

    public void setCombo(short count) {
        if (count < combocounter) {
            cancelEffectFromBuffStat(MapleBuffStat.ARAN_COMBO);
        }
        combocounter = (short) Math.min(30000, count);
        if (count > 0) {
            announce(MaplePacketCreator.showCombo(combocounter));
        }
    }

    public void setLastCombo(long time) {
        lastcombo = time;
    }

    public short getCombo() {
        return combocounter;
    }

    public long getLastCombo() {
        return lastcombo;
    }

    public int getLastMobCount() { //Used for skills that have mobCount at 1. (a/b)
        return lastmobcount;
    }

    public void setLastMobCount(byte count) {
        lastmobcount = count;
    }
    
    public boolean cannotEnterCashShop() {
        return blockCashShop;
    }
    
    public void toggleBlockCashShop() {
        blockCashShop = !blockCashShop;
    }

    public void newClient(MapleClient c) {
        this.loggedIn = true;
        c.setAccountName(this.client.getAccountName());//No null's for accountName
        this.client = c;
        MaplePortal portal = map.findClosestPlayerSpawnpoint(getPosition());
        if (portal == null) {
            portal = map.getPortal(0);
        }
        this.setPosition(portal.getPosition());
        this.initialSpawnPoint = portal.getId();
        this.map = c.getChannelServer().getMapFactory().getMap(getMapId());
    }

    public String getMedalText() {
        String medal = "";
        final Item medalItem = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
        }
        return medal;
    }

    public void Hide(boolean hide, boolean login) {
        if (isGM() && hide != this.hidden) {
            if (!hide) {
                this.hidden = false;
                announce(MaplePacketCreator.getGMEffect(0x10, (byte) 0));
                List<MapleBuffStat> dsstat = Collections.singletonList(MapleBuffStat.DARKSIGHT);
                getMap().broadcastGMMessage(this, MaplePacketCreator.cancelForeignBuff(id, dsstat), false);
                getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                
                for(MapleSummon ms: this.getSummonsValues()) {
                    getMap().broadcastNONGMMessage(this, MaplePacketCreator.spawnSummon(ms, false), false);
                }
                
                updatePartyMemberHP();
            } else {
                this.hidden = true;
                announce(MaplePacketCreator.getGMEffect(0x10, (byte) 1));
                if (!login) {
                    getMap().broadcastMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
                }
                getMap().broadcastGMMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
                getMap().broadcastGMMessage(this, MaplePacketCreator.giveForeignBuff(id, dsstat), false);
                for (MapleMonster mon : this.getControlledMonsters()) {
                    mon.setController(null);
                    mon.setControllerHasAggro(false);
                    mon.setControllerKnowsAboutAggro(false);
                    mon.getMap().updateMonsterController(mon);
                }
            }
            announce(MaplePacketCreator.enableActions());
        }
    }
    
    public void Hide(boolean hide) {
        Hide(hide, false);
    }

    public void toggleHide(boolean login) {
        Hide(!isHidden());
    }

    public void cancelMagicDoor() {
        List<MapleBuffStatValueHolder> mbsvhList = getAllStatups();
        for (MapleBuffStatValueHolder mbsvh : mbsvhList) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.announce(MaplePacketCreator.cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
            }
        }
    }

    public static boolean canCreateChar(String name) {
        String lname = name.toLowerCase();
        for (String nameTest : BLOCKED_NAMES) {
            if (lname.contains(nameTest)) {
                return false;
            }
        }
        return getIdByName(name) < 0 && Pattern.compile("[a-zA-Z0-9]{3,12}").matcher(name).matches();
    }

    public boolean canDoor() {
        return canDoor;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (gmLevel > 0) {
            return FameStatus.OK;
        } else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void setMasteries(int jobId) {
        int[] skills = new int[4];
        for (int i = 0; i > skills.length; i++) {
        	skills[i] = 0; //that initialization meng
        }
        if (jobId == 112) {
            skills[0] = Hero.ACHILLES;
            skills[1] = Hero.MONSTER_MAGNET;
            skills[2] = Hero.BRANDISH;
        } else if (jobId == 122) {
            skills[0] = Paladin.ACHILLES;
            skills[1] = Paladin.MONSTER_MAGNET;
            skills[2] = Paladin.BLAST;
        } else if (jobId == 132) {
            skills[0] = DarkKnight.BEHOLDER;
            skills[1] = DarkKnight.ACHILLES;
            skills[2] = DarkKnight.MONSTER_MAGNET;
        } else if (jobId == 212) {
            skills[0] = FPArchMage.BIG_BANG;
            skills[1] = FPArchMage.MANA_REFLECTION;
            skills[2] = FPArchMage.PARALYZE;
        } else if (jobId == 222) {
            skills[0] = ILArchMage.BIG_BANG;
            skills[1] = ILArchMage.MANA_REFLECTION;
            skills[2] = ILArchMage.CHAIN_LIGHTNING;
        } else if (jobId == 232) {
            skills[0] = Bishop.BIG_BANG;
            skills[1] = Bishop.MANA_REFLECTION;
            skills[2] = Bishop.HOLY_SHIELD;
        } else if (jobId == 312) {
            skills[0] = Bowmaster.BOW_EXPERT;
            skills[1] = Bowmaster.HAMSTRING;
            skills[2] = Bowmaster.SHARP_EYES;
        } else if (jobId == 322) {
            skills[0] = Marksman.MARKSMAN_BOOST;
            skills[1] = Marksman.BLIND;
            skills[2] = Marksman.SHARP_EYES;
        } else if (jobId == 412) {
            skills[0] = NightLord.SHADOW_STARS;
            skills[1] = NightLord.SHADOW_SHIFTER;
            skills[2] = NightLord.VENOMOUS_STAR;
        } else if (jobId == 422) {
            skills[0] = Shadower.SHADOW_SHIFTER;
            skills[1] = Shadower.VENOMOUS_STAB;
            skills[2] = Shadower.BOOMERANG_STEP;
        } else if (jobId == 512) {
            skills[0] = Buccaneer.BARRAGE;
            skills[1] = Buccaneer.ENERGY_ORB;
            skills[2] = Buccaneer.SPEED_INFUSION;
            skills[3] = Buccaneer.DRAGON_STRIKE;
        } else if (jobId == 522) {
            skills[0] = Corsair.ELEMENTAL_BOOST;
            skills[1] = Corsair.BULLSEYE;
            skills[2] = Corsair.WRATH_OF_THE_OCTOPI;
            skills[3] = Corsair.RAPID_FIRE;
        } else if (jobId == 2112) {
            skills[0] = Aran.OVER_SWING;
            skills[1] = Aran.HIGH_MASTERY;
            skills[2] = Aran.FREEZE_STANDING;
        } else if (jobId == 2217) {
        	skills[0] = Evan.MAPLE_WARRIOR;
        	skills[1] = Evan.ILLUSION;
        } else if (jobId == 2218) {
        	skills[0] = Evan.BLESSING_OF_THE_ONYX;
        	skills[1] = Evan.BLAZE;
        }
        for (Integer skillId : skills) {
            if (skillId != 0) {
                Skill skill = SkillFactory.getSkill(skillId);
                final int skilllevel = getSkillLevel(skill);
                if(skilllevel > 0) continue;
                
                changeSkillLevel(skill, (byte) 0, 10, -1);
            }
        }
    }

    public void changeJob(MapleJob newJob) {
        if (newJob == null) {
            return;//the fuck you doing idiot!
        }
        this.job = newJob;
        remainingSp[GameConstants.getSkillBook(newJob.getId())] += 1;
        if (GameConstants.hasSPTable(newJob)) {
            remainingSp[GameConstants.getSkillBook(newJob.getId())] += 2;
        } else {
            if (newJob.getId() % 10 == 2) {
                remainingSp[GameConstants.getSkillBook(newJob.getId())] += 2;
            }
        }
        if (newJob.getId() % 10 > 1) {
            this.remainingAp += 5;
        }
        int job_ = job.getId() % 1000; // lame temp "fix"
        if (job_ == 100) {                      // 1st warrior
            maxhp += Randomizer.rand(200, 250);
        } else if (job_ == 200) {               // 1st mage
            maxmp += Randomizer.rand(100, 150);
        } else if (job_ % 100 == 0) {           // 1st others
            maxhp += Randomizer.rand(100, 150);
            maxhp += Randomizer.rand(25, 50);
        } else if (job_ > 0 && job_ < 200) {    // 2nd~4th warrior
            maxhp += Randomizer.rand(300, 350);
        } else if (job_ < 300) {                // 2nd~4th mage
            maxmp += Randomizer.rand(450, 500);
        } else if (job_ > 0) {                  // 2nd~4th others
            maxhp += Randomizer.rand(300, 350);
            maxmp += Randomizer.rand(150, 200);
        }
        
        /*
        //aran perks?
        int newJobId = newJob.getId();
        if(newJobId == 2100) {          // become aran1
            maxhp += 275;
            maxmp += 15;
        } else if(newJobId == 2110) {   // become aran2
            maxmp += 275;
        } else if(newJobId == 2111) {   // become aran3
            maxhp += 275;
            maxmp += 275;
        }
        */
        
        if (maxhp >= 30000) {
            maxhp = 30000;
        }
        if (maxmp >= 30000) {
            maxmp = 30000;
        }
        
        if (!isGM()) {
            for (byte i = 1; i < 5; i++) {
                gainSlots(i, 4, true);
            }
        }
        
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
        statup.add(new Pair<>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
        statup.add(new Pair<>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
        statup.add(new Pair<>(MapleStat.JOB, Integer.valueOf(job.getId())));
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        
        if (dragon != null) {
            getMap().broadcastMessage(MaplePacketCreator.removeDragon(dragon.getObjectId()));
            dragon = null;
        }
        recalcLocalStats();
        silentPartyUpdate();
        if (this.guildid > 0) {
            getGuild().broadcast(MaplePacketCreator.jobMessage(0, job.getId(), name), this.getId());
        }
        setMasteries(this.job.getId());
        guildUpdate();
        
        getMap().broadcastMessage(this, MaplePacketCreator.removePlayerFromMap(this.getId()), false);
        getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 8), false);
        
        if (GameConstants.hasSPTable(newJob) && newJob.getId() != 2001) {
            if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
            }
            createDragon();
        }
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(Integer.valueOf(key), keybinding);
        } else {
            keymap.remove(Integer.valueOf(key));
        }
    }
    
    public MapleMap getWarpMap(int map) {
	MapleMap target;
        EventInstanceManager eim = getEventInstance();
	if (eim == null) {
            target = client.getChannelServer().getMapFactory().getMap(map);
	} else {
            target = eim.getMapInstance(map);
	}
	return target;
    }
    
    // for use ONLY inside OnUserEnter map scripts that requires a player to change map while still moving between maps.
    public void warpAhead(int map) {
        newWarpMap = map;
    }
    
    private void eventChangedMap(int map) {
        EventInstanceManager eim = getEventInstance();
        if (eim != null) eim.changedMap(this, map);
    }
    
    private void eventAfterChangedMap(int map) {
        EventInstanceManager eim = getEventInstance();
        if (eim != null) eim.afterChangedMap(this, map);
    }
    
    public boolean canRecoverLastBanish() {
        return System.currentTimeMillis() - this.banishTime < 5 * 60 * 1000;
    }
    
    public Pair<Integer, Integer> getLastBanishData() {
        return new Pair<>(this.banishMap, this.banishSp);
    }
    
    public void clearBanishPlayerData() {
        this.banishMap = -1;
        this.banishSp = -1;
        this.banishTime = 0;
    }
    
    private void setBanishPlayerData(int banishMap, int banishSp, long banishTime) {
        this.banishMap = banishMap;
        this.banishSp = banishSp;
        this.banishTime = banishTime;
    }
    
    public void changeMapBanish(int mapid, String portal, String msg) {
        if(ServerConstants.USE_SPIKES_AVOID_BANISH) {
            for(Item it: this.getInventory(MapleInventoryType.EQUIPPED).list()) {
                if((it.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES) return;
            }
        }
        
        int banMap = this.getMapId();
        int banSp = this.getMap().findClosestPlayerSpawnpoint(this.getPosition()).getId();
        long banTime = System.currentTimeMillis();
        
        dropMessage(5, msg);
        MapleMap map_ = getWarpMap(mapid);
        changeMap(map_, map_.getPortal(portal));
        
        setBanishPlayerData(banMap, banSp, banTime);
    }

    public void changeMap(int map) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();
        
        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }
        
        changeMap(warpMap, warpMap.getRandomPlayerSpawnpoint());
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();
        
        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();
        
        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();
        
        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, portal);
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }

    public void changeMap(final MapleMap target, final MaplePortal pto) {
        canWarpCounter++;
        
        eventChangedMap(target.getId());    // player can be dropped from an event here, hence the new warping target.
        MapleMap to = getWarpMap(target.getId());
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this));
        canWarpMap = false;
        
        canWarpCounter--;
        if(canWarpCounter == 0) canWarpMap = true;
        
        eventAfterChangedMap(this.getMapId());
    }

    public void changeMap(final MapleMap target, final Point pos) {
        canWarpCounter++;
        
        eventChangedMap(target.getId());
        MapleMap to = getWarpMap(target.getId());
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x80, this));//Position :O (LEFT)
        canWarpMap = false;
        
        canWarpCounter--;
        if(canWarpCounter == 0) canWarpMap = true;
        
        eventAfterChangedMap(this.getMapId());
    }
    
    private boolean buffMapProtection() {
        effLock.lock();
        chrLock.lock();
        try {
            MapleMap thisMap = client.getChannelServer().getMapFactory().getMap(mapid);
            
            for(Entry<MapleBuffStat, MapleBuffStatValueHolder> mbs : effects.entrySet()) {
                if(mbs.getKey() == MapleBuffStat.MAP_PROTECTION) {
                    byte value = (byte)mbs.getValue().value;
                    
                    if(value == 1 && (thisMap.getReturnMapId() == 211000000 || thisMap.getReturnMapId() == 193000000)) return true;       //protection from cold
                    else if(value == 2 && (thisMap.getReturnMapId() == 211000000 || thisMap.getReturnMapId() == 230000000)) return true;  //breathing underwater
                    else return false;
                }
            }    
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
        
        for(Item it: this.getInventory(MapleInventoryType.EQUIPPED).list()) {
            if((it.getFlag() & ItemConstants.COLD) == ItemConstants.COLD && map.getReturnMapId() == 211000000) return true;       //protection from cold
        }
        
        return false;
    }
    
    private void changeMapInternal(final MapleMap to, final Point pos, final byte[] warpPacket) {
        if(!canWarpMap) return;
        
        this.mapTransitioning.set(true);
        
        this.unregisterChairBuff();
        this.clearBanishPlayerData();
        this.closePlayerInteractions();
        this.resetPlayerAggro();
        
        client.announce(warpPacket);
        map.removePlayer(this);
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            map = to;
            setPosition(pos);
            map.addPlayer(this);
            
            prtLock.lock();
            try {
                if (party != null) {
                    mpc.setMapId(to.getId());
                    silentPartyUpdateInternal();
                    client.announce(MaplePacketCreator.updateParty(client.getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                    updatePartyMemberHPInternal();
                }
            } finally {
                prtLock.unlock();
            }
            
            if (getMap().getHPDec() > 0) resetHpDecreaseTask();
        }
        else {
            FilePrinter.printError(FilePrinter.MAPLE_MAP, "Character " + this.getName() + " got stuck when moving to map " + map.getId() + ".");
        }
        
        //alas, new map has been specified when a warping was being processed...
        if(newWarpMap != -1) {
            canWarpMap = true;
            
            int temp = newWarpMap;
            newWarpMap = -1;
            changeMap(temp);
        } else {
            // if this event map has a gate already opened, render it
            EventInstanceManager eim = getEventInstance();
            if(eim != null) {
                eim.recoverOpenedGate(this, map.getId());
            }

            // if this map has obstacle components moving, make it do so for this client
            for(Entry<String, Integer> e: map.getEnvironment().entrySet()) {
                announce(MaplePacketCreator.environmentMove(e.getKey(), e.getValue()));
            }
        }
    }
    
    public boolean isChangingMaps() {
        return this.mapTransitioning.get();
    }
    
    public void setMapTransitionComplete() {
        this.mapTransitioning.set(false);
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(Skill skill, byte newLevel, int newMasterlevel, long expiration) {
        if (newLevel > -1) {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
            if (!GameConstants.isHiddenSkills(skill.getId())) {
                this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, expiration));
            }
        } else {
            skills.remove(skill);
            this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, -1)); //Shouldn't use expiration anymore :)
            try {
                Connection con = DatabaseConnection.getConnection();
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND characterid = ?")) {
                    ps.setInt(1, skill.getId());
                    ps.setInt(2, id);
                    ps.execute();
                } finally {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkBerserk(final boolean isHidden) {
        if (berserkSchedule != null) {
            berserkSchedule.cancel(false);
        }
        final MapleCharacter chr = this;
        if (job.equals(MapleJob.DARKKNIGHT)) {
            Skill BerserkX = SkillFactory.getSkill(DarkKnight.BERSERK);
            final int skilllevel = getSkillLevel(BerserkX);
            if (skilllevel > 0) {
                berserk = chr.getHp() * 100 / chr.getMaxHp() < BerserkX.getEffect(skilllevel).getX();
                berserkSchedule = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        if(awayFromWorld.get()) return;
                        
                        client.announce(MaplePacketCreator.showOwnBerserk(skilllevel, berserk));
                        if(!isHidden) getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, berserk), false);
                        else getMap().broadcastGMMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, berserk), false);
                    }
                }, 5000, 3000);
            }
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            World worldz = Server.getInstance().getWorld(world);
            worldz.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(this, messengerposition), messengerposition);
            worldz.updateMessenger(getMessenger().getId(), name, client.getChannel());
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = null;
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.announce(MaplePacketCreator.controlMonster(monster, false, aggro));
    }
    
    private static boolean useItem(final MapleClient c, final int id) {
        if (id / 1000000 == 2) {
            if (ii.isConsumeOnPickup(id)) {
                if (ItemConstants.isPartyItem(id)) {
                    List<MapleCharacter> pchr = c.getPlayer().getPartyMembersOnSameMap();
                    
                    if(!ItemConstants.isPartyAllcure(id)) {
                        if(!pchr.isEmpty()) {
                            for (MapleCharacter mc : pchr) {
                                ii.getItemEffect(id).applyTo(mc);
                                mc.checkBerserk(mc.isHidden());
                            }
                        } else {
                            ii.getItemEffect(id).applyTo(c.getPlayer());
                            c.getPlayer().checkBerserk(c.getPlayer().isHidden());
                        }
                    } else {
                        if(!pchr.isEmpty()) {
                            for (MapleCharacter mc : pchr) {
                                mc.dispelDebuffs();
                            }
                        } else {
                            c.getPlayer().dispelDebuffs();
                        }
                    }
                } else {
                    ii.getItemEffect(id).applyTo(c.getPlayer());
                    c.getPlayer().checkBerserk(c.getPlayer().isHidden());
                }
                return true;
            }
        }
        return false;
    }
    
    public final void pickupItem(MapleMapObject ob) {
        pickupItem(ob, -1);
    }
    
    public final void pickupItem(MapleMapObject ob, int petIndex) {     // yes, one picks the MapleMapObject, not the MapleMapItem
        if (ob == null) {                                               // pet index refers to the one picking up the item
            return;
        }
		
        if (ob instanceof MapleMapItem) {
            MapleMapItem mapitem = (MapleMapItem) ob;
            if(System.currentTimeMillis() - mapitem.getDropTime() < 900) {
                client.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            boolean isPet = petIndex > -1;
            final byte[] pickupPacket = MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), (isPet) ? 5 : 2, this.getId(), isPet, petIndex);
            
            boolean hasSpaceInventory = true;
            if (mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866 || mapitem.getMeso() > 0 || ii.isConsumeOnPickup(mapitem.getItemId()) || (hasSpaceInventory = MapleInventoryManipulator.checkSpace(client, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner()))) {
                if ((this.getMapId() > 209000000 && this.getMapId() < 209000016) || (this.getMapId() >= 990000500 && this.getMapId() <= 990000502)) {//happyville trees and guild PQ
                    if (!mapitem.isPlayerDrop() || mapitem.getDropper().getObjectId() == client.getPlayer().getObjectId()) {
                        if(mapitem.getMeso() > 0) {
                            this.gainMeso(mapitem.getMeso(), true, true, false);
                            this.getMap().pickItemDrop(pickupPacket, mapitem);
                        } else if(mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866) {
                            // Add NX to account, show effect and make item disappear
                            int nxGain = mapitem.getItemId() == 4031865 ? 100 : 250;
                            this.getCashShop().gainCash(1, nxGain);
                            
                            showHint("You have earned #e#b" + nxGain + " NX#k#n. (" + this.getCashShop().getCash(1) + " NX)");
                            
                            this.getMap().pickItemDrop(pickupPacket, mapitem);
                        } else if (MapleInventoryManipulator.addFromDrop(client, mapitem.getItem(), true)) {
                            this.getMap().pickItemDrop(pickupPacket, mapitem);
                        } else {
                            client.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                    } else {
                        client.announce(MaplePacketCreator.showItemUnavailable());
                        client.announce(MaplePacketCreator.enableActions());
                        return;
                    }
                    client.announce(MaplePacketCreator.enableActions());
                    return;
                }
            
                synchronized (mapitem) {
                    if (mapitem.getQuest() > 0 && !this.needQuestItem(mapitem.getQuest(), mapitem.getItemId())) {
                        client.announce(MaplePacketCreator.showItemUnavailable());
                        client.announce(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (mapitem.isPickedUp()) {
                        client.announce(MaplePacketCreator.showItemUnavailable());
                        client.announce(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (mapitem.getMeso() > 0) {
                        prtLock.lock();
                        try {
                            if (this.party != null) {
                                int mesosamm = mapitem.getMeso();
                                if (mesosamm > 50000 * this.getMesoRate()) {
                                    return;
                                }
                                int partynum = 0;
                                for (MaplePartyCharacter partymem : this.party.getMembers()) {
                                    if (partymem.isOnline() && partymem.getMapId() == this.getMap().getId() && partymem.getChannel() == client.getChannel()) {
                                        partynum++;
                                    }
                                }
                                for (MaplePartyCharacter partymem : this.party.getMembers()) {
                                    if (partymem.isOnline() && partymem.getMapId() == this.getMap().getId()) {
                                        MapleCharacter somecharacter = client.getChannelServer().getPlayerStorage().getCharacterById(partymem.getId());
                                        if (somecharacter != null) {
                                            somecharacter.gainMeso(mesosamm / partynum, true, true, false);
                                        }
                                    }
                                }
                            } else {
                                this.gainMeso(mapitem.getMeso(), true, true, false);
                            }
                        } finally {
                            prtLock.unlock();
                        }
                    } else if (mapitem.getItem().getItemId() / 10000 == 243) {
                        MapleItemInformationProvider.scriptedItem info = ii.getScriptedItemInfo(mapitem.getItem().getItemId());
                        if (info.runOnPickup()) {
                            ItemScriptManager ism = ItemScriptManager.getInstance();
                            String scriptName = info.getScript();
                            if (ism.scriptExists(scriptName)) {
                                ism.getItemScript(client, scriptName);
                            }

                        } else {
                            if (!MapleInventoryManipulator.addFromDrop(client, mapitem.getItem(), true)) {
                                client.announce(MaplePacketCreator.enableActions());
                                return;
                            }
                        }
                    } else if(mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866) {
                        // Add NX to account, show effect and make item disappear
                        int nxGain = mapitem.getItemId() == 4031865 ? 100 : 250;
                        this.getCashShop().gainCash(1, nxGain);
                        
                        showHint("You have earned #e#b" + nxGain + " NX#k#n. (" + this.getCashShop().getCash(1) + " NX)");
                    } else if (useItem(client, mapitem.getItem().getItemId())) {
                        if (mapitem.getItem().getItemId() / 10000 == 238) {
                            this.getMonsterBook().addCard(client, mapitem.getItem().getItemId());
                        }
                    } else if (MapleInventoryManipulator.addFromDrop(client, mapitem.getItem(), true)) {
                    } else if (mapitem.getItem().getItemId() == 4031868) {
                        this.getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(this.getName(), this.getItemQuantity(4031868, false), false));
                    } else {
                        client.announce(MaplePacketCreator.enableActions());
                        return;
                    }
                    
                    this.getMap().pickItemDrop(pickupPacket, mapitem);
                }
            } else if(!hasSpaceInventory) {
                client.announce(MaplePacketCreator.getInventoryFull());
                client.announce(MaplePacketCreator.getShowInventoryFull());
            }
        }
        client.announce(MaplePacketCreator.enableActions());
    }

    public int countItem(int itemid) {
        return inventory[ii.getInventoryType(itemid).ordinal()].countById(itemid);
    }
    
    public boolean canHold(int itemid) {
        return canHold(itemid, 1);
    }
        
    public boolean canHold(int itemid, int quantity) {
        int hold = getCleanItemQuantity(itemid, false);
        
        if(hold > 0) {
            if(hold + quantity <= ii.getSlotMax(client, itemid))
                return true;
        }

        return getInventory(ii.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public void decreaseBattleshipHp(int decrease) {
        this.battleshipHp -= decrease;
        if (battleshipHp <= 0) {
            this.battleshipHp = 0;
            Skill battleship = SkillFactory.getSkill(Corsair.BATTLE_SHIP);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            announce(MaplePacketCreator.skillCooldown(Corsair.BATTLE_SHIP, cooldown));
            addCooldown(Corsair.BATTLE_SHIP, System.currentTimeMillis(), (long)(cooldown * 1000));
            removeCooldown(5221999);
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        } else {
            announce(MaplePacketCreator.skillCooldown(5221999, battleshipHp / 10));   //:D
            addCooldown(5221999, 0, Long.MAX_VALUE);
        }
    }
    
    public void decreaseReports() {
        this.possibleReports--;
    }

    public void deleteGuild(int guildId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?")) {
                ps.setInt(1, guildId);
                ps.execute();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?")) {
                ps.setInt(1, id);
                ps.execute();
            } finally {
                con.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void nextPendingRequest(MapleClient c) {
        CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            c.announce(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
    }
    
    private void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, BuddyList.BuddyOperation operation) {
        MapleCharacter player = c.getPlayer();
        if (remoteChannel != -1) {
            c.getWorldServer().buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation);
        }
    }
    
    public void deleteBuddy(int otherCid) {
        BuddyList bl = getBuddylist();
        
        if (bl.containsVisible(otherCid)) {
            notifyRemoteChannel(client, client.getWorldServer().find(otherCid), otherCid, BuddyList.BuddyOperation.DELETED);
        }
        bl.remove(otherCid);
        client.announce(MaplePacketCreator.updateBuddylist(getBuddylist().getBuddies()));
        nextPendingRequest(client);
    }
    
    public static boolean deleteCharFromDB(MapleCharacter player) {
            int cid = player.getId(), accId = -1, world = 0;
            
            Connection con = null;
            try {
                    con = DatabaseConnection.getConnection();
                    
                    try (PreparedStatement ps = con.prepareStatement("SELECT accountid, world FROM characters WHERE id = ?")) {
                            ps.setInt(1, cid);

                            try (ResultSet rs = ps.executeQuery()) {
                                    if(rs.next()) {
                                            accId = rs.getInt("accountid");
                                            world = rs.getInt("world");
                                    }
                            }
                    }
                    
                    try (PreparedStatement ps = con.prepareStatement("SELECT buddyid FROM buddies WHERE characterid = ?")) {
                            ps.setInt(1, cid);

                            try (ResultSet rs = ps.executeQuery()) {
                                    while(rs.next()) {
                                            int buddyid = rs.getInt("buddyid");
                                            MapleCharacter buddy = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterById(buddyid);
                                            
                                            if(buddy != null) {
                                                    buddy.deleteBuddy(cid);
                                            }
                                    }
                            }
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM buddies WHERE characterid = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    
                    try (PreparedStatement ps = con.prepareStatement("SELECT threadid FROM bbs_threads WHERE postercid = ?")) {
                            ps.setInt(1, cid);
                            
                            try (ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                            int tid = rs.getInt("threadid");
                                        
                                            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM bbs_replies WHERE threadid = ?")) {
                                                    ps2.setInt(1, tid);
                                                    ps2.executeUpdate();
                                            }
                                    }
                            }
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM bbs_threads WHERE postercid = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    
                    try (PreparedStatement ps = con.prepareStatement("SELECT id, guildid, guildrank, name, allianceRank FROM characters WHERE id = ? AND accountid = ?")) {
                            ps.setInt(1, cid);
                            ps.setInt(2, accId);
                            try (ResultSet rs = ps.executeQuery()) {
                                    if (rs.next() && rs.getInt("guildid") > 0) {
                                            Server.getInstance().deleteGuildCharacter(new MapleGuildCharacter(player, cid, 0, rs.getString("name"), (byte) -1, (byte) -1, 0, rs.getInt("guildrank"), rs.getInt("guildid"), false, rs.getInt("allianceRank")));
                                    }
                            }
                    }
                    
                    if(con.isClosed()) con = DatabaseConnection.getConnection();    //wtf tho

                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM wishlists WHERE charid = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM area_info WHERE charid = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM characters WHERE id = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM famelog WHERE characterid_to = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    
                    try (PreparedStatement ps = con.prepareStatement("SELECT inventoryitemid, petid FROM inventoryitems WHERE characterid = ?")) {
                            ps.setInt(1, cid);
                            
                            try (ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                            int inventoryitemid = rs.getInt("inventoryitemid");
                                        
                                            try (PreparedStatement ps2 = con.prepareStatement("SELECT ringid FROM inventoryequipment WHERE inventoryitemid = ?")) {
                                                    ps2.setInt(1, inventoryitemid);

                                                    try (ResultSet rs2 = ps2.executeQuery()) {
                                                            while (rs2.next()) {
                                                                    int ringid = rs2.getInt("ringid");
                                                                    
                                                                    if(ringid > -1) {
                                                                            try (PreparedStatement ps3 = con.prepareStatement("DELETE FROM rings WHERE id = ?")) {
                                                                                    ps3.setInt(1, ringid);
                                                                                    ps3.executeUpdate();
                                                                            }
                                                                    }
                                                            }
                                                    }
                                            }
                                        
                                            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM inventoryequipment WHERE inventoryitemid = ?")) {
                                                    ps2.setInt(1, inventoryitemid);
                                                    ps2.executeUpdate();
                                            }
                                            
                                            if(rs.getInt("petid") > -1) {
                                                    try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM pets WHERE petid = ?")) {
                                                            ps2.setInt(1, rs.getInt("petid"));
                                                            ps2.executeUpdate();
                                                    }
                                            }
                                    }
                            }
                    }
                    
                    try (PreparedStatement ps = con.prepareStatement("SELECT queststatusid FROM queststatus WHERE characterid = ?")) {
                            ps.setInt(1, cid);
                            
                            try (ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                            int queststatusid = rs.getInt("queststatusid");
                                        
                                            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM medalmaps WHERE queststatusid = ?")) {
                                                    ps2.setInt(1, queststatusid);
                                                    ps2.executeUpdate();
                                            }
                                            
                                            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM questprogress WHERE queststatusid = ?")) {
                                                    ps2.setInt(1, queststatusid);
                                                    ps2.executeUpdate();
                                            }
                                    }
                            }
                    }
                    
                    try (PreparedStatement ps = con.prepareStatement("SELECT id FROM mts_cart WHERE cid = ?")) {
                            ps.setInt(1, cid);
                            
                            try (ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                            int mtsid = rs.getInt("id");
                                        
                                            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM mts_items WHERE id = ?")) {
                                                    ps2.setInt(1, mtsid);
                                                    ps2.executeUpdate();
                                            }
                                    }
                            }
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM mts_cart WHERE cid = ?")) {
                            ps.setInt(1, cid);
                            ps.executeUpdate();
                    }
                    
                    String[] toDel = {"famelog", "inventoryitems", "keymap", "queststatus", "savedlocations", "trocklocations", "skillmacros", "skills", "eventstats", "server_queue"};
                    for (String s : toDel) {
                            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM `" + s + "` WHERE characterid = ?", cid);
                    }
                    
                    con.close();
                    return true;
            } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
            }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static void deleteWhereCharacterId(Connection con, String sql, int cid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ps.executeUpdate();
        }
    }

    private void stopChairTask() {
        chrLock.lock();
        try {
            if (chairRecoveryTask != null) {
                chairRecoveryTask.cancel(false);
                chairRecoveryTask = null;
            }
        } finally {
            chrLock.unlock();
        }
    }
    
    private void startChairTask() {
        if(chair.get() == 0) return;
        
        final int healInterval = 5000;
        final byte healHP = (byte) Math.max(ServerConstants.CHAIR_EXTRA_HEAL_HP, 1);
        final byte healMP = (byte) Math.max(ServerConstants.CHAIR_EXTRA_HEAL_MP, 0);
        
        chrLock.lock();
        try {
            chairRecoveryTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    if(hp < localmaxhp) {
                        client.announce(MaplePacketCreator.showOwnRecovery(healHP));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showRecovery(id, healHP), false);
                    }

                    addHP(healHP);
                    addMP(healMP);
                }
            }, healInterval, healInterval);
        } finally {
            chrLock.unlock();
        }
    }
    
    private void stopExtraTask() {
        chrLock.lock();
        try {
            if (extraRecoveryTask != null) {
                extraRecoveryTask.cancel(false);
                extraRecoveryTask = null;
            }
        } finally {
            chrLock.unlock();
        }
    }
    
    private void startExtraTask(final byte healHP, final byte healMP, final short healInterval) {
        chrLock.lock();
        try {
            startExtraTaskInternal(healHP, healMP, healInterval);
        } finally {
            chrLock.unlock();
        }
    }
    
    private void startExtraTaskInternal(final byte healHP, final byte healMP, final short healInterval) {
        extraRecInterval = healInterval;

        extraRecoveryTask = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (getBuffSource(MapleBuffStat.HPREC) == -1 && getBuffSource(MapleBuffStat.MPREC) == -1) {
                    stopExtraTask();
                    return;
                }
                
                if(hp < localmaxhp) {
                    if(healHP > 0) {
                        client.announce(MaplePacketCreator.showOwnRecovery(healHP));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showRecovery(id, healHP), false);
                    }
                }

                addHP(healHP);
                addMP(healMP);
            }
        }, healInterval, healInterval);
    }

    public void disableDoorSpawn() {
        canDoor = false;
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                canDoor = true;
            }
        }, 5000);
    }

    public void disbandGuild() {
        if (guildid < 1 || guildRank != 1) {
            return;
        }
        try {
            Server.getInstance().disbandGuild(guildid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dispel() {
        List<MapleBuffStatValueHolder> mbsvhList = getAllStatups();
        for (MapleBuffStatValueHolder mbsvh : mbsvhList) {
            if (mbsvh.effect.isSkill()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public final boolean hasDisease(final MapleDisease dis) {
        chrLock.lock();
        try {
            return diseases.containsKey(dis);
        } finally {
            chrLock.unlock();
        }
    }
    
    public final int getDiseasesSize() {
        chrLock.lock();
        try {
            return diseases.size();
        } finally {
            chrLock.unlock();
        }
    }

    public void giveDebuff(final MapleDisease disease, MobSkill skill) {
        if (!hasDisease(disease) && getDiseasesSize() < 2) {
            if (!(disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN)) {
                if (isActiveBuffedValue(Bishop.HOLY_SHIELD)) {
                    return;
                }
            }
            
            chrLock.lock();
            try {
                long curTime = System.currentTimeMillis();
                diseaseExpires.put(disease, curTime + skill.getDuration());
                diseases.put(disease, new MapleDiseaseValueHolder(curTime, skill.getDuration()));
            } finally {
                chrLock.unlock();
            }
            
            final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<>(disease, Integer.valueOf(skill.getX())));
            client.announce(MaplePacketCreator.giveDebuff(debuff, skill));
            map.broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(id, debuff, skill), false);
        }
    }

    public void dispelDebuff(MapleDisease debuff) {
        if (hasDisease(debuff)) {
            long mask = debuff.getValue();
            announce(MaplePacketCreator.cancelDebuff(mask));
            map.broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);

            chrLock.lock();
            try {
                diseases.remove(debuff);
                diseaseExpires.remove(debuff);
            } finally {
                chrLock.unlock();
            }
        }
    }

    public void dispelDebuffs() {
        dispelDebuff(MapleDisease.CURSE);
        dispelDebuff(MapleDisease.DARKNESS);
        dispelDebuff(MapleDisease.POISON);
        dispelDebuff(MapleDisease.SEAL);
        dispelDebuff(MapleDisease.WEAKEN);
        dispelDebuff(MapleDisease.SLOW);
    }

    public void cancelAllDebuffs() {
        chrLock.lock();
        try {
            diseases.clear();
        } finally {
            chrLock.unlock();
        }
    }
    
    public void dispelSkill(int skillid) {
        List<MapleBuffStatValueHolder> allBuffs = getAllStatups();
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004 || dispelSkills(mbsvh.effect.getSourceId()))) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    private boolean dispelSkills(int skillid) {
        switch (skillid) {
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Priest.SUMMON_DRAGON:
            case Bishop.BAHAMUT:
            case Ranger.PUPPET:
            case Ranger.SILVER_HAWK:
            case Sniper.PUPPET:
            case Sniper.GOLDEN_EAGLE:
            case Hermit.SHADOW_PARTNER:
                return true;
            default:
                return false;
        }
    }

    private void doHurtHp() {
        if (!(this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null || buffMapProtection())) {
            addHP(-getMap().getHPDec());
            lastHpDec = System.currentTimeMillis();
        }
        
        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                doHurtHp();
            }
        }, 10000);
    }
    
    public void resetHpDecreaseTask() {
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
        
        long lastHpTask = System.currentTimeMillis() - lastHpDec;
        if(lastHpTask >= 10000) {
            doHurtHp();
        } else {
            hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    doHurtHp();
                }
            }, 10000 - lastHpTask);
        }
    }
    
    public void dropMessage(String message) {
        dropMessage(0, message);
    }

    public void dropMessage(int type, String message) {
        client.announce(MaplePacketCreator.serverNotice(type, message));
    }

    public String emblemCost() {
        return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
    }

    public List<ScheduledFuture<?>> getTimers() {
        return timers;
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<>(2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<>(MapleStat.MP, Integer.valueOf(getMp())));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<>(MapleStat.HP, Integer.valueOf(getHp())));
        }
        if (stats.size() > 0) {
            client.announce(MaplePacketCreator.updatePlayerStats(stats, this));
        }
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid)) {
            entered.put(mapid, script);
        }
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        if (getMessenger() != null) {
            Server.getInstance().getWorld(world).updateMessenger(getMessenger(), getName(), getWorld(), client.getChannel());
        }
    }
    
    public void cancelDiseaseExpireTask() {
        if (diseaseExpireTask != null) {
            diseaseExpireTask.cancel(false);
            diseaseExpireTask = null;
        }
    }

    public void diseaseExpireTask() {
        if (diseaseExpireTask == null) {
            diseaseExpireTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    Set<MapleDisease> toExpire = new LinkedHashSet<>();
                    
                    chrLock.lock();
                    try {
                        long curTime = System.currentTimeMillis();
                        
                        for(Entry<MapleDisease, Long> d : diseaseExpires.entrySet()) {
                            if(d.getValue() < curTime) {
                                toExpire.add(d.getKey());
                            }
                        }
                    } finally {
                        chrLock.unlock();
                    }
                    
                    for(MapleDisease d : toExpire) {
                        dispelDebuff(d);
                    }
                }
            }, 1500);
        }
    }
    
    public void cancelBuffExpireTask() {
        if (buffExpireTask != null) {
            buffExpireTask.cancel(false);
            buffExpireTask = null;
        }
    }

    public void buffExpireTask() {
        if (buffExpireTask == null) {
            buffExpireTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    Set<Entry<Integer, Long>> es;
                    List<MapleBuffStatValueHolder> toCancel = new ArrayList<>();
                    
                    effLock.lock();
                    chrLock.lock();
                    try {
                        es = new LinkedHashSet<>(buffExpires.entrySet());
                        
                        long curTime = System.currentTimeMillis();
                        for(Entry<Integer, Long> bel : es) {
                            if(curTime >= bel.getValue()) {
                                toCancel.add(buffEffects.get(bel.getKey()).entrySet().iterator().next().getValue());    //rofl
                            }
                        }
                    } finally {
                        chrLock.unlock();
                        effLock.unlock();
                    }
                    
                    for(MapleBuffStatValueHolder mbsvh : toCancel) {
                        cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                    }
                }
            }, 1500);
        }
    }
    
    public void cancelSkillCooldownTask() {
        if (skillCooldownTask != null) {
            skillCooldownTask.cancel(false);
            skillCooldownTask = null;
        }
    }

    public void skillCooldownTask() {
        if (skillCooldownTask == null) {
            skillCooldownTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    Set<Entry<Integer, MapleCoolDownValueHolder>> es;
                    
                    effLock.lock();
                    chrLock.lock();
                    try {
                        es = new LinkedHashSet<>(coolDowns.entrySet());
                    } finally {
                        chrLock.unlock();
                        effLock.unlock();
                    }
                    
                    long curTime = System.currentTimeMillis();
                    for(Entry<Integer, MapleCoolDownValueHolder> bel : es) {
                        MapleCoolDownValueHolder mcdvh = bel.getValue();
                        if(curTime >= mcdvh.startTime + mcdvh.length) {
                            removeCooldown(mcdvh.skillId);
                            client.announce(MaplePacketCreator.skillCooldown(mcdvh.skillId, 0));
                        }
                    }
                }
            }, 1500);
        }
    }
    
    public void cancelExpirationTask() {
        if (itemExpireTask != null) {
            itemExpireTask.cancel(false);
            itemExpireTask = null;
        }
    }

    public void expirationTask() {
        if (itemExpireTask == null) {
            itemExpireTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    boolean deletedCoupon = false;
                    
                    long expiration, currenttime = System.currentTimeMillis();
                    Set<Skill> keys = getSkills().keySet();
                    for (Iterator<Skill> i = keys.iterator(); i.hasNext();) {
                        Skill key = i.next();
                        SkillEntry skill = getSkills().get(key);
                        if (skill.expiration != -1 && skill.expiration < currenttime) {
                            changeSkillLevel(key, (byte) -1, 0, -1);
                        }
                    }

                    List<Item> toberemove = new ArrayList<>();
                    for (MapleInventory inv : inventory) {
                        for (Item item : inv.list()) {
                            expiration = item.getExpiration();
                            
                            if (expiration != -1 && (expiration < currenttime) && ((item.getFlag() & ItemConstants.LOCK) == ItemConstants.LOCK)) {
                                byte aids = item.getFlag();
                                aids &= ~(ItemConstants.LOCK);
                                item.setFlag(aids); //Probably need a check, else people can make expiring items into permanent items...
                                item.setExpiration(-1);
                                forceUpdateItem(item);   //TEST :3
                            } else if (expiration != -1 && expiration < currenttime) {
                                if(!ItemConstants.isPet(item.getItemId()) || ServerConstants.USE_ERASE_PET_ON_EXPIRATION) {
                                    client.announce(MaplePacketCreator.itemExpired(item.getItemId()));
                                    toberemove.add(item);
                                    if(ItemConstants.isRateCoupon(item.getItemId())) {
                                        deletedCoupon = true;
                                    }
                                } else {
                                    item.setExpiration(-1);
                                    forceUpdateItem(item);
                                }
                            }
                        }
                        for (Item item : toberemove) {
                            if(item.getPetId() > -1) unequipPet(getPet(getPetIndex(item.getPetId())), true);
                            MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
                        }
                        toberemove.clear();
                        
                        if(deletedCoupon) {
                            updateCouponRates();
                        }
                    }
                }
            }, 60000);
        }
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public void forceUpdateItem(Item item) {
        final List<ModifyInventory> mods = new LinkedList<>();
        mods.add(new ModifyInventory(3, item));
        mods.add(new ModifyInventory(0, item));
        client.announce(MaplePacketCreator.modifyInventory(true, mods));
    }

    public void gainGachaExp() {
        int expgain = 0;
        int currentgexp = gachaexp.get();
        if ((currentgexp + exp.get()) >= ExpTable.getExpNeededForLevel(level)) {
            expgain += ExpTable.getExpNeededForLevel(level) - exp.get();
            int nextneed = ExpTable.getExpNeededForLevel(level + 1);
            if ((currentgexp - expgain) >= nextneed) {
                expgain += nextneed;
            }
            this.gachaexp.set(currentgexp - expgain);
        } else {
            expgain = this.gachaexp.getAndSet(0);
        }
        gainExp(expgain, false, false);
        updateSingleStat(MapleStat.GACHAEXP, this.gachaexp.get());
    }

    public void gainGachaExp(int gain) {
        updateSingleStat(MapleStat.GACHAEXP, gachaexp.addAndGet(gain));
    }
    
    public void gainExp(int gain) {
        gainExp(gain, true, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }
    
    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        gainExp(gain, 0, show, inChat, white);
    }

    public void gainExp(int gain, int party, boolean show, boolean inChat, boolean white) {
        if (hasDisease(MapleDisease.CURSE)) {
            gain *= 0.5;
            party *= 0.5;
        }
	
        if(gain < 0) gain = Integer.MAX_VALUE;   // integer overflow, heh.
        if(party < 0) party = Integer.MAX_VALUE;   // integer overflow, heh.
        int equip = (int)Math.min((long)((gain / 10) * pendantExp), Integer.MAX_VALUE);
        
        long total = (long)gain + equip + party;
        gainExpInternal(total, equip, party, show, inChat, white);
    }
    
    public void loseExp(int loss, boolean show, boolean inChat) {
        loseExp(loss, show, inChat, true);
    }
    
    public void loseExp(int loss, boolean show, boolean inChat, boolean white) {
        gainExpInternal(-loss, 0, 0, show, inChat, white);
    }
    
    private void gainExpInternal(long gain, int equip, int party, boolean show, boolean inChat, boolean white) {
        long total = Math.max(gain, -exp.get());
        
        if (level < getMaxLevel()) {
            long leftover = 0;
            long nextExp = exp.get() + total;
            
            if (nextExp > (long)Integer.MAX_VALUE) {
                total = Integer.MAX_VALUE - exp.get();
                leftover = nextExp - Integer.MAX_VALUE;
            }
            updateSingleStat(MapleStat.EXP, exp.addAndGet((int)total));
            if (show && gain != 0) {
                client.announce(MaplePacketCreator.getShowExpGain((int)Math.min(gain, Integer.MAX_VALUE), equip, party, inChat, white));
            }
            while (exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                levelUp(true);
                if (level == getMaxLevel()) {
                    setExp(0);
                    updateSingleStat(MapleStat.EXP, 0);
                    break;
                }
            }
            
            if(leftover > 0) gainExpInternal(leftover, equip, party, false, inChat, white);
        }
    }

    public void gainFame(int delta) {
        this.addFame(delta);
        this.updateSingleStat(MapleStat.FAME, this.fame);
    }
    
    public void gainMeso(int gain) {
        gainMeso(gain, true, false, true);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        if (meso.get() + gain < 0) {
            client.announce(MaplePacketCreator.enableActions());
            return;
        }
        updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), enableActions);
        if (show) {
            client.announce(MaplePacketCreator.getShowMesoGain(gain, inChat));
        }
    }

    public void genericGuildMessage(int code) {
        this.client.announce(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public int getAccountID() {
        return accountid;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<>();
        
        effLock.lock();
        chrLock.lock();
        try {
            for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
                ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
        
        return ret;
    }

    public int getAllianceRank() {
        return allianceRank;
    }

    public int getAllowWarpToId() {
        return warpToId;
    }

    public static String getAriantRoomLeaderName(int room) {
        return ariantroomleader[room];
    }

    public static int getAriantSlotsRoom(int room) {
        return ariantroomslot[room];
    }

    public int getBattleshipHp() {
        return battleshipHp;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public static Map<String, String> getCharacterFromDatabase(String name) {
        Map<String, String> character = new LinkedHashMap<>();

        try {
            Connection con = DatabaseConnection.getConnection();
            
            try (PreparedStatement ps = con.prepareStatement("SELECT `id`, `accountid`, `name` FROM `characters` WHERE `name` = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }

                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        character.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                    }
                }
            } finally {
                con.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return character;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        effLock.lock();
        chrLock.lock();
        try {
            MapleBuffStatValueHolder mbsvh = effects.get(effect);
            if (mbsvh == null) {
                return null;
            }
            return Long.valueOf(mbsvh.startTime);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        effLock.lock();
        chrLock.lock();
        try {
            MapleBuffStatValueHolder mbsvh = effects.get(effect);
            if (mbsvh == null) {
                return null;
            }
            return Integer.valueOf(mbsvh.value);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public int getBuffSource(MapleBuffStat stat) {
        effLock.lock();
        chrLock.lock();
        try {
            MapleBuffStatValueHolder mbsvh = effects.get(stat);
            if (mbsvh == null) {
                return -1;
            }
            return mbsvh.effect.getSourceId();
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public MapleStatEffect getBuffEffect(MapleBuffStat stat) {
        effLock.lock();
        chrLock.lock();
        try {
            MapleBuffStatValueHolder mbsvh = effects.get(stat);
            if (mbsvh == null) {
                return null;
            } else {
                return mbsvh.effect;
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public Set<Integer> getAvailableBuffs() {
        effLock.lock();
        chrLock.lock();
        try {
            return new LinkedHashSet<>(buffEffects.keySet());
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }
    
    private List<MapleBuffStatValueHolder> getAllStatups() {
        effLock.lock();
        chrLock.lock();
        try {
            List<MapleBuffStatValueHolder> ret = new ArrayList<>();
            for(Map<MapleBuffStat, MapleBuffStatValueHolder> bel : buffEffects.values()) {
                for(MapleBuffStatValueHolder mbsvh : bel.values()) {
                    ret.add(mbsvh);
                }
            }
            return ret;
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }
    
    public List<PlayerBuffValueHolder> getAllBuffs() {  // buff values will be stored in an arbitrary order
        effLock.lock();
        chrLock.lock();
        try {
            long curtime = System.currentTimeMillis();
            
            Map<Integer, PlayerBuffValueHolder> ret = new LinkedHashMap<>();
            for(Map<MapleBuffStat, MapleBuffStatValueHolder> bel : buffEffects.values()) {
                for(MapleBuffStatValueHolder mbsvh : bel.values()) {
                    int srcid = mbsvh.effect.getBuffSourceId();
                    if(!ret.containsKey(srcid)) {
                        ret.put(srcid, new PlayerBuffValueHolder((int)(curtime - mbsvh.startTime), mbsvh.effect));
                    }
                }
            }
            return new ArrayList<>(ret.values());
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public List<Pair<MapleBuffStat, Integer>> getAllActiveStatups() {
        effLock.lock();
        chrLock.lock();
        try {
            List<Pair<MapleBuffStat, Integer>> ret = new ArrayList<>();
            for (MapleBuffStat mbs : effects.keySet()) {
                MapleBuffStatValueHolder mbsvh = effects.get(mbs);
                ret.add(new Pair<>(mbs, mbsvh.value));
            }
            return ret;
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }
    
    private List<Pair<MapleBuffStat, Integer>> getActiveStatupsFromSourceid(int sourceid) { // already under effLock & chrLock
        List<Pair<MapleBuffStat, Integer>> ret = new ArrayList<>();
        
        for(Entry<MapleBuffStat, MapleBuffStatValueHolder> bel : buffEffects.get(sourceid).entrySet()) {
            Integer bsrcid = bel.getValue().effect.getBuffSourceId();
            MapleBuffStat mbs = bel.getKey();
            
            MapleBuffStatValueHolder mbsvh = effects.get(bel.getKey());
            if(mbsvh != null && mbsvh.effect.getBuffSourceId() == bsrcid) {
                ret.add(new Pair<>(mbs, mbsvh.value));
            } else {
                ret.add(new Pair<>(mbs, 0));
            }
        }
        
        Collections.sort(ret, new Comparator<Pair<MapleBuffStat, Integer>>() {
            @Override
            public int compare(Pair<MapleBuffStat, Integer> p1, Pair<MapleBuffStat, Integer> p2) {
                return p1.getLeft().compareTo(p2.getLeft());
            }
        });
        
        return ret;
    }
    
    private void addItemEffectHolder(Integer sourceid, long expirationtime, Map<MapleBuffStat, MapleBuffStatValueHolder> statups) {
        buffEffects.put(sourceid, statups);
        buffExpires.put(sourceid, expirationtime);
    }
    
    private boolean removeEffectFromItemEffectHolder(Integer sourceid, MapleBuffStat buffStat) {
        Map<MapleBuffStat, MapleBuffStatValueHolder> lbe = buffEffects.get(sourceid);
        
        if(lbe.remove(buffStat) != null) {        
            buffEffectsCount.put(buffStat, (byte)(buffEffectsCount.get(buffStat) - 1));
            
            if(lbe.isEmpty()) {
                buffEffects.remove(sourceid);
                buffExpires.remove(sourceid);
            }
            
            return true;
        }
        
        return false;
    }
    
    private void removeItemEffectHolder(Integer sourceid) {
        Map<MapleBuffStat, MapleBuffStatValueHolder> be = buffEffects.remove(sourceid);
        if(be != null) {
            for(Entry<MapleBuffStat, MapleBuffStatValueHolder> bei : be.entrySet()) {
                buffEffectsCount.put(bei.getKey(), (byte)(buffEffectsCount.get(bei.getKey()) - 1));
            }
        }
        
        buffExpires.remove(sourceid);
    }
    
    private void dropWorstEffectFromItemEffectHolder(MapleBuffStat mbs) {
        Integer min = Integer.MAX_VALUE;
        Integer srcid = -1;
        for(Entry<Integer, Map<MapleBuffStat, MapleBuffStatValueHolder>> bpl: buffEffects.entrySet()) {
            MapleBuffStatValueHolder mbsvh = bpl.getValue().get(mbs);
            if(mbsvh != null) {
                if(mbsvh.value < min) {
                    min = mbsvh.value;
                    srcid = bpl.getKey();
                }
            }
        }
        
        removeEffectFromItemEffectHolder(srcid, mbs);
    }
    
    private MapleBuffStatValueHolder fetchBestEffectFromItemEffectHolder(MapleBuffStat mbs) {
        Integer max = Integer.MIN_VALUE;
        MapleBuffStatValueHolder mbsvh = null;
        for(Entry<Integer, Map<MapleBuffStat, MapleBuffStatValueHolder>> bpl: buffEffects.entrySet()) {
            MapleBuffStatValueHolder mbsvhi = bpl.getValue().get(mbs);
            if(mbsvhi != null) {
                if(mbsvhi.value > max) {
                    max = mbsvhi.value;
                    mbsvh = mbsvhi;
                }    
            }
        }
        
        if(mbsvh != null) effects.put(mbs, mbsvh);
        return mbsvh;
    }
    
    private void extractBuffValue(int sourceid, MapleBuffStat stat) {
        chrLock.lock();
        try {
            removeEffectFromItemEffectHolder(sourceid, stat);
        } finally {
            chrLock.unlock();
        }
    }
    
    private void debugListAllBuffs() {
        effLock.lock();
        chrLock.lock();
        try {
            System.out.println("-------------------");
            System.out.println("CACHED BUFFS: ");
            for(Entry<Integer, Map<MapleBuffStat, MapleBuffStatValueHolder>> bpl : buffEffects.entrySet()) {
                System.out.print(bpl.getKey() + ": ");
                for(Entry<MapleBuffStat, MapleBuffStatValueHolder> pble : bpl.getValue().entrySet()) {
                    System.out.print(pble.getKey().name() + pble.getValue().value + ", ");
                }
                System.out.println();
            }
            System.out.println("-------------------");
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }
    
    private void debugListAllBuffsCount() {
        effLock.lock();
        chrLock.lock();
        try {
            for(Entry<MapleBuffStat, Byte> mbsl : buffEffectsCount.entrySet()) {
                System.out.println(mbsl.getKey().name() + " -> " + mbsl.getValue());
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }
    
    public void cancelAllBuffs(boolean softcancel) {
        if (softcancel) {
            effLock.lock();
            chrLock.lock();
            try {
                effects.clear();
                
                for(Integer srcid : new ArrayList<>(buffEffects.keySet())) {
                    removeItemEffectHolder(srcid);
                }
            } finally {
                chrLock.unlock();
                effLock.unlock();
            }
        } else {
            Map<MapleStatEffect, Long> mseBuffs = new LinkedHashMap<>();

            effLock.lock();
            chrLock.lock();
            try {
                for(Entry<Integer, Map<MapleBuffStat, MapleBuffStatValueHolder>> bpl : buffEffects.entrySet()) {
                    for(Entry<MapleBuffStat, MapleBuffStatValueHolder> mbse : bpl.getValue().entrySet()) {
                        mseBuffs.put(mbse.getValue().effect, mbse.getValue().startTime);
                    }
                }
            } finally {
                chrLock.unlock();
                effLock.unlock();
            }

            for (Entry<MapleStatEffect, Long> mse : mseBuffs.entrySet()) {
                cancelEffect(mse.getKey(), false, mse.getValue());
            }
        }
    }

    private void dropBuffStats(List<Pair<MapleBuffStat, MapleBuffStatValueHolder>> effectsToCancel) {
        for (Pair<MapleBuffStat, MapleBuffStatValueHolder> cancelEffectCancelTasks : effectsToCancel) {
            //boolean nestedCancel = false;
            
            chrLock.lock();
            try {
                /*
                if (buffExpires.get(cancelEffectCancelTasks.getRight().effect.getBuffSourceId()) != null) {
                    nestedCancel = true;
                }*/
            
                if(cancelEffectCancelTasks.getRight().bestApplied) {
                    fetchBestEffectFromItemEffectHolder(cancelEffectCancelTasks.getLeft());
                }
            } finally {
                chrLock.unlock();
            }

            recalcLocalStats();
            
            /*
            if (nestedCancel) {
                this.cancelEffect(cancelEffectCancelTasks.getRight().effect, false, -1, false);
            }*/
        }
    }
    
    private List<Pair<MapleBuffStat, MapleBuffStatValueHolder>> deregisterBuffStats(Map<MapleBuffStat, MapleBuffStatValueHolder> stats) {
        chrLock.lock();
        try {
            List<Pair<MapleBuffStat, MapleBuffStatValueHolder>> effectsToCancel = new ArrayList<>(stats.size());
            for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stat : stats.entrySet()) {
                int sourceid = stat.getValue().effect.getBuffSourceId();
                
                if(!buffEffects.containsKey(sourceid)) {
                    buffExpires.remove(sourceid);
                }
                
                MapleBuffStat mbs = stat.getKey();
                effectsToCancel.add(new Pair<>(mbs, stat.getValue()));
                
                MapleBuffStatValueHolder mbsvh = effects.get(mbs);
                if (mbsvh != null && mbsvh.effect.getBuffSourceId() == sourceid) {
                    mbsvh.bestApplied = true;
                    effects.remove(mbs);
                    
                    if (mbs == MapleBuffStat.RECOVERY) {
                        if (recoveryTask != null) {
                            recoveryTask.cancel(false);
                            recoveryTask = null;
                        }
                    } else if (mbs == MapleBuffStat.SUMMON || mbs == MapleBuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();

                        MapleSummon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastMessage(MaplePacketCreator.removeSummon(summon, true), summon.getPosition());
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);
                            summons.remove(summonId);

                            if (summon.getSkill() == DarkKnight.BEHOLDER) {
                                if (beholderHealingSchedule != null) {
                                    beholderHealingSchedule.cancel(false);
                                    beholderHealingSchedule = null;
                                }
                                if (beholderBuffSchedule != null) {
                                    beholderBuffSchedule.cancel(false);
                                    beholderBuffSchedule = null;
                                }
                            }
                        }
                    } else if (mbs == MapleBuffStat.DRAGONBLOOD) {
                        dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;
                    } else if (mbs == MapleBuffStat.HPREC || mbs == MapleBuffStat.MPREC) {
                        if(mbs == MapleBuffStat.HPREC) {
                            extraHpRec = 0;
                        } else {
                            extraMpRec = 0;
                        }
                        
                        if (extraRecoveryTask != null) {
                            extraRecoveryTask.cancel(false);
                            extraRecoveryTask = null;
                        }
                        
                        if(extraHpRec != 0 || extraMpRec != 0) {
                            startExtraTaskInternal(extraHpRec, extraMpRec, extraRecInterval);
                        }
                    }
                }
            }

            return effectsToCancel;
        } finally {
            chrLock.unlock();
        }
    }
    
    public void cancelEffect(int itemId) {
        cancelEffect(ii.getItemEffect(itemId), false, -1);
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        effLock.lock();
        try {
            cancelEffect(effect, overwrite, startTime, true);
        } finally {
            effLock.unlock();
        }
    }
    
    private void updateEffects(Set<MapleBuffStat> removedStats) {
        chrLock.lock();
        try {
            Map<Integer, Pair<MapleStatEffect, Long>> retrievedEffects = new LinkedHashMap<>();
            Map<MapleBuffStat, Pair<Integer, Integer>> maxStatups = new LinkedHashMap<>();
            
            for(Entry<Integer, Map<MapleBuffStat, MapleBuffStatValueHolder>> bel : buffEffects.entrySet()) {
                for(Entry<MapleBuffStat, MapleBuffStatValueHolder> belv : bel.getValue().entrySet()) {
                    if(removedStats.contains(belv.getKey())) {
                        if(!retrievedEffects.containsKey(bel.getKey())) {
                            retrievedEffects.put(bel.getKey(), new Pair<>(belv.getValue().effect, belv.getValue().startTime));
                        }
                        
                        Pair<Integer, Integer> thisStat = maxStatups.get(belv.getKey());
                        if(thisStat == null || belv.getValue().value > thisStat.getRight()) {
                            maxStatups.put(belv.getKey(), new Pair<>(bel.getKey(), belv.getValue().value));
                        }
                    }
                }
            }
            
            Map<Integer, Pair<MapleStatEffect, Long>> bestEffects = new LinkedHashMap<>();
            for(Entry<MapleBuffStat, Pair<Integer, Integer>> lmse: maxStatups.entrySet()) {
                Integer srcid = lmse.getValue().getLeft();
                if(!bestEffects.containsKey(srcid)) {
                    bestEffects.put(srcid, retrievedEffects.get(srcid));
                }
            }
            
            for(Entry<Integer, Pair<MapleStatEffect, Long>> lmse: bestEffects.entrySet()) {
                lmse.getValue().getLeft().updateBuffEffect(this, getActiveStatupsFromSourceid(lmse.getKey()), lmse.getValue().getRight());
            }
        } finally {
            chrLock.unlock();
        }
    }
    
    private void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime, boolean firstCancel) {
        Set<MapleBuffStat> removedStats = new LinkedHashSet<>();
        dropBuffStats(cancelEffectInternal(effect, overwrite, startTime, removedStats));
        updateEffects(removedStats);
    }
    
    private List<Pair<MapleBuffStat, MapleBuffStatValueHolder>> cancelEffectInternal(MapleStatEffect effect, boolean overwrite, long startTime, Set<MapleBuffStat> removedStats) {
        Map<MapleBuffStat, MapleBuffStatValueHolder> buffstats;
        if (!overwrite) {   // is removing the source effect, meaning every effect from this srcid is being purged
            buffstats = extractCurrentBuffStats(effect);
        } else {            // is dropping ALL current statups that uses same stats as the given effect
            buffstats = extractLeastRelevantStatEffectsIfFull(effect);
        }
        
        if (effect.isMagicDoor()) {
            MapleDoor destroyDoor;
                    
            chrLock.lock();
            try {
                destroyDoor = doors.remove(this.getId());
            } finally {
                chrLock.unlock();
            }
            
            if (destroyDoor != null) {
                destroyDoor.getTarget().removeMapObject(destroyDoor.getAreaDoor());
                destroyDoor.getTown().removeMapObject(destroyDoor.getTownDoor());
                
                for (MapleCharacter chr : destroyDoor.getTarget().getCharacters()) {
                    destroyDoor.getAreaDoor().sendDestroyData(chr.getClient());
                }
                for (MapleCharacter chr : destroyDoor.getTown().getCharacters()) {
                    destroyDoor.getTownDoor().sendDestroyData(chr.getClient());
                }

                prtLock.lock();
                try {
                    if (party != null) {
                        for (MaplePartyCharacter partyMembers : party.getMembers()) {
                            partyMembers.getPlayer().removeDoor(this.getId());
                            partyMembers.removeDoor(this.getId());
                        }
                        silentPartyUpdateInternal();
                    }
                } finally {
                    prtLock.unlock();
                }
            }
        } else if (effect.isMapChair()) {
            stopChairTask();
        }
        
        List<Pair<MapleBuffStat, MapleBuffStatValueHolder>> toCancel = deregisterBuffStats(buffstats);
        if (effect.getSourceId() == Spearman.HYPER_BODY || effect.getSourceId() == GM.HYPER_BODY || effect.getSourceId() == SuperGM.HYPER_BODY) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(4);
            statup.add(new Pair<>(MapleStat.HP, Math.min(hp, maxhp)));
            statup.add(new Pair<>(MapleStat.MP, Math.min(mp, maxmp)));
            statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
            client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        }
        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != Corsair.BATTLE_SHIP) {
                this.getClient().getWorldServer().unregisterMountHunger(this);
                this.getMount().setActive(false);
            }
        }
        
        if (!overwrite) {
            List<MapleBuffStat> cancelStats = new LinkedList<>();
            
            chrLock.lock();
            try {
                for(Entry<MapleBuffStat, MapleBuffStatValueHolder> mbsl : buffstats.entrySet()) {
                    cancelStats.add(mbsl.getKey());
                }
            } finally {
                chrLock.unlock();
            }
            
            for(MapleBuffStat mbs : cancelStats) removedStats.add(mbs);
            cancelPlayerBuffs(cancelStats);
        }
        
        return toCancel;
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        MapleBuffStatValueHolder effect;
        
        effLock.lock();
        chrLock.lock();
        try {
            effect = effects.get(stat);
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
        if (effect != null) {
            cancelEffect(effect.effect, false, -1);
        }
    }
    
    public void cancelBuffStats(MapleBuffStat stat) {
        effLock.lock();
        try {
            List<Pair<Integer, MapleBuffStatValueHolder>> cancelList = new LinkedList<>();
            
            chrLock.lock();
            try {
                for(Entry<Integer, Map<MapleBuffStat, MapleBuffStatValueHolder>> bel : this.buffEffects.entrySet()) {
                    MapleBuffStatValueHolder beli = bel.getValue().get(stat);
                    if(beli != null) {
                        cancelList.add(new Pair<>(bel.getKey(), beli));
                    }
                }
            } finally {
                chrLock.unlock();
            }

            Map<MapleBuffStat, MapleBuffStatValueHolder> buffStatList = new LinkedHashMap<>();
            for(Pair<Integer, MapleBuffStatValueHolder> p : cancelList) {
                buffStatList.put(stat, p.getRight());
                extractBuffValue(p.getLeft(), stat);
                dropBuffStats(deregisterBuffStats(buffStatList));
            }
        } finally {
            effLock.unlock();
        }
        
        cancelPlayerBuffs(Arrays.asList(stat));
    }
    
    private Map<MapleBuffStat, MapleBuffStatValueHolder> extractCurrentBuffStats(MapleStatEffect effect) {
        chrLock.lock();
        try {
            Map<MapleBuffStat, MapleBuffStatValueHolder> stats = new LinkedHashMap<>();
            Map<MapleBuffStat, MapleBuffStatValueHolder> buffList = buffEffects.remove(effect.getBuffSourceId());
            
            if(buffList != null) {
                for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : buffList.entrySet()) {
                    stats.put(stateffect.getKey(), stateffect.getValue());
                    buffEffectsCount.put(stateffect.getKey(), (byte)(buffEffectsCount.get(stateffect.getKey()) - 1));
                }
            }
            
            return stats;
        } finally {
            chrLock.unlock();
        }
    }
    
    private Map<MapleBuffStat, MapleBuffStatValueHolder> extractLeastRelevantStatEffectsIfFull(MapleStatEffect effect) {
        Map<MapleBuffStat, MapleBuffStatValueHolder> extractedStatBuffs = new LinkedHashMap<>();
        
        chrLock.lock();
        try {
            Map<MapleBuffStat, Byte> stats = new LinkedHashMap<>();
            Map<MapleBuffStat, MapleBuffStatValueHolder> minStatBuffs = new LinkedHashMap<>();
            
            for(Entry<Integer, Map<MapleBuffStat, MapleBuffStatValueHolder>> mbsvhi : buffEffects.entrySet()) {
                for(Entry<MapleBuffStat, MapleBuffStatValueHolder> mbsvh : mbsvhi.getValue().entrySet()) {
                    MapleBuffStat mbs = mbsvh.getKey();
                    Byte it = stats.get(mbs);
                    
                    if(it != null) {
                        stats.put(mbs, (byte) (it + 1));
                        if(mbsvh.getValue().value < minStatBuffs.get(mbs).value) minStatBuffs.put(mbs, mbsvh.getValue());
                    } else {
                        stats.put(mbs, (byte) 1);
                        minStatBuffs.put(mbs, mbsvh.getValue());
                    }
                }
            }
            
            Set<MapleBuffStat> effectStatups = new LinkedHashSet<>();
            for(Pair<MapleBuffStat, Integer> efstat : effect.getStatups()) {
                effectStatups.add(efstat.getLeft());
            }
            
            for(Entry<MapleBuffStat, Byte> it : stats.entrySet()) {
                boolean uniqueBuff = isSingletonStatup(it.getKey());
                
                if(it.getValue() >= (!uniqueBuff ? ServerConstants.MAX_MONITORED_BUFFSTATS : 1) && effectStatups.contains(it.getKey())) {
                    MapleBuffStatValueHolder mbsvh = minStatBuffs.get(it.getKey());
                    
                    Map<MapleBuffStat, MapleBuffStatValueHolder> lpbe = buffEffects.get(mbsvh.effect.getBuffSourceId());
                    lpbe.remove(it.getKey());
                    buffEffectsCount.put(it.getKey(), (byte)(buffEffectsCount.get(it.getKey()) - 1));
                    
                    if(lpbe.isEmpty()) buffEffects.remove(mbsvh.effect.getBuffSourceId());
                    extractedStatBuffs.put(it.getKey(), mbsvh);
                }
            }
        } finally {
            chrLock.unlock();
        }
        
        return extractedStatBuffs;
    }
    
    private boolean isSingletonStatup(MapleBuffStat mbs) {
        switch(mbs) {           //HPREC and MPREC are supposed to be singleton
            case COUPON_EXP1:
            case COUPON_EXP2:
            case COUPON_EXP3:
            case COUPON_EXP4:
            case COUPON_DRP1:
            case COUPON_DRP2:
            case COUPON_DRP3:
            case WATK:
            case WDEF:
            case MATK:
            case MDEF:
            case ACC:
            case AVOID:
            case SPEED:
            case JUMP:
                return false;
                
            default:
                return true;
        }
    }
    
    public void registerEffect(MapleStatEffect effect, long starttime, long expirationtime, boolean isSilent) {
        if (effect.isDragonBlood()) {
            prepareDragonBlood(effect);
        } else if (effect.isBerserk()) {
            checkBerserk(isHidden());
        } else if (effect.isBeholder()) {
            final int beholder = DarkKnight.BEHOLDER;
            if (beholderHealingSchedule != null) {
                beholderHealingSchedule.cancel(false);
            }
            if (beholderBuffSchedule != null) {
                beholderBuffSchedule.cancel(false);
            }
            Skill bHealing = SkillFactory.getSkill(DarkKnight.AURA_OF_BEHOLDER);
            int bHealingLvl = getSkillLevel(bHealing);
            if (bHealingLvl > 0) {
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                int healInterval = healEffect.getX() * 1000;
                beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        if(awayFromWorld.get()) return;
                        
                        addHP(healEffect.getHp());
                        client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, 5), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(beholder, 2), false);
                    }
                }, healInterval, healInterval);
            }
            Skill bBuff = SkillFactory.getSkill(DarkKnight.HEX_OF_BEHOLDER);
            if (getSkillLevel(bBuff) > 0) {
                final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
                int buffInterval = buffEffect.getX() * 1000;
                beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        if(awayFromWorld.get()) return;
                        
                        buffEffect.applyTo(MapleCharacter.this);
                        client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, (int) (Math.random() * 3) + 6), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), beholder, 2), false);
                    }
                }, buffInterval, buffInterval);
            }
        } else if (effect.isRecovery()) {
            int healInterval = (ServerConstants.USE_ULTRA_RECOVERY) ? 2000 : 5000;
            final byte heal = (byte) effect.getX();
            
            chrLock.lock();
            try {
                if(recoveryTask != null) {
                    recoveryTask.cancel(false);
                }
                
                recoveryTask = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        if (getBuffSource(MapleBuffStat.RECOVERY) == -1) {
                            chrLock.lock();
                            try {
                                if (recoveryTask != null) {
                                    recoveryTask.cancel(false);
                                    recoveryTask = null;
                                }
                            } finally {
                                chrLock.unlock();
                            }

                            return;
                        }

                        addHP(heal);
                        client.announce(MaplePacketCreator.showOwnRecovery(heal));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showRecovery(id, heal), false);
                    }
                }, healInterval, healInterval);
            } finally {
                chrLock.unlock();
            }
        } else if (effect.isDojoBuff() || effect.getSourceId() == 2022337) {
            boolean isRecoveryBuff = false;
            if(effect.getHpRRate() > 0) {
                extraHpRec = effect.getHpR();
                extraRecInterval = effect.getHpRRate();
                isRecoveryBuff = true;
            }
            
            if(effect.getMpRRate() > 0) {
                extraMpRec = effect.getMpR();
                extraRecInterval = effect.getMpRRate();
                isRecoveryBuff = true;
            }
            
            if(isRecoveryBuff) {
                stopExtraTask();
                startExtraTask(extraHpRec, extraMpRec, extraRecInterval);   // HP & MP sharing the same task holder
            }
        } else if (effect.isMapChair()) {
            startChairTask();
        }
        
        effLock.lock();
        chrLock.lock();
        try {
            Integer sourceid = effect.getBuffSourceId();
            Map<MapleBuffStat, MapleBuffStatValueHolder> toDeploy;
            Map<MapleBuffStat, MapleBuffStatValueHolder> appliedStatups = new LinkedHashMap<>();
            
            for(Pair<MapleBuffStat, Integer> ps : effect.getStatups()) {
                appliedStatups.put(ps.getLeft(), new MapleBuffStatValueHolder(effect, starttime, ps.getRight()));
            }
            
            if(ServerConstants.USE_BUFF_MOST_SIGNIFICANT) {
                toDeploy = new LinkedHashMap<>();
                Map<Integer, Pair<MapleStatEffect, Long>> retrievedEffects = new LinkedHashMap<>();
                
                for (Entry<MapleBuffStat, MapleBuffStatValueHolder> statup : appliedStatups.entrySet()) {
                    MapleBuffStatValueHolder mbsvh = effects.get(statup.getKey());
                    if(mbsvh == null || mbsvh.value <= statup.getValue().value) {
                        toDeploy.put(statup.getKey(), statup.getValue());
                    } else {
                        retrievedEffects.put(mbsvh.effect.getBuffSourceId(), new Pair<>(mbsvh.effect, mbsvh.startTime));
                    }
                    
                    Byte val = buffEffectsCount.get(statup.getKey());
                    if(val != null) val = (byte)(val + 1);
                    else val = (byte) 1;
                    
                    buffEffectsCount.put(statup.getKey(), val);
                }
                
                if(!isSilent) {
                    for(Entry<Integer, Pair<MapleStatEffect, Long>> lmse: retrievedEffects.entrySet()) {
                        lmse.getValue().getLeft().updateBuffEffect(this, getActiveStatupsFromSourceid(lmse.getKey()), lmse.getValue().getRight());
                    }
                }
            } else {
                for (Entry<MapleBuffStat, MapleBuffStatValueHolder> statup : appliedStatups.entrySet()) {
                    Byte val = buffEffectsCount.get(statup.getKey());
                    if(val != null) val = (byte)(val + 1);
                    else val = (byte) 1;
                    
                    buffEffectsCount.put(statup.getKey(), val);
                }
                
                toDeploy = appliedStatups;
            }
            
            addItemEffectHolder(sourceid, expirationtime, appliedStatups);
            
            for (Entry<MapleBuffStat, MapleBuffStatValueHolder> statup : toDeploy.entrySet()) {
                effects.put(statup.getKey(), statup.getValue());
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
        
        recalcLocalStats();
    }
    
    private static int getJobMapChair(MapleJob job) {
        switch(job.getId() / 1000) {
            case 0:
                return Beginner.MAP_CHAIR;
            case 1:
                return Noblesse.MAP_CHAIR;
            default:
                return Legend.MAP_CHAIR;
        }
    }
    
    public void unregisterChairBuff() {
        if(!ServerConstants.USE_CHAIR_EXTRAHEAL) return;
        
        int skillId = getJobMapChair(job);
        int skillLv = getSkillLevel(skillId);
        if(skillLv > 0) {
            MapleStatEffect mapChairSkill = SkillFactory.getSkill(skillId).getEffect(skillLv);
            cancelEffect(mapChairSkill, false, -1);
        }
    }
    
    public void registerChairBuff() {
        if(!ServerConstants.USE_CHAIR_EXTRAHEAL) return;
        
        int skillId = getJobMapChair(job);
        int skillLv = getSkillLevel(skillId);
        if(skillLv > 0) {
            MapleStatEffect mapChairSkill = SkillFactory.getSkill(skillId).getEffect(skillLv);
            mapChairSkill.applyTo(this);
        }
    }
    
    public int getChair() {
        return chair.get();
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public MapleClient getClient() {
        return client;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        synchronized (quests) {
            List<MapleQuestStatus> ret = new LinkedList<>();
            for (MapleQuestStatus q : quests.values()) {
                if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                    ret.add(q);
                }
            }
            
            return Collections.unmodifiableList(ret);
        }
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getDex() {
        return dex;
    }

    public int getDojoEnergy() {
        return dojoEnergy;
    }

    public boolean getDojoParty() {
        return mapid >= 925030100 && mapid < 925040000;
    }

    public int getDojoPoints() {
        return dojoPoints;
    }

    public int getDojoStage() {
        return dojoStage;
    }

    public Map<Integer, MapleDoor> getDoors() {
        chrLock.lock();
        try {
            return Collections.unmodifiableMap(doors);
        } finally {
            chrLock.unlock();
        }
    }

    public int getEnergyBar() {
        return energybar;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public void resetExcluded(int petId) {
        chrLock.lock();
        try {
            Set<Integer> petExclude = excluded.get(petId);
        
            if(petExclude != null) petExclude.clear();
            else excluded.put(petId, new LinkedHashSet<Integer>());
        } finally {
            chrLock.unlock();
        }
    }
    
    public void addExcluded(int petId, int x) {
        chrLock.lock();
        try {
            excluded.get(petId).add(x);
        } finally {
            chrLock.unlock();
        }
    }
    
    public void commitExcludedItems() {
        Map<Integer, Set<Integer>> petExcluded = this.getExcluded();
        
        chrLock.lock();
        try {
            excludedItems.clear();
        } finally {
            chrLock.unlock();
        }
        
        for(Map.Entry<Integer, Set<Integer>> pe : petExcluded.entrySet()) {
            byte petIndex = this.getPetIndex(pe.getKey());
            if(petIndex < 0) continue;

            Set<Integer> exclItems = pe.getValue();
            if (!exclItems.isEmpty()) {
                client.announce(MaplePacketCreator.loadExceptionList(this.getId(), pe.getKey(), petIndex, new ArrayList<>(exclItems)));

                chrLock.lock();
                try {
                    for(Integer itemid: exclItems) {
                        excludedItems.add(itemid);
                    }
                } finally {
                    chrLock.unlock();
                }
            }
        }
    }
    
    public void exportExcludedItems(MapleClient c) {
        Map<Integer, Set<Integer>> petExcluded = this.getExcluded();
        for(Map.Entry<Integer, Set<Integer>> pe : petExcluded.entrySet()) {
            byte petIndex = this.getPetIndex(pe.getKey());
            if(petIndex < 0) continue;

            Set<Integer> exclItems = pe.getValue();
            if (!exclItems.isEmpty()) {
                c.announce(MaplePacketCreator.loadExceptionList(this.getId(), pe.getKey(), petIndex, new ArrayList<>(exclItems)));
            }
        }
    }
    
    public Map<Integer, Set<Integer>> getExcluded() {
        chrLock.lock();
        try {
            return Collections.unmodifiableMap(excluded);
        } finally {
            chrLock.unlock();
        }
    }
    
    public Set<Integer> getExcludedItems() {
        chrLock.lock();
        try {
            return Collections.unmodifiableSet(excludedItems);
        } finally {
            chrLock.unlock();
        }
    }

    public int getExp() {
        return exp.get();
    }

    public int getGachaExp() {
        return gachaexp.get();
    }

    public int getExpRate() {
        return expRate;
    }
    
    public int getCouponExpRate() {
        return expCoupon;
    }
    
    public int getRawExpRate() {
        return expRate / (expCoupon * client.getWorldServer().getExpRate());
    }
    
    public int getDropRate() {
        return dropRate;
    }
    
    public int getCouponDropRate() {
        return dropCoupon;
    }
    
    public int getRawDropRate() {
        return dropRate / (dropCoupon * client.getWorldServer().getDropRate());
    }
    
    public int getMesoRate() {
        return mesoRate;
    }
    
    public int getCouponMesoRate() {
        return mesoCoupon;
    }
    
    public int getRawMesoRate() {
        return mesoRate / (mesoCoupon * client.getWorldServer().getMesoRate());
    }

    public int getFace() {
        return face;
    }

    public int getFame() {
        return fame;
    }

    public MapleFamily getFamily() {
        return family;
    }

    public void setFamily(MapleFamily f) {
        this.family = f;
    }

    public int getFamilyId() {
        return familyId;
    }

    public boolean getFinishedDojoTutorial() {
        return finishedDojoTutorial;
    }

    public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }

    public int getGender() {
        return gender;
    }

    public boolean isMale() {
        return getGender() == 0;
    }

    public MapleGuild getGuild() {
        try {
            return Server.getInstance().getGuild(getGuildId(), getWorld(), this);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public MapleAlliance getAlliance() {
        if(mgc != null) {
            try {
                return Server.getInstance().getAlliance(getGuild().getAllianceId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        return null;
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildRank;
    }

    public int getHair() {
        return hair;
    }

    public MapleHiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public int getHp() {
        return hp;
    }

    public int getHpMpApUsed() {
        return hpMpApUsed;
    }

    public int getId() {
        return id;
    }

    public static int getAccountIdByName(String name) {
        try {
            int id;
            Connection con = DatabaseConnection.getConnection();
            
            try (PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return -1;
                    }
                    id = rs.getInt("accountid");
                }
            } finally {
                con.close();
            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public static int getIdByName(String name) {
        try {
            int id;
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return -1;
                    }
                    id = rs.getInt("id");
                }
            } finally {
                con.close();
            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getNameById(int id) {
        try {
            String name;
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }
                    name = rs.getString("name");
                }
            } finally {
                con.close();
            }
            return name;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getInt() {
        return int_;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[ii.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }
    
    public int getCleanItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[ii.getInventoryType(itemid).ordinal()].countNotOwnedById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countNotOwnedById(itemid);
        }
        return possesed;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getJobType() {
        return job.getId() / 1000;
    }

    public Map<Integer, MapleKeyBinding> getKeymap() {
        return keymap;
    }

    public long getLastHealed() {
        return lastHealed;
    }

    public long getLastUsedCashItem() {
        return lastUsedCashItem;
    }

    public int getLevel() {
        return level;
    }

    public int getLuk() {
        return luk;
    }

    public int getFh() {
		Point pos = this.getPosition();
		pos.y -= 6;
        if (getMap().getFootholds().findBelow(pos) == null) {
            return 0;
        } else {
            return getMap().getFootholds().findBelow(pos).getY1();
        }
    }

    public MapleMap getMap() {
        return map;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public int getMarkedMonster() {
        return markedMonster;
    }

    public MapleRing getMarriageRing() {
        return marriageRing;
    }

    public int getMarried() {
        return married;
    }

    public int getMasterLevel(Skill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).masterlevel;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public int getMaxLevel() {
        return isCygnus() ? 120 : 200;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMerchantMeso() {
        return merchantmeso;
    }

    public int getMesosTraded() {
        return mesosTraded;
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }
    
    public void setMGC(MapleGuildCharacter mgc) {
        this.mgc = mgc;
    }

    public MaplePartyCharacter getMPC() {
        if (mpc == null) {
            mpc = new MaplePartyCharacter(this);
        }
        return mpc;
    }

    public void setMPC(MaplePartyCharacter mpc) {
        this.mpc = mpc;
    }
    
    public int getTargetHpBarHash() {
        return this.targetHpBarHash;
    }
    
    public void setTargetHpBarHash(int mobHash) {
        this.targetHpBarHash = mobHash;
    }
    
    public long getTargetHpBarTime() {
        return this.targetHpBarTime;
    }
    
    public void setTargetHpBarTime(long timeNow) {
        this.targetHpBarTime = timeNow;
    }
    
    public void setPlayerAggro(int mobHash) {
        setTargetHpBarHash(mobHash);
        setTargetHpBarTime(System.currentTimeMillis());
    }
    
    public void resetPlayerAggro() {
        setTargetHpBarHash(0);
        setTargetHpBarTime(0);
    }
    
    public int getDoorSlot() {
        if(doorSlot == -1) {
            prtLock.lock();
            try {
                doorSlot = (party == null) ? 0 : party.getPartyDoor(this.getId());
            } finally {
                prtLock.unlock();
            }
        }
        
        return doorSlot;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public int getMiniGamePoints(String type, boolean omok) {
        if (omok) {
            switch (type) {
                case "wins":
                    return omokwins;
                case "losses":
                    return omoklosses;
                default:
                    return omokties;
            }
        } else {
            switch (type) {
                case "wins":
                    return matchcardwins;
                case "losses":
                    return matchcardlosses;
                default:
                    return matchcardties;
            }
        }
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public int getMp() {
        return mp;
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public String getName() {
        return name;
    }

    public int getNextEmptyPetIndex() {
        petLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                if (pets[i] == null) {
                    return i;
                }
            }
            return 3;
        } finally {
            petLock.unlock();
        }
    }

    public int getNoPets() {
        petLock.lock();
        try {
            int ret = 0;
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    ret++;
                }
            }
            return ret;
        } finally {
            petLock.unlock();
        }
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    public MapleParty getParty() {
        prtLock.lock();
        try {
            return party;
        } finally {
            prtLock.unlock();
        }
    }

    public int getPartyId() {
        prtLock.lock();
        try {
            return (party != null ? party.getId() : -1);
        } finally {
            prtLock.unlock();
        }
    }
    
    public List<MapleCharacter> getPartyMembers() {
        List<MapleCharacter> list = new LinkedList<>();
        
        prtLock.lock();
        try {
            if(party != null) {
                for(MaplePartyCharacter partyMembers: party.getMembers()) {
                    list.add(partyMembers.getPlayer());
                }
            }
        } finally {
            prtLock.unlock();
        }
        
        return list;
    }
    
    public List<MapleCharacter> getPartyMembersOnSameMap() {
        List<MapleCharacter> list = new LinkedList<>();
        int thisMapHash = this.getMap().hashCode();
        
        prtLock.lock();
        try {
            if(party != null) {
                for(MaplePartyCharacter partyMembers: party.getMembers()) {
                    if(partyMembers.getPlayer().getMap().hashCode() == thisMapHash) list.add(partyMembers.getPlayer());
                }
            }
        } finally {
            prtLock.unlock();
        }
        
        return list;
    }
    
    public boolean isPartyMember(MapleCharacter chr) {
        return isPartyMember(chr.getId());
    }
    
    public boolean isPartyMember(int cid) {
        for(MapleCharacter mpcu: getPartyMembers()) {
            if(mpcu.getId() == cid) {
                return true;
            }
        }
        
        return false;
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }
    
    public void setGMLevel(int level) {
        this.gmLevel = Math.min(level, 6);
        this.gmLevel = Math.max(level, 0);
    }
    
    public void closePlayerInteractions() {
        closeNpcShop();
        closeTrade();
        closePlayerShop();
        closeMiniGame();
        closeHiredMerchant(false);
        closePlayerMessenger();
        
        client.closePlayerScriptInteractions();
    }
    
    public void closeNpcShop() {
        setShop(null);
    }
    
    public void closeTrade() {
        MapleTrade.cancelTrade(this);
    }
    
    public void closePlayerShop() {
        MaplePlayerShop mps = this.getPlayerShop();
        if(mps == null) return;
        
        if (mps.isOwner(this)) {
            mps.setOpen(false);
            client.getWorldServer().unregisterPlayerShop(mps);
            
            for (MaplePlayerShopItem mpsi : mps.getItems()) {
                if (mpsi.getBundles() >= 2) {
                    Item iItem = mpsi.getItem().copy();
                    iItem.setQuantity((short) (mpsi.getBundles() * iItem.getQuantity()));
                    MapleInventoryManipulator.addFromDrop(this.getClient(), iItem, false);
                } else if (mpsi.isExist()) {
                    MapleInventoryManipulator.addFromDrop(this.getClient(), mpsi.getItem(), true);
                }
            }
            mps.closeShop();
        } else {
            mps.removeVisitor(this);
        }
        this.setPlayerShop(null);
    }
    
    public void closeMiniGame() {
        MapleMiniGame game = this.getMiniGame();
        if(game == null) return;
        
        this.setMiniGame(null);
        if (game.isOwner(this)) {
            this.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(this));
            game.broadcastToVisitor(MaplePacketCreator.getMiniGameClose());
        } else {
            game.removeVisitor(this);
        }
    }
    
    public void closeHiredMerchant(boolean closeMerchant) {
        MapleHiredMerchant merchant = this.getHiredMerchant();
        if(merchant == null) return;
        
        if(closeMerchant) {
            merchant.removeVisitor(this);
            this.setHiredMerchant(null);
        }
        else {
            if (merchant.isOwner(this)) {
                merchant.setOpen(true);
            } else {
                merchant.removeVisitor(this);
            }
            try {
                merchant.saveItems(false);
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println("Error while saving Hired Merchant items.");
            }
        }
    }
    
    public void closePlayerMessenger() {
        MapleMessenger m = this.getMessenger();
        if(m == null) return;
        
        World w = client.getWorldServer();
        MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(this, this.getMessengerPosition());
        
        w.leaveMessenger(m.getId(), messengerplayer);
        this.setMessenger(null);
        this.setMessengerPosition(4);
    }

    public MaplePet[] getPets() {
        petLock.lock();
        try {
            return Arrays.copyOf(pets, pets.length);
        } finally {
            petLock.unlock();
        }
    }

    public MaplePet getPet(int index) {
        if(index < 0) return null;
        
        petLock.lock();
        try {
            return pets[index];
        } finally {
            petLock.unlock();
        }
    }

    public byte getPetIndex(int petId) {
        petLock.lock();
        try {
            for (byte i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == petId) {
                        return i;
                    }
                }
            }
            return -1;
        } finally {
            petLock.unlock();
        }
    }
    
    public byte getPetIndex(MaplePet pet) {
        petLock.lock();
        try {
            for (byte i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == pet.getUniqueId()) {
                        return i;
                    }
                }
            }
            return -1;
        } finally {
            petLock.unlock();
        }
    }

    public int getPossibleReports() {
        return possibleReports;
    }

    public final byte getQuestStatus(final int quest) {
        synchronized (quests) {
            for (final MapleQuestStatus q : quests.values()) {
                if (q.getQuest().getId() == quest) {
                    return (byte) q.getStatus().getId();
                }
            }
            return 0;
        }
    }
    
    public final MapleQuestStatus getMapleQuestStatus(final int quest) {
        synchronized (quests) {
            for (final MapleQuestStatus q : quests.values()) {
                if (q.getQuest().getId() == quest) {
                    return q;
                }
            }
            return null;
        }
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        synchronized (quests) {
            if (!quests.containsKey(quest.getId())) {
                return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
            }
            return quests.get(quest.getId());
        }
    }
    
    //---- \/ \/ \/ \/ \/ \/ \/  NOT TESTED  \/ \/ \/ \/ \/ \/ \/ \/ \/ ----
    
    public final void setQuestAdd(final MapleQuest quest, final byte status, final String customData) {
        synchronized (quests) {
            if (!quests.containsKey(quest.getId())) {
                final MapleQuestStatus stat = new MapleQuestStatus(quest, MapleQuestStatus.Status.getById((int)status));
                stat.setCustomData(customData);
                quests.put(quest.getId(), stat);
            }
        }
    }

    public final MapleQuestStatus getQuestNAdd(final MapleQuest quest) {
        synchronized (quests) {
            if (!quests.containsKey(quest.getId())) {
                final MapleQuestStatus status = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
                quests.put(quest.getId(), status);
                return status;
            }
            return quests.get(quest.getId());
        }
    }

    public final MapleQuestStatus getQuestNoAdd(final MapleQuest quest) {
        synchronized (quests) {
            return quests.get(quest.getId());
        }
    }

    public final MapleQuestStatus getQuestRemove(final MapleQuest quest) {
        synchronized (quests) {
            return quests.remove(quest.getId());
        }
    }

    //---- /\ /\ /\ /\ /\ /\ /\  NOT TESTED  /\ /\ /\ /\ /\ /\ /\ /\ /\ ----
    
    public boolean needQuestItem(int questid, int itemid) {
        if (questid <= 0) return true; //For non quest items :3
        if (this.getQuestStatus(questid) != 1) return false;
        
        MapleQuest quest = MapleQuest.getInstance(questid);
        return getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid) < quest.getItemAmountNeeded(itemid);
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp[GameConstants.getSkillBook(job.getId())]; //default
    }

    public int getRemainingSpBySkill(final int skillbook) {
        return remainingSp[skillbook];
    }

    public int[] getRemainingSps() {
        return remainingSp;
    }

    public int getSavedLocation(String type) {
        SavedLocation sl = savedLocations[SavedLocationType.fromString(type).ordinal()];
        if (sl == null) {
            return -1;
        }
        int m = sl.getMapId();
        if (!SavedLocationType.fromString(type).equals(SavedLocationType.WORLDTOUR)) {
            clearSavedLocation(SavedLocationType.fromString(type));
        }
        return m;
    }

    public String getSearch() {
        return search;
    }

    public MapleShop getShop() {
        return shop;
    }

    public Map<Skill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }
    
    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public byte getSkillLevel(Skill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).skillevel;
    }

    public long getSkillExpiration(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return -1;
        }
        return ret.expiration;
    }

    public long getSkillExpiration(Skill skill) {
        if (skills.get(skill) == null) {
            return -1;
        }
        return skills.get(skill).expiration;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public int getSlot() {
        return slots;
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        synchronized (quests) {
            List<MapleQuestStatus> ret = new LinkedList<>();
            for (MapleQuestStatus q : quests.values()) {
                if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                    ret.add(q);
                }
            }
            return Collections.unmodifiableList(ret);
        }
    }

    public final int getStartedQuestsSize() {
        synchronized (quests) {
            int i = 0;
            for (MapleQuestStatus q : quests.values()) {
                if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                    if (q.getQuest().getInfoNumber() > 0) {
                        i++;
                    }
                    i++;
                }
            }
            return i;
        }
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        effLock.lock();
        chrLock.lock();
        try {
            MapleBuffStatValueHolder mbsvh = effects.get(effect);
            if (mbsvh == null) {
                return null;
            }
            return mbsvh.effect;
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getStr() {
        return str;
    }
    
    public Collection<MapleSummon> getSummonsValues() {
        return summons.values();
    }
    
    public void clearSummons() {
        summons.clear();
    }
    
    public MapleSummon getSummonByKey(int id) {
        return summons.get(id);
    }
    
    public boolean isSummonsEmpty() {
        return summons.isEmpty();
    }
    
    public boolean containsSummon(MapleSummon summon) {
        return summons.containsValue(summon);
    }

    public int getTotalStr() {
        return localstr;
    }

    public int getTotalDex() {
        return localdex;
    }

    public int getTotalInt() {
        return localint_;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public int getTotalWatk() {
        return watk;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public int getVanquisherKills() {
        return vanquisherKills;
    }

    public int getVanquisherStage() {
        return vanquisherStage;
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public int getWorld() {
        return world;
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        if (skillid == 5221999) {
            this.battleshipHp = (int) length;
            addCooldown(skillid, 0, length);
        } else {
            int time = (int) ((length + starttime) - System.currentTimeMillis());
            addCooldown(skillid, System.currentTimeMillis(), time);
        }
    }
    
    public int gmLevel() {
        return gmLevel;
    }

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    private void guildUpdate() {
        mgc.setLevel(level);
        mgc.setJobId(job.getId());
        
        if (this.guildid < 1) {
            return;
        }
        
        try {
            Server.getInstance().memberLevelJobUpdate(this.mgc);
            //Server.getInstance().getGuild(guildid, world, mgc).gainGP(40);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0) {
                Server.getInstance().allianceMessage(allianceId, MaplePacketCreator.updateAllianceJobLevel(this), getId(), -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleEnergyChargeGain() { // to get here energychargelevel has to be > 0
        Skill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
        MapleStatEffect ceffect;
        ceffect = energycharge.getEffect(getSkillLevel(energycharge));
        TimerManager tMan = TimerManager.getInstance();
        if (energybar < 10000) {
            energybar += 102;
            if (energybar > 10000) {
                energybar = 10000;
            }
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ENERGY_CHARGE, energybar));
            setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
            client.announce(MaplePacketCreator.giveBuff(energybar, 0, stat));
            client.announce(MaplePacketCreator.showOwnBuffEffect(energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(id, energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(energybar, stat));
        }
        if (energybar >= 10000 && energybar < 11000) {
            energybar = 15000;
            final MapleCharacter chr = this;
            tMan.schedule(new Runnable() {
                @Override
                public void run() {
                    energybar = 0;
                    List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ENERGY_CHARGE, energybar));
                    setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
                    client.announce(MaplePacketCreator.giveBuff(energybar, 0, stat));
                    getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignBuff(energybar, stat));
                }
            }, ceffect.getDuration());
        }
    }

    public void handleOrbconsume() {
        int skillid = isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
        Skill combo = SkillFactory.getSkill(skillid);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        client.announce(MaplePacketCreator.giveBuff(skillid, combo.getEffect(getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat), false);
    }

    public boolean hasEntered(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEntered(String script, int mapId) {
        if (entered.containsKey(mapId)) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)")) {
                ps.setInt(1, getId());
                ps.setInt(2, to.getId());
                ps.executeUpdate();
            }
            finally {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean haveItem(int itemid) {
        return getItemQuantity(itemid, false) > 0;
    }
    
    public boolean haveCleanItem(int itemid) {
        return getCleanItemQuantity(itemid, false) > 0;
    }
    
    public boolean hasEmptySlot(int itemId) {
        return getInventory(ii.getInventoryType(itemId)).getNextFreeSlot() > -1;
    }
    
    public boolean hasEmptySlot(byte invType) {
        return getInventory(MapleInventoryType.getByType(invType)).getNextFreeSlot() > -1;
    }

    public void increaseGuildCapacity() { //hopefully nothing is null
        if (getMeso() < getGuild().getIncreaseGuildCost(getGuild().getCapacity())) {
            dropMessage(1, "You don't have enough mesos.");
            return;
        }
        Server.getInstance().increaseGuildCapacity(guildid);
        gainMeso(-getGuild().getIncreaseGuildCost(getGuild().getCapacity()), true, false, false);
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs;
        
        effLock.lock();
        chrLock.lock();
        try {
            allBuffs = new LinkedList<>(effects.values());
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
        
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isBuffFrom(MapleBuffStat stat, Skill skill) {
        effLock.lock();
        chrLock.lock();
        try {
            MapleBuffStatValueHolder mbsvh = effects.get(stat);
            if (mbsvh == null) {
                return false;
            }
            return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public boolean isCygnus() {
        return getJobType() == 1;
    }

    public boolean isAran() {
        return getJob().getId() >= 2000 && getJob().getId() <= 2112;
    }

    public boolean isBeginnerJob() {
        return (getJob().getId() == 0 || getJob().getId() == 1000 || getJob().getId() == 2000);
    }

    public boolean isGM() {
        return gmLevel > 1;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        prtLock.lock();
        try {
            return party.getLeaderId() == getId();
        } finally {
            prtLock.unlock();
        }
    }
    
    public boolean isGuildLeader() {    // true on guild master or jr. master
        return guildid > 0 && guildRank < 3;
    }

    public void leaveMap() {
        controlled.clear();
        visibleMapObjects.clear();
        chair.set(0);
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
    }

    public void levelUp(boolean takeexp) {
        Skill improvingMaxHP = null;
        Skill improvingMaxMP = null;
        int improvingMaxHPLevel = 0;
        int improvingMaxMPLevel = 0;

        if (isBeginnerJob() && getLevel() < 11) {
            remainingAp = 0;
            if (getLevel() < 6) {
                str += 5;
            } else {
                str += 4;
                dex += 1;
            }
        } else {
            remainingAp += 5;
            if (isCygnus() && level < 70) {
                remainingAp++;
            }
        }
        if (job == MapleJob.BEGINNER || job == MapleJob.NOBLESSE || job == MapleJob.LEGEND) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_INCREASE) : SkillFactory.getSkill(Swordsman.IMPROVED_MAX_HP_INCREASE);
            if (job.isA(MapleJob.CRUSADER)) {
                improvingMaxMP = SkillFactory.getSkill(1210000);
            } else if (job.isA(MapleJob.DAWNWARRIOR2)) {
                improvingMaxMP = SkillFactory.getSkill(11110000);
            }
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            improvingMaxMP = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
            improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || (job.getId() > 1299 && job.getId() < 1500)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            maxhp = 30000;
            maxmp = 30000;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAX_HP) : SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(22, 28);
            maxmp += Randomizer.rand(18, 23);
        } else if (job.isA(MapleJob.ARAN1)) {
            maxhp += Randomizer.rand(44, 48);
            int aids = Randomizer.rand(4, 8);
            maxmp += aids + Math.floor(aids * 0.1);
        }
        if (improvingMaxHPLevel > 0 && (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.DAWNWARRIOR1))) {
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0 && (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.CRUSADER) || job.isA(MapleJob.BLAZEWIZARD1))) {
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }
        maxmp += localint_ / 10;
        if (takeexp) {
            exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
            if (exp.get() < 0) {
                exp.set(0);
            }
        }
        level++;
        if (level >= getMaxLevel()) {
            exp.set(0);
            level = getMaxLevel(); //To prevent levels past 200
        }
        
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);
        if (level == 200) {
            exp.set(0);
        }
        recalcLocalStats();
        hp = localmaxhp;
        mp = localmaxmp;
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(10);
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<>(MapleStat.HP, localmaxhp));
        statup.add(new Pair<>(MapleStat.MP, localmaxmp));
        statup.add(new Pair<>(MapleStat.EXP, exp.get()));
        statup.add(new Pair<>(MapleStat.LEVEL, level));
        statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
        statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
        statup.add(new Pair<>(MapleStat.STR, Math.min(str, Short.MAX_VALUE)));
        statup.add(new Pair<>(MapleStat.DEX, Math.min(dex, Short.MAX_VALUE)));
        if (job.getId() % 1000 > 0) {
        	remainingSp[GameConstants.getSkillBook(job.getId())] += 3;
        	statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
        }
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
        recalcLocalStats();
        setMPC(new MaplePartyCharacter(this));
        silentPartyUpdate();
        
        if(level == 10 && party != null) {
            if(this.isPartyLeader()) party.assignNewLeader(client);
            PartyOperationHandler.leaveParty(party, mpc, client);
            
            client.announceHint("You have reached #blevel 10#k, therefore you must leave your #rstarter party#k.");
        }
        
        if (this.guildid > 0) {
            getGuild().broadcast(MaplePacketCreator.levelUpMessage(2, level, name), this.getId());
        }
        if (ServerConstants.USE_PERFECT_PITCH && level >= 30) {
            //milestones?
            if (MapleInventoryManipulator.checkSpace(client, 4310000, (short) 1, "")) {
                MapleInventoryManipulator.addById(client, 4310000, (short) 1);
            }
        }
        if (level == 200 && !isGM()) {
            final String names = (getMedalText() + name);
            client.getWorldServer().broadcastPacket(MaplePacketCreator.serverNotice(6, String.format(LEVEL_200, names, names)));
        }
        
        if(level % 20 == 0 && ServerConstants.USE_ADD_SLOTS_BY_LEVEL == true) {
            if (!isGM()) {
                for (byte i = 1; i < 5; i++) {
                    gainSlots(i, 4, true);
                }
                
                this.yellowMessage("You reached level " + level + ". Congratulations! As a token of your success, your inventory has been expanded a little bit.");
            }            
        }
        if (level % 20 == 0 && ServerConstants.USE_ADD_RATES_BY_LEVEL == true) { //For the drop & meso rate
            revertLastPlayerRates();
            setPlayerRates();
            this.yellowMessage("You managed to get level " + level + "! Getting experience and items seems a little easier now, huh?");
        }
        
        levelUpMessages();
        guildUpdate();
    }

    public void gainAp(int amount) {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(1);
        remainingAp += amount;
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
    }

    public void gainSp(int amount) {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(1);
        remainingSp[GameConstants.getSkillBook(job.getId())] += amount;
        statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
    }

    private void levelUpMessages() {
        if (level % 5 != 0) { //Performance FTW?
            return;
        }
        if (level == 5) {
            yellowMessage("Aww, you're level 5, how cute!");
        } else if (level == 10) {
            yellowMessage("Henesys Party Quest is now open to you! Head over to Henesys, find some friends, and try it out!");
        } else if (level == 15) {
            yellowMessage("Half-way to your 2nd job advancement, nice work!");
        } else if (level == 20) {
            yellowMessage("You can almost Kerning Party Quest!");
        } else if (level == 25) {
            yellowMessage("You seem to be improving, but you are still not ready to move on to the next step.");
        } else if (level == 30) {
            yellowMessage("You have finally reached level 30! Try job advancing, after that try the Mushroom Kingdom!");
        } else if (level == 35) {
            yellowMessage("Hey did you hear about this mall that opened in Kerning? Try visiting the Kerning Mall.");
        } else if (level == 40) {
            yellowMessage("Do @rates to see what all your rates are!");
        } else if (level == 45) {
            yellowMessage("I heard that a rock and roll artist died during the grand opening of the Kerning Mall. People are naming him the Spirit of Rock.");
        } else if (level == 50) {
            yellowMessage("You seem to be growing very fast, would you like to test your new found strength with the mighty Zakum?");
        } else if (level == 55) {
            yellowMessage("You can now try out the Ludibrium Maze Party Quest!");
        } else if (level == 60) {
            yellowMessage("Feels good to be near the end of 2nd job, doesn't it?");
        } else if (level == 65) {
            yellowMessage("You're only 5 more levels away from 3rd job, not bad!");
        } else if (level == 70) {
            yellowMessage("I see many people wearing a teddy bear helmet. I should ask someone where they got it from.");
        } else if (level == 75) {
            yellowMessage("You have reached level 3 quarters!");
        } else if (level == 80) {
            yellowMessage("You think you are powerful enough? Try facing horntail!");
        } else if (level == 85) {
            yellowMessage("Did you know? The majority of people who hit level 85 in Solaxia don't live to be 85 years old?");
        } else if (level == 90) {
            yellowMessage("Hey do you like the amusement park? I heard Spooky World is the best theme park around. I heard they sell cute teddy-bears.");
        } else if (level == 95) {
            yellowMessage("100% of people who hit level 95 in Solaxia don't live to be 95 years old.");
        } else if (level == 100) {
            yellowMessage("Mid-journey so far... You just reached level 100! Now THAT's such a feat, however to manage the 200 you will need even more passion and determination than ever! Good hunting!");
        } else if (level == 105) {
            yellowMessage("Have you ever been to leafre? I heard they have dragons!");
        } else if (level == 110) {
            yellowMessage("I see many people wearing a teddy bear helmet. I should ask someone where they got it from.");
        } else if (level == 115) {
            yellowMessage("I bet all you can think of is level 120, huh? Level 115 gets no love.");
        } else if (level == 120) {
            yellowMessage("Are you ready to learn from the masters? Head over to your job instructor!");
        } else if (level == 125) {
            yellowMessage("The struggle for mastery books has begun, huh?");
        } else if (level == 130) {
            yellowMessage("You should try Temple of Time. It should be pretty decent EXP.");
        } else if (level == 135) {
            yellowMessage("I hope you're still not struggling for mastery books!");
        } else if (level == 140) {
            yellowMessage("You're well into 4th job at this point, great work!");
        } else if (level == 145) {
            yellowMessage("Level 145 is serious business!");
        } else if (level == 150) {
            yellowMessage("You have becomed quite strong, but the journey is not yet over.");
        } else if (level == 155) {
            yellowMessage("At level 155, Zakum should be a joke to you. Nice job!");
        } else if (level == 160) {
            yellowMessage("Level 160 is pretty impressive. Try taking a picture and putting it on Instagram.");
        } else if (level == 165) {
            yellowMessage("At this level, you should start looking into doing some boss runs.");
        } else if (level == 170) {
            yellowMessage("Level 170, huh? You have the heart of a champion.");
        } else if (level == 175) {
            yellowMessage("You came a long way from level 1. Amazing job so far.");
        } else if (level == 180) {
            yellowMessage("Have you ever tried taking a boss on by yourself? It is quite difficult.");
        } else if (level == 185) {
            yellowMessage("Legend has it that you're a legend.");
        } else if (level == 190) {
            yellowMessage("You only have 10 more levels to go until you hit 200!");
        } else if (level == 195) {
            yellowMessage("Nothing is stopping you at this point, level 195!");
        } else if (level == 200) {
            yellowMessage("Very nicely done! You have reached the so-long dreamed LEVEL 200!!! You are truly a hero among men, cheers upon you!");
        }
    }
    
    public void setPlayerRates() {
        this.expRate  *=  GameConstants.getPlayerBonusExpRate(this.level / 20);
        this.mesoRate *= GameConstants.getPlayerBonusMesoRate(this.level / 20);
        this.dropRate *= GameConstants.getPlayerBonusDropRate(this.level / 20);
    }

    public void revertLastPlayerRates() {
        this.expRate  /=  GameConstants.getPlayerBonusExpRate((this.level - 1) / 20);
        this.mesoRate /= GameConstants.getPlayerBonusMesoRate((this.level - 1) / 20);
        this.dropRate /= GameConstants.getPlayerBonusDropRate((this.level - 1) / 20);
    }
    
    public void revertPlayerRates() {
        this.expRate  /=  GameConstants.getPlayerBonusExpRate(this.level / 20);
        this.mesoRate /= GameConstants.getPlayerBonusMesoRate(this.level / 20);
        this.dropRate /= GameConstants.getPlayerBonusDropRate(this.level / 20);
    }
    
    public void setWorldRates() {
        World worldz = Server.getInstance().getWorld(world);
        this.expRate *= worldz.getExpRate();
        this.mesoRate *= worldz.getMesoRate();
        this.dropRate *= worldz.getDropRate();
    }
    
    public void revertWorldRates() {
        World worldz = Server.getInstance().getWorld(world);
        this.expRate /= worldz.getExpRate();
        this.mesoRate /= worldz.getMesoRate();
        this.dropRate /= worldz.getDropRate();
    }
    
    private void setCouponRates() {
        List<Integer> couponEffects;
        
        chrLock.lock();
        try {
            setActiveCoupons();
            couponEffects = activateCouponsEffects();
        } finally {
            chrLock.unlock();
        }
        
        for(Integer couponId: couponEffects) {
            commitBuffCoupon(couponId);
        }
    }
    
    private void revertCouponRates() {
        revertCouponsEffects();
    }
    
    public void updateCouponRates() {
        revertCouponRates();
        setCouponRates();
    }
    
    public void resetPlayerRates() {
        expRate = 1;
        mesoRate = 1;
        dropRate = 1;
        
        expCoupon = 1;
        mesoCoupon = 1;
        dropCoupon = 1;
    }
    
    private int getCouponMultiplier(int couponId) {
        return activeCouponRates.get(couponId);
    }
    
    private void setExpCouponRate(int couponId, int couponQty) {
        this.expCoupon *= (getCouponMultiplier(couponId) * couponQty);
    }
    
    private void setDropCouponRate(int couponId, int couponQty) {
        this.dropCoupon *= (getCouponMultiplier(couponId) * couponQty);
        this.mesoCoupon *= (getCouponMultiplier(couponId) * couponQty);
    }
    
    private void revertCouponsEffects() {
        dispelBuffCoupons();
        
        this.expRate /= this.expCoupon;
        this.dropRate /= this.dropCoupon;
        this.mesoRate /= this.mesoCoupon;
        
        this.expCoupon = 1;
        this.dropCoupon = 1;
        this.mesoCoupon = 1;
    }
    
    private List<Integer> activateCouponsEffects() {
        List<Integer> toCommitEffect = new LinkedList<>();
        
        if(ServerConstants.USE_STACK_COUPON_RATES) {
            for(Entry<Integer,Integer> coupon: activeCoupons.entrySet()) {
                int couponId = coupon.getKey();
                int couponQty = coupon.getValue();

                toCommitEffect.add(couponId);
                
                if(ItemConstants.isExpCoupon(couponId)) setExpCouponRate(couponId, couponQty);
                else setDropCouponRate(couponId, couponQty);
            }
        }
        else {
            int maxExpRate = 1, maxDropRate = 1, maxExpCouponId = -1, maxDropCouponId = -1;
            
            for(Entry<Integer,Integer> coupon: activeCoupons.entrySet()) {
                int couponId = coupon.getKey();

                if(ItemConstants.isExpCoupon(couponId)) {
                    if(maxExpRate < getCouponMultiplier(couponId)) {
                        maxExpCouponId = couponId;
                        maxExpRate = getCouponMultiplier(couponId);
                    }
                }
                else {
                    if(maxDropRate < getCouponMultiplier(couponId)) {
                        maxDropCouponId = couponId;
                        maxDropRate = getCouponMultiplier(couponId);
                    }
                }
            }
            
            if(maxExpCouponId > -1) toCommitEffect.add(maxExpCouponId);
            if(maxDropCouponId > -1) toCommitEffect.add(maxDropCouponId);
            
            this.expCoupon = maxExpRate;
            this.dropCoupon = maxDropRate;
            this.mesoCoupon = maxDropRate;
        }
        
        this.expRate *= this.expCoupon;
        this.dropRate *= this.dropCoupon;
        this.mesoRate *= this.mesoCoupon;
        
        return toCommitEffect;
    }
    
    private void setActiveCoupons() {
        activeCoupons.clear();
        activeCouponRates.clear();

        Map<Integer, Integer> coupons = Server.getInstance().getCouponRates();
        List<Integer> active = Server.getInstance().getActiveCoupons();

        for(Item it: this.getInventory(MapleInventoryType.CASH).list()) {
            if(ItemConstants.isRateCoupon(it.getItemId()) && active.contains(it.getItemId())) {
                Integer count = activeCoupons.get(it.getItemId());

                if(count != null) activeCoupons.put(it.getItemId(), count + 1);
                else {
                    activeCoupons.put(it.getItemId(), 1);
                    activeCouponRates.put(it.getItemId(), coupons.get(it.getItemId()));
                }
            }
        }
    }
    
    private void commitBuffCoupon(int couponid) {
        if(!isLoggedin() || getCashShop().isOpened()) return;
        
        MapleStatEffect mse = ii.getItemEffect(couponid);
        mse.applyTo(this);
    }
    
    public void dispelBuffCoupons() {
        List<MapleBuffStatValueHolder> allBuffs = getAllStatups();
        
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (ItemConstants.isRateCoupon(mbsvh.effect.getSourceId())) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }
    
    public Set<Integer> getActiveCoupons() {
        chrLock.lock();
        try {
            return Collections.unmodifiableSet(activeCoupons.keySet());
        } finally {
            chrLock.unlock();
        }    
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        try {
            MapleCharacter ret = new MapleCharacter();
            ret.client = client;
            ret.id = charid;
            
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, charid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("Loading char failed (not found)");
            }
            ret.name = rs.getString("name");
            ret.level = rs.getInt("level");
            ret.fame = rs.getInt("fame");
            ret.quest_fame = rs.getInt("fquest");
            ret.str = rs.getInt("str");
            ret.dex = rs.getInt("dex");
            ret.int_ = rs.getInt("int");
            ret.luk = rs.getInt("luk");
            ret.exp.set(rs.getInt("exp"));
            ret.gachaexp.set(rs.getInt("gachaexp"));
            ret.hp = rs.getInt("hp");
            ret.maxhp = rs.getInt("maxhp");
            ret.mp = rs.getInt("mp");
            ret.maxmp = rs.getInt("maxmp");
            ret.hpMpApUsed = rs.getInt("hpMpUsed");
            ret.hasMerchant = rs.getInt("HasMerchant") == 1;
            String[] skillPoints = rs.getString("sp").split(",");
            for (int i = 0; i < ret.remainingSp.length; i++) {
                ret.remainingSp[i] = Integer.parseInt(skillPoints[i]);
            }
            ret.remainingAp = rs.getInt("ap");
            ret.meso.set(rs.getInt("meso"));
            ret.merchantmeso = rs.getInt("MerchantMesos");
            ret.gmLevel = rs.getInt("gm");
            ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
            ret.gender = rs.getInt("gender");
            ret.job = MapleJob.getById(rs.getInt("job"));
            ret.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
            ret.vanquisherKills = rs.getInt("vanquisherKills");
            ret.omokwins = rs.getInt("omokwins");
            ret.omoklosses = rs.getInt("omoklosses");
            ret.omokties = rs.getInt("omokties");
            ret.matchcardwins = rs.getInt("matchcardwins");
            ret.matchcardlosses = rs.getInt("matchcardlosses");
            ret.matchcardties = rs.getInt("matchcardties");
            ret.hair = rs.getInt("hair");
            ret.face = rs.getInt("face");
            ret.accountid = rs.getInt("accountid");
            ret.mapid = rs.getInt("map");
            ret.jailExpiration = rs.getLong("jailexpire");
            ret.initialSpawnPoint = rs.getInt("spawnpoint");
            ret.world = rs.getByte("world");
            ret.rank = rs.getInt("rank");
            ret.rankMove = rs.getInt("rankMove");
            ret.jobRank = rs.getInt("jobRank");
            ret.jobRankMove = rs.getInt("jobRankMove");
            int mountexp = rs.getInt("mountexp");
            int mountlevel = rs.getInt("mountlevel");
            int mounttiredness = rs.getInt("mounttiredness");
            ret.guildid = rs.getInt("guildid");
            ret.guildRank = rs.getInt("guildrank");
            ret.allianceRank = rs.getInt("allianceRank");
            ret.familyId = rs.getInt("familyId");
            ret.bookCover = rs.getInt("monsterbookcover");
            ret.monsterbook = new MonsterBook();
            ret.monsterbook.loadCards(charid);
            ret.vanquisherStage = rs.getInt("vanquisherStage");
            ret.dojoPoints = rs.getInt("dojoPoints");
            ret.dojoStage = rs.getInt("lastDojoStage");
            ret.dataString = rs.getString("dataString");
            ret.mgc = new MapleGuildCharacter(ret);
            int buddyCapacity = rs.getInt("buddyCapacity");
            ret.buddylist = new BuddyList(buddyCapacity);
            ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equipslots"));
            ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("useslots"));
            ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setupslots"));
            ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etcslots"));
            for (Pair<Item, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(ret.id, !channelserver)) {
                ret.getInventory(item.getRight()).addFromDB(item.getLeft());
                Item itemz = item.getLeft();
                if (itemz.getPetId() > -1) {
                    MaplePet pet = itemz.getPet();
                    if (pet != null && pet.isSummoned()) {
                        ret.addPet(pet);
                    }
                    continue;
                }
                if (item.getRight().equals(MapleInventoryType.EQUIP) || item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                    Equip equip = (Equip) item.getLeft();
                    if (equip.getRingId() > -1) {
                        MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                        if (item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                            ring.equip();
                        }
                        if (ring.getItemId() > 1112012) {
                            ret.addFriendshipRing(ring);
                        } else {
                            ret.addCrushRing(ring);
                        }
                    }
                }
            }
            
            PreparedStatement ps2, ps3;
            ResultSet rs2, rs3;
            
            ps3 = con.prepareStatement("SELECT petid FROM inventoryitems WHERE characterid = ? AND petid > -1");
            ps3.setInt(1, charid);
            rs3 = ps3.executeQuery();
            while(rs3.next()) {
                int petId = rs3.getInt("petid");

                ps2 = con.prepareStatement("SELECT itemid FROM petignores WHERE petid = ?");
                ps2.setInt(1, petId);

                ret.resetExcluded(petId);

                rs2 = ps2.executeQuery();
                while(rs2.next()) {
                    ret.addExcluded(petId, rs2.getInt("itemid"));
                }

                ps2.close();
                rs2.close();
            }
            ps3.close();
            rs3.close();
            
            ret.commitExcludedItems();
            
            if (channelserver) {
                MapleMapFactory mapFactory = client.getChannelServer().getMapFactory();
                ret.map = mapFactory.getMap(ret.mapid);
                
                if (ret.map == null) {
                    ret.map = mapFactory.getMap(100000000);
                }
                MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                if (portal == null) {
                    portal = ret.map.getPortal(0);
                    ret.initialSpawnPoint = 0;
                }
                ret.setPosition(portal.getPosition());
                int partyid = rs.getInt("party");
                MapleParty party = Server.getInstance().getWorld(ret.world).getParty(partyid);
                if (party != null) {
                    ret.mpc = party.getMemberById(ret.id);
                    if (ret.mpc != null) {
                        ret.mpc = new MaplePartyCharacter(ret);
                        ret.party = party;
                    }
                }
                int messengerid = rs.getInt("messengerid");
                int position = rs.getInt("messengerposition");
                if (messengerid > 0 && position < 4 && position > -1) {
                    MapleMessenger messenger = Server.getInstance().getWorld(ret.world).getMessenger(messengerid);
                    if (messenger != null) {
                        ret.messenger = messenger;
                        ret.messengerposition = position;
                    }
                }
                ret.loggedIn = true;
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT mapid,vip FROM trocklocations WHERE characterid = ? LIMIT 15");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            byte v = 0;
            byte r = 0;
            while (rs.next()) {
                if (rs.getInt("vip") == 1) {
                    ret.viptrockmaps.add(rs.getInt("mapid"));
                    v++;
                } else {
                    ret.trockmaps.add(rs.getInt("mapid"));
                    r++;
                }
            }
            while (v < 10) {
                ret.viptrockmaps.add(999999999);
                v++;
            }
            while (r < 5) {
                ret.trockmaps.add(999999999);
                r++;
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT name FROM accounts WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ret.accountid);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.getClient().setAccountName(rs.getString("name"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `area`,`info` FROM area_info WHERE charid = ?");
            ps.setInt(1, ret.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.area_info.put(rs.getShort("area"), rs.getString("info"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `name`,`info` FROM eventstats WHERE characterid = ?");
            ps.setInt(1, ret.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                if (rs.getString("name").equals("rescueGaga")) {
                    ret.events.put(name, new RescueGaga(rs.getInt("info")));
                }
                //ret.events = new MapleEvents(new RescueGaga(rs.getInt("rescuegaga")), new ArtifactHunt(rs.getInt("artifacthunt")));
            }
            rs.close();
            ps.close();
            ret.cashshop = new CashShop(ret.accountid, ret.id, ret.getJobType());
            ret.autoban = new AutobanManager(ret);
            ret.marriageRing = null; //for now
            ps = con.prepareStatement("SELECT name, level FROM characters WHERE accountid = ? AND id != ? ORDER BY level DESC limit 1");
            ps.setInt(1, ret.accountid);
            ps.setInt(2, charid);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.linkedName = rs.getString("name");
                ret.linkedLevel = rs.getInt("level");
            }
            rs.close();
            ps.close();
            if (channelserver) {
                ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                PreparedStatement psf;
                try (PreparedStatement pse = con.prepareStatement("SELECT * FROM questprogress WHERE queststatusid = ?")) {
                    psf = con.prepareStatement("SELECT mapid FROM medalmaps WHERE queststatusid = ?");
                    while (rs.next()) {
                        MapleQuest q = MapleQuest.getInstance(rs.getShort("quest"));
                        MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                        long cTime = rs.getLong("time");
                        if (cTime > -1) {
                            status.setCompletionTime(cTime * 1000);
                        }
                        
                        long eTime = rs.getLong("expires");
                        if (eTime > 0) {
                            status.setExpirationTime(eTime);
                        }
                        
                        status.setForfeited(rs.getInt("forfeited"));
                        ret.quests.put(q.getId(), status);
                        pse.setInt(1, rs.getInt("queststatusid"));
                        try (ResultSet rsProgress = pse.executeQuery()) {
                            while (rsProgress.next()) {
                                status.setProgress(rsProgress.getInt("progressid"), rsProgress.getString("progress"));
                            }
                        }
                        psf.setInt(1, rs.getInt("queststatusid"));
                        try (ResultSet medalmaps = psf.executeQuery()) {
                            while (medalmaps.next()) {
                                status.addMedalMap(medalmaps.getInt("mapid"));
                            }
                        }
                    }
                    rs.close();
                    ps.close();
                }
                psf.close();
                ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel,expiration FROM skills WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getByte("skilllevel"), rs.getInt("masterlevel"), rs.getLong("expiration")));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM cooldowns WHERE charid = ?");
                ps.setInt(1, ret.getId());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int skillid = rs.getInt("SkillID");
                    final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
                    if (skillid != 5221999 && (length + startTime < System.currentTimeMillis())) {
                        continue;
                    }
                    ret.giveCoolDowns(skillid, startTime, length);
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?");
                ps.setInt(1, ret.getId());
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int position = rs.getInt("position");
                    SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                    ret.skillMacros[position] = macro;
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int key = rs.getInt("key");
                    int type = rs.getInt("type");
                    int action = rs.getInt("action");
                    ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(type, action));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `locationtype`,`map`,`portal` FROM savedlocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ret.lastfametime = 0;
                ret.lastmonthfameids = new ArrayList<>(31);
                while (rs.next()) {
                    ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                    ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
                }
                rs.close();
                ps.close();
                ret.buddylist.loadFromDb(charid);
                ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid, ret.world);
                ret.recalcLocalStats();
                //ret.resetBattleshipHp();
                ret.silentEnforceMaxHpMp();
            }
            int mountid = ret.getJobType() * 10000000 + 1004;
            if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18) != null) {
                ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18).getItemId(), mountid);
            } else {
                ret.maplemount = new MapleMount(ret, 0, mountid);
            }
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
            
            con.close();
            return ret;
        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void reloadQuestExpirations() {
        for(MapleQuestStatus mqs: quests.values()) {
            if(mqs.getExpirationTime() > 0) {
                questTimeLimit2(mqs.getQuest(), mqs.getExpirationTime());
            }
        }
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;

    }

    private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public boolean bestApplied;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.value = value;
            this.bestApplied = false;
        }
    }

    public static class MapleCoolDownValueHolder {
        public int skillId;
        public long startTime, length;

        public MapleCoolDownValueHolder(int skillId, long startTime, long length) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
        }
    }

    public void message(String m) {
        dropMessage(5, m);
    }

    public void yellowMessage(String m) {
        announce(MaplePacketCreator.sendYellowTip(m));
    }

    public void mobKilled(int id) {
        // It seems nexon uses monsters that don't exist in the WZ (except string) to merge multiple mobs together for these 3 monsters.
        // We also want to run mobKilled for both since there are some quest that don't use the updated ID...
        if (id == 1110100 || id == 1110130) {
            mobKilled(9101000);
        } else if (id == 2230101 || id == 2230131) {
            mobKilled(9101001);
        } else if (id == 1140100 || id == 1140130) {
            mobKilled(9101002);
        }
		int lastQuestProcessed = 0;
        try {
            synchronized (quests) {
                for (MapleQuestStatus q : quests.values()) {
                    lastQuestProcessed = q.getQuest().getId();
                    if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
                        continue;
                    }
                    String progress = q.getProgress(id);
                    if (!progress.isEmpty() && Integer.parseInt(progress) >= q.getQuest().getMobAmountNeeded(id)) {
                        continue;
                    }
                    if (q.progress(id)) {
                        client.announce(MaplePacketCreator.updateQuest(q, false));
                    }
                }
            }
        } catch (Exception e) {
            FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, e, "MapleCharacter.mobKilled. CID: " + this.id + " last Quest Processed: " + lastQuestProcessed);
        }
    }

    public void mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    public void playerNPC(MapleCharacter v, int scriptId) {
        int npcId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM playernpcs WHERE ScriptId = ?");
            ps.setInt(1, scriptId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps = con.prepareStatement("INSERT INTO playernpcs (name, hair, face, skin, x, cy, map, ScriptId, Foothold, rx0, rx1) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, v.getName());
                ps.setInt(2, v.getHair());
                ps.setInt(3, v.getFace());
                ps.setInt(4, v.getSkinColor().getId());
                ps.setInt(5, getPosition().x);
                ps.setInt(6, getPosition().y);
                ps.setInt(7, getMapId());
                ps.setInt(8, scriptId);
                ps.setInt(9, getMap().getFootholds().findBelow(getPosition()).getId());
                ps.setInt(10, getPosition().x + 50);
                ps.setInt(11, getPosition().x - 50);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                npcId = rs.getInt(1);
                ps.close();
                ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
                ps.setInt(1, npcId);
                for (Item equip : getInventory(MapleInventoryType.EQUIPPED)) {
                    int position = Math.abs(equip.getPosition());
                    if ((position < 12 && position > 0) || (position > 100 && position < 112)) {
                        ps.setInt(2, equip.getItemId());
                        ps.setInt(3, equip.getPosition());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                ps.close();
                rs.close();
                ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
                ps.setInt(1, scriptId);
                rs = ps.executeQuery();
                rs.next();
                PlayerNPCs pn = new PlayerNPCs(rs);
                for (Channel channel : Server.getInstance().getChannelsFromWorld(world)) {
                    MapleMap m = channel.getMapFactory().getMap(getMapId());
                    m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
                    m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                    m.addMapObject(pn);
                }
            }
            ps.close();
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void playerDead() {
        cancelAllBuffs(false);
        dispelDebuffs();
        
        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            eim.playerKilled(this);
        }
        int[] charmID = {5130000, 4031283, 4140903};
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }
        if (possesed > 0) {
            message("You have used a safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(client, ii.getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else if (mapid > 925020000 && mapid < 925030000) {
            this.dojoStage = 0;
        } else if (mapid > 980000100 && mapid < 980000700) {
            getMap().broadcastMessage(this, MaplePacketCreator.CPQDied(this));
        } else if (getJob() != MapleJob.BEGINNER) { //Hmm...
            int XPdummy = ExpTable.getExpNeededForLevel(getLevel());
            if (getMap().isTown()) {
                XPdummy /= 100;
            }
            if (XPdummy == ExpTable.getExpNeededForLevel(getLevel())) {
                if (getLuk() <= 100 && getLuk() > 8) {
                    XPdummy *= (200 - getLuk()) / 2000;
                } else if (getLuk() < 8) {
                    XPdummy /= 10;
                } else {
                    XPdummy /= 20;
                }
            }
            if (getExp() > XPdummy) {
                loseExp(XPdummy, false, false);
            } else {
                loseExp(getExp(), false, false);
            }
        }
        if (getBuffedValue(MapleBuffStat.MORPH) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        }

        if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }

        if (getChair() != 0) {
            setChair(0);
            client.announce(MaplePacketCreator.cancelChair(-1));
            getMap().broadcastMessage(this, MaplePacketCreator.showChair(getId(), 0), false);
        }
        client.announce(MaplePacketCreator.enableActions());
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        dragonBloodSchedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if(awayFromWorld.get()) return;
                
                addHP(-bloodEffect.getX());
                client.announce(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
                getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
                checkBerserk(isHidden());
            }
        }, 4000, 4000);
    }

    private void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        int speed = 100, jump = 100;
        magic = localint_;
        watk = 0;

        for (Item item : getInventory(MapleInventoryType.EQUIPPED)) {
            Equip equip = (Equip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
            speed += equip.getSpeed();
            jump += equip.getJump();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null) {
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        }
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null) {
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        }
        localmaxhp = Math.min(30000, localmaxhp);
        localmaxmp = Math.min(30000, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null) {
            watk += watkbuff.intValue();
        }
        MapleStatEffect combo = getBuffEffect(MapleBuffStat.ARAN_COMBO);
        if (combo != null) {
            watk += combo.getX();
        }

        if (energybar == 15000) {
            Skill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
            MapleStatEffect ceffect = energycharge.getEffect(getSkillLevel(energycharge));
            watk += ceffect.getWatk();
        }

        Integer mwarr = getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
        if (mwarr != null) {
            localstr += getStr() * mwarr / 100;
            localdex += getDex() * mwarr / 100;
            localint_ += getInt() * mwarr / 100;
            localluk += getLuk() * mwarr / 100;
        }
        if (job.isA(MapleJob.BOWMAN)) {
            Skill expert = null;
            if (job.isA(MapleJob.MARKSMAN)) {
                expert = SkillFactory.getSkill(3220004);
            } else if (job.isA(MapleJob.BOWMASTER)) {
                expert = SkillFactory.getSkill(3120005);
            }
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0) {
                    watk += expert.getEffect(boostLevel).getX();
                }
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null) {
            magic += matkbuff.intValue();
        }
        Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
        if (speedbuff != null) {
            speed += speedbuff.intValue();
        }
        Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
        if (jumpbuff != null) {
            jump += jumpbuff.intValue();
        }

        Integer blessing = getSkillLevel(10000000 * getJobType() + 12);
        if (blessing > 0) {
            watk += blessing;
            magic += blessing * 2;
        }

        if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.NIGHTWALKER1) || job.isA(MapleJob.WINDARCHER1)) {
            Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
            if (weapon_item != null) {
                MapleWeaponType weapon = ii.getWeaponType(weapon_item.getItemId());
                boolean bow = weapon == MapleWeaponType.BOW;
                boolean crossbow = weapon == MapleWeaponType.CROSSBOW;
                boolean claw = weapon == MapleWeaponType.CLAW;
                boolean gun = weapon == MapleWeaponType.GUN;
                if (bow || crossbow || claw || gun) {
                    // Also calc stars into this.
                    MapleInventory inv = getInventory(MapleInventoryType.USE);
                    for (short i = 1; i <= inv.getSlotLimit(); i++) {
                        Item item = inv.getItem(i);
                        if (item != null) {
                            if ((claw && ItemConstants.isThrowingStar(item.getItemId())) || (gun && ItemConstants.isBullet(item.getItemId())) || (bow && ItemConstants.isArrowForBow(item.getItemId())) || (crossbow && ItemConstants.isArrowForCrossBow(item.getItemId()))) {
                                if (item.getQuantity() > 0) {
                                    // Finally there!
                                    watk += ii.getWatkForProjectile(item.getItemId());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // Add throwing stars to dmg.
        }

        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            updatePartyMemberHP();
        }
    }

    public void receivePartyMemberHP() {
        prtLock.lock();
        try {
            if (party != null) {
                int channel = client.getChannel();
                for (MaplePartyCharacter partychar : party.getMembers()) {
                    if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                        MapleCharacter other = Server.getInstance().getWorld(world).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                        if (other != null) {
                            client.announce(MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                        }
                    }
                }
            }
        } finally {
            prtLock.unlock();
        }
    }

    public void removeAllCooldownsExcept(int id, boolean packet) {
        effLock.lock();
        chrLock.lock();
        try {
            ArrayList<MapleCoolDownValueHolder> list = new ArrayList<>(coolDowns.values());
            for (MapleCoolDownValueHolder mcvh : list) {
                if (mcvh.skillId != id) {
                    coolDowns.remove(mcvh.skillId);
                    if (packet) {
                        client.announce(MaplePacketCreator.skillCooldown(mcvh.skillId, 0));
                    }
                }
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public static void removeAriantRoom(int room) {
        ariantroomleader[room] = "";
        ariantroomslot[room] = 0;
    }

    public void removeCooldown(int skillId) {
        effLock.lock();
        chrLock.lock();
        try {
            if (this.coolDowns.containsKey(skillId)) {
                this.coolDowns.remove(skillId);
            }
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        petLock.lock();
        try {
            int slot = -1;
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    if (pets[i].getUniqueId() == pet.getUniqueId()) {
                        pets[i] = null;
                        slot = i;
                        break;
                    }
                }
            }
            if (shift_left) {
                if (slot > -1) {
                    for (int i = slot; i < 3; i++) {
                        if (i != 2) {
                            pets[i] = pets[i + 1];
                        } else {
                            pets[i] = null;
                        }
                    }
                }
            }
        } finally {
            petLock.unlock();
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public void resetStats() {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
        int tap = 0, tsp = 1;
        int tstr = 4, tdex = 4, tint = 4, tluk = 4;
        int levelap = (isCygnus() ? 6 : 5);
        switch (job.getId()) {
            case 100:
            case 1100:
            case 2100:
                tstr = 35;
                tap = ((getLevel() - 10) * levelap) + 14;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 200:
            case 1200:
                tint = 20;
                tap = ((getLevel() - 8) * levelap) + 29;
                tsp += ((getLevel() - 8) * 3);
                break;
            case 300:
            case 1300:
            case 400:
            case 1400:
                tdex = 25;
                tap = ((getLevel() - 10) * levelap) + 24;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 500:
            case 1500:
                tdex = 20;
                tap = ((getLevel() - 10) * levelap) + 29;
                tsp += ((getLevel() - 10) * 3);
                break;
        }
        this.remainingAp = tap;
        this.remainingSp[GameConstants.getSkillBook(job.getId())] = tsp;
        this.dex = tdex;
        this.int_ = tint;
        this.str = tstr;
        this.luk = tluk;
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, tap));
        statup.add(new Pair<>(MapleStat.AVAILABLESP, tsp));
        statup.add(new Pair<>(MapleStat.STR, tstr));
        statup.add(new Pair<>(MapleStat.DEX, tdex));
        statup.add(new Pair<>(MapleStat.INT, tint));
        statup.add(new Pair<>(MapleStat.LUK, tluk));
        announce(MaplePacketCreator.updatePlayerStats(statup, this));
    }

    public void resetBattleshipHp() {
        this.battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP)) + ((getLevel() - 120) * 2000);
    }

    public void resetEnteredScript() {
        if (entered.containsKey(map.getId())) {
            entered.remove(map.getId());
        }
    }

    public void resetEnteredScript(int mapId) {
        if (entered.containsKey(mapId)) {
            entered.remove(mapId);
        }
    }

    public void resetEnteredScript(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                entered.remove(mapId);
            }
        }
    }

    public synchronized void saveCooldowns() {
        List<PlayerCoolDownValueHolder> listcd = getAllCooldowns();
                
        if (listcd.size() > 0) {
            try {
                Connection con = DatabaseConnection.getConnection();
                deleteWhereCharacterId(con, "DELETE FROM cooldowns WHERE charid = ?");
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, getId());
                    for (PlayerCoolDownValueHolder cooling : listcd) {
                        ps.setInt(2, cooling.skillId);
                        ps.setLong(3, cooling.startTime);
                        ps.setLong(4, cooling.length);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                
                con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public void saveGuildStatus() {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?")) {
                ps.setInt(1, guildid);
                ps.setInt(2, guildRank);
                ps.setInt(3, allianceRank);
                ps.setInt(4, id);
                ps.executeUpdate();
            }
            
            con.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void saveLocation(String type) {
        MaplePortal closest = map.findClosestPortal(getPosition());
        savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
    }

    public final boolean insertNewChar() {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DatabaseConnection.getConnection();
            
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            ps = con.prepareStatement("INSERT INTO characters (str, dex, luk, `int`, gm, skincolor, gender, job, hair, face, map, meso, spawnpoint, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, 12);
            ps.setInt(2, 5);
            ps.setInt(3, 4);
            ps.setInt(4, 4);
            ps.setInt(5, gmLevel);
            ps.setInt(6, skinColor.getId());
            ps.setInt(7, gender);
            ps.setInt(8, getJob().getId());
            ps.setInt(9, hair);
            ps.setInt(10, face);
            ps.setInt(11, mapid);
            ps.setInt(12, Math.abs(meso.get()));
            ps.setInt(13, 0);
            ps.setInt(14, accountid);
            ps.setString(15, name);
            ps.setInt(16, world);

            int updateRows = ps.executeUpdate();
            if (updateRows < 1) {
                ps.close();
                FilePrinter.printError(FilePrinter.INSERT_CHAR, "Error trying to insert " + name);
                return false;
            }
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                this.id = rs.getInt(1);
                rs.close();
                ps.close();
            } else {
                rs.close();
                ps.close();
                FilePrinter.printError(FilePrinter.INSERT_CHAR, "Inserting char failed " + name);
                return false;
            }

            // Select a keybinding method
            int[] selectedKey;
            int[] selectedType;
            int[] selectedAction;

            if(ServerConstants.USE_CUSTOM_KEYSET) {
                selectedKey = GameConstants.getCustomKey(true);
                selectedType = GameConstants.getCustomType(true);
                selectedAction = GameConstants.getCustomAction(true);
            }
            else {
                selectedKey = GameConstants.getCustomKey(false);
                selectedType = GameConstants.getCustomType(false);
                selectedAction = GameConstants.getCustomAction(false);
            }
            
            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (int i = 0; i < selectedKey.length; i++) {
                ps.setInt(2, selectedKey[i]);
                ps.setInt(3, selectedType[i]);
                ps.setInt(4, selectedAction[i]);
                ps.execute();
            }
            ps.close();

            final List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

            for (MapleInventory iv : inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair<>(item, iv.getType()));
                }
            }

            ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
            con.commit();
            return true;
        } catch (Throwable t) {
            FilePrinter.printError(FilePrinter.INSERT_CHAR, t, "Error creating " + name + " Level: " + level + " Job: " + job.getId());
            try {
                con.rollback();
            } catch (SQLException se) {
                FilePrinter.printError(FilePrinter.INSERT_CHAR, se, "Error trying to rollback " + name);
            }
            return false;
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveToDB() {
        if(ServerConstants.USE_AUTOSAVE) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    saveToDB(true);
                }
            };
            
            Thread t = new Thread(r);  //spawns a new thread to deal with this
            t.start();
        } else {
            saveToDB(true);
        }
    }
    
    public synchronized void saveToDB(boolean notAutosave) {
        Calendar c = Calendar.getInstance();
        
        if(notAutosave) FilePrinter.print(FilePrinter.SAVING_CHARACTER, "Attempting to save " + name + " at " + c.getTime().toString());
        else FilePrinter.print(FilePrinter.AUTOSAVING_CHARACTER, "Attempting to autosave " + name + " at " + c.getTime().toString());
        
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            PreparedStatement ps;
            ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, gachaexp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpMpUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?,  monsterbookcover = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ?, matchcardwins = ?, matchcardlosses = ?, matchcardties = ?, omokwins = ?, omoklosses = ?, omokties = ?, dataString = ?, fquest = ?, jailexpire = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
            if (gmLevel < 1 && level > 199) {
                ps.setInt(1, isCygnus() ? 120 : 200);
            } else {
                ps.setInt(1, level);
            }
            ps.setInt(2, fame);
            ps.setInt(3, str);
            ps.setInt(4, dex);
            ps.setInt(5, luk);
            ps.setInt(6, int_);
            ps.setInt(7, Math.abs(exp.get()));
            ps.setInt(8, Math.abs(gachaexp.get()));
            ps.setInt(9, hp);
            ps.setInt(10, mp);
            ps.setInt(11, maxhp);
            ps.setInt(12, maxmp);
            StringBuilder sps = new StringBuilder();
            for (int i = 0; i < remainingSp.length; i++) {
                sps.append(remainingSp[i]);
                sps.append(",");
            }
            String sp = sps.toString();
            ps.setString(13, sp.substring(0, sp.length() - 1));
            ps.setInt(14, remainingAp);
            ps.setInt(15, gmLevel);
            ps.setInt(16, skinColor.getId());
            ps.setInt(17, gender);
            ps.setInt(18, job.getId());
            ps.setInt(19, hair);
            ps.setInt(20, face);
            if (map == null || (cashshop != null && cashshop.isOpened())) {
                ps.setInt(21, mapid);
            } else {
                if (map.getForcedReturnId() != 999999999) {
                    ps.setInt(21, map.getForcedReturnId());
                } else {
                    ps.setInt(21, getHp() < 1 ? map.getReturnMapId() : map.getId());
                }
            }
            ps.setInt(22, meso.get());
            ps.setInt(23, hpMpApUsed);
            if (map == null || map.getId() == 610020000 || map.getId() == 610020001) {  // reset to first spawnpoint on those maps
                ps.setInt(24, 0);
            } else {
                MaplePortal closest = map.findClosestPlayerSpawnpoint(getPosition());
                if (closest != null) {
                    ps.setInt(24, closest.getId());
                } else {
                    ps.setInt(24, 0);
                }
            }
            
            prtLock.lock();
            try {
                if (party != null) {
                    ps.setInt(25, party.getId());
                } else {
                    ps.setInt(25, -1);
                }
            } finally {
                prtLock.unlock();
            }
            
            ps.setInt(26, buddylist.getCapacity());
            if (messenger != null) {
                ps.setInt(27, messenger.getId());
                ps.setInt(28, messengerposition);
            } else {
                ps.setInt(27, 0);
                ps.setInt(28, 4);
            }
            if (maplemount != null) {
                ps.setInt(29, maplemount.getLevel());
                ps.setInt(30, maplemount.getExp());
                ps.setInt(31, maplemount.getTiredness());
            } else {
                ps.setInt(29, 1);
                ps.setInt(30, 0);
                ps.setInt(31, 0);
            }
            for (int i = 1; i < 5; i++) {
                ps.setInt(i + 31, getSlots(i));
            }

            monsterbook.saveCards(getId());

            ps.setInt(36, bookCover);
            ps.setInt(37, vanquisherStage);
            ps.setInt(38, dojoPoints);
            ps.setInt(39, dojoStage);
            ps.setInt(40, finishedDojoTutorial ? 1 : 0);
            ps.setInt(41, vanquisherKills);
            ps.setInt(42, matchcardwins);
            ps.setInt(43, matchcardlosses);
            ps.setInt(44, matchcardties);
            ps.setInt(45, omokwins);
            ps.setInt(46, omoklosses);
            ps.setInt(47, omokties);
            ps.setString(48, dataString);
            ps.setInt(49, quest_fame);
            ps.setLong(50, jailExpiration);
            ps.setInt(51, id);

            int updateRows = ps.executeUpdate();
            if (updateRows < 1) {
                throw new RuntimeException("Character not in database (" + id + ")");
            }
            
            petLock.lock();
            try {
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        pets[i].saveToDb();
                    }
                }
            } finally {
                petLock.unlock();
            }
            
            for(Entry<Integer, Set<Integer>> es: getExcluded().entrySet()) {    // this set is already protected
                try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM petignores WHERE petid=?")) {
                    ps2.setInt(1, es.getKey());
                    ps2.executeUpdate();
                }
                
                try (PreparedStatement ps2 = con.prepareStatement("INSERT INTO petignores (petid, itemid) VALUES (?, ?)")) {
                    ps2.setInt(1, es.getKey());
                    for(Integer x: es.getValue()) {
                        ps2.setInt(2, x);
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }
            }
            
            deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            
            Set<Entry<Integer, MapleKeyBinding>> keybindingItems = Collections.unmodifiableSet(keymap.entrySet());
            for (Entry<Integer, MapleKeyBinding> keybinding : keybindingItems) {
                ps.setInt(2, keybinding.getKey());
                ps.setInt(3, keybinding.getValue().getType());
                ps.setInt(4, keybinding.getValue().getAction());
                ps.addBatch();
            }
            ps.executeBatch();
            
            deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, getId());
            for (int i = 0; i < 5; i++) {
                SkillMacro macro = skillMacros[i];
                if (macro != null) {
                    ps.setInt(2, macro.getSkill1());
                    ps.setInt(3, macro.getSkill2());
                    ps.setInt(4, macro.getSkill3());
                    ps.setString(5, macro.getName());
                    ps.setInt(6, macro.getShout());
                    ps.setInt(7, i);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            
            List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
            for (MapleInventory iv : inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair<>(item, iv.getType()));
                }
            }
            ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
			
            deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
                ps.setInt(2, skill.getKey().getId());
                ps.setInt(3, skill.getValue().skillevel);
                ps.setInt(4, skill.getValue().masterlevel);
                ps.setLong(5, skill.getValue().expiration);
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`, `portal`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                if (savedLocations[savedLocationType.ordinal()] != null) {
                    ps.setString(2, savedLocationType.name());
                    ps.setInt(3, savedLocations[savedLocationType.ordinal()].getMapId());
                    ps.setInt(4, savedLocations[savedLocationType.ordinal()].getPortal());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 0)");
            for (int i = 0; i < getTrockSize(); i++) {
                if (trockmaps.get(i) != 999999999) {
                    ps.setInt(1, getId());
                    ps.setInt(2, trockmaps.get(i));
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 1)");
            for (int i = 0; i < getVipTrockSize(); i++) {
                if (viptrockmaps.get(i) != 999999999) {
                    ps.setInt(1, getId());
                    ps.setInt(2, viptrockmaps.get(i));
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
            ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`, `group`) VALUES (?, ?, 0, ?)");
            ps.setInt(1, id);
            for (BuddylistEntry entry : buddylist.getBuddies()) {
                if (entry.isVisible()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setString(3, entry.getGroup());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM area_info WHERE charid = ?");
            ps = con.prepareStatement("INSERT INTO area_info (id, charid, area, info) VALUES (DEFAULT, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Short, String> area : area_info.entrySet()) {
                ps.setInt(2, area.getKey());
                ps.setString(3, area.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM eventstats WHERE characterid = ?");
            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `expires`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement psf;
            try (PreparedStatement pse = con.prepareStatement("INSERT INTO questprogress VALUES (DEFAULT, ?, ?, ?)")) {
                psf = con.prepareStatement("INSERT INTO medalmaps VALUES (DEFAULT, ?, ?)");
                ps.setInt(1, id);
                
                synchronized (quests) {
                    for (MapleQuestStatus q : quests.values()) {
                        ps.setInt(2, q.getQuest().getId());
                        ps.setInt(3, q.getStatus().getId());
                        ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                        ps.setLong(5, q.getExpirationTime());
                        ps.setInt(6, q.getForfeited());
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            rs.next();
                            for (int mob : q.getProgress().keySet()) {
                                pse.setInt(1, rs.getInt(1));
                                pse.setInt(2, mob);
                                pse.setString(3, q.getProgress(mob));
                                pse.addBatch();
                            }
                            for (int i = 0; i < q.getMedalMaps().size(); i++) {
                                psf.setInt(1, rs.getInt(1));
                                psf.setInt(2, q.getMedalMaps().get(i));
                                psf.addBatch();
                            }
                            pse.executeBatch();
                            psf.executeBatch();
                        }
                    }
                }
            }
            psf.close();
            ps = con.prepareStatement("UPDATE accounts SET gm = ? WHERE id = ?");
            ps.setInt(1, gmLevel > 1 ? 1 : 0);
            ps.setInt(2, client.getAccID());
            ps.executeUpdate();
            ps.close();
			
            con.commit();
            con.setAutoCommit(true);
			
            if (cashshop != null) {
                cashshop.save(con);
            }
            if (storage != null) {
                storage.saveToDB(con);
            }
        } catch (SQLException | RuntimeException t) {
            FilePrinter.printError(FilePrinter.SAVE_CHAR, t, "Error saving " + name + " Level: " + level + " Job: " + job.getId());
            try {
                con.rollback();
            } catch (SQLException se) {
                FilePrinter.printError(FilePrinter.SAVE_CHAR, se, "Error trying to rollback " + name);
            }
        } finally {
            try {
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPolice(int greason, String reason, int duration) {
        announce(MaplePacketCreator.sendPolice(String.format("You have been blocked by the#b %s Police for %s.#k", "Solaxia", reason)));
        this.isbanned = true;
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                client.disconnect(false, false);
            }
        }, duration);
    }

    public void sendPolice(String text) {
        String message = getName() + " received this - " + text;
        if (Server.getInstance().isGmOnline()) { //Alert and log if a GM is online
            Server.getInstance().broadcastGMMessage(MaplePacketCreator.sendYellowTip(message));
            FilePrinter.printError("autobanwarning.txt", message + "\r\n");
        } else { //Auto DC and log if no GM is online
            client.disconnect(false, false);
            FilePrinter.printError("autobandced.txt", message + "\r\n");
        }
        //Server.getInstance().broadcastGMMessage(0, MaplePacketCreator.serverNotice(1, getName() + " received this - " + text));
        //announce(MaplePacketCreator.sendPolice(text));
        //this.isbanned = true;
        //TimerManager.getInstance().schedule(new Runnable() {
        //    @Override
        //    public void run() {
        //        client.disconnect(false, false);
        //    }
        //}, 6000);
    }

    public void sendKeymap() {
        client.announce(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        // Always send the macro packet to fix a client side bug when switching characters.
        client.announce(MaplePacketCreator.getMacros(skillMacros));
    }

    public void sendNote(String to, String msg, byte fame) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `fame`) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, to);
            ps.setString(2, this.getName());
            ps.setString(3, msg);
            ps.setLong(4, System.currentTimeMillis());
            ps.setByte(5, fame);
            ps.executeUpdate();
        } finally {
            con.close();
        }
    }

    public void setAllowWarpToId(int id) {
        this.warpToId = id;
    }

    public static void setAriantRoomLeader(int room, String charname) {
        ariantroomleader[room] = charname;
    }

    public static void setAriantSlotRoom(int room, int slot) {
        ariantroomslot[room] = slot;
    }

    public void setBattleshipHp(int battleshipHp) {
        this.battleshipHp = battleshipHp;
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.announce(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        effLock.lock();
        chrLock.lock();
        try {
            MapleBuffStatValueHolder mbsvh = effects.get(effect);
            if (mbsvh == null) {
                return;
            }
            mbsvh.value = value;
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public void setChair(int chair) {
        this.chair.set(chair);
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setDojoEnergy(int x) {
        this.dojoEnergy = Math.min(x, 10000);
    }

    public void setDojoPoints(int x) {
        this.dojoPoints = x;
    }

    public void setDojoStage(int x) {
        this.dojoStage = x;
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public void setGachaExp(int amount) {
        this.gachaexp.set(amount);
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public void setFinishedDojoTutorial() {
        this.finishedDojoTutorial = true;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGM(int level) {
        this.gmLevel = level;
    }

    public void setGuildId(int _id) {
        guildid = _id;
    }

    public void setGuildRank(int _rank) {
        guildRank = _rank;
    }
    
    public void setAllianceRank(int _rank) {
        allianceRank = _rank;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setHasMerchant(boolean set) {
        try {
            Connection con = DatabaseConnection.getConnection();
            
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?")) {
                ps.setInt(1, set ? 1 : 0);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        hasMerchant = set;
    }

    public void addMerchantMesos(int add) {
        int newAmount;
        
        try {
            newAmount = (int)Math.min((long)merchantmeso + add, Integer.MAX_VALUE);
            
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, newAmount);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        merchantmeso = newAmount;
    }

    public void setMerchantMeso(int set) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, set);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        merchantmeso = set;
    }

    public void setHiredMerchant(MapleHiredMerchant merchant) {
        this.hiredMerchant = merchant;
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public void setHp(int newhp, boolean silent) {
        int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;
        if (!silent) {
            updatePartyMemberHP();
        }
        if (oldHp > hp && !isAlive()) {
            playerDead();
        }
    }

    public void setHpMpApUsed(int mpApUsed) {
        this.hpMpApUsed = mpApUsed;
    }

    public void setHpMp(int x) {
        setHp(x);
        setMp(x);
        updateSingleStat(MapleStat.HP, hp);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public void setInventory(MapleInventoryType type, MapleInventory inv) {
        inventory[type.ordinal()] = inv;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public void setLastHealed(long time) {
        this.lastHealed = time;
    }

    public void setLastUsedCashItem(long time) {
        this.lastUsedCashItem = time;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public void setMarkedMonster(int markedMonster) {
        this.markedMonster = markedMonster;
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxHp(int hp, boolean ap) {
        hp = Math.min(30000, hp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp, boolean ap) {
        mp = Math.min(30000, mp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public void setMiniGamePoints(MapleCharacter visitor, int winnerslot, boolean omok) {
        if (omok) {
            if (winnerslot == 1) {
                this.omokwins++;
                visitor.omoklosses++;
            } else if (winnerslot == 2) {
                visitor.omokwins++;
                this.omoklosses++;
            } else {
                this.omokties++;
                visitor.omokties++;
            }
        } else {
            if (winnerslot == 1) {
                this.matchcardwins++;
                visitor.matchcardlosses++;
            } else if (winnerslot == 2) {
                visitor.matchcardwins++;
                this.matchcardlosses++;
            } else {
                this.matchcardties++;
                visitor.matchcardties++;
            }
        }
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public void setMp(int newmp) {
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        this.name = name;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE `characters` SET `name` = ? WHERE `id` = ?");
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setParty(MapleParty p) {
        prtLock.lock();
        try {
            if (p == null) {
                this.mpc = null;
                doorSlot = -1;

                party = null;
                //cancelMagicDoor();  // cancel magic doors if kicked out / quitted from party.
            } else {
                party = p;
            }
        } finally {
            prtLock.unlock();
        }
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp[GameConstants.getSkillBook(job.getId())] = remainingSp; //default
    }

    public void setRemainingSp(int remainingSp, int skillbook) {
        this.remainingSp[skillbook] = remainingSp;
    }

    public void setSearch(String find) {
        search = find;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public byte getSlots(int type) {
        return type == MapleInventoryType.CASH.getType() ? 96 : inventory[type].getSlotLimit();
    }

    public boolean gainSlots(int type, int slots) {
        return gainSlots(type, slots, true);
    }

    public boolean gainSlots(int type, int slots, boolean update) {
        slots += inventory[type].getSlotLimit();
        if (slots <= 96) {
            inventory[type].setSlotLimit(slots);

            this.saveToDB();
            if (update) {
                client.announce(MaplePacketCreator.updateInventorySlotLimit(type, slots));
            }

            return true;
        }

        return false;
    }
    
    public int sellAllItemsFromName(byte invTypeId, String name) {
        //player decides from which inventory items should be sold.
        
        MapleInventoryType type = MapleInventoryType.getByType(invTypeId);
        
        Item it = getInventory(type).findByName(name);
        if(it == null) {
            return(-1);
        }
        
        return(sellAllItemsFromPosition(ii, type, it.getPosition()));
    }
    
    public int sellAllItemsFromPosition(MapleItemInformationProvider ii, MapleInventoryType type, short pos) {
        int mesoGain = 0;
        
        for(short i = pos; i <= getInventory(type).getSlotLimit(); i++) {
            if(getInventory(type).getItem(i) == null) continue;
            mesoGain += standaloneSell(getClient(), ii, type, i, getInventory(type).getItem(i).getQuantity());
        }
        
        return(mesoGain);
    }

    private int standaloneSell(MapleClient c, MapleItemInformationProvider ii, MapleInventoryType type, short slot, short quantity) {
        if (quantity == 0xFFFF || quantity == 0) {
            quantity = 1;
        }
        Item item = getInventory(type).getItem((short) slot);
        if (item == null){ //Basic check
            return(0);
        }
        if (ItemConstants.isRechargable(item.getItemId())) {
            quantity = item.getQuantity();
        }
        if (quantity < 0) {
            return(0);
        }
        short iQuant = item.getQuantity();
        if (iQuant == 0xFFFF) {
            iQuant = 1;
        }
        
        if (quantity <= iQuant && iQuant > 0) {
            MapleInventoryManipulator.removeFromSlot(c, type, (byte) slot, quantity, false);
            double price;
            if (ItemConstants.isRechargable(item.getItemId())) {
            	price = ii.getWholePrice(item.getItemId()) / (double) ii.getSlotMax(c, item.getItemId());
            } else {
                price = ii.getPrice(item.getItemId());
            }
            
            int recvMesos = (int) Math.max(Math.ceil(price * quantity), 0);
            if (price != -1 && recvMesos > 0) {
                gainMeso(recvMesos, false);
                return(recvMesos);
            }
        }
        
        return(0);
    }
    
    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public void setVanquisherKills(int x) {
        this.vanquisherKills = x;
    }

    public void setVanquisherStage(int x) {
        this.vanquisherStage = x;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void shiftPetsRight() {
        petLock.lock();
        try {
            if (pets[2] == null) {
                pets[2] = pets[1];
                pets[1] = pets[0];
                pets[0] = null;
            }
        } finally {
            petLock.unlock();
        }
    }

    private long getDojoTimeLeft() {
        return client.getChannelServer().getDojoFinishTime(map.getId()) - System.currentTimeMillis();
    }
    
    public void showDojoClock() {
        if (map.isDojoFightMap()) {
            client.announce(MaplePacketCreator.getClock((int) (getDojoTimeLeft() / 1000)));
        }
    }
    
    public void timeoutFromDojo() {
        if(map.isDojoMap()) {
            client.getPlayer().changeMap(client.getChannelServer().getMapFactory().getMap(925020002));
        }
    }
    
    public void showUnderleveledInfo(MapleMonster mob) {
        chrLock.lock();
        try {
            long curTime = System.currentTimeMillis();
            if(nextUnderlevelTime < curTime) {
                nextUnderlevelTime = curTime + (60 * 1000);   // show underlevel info again after 1 minute
                
                showHint("You have gained #rno experience#k from defeating #e#b" + mob.getName() + "#k#n (lv. #b" + mob.getLevel() + "#k)! Take note you must have around the same level as the mob to start earning EXP from it.");
            }
        } finally {
            chrLock.unlock();
        }
    }
    
    public void showHint(String msg) {
        client.announceHint(msg);
    }
    
    public void showNote() {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=? AND `deleted` = 0", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                ps.setString(1, this.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.last();
                    int count = rs.getRow();
                    rs.first();
                    client.announce(MaplePacketCreator.showNotes(rs, count));
                }
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }

    public void silentGiveBuffs(List<Pair<Long, PlayerBuffValueHolder>> buffs) {
        for (Pair<Long, PlayerBuffValueHolder> mbsv : buffs) {
            PlayerBuffValueHolder mbsvh = mbsv.getRight();
            mbsvh.effect.silentApplyBuff(this, mbsv.getLeft());
        }
    }

    public void silentPartyUpdate() {
        prtLock.lock();
        try {
            silentPartyUpdateInternal();
        } finally {
            prtLock.unlock();
        }
    }
    
    private void silentPartyUpdateInternal() {
        if (party != null) {
            Server.getInstance().getWorld(world).updateParty(party.getId(), PartyOperation.SILENT_UPDATE, getMPC());
        }
    }

    public static class SkillEntry {

        public int masterlevel;
        public byte skillevel;
        public long expiration;

        public SkillEntry(byte skillevel, int masterlevel, long expiration) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
            this.expiration = expiration;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }

    public boolean skillIsCooling(int skillId) {
        effLock.lock();
        chrLock.lock();
        try {
            return coolDowns.containsKey(Integer.valueOf(skillId));
        } finally {
            chrLock.unlock();
            effLock.unlock();
        }
    }

    public void runFullnessSchedule(int petSlot) {
        MaplePet pet = getPet(petSlot);
        if(pet == null) return;
        
        int newFullness = pet.getFullness() - PetDataFactory.getHunger(pet.getItemId());
        if (newFullness <= 5) {
            pet.setFullness(15);
            pet.saveToDb();
            unequipPet(pet, true);
            dropMessage(6, "Your pet grew hungry! Treat it some pet food to keep it healthy!");
        } else {
            pet.setFullness(newFullness);
            pet.saveToDb();
            Item petz = getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            if (petz != null) {
                forceUpdateItem(petz);
            }
        }
    }
    
    public void runTirednessSchedule() {
        if(maplemount != null) {
            int tiredness = maplemount.incrementAndGetTiredness();
            
            this.getMap().broadcastMessage(MaplePacketCreator.updateMount(this.getId(), maplemount, false));
            if (tiredness > 99) {
                maplemount.setTiredness(99);
                this.dispelSkill(this.getJobType() * 10000000 + 1004);
                this.dropMessage(6, "Your mount grew tired! Treat it some revitalizer before riding it again!");
            }
        }
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public void startMapEffect(String msg, int itemId, int duration) {
        final MapleMapEffect mapEffect = new MapleMapEffect(msg, itemId);
        getClient().announce(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                getClient().announce(mapEffect.makeDestroyData());
            }
        }, duration);
    }

    public void stopControllingMonster(MapleMonster monster) {
        controlled.remove(monster);
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            MaplePet pet = getPet(i);
            if (pet != null) {
                unequipPet(pet, true);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        if (this.getPet(this.getPetIndex(pet)) != null) {
            this.getPet(this.getPetIndex(pet)).setSummoned(false);
            this.getPet(this.getPetIndex(pet)).saveToDb();
        }
        
        this.getClient().getWorldServer().unregisterPetHunger(this, getPetIndex(pet));
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        
        removePet(pet, shift_left);
        commitExcludedItems();
        
        client.announce(MaplePacketCreator.petStatUpdate(this));
        client.announce(MaplePacketCreator.enableActions());
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        prtLock.lock();
        try {
            updatePartyMemberHPInternal();
        } finally {
            prtLock.unlock();
        }
    }
    
    private void updatePartyMemberHPInternal() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getInstance().getWorld(world).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.client.announce(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, maxhp));
                    }
                }
            }
        }
    }

    public String getQuestInfo(int quest) {
        MapleQuestStatus qs = getQuest(MapleQuest.getInstance(quest));
        return qs.getInfo();
    }

    public void updateQuestInfo(int quest, String info) {
        MapleQuest q = MapleQuest.getInstance(quest);
        MapleQuestStatus qs = getQuest(q);
        qs.setInfo(info);

        synchronized (quests) {
            quests.put(q.getId(), qs);
        }

        announce(MaplePacketCreator.updateQuest(qs, false));
        if (qs.getQuest().getInfoNumber() > 0) {
            announce(MaplePacketCreator.updateQuest(qs, true));
        }
        announce(MaplePacketCreator.updateQuestInfo((short) qs.getQuest().getId(), qs.getNpc()));
    }

    private void fameGainByQuest() {
        int delta = quest_fame / ServerConstants.FAME_GAIN_BY_QUEST;
        if(delta > 0) {
            gainFame(delta);
            client.announce(MaplePacketCreator.getShowFameGain(delta));
        }
        
        quest_fame %= ServerConstants.FAME_GAIN_BY_QUEST;
    }
    
    public void updateQuest(MapleQuestStatus quest) {
        synchronized (quests) {
            quests.put(quest.getQuestID(), quest);
        }
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            announce(MaplePacketCreator.updateQuest(quest, false));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.updateQuest(quest, true));
            }
            announce(MaplePacketCreator.updateQuestInfo((short) quest.getQuest().getId(), quest.getNpc()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
            quest_fame += 1;
            if(ServerConstants.FAME_GAIN_BY_QUEST > 0)
                fameGainByQuest();
            
            announce(MaplePacketCreator.completeQuest((short) quest.getQuest().getId(), quest.getCompletionTime()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
            announce(MaplePacketCreator.updateQuest(quest, false));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.updateQuest(quest, true));
            }
        }
    }

    private void expireQuest(MapleQuest quest) {
        if(getQuestStatus(quest.getId()) == MapleQuestStatus.Status.COMPLETED.getId()) return;
        if(System.currentTimeMillis() < getMapleQuestStatus(quest.getId()).getExpirationTime()) return;
        
        announce(MaplePacketCreator.questExpire(quest.getId()));
        MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        newStatus.setForfeited(getQuest(quest).getForfeited() + 1);
        updateQuest(newStatus);
    }
    
    public void questTimeLimit(final MapleQuest quest, int seconds) {
        ScheduledFuture<?> sf = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                expireQuest(quest);
            }
        }, seconds * 1000);
        announce(MaplePacketCreator.addQuestTimeLimit(quest.getId(), seconds * 1000));
        timers.add(sf);
    }
    
    public void questTimeLimit2(final MapleQuest quest, long expires) {
        long timeLeft = expires - System.currentTimeMillis();
        
        if(timeLeft <= 0) {
            expireQuest(quest);
        } else {
            ScheduledFuture<?> sf = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    expireQuest(quest);
                }
            }, timeLeft);

            timers.add(sf);
        }
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        announce(MaplePacketCreator.updatePlayerStats(Collections.singletonList(new Pair<>(stat, Integer.valueOf(newval))), itemReaction, this));
    }

    public void announce(final byte[] packet) {
        client.announce(packet);
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (!this.isHidden() || client.getPlayer().gmLevel() > 1) {
            client.announce(MaplePacketCreator.spawnPlayerMapobject(this));
        }

        if (this.isHidden()) {
            List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT, 0));
            getMap().broadcastGMMessage(this, MaplePacketCreator.giveForeignBuff(getId(), dsstat), false);
        }
    }

    @Override
    public void setObjectId(int id) {
    }

    @Override
    public String toString() {
        return name;
    }

    public int getLinkedLevel() {
        return linkedLevel;
    }

    public String getLinkedName() {
        return linkedName;
    }

    public CashShop getCashShop() {
        return cashshop;
    }

    public void portalDelay(long delay) {
        this.portaldelay = System.currentTimeMillis() + delay;
    }

    public long portalDelay() {
        return portaldelay;
    }

    public void blockPortal(String scriptName) {
        if (!blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.add(scriptName);
            client.announce(MaplePacketCreator.enableActions());
        }
    }

    public void unblockPortal(String scriptName) {
        if (blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.remove(scriptName);
        }
    }

    public List<String> getBlockedPortals() {
        return blockedPortals;
    }

    public boolean containsAreaInfo(int area, String info) {
        Short area_ = Short.valueOf((short) area);
        if (area_info.containsKey(area_)) {
            return area_info.get(area_).contains(info);
        }
        return false;
    }

    public void updateAreaInfo(int area, String info) {
        area_info.put(Short.valueOf((short) area), info);
        announce(MaplePacketCreator.updateAreaInfo(area, info));
    }

    public String getAreaInfo(int area) {
        return area_info.get(Short.valueOf((short) area));
    }

    public Map<Short, String> getAreaInfos() {
        return area_info;
    }

    public void autoban(String reason) {
        this.ban(reason);
        announce(MaplePacketCreator.sendPolice(String.format("You have been blocked by the#b %s Police for HACK reason.#k", "Solaxia")));
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                client.disconnect(false, false);
            }
        }, 5000);
        Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, MapleCharacter.makeMapleReadable(this.name) + " was autobanned for " + reason));
    }

    public void block(int reason, int days, String desc) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?")) {
                ps.setString(1, desc);
                ps.setTimestamp(2, TS);
                ps.setInt(3, reason);
                ps.setInt(4, accountid);
                ps.executeUpdate();
            } finally {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBanned() {
        return isbanned;
    }

    public List<Integer> getTrockMaps() {
        return trockmaps;
    }

    public List<Integer> getVipTrockMaps() {
        return viptrockmaps;
    }

    public int getTrockSize() {
        int ret = trockmaps.indexOf(999999999);
        if (ret == -1) {
            ret = 5;
        }

        return ret;
    }

    public void deleteFromTrocks(int map) {
        trockmaps.remove(Integer.valueOf(map));
        while (trockmaps.size() < 10) {
            trockmaps.add(999999999);
        }
    }

    public void addTrockMap() {
        int index = trockmaps.indexOf(999999999);
        if (index != -1) {
            trockmaps.set(index, getMapId());
        }
    }

    public boolean isTrockMap(int id) {
        int index = trockmaps.indexOf(id);
        if (index != -1) {
            return true;
        }

        return false;
    }

    public int getVipTrockSize() {
        int ret = viptrockmaps.indexOf(999999999);

        if (ret == -1) {
            ret = 10;
        }

        return ret;
    }

    public void deleteFromVipTrocks(int map) {
        viptrockmaps.remove(Integer.valueOf(map));
        while (viptrockmaps.size() < 10) {
            viptrockmaps.add(999999999);
        }
    }

    public void addVipTrockMap() {
        int index = viptrockmaps.indexOf(999999999);
        if (index != -1) {
            viptrockmaps.set(index, getMapId());
        }
    }

    public boolean isVipTrockMap(int id) {
        int index = viptrockmaps.indexOf(id);
        if (index != -1) {
            return true;
        }

        return false;
    }
    //EVENTS
    private byte team = 0;
    private MapleFitness fitness;
    private MapleOla ola;
    private long snowballattack;

    public byte getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = (byte) team;
    }

    public MapleOla getOla() {
        return ola;
    }

    public void setOla(MapleOla ola) {
        this.ola = ola;
    }

    public MapleFitness getFitness() {
        return fitness;
    }

    public void setFitness(MapleFitness fit) {
        this.fitness = fit;
    }

    public long getLastSnowballAttack() {
        return snowballattack;
    }

    public void setLastSnowballAttack(long time) {
        this.snowballattack = time;
    }
    //Monster Carnival
    private int cp = 0;
    private int obtainedcp = 0;
    private MonsterCarnivalParty carnivalparty;
    private MonsterCarnival carnival;

    public MonsterCarnivalParty getCarnivalParty() {
        return carnivalparty;
    }

    public void setCarnivalParty(MonsterCarnivalParty party) {
        this.carnivalparty = party;
    }

    public MonsterCarnival getCarnival() {
        return carnival;
    }

    public void setCarnival(MonsterCarnival car) {
        this.carnival = car;
    }

    public int getCP() {
        return cp;
    }

    public int getObtainedCP() {
        return obtainedcp;
    }

    public void addCP(int cp) {
        this.cp += cp;
        this.obtainedcp += cp;
    }

    public void useCP(int cp) {
        this.cp -= cp;
    }

    public void setObtainedCP(int cp) {
        this.obtainedcp = cp;
    }

    public int getAndRemoveCP() {
        int rCP = 10;
        if (cp < 9) {
            rCP = cp;
            cp = 0;
        } else {
            cp -= 10;
        }

        return rCP;
    }

    public AutobanManager getAutobanManager() {
        return autoban;
    }

    public void equipPendantOfSpirit() {
        if (pendantOfSpirit == null) {
            pendantOfSpirit = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    if (pendantExp < 3) {
                        pendantExp++;
                        message("Pendant of the Spirit has been equipped for " + pendantExp + " hour(s), you will now receive " + pendantExp + "0% bonus exp.");
                    } else {
                        pendantOfSpirit.cancel(false);
                    }
                }
            }, 3600000); //1 hour
        }
    }

    public void unequipPendantOfSpirit() {
        if (pendantOfSpirit != null) {
            pendantOfSpirit.cancel(false);
            pendantOfSpirit = null;
        }
        pendantExp = 0;
    }

    public void increaseEquipExp(int expGain) {
        if(expGain < 0) expGain = Integer.MAX_VALUE;
        
        for (Item item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            Equip nEquip = (Equip) item;
            String itemName = ii.getName(nEquip.getItemId());
            if (itemName == null) {
                continue;
            }

            if ((nEquip.getItemLevel() < ServerConstants.USE_EQUIPMNT_LVLUP) || (itemName.contains("Reverse") && nEquip.getItemLevel() < 4) || (itemName.contains("Timeless") && nEquip.getItemLevel() < 6)) {
                nEquip.gainItemExp(client, expGain);
            }
        }
    }
    
    public void showAllEquipFeatures() {
        String showMsg = "";
        
        for (Item item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            Equip nEquip = (Equip) item;
            String itemName = ii.getName(nEquip.getItemId());
            if (itemName == null) {
                continue;
            }
            
            showMsg += nEquip.showEquipFeatures(client);
        }
        
        if(!showMsg.isEmpty()) {
            this.showHint("#ePLAYER EQUIPMENTS:#n\r\n\r\n" + showMsg);
        }
    }

    public Map<String, MapleEvents> getEvents() {
        return events;
    }

    public PartyQuest getPartyQuest() {
        return partyQuest;
    }

    public void setPartyQuest(PartyQuest pq) {
        this.partyQuest = pq;
    }

    public final void empty(final boolean remove) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
        if (beholderHealingSchedule != null) {
            beholderHealingSchedule.cancel(false);
        }
        if (beholderBuffSchedule != null) {
            beholderBuffSchedule.cancel(false);
        }
        if (berserkSchedule != null) {
            berserkSchedule.cancel(false);
        }
        if (recoveryTask != null) {
            recoveryTask.cancel(false);
        }
        if (extraRecoveryTask != null) {
            extraRecoveryTask.cancel(false);
        }
        
        unregisterChairBuff();
        cancelBuffExpireTask();
        cancelDiseaseExpireTask();
        cancelSkillCooldownTask();
        cancelExpirationTask();
        
        for (ScheduledFuture<?> sf : timers) {
            sf.cancel(false);
        }
        timers.clear();
        if (maplemount != null) {
            maplemount.empty();
            maplemount = null;
        }
        if (remove) {
            partyQuest = null;
            events = null;
            mpc = null;
            mgc = null;
            events = null;
            party = null;
            family = null;
            client = null;
            map = null;
            timers = null;
        }
    }

    public void logOff() {
        this.loggedIn = false;
    }

    public boolean isLoggedin() {
        return loggedIn;
    }

    public void setMapId(int mapid) {
        this.mapid = mapid;
    }

    public boolean getWhiteChat() {
    	return !isGM() ? false : whiteChat;
    }

    public void toggleWhiteChat() {
        whiteChat = !whiteChat;
    }

    public boolean canDropMeso() {
        if (System.currentTimeMillis() - lastMesoDrop >= 200 || lastMesoDrop == -1) { //About 200 meso drops a minute
            lastMesoDrop = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    // These need to be renamed, but I am too lazy right now to go through the scripts and rename them...
    public String getPartyQuestItems() {
        return dataString;
    }

    public boolean gotPartyQuestItem(String partyquestchar) {
        return dataString.contains(partyquestchar);
    }

    public void removePartyQuestItem(String letter) {
        if (gotPartyQuestItem(letter)) {
            dataString = dataString.substring(0, dataString.indexOf(letter)) + dataString.substring(dataString.indexOf(letter) + letter.length());
        }
    }

    public void setPartyQuestItemObtained(String partyquestchar) {
        if (!dataString.contains(partyquestchar)) {
            this.dataString += partyquestchar;
        }
    }

    public void createDragon() {
        dragon = new MapleDragon(this);
    }

    public MapleDragon getDragon() {
        return dragon;
    }

    public void setDragon(MapleDragon dragon) {
        this.dragon = dragon;
    }

    public int getRemainingSpSize() {
        int sp = 0;
        for (int i = 0; i < remainingSp.length; i++) {
            if (remainingSp[i] > 0) {
                sp++;
            }
        }
        return sp;
    }
    
    public long getJailExpirationTimeLeft() {
        return jailExpiration - System.currentTimeMillis();
    }
    
    private void setFutureJailExpiration(long time) {
        jailExpiration = System.currentTimeMillis() + time;
    }
    
    public void addJailExpirationTime(long time) {
        long timeLeft = getJailExpirationTimeLeft();

        if(timeLeft <= 0) setFutureJailExpiration(time);
        else setFutureJailExpiration(timeLeft + time);
    }
    
    public void removeJailExpirationTime() {
        jailExpiration = 0;
    }
}
