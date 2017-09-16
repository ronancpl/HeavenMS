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
import constants.ItemConstants;
import constants.ServerConstants;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.lang.ref.WeakReference;
import net.server.Server;
import net.server.channel.Channel;
import scripting.map.MapScriptManager;
import server.MapleItemInformationProvider;
import server.MaplePortal;
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
import server.partyquest.MonsterCarnival;
import server.partyquest.MonsterCarnivalParty;
import server.partyquest.Pyramid;
import scripting.event.EventInstanceManager;
import server.life.MonsterListener;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

public class MapleMap {
    private static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.ITEM, MapleMapObjectType.NPC, MapleMapObjectType.MONSTER, MapleMapObjectType.DOOR, MapleMapObjectType.SUMMON, MapleMapObjectType.REACTOR);
    private Map<Integer, MapleMapObject> mapobjects = new LinkedHashMap<>();
    private Collection<SpawnPoint> monsterSpawn = Collections.synchronizedList(new LinkedList<SpawnPoint>());
    private Collection<SpawnPoint> allMonsterSpawn = Collections.synchronizedList(new LinkedList<SpawnPoint>());
    private AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private AtomicInteger droppedItemCount = new AtomicInteger(0);
    private Collection<MapleCharacter> characters = new LinkedHashSet<>();
    private Map<Integer, MaplePortal> portals = new HashMap<>();
    private Map<Integer, Integer> backgroundTypes = new HashMap<>();
    private Map<String, Integer> environment = new LinkedHashMap<>();
    private LinkedList<WeakReference<MapleMapObject>> registeredDrops = new LinkedList<>();
    private List<Rectangle> areas = new ArrayList<>();
    private MapleFootholdTree footholds = null;
    private Rectangle mapArea = new Rectangle();
    private int mapid;
    private AtomicInteger runningOid = new AtomicInteger(100);
    private int returnMapId;
    private int channel, world;
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
    private long timeLimit;
    private int decHP = 0;
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
    private ScheduledFuture<?> mapMonitor = null;
    private ScheduledFuture<?> itemMonitor = null;
    private short itemMonitorTimeout;
    private Pair<Integer, String> timeMob = null;
    private short mobInterval = 5000;
    private boolean allowSummons = true; // All maps should have this true at the beginning
    private int lastDoorOwner = -1;
    // HPQ
    private int riceCakes = 0;
    private int bunnyDamage = 0;
    // events
    private boolean eventstarted = false, isMuted = false;
    private MapleSnowball snowball0 = null;
    private MapleSnowball snowball1 = null;
    private MapleCoconut coconut;
    //locks
    private final ReadLock chrRLock;
    private final WriteLock chrWLock;
    private final ReadLock objectRLock;
    private final WriteLock objectWLock;

    public MapleMap(int mapid, int world, int channel, int returnMapId, float monsterRate) {
        this.mapid = mapid;
        this.channel = channel;
        this.world = world;
        this.returnMapId = returnMapId;
        this.monsterRate = (byte) Math.ceil(monsterRate);
        if (this.monsterRate == 0) {
            this.monsterRate = 1;
        }
        final ReentrantReadWriteLock chrLock = new ReentrantReadWriteLock(true);
        chrRLock = chrLock.readLock();
        chrWLock = chrLock.writeLock();

        final ReentrantReadWriteLock objectLock = new ReentrantReadWriteLock(true);
        objectRLock = objectLock.readLock();
        objectWLock = objectLock.writeLock();
    }
    
    public void setEventInstance(EventInstanceManager eim) {
        event = eim;
    }
    
    public EventInstanceManager getEventInstance() {
        return event;
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
        return(ServerConstants.USE_MAXRANGE ? Double.POSITIVE_INFINITY : 722500);
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

    public MapleMap getReturnMap() {
        return Server.getInstance().getWorld(world).getChannel(channel).getMapFactory().getMap(returnMapId);
    }

    public int getReturnMapId() {
        return returnMapId;
    }

    public void setReactorState() {
        objectRLock.lock();
        try {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) o).getState() < 1) {
                        MapleReactor mr = (MapleReactor) o;
                        mr.lockReactor();
                        try {
                            mr.setState((byte) 1);
                            mr.resetReactorActions();
                            
                            broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 1));
                        } finally {
                            mr.unlockReactor();
                        }
                    }
                }
            }
        } finally {
            objectRLock.unlock();
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
    
    public int getForcedReturnId() {
        return forcedReturnMap;
    }

    public MapleMap getForcedReturnMap() {
        return Server.getInstance().getWorld(world).getChannel(channel).getMapFactory().getMap(forcedReturnMap);
    }

    public void setForcedReturnMap(int map) {
        this.forcedReturnMap = map;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTimeLeft() {
        return (int) ((timeLimit - System.currentTimeMillis()) / 1000);
    }

    public int getCurrentPartyId() {
        for (MapleCharacter chr : this.getCharacters()) {
            if (chr.getPartyId() != -1) {
                return chr.getPartyId();
            }
        }
        return -1;
    }

    public void addMapObject(MapleMapObject mapobject) {
        objectWLock.lock();
        try {
            int curOID = getUsableOID();
            mapobject.setObjectId(curOID);
            this.mapobjects.put(curOID, mapobject);
        } finally {
            objectWLock.unlock();
        }
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery) {
        spawnAndAddRangedMapObject(mapobject, packetbakery, null);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) {
        chrRLock.lock();
        objectWLock.lock();
        try {
            int curOID = getUsableOID();
            mapobject.setObjectId(curOID);
            this.mapobjects.put(curOID, mapobject);
            for (MapleCharacter chr : characters) {
                if (condition == null || condition.canSpawn(chr)) {
                    if (chr.getPosition().distanceSq(mapobject.getPosition()) <= getRangedDistance()) {
                        packetbakery.sendPackets(chr.getClient());
                        chr.addVisibleMapObject(mapobject);
                    }
                }
            }
        } finally {
            objectWLock.unlock();
            chrRLock.unlock();
        }
    }
    
    private void spawnRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) {
        chrRLock.lock();
        
        try {
            int curOID = getUsableOID();
            mapobject.setObjectId(curOID);
            for (MapleCharacter chr : characters) {
                if (condition == null || condition.canSpawn(chr)) {
                    if (chr.getPosition().distanceSq(mapobject.getPosition()) <= getRangedDistance()) {
                        packetbakery.sendPackets(chr.getClient());
                        chr.addVisibleMapObject(mapobject);
                    }
                }
            }
        } finally {
            chrRLock.unlock();
        }
    }

    private int getUsableOID() {
        if (runningOid.incrementAndGet() > 2000000000) {
            runningOid.set(1000);
        }
        objectRLock.lock();
        try {
            if (mapobjects.containsKey(runningOid.get())) {
                while (mapobjects.containsKey(runningOid.incrementAndGet()));
            }
        } finally {
            objectRLock.unlock();
        }

        return runningOid.get();
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

    public Point calcDropPos(Point initial, Point fallback) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 85));
        if (ret == null) {
            return fallback;
        } else if(!mapArea.contains(ret)) {
            if(initial.y > mapArea.y + mapArea.height) return fallback; // found drop pos underneath the map :O
            
            int borderX = (initial.x < mapArea.x) ? mapArea.x : mapArea.x + mapArea.width;
            ret = calcPointBelow(new Point(borderX, initial.y - 85));
            
            if(ret == null) return fallback;
        }
        
        return ret;
    }
    
    public boolean canDeployDoor(Point pos) {
        Point toStep = calcPointBelow(pos);
        return toStep != null && toStep.distance(pos) <= 42;
    }
    
    /**
     * Fetches angle relative between spawn and door points
     * where 3 O'Clock is 0 and 12 O'Clock is 270 degrees
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
        if (inRads < 0)
            inRads = Math.abs(inRads);
        else
            inRads = 2 * Math.PI - inRads;

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
        return new Pair(getRoundedCoordinate(angle), Integer.valueOf((int)distn));
    }

    private void dropFromMonster(final MapleCharacter chr, final MapleMonster mob) {
        if (mob.dropsDisabled() || !dropsOn) {
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final byte droptype = (byte) (mob.getStats().isExplosiveReward() ? 3 : mob.getStats().isFfaLoot() ? 2 : chr.getParty() != null ? 1 : 0);
        final int mobpos = mob.getPosition().x;
        int chRate = chr.getDropRate();
        Item idrop;
        byte d = 1;
        Point pos = new Point(0, mob.getPosition().y);

        Map<MonsterStatus, MonsterStatusEffect> stati = mob.getStati();
        if (stati.containsKey(MonsterStatus.SHOWDOWN)) {
            chRate *= (stati.get(MonsterStatus.SHOWDOWN).getStati().get(MonsterStatus.SHOWDOWN).doubleValue() / 100.0 + 1.0);
        }

        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        final List<MonsterDropEntry> dropEntry = new ArrayList<>(mi.retrieveDrop(mob.getId()));

        Collections.shuffle(dropEntry);
        for (final MonsterDropEntry de : dropEntry) {
            if (Randomizer.nextInt(999999) < de.chance * chRate) {
                if (droptype == 3) {
                    pos.x = (int) (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                } else {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                }
                if (de.itemId == 0) { // meso
                    int mesos = Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum;

                    if (mesos > 0) {
                        if (chr.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                            mesos = (int) (mesos * chr.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                        }
                        mesos = mesos * chr.getMesoRate();
                        if(mesos <= 0) mesos = Integer.MAX_VALUE;
                        
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
        final List<MonsterGlobalDropEntry> globalEntry = mi.getGlobalDrop();
        // Global Drops
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
    }
    
    public void dropFromReactor(final MapleCharacter chr, final MapleReactor reactor, Item drop, Point dropPos, short questid) {
        spawnDrop(drop, this.calcDropPos(dropPos, reactor.getPosition()), reactor, chr, (byte)(chr.getParty() != null ? 1 : 0), questid);
    }

    private void stopItemMonitor() {
        chrWLock.lock();
        try {
            itemMonitor.cancel(false);
            itemMonitor = null;
        } finally {
            chrWLock.unlock();
        }
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
            itemMonitor = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    if (getCharacters().isEmpty()) {
                        if(itemMonitorTimeout == 0) {
                            stopItemMonitor();
                            return;
                        } else {
                            itemMonitorTimeout--;
                        }
                    } else {
                        itemMonitorTimeout = 1;
                    }
                    
                    if(!registeredDrops.isEmpty()) cleanItemMonitor();
                }
            }, ServerConstants.ITEM_MONITOR_TIME, ServerConstants.ITEM_MONITOR_TIME);
                    
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
    
    private void registerItemDrop(MapleMapItem mdrop) {
        if(droppedItemCount.get() >= ServerConstants.ITEM_LIMIT_ON_MAP) {
            MapleMapObject mapobj;
            
            objectWLock.lock();
            try {
                mapobj = registeredDrops.remove(0).get();
                while(mapobj == null) {
                    mapobj = registeredDrops.remove(0).get();
                }
            } finally {
                objectWLock.unlock();
            }

            makeDisappearItemFromMap(mapobj);
        }
        
        if(!everlast) TimerManager.getInstance().schedule(new ExpireMapItemJob(mdrop), ServerConstants.ITEM_EXPIRE_TIME);
        
        objectWLock.lock();
        try {
            registeredDrops.add(new WeakReference<>((MapleMapObject) mdrop));
        } finally {
            objectWLock.unlock();
        }
        
        droppedItemCount.incrementAndGet();
    }
    
    public void pickItemDrop(byte[] pickupPacket, MapleMapItem mdrop) {
        broadcastMessage(pickupPacket, mdrop.getPosition());
        
        this.removeMapObject(mdrop);
        mdrop.setPickedUp(true);
        droppedItemCount.decrementAndGet();
    }
    
    private void spawnDrop(final Item idrop, final Point dropPos, final MapleMapObject dropper, final MapleCharacter chr, final byte droptype, final short questid) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, dropper, chr, droptype, false, questid);
        mdrop.setDropTime(System.currentTimeMillis());
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if (questid <= 0 || (c.getPlayer().getQuestStatus(questid) == 1 && c.getPlayer().needQuestItem(questid, idrop.getItemId()))) {
                    c.announce(MaplePacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), dropPos, (byte) 1));
                }
            }
        }, null);

        registerItemDrop(mdrop);
        activateItemReactors(mdrop, chr.getClient());
    }

    public final void spawnMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);
        mdrop.setDropTime(System.currentTimeMillis());

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), droppos, (byte) 1));
            }
        }, null);

        registerItemDrop(mdrop);
    }

    public final void disappearingItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 1, false);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 3), drop.getPosition());
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
        List<MapleCharacter> character = new LinkedList<>();
        chrRLock.lock();
        try {
            for (MapleCharacter a : characters) {
                character.add(a);
            }
        } finally {
            chrRLock.unlock();
        }
        
        return character;
    }
    
    public List<MapleCharacter> getPlayersInRange(Rectangle box, List<MapleCharacter> chr) {
        List<MapleCharacter> character = new LinkedList<>();
        chrRLock.lock();
        try {
            for (MapleCharacter a : characters) {
                if (chr.contains(a.getClient().getPlayer())) {
                    if (box.contains(a.getPosition())) {
                        character.add(a);
                    }
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
            if(mc.isAlive()) count++;
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
            boolean killed = false;
            monster.lockMonster();
            try {
                if (!monster.isAlive()) {
                    return false;
                }
                Pair<Integer, Integer> cool = monster.getStats().getCool();
                if (cool != null) {
                    Pyramid pq = (Pyramid) chr.getPartyQuest();
                    if (pq != null) {
                        if (damage > 0) {
                            if (damage >= cool.getLeft()) {
                                if ((Math.random() * 100) < cool.getRight()) {
                                    pq.cool();
                                } else {
                                    pq.kill();
                                }
                            } else {
                                pq.kill();
                            }
                        } else {
                            pq.miss();
                        }
                        killed = true;
                    }
                }
                if (damage > 0) {
                    monster.damage(chr, damage);
                    if (!monster.isAlive()) {  // monster just died
                        killed = true;
                    }
                } else if (monster.getId() >= 8810002 && monster.getId() <= 8810009) {
                    for (MapleMapObject object : chr.getMap().getMapObjects()) {
                        MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                        if (mons != null) {
                            if (monster.isAlive() && (monster.getId() >= 8810010 && monster.getId() <= 8810017)) {
                                if (mons.getId() == 8810018) {
                                    killMonster(mons, chr, true);
                                }
                            }
                        }
                    }
                }
            } finally {
                monster.unlockMonster();
            }
            if (monster.getStats().selfDestruction() != null && monster.getStats().selfDestruction().getHp() > -1) {// should work ;p
                if (monster.getHp() <= monster.getStats().selfDestruction().getHp()) {
                    killMonster(monster, chr, true, monster.getStats().selfDestruction().getAction());
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

    public List<MapleMonster> getMonsters() {
        List<MapleMonster> mobs = new ArrayList<>();
        for (MapleMapObject object : this.getMapObjects()) {
            if(object instanceof MapleMonster) mobs.add((MapleMonster)object);
        }
        return mobs;
    }
    
    public void broadcastHorntailVictory() {
        for (Channel cserv : Server.getInstance().getWorld(world).getChannels()) {
            for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                player.dropMessage(6, "[VICTORY] To the crew that have finally conquered Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!");
            }
        }
    }
    
    public void broadcastZakumVictory() {
        for (Channel cserv : Server.getInstance().getWorld(world).getChannels()) {
            for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                player.dropMessage(6, "[VICTORY] At last, the tree of evil that for so long overwhelmed Ossyria has fallen. To the crew that managed to finally conquer Zakum, after numerous attempts, victory! You are the true heroes of Ossyria!!");
            }
        }
    }
    
    public void broadcastPinkBeanVictory(int channel) {
        for (Channel cserv : Server.getInstance().getWorld(world).getChannels()) {
            for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                player.dropMessage(6, "[VICTORY] In a swift stroke of sorts, the crew that has attempted Pink Bean at channel " + channel + " has ultimately defeated it. The Temple of Time shines radiantly once again, the day finally coming back, as the crew that managed to finally conquer it returns victoriously from the battlefield!!");
            }
        }
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops) {
        killMonster(monster, chr, withDrops, 1);
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, int animation) {
        if(monster == null) return;
        
        if (chr == null) {
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            removeMapObject(monster);
            monster.dispatchMonsterKilled();
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), animation), monster.getPosition());
            return;
        }
        if (monster.getStats().getLevel() >= chr.getLevel() + 30 && !chr.isGM()) {
            AutobanFactory.GENERAL.alert(chr, " for killing a " + monster.getName() + " which is over 30 levels higher.");
        }
        /*if (chr.getQuest(MapleQuest.getInstance(29400)).getStatus().equals(MapleQuestStatus.Status.STARTED)) {
         if (chr.getLevel() >= 120 && monster.getStats().getLevel() >= 120) {
         //FIX MEDAL SHET
         } else if (monster.getStats().getLevel() >= chr.getLevel()) {
         }
         }*/
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
        
        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);
        //if (monster.getStats().selfDestruction() == null) {//FUU BOMBS D:
        removeMapObject(monster);
        //}
        if (monster.getCP() > 0 && chr.getCarnival() != null) {
            chr.getCarnivalParty().addCP(chr, monster.getCP());
            chr.announce(MaplePacketCreator.updateCP(chr.getCP(), chr.getObtainedCP()));
            broadcastMessage(MaplePacketCreator.updatePartyCP(chr.getCarnivalParty()));
            //they drop items too ):
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
                for (MapleMapObject object : objects) {
                    MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                    if (mons != null) {
                        if (mons.getId() == 8800000) {
                            makeMonsterReal(mons);
                            updateMonsterController(mons);
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
            dropFromMonster(dropOwner, monster);
        }
        
        if (monster.hasBossHPBar()) {
            for(MapleCharacter mc : this.getAllPlayers()) {
                if(mc.getTargetHpBarHash() == monster.hashCode()) {
                    mc.resetPlayerAggro();
                }
            }
        }
        
        monster.dispatchMonsterKilled();
        broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), animation), monster.getPosition());
    }

    public void killFriendlies(MapleMonster mob) {
        this.killMonster(mob, (MapleCharacter) getPlayers().get(0), false);
    }

    public void killMonster(int monsId) {
        List<MapleMapObject> mmoL = new LinkedList(getMapObjects());
        
        for (MapleMapObject mmo : mmoL) {
            if (mmo instanceof MapleMonster) {
                if (((MapleMonster) mmo).getId() == monsId) {
                    this.killMonster((MapleMonster) mmo, (MapleCharacter) getPlayers().get(0), false);
                }
            }
        }
    }

    public void monsterCloakingDevice() {
        for (MapleMapObject monstermo : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monstermo;
            broadcastMessage(MaplePacketCreator.makeMonsterInvisible(monster));
        }
    }

    public void softKillAllMonsters() {
        for (SpawnPoint spawnPoint : monsterSpawn) {
            spawnPoint.setDenySpawn(true);
        }
        
        for (MapleMapObject monstermo : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monstermo;
            if (monster.getStats().isFriendly()) {
                continue;
            }
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            removeMapObject(monster);
        }
    }

    public void killAllMonstersNotFriendly() {
        for (SpawnPoint spawnPoint : monsterSpawn) {
            spawnPoint.setDenySpawn(true);
        }
        
        for (MapleMapObject monstermo : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monstermo;
            if (monster.getStats().isFriendly()) {
                continue;
            }
            
            killMonster(monster, null, false, 1);
        }
    }

    public void killAllMonsters() {
        for (SpawnPoint spawnPoint : monsterSpawn) {
            spawnPoint.setDenySpawn(true);
        }
        
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
        TimerManager tMan = TimerManager.getInstance();
        broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
        reactor.cancelReactorTimeout();
        reactor.setAlive(false);
        removeMapObject(reactor);
        
        if (reactor.getDelay() > 0) {
            tMan.schedule(new Runnable() {
                @Override
                public void run() {
                    respawnReactor(reactor);
                }
            }, reactor.getDelay());
        }
    }

    public void resetReactors() {
        objectRLock.lock();
        try {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    final MapleReactor r = ((MapleReactor) o);
                    
                    r.lockReactor();
                    try {
                        r.setState((byte) 0);
                        r.resetReactorActions();
                        
                        broadcastMessage(MaplePacketCreator.triggerReactor(r, 0));
                    } finally {
                        r.unlockReactor();
                    }
                }
            }
        } finally {
            objectRLock.unlock();
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
            for (Object obj : list) {
                if(obj instanceof MapleMapObject) {
                    MapleMapObject mmo = (MapleMapObject) obj;
                    
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

    /**
     * Automagically finds a new controller for the given monster from the chars
     * on the map...
     *
     * @param monster
     */
    public void updateMonsterController(MapleMonster monster) {
        monster.lockMonster();
        try {
            if (!monster.isAlive()) {
                return;
            }
            if (monster.getController() != null) {
                if (monster.getController().getMap() != this) {
                    monster.getController().stopControllingMonster(monster);
                } else {
                    return;
                }
            }
            int mincontrolled = -1;
            MapleCharacter newController = null;
            chrRLock.lock();
            try {
                for (MapleCharacter chr : characters) {
                    if (!chr.isHidden() && (chr.getControlledMonsters().size() < mincontrolled || mincontrolled == -1)) {
                        mincontrolled = chr.getControlledMonsters().size();
                        newController = chr;
                    }
                }
            } finally {
                chrRLock.unlock();
            }
            if (newController != null) {// was a new controller found? (if not no one is on the map)
                if (monster.isFirstAttack()) {
                    newController.controlMonster(monster, true);
                    monster.setControllerHasAggro(true);
                    monster.setControllerKnowsAboutAggro(true);
                } else {
                    newController.controlMonster(monster, false);
                }
            }
        } finally {
            monster.unlockMonster();
        }
    }

    public Collection<MapleMapObject> getMapObjects() {
        objectRLock.lock();
        try {
            return Collections.unmodifiableCollection(mapobjects.values());
        }
        finally {
            objectRLock.unlock();
        }
    }

    public boolean containsNPC(int npcid) {
        if (npcid == 9000066) {
            return true;
        }
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
    
    public void destroyNPC(int npcid) {
        List<MapleMapObject> npcs = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
        
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

    public void addBunnyHit() {
        bunnyDamage++;
        if (bunnyDamage > 5) {
            broadcastMessage(MaplePacketCreator.serverNotice(6, "The Moon Bunny is feeling sick. Please protect it so it can make delicious rice cakes."));
            bunnyDamage = 0;
        }
    }

    private void monsterItemDrop(final MapleMonster m, final Item item, long delay) {
        final ScheduledFuture<?> monsterItemDrop = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (m.isAlive() && !MapleMap.this.getPlayers().isEmpty()) {
                    if (item.getItemId() == 4001101) {
                        MapleMap.this.riceCakes++;
                        MapleMap.this.broadcastMessage(MaplePacketCreator.serverNotice(6, "The Moon Bunny made rice cake number " + (MapleMap.this.riceCakes)));
                    }
                    spawnItemDrop(m, (MapleCharacter) getPlayers().get(0), item, m.getPosition(), false, false);
                }
            }
        }, delay, delay);
        if (!m.isAlive()) {
            monsterItemDrop.cancel(true);
        }
    }

    public void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = getGroundBelow(pos);
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    public Point getGroundBelow(Point pos) {
        Point spos = new Point(pos.x, pos.y - 3); // Using -3 fixes issues with spawning pets causing a lot of issues.
        spos = calcPointBelow(spos);
        spos.y--;//shouldn't be null!
        return spos;
    }

    public void spawnRevives(final MapleMonster monster) {
        monster.setMap(this);

        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {

                c.announce(MaplePacketCreator.spawnMonster(monster, false));
            }
        });
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }
    
    public void spawnAllMonsterIdFromMapSpawnList(int id) {
        spawnAllMonsterIdFromMapSpawnList(id, 1, false);
    }
    
    public void spawnAllMonsterIdFromMapSpawnList(int id, int difficulty, boolean isPq) {
        for(SpawnPoint sp: allMonsterSpawn) {
            if(sp.getMonsterId() == id) {
                spawnMonster(sp.getMonster(), difficulty, isPq);
            }
        }
    }
    
    public void spawnAllMonstersFromMapSpawnList() {
        spawnAllMonstersFromMapSpawnList(1, false);
    }
    
    public void spawnAllMonstersFromMapSpawnList(int difficulty, boolean isPq) {
        for(SpawnPoint sp: allMonsterSpawn) {
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
        if(getEventInstance() != null) getEventInstance().registerMonster(monster);

        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnMonster(monster, true));
            }
        }, null);
        
        updateMonsterController(monster);

        if (monster.getDropPeriodTime() > 0) { //9300102 - Watchhog, 9300061 - Moon Bunny (HPQ), 9300093 - Tylus
            if (monster.getId() == 9300102) {
                monsterItemDrop(monster, new Item(4031507, (short) 0, (short) 1), monster.getDropPeriodTime());
            } else if (monster.getId() == 9300061) {
                monsterItemDrop(monster, new Item(4001101, (short) 0, (short) 1), monster.getDropPeriodTime() / 3);
            } else if (monster.getId() == 9300093) {
                monsterItemDrop(monster, new Item(4031495, (short) 0, (short) 1), monster.getDropPeriodTime());
            } else {
                FilePrinter.printError(FilePrinter.UNHANDLED_EVENT, "UNCODED TIMED MOB DETECTED: " + monster.getId() + "\r\n");
            }
        }
        spawnedMonstersOnMap.incrementAndGet();
        final selfDestruction selfDestruction = monster.getStats().selfDestruction();
        if (monster.getStats().removeAfter() > 0 || selfDestruction != null && selfDestruction.getHp() < 0) {
            if (selfDestruction == null) {
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        killMonster(monster, null, false);
                    }
                }, monster.getStats().removeAfter() * 1000);
            } else {
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        killMonster(monster, null, false, selfDestruction.getAction());
                    }
                }, selfDestruction.removeAfter() * 1000);
            }
        }
    }

    public void spawnDojoMonster(final MapleMonster monster) {
        Point[] pts = {new Point(140, 0), new Point(190, 7), new Point(187, 7)};
        spawnMonsterWithEffect(monster, 15, pts[Randomizer.nextInt(3)]);
    }

    public void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        monster.setMap(this);
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        if(spos == null) return;
        
        spos.y--;
        monster.setPosition(spos);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnMonster(monster, true, effect));
            }
        });
        if (monster.hasBossHPBar()) {
            broadcastBossHpMessage(monster, monster.hashCode(), monster.makeBossHPBarPacket(), monster.getPosition());
        }
        updateMonsterController(monster);

        spawnedMonstersOnMap.incrementAndGet();
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
    }

    public void makeMonsterReal(final MapleMonster monster) {
        monster.setFake(false);
        broadcastMessage(MaplePacketCreator.makeMonsterReal(monster));
        updateMonsterController(monster);
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

    private void respawnReactor(final MapleReactor reactor) {
        reactor.lockReactor();
        try {
            reactor.setState((byte) 0);
            reactor.resetReactorActions();
            reactor.setAlive(true);
        } finally {
            reactor.unlockReactor();
        }
        
        spawnReactor(reactor);
    }

    public void spawnDoor(final MapleDoorObject door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if (door.getFrom().getId() == c.getPlayer().getMapId()) {
                    if (c.getPlayer().getParty() != null && (door.getOwnerId() == c.getPlayer().getId() || c.getPlayer().getParty().getMemberById(door.getOwnerId()) != null)) {
                        c.announce(MaplePacketCreator.partyPortal(door.getFrom().getId(), door.getTo().getId(), door.toPosition()));
                    }
                    
                    c.announce(MaplePacketCreator.spawnPortal(door.getFrom().getId(), door.getTo().getId(), door.toPosition()));
                    if(!door.inTown()) c.announce(MaplePacketCreator.spawnDoor(door.getOwnerId(), door.getPosition(), false));
                }

                c.announce(MaplePacketCreator.enableActions());
            }
        }, new SpawnCondition() {
            @Override
            public boolean canSpawn(MapleCharacter chr) {
                return chr.getMapId() == door.getFrom().getId();
            }
        });
        
        if(!door.inTown()) setLastDoorOwner(door.getOwnerId());
    }
    
    public List<MaplePortal> getAvailableDoorPortals() {
        objectRLock.lock();
        try {
            List<MaplePortal> availablePortals = new ArrayList<>();
            
            for (MaplePortal port : getPortals()) {
                if (port.getType() == MaplePortal.DOOR_PORTAL) {
                    availablePortals.add(port);
                }
            }
            
            return availablePortals;
        } finally {
            objectRLock.unlock();
        }
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
        tMan.schedule(new Runnable() {
            @Override
            public void run() {
                removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
                broadcastMessage(mist.makeDestroyData());
            }
        }, duration);
    }
    
    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, Point pos, final boolean ffaDrop, final boolean playerDrop) {
        spawnItemDrop(dropper, owner, item, pos, (byte)(ffaDrop ? 2 : 0), playerDrop);
    }

    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, Point pos, final byte dropType, final boolean playerDrop) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem mdrop = new MapleMapItem(item, droppos, dropper, owner, dropType, playerDrop);
        mdrop.setDropTime(System.currentTimeMillis());

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), droppos, (byte) 1));
            }
        }, null);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), droppos, (byte) 0));

        registerItemDrop(mdrop);
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
        if(copies < 1) return;
        
        Collections.shuffle(list);
        
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Random rnd = new Random();
        
        final Point dropPos = new Point(pos);
        dropPos.x -= (12 * list.size());
        
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i) == 0) {
                spawnMesoDrop(owner != null ? 10 * owner.getMesoRate() : 10, calcDropPos(dropPos, pos), dropper, owner, playerDrop, (byte) (ffaDrop ? 2 : 0));
            }
            else {
                final Item drop;
                int randomedId = list.get(i);

                if (ii.getInventoryType(randomedId) != MapleInventoryType.EQUIP) {
                    drop = new Item(randomedId, (short) 0, (short) (rnd.nextInt(copies) + minCopies));
                } else {
                    drop = ii.randomizeStats((Equip) ii.getEquipById(randomedId));
                }

                spawnItemDrop(dropper, owner, drop, calcDropPos(dropPos, pos), ffaDrop, playerDrop);
            }
            
            dropPos.x += 25;
        }
    }

    private void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
        final Item item = drop.getItem();

        for (final MapleMapObject o : getReactors()) {
            final MapleReactor react = (MapleReactor) o;

            if (react.getReactorType() == 100) {
                if (react.getReactItem(react.getEventState()).getLeft() == item.getItemId() && react.getReactItem(react.getEventState()).getRight() == item.getQuantity()) {

                    if (react.getArea().contains(drop.getPosition())) {
                        TimerManager.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000);
                        break;
                    }
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
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, time);
    }
    
    public void addPlayer(final MapleCharacter chr) {
        chrWLock.lock();
        try {
            characters.add(chr);
        } finally {
            chrWLock.unlock();
        }
        chr.setMapId(mapid);
            
        itemMonitorTimeout = 1;
        if (getCharacters().size() <= 1) {
            if(!hasItemMonitor()) startItemMonitor();
            
            if (onFirstUserEnter.length() != 0 && !chr.hasEntered(onFirstUserEnter, mapid) && MapScriptManager.getInstance().scriptExists(onFirstUserEnter, true)) {
                chr.enteredScript(onFirstUserEnter, mapid);
                MapScriptManager.getInstance().getMapScript(chr.getClient(), onFirstUserEnter, true);
            }
        }
        if (onUserEnter.length() != 0) {
            if (onUserEnter.equals("cygnusTest") && (mapid < 913040000 || mapid > 913040006)) {
                chr.saveLocation("INTRO");
            }
            MapScriptManager.getInstance().getMapScript(chr.getClient(), onUserEnter, false);
        }
        if (FieldLimit.CANNOTUSEMOUNTS.check(fieldLimit) && chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            chr.cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
        }
        if (mapid == 923010000 && getMonsterById(9300102) == null) { // Kenta's Mount Quest
            spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300102), new Point(77, 426));
        } else if (mapid == 200090060) { // To Rien
            chr.announce(MaplePacketCreator.getClock(60));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090060) {
                        chr.changeMap(140020300);
                    }
                }
            }, 60 * 1000);
        } else if (mapid == 200090070) { // To Lith Harbor
            chr.announce(MaplePacketCreator.getClock(60));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090070) {
                        chr.changeMap(104000000, 3);
                    }
                }
            }, 60 * 1000);
        } else if (mapid == 200090030) { // To Ereve (SkyFerry)
            chr.getClient().announce(MaplePacketCreator.getClock(60));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090030) {
                        chr.changeMap(130000210);
                    }
                }
            }, 60 * 1000);
        } else if (mapid == 200090031) { // To Victoria Island (SkyFerry)
            chr.getClient().announce(MaplePacketCreator.getClock(60));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090031) {
                        chr.changeMap(101000400);
                    }
                }
            }, 60 * 1000);
        } else if (mapid == 200090021) { // To Orbis (SkyFerry)
            chr.getClient().announce(MaplePacketCreator.getClock(60));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090021) {
                        chr.changeMap(200000161);
                    }
                }
            }, 60 * 1000);
        } else if (mapid == 200090020) { // To Ereve From Orbis (SkyFerry)
            chr.getClient().announce(MaplePacketCreator.getClock(60));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (chr.getMapId() == 200090020) {
                        chr.changeMap(130000210);
                    }
                }
            }, 60 * 1000);
        } else if (mapid == 103040400) {
            if (chr.getEventInstance() != null) {
                chr.getEventInstance().movePlayer(chr);
            }
        } else if (MapleMiniDungeon.isDungeonMap(mapid)) {
            final MapleMiniDungeon dungeon = MapleMiniDungeon.getDungeon(mapid);
            chr.getClient().announce(MaplePacketCreator.getClock(30 * 60));
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (MapleMiniDungeon.isDungeonMap(chr.getMapId())) {
                        chr.changeMap(dungeon.getBase());
                    }
                }
            }, 30 * 60 * 1000);
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
        if (chr.isHidden()) {
            broadcastGMMessage(chr, MaplePacketCreator.spawnPlayerMapobject(chr), false);
            chr.announce(MaplePacketCreator.getGMEffect(0x10, (byte) 1));

            List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
            broadcastGMMessage(chr, MaplePacketCreator.giveForeignBuff(chr.getId(), dsstat), false);
        } else {
            broadcastMessage(chr, MaplePacketCreator.spawnPlayerMapobject(chr), false);
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

        MonsterCarnival carnival = chr.getCarnival();
        MonsterCarnivalParty cparty = chr.getCarnivalParty();
        if (carnival != null && cparty != null && (mapid == 980000101 || mapid == 980000201 || mapid == 980000301 || mapid == 980000401 || mapid == 980000501 || mapid == 980000601)) {
            chr.getClient().announce(MaplePacketCreator.getClock((int) (carnival.getTimeLeft() / 1000)));
            chr.getClient().announce(MaplePacketCreator.startCPQ(chr, carnival.oppositeTeam(cparty)));
        }
        if (hasClock()) {
            Calendar cal = Calendar.getInstance();
            chr.getClient().announce((MaplePacketCreator.getClockTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
        }
        if (hasBoat() > 0) {
            if(hasBoat() == 1) chr.getClient().announce((MaplePacketCreator.boatPacket(true)));
            else chr.getClient().announce(MaplePacketCreator.boatPacket(false));
        }
        
        chr.receivePartyMemberHP();
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

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }

    public void removePlayer(MapleCharacter chr) {
        chrWLock.lock();
        try {
            characters.remove(chr);
        } finally {
            chrWLock.unlock();
        }
        removeMapObject(chr.getObjectId());
        if (!chr.isHidden()) {
            broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        } else {
            broadcastGMMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        }

        for (MapleMonster monster : chr.getControlledMonsters()) {
            monster.setController(null);
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
            updateMonsterController(monster);
        }
        chr.leaveMap();
        
        for (MapleSummon summon : new ArrayList<>(chr.getSummonsValues())) {
            if (summon.isStationary()) {
                chr.cancelBuffStats(MapleBuffStat.PUPPET);
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
    
    public void dropMessage(int type, String message) {
        broadcastStringMessage(type, message);
    }
    
    public void broadcastStringMessage(int type, String message) {
        broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void broadcastBossHpMessage(MapleMonster mm, int bossHash, final byte[] packet) {
        broadcastBossHpMessage(mm, bossHash, null, packet, Double.POSITIVE_INFINITY, null);
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
    
    public void broadcastBossHpMessage(MapleMonster mm, int bossHash, final byte[] packet, Point rangedFrom) {
        broadcastBossHpMessage(mm, bossHash, null, packet, getRangedDistance(), rangedFrom);
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

    private boolean isNonRangedType(MapleMapObjectType type) {
        switch (type) {
            case NPC:
            case PLAYER:
            case HIRED_MERCHANT:
            case PLAYER_NPC:
            case DRAGON:
            case MIST:
                return true;
            default:
                return false;
        }
    }

    private void sendObjectPlacement(MapleClient mapleClient) {
        MapleCharacter chr = mapleClient.getPlayer();
        Collection<MapleMapObject> objects;
        
        objectRLock.lock();
        try {
            objects = Collections.unmodifiableCollection(mapobjects.values());
        } finally {
            objectRLock.unlock();
        }
        
        for (MapleMapObject o : objects) {
            if (o.getType() == MapleMapObjectType.SUMMON) {
                MapleSummon summon = (MapleSummon) o;
                if (summon.getOwner() == chr) {
                    if (chr.isSummonsEmpty() || !chr.containsSummon(summon)) {
                        objectWLock.lock();
                        try {
                            mapobjects.remove(o.getObjectId());
                        } finally {
                            objectWLock.unlock();
                        }
                        
                        continue;
                    }
                }
            }
            if (isNonRangedType(o.getType())) {
                o.sendSpawnData(mapleClient);
            } else if (o.getType() == MapleMapObjectType.MONSTER) {
                updateMonsterController((MapleMonster) o);
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
        mapArea.setBounds(px + 7, py, w - 14, h);
    }
    
    public void setMapLineBoundings(int vrTop, int vrBottom, int vrLeft, int vrRight) {
        mapArea.setBounds(vrLeft + 7, vrTop, vrRight - vrLeft - 14, vrBottom - vrTop);
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
    
    public void reportMonsterSpawnPoints(MapleCharacter chr) {
        chr.dropMessage(6, "Mob spawnpoints on map " + getId() + ", with available Mob SPs " + monsterSpawn.size() + ", used " + spawnedMonstersOnMap.get() + ":");
        for(SpawnPoint sp: allMonsterSpawn) {
            chr.dropMessage(6, "  id: " + sp.getMonsterId() + " canSpawn: " + !sp.getDenySpawn() + " numSpawned: " + sp.getSpawned() + " x: " + sp.getPosition().getX() + " y: " + sp.getPosition().getY() + " time: " + sp.getMobTime() + " team: " + sp.getTeam());
        }
    }

    public Collection<MapleCharacter> getCharacters() {
        chrRLock.lock();
        try {
            return Collections.unmodifiableCollection(this.characters);
        }
        finally {
            chrRLock.unlock();
        }
    }

    public MapleCharacter getCharacterById(int id) {
        chrRLock.lock();
        try {
            for (MapleCharacter c : this.characters) {
                if (c.getId() == id) {
                    return c;
                }
            }
        } finally {
            chrRLock.unlock();
        }
        return null;
    }

    private void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
        if (!chr.isMapObjectVisible(mo)) { // item entered view range
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
        chrRLock.lock();
        try {
            for (MapleCharacter chr : characters) {
                updateMapObjectVisibility(chr, monster);
            }
        } finally {
            chrRLock.unlock();
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        player.setPosition(newPosition);
        Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
        
        objectRLock.lock();
        try {
            MapleMapObject[] visibleObjectsNow = visibleObjects.toArray(new MapleMapObject[visibleObjects.size()]);
            
            for (MapleMapObject mo : visibleObjectsNow) {
                if (mo != null) {
                    if (mapobjects.get(mo.getObjectId()) == mo) {
                        updateMapObjectVisibility(player, mo);
                    } else {
                        player.removeVisibleMapObject(mo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            objectRLock.unlock();
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
            for (MapleCharacter c : characters) {
                sendNightEffect(c);
            }
        } finally {
            chrRLock.unlock();
        }
    }

    public MapleCharacter getCharacterByName(String name) {
        chrRLock.lock();
        try {
            for (MapleCharacter c : this.characters) {
                if (c.getName().toLowerCase().equals(name.toLowerCase())) {
                    return c;
                }
            }
        } finally {
            chrRLock.unlock();
        }
        return null;
    }
    
    public void makeDisappearItemFromMap(MapleMapObject mapobj) {
        if(mapobj instanceof MapleMapItem) {
            makeDisappearItemFromMap((MapleMapItem) mapobj);
        }
    }
    
    public void makeDisappearItemFromMap(MapleMapItem mapitem) {
        if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
            mapitem.lockItem();
            try {
                if (mapitem.isPickedUp()) {
                    return;
                }
                MapleMap.this.pickItemDrop(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem);
            } finally {
                mapitem.unlockItem();
            }
        }
    }

    private class ExpireMapItemJob implements Runnable {

        private MapleMapItem mapitem;

        public ExpireMapItemJob(MapleMapItem mapitem) {
            this.mapitem = mapitem;
        }

        @Override
        public void run() {
            makeDisappearItemFromMap(mapitem);
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
            reactor.lockReactor();
            try {
                if(reactor.getReactorType() == 100) {
                    if (reactor.getShouldCollect() == true && mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                        mapitem.lockItem();
                        try {
                            TimerManager tMan = TimerManager.getInstance();
                            if (mapitem.isPickedUp()) {
                                return;
                            }

                            reactor.setShouldCollect(false);
                            MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem.getPosition());
                            MapleMap.this.removeMapObject(mapitem);
                            reactor.hitReactor(c);

                            if (reactor.getDelay() > 0) {
                                tMan.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        reactor.lockReactor();
                                        try {
                                            reactor.setState((byte) 0);
                                            reactor.resetReactorActions();

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
                reactor.unlockReactor();
            }
        }
    }
    
    public void instanceMapFirstSpawn(int difficulty, boolean isPq) {
        for(SpawnPoint spawnPoint: allMonsterSpawn) {
            if(spawnPoint.getMobTime() == -1) {   //just those allowed to be spawned only once
                spawnMonster(spawnPoint.getMonster());
            }
        }
    }

    public void instanceMapRespawn() {
        if(!allowSummons) return;
        
        final int numShouldSpawn = (short) ((monsterSpawn.size() - spawnedMonstersOnMap.get()));//Fking lol'd
        if (numShouldSpawn > 0) {
            List<SpawnPoint> randomSpawn = new ArrayList<>(monsterSpawn);
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
        if(!allowSummons) return;
        
        final int numShouldSpawn = (short) ((monsterSpawn.size() - spawnedMonstersOnMap.get()));//Fking lol'd
        if (numShouldSpawn > 0) {
            List<SpawnPoint> randomSpawn = new ArrayList<>(monsterSpawn);
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
    
    public void restoreMapSpawnPoints() {
        for (SpawnPoint spawnPoint : monsterSpawn) {
            spawnPoint.setDenySpawn(false);
        }
    }
    
    public void setAllowSpawnPointInBox(boolean allow, Rectangle box) {
        for(SpawnPoint sp: monsterSpawn)  {
            if(box.contains(sp.getPosition())) {
                sp.setDenySpawn(!allow);
            }
        }
    }
    
    public void setAllowSpawnPointInRange(boolean allow, Point from, double rangeSq) {
        for(SpawnPoint sp: monsterSpawn)  {
            if(from.distanceSq(sp.getPosition()) <= rangeSq) {
                sp.setDenySpawn(!allow);
            }
        }
    }
    
    public SpawnPoint findClosestSpawnpoint(Point from) {
        SpawnPoint closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (SpawnPoint sp : monsterSpawn) {
            double distance = sp.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = sp;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public void respawn() {
        if(!allowSummons) return;
        
        chrRLock.lock();
        try {
            if(characters.isEmpty()) {
                return;
            }
        }
        finally {
            chrRLock.unlock();
        }
        
        /*
        System.out.println("----------------------------------");
        for (SpawnPoint spawnPoint : monsterSpawn) {
            System.out.println("sp " + spawnPoint.getPosition().getX() + ", " + spawnPoint.getPosition().getY() + ": " + spawnPoint.getDenySpawn());
        }
        System.out.println("try " + monsterSpawn.size() + " - " + spawnedMonstersOnMap.get());
        System.out.println("----------------------------------");
        */
        
        short numShouldSpawn = (short) ((monsterSpawn.size() - spawnedMonstersOnMap.get()));//Fking lol'd
        if(numShouldSpawn > 0) {
            List<SpawnPoint> randomSpawn = new ArrayList<>(monsterSpawn);
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
        List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
        for (MapleMapObject i : items) {
            player.getMap().removeMapObject(i);
            player.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, player.getId()));
        }
    }

    public void clearDrops() {
        for (MapleMapObject i : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
            removeMapObject(i);
            this.broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, 0));
        }
    }

    public void addMapTimer(int time) {
        timeLimit = System.currentTimeMillis() + (time * 1000);
        broadcastMessage(MaplePacketCreator.getClock(time));
        mapMonitor = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (timeLimit != 0 && timeLimit < System.currentTimeMillis()) {
                    warpEveryone(getForcedReturnId());
                }
                
                if (getCharacters().isEmpty()) {
                    resetReactors();
                    killAllMonsters();
                    clearDrops();
                    timeLimit = 0;
                    if (mapid >= 922240100 && mapid <= 922240119) {
                        toggleHiddenNPC(9001108);
                    }
                    mapMonitor.cancel(true);
                    mapMonitor = null;
                }
            }
        }, 1000);
    }

    public void setFieldLimit(int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public int getFieldLimit() {
        return fieldLimit;
    }

    public void resetRiceCakes() {
        this.riceCakes = 0;
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

    public void timeMob(int id, String msg) {
        timeMob = new Pair<>(id, msg);
    }

    public Pair<Integer, String> getTimeMob() {
        return timeMob;
    }

    public void toggleHiddenNPC(int id) {
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
    
    public boolean isLastDoorOwner(int cid) {
        return lastDoorOwner == cid;
    }
    
    public void setLastDoorOwner(int cid) {
        lastDoorOwner = cid;
    }
    
    public boolean isDojoMap() {
        return mapid >= 925020000 && mapid < 925040000;
    }
    
    public boolean isDojoFightMap() {
        return isDojoMap() && (((mapid / 100) % 100) % 6) > 0;
    }
    
    public boolean isHorntailDefeated() {   // all parts of dead horntail can be found here?
        for(int i = 8810010; i <= 8810017; i++) {
            if(getMonsterById(i) == null) return false;
        }
        
        return true;
    }
    
    public void spawnHorntailOnGroundBelow(final Point targetPoint) {   // ayy lmao
        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8810026), targetPoint);
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                for (int x = 8810002; x <= 8810009; x++) {
                    spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(x), targetPoint);
                }

                spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8810018), targetPoint);
                
                final MapleMonster ht = getMonsterById(8810018);
                for(int mobId = 8810002; mobId <= 8810009; mobId++) {
                    getMonsterById(mobId).addListener(new MonsterListener() {
                        @Override
                        public void monsterKilled(int aniTime) {}

                        @Override
                        public void monsterDamaged(MapleCharacter from, int trueDmg) {
                            ht.damage(from, trueDmg);
                        }
                    });
                }
            }

        }, 5 * 1000);
    }
}
