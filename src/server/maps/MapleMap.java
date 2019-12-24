/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server.maps;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import config.YamlConfig;
import constants.game.GameConstants;
import constants.inventory.ItemConstants;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.MonitoredWriteLock;
import net.server.audit.locks.factory.MonitoredReadLockFactory;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.server.audit.locks.factory.MonitoredWriteLockFactory;
import java.lang.ref.WeakReference;
import net.server.Server;
import net.server.coordinator.world.MapleMonsterAggroCoordinator;
import net.server.channel.Channel;
import net.server.services.type.ChannelServices;
import net.server.services.task.channel.FaceExpressionService;
import net.server.services.task.channel.MobMistService;
import net.server.services.task.channel.OverallService;
import net.server.world.MapleParty;
import net.server.world.World;
import scripting.map.MapScriptManager;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.TimerManager;
import server.events.gm.MapleCoconut;
import server.events.gm.MapleFitness;
import server.events.gm.MapleOla;
import server.events.gm.MapleOxQuiz;
import server.events.gm.MapleSnowball;
import server.life.MapleLifeFactory;
import server.life.MapleLifeFactory.selfDestruction;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.life.SpawnPoint;
import scripting.event.EventInstanceManager;
import server.life.MaplePlayerNPC;
import server.life.MonsterListener;
import server.partyquest.GuardianSpawnPoint;
import server.partyquest.MapleCarnivalFactory;
import server.partyquest.MapleCarnivalFactory.MCSkill;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

public class MapleMap {
    
    private static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.ITEM, MapleMapObjectType.NPC, MapleMapObjectType.MONSTER, MapleMapObjectType.DOOR, MapleMapObjectType.SUMMON, MapleMapObjectType.REACTOR);
    private static final Map<Integer, Pair<Integer, Integer>> dropBoundsCache = new HashMap<>(100);
    
    private Map<Integer, MapleMapObject> mapobjects = new LinkedHashMap<>();
    private Set<Integer> selfDestructives = new LinkedHashSet<>();
    private Collection<SpawnPoint> monsterSpawn = Collections.synchronizedList(new LinkedList<SpawnPoint>());
    private Collection<SpawnPoint> allMonsterSpawn = Collections.synchronizedList(new LinkedList<SpawnPoint>());
    private AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private AtomicInteger droppedItemCount = new AtomicInteger(0);
    private Collection<MapleCharacter> characters = new LinkedHashSet<>();
    private Map<Integer, Set<Integer>> mapParty = new LinkedHashMap<>();
    private Map<Integer, MaplePortal> portals = new HashMap<>();
    private Map<Integer, Integer> backgroundTypes = new HashMap<>();
    private Map<String, Integer> environment = new LinkedHashMap<>();
    private Map<MapleMapItem, Long> droppedItems = new LinkedHashMap<>();
    private LinkedList<WeakReference<MapleMapObject>> registeredDrops = new LinkedList<>();
    private Map<MobLootEntry, Long> mobLootEntries = new HashMap(20);
    private List<Runnable> statUpdateRunnables = new ArrayList(50);
    private List<Rectangle> areas = new ArrayList<>();
    private MapleFootholdTree footholds = null;
    private Pair<Integer, Integer> xLimits;  // caches the min and max x's with available footholds
    private Rectangle mapArea = new Rectangle();
    private int mapid;
    private AtomicInteger runningOid = new AtomicInteger(1000000001);
    private int returnMapId;
    private int channel, world;
    private int seats;
    private byte monsterRate;
    private boolean clock;
    private boolean boat;
    private boolean docked = false;
    private EventInstanceManager event = null;
    private String mapName;
    private String streetName;
    private MapleMapEffect mapEffect = null;
    private boolean everlast = false;
    private int forcedReturnMap = 999999999;
    private int timeLimit;
    private long mapTimer;
    private int decHP = 0;
    private float recovery = 1.0f;
    private int protectItem = 0;
    private boolean town;
    private MapleOxQuiz ox;
    private boolean isOxQuiz = false;
    private boolean dropsOn = true;
    private String onFirstUserEnter;
    private String onUserEnter;
    private int fieldType;
    private int fieldLimit = 0;
    private int mobCapacity = -1;
    private MapleMonsterAggroCoordinator aggroMonitor = null;   // aggroMonitor activity in sync with itemMonitor
    private ScheduledFuture<?> itemMonitor = null;
    private ScheduledFuture<?> expireItemsTask = null;
    private ScheduledFuture<?> mobSpawnLootTask = null;
    private ScheduledFuture<?> characterStatUpdateTask = null;
    private short itemMonitorTimeout;
    private Pair<Integer, String> timeMob = null;
    private short mobInterval = 5000;
    private boolean allowSummons = true; // All maps should have this true at the beginning
    private MapleCharacter mapOwner = null;
    private long mapOwnerLastActivityTime = Long.MAX_VALUE;
    
    // events
    private boolean eventstarted = false, isMuted = false;
    private MapleSnowball snowball0 = null;
    private MapleSnowball snowball1 = null;
    private MapleCoconut coconut;
    
    //CPQ
    private int maxMobs;
    private int maxReactors;
    private int deathCP;
    private int timeDefault;
    private int timeExpand;

    //locks
    private MonitoredReadLock chrRLock;
    private MonitoredWriteLock chrWLock;
    private MonitoredReadLock objectRLock;
    private MonitoredWriteLock objectWLock;

    private Lock lootLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MAP_LOOT, true);
    
    // due to the nature of loadMapFromWz (synchronized), sole function that calls 'generateMapDropRangeCache', this lock remains optional.
    private static final Lock bndLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MAP_BOUNDS, true);
    
    public MapleMap(int mapid, int world, int channel, int returnMapId, float monsterRate) {
        this.mapid = mapid;
        this.channel = channel;
        this.world = world;
        this.returnMapId = returnMapId;
        this.monsterRate = (byte) Math.ceil(monsterRate);
        if (this.monsterRate == 0) {
            this.monsterRate = 1;
        }
        final MonitoredReentrantReadWriteLock chrLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_CHRS, true);
        chrRLock = MonitoredReadLockFactory.createLock(chrLock);
        chrWLock = MonitoredWriteLockFactory.createLock(chrLock);

        final MonitoredReentrantReadWriteLock objectLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_OBJS, true);
        objectRLock = MonitoredReadLockFactory.createLock(objectLock);
        objectWLock = MonitoredWriteLockFactory.createLock(objectLock);
        
        aggroMonitor = new MapleMonsterAggroCoordinator();
    }
    
    public void setEventInstance(EventInstanceManager eim) {
        event = eim;
    }
    
    public EventInstanceManager getEventInstance() {
        return event;
    }
    
    public Rectangle getMapArea() {
        return mapArea;
    }
    
    public int getWorld() {
        return world;
    }
    
    public void broadcastMessage(MapleCharacter source, final byte[] packet) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    chr.getClient().announce(packet);
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }

    public void broadcastGMMessage(MapleCharacter source, final byte[] packet) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source && (chr.gmLevel() >= source.gmLevel())) {
                    chr.getClient().announce(packet);
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }

    public void toggleDrops() {
        this.dropsOn = !dropsOn;
    }
    
    
    private static double getRangedDistance() {
        return(YamlConfig.config.server.USE_MAXRANGE ? Double.POSITIVE_INFINITY : 722500);
    }

    public List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> types) {
        objectRLock.lock();
        final List<MapleMapObject> ret = new LinkedList<>();
        try {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
        } finally {
            objectRLock.unlock();
        }
        return ret;
    }

    public int getId() {
        return mapid;
    }
    
    public Channel getChannelServer() {
        return Server.getInstance().getWorld(world).getChannel(channel);
    }
    
    public World getWorldServer() {
        return Server.getInstance().getWorld(world);
    }

    public MapleMap getReturnMap() {
        if (returnMapId == 999999999) {
            return this;
        }
        return getChannelServer().getMapFactory().getMap(returnMapId);
    }

    public int getReturnMapId() {
        return returnMapId;
    }
    
    public MapleMap getForcedReturnMap() {
        return getChannelServer().getMapFactory().getMap(forcedReturnMap);
    }
    
    public int getForcedReturnId() {
        return forcedReturnMap;
    }

    public void setForcedReturnMap(int map) {
        this.forcedReturnMap = map;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTimeLeft() {
        return (int) ((mapTimer - System.currentTimeMillis()) / 1000);
    }
    
    public void setReactorState() {
        for (MapleMapObject o : getMapObjects()) {
            if (o.getType() == MapleMapObjectType.REACTOR) {
                if (((MapleReactor) o).getState() < 1) {
                    MapleReactor mr = (MapleReactor) o;
                    mr.lockReactor();
                    try {
                        mr.resetReactorActions(1);
                        broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 1));
                    } finally {
                        mr.unlockReactor();
                    }
                }
            }
        }
    }
    
    public final void limitReactor(final int rid, final int num) {
        List<MapleReactor> toDestroy = new ArrayList<>();
        Map<Integer, Integer> contained = new LinkedHashMap<>();
        
        for (MapleMapObject obj : getReactors()) {
            MapleReactor mr = (MapleReactor) obj;
            if (contained.containsKey(mr.getId())) {
                if (contained.get(mr.getId()) >= num) {
                    toDestroy.add(mr);
                } else {
                    contained.put(mr.getId(), contained.get(mr.getId()) + 1);
                }
            } else {
                contained.put(mr.getId(), 1);
            }
        }
        
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
        }
    }

    public boolean isAllReactorState(final int reactorId, final int state) {
        for (MapleMapObject mo : getReactors()) {
            MapleReactor r = (MapleReactor) mo;
            
            if (r.getId() == reactorId && r.getState() != state) {
                return false;
            }
        }
        return true;
    }
    
    public int getCurrentPartyId() {
        for (MapleCharacter chr : this.getCharacters()) {
            if (chr.getPartyId() != -1) {
                return chr.getPartyId();
            }
        }
        return -1;
    }

    public void addPlayerNPCMapObject(MaplePlayerNPC pnpcobject) {
        objectWLock.lock();
        try {
            this.mapobjects.put(pnpcobject.getObjectId(), pnpcobject);
        } finally {
            objectWLock.unlock();
        }
    }
    
    public void addMapObject(MapleMapObject mapobject) {
        int curOID = getUsableOID();
        
        objectWLock.lock();
        try {
            mapobject.setObjectId(curOID);
            this.mapobjects.put(curOID, mapobject);
        } finally {
            objectWLock.unlock();
        }
    }
    
    public void addSelfDestructive(MapleMonster mob) {
        if (mob.getStats().selfDestruction() != null) {
            this.selfDestructives.add(mob.getObjectId());
        }
    }
    
    public boolean removeSelfDestructive(int mapobjectid) {
        return this.selfDestructives.remove(mapobjectid);
    }
    
    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery) {
        spawnAndAddRangedMapObject(mapobject, packetbakery, null);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) {
        List<MapleCharacter> inRangeCharacters = new LinkedList<>();
        int curOID = getUsableOID();
        
        chrRLock.lock();
        objectWLock.lock();
        try {
            mapobject.setObjectId(curOID);
            this.mapobjects.put(curOID, mapobject);
            for (MapleCharacter chr : characters) {
                if (condition == null || condition.canSpawn(chr)) {
                    if (chr.getPosition().distanceSq(mapobject.getPosition()) <= getRangedDistance()) {
                        inRangeCharacters.add(chr);
                        chr.addVisibleMapObject(mapobject);
                    }
                }
            }
        } finally {
            objectWLock.unlock();
            chrRLock.unlock();
        }
        
        for (MapleCharacter chr : inRangeCharacters) {
            packetbakery.sendPackets(chr.getClient());
        }
    }
    
    private void spawnRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) {
        List<MapleCharacter> inRangeCharacters = new LinkedList<>();
        
        chrRLock.lock();
        try {
            int curOID = getUsableOID();
            mapobject.setObjectId(curOID);
            for (MapleCharacter chr : characters) {
                if (condition == null || condition.canSpawn(chr)) {
                    if (chr.getPosition().distanceSq(mapobject.getPosition()) <= getRangedDistance()) {
                        inRangeCharacters.add(chr);
                        chr.addVisibleMapObject(mapobject);
                    }
                }
            }
        } finally {
            chrRLock.unlock();
        }
        
        for (MapleCharacter chr : inRangeCharacters) {
            packetbakery.sendPackets(chr.getClient());
        }
    }

    private int getUsableOID() {
        objectRLock.lock();
        try {
            Integer curOid;
            
            // clashes with playernpc on curOid >= 2147000000, developernpc uses >= 2147483000
            do {
                if ((curOid = runningOid.incrementAndGet()) >= 2147000000) {
                    runningOid.set(curOid = 1000000001);
                }
            } while (mapobjects.containsKey(curOid));
            
            return curOid;
        } finally {
            objectRLock.unlock();
        }
    }

    public void removeMapObject(int num) {
        objectWLock.lock();
        try {
            this.mapobjects.remove(Integer.valueOf(num));
        } finally {
            objectWLock.unlock();
        }
    }

    public void removeMapObject(final MapleMapObject obj) {
        removeMapObject(obj.getObjectId());
    }

    private Point calcPointBelow(Point initial) {
        MapleFoothold fh = footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    }

    public void generateMapDropRangeCache() {
        bndLock.lock();
        try {
            Pair<Integer, Integer> bounds = dropBoundsCache.get(mapid);
            
            if(bounds != null) {
                xLimits = bounds;
            } else {
                // assuming MINIMAP always have an equal-greater picture representation of the map area (players won't walk beyond the area known by the minimap).
                Point lp = new Point(mapArea.x, mapArea.y), rp = new Point(mapArea.x + mapArea.width, mapArea.y), fallback = new Point(mapArea.x + (mapArea.width / 2), mapArea.y);

                lp = bsearchDropPos(lp, fallback);  // approximated leftmost fh node position
                rp = bsearchDropPos(rp, fallback);  // approximated rightmost fh node position

                xLimits = new Pair<>(lp.x + 14, rp.x - 14);
                dropBoundsCache.put(mapid, xLimits);
            }
        } finally {
            bndLock.unlock();
        }
    }
    
    private Point bsearchDropPos(Point initial, Point fallback) {
        Point res, dropPos = null;
                
        int awayx = fallback.x;
        int homex = initial.x;
                
        int y = initial.y - 85;
        
        do {
            int distx = awayx - homex;
            int dx = distx / 2;
            
            int searchx = homex + dx;
            if((res = calcPointBelow(new Point(searchx, y))) != null) {
                awayx = searchx;
                dropPos = res;
            } else {
                homex = searchx;
            }
        } while(Math.abs(homex - awayx) > 5);
        
        return (dropPos != null) ? dropPos : fallback;
    }
    
    public Point calcDropPos(Point initial, Point fallback) {
        if (initial.x < xLimits.left) {
            initial.x = xLimits.left;
        } else if(initial.x > xLimits.right) {
            initial.x = xLimits.right;
        }
        
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 85));   // actual drop ranges: default - 120, explosive - 360
        if (ret == null) {
            ret = bsearchDropPos(initial, fallback);
        }
        
        if(!mapArea.contains(ret)) { // found drop pos outside the map :O
            return fallback;
        }
        
        return ret;
    }
    
    public boolean canDeployDoor(Point pos) {
        Point toStep = calcPointBelow(pos);
        return toStep != null && toStep.distance(pos) <= 42;
    }
    
    /**
     * Fetches angle relative between spawn and door points where 3 O'Clock is 0
     * and 12 O'Clock is 270 degrees
     * 
     * @param spawnPoint
     * @param doorPoint
     * @return angle in degress from 0-360.
     */
    private static double getAngle(Point doorPoint, Point spawnPoint) {
        double dx = doorPoint.getX() - spawnPoint.getX();
        // Minus to correct for coord re-mapping
        double dy = -(doorPoint.getY() - spawnPoint.getY());

        double inRads = Math.atan2(dy, dx);

        // We need to map to coord system when 0 degree is at 3 O'clock, 270 at 12 O'clock
        if (inRads < 0) {
            inRads = Math.abs(inRads);
        } else {
            inRads = 2 * Math.PI - inRads;
        }

        return Math.toDegrees(inRads);
    }
    
    /**
     * Converts angle in degrees to rounded cardinal coordinate.
     * 
     * @param angle
     * @return correspondent coordinate.
     */
    public static String getRoundedCoordinate(double angle) {
        String directions[] = {"E", "SE", "S", "SW", "W", "NW", "N", "NE", "E"};
        return directions[ (int)Math.round((  ((double)angle % 360) / 45)) ];
    }
    
    public Pair<String, Integer> getDoorPositionStatus(Point pos) {
        MaplePortal portal = findClosestPlayerSpawnpoint(pos);
        
        double angle = getAngle(portal.getPosition(), pos);
        double distn = pos.distanceSq(portal.getPosition());
        
        if(distn <= 777777.7) {
            return null;
        }
        
        distn = Math.sqrt(distn);
        return new Pair<>(getRoundedCoordinate(angle), Integer.valueOf((int)distn));
    }

    private static void sortDropEntries(List<MonsterDropEntry> from, List<MonsterDropEntry> item, List<MonsterDropEntry> visibleQuest, List<MonsterDropEntry> otherQuest, MapleCharacter chr) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        
        for(MonsterDropEntry mde : from) {
            if(!ii.isQuestItem(mde.itemId)) {
                item.add(mde);
            } else {
                if(chr.needQuestItem(mde.questid, mde.itemId)) {
                    visibleQuest.add(mde);
                } else {
                    otherQuest.add(mde);
                }
            }
        }
    }
    
    private byte dropItemsFromMonsterOnMap(List<MonsterDropEntry> dropEntry, Point pos, byte d, int chRate, byte droptype, int mobpos, MapleCharacter chr, MapleMonster mob) {
        if(dropEntry.isEmpty()) {
            return d;
        }
        
        Collections.shuffle(dropEntry);
        
        Item idrop;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        
        for (final MonsterDropEntry de : dropEntry) {
            float cardRate = chr.getCardRate(de.itemId);
            int dropChance = (int) Math.min((float) de.chance * chRate * cardRate, Integer.MAX_VALUE);
            
            if (Randomizer.nextInt(999999) < dropChance) {
                if (droptype == 3) {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (40 * ((d + 1) / 2)) : -(40 * (d / 2))));
                } else {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * ((d + 1) / 2)) : -(25 * (d / 2))));
                }
                if (de.itemId == 0) { // meso
                    int mesos = Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum;

                    if (mesos > 0) {
                        if (chr.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                            mesos = (int) (mesos * chr.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                        }
                        mesos = mesos * chr.getMesoRate();
                        if (mesos <= 0) {
                            mesos = Integer.MAX_VALUE;
                        }
                        
                        spawnMesoDrop(mesos, calcDropPos(pos, mob.getPosition()), mob, chr, false, droptype);
                    }
                } else {
                    if (ItemConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        idrop = new Item(de.itemId, (short) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1));
                    }
                    spawnDrop(idrop, calcDropPos(pos, mob.getPosition()), mob, chr, droptype, de.questid);
                }
                d++;
            }
        }
        
        return d;
    }
    
    private byte dropGlobalItemsFromMonsterOnMap(List<MonsterGlobalDropEntry> globalEntry, Point pos, byte d, byte droptype, int mobpos, MapleCharacter chr, MapleMonster mob) {
        Collections.shuffle(globalEntry);
        
        Item idrop;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        
        for (final MonsterGlobalDropEntry de : globalEntry) {
            if (Randomizer.nextInt(999999) < de.chance) {
                if (droptype == 3) {
                    pos.x = (int) (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                } else {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                }
                if (de.itemId != 0) {
                    if (ItemConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        idrop = new Item(de.itemId, (short) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1));
                    }
                    spawnDrop(idrop, calcDropPos(pos, mob.getPosition()), mob, chr, droptype, de.questid);
                    d++;
                }
            }
        }
        
        return d;
    }
    
    private void dropFromMonster(final MapleCharacter chr, final MapleMonster mob, final boolean useBaseRate) {
        if (mob.dropsDisabled() || !dropsOn) {
            return;
        }
        
        final byte droptype = (byte) (mob.getStats().isExplosiveReward() ? 3 : mob.getStats().isFfaLoot() ? 2 : chr.getParty() != null ? 1 : 0);
        final int mobpos = mob.getPosition().x;
        int chRate = !mob.isBoss() ? chr.getDropRate() : chr.getBossDropRate();
        byte d = 1;
        Point pos = new Point(0, mob.getPosition().y);

        MonsterStatusEffect stati = mob.getStati(MonsterStatus.SHOWDOWN);
        if (stati != null) {
            chRate *= (stati.getStati().get(MonsterStatus.SHOWDOWN).doubleValue() / 100.0 + 1.0);
        }
        
        if (useBaseRate) {
            chRate = 1;
        }

        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        final List<MonsterGlobalDropEntry> globalEntry = mi.getRelevantGlobalDrops(this.getId());
        
        final List<MonsterDropEntry>  dropEntry = new ArrayList<>();
        final List<MonsterDropEntry> visibleQuestEntry = new ArrayList<>();
        final List<MonsterDropEntry> otherQuestEntry = new ArrayList<>();
        
        List<MonsterDropEntry> lootEntry = YamlConfig.config.server.USE_SPAWN_RELEVANT_LOOT ? mob.retrieveRelevantDrops() : mi.retrieveEffectiveDrop(mob.getId());
        sortDropEntries(lootEntry, dropEntry, visibleQuestEntry, otherQuestEntry, chr);     // thanks Articuno, Limit, Rohenn for noticing quest loots not showing up in only-quest item drops scenario
        
        if (lootEntry.isEmpty()) {   // thanks resinate
            return;
        }
        
        registerMobItemDrops(droptype, mobpos, chRate, pos, dropEntry, visibleQuestEntry, otherQuestEntry, globalEntry, chr, mob);
    }
    
    public void dropItemsFromMonster(List<MonsterDropEntry> list, final MapleCharacter chr, final MapleMonster mob) {
        if (mob.dropsDisabled() || !dropsOn) {
            return;
        }
        
        final byte droptype = (byte) (chr.getParty() != null ? 1 : 0);
        final int mobpos = mob.getPosition().x;
        int chRate = 1000000;   // guaranteed item drop
        byte d = 1;
        Point pos = new Point(0, mob.getPosition().y);
        
        dropItemsFromMonsterOnMap(list, pos, d, chRate, droptype, mobpos, chr, mob);
    }
    
    public void dropFromFriendlyMonster(final MapleCharacter chr, final MapleMonster mob) {
        dropFromMonster(chr, mob, true);
    }
    
    public void dropFromReactor(final MapleCharacter chr, final MapleReactor reactor, Item drop, Point dropPos, short questid) {
        spawnDrop(drop, this.calcDropPos(dropPos, reactor.getPosition()), reactor, chr, (byte)(chr.getParty() != null ? 1 : 0), questid);
    }

    private void stopItemMonitor() {
        itemMonitor.cancel(false);
        itemMonitor = null;

        expireItemsTask.cancel(false);
        expireItemsTask = null;

        if(YamlConfig.config.server.USE_SPAWN_LOOT_ON_ANIMATION) {
            mobSpawnLootTask.cancel(false);
            mobSpawnLootTask = null;
        }
        
        characterStatUpdateTask.cancel(false);
        characterStatUpdateTask = null;
    }
    
    private void cleanItemMonitor() {
        objectWLock.lock();
        try {
            registeredDrops.removeAll(Collections.singleton(null));
        } finally {
            objectWLock.unlock();
        }
    }
    
    private void startItemMonitor() {
        chrWLock.lock();
        try {
            if (itemMonitor != null) {
                return;
            }
            
            itemMonitor = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    chrWLock.lock();
                    try {
                        if (characters.isEmpty()) {
                            if(itemMonitorTimeout == 0) {
                                if(itemMonitor != null) {
                                    stopItemMonitor();
                                    aggroMonitor.stopAggroCoordinator();
                                }
                                
                                return;
                            } else {
                                itemMonitorTimeout--;
                            }
                        } else {
                            itemMonitorTimeout = 1;
                        }
                    } finally {
                        chrWLock.unlock();
                    }
                    
                    boolean tryClean;
                    objectRLock.lock();
                    try {
                        tryClean = registeredDrops.size() > 70;
                    } finally {
                        objectRLock.unlock();
                    }
                    
                    if (tryClean) {
                        cleanItemMonitor();
                    }
                }
            }, YamlConfig.config.server.ITEM_MONITOR_TIME, YamlConfig.config.server.ITEM_MONITOR_TIME);
            
            expireItemsTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    makeDisappearExpiredItemDrops();
                }
            }, YamlConfig.config.server.ITEM_EXPIRE_CHECK, YamlConfig.config.server.ITEM_EXPIRE_CHECK);
            
            if(YamlConfig.config.server.USE_SPAWN_LOOT_ON_ANIMATION) {
                lootLock.lock();
                try {
                    mobLootEntries.clear();
                } finally {
                    lootLock.unlock();
                }

                mobSpawnLootTask = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        spawnMobItemDrops();
                    }
                }, 200, 200);
            }
            
            characterStatUpdateTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    runCharacterStatUpdate();
                }
            }, 200, 200);
                    
            itemMonitorTimeout = 1;
        } finally {
            chrWLock.unlock();
        }
    }
    
    private boolean hasItemMonitor() {
        chrRLock.lock();
        try {
            return itemMonitor != null;
        } finally {
            chrRLock.unlock();
        }
    }
    
    public int getDroppedItemCount() {
        return droppedItemCount.get();
    }
    
    private void instantiateItemDrop(MapleMapItem mdrop) {
        if(droppedItemCount.get() >= YamlConfig.config.server.ITEM_LIMIT_ON_MAP) {
            MapleMapObject mapobj;
            
            do {
                mapobj = null;
                
                objectWLock.lock();
                try {
                    while (mapobj == null) {
                        if (registeredDrops.isEmpty()) {
                            break;
                        }
                        mapobj = registeredDrops.remove(0).get();
                    }
                } finally {
                    objectWLock.unlock();
                }
            } while (!makeDisappearItemFromMap(mapobj));
        }
        
        objectWLock.lock();
        try {
            registerItemDrop(mdrop);
            registeredDrops.add(new WeakReference<>((MapleMapObject) mdrop));
        } finally {
            objectWLock.unlock();
        }
        
        droppedItemCount.incrementAndGet();
    }
    
    private void registerItemDrop(MapleMapItem mdrop) {
        droppedItems.put(mdrop, !everlast ? Server.getInstance().getCurrentTime() + YamlConfig.config.server.ITEM_EXPIRE_TIME : Long.MAX_VALUE);
    }
    
    private void unregisterItemDrop(MapleMapItem mdrop) {
        objectWLock.lock();
        try {
            droppedItems.remove(mdrop);
        } finally {
            objectWLock.unlock();
        }
    }
    
    private void makeDisappearExpiredItemDrops() {
        List<MapleMapItem> toDisappear = new LinkedList<>();
        
        objectRLock.lock();
        try {
            long timeNow = Server.getInstance().getCurrentTime();
            
            for(Entry<MapleMapItem, Long> it : droppedItems.entrySet()) {
                if(it.getValue() < timeNow) {
                    toDisappear.add(it.getKey());
                }
            }
        } finally {
            objectRLock.unlock();
        }
        
        for(MapleMapItem mmi : toDisappear) {
            makeDisappearItemFromMap(mmi);
        }
        
        objectWLock.lock();
        try {
            for(MapleMapItem mmi : toDisappear) {
                droppedItems.remove(mmi);
            }
        } finally {
            objectWLock.unlock();
        }
    }
    
    private void registerMobItemDrops(byte droptype, int mobpos, int chRate, Point pos, List<MonsterDropEntry> dropEntry, List<MonsterDropEntry> visibleQuestEntry, List<MonsterDropEntry> otherQuestEntry, List<MonsterGlobalDropEntry> globalEntry, MapleCharacter chr, MapleMonster mob) {
        MobLootEntry mle = new MobLootEntry(droptype, mobpos, chRate, pos, dropEntry, visibleQuestEntry, otherQuestEntry, globalEntry, chr, mob);
        
        if(YamlConfig.config.server.USE_SPAWN_LOOT_ON_ANIMATION) {
            int animationTime = mob.getAnimationTime("die1");

            lootLock.lock();
            try {
                long timeNow = Server.getInstance().getCurrentTime();
                mobLootEntries.put(mle, timeNow + ((long)(0.42 * animationTime)));
            } finally {
                lootLock.unlock();
            }
        } else {
            mle.run();
        }
    }
    
    private void spawnMobItemDrops() {
        Set<Entry<MobLootEntry, Long>> mleList;
        
        lootLock.lock();
        try {
            mleList = new HashSet<>(mobLootEntries.entrySet());
        } finally {
            lootLock.unlock();
        }
        
        long timeNow = Server.getInstance().getCurrentTime();
        List<MobLootEntry> toRemove = new LinkedList<>();
        for(Entry<MobLootEntry, Long> mlee : mleList) {
            if(mlee.getValue() < timeNow) {
                toRemove.add(mlee.getKey());
            }
        }
        
        if(!toRemove.isEmpty()) {
            List<MobLootEntry> toSpawnLoot = new LinkedList<>();
            
            lootLock.lock();
            try {
                for(MobLootEntry mle : toRemove) {
                    Long mler = mobLootEntries.remove(mle);
                    if(mler != null) {
                        toSpawnLoot.add(mle);
                    }
                }
            } finally {
                lootLock.unlock();
            }
            
            for(MobLootEntry mle : toSpawnLoot) {
                mle.run();
            }
        }
    }
    
    private List<MapleMapItem> getDroppedItems() {
        objectRLock.lock();
        try {
            return new LinkedList<>(droppedItems.keySet());
        } finally {
            objectRLock.unlock();
        }
    }
    
    public int getDroppedItemsCountById(int itemid) {
        int count = 0;
        for (MapleMapItem mmi : getDroppedItems()) {
            if (mmi.getItemId() == itemid) {
                count++;
            }
        }
        
        return count;
    }
    
    public void pickItemDrop(byte[] pickupPacket, MapleMapItem mdrop) { // mdrop must be already locked and not-pickedup checked at this point
        broadcastMessage(pickupPacket, mdrop.getPosition());
        
        droppedItemCount.decrementAndGet();
        this.removeMapObject(mdrop);
        mdrop.setPickedUp(true);
        unregisterItemDrop(mdrop);
    }
    
    public List<MapleMapItem> updatePlayerItemDropsToParty(int partyid, int charid, List<MapleCharacter> partyMembers, MapleCharacter partyLeaver) {
        List<MapleMapItem> partyDrops = new LinkedList<>();
        
        for (MapleMapItem mdrop : getDroppedItems()) {
            if (mdrop.getOwnerId() == charid) {
                mdrop.lockItem();
                try {
                    if (mdrop.isPickedUp()) {
                        continue;
                    }
                    
                    mdrop.setPartyOwnerId(partyid);
                    
                    byte[] removePacket = MaplePacketCreator.silentRemoveItemFromMap(mdrop.getObjectId());
                    byte[] updatePacket = MaplePacketCreator.updateMapItemObject(mdrop, partyLeaver == null);
                    
                    for (MapleCharacter mc : partyMembers) {
                        if (this.equals(mc.getMap())) {
                            mc.announce(removePacket);
                            
                            if (mc.needQuestItem(mdrop.getQuest(), mdrop.getItemId())) {
                                mc.announce(updatePacket);
                            }
                        }
                    }
                    
                    if (partyLeaver != null) {
                        if (this.equals(partyLeaver.getMap())) {
                            partyLeaver.announce(removePacket);
                            
                            if (partyLeaver.needQuestItem(mdrop.getQuest(), mdrop.getItemId())) {
                                partyLeaver.announce(MaplePacketCreator.updateMapItemObject(mdrop, true));
                            }
                        }
                    }
                } finally {
                    mdrop.unlockItem();
                }
            } else if (partyid != -1 && mdrop.getPartyOwnerId() == partyid) {
                partyDrops.add(mdrop);
            }
        }
        
        return partyDrops;
    }
    
    public void updatePartyItemDropsToNewcomer(MapleCharacter newcomer, List<MapleMapItem> partyItems) {
        for (MapleMapItem mdrop : partyItems) {
            mdrop.lockItem();
            try {
                if (mdrop.isPickedUp()) {
                    continue;
                }

                byte[] removePacket = MaplePacketCreator.silentRemoveItemFromMap(mdrop.getObjectId());
                byte[] updatePacket = MaplePacketCreator.updateMapItemObject(mdrop, true);

                if (newcomer != null) {
                    if (this.equals(newcomer.getMap())) {
                        newcomer.announce(removePacket);

                        if (newcomer.needQuestItem(mdrop.getQuest(), mdrop.getItemId())) {
                            newcomer.announce(updatePacket);
                        }
                    }
                }
            } finally {
                mdrop.unlockItem();
            }
        }
    }
    
    private void spawnDrop(final Item idrop, final Point dropPos, final MapleMapObject dropper, final MapleCharacter chr, final byte droptype, final short questid) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, dropper, chr, chr.getClient(), droptype, false, questid);
        mdrop.setDropTime(Server.getInstance().getCurrentTime());
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                MapleCharacter chr = c.getPlayer();
                
                if (chr.needQuestItem(questid, idrop.getItemId())) {
                    mdrop.lockItem();
                    try {
                        c.announce(MaplePacketCreator.dropItemFromMapObject(chr, mdrop, dropper.getPosition(), dropPos, (byte) 1));
                    } finally {
                        mdrop.unlockItem();
                    }
                }
            }
        }, null);

        instantiateItemDrop(mdrop);
        activateItemReactors(mdrop, chr.getClient());
    }

    public final void spawnMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, owner.getClient(), droptype, playerDrop);
        mdrop.setDropTime(Server.getInstance().getCurrentTime());

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                mdrop.lockItem();
                try {
                    c.announce(MaplePacketCreator.dropItemFromMapObject(c.getPlayer(), mdrop, dropper.getPosition(), droppos, (byte) 1));
                } finally {
                    mdrop.unlockItem();
                }
            }
        }, null);

        instantiateItemDrop(mdrop);
    }

    public final void disappearingItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem mdrop = new MapleMapItem(item, droppos, dropper, owner, owner.getClient(), (byte) 1, false);
        
        mdrop.lockItem();
        try {
            broadcastItemDropMessage(mdrop, dropper.getPosition(), droppos, (byte) 3, mdrop.getPosition());
        } finally {
            mdrop.unlockItem();
        }
    }
    
    public final void disappearingMesoDrop(final int meso, final MapleMapObject dropper, final MapleCharacter owner, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, owner.getClient(), (byte) 1, false);
        
        mdrop.lockItem();
        try {
            broadcastItemDropMessage(mdrop, dropper.getPosition(), droppos, (byte) 3, mdrop.getPosition());
        } finally {
            mdrop.unlockItem();
        }
    }

    public MapleMonster getMonsterById(int id) {
        objectRLock.lock();
        try {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.MONSTER) {
                    if (((MapleMonster) obj).getId() == id) {
                        return (MapleMonster) obj;
                    }
                }
            }
        } finally {
            objectRLock.unlock();
        }
        return null;
    }

    public int countMonster(int id) {
        return countMonster(id, id);
    }
    
    public int countMonster(int minid, int maxid) {
        int count = 0;
        for (MapleMapObject m : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster mob = (MapleMonster) m;
            if (mob.getId() >= minid && mob.getId() <= maxid) {
                count++;
            }
        }
        return count;
    }
    
    public int countMonsters() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER)).size();
    }
    
    public int countReactors() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR)).size();
    }
    
    public final List<MapleMapObject> getReactors() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
    }
    
    public final List<MapleMapObject> getMonsters() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
    }
    
    public final List<MapleReactor> getAllReactors() {
        List<MapleReactor> list = new LinkedList<>();
        for (MapleMapObject mmo : getReactors()) {
            list.add((MapleReactor) mmo);
        }
        
        return list;
    }
    
    public final List<MapleMonster> getAllMonsters() {
        List<MapleMonster> list = new LinkedList<>();
        for (MapleMapObject mmo : getMonsters()) {
            list.add((MapleMonster) mmo);
        }
        
        return list;
    }
    
    public int countItems() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM)).size();
    }
    
    public final List<MapleMapObject> getItems() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
    }

    public int countPlayers() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER)).size();
    }
    
    public List<MapleMapObject> getPlayers() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
    }
    
    public List<MapleCharacter> getAllPlayers() {
        List<MapleCharacter> character;
        chrRLock.lock();
        try {
            character = new ArrayList<>(characters);
        } finally {
            chrRLock.unlock();
        }
        
        return character;
    }
    
    public Map<Integer, MapleCharacter> getMapAllPlayers() {
        Map<Integer, MapleCharacter> pchars = new HashMap<>();
        for (MapleCharacter chr : this.getAllPlayers()) {
            pchars.put(chr.getId(), chr);
        }
        
        return pchars;
    }
    
    public List<MapleCharacter> getPlayersInRange(Rectangle box) {
        List<MapleCharacter> character = new LinkedList<>();
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                if (box.contains(chr.getPosition())) {
                    character.add(chr);
                }
            }
        } finally {
            chrRLock.unlock();
        }
        
        return character;
    }
    
    public int countAlivePlayers() {
        int count = 0;
        
        for(MapleCharacter mc: getAllPlayers()) {
            if (mc.isAlive()) {
                count++;
            }
        }
        
        return count;
    }
    
    public int countBosses() {
        int count = 0;
        
        for(MapleMonster mob: getAllMonsters()) {
            if (mob.isBoss()) {
                count++;
            }
        }
        
        return count;
    }
    
    public boolean damageMonster(final MapleCharacter chr, final MapleMonster monster, final int damage) {
        if (monster.getId() == 8800000) {
            for (MapleMapObject object : chr.getMap().getMapObjects()) {
                MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                if (mons != null) {
                    if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                        return true;
                    }
                }
            }
        }
        if (monster.isAlive()) {
            boolean killed = monster.damage(chr, damage, false);
            
            selfDestruction selfDestr = monster.getStats().selfDestruction();
            if (selfDestr != null && selfDestr.getHp() > -1) {// should work ;p
                if (monster.getHp() <= selfDestr.getHp()) {
                    killMonster(monster, chr, true, selfDestr.getAction());
                    return true;
                }
            }
            if (killed) {
                killMonster(monster, chr, true);
            }
            return true;
        }
        return false;
    }
    
    public void broadcastBalrogVictory(String leaderName) {
        getWorldServer().dropMessage(6, "[Victory] " + leaderName + "'s party has successfully defeated the Balrog! Praise to them, they finished with " + countAlivePlayers() + " players alive.");
    }
    
    public void broadcastHorntailVictory() {
        getWorldServer().dropMessage(6, "[Victory] To the crew that have finally conquered Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!");
    }
    
    public void broadcastZakumVictory() {
        getWorldServer().dropMessage(6, "[Victory] At last, the tree of evil that for so long overwhelmed Ossyria has fallen. To the crew that managed to finally conquer Zakum, after numerous attempts, victory! You are the true heroes of Ossyria!!");
    }
    
    public void broadcastPinkBeanVictory(int channel) {
        getWorldServer().dropMessage(6, "[Victory] In a swift stroke of sorts, the crew that has attempted Pink Bean at channel " + channel + " has ultimately defeated it. The Temple of Time shines radiantly once again, the day finally coming back, as the crew that managed to finally conquer it returns victoriously from the battlefield!!");
    }
    
    private boolean removeKilledMonsterObject(MapleMonster monster) {
        monster.lockMonster();
        try {
            if(monster.getHp() < 0) {
                return false;
            }
            
            spawnedMonstersOnMap.decrementAndGet();
            removeMapObject(monster);
            monster.disposeMapObject();
            if (monster.hasBossHPBar()) {   // thanks resinate for noticing boss HPbar not clearing after mob defeat in certain scenarios
                broadcastBossHpMessage(monster, monster.hashCode(), monster.makeBossHPBarPacket(), monster.getPosition());
            }
            
            return true;
        } finally {
            monster.unlockMonster();
        }
    }
    
    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops) {
        killMonster(monster, chr, withDrops, 1);
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, int animation) {
        if (monster == null) {
            return;
        }
        
        if (chr == null) {
            if (removeKilledMonsterObject(monster)) {
                monster.dispatchMonsterKilled(false);
                broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), animation), monster.getPosition());
                monster.aggroSwitchController(null, false);
            }
        } else {
            if (removeKilledMonsterObject(monster)) {
                try {
                    if (monster.getStats().getLevel() >= chr.getLevel() + 30 && !chr.isGM()) {
                        AutobanFactory.GENERAL.alert(chr, " for killing a " + monster.getName() + " which is over 30 levels higher.");
                    }

                    /*if (chr.getQuest(MapleQuest.getInstance(29400)).getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                     if (chr.getLevel() >= 120 && monster.getStats().getLevel() >= 120) {
                     //FIX MEDAL SHET
                     } else if (monster.getStats().getLevel() >= chr.getLevel()) {
                     }
                     }*/

                    if (monster.getCP() > 0 && chr.getMap().isCPQMap()) {
                        chr.gainCP(monster.getCP());
                    }

                    int buff = monster.getBuffToGive();
                    if (buff > -1) {
                        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
                        for (MapleMapObject mmo : this.getPlayers()) {
                            MapleCharacter character = (MapleCharacter) mmo;
                            if (character.isAlive()) {
                                MapleStatEffect statEffect = mii.getItemEffect(buff);
                                character.getClient().announce(MaplePacketCreator.showOwnBuffEffect(buff, 1));
                                broadcastMessage(character, MaplePacketCreator.showBuffeffect(character.getId(), buff, 1), false);
                                statEffect.applyTo(character);
                            }
                        }
                    }

                    if (monster.getId() >= 8800003 && monster.getId() <= 8800010) {
                        boolean makeZakReal = true;
                        Collection<MapleMapObject> objects = getMapObjects();
                        for (MapleMapObject object : objects) {
                            MapleMonster mons = getMonsterByOid(object.getObjectId());
                            if (mons != null) {
                                if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                                    makeZakReal = false;
                                    break;
                                }
                            }
                        }
                        if (makeZakReal) {
                            MapleMap map = chr.getMap();

                            for (MapleMapObject object : objects) {
                                MapleMonster mons = map.getMonsterByOid(object.getObjectId());
                                if (mons != null) {
                                    if (mons.getId() == 8800000) {
                                        makeMonsterReal(mons);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    MapleCharacter dropOwner = monster.killBy(chr);
                    if (withDrops && !monster.dropsDisabled()) {
                        if (dropOwner == null) {
                            dropOwner = chr;
                        }
                        dropFromMonster(dropOwner, monster, false);
                    }

                    if (monster.hasBossHPBar()) {
                        for(MapleCharacter mc : this.getAllPlayers()) {
                            if(mc.getTargetHpBarHash() == monster.hashCode()) {
                                mc.resetPlayerAggro();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {     // thanks resinate for pointing out a memory leak possibly from an exception thrown
                    monster.dispatchMonsterKilled(true);
                    broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), animation), monster.getPosition());
                }
            }
        }
    }

    public void killFriendlies(MapleMonster mob) {
        this.killMonster(mob, (MapleCharacter) getPlayers().get(0), false);
    }

    public void killMonster(int mobId) {
        MapleCharacter chr = (MapleCharacter) getPlayers().get(0);
        List<MapleMonster> mobList = getAllMonsters();
        
        for (MapleMonster mob : mobList) {    
            if (mob.getId() == mobId) {
                this.killMonster(mob, chr, false);
            }
        }
    }
    
    public void killMonsterWithDrops(int mobId) {
        Map<Integer, MapleCharacter> mapChars = this.getMapPlayers();
        
        if(!mapChars.isEmpty()) {
            MapleCharacter defaultChr = mapChars.entrySet().iterator().next().getValue();
            List<MapleMonster> mobList = getAllMonsters();
            
            for (MapleMonster mob : mobList) {
                if (mob.getId() == mobId) {
                    MapleCharacter chr = mapChars.get(mob.getHighestDamagerId());
                    if (chr == null) {
                        chr = defaultChr;
                    }
                    
                    this.killMonster(mob, chr, true);
                }
            }
        }
    }
    
    public void softKillAllMonsters() {
        closeMapSpawnPoints();
        
        for (MapleMapObject monstermo : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monstermo;
            if (monster.getStats().isFriendly()) {
                continue;
            }
            
            if(removeKilledMonsterObject(monster)) {
                monster.dispatchMonsterKilled(false);
            }
        }
    }

    public void killAllMonstersNotFriendly() {
        closeMapSpawnPoints();
        
        for (MapleMapObject monstermo : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monstermo;
            if (monster.getStats().isFriendly()) {
                continue;
            }
            
            killMonster(monster, null, false, 1);
        }
    }

    public void killAllMonsters() {
        closeMapSpawnPoints();
        
        for (MapleMapObject monstermo : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monstermo;
            
            killMonster(monster, null, false, 1);
        }
    }
    
    public final void destroyReactors(final int first, final int last) {
        List<MapleReactor> toDestroy = new ArrayList<>();
        List<MapleMapObject> reactors = getReactors();
        
        for (MapleMapObject obj : reactors) {
            MapleReactor mr = (MapleReactor) obj;
            if (mr.getId() >= first && mr.getId() <= last) {
                toDestroy.add(mr);
            }
        }
        
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
        }
    }

    public void destroyReactor(int oid) {
        final MapleReactor reactor = getReactorByOid(oid);
        
        if (reactor != null) {
            if (reactor.destroy()) {
                removeMapObject(reactor);
            }
        }
    }

    public void resetReactors() {
        List<MapleReactor> list = new ArrayList<>();
        
        objectRLock.lock();
        try {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    final MapleReactor r = ((MapleReactor) o);
                    list.add(r);
                }
            }
        } finally {
            objectRLock.unlock();
        }
        
        resetReactors(list);
    }
    
    public final void resetReactors(List<MapleReactor> list) {
        for (MapleReactor r : list) {
            if (r.forceDelayedRespawn()) {  // thanks Conrad for suggesting reactor with delay respawning immediately
                continue;
            }
            
            r.lockReactor();
            try {
                r.resetReactorActions(0);
                r.setAlive(true);
                broadcastMessage(MaplePacketCreator.triggerReactor(r, 0));
            } finally {
                r.unlockReactor();
            }
        }
    }

    public void shuffleReactors() {
        List<Point> points = new ArrayList<>();
        objectRLock.lock();
        try {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    points.add(((MapleReactor) o).getPosition());
                }
            }
            Collections.shuffle(points);
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setPosition(points.remove(points.size() - 1));
                }
            }
        } finally {
            objectRLock.unlock();
        }
    }
    
    public final void shuffleReactors(int first, int last) {
        List<Point> points = new ArrayList<>();
        List<MapleMapObject> reactors = getReactors();
        List<MapleMapObject> targets = new LinkedList<>();
        
        for (MapleMapObject obj : reactors) {
            MapleReactor mr = (MapleReactor) obj;
            if (mr.getId() >= first && mr.getId() <= last) {
                points.add(mr.getPosition());
                targets.add(obj);
            }
        }
        Collections.shuffle(points);
        for (MapleMapObject obj : targets) {
            MapleReactor mr = (MapleReactor) obj;
            mr.setPosition(points.remove(points.size() - 1));
        }
    }
    
    public final void shuffleReactors(List<Object> list) {
        List<Point> points = new ArrayList<>();
        List<MapleMapObject> listObjects = new ArrayList<>();
        List<MapleMapObject> targets = new LinkedList<>();
        
        objectRLock.lock();
        try {
            for (Object ob : list) {
                if(ob instanceof MapleMapObject) {
                    MapleMapObject mmo = (MapleMapObject) ob;
                    
                    if(mapobjects.containsValue(mmo) && mmo.getType() == MapleMapObjectType.REACTOR) {
                        listObjects.add(mmo);
                    }
                }
            }
        } finally {
            objectRLock.unlock();
        }
        
        for (MapleMapObject obj : listObjects) {
            MapleReactor mr = (MapleReactor) obj;
            
            points.add(mr.getPosition());
            targets.add(obj);
        }
        Collections.shuffle(points);
        for (MapleMapObject obj : targets) {
            MapleReactor mr = (MapleReactor) obj;
            mr.setPosition(points.remove(points.size() - 1));
        }
    }
    
    private Map<Integer, MapleMapObject> getCopyMapObjects() {
        objectRLock.lock();
        try {
            return new HashMap<>(mapobjects);
        } finally {
            objectRLock.unlock();
        }
    }
    
    public List<MapleMapObject> getMapObjects() {
        objectRLock.lock();
        try {
            return new LinkedList(mapobjects.values());
        } finally {
            objectRLock.unlock();
        }
    }

    public MapleNPC getNPCById(int id) {
        for (MapleMapObject obj : getMapObjects()) {
            if (obj.getType() == MapleMapObjectType.NPC) {
                MapleNPC npc = (MapleNPC) obj;
                if (npc.getId() == id) {
                    return npc;
                }
            }
        }
        
        return null;
    }
    
    public boolean containsNPC(int npcid) {
        objectRLock.lock();
        try {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.NPC) {
                    if (((MapleNPC) obj).getId() == npcid) {
                        return true;
                    }
                }
            }
        } finally {
            objectRLock.unlock();
        }
        return false;
    }
    
    public void destroyNPC(int npcid) {     // assumption: there's at most one of the same NPC in a map.
        List<MapleMapObject> npcs = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
        
        chrRLock.lock();
        objectWLock.lock();
        try {
            for (MapleMapObject obj : npcs) {
                if (((MapleNPC) obj).getId() == npcid) {
                    broadcastMessage(MaplePacketCreator.removeNPCController(obj.getObjectId()));
                    broadcastMessage(MaplePacketCreator.removeNPC(obj.getObjectId()));
                    
                    this.mapobjects.remove(Integer.valueOf(obj.getObjectId()));
                }
            }
        } finally {
            objectWLock.unlock();
            chrRLock.unlock();
        }
    }

    public MapleMapObject getMapObject(int oid) {
        objectRLock.lock();
        try {
            return mapobjects.get(oid);
        } finally {
            objectRLock.unlock();
        }
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns
     * null
     *
     * @param oid
     * @return
     */
    public MapleMonster getMonsterByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.MONSTER) ? (MapleMonster) mmo : null;
    }

    public MapleReactor getReactorByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.REACTOR) ? (MapleReactor) mmo : null;
    }
    
    public MapleReactor getReactorById(int Id) {
        objectRLock.lock();
        try {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getId() == Id) {
                        return (MapleReactor) obj;
                    }
                }
            }
            return null;
        } finally {
            objectRLock.unlock();
        }
    }
    
    public List<MapleReactor> getReactorsByIdRange(final int first, final int last) {
        List<MapleReactor> list = new LinkedList<>();
        
        objectRLock.lock();
        try {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    MapleReactor mr = (MapleReactor) obj;
                    
                    if (mr.getId() >= first && mr.getId() <= last) {
                        list.add(mr);
                    }
                }
            }
            
            return list;
        } finally {
            objectRLock.unlock();
        }
    }

    public MapleReactor getReactorByName(String name) {
        objectRLock.lock();
        try {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getName().equals(name)) {
                        return (MapleReactor) obj;
                    }
                }
            }
        } finally {
            objectRLock.unlock();
        }
        return null;
    }

    public void spawnMonsterOnGroundBelow(int id, int x, int y) {
        MapleMonster mob = MapleLifeFactory.getMonster(id);
        spawnMonsterOnGroundBelow(mob, new Point(x, y));
    }

    public void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y--;
        mob.setPosition(spos);
        spawnMonster(mob);
    }

    public void spawnCPQMonster(MapleMonster mob, Point pos, int team) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y--;
        mob.setPosition(spos);
        mob.setTeam(team);
        spawnMonster(mob);
    }

    private void monsterItemDrop(final MapleMonster m, long delay) {
        m.dropFromFriendlyMonster(delay);
    }

    public void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = getGroundBelow(pos);
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    public Point getGroundBelow(Point pos) {
        Point spos = new Point(pos.x, pos.y - 14); // Using -14 fixes spawning pets causing a lot of issues.
        spos = calcPointBelow(spos);
        spos.y--;//shouldn't be null!
        return spos;
    }
    
    public Point getPointBelow(Point pos) {
        return calcPointBelow(pos);
    }

    public void spawnRevives(final MapleMonster monster) {
        monster.setMap(this);
        if (getEventInstance() != null) {
            getEventInstance().registerMonster(monster);
        }

        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnMonster(monster, false));
            }
        });
        
        monster.aggroUpdateController();
        updateBossSpawn(monster);
        
        spawnedMonstersOnMap.incrementAndGet();
        addSelfDestructive(monster);
        applyRemoveAfter(monster);
    }
    
    private void applyRemoveAfter(final MapleMonster monster) {
        final selfDestruction selfDestruction = monster.getStats().selfDestruction();
        if (monster.getStats().removeAfter() > 0 || selfDestruction != null && selfDestruction.getHp() < 0) {
            Runnable removeAfterAction;
            
            if (selfDestruction == null) {
                removeAfterAction = new Runnable() {
                    @Override
                    public void run() {
                        killMonster(monster, null, false);
                    }
                };
                
                registerMapSchedule(removeAfterAction, monster.getStats().removeAfter() * 1000);
            } else {
                removeAfterAction = new Runnable() {
                    @Override
                    public void run() {
                        killMonster(monster, null, false, selfDestruction.getAction());
                    }
                };
                
                registerMapSchedule(removeAfterAction, selfDestruction.removeAfter() * 1000);
            }
            
            monster.pushRemoveAfterAction(removeAfterAction);
        }
    }
    
    public void dismissRemoveAfter(final MapleMonster monster) {
        Runnable removeAfterAction = monster.popRemoveAfterAction();
        if (removeAfterAction != null) {
            OverallService service = (OverallService) this.getChannelServer().getServiceAccess(ChannelServices.OVERALL);
            service.forceRunOverallAction(mapid, removeAfterAction);
        }
    }
    
    private List<SpawnPoint> getMonsterSpawn() {
        synchronized (monsterSpawn) {
            return new ArrayList<>(monsterSpawn);
        }
    }
    
    private List<SpawnPoint> getAllMonsterSpawn() {
        synchronized (allMonsterSpawn) {
            return new ArrayList<>(allMonsterSpawn);
        }
    }
    
    public void spawnAllMonsterIdFromMapSpawnList(int id) {
        spawnAllMonsterIdFromMapSpawnList(id, 1, false);
    }
    
    public void spawnAllMonsterIdFromMapSpawnList(int id, int difficulty, boolean isPq) {
        for(SpawnPoint sp: getAllMonsterSpawn()) {
            if(sp.getMonsterId() == id && sp.shouldForceSpawn()) {
                spawnMonster(sp.getMonster(), difficulty, isPq);
            }
        }
    }
    
    public void spawnAllMonstersFromMapSpawnList() {
        spawnAllMonstersFromMapSpawnList(1, false);
    }
    
    public void spawnAllMonstersFromMapSpawnList(int difficulty, boolean isPq) {
        for(SpawnPoint sp: getAllMonsterSpawn()) {
            spawnMonster(sp.getMonster(), difficulty, isPq);
        }
    }
    
    public void spawnMonster(final MapleMonster monster) {
        spawnMonster(monster, 1, false);
    }
    
    public void spawnMonster(final MapleMonster monster, int difficulty, boolean isPq) {
        if (mobCapacity != -1 && mobCapacity == spawnedMonstersOnMap.get()) {
            return;//PyPQ
        }
        
        monster.changeDifficulty(difficulty, isPq);
        
        monster.setMap(this);
        if (getEventInstance() != null) {
            getEventInstance().registerMonster(monster);
        }
        
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnMonster(monster, true));
            }
        }, null);
        
        monster.aggroUpdateController();
        updateBossSpawn(monster);
        
        if ((monster.getTeam() == 1 || monster.getTeam() == 0) && (isCPQMap() || isCPQMap2())) {
            List<MCSkill> teamS = null;
            if (monster.getTeam() == 0) {
                teamS = redTeamBuffs;
            } else if (monster.getTeam() == 1) {
                teamS = blueTeamBuffs;
            }
            if (teamS != null) {
                for (MCSkill skil : teamS) {
                    if (skil != null) {
                        skil.getSkill().applyEffect(null, monster, false, null);
                    }
                }
            }
        }

        if (monster.getDropPeriodTime() > 0) { //9300102 - Watchhog, 9300061 - Moon Bunny (HPQ), 9300093 - Tylus
            if (monster.getId() == 9300102) {
                monsterItemDrop(monster, monster.getDropPeriodTime());
            } else if (monster.getId() == 9300061) {
                monsterItemDrop(monster, monster.getDropPeriodTime() / 3);
            } else if (monster.getId() == 9300093) {
                monsterItemDrop(monster, monster.getDropPeriodTime());
            } else if (monster.getId() == 9400326 || monster.getId() == 9400331 || monster.getId() == 9400336) {
                monsterItemDrop(monster, monster.getDropPeriodTime());
            } else {
                FilePrinter.printError(FilePrinter.UNHANDLED_EVENT, "UNCODED TIMED MOB DETECTED: " + monster.getId());
            }
        }
        
        spawnedMonstersOnMap.incrementAndGet();
        addSelfDestructive(monster);
        applyRemoveAfter(monster);  // thanks LightRyuzaki for pointing issues with spawned CWKPQ mobs not applying this
    }

    public void spawnDojoMonster(final MapleMonster monster) {
        Point[] pts = {new Point(140, 0), new Point(190, 7), new Point(187, 7)};
        spawnMonsterWithEffect(monster, 15, pts[Randomizer.nextInt(3)]);
    }

    public void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        monster.setMap(this);
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        if (spos == null) {
            return;
        }
        
        if (getEventInstance() != null) {
            getEventInstance().registerMonster(monster);
        }
        
        spos.y--;
        monster.setPosition(spos);
        monster.setSpawnEffect(effect);
        
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnMonster(monster, true, effect));
            }
        });
        
        monster.aggroUpdateController();
        updateBossSpawn(monster);

        spawnedMonstersOnMap.incrementAndGet();
        addSelfDestructive(monster);
        applyRemoveAfter(monster);
    }

    public void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnFakeMonster(monster, 0));
            }
        });
        
        spawnedMonstersOnMap.incrementAndGet();
        addSelfDestructive(monster);
    }

    public void makeMonsterReal(final MapleMonster monster) {
        monster.setFake(false);
        broadcastMessage(MaplePacketCreator.makeMonsterReal(monster));
        monster.aggroUpdateController();
        updateBossSpawn(monster);
    }

    public void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);
        spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(reactor.makeSpawnData());
            }
        });
    }

    public void spawnDoor(final MapleDoorObject door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                MapleCharacter chr = c.getPlayer();
                if (chr != null) {
                    door.sendSpawnData(c, false);
                    chr.addVisibleMapObject(door);
                }
            }
        }, new SpawnCondition() {
            @Override
            public boolean canSpawn(MapleCharacter chr) {
                return chr.getMapId() == door.getFrom().getId();
            }
        });
    }
    
    public MaplePortal getDoorPortal(int doorid) {
        MaplePortal doorPortal = portals.get(0x80 + doorid);
        if(doorPortal == null) {
            FilePrinter.printError(FilePrinter.EXCEPTION, "[Door] " + mapName + "(" + mapid + ") does not contain door portalid " + doorid);
            return portals.get(0x80);
        }
        
        return doorPortal;
    }
    
    public void spawnSummon(final MapleSummon summon) {
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if (summon != null) {
                    c.announce(MaplePacketCreator.spawnSummon(summon, true));
                }
            }
        }, null);
    }

    public void spawnMist(final MapleMist mist, final int duration, boolean poison, boolean fake, boolean recovery) {
        addMapObject(mist);
        broadcastMessage(fake ? mist.makeFakeSpawnData(30) : mist.makeSpawnData());
        TimerManager tMan = TimerManager.getInstance();
        final ScheduledFuture<?> poisonSchedule;
        if (poison) {
            Runnable poisonTask = new Runnable() {
                @Override
                public void run() {
                    List<MapleMapObject> affectedMonsters = getMapObjectsInBox(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
                    for (MapleMapObject mo : affectedMonsters) {
                        if (mist.makeChanceResult()) {
                            MonsterStatusEffect poisonEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), mist.getSourceSkill(), null, false);
                            ((MapleMonster) mo).applyStatus(mist.getOwner(), poisonEffect, true, duration);
                        }
                    }
                }
            };
            poisonSchedule = tMan.register(poisonTask, 2000, 2500);
        } else if (recovery) {
            Runnable poisonTask = new Runnable() {
                @Override
                public void run() {
                    List<MapleMapObject> players = getMapObjectsInBox(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER));
                    for (MapleMapObject mo : players) {
                        if (mist.makeChanceResult()) {
                            MapleCharacter chr = (MapleCharacter) mo;
                            if (mist.getOwner().getId() == chr.getId() || mist.getOwner().getParty() != null && mist.getOwner().getParty().containsMembers(chr.getMPC())) {	                        	
                                chr.addMP((int) mist.getSourceSkill().getEffect(chr.getSkillLevel(mist.getSourceSkill().getId())).getX() * chr.getMp() / 100);
                            }
                        }
                    }
                }
            };
            poisonSchedule = tMan.register(poisonTask, 2000, 2500);
        } else {
            poisonSchedule = null;
        }
        
        Runnable mistSchedule = new Runnable() {
            @Override
            public void run() {
                removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
                broadcastMessage(mist.makeDestroyData());
            }
        };
        
        MobMistService service = (MobMistService) this.getChannelServer().getServiceAccess(ChannelServices.MOB_MIST);
        service.registerMobMistCancelAction(mapid, mistSchedule, duration);
    }
    
    public void spawnKite(final MapleKite kite) {
        addMapObject(kite);
        broadcastMessage(kite.makeSpawnData());
        
        Runnable expireKite = new Runnable() {
            @Override
            public void run() {
                removeMapObject(kite);
                broadcastMessage(kite.makeDestroyData());
            }
        };
        
        getWorldServer().registerTimedMapObject(expireKite, YamlConfig.config.server.KITE_EXPIRE_TIME);
    }
    
    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, Point pos, final boolean ffaDrop, final boolean playerDrop) {
        spawnItemDrop(dropper, owner, item, pos, (byte)(ffaDrop ? 2 : 0), playerDrop);
    }

    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, Point pos, final byte dropType, final boolean playerDrop) {
        if (FieldLimit.DROP_LIMIT.check(this.getFieldLimit())) { // thanks Conrad for noticing some maps shouldn't have loots available
            this.disappearingItemDrop(dropper, owner, item, pos);
            return;
        }
        
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem mdrop = new MapleMapItem(item, droppos, dropper, owner, owner.getClient(), dropType, playerDrop);
        mdrop.setDropTime(Server.getInstance().getCurrentTime());

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                mdrop.lockItem();
                try {
                    c.announce(MaplePacketCreator.dropItemFromMapObject(c.getPlayer(), mdrop, dropper.getPosition(), droppos, (byte) 1));
                } finally {
                    mdrop.unlockItem();
                }
            }
        }, null);
        
        mdrop.lockItem();
        try {
            broadcastItemDropMessage(mdrop, dropper.getPosition(), droppos, (byte) 0);
        } finally {
            mdrop.unlockItem();
        }
        
        instantiateItemDrop(mdrop);
        activateItemReactors(mdrop, owner.getClient());
    }
    
    public final void spawnItemDropList(List<Integer> list, final MapleMapObject dropper, final MapleCharacter owner, Point pos) {
        spawnItemDropList(list, 1, 1, dropper, owner, pos, true, false);
    }
    
    public final void spawnItemDropList(List<Integer> list, int minCopies, int maxCopies, final MapleMapObject dropper, final MapleCharacter owner, Point pos) {
        spawnItemDropList(list, minCopies, maxCopies, dropper, owner, pos, true, false);
    }
        
    // spawns item instances of all defined item ids on a list
    public final void spawnItemDropList(List<Integer> list, int minCopies, int maxCopies, final MapleMapObject dropper, final MapleCharacter owner, Point pos, final boolean ffaDrop, final boolean playerDrop) {
        int copies = (maxCopies - minCopies) + 1;
        if(copies < 1) {
            return;
        }
        
        Collections.shuffle(list);
        
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Random rnd = new Random();
        
        final Point dropPos = new Point(pos);
        dropPos.x -= (12 * list.size());
        
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i) == 0) {
                spawnMesoDrop(owner != null ? 10 * owner.getMesoRate() : 10, calcDropPos(dropPos, pos), dropper, owner, playerDrop, (byte) (ffaDrop ? 2 : 0));
            } else {
                final Item drop;
                int randomedId = list.get(i);

                if (ItemConstants.getInventoryType(randomedId) != MapleInventoryType.EQUIP) {
                    drop = new Item(randomedId, (short) 0, (short) (rnd.nextInt(copies) + minCopies));
                } else {
                    drop = ii.randomizeStats((Equip) ii.getEquipById(randomedId));
                }

                spawnItemDrop(dropper, owner, drop, calcDropPos(dropPos, pos), ffaDrop, playerDrop);
            }
            
            dropPos.x += 25;
        }
    }

    private void registerMapSchedule(Runnable r, long delay) {
        OverallService service = (OverallService) this.getChannelServer().getServiceAccess(ChannelServices.OVERALL);
        service.registerOverallAction(mapid, r, delay);
    }
    
    private void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
        final Item item = drop.getItem();

        for (final MapleMapObject o : getReactors()) {
            final MapleReactor react = (MapleReactor) o;

            if (react.getReactorType() == 100) {
                if (react.getReactItem(react.getEventState()).getLeft() == item.getItemId() && react.getReactItem(react.getEventState()).getRight() == item.getQuantity()) {

                    if (react.getArea().contains(drop.getPosition())) {
                        registerMapSchedule(new ActivateItemReactor(drop, react, c), 5000);
                        break;
                    }
                }
            }
        }
    }
    
    public void searchItemReactors(final MapleReactor react) {
        if (react.getReactorType() == 100) {
            Pair<Integer, Integer> reactProp = react.getReactItem(react.getEventState());
            int reactItem = reactProp.getLeft(), reactQty = reactProp.getRight();
            Rectangle reactArea = react.getArea();
            
            List<MapleMapItem> list;
            objectRLock.lock();
            try {
                list = new ArrayList<>(droppedItems.keySet());
            } finally {
                objectRLock.unlock();
            }
            
            for(final MapleMapItem drop : list) {
                drop.lockItem();
                try {
                    if(!drop.isPickedUp()) {
                        final Item item = drop.getItem();
                        
                        if (item != null && reactItem == item.getItemId() && reactQty == item.getQuantity()) {
                            if (reactArea.contains(drop.getPosition())) {
                                MapleClient owner = drop.getOwnerClient();
                                if(owner != null) {
                                    registerMapSchedule(new ActivateItemReactor(drop, react, owner), 5000);
                                }
                            }
                        }
                    }
                } finally {
                    drop.unlockItem();
                }
            }
        }
    }
    
    public void changeEnvironment(String mapObj, int newState) {
        broadcastMessage(MaplePacketCreator.environmentChange(mapObj, newState));
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }
    
    public void startMapEffect(String msg, int itemId, long time) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        broadcastMessage(mapEffect.makeStartData());
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        };
        
        registerMapSchedule(r, time);
    }
    
    public MapleCharacter getAnyCharacterFromParty(int partyid) {
        for (MapleCharacter chr : this.getAllPlayers()) {
            if (chr.getPartyId() == partyid) {
                return chr;
            }
        }
        
        return null;
    }
    
    private void addPartyMemberInternal(MapleCharacter chr, int partyid) {
        if (partyid == -1) {
            return;
        }
        
        Set<Integer> partyEntry = mapParty.get(partyid);
        if(partyEntry == null) {
            partyEntry = new LinkedHashSet<>();
            partyEntry.add(chr.getId());
            
            mapParty.put(partyid, partyEntry);
        } else {
            partyEntry.add(chr.getId());
        }
    }
    
    private void removePartyMemberInternal(MapleCharacter chr, int partyid) {
        if (partyid == -1) {
            return;
        }
        
        Set<Integer> partyEntry = mapParty.get(partyid);
        if(partyEntry != null) {
            if (partyEntry.size() > 1) {
                partyEntry.remove(chr.getId());
            } else {
                mapParty.remove(partyid);
            }
        }
    }
    
    public void addPartyMember(MapleCharacter chr, int partyid) {
        chrWLock.lock();
        try {
            addPartyMemberInternal(chr, partyid);
        } finally {
            chrWLock.unlock();
        }
    }
            
    public void removePartyMember(MapleCharacter chr, int partyid) {
        chrWLock.lock();
        try {
            removePartyMemberInternal(chr, partyid);
        } finally {
            chrWLock.unlock();
        }
    }
    
    public void removeParty(int partyid) {
        chrWLock.lock();
        try {
            mapParty.remove(partyid);
        } finally {
            chrWLock.unlock();
        }
    }
    
    public void addPlayer(final MapleCharacter chr) {
        int chrSize;
        MapleParty party = chr.getParty();
        chrWLock.lock();
        try {
            characters.add(chr);
            chrSize = characters.size();
            
            if (party != null && party.getMemberById(chr.getId()) != null) {
                addPartyMemberInternal(chr, party.getId());
            }
            itemMonitorTimeout = 1;
        } finally {
            chrWLock.unlock();
        }
        
        chr.setMapId(mapid);
        chr.updateActiveEffects();
        
        MapScriptManager msm = MapScriptManager.getInstance();
        if (chrSize == 1) {
            if(!hasItemMonitor()) {
                startItemMonitor();
                aggroMonitor.startAggroCoordinator();
            }
            
            if (onFirstUserEnter.length() != 0) {
                msm.runMapScript(chr.getClient(), "onFirstUserEnter/" + onFirstUserEnter, true);
            }
        }
        if (onUserEnter.length() != 0) {
            if (onUserEnter.equals("cygnusTest") && (mapid < 913040000 || mapid > 913040006)) {
                chr.saveLocation("INTRO");
            }
            
            msm.runMapScript(chr.getClient(), "onUserEnter/" + onUserEnter, false);
        }
        if (FieldLimit.CANNOTUSEMOUNTS.check(fieldLimit) && chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            chr.cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
        }
        
        if (mapid == 200090060) { // To Rien
            int travelTime = getWorldServer().getTransportationTime(1 * 60 * 1000);
            chr.announce(MaplePacketCreator.getClock(travelTime / 1000));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090060) {
                        chr.changeMap(140020300, 0);
                    }
                }
            }, travelTime);
        } else if (mapid == 200090070) { // To Lith Harbor
            int travelTime = getWorldServer().getTransportationTime(1 * 60 * 1000);
            chr.announce(MaplePacketCreator.getClock(travelTime / 1000));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090070) {
                        chr.changeMap(104000000, 3);
                    }
                }
            }, travelTime);
        } else if (mapid == 200090030) { // To Ereve (SkyFerry)
            int travelTime = getWorldServer().getTransportationTime(2 * 60 * 1000);
            chr.announce(MaplePacketCreator.getClock(travelTime / 1000));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090030) {
                        chr.changeMap(130000210, 0);
                    }
                }
            }, travelTime);
        } else if (mapid == 200090031) { // To Victoria Island (SkyFerry)
            int travelTime = getWorldServer().getTransportationTime(2 * 60 * 1000);
            chr.announce(MaplePacketCreator.getClock(travelTime / 1000));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090031) {
                        chr.changeMap(101000400, 0);
                    }
                }
            }, travelTime);
        } else if (mapid == 200090021) { // To Orbis (SkyFerry)
            int travelTime = getWorldServer().getTransportationTime(8 * 60 * 1000);
            chr.announce(MaplePacketCreator.getClock(travelTime / 1000));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090021) {
                        chr.changeMap(200000161, 0);
                    }
                }
            }, travelTime);
        } else if (mapid == 200090020) { // To Ereve From Orbis (SkyFerry)
            int travelTime = getWorldServer().getTransportationTime(8 * 60 * 1000);
            chr.announce(MaplePacketCreator.getClock(travelTime / 1000));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090020) {
                        chr.changeMap(130000210, 0);
                    }
                }
            }, travelTime);
        } else if (MapleMiniDungeonInfo.isDungeonMap(mapid)) {
            MapleMiniDungeon mmd = chr.getClient().getChannelServer().getMiniDungeon(mapid);
            if (mmd != null) {
                mmd.registerPlayer(chr);
            }
        } else if (GameConstants.isAriantColiseumArena(mapid)) {
            int pqTimer = (10 * 60 * 1000);
            chr.announce(MaplePacketCreator.getClock(pqTimer / 1000));
        }
        
        MaplePet[] pets = chr.getPets();
        for (int i = 0; i < pets.length; i++) {
            if (pets[i] != null) {
                pets[i].setPos(getGroundBelow(chr.getPosition()));
                chr.announce(MaplePacketCreator.showPet(chr, pets[i], false, false));
            } else {
                break;
            }
        }
        chr.commitExcludedItems();  // thanks OishiiKawaiiDesu for noticing pet item ignore registry erasing upon changing maps
        
        if (chr.getMonsterCarnival() != null) {
            chr.getClient().announce(MaplePacketCreator.getClock(chr.getMonsterCarnival().getTimeLeftSeconds()));
            if (isCPQMap()) {
                int team = -1;
                int oposition = -1;
                if (chr.getTeam() == 0) {
                    team = 0;
                    oposition = 1;
                }
                if (chr.getTeam() == 1) {
                    team = 1;
                    oposition = 0;
                }
                chr.getClient().announce(MaplePacketCreator.startMonsterCarnival(chr, team, oposition));
            }
        }
        
        chr.removeSandboxItems();
        
        if (chr.getChalkboard() != null) {
            if (!GameConstants.isFreeMarketRoom(mapid)) {
                chr.announce(MaplePacketCreator.useChalkboard(chr, false)); // update player's chalkboard when changing maps found thanks to Vcoc
            } else {
                chr.setChalkboard(null);
            }
        }
        
        if (chr.isHidden()) {
            broadcastGMSpawnPlayerMapObjectMessage(chr, chr, true);
            chr.announce(MaplePacketCreator.getGMEffect(0x10, (byte) 1));

            List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
            broadcastGMMessage(chr, MaplePacketCreator.giveForeignBuff(chr.getId(), dsstat), false);
        } else {
            broadcastSpawnPlayerMapObjectMessage(chr, chr, true);
        }

        sendObjectPlacement(chr.getClient());
        
        if (isStartingEventMap() && !eventStarted()) {
            chr.getMap().getPortal("join00").setPortalStatus(false);
        }
        if (hasForcedEquip()) {
            chr.getClient().announce(MaplePacketCreator.showForcedEquip(-1));
        }
        if (specialEquip()) {
            chr.getClient().announce(MaplePacketCreator.coconutScore(0, 0));
            chr.getClient().announce(MaplePacketCreator.showForcedEquip(chr.getTeam()));
        }
        objectWLock.lock();
        try {
            this.mapobjects.put(Integer.valueOf(chr.getObjectId()), chr);
        } finally {
            objectWLock.unlock();
        }
        
        if (chr.getPlayerShop() != null) {
            addMapObject(chr.getPlayerShop());
        }
        
        final MapleDragon dragon = chr.getDragon();
        if (dragon != null) {
            dragon.setPosition(chr.getPosition());
            this.addMapObject(dragon);
            if (chr.isHidden()) {
                this.broadcastGMMessage(chr, MaplePacketCreator.spawnDragon(dragon));
            } else {
                this.broadcastMessage(chr, MaplePacketCreator.spawnDragon(dragon));
            }
        }

        MapleStatEffect summonStat = chr.getStatForBuff(MapleBuffStat.SUMMON);
        if (summonStat != null) {
            MapleSummon summon = chr.getSummonByKey(summonStat.getSourceId());
            summon.setPosition(chr.getPosition());
            chr.getMap().spawnSummon(summon);
            updateMapObjectVisibility(chr, summon);
        }
        if (mapEffect != null) {
            mapEffect.sendStartData(chr.getClient());
        }
        chr.getClient().announce(MaplePacketCreator.resetForcedStats());
        if (mapid == 914000200 || mapid == 914000210 || mapid == 914000220) {
            chr.getClient().announce(MaplePacketCreator.aranGodlyStats());
        }
        if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) {
            chr.getClient().announce(MaplePacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000)));
        }
        if (chr.getFitness() != null && chr.getFitness().isTimerStarted()) {
            chr.getClient().announce(MaplePacketCreator.getClock((int) (chr.getFitness().getTimeLeft() / 1000)));
        }

        if (chr.getOla() != null && chr.getOla().isTimerStarted()) {
            chr.getClient().announce(MaplePacketCreator.getClock((int) (chr.getOla().getTimeLeft() / 1000)));
        }

        if (mapid == 109060000) {
            chr.announce(MaplePacketCreator.rollSnowBall(true, 0, null, null));
        }

        if (hasClock()) {
            Calendar cal = Calendar.getInstance();
            chr.getClient().announce((MaplePacketCreator.getClockTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
        }
        if (hasBoat() > 0) {
            if (hasBoat() == 1) {
                chr.getClient().announce((MaplePacketCreator.boatPacket(true)));
            } else {
                chr.getClient().announce(MaplePacketCreator.boatPacket(false));
            }
        }
        
        chr.receivePartyMemberHP();
        announcePlayerDiseases(chr.getClient());
    }
    
    private static void announcePlayerDiseases(final MapleClient c) {
        Server.getInstance().registerAnnouncePlayerDiseases(c);
    }

    public MaplePortal getRandomPlayerSpawnpoint() {
        List<MaplePortal> spawnPoints = new ArrayList<>();
        for (MaplePortal portal : portals.values()) {
            if (portal.getType() >= 0 && portal.getType() <= 1 && portal.getTargetMapId() == 999999999) {
                spawnPoints.add(portal);
            }
        }
        MaplePortal portal = spawnPoints.get(new Random().nextInt(spawnPoints.size()));
        return portal != null ? portal : getPortal(0);
    }
    
    public MaplePortal findClosestTeleportPortal(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (portal.getType() == MaplePortal.TELEPORT_PORTAL && distance < shortestDistance && portal.getTargetMapId() != 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }
    
    public MaplePortal findClosestPlayerSpawnpoint(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (portal.getType() >= 0 && portal.getType() <= 1 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }
    
    public MaplePortal findClosestPortal(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }
    
    public MaplePortal findMarketPortal() {
        for (MaplePortal portal : portals.values()) {
            String ptScript = portal.getScriptName();
            if(ptScript != null && ptScript.contains("market")) {
                return portal;
            }
        }
        return null;
    }

    /*
    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }
    */
    
    public void addPlayerPuppet(MapleCharacter player) {
        for (MapleMonster mm : this.getAllMonsters()) {
            mm.aggroAddPuppet(player);
        }
    }
    
    public void removePlayerPuppet(MapleCharacter player) {
        for (MapleMonster mm : this.getAllMonsters()) {
            mm.aggroRemovePuppet(player);
        }
    }

    public void removePlayer(MapleCharacter chr) {
        Channel cserv = chr.getClient().getChannelServer();
        
        FaceExpressionService service = (FaceExpressionService) this.getChannelServer().getServiceAccess(ChannelServices.FACE_EXPRESSION);
        service.unregisterFaceExpression(mapid, chr);
        chr.unregisterChairBuff();
        
        MapleParty party = chr.getParty();
        chrWLock.lock();
        try {
            if (party != null && party.getMemberById(chr.getId()) != null) {
                removePartyMemberInternal(chr, party.getId());
            }
            
            characters.remove(chr);
        } finally {
            chrWLock.unlock();
        }
        
        if (MapleMiniDungeonInfo.isDungeonMap(mapid)) {
            MapleMiniDungeon mmd = cserv.getMiniDungeon(mapid);
            if(mmd != null) {
                if(!mmd.unregisterPlayer(chr)) {
                    cserv.removeMiniDungeon(mapid);
                }
            }
        }
        
        removeMapObject(chr.getObjectId());
        if (!chr.isHidden()) {
            broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        } else {
            broadcastGMMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        }

        chr.leaveMap();
        
        for (MapleSummon summon : new ArrayList<>(chr.getSummonsValues())) {
            if (summon.isStationary()) {
                chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
            } else {
                removeMapObject(summon);
            }
        }

        if (chr.getDragon() != null) {
            removeMapObject(chr.getDragon());
            if (chr.isHidden()) {
                this.broadcastGMMessage(chr, MaplePacketCreator.removeDragon(chr.getId()));
            } else {
                this.broadcastMessage(chr, MaplePacketCreator.removeDragon(chr.getId()));
            }
        }
    }
    
    public void broadcastMessage(final byte[] packet) {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }
    
    public void broadcastGMMessage(final byte[] packet) {
        broadcastGMMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Nonranged. Repeat to source according to parameter.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     */
    public void broadcastMessage(MapleCharacter source, final byte[] packet, boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Ranged and repeat according to parameters.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     * @param ranged
     */
    public void broadcastMessage(MapleCharacter source, final byte[] packet, boolean repeatToSource, boolean ranged) {
        broadcastMessage(repeatToSource ? null : source, packet, ranged ? getRangedDistance() : Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Always ranged from Point.
     *
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(final byte[] packet, Point rangedFrom) {
        broadcastMessage(null, packet, getRangedDistance(), rangedFrom);
    }
    
    /**
     * Always ranged from point. Does not repeat to source.
     *
     * @param source
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MapleCharacter source, final byte[] packet, Point rangedFrom) {
        broadcastMessage(source, packet, getRangedDistance(), rangedFrom);
    }

    private void broadcastMessage(MapleCharacter source, final byte[] packet, double rangeSq, Point rangedFrom) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().announce(packet);
                        }
                    } else {
                        chr.getClient().announce(packet);
                    }
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }
    
    private void updateBossSpawn(MapleMonster monster) {
        if (monster.hasBossHPBar()) {
            broadcastBossHpMessage(monster, monster.hashCode(), monster.makeBossHPBarPacket(), monster.getPosition());
        }
        if (monster.isBoss()) {
            if (unclaimOwnership() != null) {
                String mobName = MapleMonsterInformationProvider.getInstance().getMobNameFromId(monster.getId());
                if (mobName != null) {
                    mobName = mobName.trim();
                    this.dropMessage(5, "This lawn has been taken siege by " + mobName + "'s forces and will be kept hold until their defeat.");
                }
            }
        }
    }
    
    public void broadcastBossHpMessage(MapleMonster mm, int bossHash, final byte[] packet) {
        broadcastBossHpMessage(mm, bossHash, null, packet, Double.POSITIVE_INFINITY, null);
    }
    
    public void broadcastBossHpMessage(MapleMonster mm, int bossHash, final byte[] packet, Point rangedFrom) {
        broadcastBossHpMessage(mm, bossHash, null, packet, getRangedDistance(), rangedFrom);
    }
    
    private void broadcastBossHpMessage(MapleMonster mm, int bossHash, MapleCharacter source, final byte[] packet, double rangeSq, Point rangedFrom) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().announceBossHpBar(mm, bossHash, packet);
                        }
                    } else {
                        chr.getClient().announceBossHpBar(mm, bossHash, packet);
                    }
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }

    private void broadcastItemDropMessage(MapleMapItem mdrop, Point dropperPos, Point dropPos, byte mod, Point rangedFrom) {
        broadcastItemDropMessage(mdrop, dropperPos, dropPos, mod, getRangedDistance(), rangedFrom);
    }
    
    private void broadcastItemDropMessage(MapleMapItem mdrop, Point dropperPos, Point dropPos, byte mod) {
        broadcastItemDropMessage(mdrop, dropperPos, dropPos, mod, Double.POSITIVE_INFINITY, null);
    }
    
    private void broadcastItemDropMessage(MapleMapItem mdrop, Point dropperPos, Point dropPos, byte mod, double rangeSq, Point rangedFrom) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                final byte[] packet = MaplePacketCreator.dropItemFromMapObject(chr, mdrop, dropperPos, dropPos, mod);
                
                if (rangeSq < Double.POSITIVE_INFINITY) {
                    if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                        chr.announce(packet);
                    }
                } else {
                    chr.announce(packet);
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }
    
    public void broadcastSpawnPlayerMapObjectMessage(MapleCharacter source, MapleCharacter player, boolean enteringField) {
        broadcastSpawnPlayerMapObjectMessage(source, player, enteringField, false);
    }
            
    public void broadcastGMSpawnPlayerMapObjectMessage(MapleCharacter source, MapleCharacter player, boolean enteringField) {
        broadcastSpawnPlayerMapObjectMessage(source, player, enteringField, true);
    }
    
    private void broadcastSpawnPlayerMapObjectMessage(MapleCharacter source, MapleCharacter player, boolean enteringField, boolean gmBroadcast) {
        chrRLock.lock();
        try {
            if (gmBroadcast) {
                for (MapleCharacter chr : characters) {
                    if (chr.isGM()) {
                        if (chr != source) {
                            chr.announce(MaplePacketCreator.spawnPlayerMapObject(chr.getClient(), player, enteringField));
                        }
                    }
                }
            } else {
                for (MapleCharacter chr : characters) {
                    if (chr != source) {
                        chr.announce(MaplePacketCreator.spawnPlayerMapObject(chr.getClient(), player, enteringField));
                    }
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }
    
    public void broadcastUpdateCharLookMessage(MapleCharacter source, MapleCharacter player) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    chr.announce(MaplePacketCreator.updateCharLook(chr.getClient(), player));
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }
    
    public void dropMessage(int type, String message) {
        broadcastStringMessage(type, message);
    }
    
    public void broadcastStringMessage(int type, String message) {
        broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }
    
    private static boolean isNonRangedType(MapleMapObjectType type) {
        switch (type) {
            case NPC:
            case PLAYER:
            case HIRED_MERCHANT:
            case PLAYER_NPC:
            case DRAGON:
            case MIST:
            case KITE:
                return true;
            default:
                return false;
        }
    }

    private void sendObjectPlacement(MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        Collection<MapleMapObject> objects;
        
        objectRLock.lock();
        try {
            objects = new ArrayList<>(mapobjects.values());
        } finally {
            objectRLock.unlock();
        }
        
        for (MapleMapObject o : objects) {
            if (isNonRangedType(o.getType())) {
                o.sendSpawnData(c);
            } else if (o.getType() == MapleMapObjectType.SUMMON) {
                MapleSummon summon = (MapleSummon) o;
                if (summon.getOwner() == chr) {
                    if (chr.isSummonsEmpty() || !chr.containsSummon(summon)) {
                        objectWLock.lock();
                        try {
                            mapobjects.remove(o.getObjectId());
                        } finally {
                            objectWLock.unlock();
                        }
                        
                        //continue;
                    }
                }
            }
        }
        
        if (chr != null) {
            for (MapleMapObject o : getMapObjectsInRange(chr.getPosition(), getRangedDistance(), rangedMapobjectTypes)) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) o).isAlive()) {
                        o.sendSpawnData(chr.getClient());
                        chr.addVisibleMapObject(o);
                    }
                } else {
                    o.sendSpawnData(chr.getClient());
                    chr.addVisibleMapObject(o);
                    
                    if (o.getType() == MapleMapObjectType.MONSTER) {
                        ((MapleMonster) o).aggroUpdateController();
                    }
                }
            }
        }
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<>();
        objectRLock.lock();
        try {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (from.distanceSq(l.getPosition()) <= rangeSq) {
                        ret.add(l);
                    }
                }
            }
            return ret;
        } finally {
            objectRLock.unlock();
        }
    }

    public List<MapleMapObject> getMapObjectsInBox(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<>();
        objectRLock.lock();
        try {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
            return ret;
        } finally {
            objectRLock.unlock();
        }
    }

    public void addPortal(MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
    }

    public MaplePortal getPortal(String portalname) {
        for (MaplePortal port : portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public MaplePortal getPortal(int portalid) {
        return portals.get(portalid);
    }
    
    public void addMapleArea(Rectangle rec) {
        areas.add(rec);
    }

    public List<Rectangle> getAreas() {
        return new ArrayList<>(areas);
    }

    public Rectangle getArea(int index) {
        return areas.get(index);
    }

    public void setFootholds(MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public MapleFootholdTree getFootholds() {
        return footholds;
    }
    
    public void setMapPointBoundings(int px, int py, int h, int w) {
        mapArea.setBounds(px, py, w, h);
    }
    
    public void setMapLineBoundings(int vrTop, int vrBottom, int vrLeft, int vrRight) {
        mapArea.setBounds(vrLeft, vrTop, vrRight - vrLeft, vrBottom - vrTop);
    }
    
    public MapleMonsterAggroCoordinator getAggroCoordinator() {
        return aggroMonitor;
    }
    
    /**
     * it's threadsafe, gtfo :D
     *
     * @param monster
     * @param mobTime
     */
    public void addMonsterSpawn(MapleMonster monster, int mobTime, int team) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, !monster.isMobile(), mobTime, mobInterval, team);
        monsterSpawn.add(sp);
        if (sp.shouldSpawn() || mobTime == -1) {// -1 does not respawn and should not either but force ONE spawn
            spawnMonster(sp.getMonster());
        }
    }
    
    public void addAllMonsterSpawn(MapleMonster monster, int mobTime, int team) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, !monster.isMobile(), mobTime, mobInterval, team);
        allMonsterSpawn.add(sp);
    }
    
    public void removeMonsterSpawn(int mobId, int x, int y) {
        // assumption: spawn points identifies by tuple (lifeid, x, y)
        
        Point checkpos = calcPointBelow(new Point(x, y));
        checkpos.y -= 1;
        
        List<SpawnPoint> toRemove = new LinkedList<>();
        for(SpawnPoint sp: getMonsterSpawn()) {
            Point pos = sp.getPosition();
            if (sp.getMonsterId() == mobId && checkpos.equals(pos)) {
                toRemove.add(sp);
            }
        }
        
        if (!toRemove.isEmpty()) {
            synchronized (monsterSpawn) {
                for (SpawnPoint sp : toRemove) {
                    monsterSpawn.remove(sp);
                }
            }
        }
    }
    
    public void removeAllMonsterSpawn(int mobId, int x, int y) {
        // assumption: spawn points identifies by tuple (lifeid, x, y)
        
        Point checkpos = calcPointBelow(new Point(x, y));
        checkpos.y -= 1;
        
        List<SpawnPoint> toRemove = new LinkedList<>();
        for(SpawnPoint sp: getAllMonsterSpawn()) {
            Point pos = sp.getPosition();
            if (sp.getMonsterId() == mobId && checkpos.equals(pos)) {
                toRemove.add(sp);
            }
        }
        
        if (!toRemove.isEmpty()) {
            synchronized (allMonsterSpawn) {
                for (SpawnPoint sp : toRemove) {
                    allMonsterSpawn.remove(sp);
                }
            }
        }
    }
    
    public void reportMonsterSpawnPoints(MapleCharacter chr) {
        chr.dropMessage(6, "Mob spawnpoints on map " + getId() + ", with available Mob SPs " + monsterSpawn.size() + ", used " + spawnedMonstersOnMap.get() + ":");
        for(SpawnPoint sp: getAllMonsterSpawn()) {
            chr.dropMessage(6, "  id: " + sp.getMonsterId() + " canSpawn: " + !sp.getDenySpawn() + " numSpawned: " + sp.getSpawned() + " x: " + sp.getPosition().getX() + " y: " + sp.getPosition().getY() + " time: " + sp.getMobTime() + " team: " + sp.getTeam());
        }
    }

    public Map<Integer, MapleCharacter> getMapPlayers() {
        chrRLock.lock();
        try {
            Map<Integer, MapleCharacter> mapChars = new HashMap<>(characters.size());
            
            for(MapleCharacter chr : characters) {
                mapChars.put(chr.getId(), chr);
            }
            
            return mapChars;
        } finally {
            chrRLock.unlock();
        }
    }
    
    public Collection<MapleCharacter> getCharacters() {
        chrRLock.lock();
        try {
            return Collections.unmodifiableCollection(this.characters);
        } finally {
            chrRLock.unlock();
        }
    }

    public MapleCharacter getCharacterById(int id) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (chr.getId() == id) {
                    return chr;
                }
            }
        } finally {
            chrRLock.unlock();
        }
        return null;
    }

    private static void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
        if (!chr.isMapObjectVisible(mo)) { // object entered view range
            if (mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq(chr.getPosition()) <= getRangedDistance()) {
                chr.addVisibleMapObject(mo);
                mo.sendSpawnData(chr.getClient());
            }
        } else if (mo.getType() != MapleMapObjectType.SUMMON && mo.getPosition().distanceSq(chr.getPosition()) > getRangedDistance()) {
            chr.removeVisibleMapObject(mo);
            mo.sendDestroyData(chr.getClient());
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);
        for (MapleCharacter chr : getAllPlayers()) {
            updateMapObjectVisibility(chr, monster);
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        player.setPosition(newPosition);
        
        try {
            MapleMapObject[] visibleObjects = player.getVisibleMapObjects();

            Map<Integer, MapleMapObject> mapObjects = getCopyMapObjects();
            for (MapleMapObject mo : visibleObjects) {
                if (mo != null) {
                    if (mapObjects.get(mo.getObjectId()) == mo) {
                        updateMapObjectVisibility(player, mo);
                    } else {
                        player.removeVisibleMapObject(mo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), getRangedDistance(), rangedMapobjectTypes)) {
            if (!player.isMapObjectVisible(mo)) {
                mo.sendSpawnData(player.getClient());
                player.addVisibleMapObject(mo);
            }
        }
    }
    
    public final void toggleEnvironment(final String ms) {
        Map<String, Integer> env = getEnvironment();
        
        if (env.containsKey(ms)) {
            moveEnvironment(ms, env.get(ms) == 1 ? 2 : 1);
        } else {
            moveEnvironment(ms, 1);
        }
    }

    public final void moveEnvironment(final String ms, final int type) {
        broadcastMessage(MaplePacketCreator.environmentMove(ms, type));
        
        objectWLock.lock();
        try {
            environment.put(ms, type);
        } finally {
            objectWLock.unlock();
        }
    }

    public final Map<String, Integer> getEnvironment() {
        objectRLock.lock();
        try {
            return Collections.unmodifiableMap(environment);
        } finally {
            objectRLock.unlock();
        }
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setClock(boolean hasClock) {
        this.clock = hasClock;
    }

    public boolean hasClock() {
        return clock;
    }
    
    public void setTown(boolean isTown) {
        this.town = isTown;
    }

    public boolean isTown() {
        return town;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean mute) {
        isMuted = mute;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setEverlast(boolean everlast) {
        this.everlast = everlast;
    }

    public boolean getEverlast() {
        return everlast;
    }

    public int getSpawnedMonstersOnMap() {
        return spawnedMonstersOnMap.get();
    }

    public void setMobCapacity(int capacity) {
        this.mobCapacity = capacity;
    }

    public void setBackgroundTypes(HashMap<Integer, Integer> backTypes) {
        backgroundTypes.putAll(backTypes);
    }
    
    // not really costly to keep generating imo
    public void sendNightEffect(MapleCharacter mc) {
        for (Entry<Integer, Integer> types : backgroundTypes.entrySet()) {
            if (types.getValue() >= 3) { // 3 is a special number
                mc.announce(MaplePacketCreator.changeBackgroundEffect(true, types.getKey(), 0));
            }
        }
    }
    
    public void broadcastNightEffect() {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : this.characters) {
                sendNightEffect(chr);
            }
        } finally {
            chrRLock.unlock();
        }
    }

    public MapleCharacter getCharacterByName(String name) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (chr.getName().toLowerCase().equals(name.toLowerCase())) {
                    return chr;
                }
            }
        } finally {
            chrRLock.unlock();
        }
        return null;
    }
    
    public boolean makeDisappearItemFromMap(MapleMapObject mapobj) {
        if(mapobj instanceof MapleMapItem) {
            return makeDisappearItemFromMap((MapleMapItem) mapobj);
        } else {
            return mapobj == null;  // no drop to make disappear...
        }
    }
    
    public boolean makeDisappearItemFromMap(MapleMapItem mapitem) {
        if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
            mapitem.lockItem();
            try {
                if (mapitem.isPickedUp()) {
                    return true;
                }
                
                MapleMap.this.pickItemDrop(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem);
                return true;
            } finally {
                mapitem.unlockItem();
            }
        }
        
        return false;
    }

    private class MobLootEntry implements Runnable {
        
        private byte droptype;
        private int mobpos;
        private int chRate;
        private Point pos;
        private List<MonsterDropEntry> dropEntry;
        private List<MonsterDropEntry> visibleQuestEntry;
        private List<MonsterDropEntry> otherQuestEntry;
        private List<MonsterGlobalDropEntry> globalEntry;
        private MapleCharacter chr;
        private MapleMonster mob;

        protected MobLootEntry(byte droptype, int mobpos, int chRate, Point pos, List<MonsterDropEntry> dropEntry, List<MonsterDropEntry> visibleQuestEntry, List<MonsterDropEntry> otherQuestEntry, List<MonsterGlobalDropEntry> globalEntry, MapleCharacter chr, MapleMonster mob) {
            this.droptype = droptype;
            this.mobpos = mobpos;
            this.chRate = chRate;
            this.pos = pos;
            this.dropEntry = dropEntry;
            this.visibleQuestEntry = visibleQuestEntry;
            this.otherQuestEntry = otherQuestEntry;
            this.globalEntry = globalEntry;
            this.chr = chr;
            this.mob = mob;
        }

        @Override
        public void run() {
            byte d = 1;
            
            // Normal Drops
            d = dropItemsFromMonsterOnMap(dropEntry, pos, d, chRate, droptype, mobpos, chr, mob);

            // Global Drops
            d = dropGlobalItemsFromMonsterOnMap(globalEntry, pos, d, droptype, mobpos, chr, mob);

            // Quest Drops
            d = dropItemsFromMonsterOnMap(visibleQuestEntry, pos, d, chRate, droptype, mobpos, chr, mob);
            dropItemsFromMonsterOnMap(otherQuestEntry, pos, d, chRate, droptype, mobpos, chr, mob);
        }
    }
    
    private class ActivateItemReactor implements Runnable {

        private MapleMapItem mapitem;
        private MapleReactor reactor;
        private MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            reactor.hitLockReactor();
            try {
                if(reactor.getReactorType() == 100) {
                    if (reactor.getShouldCollect() == true && mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                        mapitem.lockItem();
                        try {
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            mapitem.setPickedUp(true);
                            unregisterItemDrop(mapitem);

                            reactor.setShouldCollect(false);
                            MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem.getPosition());
                            
                            droppedItemCount.decrementAndGet();
                            MapleMap.this.removeMapObject(mapitem);
                            
                            reactor.hitReactor(c);

                            if (reactor.getDelay() > 0) {
                                MapleMap reactorMap = reactor.getMap();
                                
                                OverallService service = (OverallService) reactorMap.getChannelServer().getServiceAccess(ChannelServices.OVERALL);
                                service.registerOverallAction(reactorMap.getId(), new Runnable() {
                                    @Override
                                    public void run() {
                                        reactor.lockReactor();
                                        try {
                                            reactor.resetReactorActions(0);
                                            reactor.setAlive(true);
                                            broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
                                        } finally {
                                            reactor.unlockReactor();
                                        }
                                    }
                                }, reactor.getDelay());
                            }
                        } finally {
                            mapitem.unlockItem();
                        }
                    }
                }
            } finally {
                reactor.hitUnlockReactor();
            }
        }
    }
    
    public void instanceMapFirstSpawn(int difficulty, boolean isPq) {
        for(SpawnPoint spawnPoint: getAllMonsterSpawn()) {
            if(spawnPoint.getMobTime() == -1) {   //just those allowed to be spawned only once
                spawnMonster(spawnPoint.getMonster());
            }
        }
    }

    public void instanceMapRespawn() {
        if (!allowSummons) {
            return;
        }
        
        final int numShouldSpawn = (short) ((monsterSpawn.size() - spawnedMonstersOnMap.get()));//Fking lol'd
        if (numShouldSpawn > 0) {
            List<SpawnPoint> randomSpawn = getMonsterSpawn();
            Collections.shuffle(randomSpawn);
            int spawned = 0;
            for (SpawnPoint spawnPoint : randomSpawn) {
                if(spawnPoint.shouldSpawn()) {
                    spawnMonster(spawnPoint.getMonster());
                    spawned++;
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        }
    }
    
    public void instanceMapForceRespawn() {
        if (!allowSummons) {
            return;
        }
        
        final int numShouldSpawn = (short) ((monsterSpawn.size() - spawnedMonstersOnMap.get()));//Fking lol'd
        if (numShouldSpawn > 0) {
            List<SpawnPoint> randomSpawn = getMonsterSpawn();
            Collections.shuffle(randomSpawn);
            int spawned = 0;
            for (SpawnPoint spawnPoint : randomSpawn) {
                if(spawnPoint.shouldForceSpawn()) {
                    spawnMonster(spawnPoint.getMonster());
                    spawned++;
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        }
    }
    
    public void closeMapSpawnPoints() {
        for (SpawnPoint spawnPoint : getMonsterSpawn()) {
            spawnPoint.setDenySpawn(true);
        }
    }
    
    public void restoreMapSpawnPoints() {
        for (SpawnPoint spawnPoint : getMonsterSpawn()) {
            spawnPoint.setDenySpawn(false);
        }
    }
    
    public void setAllowSpawnPointInBox(boolean allow, Rectangle box) {
        for(SpawnPoint sp: getMonsterSpawn())  {
            if(box.contains(sp.getPosition())) {
                sp.setDenySpawn(!allow);
            }
        }
    }
    
    public void setAllowSpawnPointInRange(boolean allow, Point from, double rangeSq) {
        for(SpawnPoint sp: getMonsterSpawn())  {
            if(from.distanceSq(sp.getPosition()) <= rangeSq) {
                sp.setDenySpawn(!allow);
            }
        }
    }
    
    public SpawnPoint findClosestSpawnpoint(Point from) {
        SpawnPoint closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (SpawnPoint sp : getMonsterSpawn()) {
            double distance = sp.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = sp;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    private static double getCurrentSpawnRate(int numPlayers) {
        return 0.70 + (0.05 * Math.min(6, numPlayers));
    }
    
    private int getNumShouldSpawn(int numPlayers) {
        /*
        System.out.println("----------------------------------");
        for (SpawnPoint spawnPoint : getMonsterSpawn()) {
            System.out.println("sp " + spawnPoint.getPosition().getX() + ", " + spawnPoint.getPosition().getY() + ": " + spawnPoint.getDenySpawn());
        }
        System.out.println("try " + monsterSpawn.size() + " - " + spawnedMonstersOnMap.get());
        System.out.println("----------------------------------");
        */
        
        if(YamlConfig.config.server.USE_ENABLE_FULL_RESPAWN) {
            return (monsterSpawn.size() - spawnedMonstersOnMap.get());
        }
        
        int maxNumShouldSpawn = (int) Math.ceil(getCurrentSpawnRate(numPlayers) * monsterSpawn.size());
        return maxNumShouldSpawn - spawnedMonstersOnMap.get();
    }
    
    public void respawn() {
        if (!allowSummons) {
            return;
        }
        
        int numPlayers;
        chrRLock.lock();
        try {
            numPlayers = characters.size();
            
            if(numPlayers == 0) {
                return;
            }
        } finally {
            chrRLock.unlock();
        }
        
        int numShouldSpawn = getNumShouldSpawn(numPlayers);
        if(numShouldSpawn > 0) {
            List<SpawnPoint> randomSpawn = new ArrayList<>(getMonsterSpawn());
            Collections.shuffle(randomSpawn);
            short spawned = 0;
            for(SpawnPoint spawnPoint : randomSpawn) {
                if(spawnPoint.shouldSpawn()) {
                    spawnMonster(spawnPoint.getMonster());
                    spawned++;
                    
                    if(spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        }
    }
    
    public void mobMpRecovery() {
        for (MapleMonster mob : this.getAllMonsters()) {
            if (mob.isAlive()) {
                mob.heal(0, mob.getLevel());
            }
        }
    }
    
    public final int getNumPlayersInArea(final int index) {
        return getNumPlayersInRect(getArea(index));
    }

    public final int getNumPlayersInRect(final Rectangle rect) {
        int ret = 0;

        chrRLock.lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            while (ltr.hasNext()) {
                if (rect.contains(ltr.next().getPosition())) {
                    ret++;
                }
            }
        } finally {
            chrRLock.unlock();
        }
        return ret;
    }

    public final int getNumPlayersItemsInArea(final int index) {
        return getNumPlayersItemsInRect(getArea(index));
    }

    public final int getNumPlayersItemsInRect(final Rectangle rect) {
        int retP = getNumPlayersInRect(rect);
        int retI = getMapObjectsInBox(rect, Arrays.asList(MapleMapObjectType.ITEM)).size();

        return retP + retI;
    }

    private static interface DelayedPacketCreation {

        void sendPackets(MapleClient c);
    }

    private static interface SpawnCondition {

        boolean canSpawn(MapleCharacter chr);
    }

    public int getHPDec() {
        return decHP;
    }

    public void setHPDec(int delta) {
        decHP = delta;
    }

    public int getHPDecProtect() {
        return protectItem;
    }

    public void setHPDecProtect(int delta) {
        this.protectItem = delta;
    }
    
    public float getRecovery() {
        return recovery;
    }

    public void setRecovery(float recRate) {
        recovery = recRate;
    }

    private int hasBoat() {
        return !boat ? 0 : (docked ? 1 : 2);
    }

    public void setBoat(boolean hasBoat) {
        this.boat = hasBoat;
    }

    public void setDocked(boolean isDocked) {
        this.docked = isDocked;
    }
    
    public boolean getDocked() {
        return this.docked;
    }
    
    public void setSeats(int seats) {
        this.seats = seats;
    }
    
    public int getSeats() {
        return seats;
    }

    public void broadcastGMMessage(MapleCharacter source, final byte[] packet, boolean repeatToSource) {
        broadcastGMMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastGMMessage(MapleCharacter source, final byte[] packet, double rangeSq, Point rangedFrom) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source && chr.isGM()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().announce(packet);
                        }
                    } else {
                        chr.getClient().announce(packet);
                    }
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }

    public void broadcastNONGMMessage(MapleCharacter source, final byte[] packet, boolean repeatToSource) {
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isGM()) {
                    chr.getClient().announce(packet);
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }

    public MapleOxQuiz getOx() {
        return ox;
    }

    public void setOx(MapleOxQuiz set) {
        this.ox = set;
    }

    public void setOxQuiz(boolean b) {
        this.isOxQuiz = b;
    }

    public boolean isOxQuiz() {
        return isOxQuiz;
    }

    public void setOnUserEnter(String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }

    public String getOnUserEnter() {
        return onUserEnter;
    }

    public void setOnFirstUserEnter(String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public String getOnFirstUserEnter() {
        return onFirstUserEnter;
    }

    private boolean hasForcedEquip() {
        return fieldType == 81 || fieldType == 82;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public void clearDrops(MapleCharacter player) {
        for (MapleMapObject i : getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
            droppedItemCount.decrementAndGet();
            removeMapObject(i);
            this.broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, player.getId()));
        }
    }

    public void clearDrops() {
        for (MapleMapObject i : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
            droppedItemCount.decrementAndGet();
            removeMapObject(i);
            this.broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, 0));
        }
    }
    
    public void setFieldLimit(int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public int getFieldLimit() {
        return fieldLimit;
    }

    public void allowSummonState(boolean b) {
        MapleMap.this.allowSummons = b;
    }

    public boolean getSummonState() {
        return MapleMap.this.allowSummons;
    }

    public void warpEveryone(int to) {
        List<MapleCharacter> players = new ArrayList<>(getCharacters());
        
        for (MapleCharacter chr : players) {
            chr.changeMap(to);
        }
    }
    
    public void warpEveryone(int to, int pto) {
        List<MapleCharacter> players = new ArrayList<>(getCharacters());
        
        for (MapleCharacter chr : players) {
            chr.changeMap(to, pto);
        }
    }

    // BEGIN EVENTS
    public void setSnowball(int team, MapleSnowball ball) {
        switch (team) {
            case 0:
                this.snowball0 = ball;
                break;
            case 1:
                this.snowball1 = ball;
                break;
            default:
                break;
        }
    }

    public MapleSnowball getSnowball(int team) {
        switch (team) {
            case 0:
                return snowball0;
            case 1:
                return snowball1;
            default:
                return null;
        }
    }

    private boolean specialEquip() {//Maybe I shouldn't use fieldType :\
        return fieldType == 4 || fieldType == 19;
    }
    
    public void setCoconut(MapleCoconut nut) {
        this.coconut = nut;
    }

    public MapleCoconut getCoconut() {
        return coconut;
    }

    public void warpOutByTeam(int team, int mapid) {
        List<MapleCharacter> chars = new ArrayList<>(getCharacters());
        for (MapleCharacter chr : chars) {
            if (chr != null) {
                if (chr.getTeam() == team) {
                    chr.changeMap(mapid);
                }
            }
        }
    }

    public void startEvent(final MapleCharacter chr) {
        if (this.mapid == 109080000 && getCoconut() == null) {
            setCoconut(new MapleCoconut(this));
            coconut.startEvent();
        } else if (this.mapid == 109040000) {
            chr.setFitness(new MapleFitness(chr));
            chr.getFitness().startFitness();
        } else if (this.mapid == 109030101 || this.mapid == 109030201 || this.mapid == 109030301 || this.mapid == 109030401) {
            chr.setOla(new MapleOla(chr));
            chr.getOla().startOla();
        } else if (this.mapid == 109020001 && getOx() == null) {
            setOx(new MapleOxQuiz(this));
            getOx().sendQuestion();
            setOxQuiz(true);
        } else if (this.mapid == 109060000 && getSnowball(chr.getTeam()) == null) {
            setSnowball(0, new MapleSnowball(0, this));
            setSnowball(1, new MapleSnowball(1, this));
            getSnowball(chr.getTeam()).startEvent();
        }
    }

    public boolean eventStarted() {
        return eventstarted;
    }

    public void startEvent() {
        this.eventstarted = true;
    }

    public void setEventStarted(boolean event) {
        this.eventstarted = event;
    }

    public String getEventNPC() {
        StringBuilder sb = new StringBuilder();
        sb.append("Talk to ");
        if (mapid == 60000) {
            sb.append("Paul!");
        } else if (mapid == 104000000) {
            sb.append("Jean!");
        } else if (mapid == 200000000) {
            sb.append("Martin!");
        } else if (mapid == 220000000) {
            sb.append("Tony!");
        } else {
            return null;
        }
        return sb.toString();
    }

    public boolean hasEventNPC() {
        return this.mapid == 60000 || this.mapid == 104000000 || this.mapid == 200000000 || this.mapid == 220000000;
    }

    public boolean isStartingEventMap() {
        return this.mapid == 109040000 || this.mapid == 109020001 || this.mapid == 109010000 || this.mapid == 109030001 || this.mapid == 109030101;
    }

    public boolean isEventMap() {
        return this.mapid >= 109010000 && this.mapid < 109050000 || this.mapid > 109050001 && this.mapid <= 109090000;
    }

    public void setTimeMob(int id, String msg) {
        timeMob = new Pair<>(id, msg);
    }

    public Pair<Integer, String> getTimeMob() {
        return timeMob;
    }

    public void toggleHiddenNPC(int id) {
        chrRLock.lock();
        objectRLock.lock();
        try {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.NPC) {
                    MapleNPC npc = (MapleNPC) obj;
                    if (npc.getId() == id) {
                        npc.setHide(!npc.isHidden());
                        if (!npc.isHidden()) //Should only be hidden upon changing maps
                        {
                            broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                        }
                    }
                }
            }
        } finally {
            objectRLock.unlock();
            chrRLock.unlock();
        }
    }
    
    public void setMobInterval(short interval) {
        this.mobInterval = interval;
    }

    public short getMobInterval() {
        return mobInterval;
    }
    
    public void clearMapObjects() {
        clearDrops();
        killAllMonsters();
        resetReactors();
    }
    
    public final void resetFully() {
        resetMapObjects();
    }
    
    public void resetMapObjects() {
        resetMapObjects(1, false);
    }
    
    public void resetPQ() {
        resetPQ(1);
    }
    
    public void resetPQ(int difficulty) {
        resetMapObjects(difficulty, true);
    }
    
    public void resetMapObjects(int difficulty, boolean isPq) {
        clearMapObjects();
        
        restoreMapSpawnPoints();
        instanceMapFirstSpawn(difficulty, isPq);
    }
    
    public void broadcastShip(final boolean state) {
        broadcastMessage(MaplePacketCreator.boatPacket(state));
        this.setDocked(state);
    }
    
    public void broadcastEnemyShip(final boolean state) {
        broadcastMessage(MaplePacketCreator.crogBoatPacket(state));
        this.setDocked(state);
    }
    
    public boolean isHorntailDefeated() {   // all parts of dead horntail can be found here?
        for(int i = 8810010; i <= 8810017; i++) {
            if (getMonsterById(i) == null) {
                return false;
            }
        }
        
        return true;
    }
    
    public void spawnHorntailOnGroundBelow(final Point targetPoint) {   // ayy lmao
        MapleMonster htIntro = MapleLifeFactory.getMonster(8810026);
        spawnMonsterOnGroundBelow(htIntro, targetPoint);    // htintro spawn animation converting into horntail detected thanks to Arnah
        
        final MapleMonster ht = MapleLifeFactory.getMonster(8810018);
        ht.setParentMobOid(htIntro.getObjectId());
        ht.addListener(new MonsterListener() {
            @Override
            public void monsterKilled(int aniTime) {}

            @Override
            public void monsterDamaged(MapleCharacter from, int trueDmg) {
                ht.addHp(trueDmg);
            }

            @Override
            public void monsterHealed(int trueHeal) {
                ht.addHp(-trueHeal);
            }
        });
        spawnMonsterOnGroundBelow(ht, targetPoint);

        for (int x = 8810002; x <= 8810009; x++) {
            MapleMonster m = MapleLifeFactory.getMonster(x);
            m.setParentMobOid(htIntro.getObjectId());

            m.addListener(new MonsterListener() {
                @Override
                public void monsterKilled(int aniTime) {}

                @Override
                public void monsterDamaged(MapleCharacter from, int trueDmg) {
                    // thanks Halcyon for noticing HT not dropping loots due to propagated damage not registering attacker
                    ht.applyFakeDamage(from, trueDmg, true);
                }

                @Override
                public void monsterHealed(int trueHeal) {
                    ht.addHp(trueHeal);
                }
            });

            spawnMonsterOnGroundBelow(m, targetPoint);
        }
    }
    
    public boolean claimOwnership(MapleCharacter chr) {
        if (mapOwner == null) {
            this.mapOwner = chr;
            chr.setOwnedMap(this);
            
            mapOwnerLastActivityTime = Server.getInstance().getCurrentTime();
            
            getChannelServer().registerOwnedMap(this);
            return true;
        } else {
            return chr == mapOwner;
        }
    }
    
    public MapleCharacter unclaimOwnership() {
        MapleCharacter lastOwner = this.mapOwner;
        return unclaimOwnership(lastOwner) ? lastOwner : null;
    }
    
    public boolean unclaimOwnership(MapleCharacter chr) {
        if (chr != null && mapOwner == chr) {
            this.mapOwner = null;
            chr.setOwnedMap(null);
            
            mapOwnerLastActivityTime = Long.MAX_VALUE;
            
            getChannelServer().unregisterOwnedMap(this);
            return true;
        } else {
            return false;
        }
    }
    
    private void refreshOwnership() {
        mapOwnerLastActivityTime = Server.getInstance().getCurrentTime();
    }
    
    public boolean isOwnershipRestricted(MapleCharacter chr) {
        MapleCharacter owner = mapOwner;
        
        if (owner != null) {
            if (owner != chr && !owner.isPartyMember(chr)) {    // thanks Vcoc & BHB for suggesting the map ownership feature
                chr.showMapOwnershipInfo(owner);
                return true;
            } else {
                this.refreshOwnership();
            }
        }
        
        return false;
    }
    
    public void checkMapOwnerActivity() {
        long timeNow = Server.getInstance().getCurrentTime();
        if (timeNow - mapOwnerLastActivityTime > 60000) {
            if (unclaimOwnership() != null) {
                this.dropMessage(5, "This lawn is now free real estate.");
            }
        }
    }
    
    private final List<Point> takenSpawns = new LinkedList<>();
    private final List<GuardianSpawnPoint> guardianSpawns = new LinkedList<>();
    private final List<MCSkill> blueTeamBuffs = new ArrayList();
    private final List<MCSkill> redTeamBuffs = new ArrayList();
    private List<Integer> skillIds = new ArrayList();
    private List<Pair<Integer, Integer>> mobsToSpawn = new ArrayList();

    public List<MCSkill> getBlueTeamBuffs() {
        return blueTeamBuffs;
    }

    public List<MCSkill> getRedTeamBuffs() {
        return redTeamBuffs;
    }

    public void clearBuffList() {
        redTeamBuffs.clear();
        blueTeamBuffs.clear();
    }

    public List<MapleMapObject> getAllPlayer() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
    }

    public boolean isCPQMap() {
        switch (this.getId()) {
            case 980000101:
            case 980000201:
            case 980000301:
            case 980000401:
            case 980000501:
            case 980000601:
            case 980031100:
            case 980032100:
            case 980033100:
                return true;
        }
        return false;
    }

    public boolean isCPQMap2() {
        switch (this.getId()) {
            case 980031100:
            case 980032100:
            case 980033100:
                return true;
        }
        return false;
    }

    public boolean isCPQLobby() {
        switch (this.getId()) {
            case 980000100:
            case 980000200:
            case 980000300:
            case 980000400:
            case 980000500:
            case 980000600:
                return true;
        }
        return false;
    }

    public boolean isBlueCPQMap() {
        switch (this.getId()) {
            case 980000501:
            case 980000601:
            case 980031200:
            case 980032200:
            case 980033200:
                return true;
        }
        return false;
    }

    public boolean isPurpleCPQMap() {
        switch (this.getId()) {
            case 980000301:
            case 980000401:
            case 980031200:
            case 980032200:
            case 980033200:
                return true;
        }
        return false;
    }

    public Point getRandomSP(int team) {
        if (takenSpawns.size() > 0) {
            for (SpawnPoint sp : monsterSpawn) {
                for (Point pt : takenSpawns) {
                    if ((sp.getPosition().x == pt.x && sp.getPosition().y == pt.y) || (sp.getTeam() != team && !this.isBlueCPQMap())) {
                        continue;
                    } else {
                        takenSpawns.add(pt);
                        return sp.getPosition();
                    }
                }
            }
        } else {
            for (SpawnPoint sp : monsterSpawn) {
                if (sp.getTeam() == team || this.isBlueCPQMap()) {
                    takenSpawns.add(sp.getPosition());
                    return sp.getPosition();
                }
            }
        }
        return null;
    }

    public GuardianSpawnPoint getRandomGuardianSpawn(int team) {
        boolean alltaken = false;
        for (GuardianSpawnPoint a : this.guardianSpawns) {
            if (!a.isTaken()) {
                alltaken = false;
                break;
            }
        }
        if (alltaken) {
            return null;
        }
        if (this.guardianSpawns.size() > 0) {
            while (true) {
                for (GuardianSpawnPoint gsp : this.guardianSpawns) {
                    if (!gsp.isTaken() && Math.random() < 0.3 && (gsp.getTeam() == -1 || gsp.getTeam() == team)) {
                        return gsp;
                    }
                }
            }
        }
        return null;
    }

    public void addGuardianSpawnPoint(GuardianSpawnPoint a) {
        this.guardianSpawns.add(a);
    }

    public int spawnGuardian(int team, int num) {
        try {
            if (team == 0 && redTeamBuffs.size() >= 4 || team == 1 && blueTeamBuffs.size() >= 4) {
                return 2;
            }
            final MCSkill skill = MapleCarnivalFactory.getInstance().getGuardian(num);
            if (team == 0 && redTeamBuffs.contains(skill)) {
                return 0;
            } else if (team == 1 && blueTeamBuffs.contains(skill)) {
                return 0;
            }
            GuardianSpawnPoint pt = this.getRandomGuardianSpawn(team);
            if (pt == null) {
                return -1;
            }
            int reactorID = 9980000 + team;
            MapleReactor reactor = new MapleReactor(MapleReactorFactory.getReactorS(reactorID), reactorID);
            pt.setTaken(true);
            reactor.setPosition(pt.getPosition());
            reactor.setName(team + "" + num); //lol
            reactor.resetReactorActions(0);
            this.spawnReactor(reactor);
            reactor.setGuardian(pt);
            this.buffMonsters(team, skill);
            getReactorByOid(reactor.getObjectId()).hitReactor(((MapleCharacter) this.getAllPlayer().get(0)).getClient());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void buffMonsters(int team, MCSkill skill) {
        if (skill == null) return;
        
        if (team == 0) {
            redTeamBuffs.add(skill);
        } else if (team == 1) {
            blueTeamBuffs.add(skill);
        }
        for (MapleMapObject mmo : this.mapobjects.values()) {
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mmo;
                if (mob.getTeam() == team) {
                    skill.getSkill().applyEffect(null, mob, false, null);
                }
            }
        }
    }

    public final List<Integer> getSkillIds() {
        return skillIds;
    }

    public final void addSkillId(int z) {
        this.skillIds.add(z);
    }

    public final void addMobSpawn(int mobId, int spendCP) {
        this.mobsToSpawn.add(new Pair<Integer, Integer>(mobId, spendCP));
    }

    public final List<Pair<Integer, Integer>> getMobsToSpawn() {
        return mobsToSpawn;
    }

    public boolean isCPQWinnerMap() {
        switch (this.getId()) {
            case 980000103:
            case 980000203:
            case 980000303:
            case 980000403:
            case 980000503:
            case 980000603:
            case 980031300:
            case 980032300:
            case 980033300:
                return true;
        }
        return false;
    }

    public boolean isCPQLoserMap() {
        switch (this.getId()) {
            case 980000104:
            case 980000204:
            case 980000304:
            case 980000404:
            case 980000504:
            case 980000604:
            case 980031400:
            case 980032400:
            case 980033400:
                return true;
        }
        return false;
    }
    
    public void runCharacterStatUpdate() {
        if (!statUpdateRunnables.isEmpty()) {
            List<Runnable> toRun = new ArrayList<>(statUpdateRunnables);
            statUpdateRunnables.clear();

            for (Runnable r : toRun) {
                r.run();
            }
        }
    }
    
    public void registerCharacterStatUpdate(Runnable r) {
        statUpdateRunnables.add(r);
    }
    
    public void dispose() {
        for(MapleMonster mm : this.getAllMonsters()) {
            mm.dispose();
        }
        
        clearMapObjects();
        
        event = null;
        footholds = null;
        portals.clear();
        mapEffect = null;
        
        chrWLock.lock();
        try {
            aggroMonitor.dispose();
            aggroMonitor = null;
            
            if(itemMonitor != null) {
                itemMonitor.cancel(false);
                itemMonitor = null;
            }

            if(expireItemsTask != null) {
                expireItemsTask.cancel(false);
                expireItemsTask = null;
            }
            
            if(mobSpawnLootTask != null) {
                mobSpawnLootTask.cancel(false);
                mobSpawnLootTask = null;
            }
            
            if(characterStatUpdateTask != null) {
                characterStatUpdateTask.cancel(false);
                characterStatUpdateTask = null;
            }
        } finally {
            chrWLock.unlock();
        }
    }
    
    public int getMaxMobs() {
        return maxMobs;
    }

    public void setMaxMobs(int maxMobs) {
        this.maxMobs = maxMobs;
    }

    public int getMaxReactors() {
        return maxReactors;
    }

    public void setMaxReactors(int maxReactors) {
        this.maxReactors = maxReactors;
    }

    public int getDeathCP() {
        return deathCP;
    }

    public void setDeathCP(int deathCP) {
        this.deathCP = deathCP;
    }

    public int getTimeDefault() {
        return timeDefault;
    }

    public void setTimeDefault(int timeDefault) {
        this.timeDefault = timeDefault;
    }

    public int getTimeExpand() {
        return timeExpand;
    }

    public void setTimeExpand(int timeExpand) {
        this.timeExpand = timeExpand;
    }

}
