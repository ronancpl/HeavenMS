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
package scripting.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Properties;
import javax.script.ScriptException;

import config.YamlConfig;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.MonitoredWriteLock;
import net.server.audit.locks.factory.MonitoredReadLockFactory;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.server.audit.locks.factory.MonitoredWriteLockFactory;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.maps.MaplePortal;
import server.TimerManager;
import server.MapleStatEffect;
import server.expeditions.MapleExpedition;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapManager;
import server.maps.MapleReactor;
import client.MapleCharacter;
import client.SkillFactory;
import client.Skill;
import constants.inventory.ItemConstants;
import constants.net.ServerConstants;
import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.server.coordinator.world.MapleEventRecallCoordinator;
import scripting.AbstractPlayerInteraction;
import scripting.event.scheduler.EventScriptScheduler;
import server.MapleItemInformationProvider;
import server.ThreadManager;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author Matze
 * @author Ronan
 */
public class EventInstanceManager {
	private Map<Integer, MapleCharacter> chars = new HashMap<>();
        private int leaderId = -1;
	private List<MapleMonster> mobs = new LinkedList<>();
	private Map<MapleCharacter, Integer> killCount = new HashMap<>();
	private EventManager em;
        private EventScriptScheduler ess;
	private MapleMapManager mapManager;
	private String name;
	private Properties props = new Properties();
        private Map<String, Object> objectProps = new HashMap<>();
	private long timeStarted = 0;
	private long eventTime = 0;
	private MapleExpedition expedition = null;
        private List<Integer> mapIds = new LinkedList<>();
        
        private final MonitoredReentrantReadWriteLock lock = new MonitoredReentrantReadWriteLock(MonitoredLockType.EIM, true);
        private MonitoredReadLock rL = MonitoredReadLockFactory.createLock(lock);
        private MonitoredWriteLock wL = MonitoredWriteLockFactory.createLock(lock);
        
        private MonitoredReentrantLock pL = MonitoredReentrantLockFactory.createLock(MonitoredLockType.EIM_PARTY, true);
        private MonitoredReentrantLock sL = MonitoredReentrantLockFactory.createLock(MonitoredLockType.EIM_SCRIPT, true);
        
        private ScheduledFuture<?> event_schedule = null;
        private boolean disposed = false;
        private boolean eventCleared = false;
        private boolean eventStarted = false;
        
        // multi-leveled PQ rewards!
        private Map<Integer, List<Integer>> collectionSet = new HashMap<>(YamlConfig.config.server.MAX_EVENT_LEVELS);
        private Map<Integer, List<Integer>> collectionQty = new HashMap<>(YamlConfig.config.server.MAX_EVENT_LEVELS);
        private Map<Integer, Integer> collectionExp = new HashMap<>(YamlConfig.config.server.MAX_EVENT_LEVELS);
        
        // Exp/Meso rewards by CLEAR on a stage
        private List<Integer> onMapClearExp = new ArrayList<>();
        private List<Integer> onMapClearMeso = new ArrayList<>();
        
        // registers player status on an event (null on this Map structure equals to 0)
        private Map<Integer, Integer> playerGrid = new HashMap<>();
        
        // registers all opened gates on the event. Will help late characters to encounter next stages gates already opened
        private Map<Integer, Pair<String, Integer>> openedGates = new HashMap<>();
        
        // forces deletion of items not supposed to be held outside of the event, dealt on a player's leaving moment.
        private Set<Integer> exclusiveItems = new HashSet<>();
        
	public EventInstanceManager(EventManager em, String name) {
		this.em = em;
		this.name = name;
                this.ess = new EventScriptScheduler();
		this.mapManager = new MapleMapManager(this, em.getWorldServer().getId(), em.getChannelServer().getId());
	}
        
        public void setName(String name) {
                this.name = name;
        }

	public EventManager getEm() {
                sL.lock();
                try {
                        return em;
                } finally {
                        sL.unlock();
                }
	}
        
        public int getEventPlayersJobs() {
                //Bits -> 0: BEGINNER 1: WARRIOR 2: MAGICIAN
                //        3: BOWMAN 4: THIEF 5: PIRATE
            
                int mask = 0;
                for(MapleCharacter chr: getPlayers()) {
                        mask |= (1 << chr.getJob().getJobNiche());
                }
                
                return mask;
        }
        
        public void applyEventPlayersItemBuff(int itemId) {
                List<MapleCharacter> players = getPlayerList();
                MapleStatEffect mse = MapleItemInformationProvider.getInstance().getItemEffect(itemId);
                
                if(mse != null) {
                        for (MapleCharacter player: players) {
                                mse.applyTo(player);
                        }
                }
        }
        
        public void applyEventPlayersSkillBuff(int skillId) {
                applyEventPlayersSkillBuff(skillId, Integer.MAX_VALUE);
        }
        
        public void applyEventPlayersSkillBuff(int skillId, int skillLv) {
                List<MapleCharacter> players = getPlayerList();
                Skill skill = SkillFactory.getSkill(skillId);
                
                if(skill != null) {
                        MapleStatEffect mse = skill.getEffect(Math.min(skillLv, skill.getMaxLevel()));
                        if(mse != null) {
                                for (MapleCharacter player: players) {
                                        mse.applyTo(player);
                                }
                        }
                }
        }
        
        public void giveEventPlayersExp(int gain) {
                giveEventPlayersExp(gain, -1);
        }
        
        public void giveEventPlayersExp(int gain, int mapId) {
                if(gain == 0) return;
                
                List<MapleCharacter> players = getPlayerList();
            
                if(mapId == -1) {
                        for(MapleCharacter mc: players) {
                                mc.gainExp(gain * mc.getExpRate(), true, true);
                        }
                }
                else {
                        for(MapleCharacter mc: players) {
                                if(mc.getMapId() == mapId) mc.gainExp(gain * mc.getExpRate(), true, true);
                        }
                }
	}
        
        public void giveEventPlayersMeso(int gain) {
                giveEventPlayersMeso(gain, -1);
        }
        
        public void giveEventPlayersMeso(int gain, int mapId) {
                if(gain == 0) return;
                
                List<MapleCharacter> players = getPlayerList();
                
                if(mapId == -1) {
                        for(MapleCharacter mc: players) {
                                mc.gainMeso(gain * mc.getMesoRate());
                        }
                }
                else {
                        for(MapleCharacter mc: players) {
                                if(mc.getMapId() == mapId) mc.gainMeso(gain * mc.getMesoRate());
                        }
                }
                
	}
        
        public Object invokeScriptFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
                if (!disposed) {
                        return em.getIv().invokeFunction(name, args);
                } else {
                        return null;
                }
        }

        public synchronized void registerPlayer(final MapleCharacter chr) {
                registerPlayer(chr, true);
        }
        
	public synchronized void registerPlayer(final MapleCharacter chr, boolean runEntryScript) {
		if (chr == null || !chr.isLoggedinWorld() || disposed) {
			return;
		}
                
                wL.lock();
                try {
                        if(chars.containsKey(chr.getId())) {
                                return;
                        }

                        chars.put(chr.getId(), chr);
                        chr.setEventInstance(this);
                } finally {
                        wL.unlock();
                }
                
                if (runEntryScript) {
                        try {
                                invokeScriptFunction("playerEntry", EventInstanceManager.this, chr);
                        } catch (ScriptException | NoSuchMethodException ex) {
                                ex.printStackTrace();
                        }
                }
	}  
        
        public void exitPlayer(final MapleCharacter chr) {
		if (chr == null || !chr.isLoggedin()){
			return;
		}
		
                unregisterPlayer(chr);
                
                try {
                        invokeScriptFunction("playerExit", EventInstanceManager.this, chr);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
	}
        
        public void dropMessage(int type, String message) {
                for (MapleCharacter chr : getPlayers()) {
                        chr.dropMessage(type, message);
                }
        }

        public void restartEventTimer(long time) {
                stopEventTimer();
                startEventTimer(time);
        }
        
	public void startEventTimer(long time) {
                timeStarted = System.currentTimeMillis();
		eventTime = time;
                
                for(MapleCharacter chr: getPlayers()) {
                        chr.announce(MaplePacketCreator.getClock((int) (time / 1000)));
                }
                
                event_schedule = TimerManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                                dismissEventTimer();
                                
                                try {
                                        invokeScriptFunction("scheduledTimeout", EventInstanceManager.this);
                                } catch (ScriptException | NoSuchMethodException ex) {
                                        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, "Event '" + em.getName() + "' does not implement scheduledTimeout function.", ex);
                                }
                        }
                }, time);
	}
        
        public void addEventTimer(long time) {
                if (event_schedule != null) {
                        if (event_schedule.cancel(false)) {
                                long nextTime = getTimeLeft() + time;
                                eventTime += time;

                                event_schedule = TimerManager.getInstance().schedule(new Runnable() {
                                        @Override
                                        public void run() {
                                                dismissEventTimer();

                                                try {
                                                        invokeScriptFunction("scheduledTimeout", EventInstanceManager.this);
                                                } catch (ScriptException | NoSuchMethodException ex) {
                                                        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, "Event '" + em.getName() + "' does not implement scheduledTimeout function.", ex);
                                                }
                                        }
                                }, nextTime);
                        }
                } else {
                        startEventTimer(time);
                }
        }
        
        private void dismissEventTimer() {
                for(MapleCharacter chr: getPlayers()) {
                        chr.getClient().announce(MaplePacketCreator.removeClock());
                }
                
                event_schedule = null;
                eventTime = 0;
                timeStarted = 0;
        }
        
        public void stopEventTimer() {
                if(event_schedule != null) {
                        event_schedule.cancel(false);
                        event_schedule = null;
                }
                
                dismissEventTimer();
        }
        
	public boolean isTimerStarted() {
		return eventTime > 0 && timeStarted > 0;
	}

	public long getTimeLeft() {
		return eventTime - (System.currentTimeMillis() - timeStarted);
	}

        public void registerParty(MapleCharacter chr) {
                if (chr.isPartyLeader()) {
                        registerParty(chr.getParty(), chr.getMap());
                }
        }
        
	public void registerParty(MapleParty party, MapleMap map) {
		for (MaplePartyCharacter mpc : party.getEligibleMembers()) {
                        if (mpc.isOnline()) {   // thanks resinate
                                MapleCharacter chr = map.getCharacterById(mpc.getId());
                                if (chr != null) {
                                        registerPlayer(chr);
                                }
                        }
		}
	}

	public void registerExpedition(MapleExpedition exped) {
		expedition = exped;
                registerExpeditionTeam(exped, exped.getRecruitingMap().getId());
	}
        
        private void registerExpeditionTeam(MapleExpedition exped, int recruitMap) {
		expedition = exped;
                
                for (MapleCharacter chr: exped.getActiveMembers()) {
                        if (chr.getMapId() == recruitMap) {
                                registerPlayer(chr);
                        }
                }
	}

	public void unregisterPlayer(final MapleCharacter chr) {
                try {
                        invokeScriptFunction("playerUnregistered", EventInstanceManager.this, chr);
                } catch (ScriptException | NoSuchMethodException ex) {
                        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, "Event '" + em.getName() + "' does not implement playerUnregistered function.", ex);
                }
                        
                wL.lock();
                try {
                        chars.remove(chr.getId());
                        chr.setEventInstance(null);
                } finally {
                        wL.unlock();
                }
                
                gridRemove(chr);
                dropExclusiveItems(chr);
	}
	
	public int getPlayerCount() {
                rL.lock();
                try {
                        return chars.size();
                } finally {
                        rL.unlock();
                }
	}
        
        public MapleCharacter getPlayerById(int id) {
                rL.lock();
                try {
                        return chars.get(id);
                } finally {
                        rL.unlock();
                }
	}

	public List<MapleCharacter> getPlayers() {
                rL.lock();
                try {
                        return new ArrayList<>(chars.values());
                } finally {
                        rL.unlock();
                }
	}
        
        private List<MapleCharacter> getPlayerList() {
                rL.lock();
                try {
                        return new LinkedList<>(chars.values());
                } finally {
                        rL.unlock();
                }
        }
        
	public void registerMonster(MapleMonster mob) {
		if (!mob.getStats().isFriendly()) { //We cannot register moon bunny
			mobs.add(mob);
		}
	}

	public void movePlayer(final MapleCharacter chr) {
                try {
                        invokeScriptFunction("moveMap", EventInstanceManager.this, chr);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
	}
        
        public void changedMap(final MapleCharacter chr, final int mapId) {
                try {
                        invokeScriptFunction("changedMap", EventInstanceManager.this, chr, mapId);
                } catch (ScriptException | NoSuchMethodException ex) {} // optional
	}
        
        public void afterChangedMap(final MapleCharacter chr, final int mapId) {
                try {
                        invokeScriptFunction("afterChangedMap", EventInstanceManager.this, chr, mapId);
                } catch (ScriptException | NoSuchMethodException ex) {} // optional
	}
        
        public synchronized void changedLeader(final MaplePartyCharacter ldr) {
                try {
                        invokeScriptFunction("changedLeader", EventInstanceManager.this, ldr);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
		
                leaderId = ldr.getId();
	}
	
	public void monsterKilled(final MapleMonster mob, final boolean hasKiller) {
                int scriptResult = 0;
                
		sL.lock();
                try {
                        mobs.remove(mob);
                        
                        if(eventStarted) {
                                scriptResult = 1;

                                if (mobs.isEmpty()) {
                                        scriptResult = 2;
                                }
                        }
                } finally {
                        sL.unlock();
                }
                
                if (scriptResult > 0) {
                        try {
                                invokeScriptFunction("monsterKilled", mob, EventInstanceManager.this, hasKiller);
                        } catch (ScriptException | NoSuchMethodException ex) {
                                ex.printStackTrace();
                        }
                        
                        if (scriptResult > 1) {
                                try {
                                        invokeScriptFunction("allMonstersDead", EventInstanceManager.this, hasKiller);
                                } catch (ScriptException | NoSuchMethodException ex) {
                                        ex.printStackTrace();
                                }
                        }
                }
        }
        
        public void friendlyKilled(final MapleMonster mob, final boolean hasKiller) {
                try {
                        invokeScriptFunction("friendlyKilled", mob, EventInstanceManager.this, hasKiller);
                } catch (ScriptException | NoSuchMethodException ex) {} //optional
	}
        
        public void friendlyDamaged(final MapleMonster mob) {
                try {
                        invokeScriptFunction("friendlyDamaged", EventInstanceManager.this, mob);
                } catch (ScriptException | NoSuchMethodException ex) {} // optional
	}
        
        public void friendlyItemDrop(final MapleMonster mob) {
                try {
                        invokeScriptFunction("friendlyItemDrop", EventInstanceManager.this, mob);
                } catch (ScriptException | NoSuchMethodException ex) {} // optional
	}

	public void playerKilled(final MapleCharacter chr) {
                ThreadManager.getInstance().newTask(new Runnable() {
                        @Override
                        public void run() {
                                try {
                                        invokeScriptFunction("playerDead", EventInstanceManager.this, chr);
                                } catch (ScriptException | NoSuchMethodException ex) {} // optional
                        }
                });
	}

        public void reviveMonster(final MapleMonster mob) {
                try {
                        invokeScriptFunction("monsterRevive", EventInstanceManager.this, mob);
                } catch (ScriptException | NoSuchMethodException ex) {} // optional
	}
        
	public boolean revivePlayer(final MapleCharacter chr) {
                try {
                        Object b = invokeScriptFunction("playerRevive", EventInstanceManager.this, chr);
                        if (b instanceof Boolean) {
                                return (Boolean) b;
                        }
                } catch (ScriptException | NoSuchMethodException ex) {} // optional
                
		return true;
	}
        
	public void playerDisconnected(final MapleCharacter chr) {
                try {
                        invokeScriptFunction("playerDisconnected", EventInstanceManager.this, chr);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
                
                MapleEventRecallCoordinator.getInstance().storeEventInstance(chr.getId(), this);
	}
        
	public void monsterKilled(MapleCharacter chr, final MapleMonster mob) {
                try {
                        int inc;
                        
                        if (ServerConstants.JAVA_8) {
                                inc = (int)invokeScriptFunction("monsterValue", EventInstanceManager.this, mob.getId());
                        } else {
                                inc = ((Double) invokeScriptFunction("monsterValue", EventInstanceManager.this, mob.getId())).intValue();
                        }
                        
                        if (inc != 0) {
                                Integer kc = killCount.get(chr);
                                if (kc == null) {
                                        kc = inc;
                                } else {
                                        kc += inc;
                                }
                                killCount.put(chr, kc);
                                if (expedition != null){
                                        expedition.monsterKilled(chr, mob);
                                }
                        }
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
	}

	public int getKillCount(MapleCharacter chr) {
		Integer kc = killCount.get(chr);
		return (kc == null) ? 0 : kc;
	}
        
        public void dispose() {
                rL.lock();
                try {
                        for(MapleCharacter chr: chars.values()) chr.setEventInstance(null);
                } finally {
                        rL.unlock();
                }
                
                dispose(false);
        }
        
        public synchronized void dispose(boolean shutdown) {    // should not trigger any event script method after disposed
                if(disposed) return;
                
                try {
                        invokeScriptFunction("dispose", EventInstanceManager.this);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
                disposed = true;
                
                ess.dispose();
                
                wL.lock();
                try {
                        for(MapleCharacter chr: chars.values()) chr.setEventInstance(null);
                        chars.clear();
                        mobs.clear();
                        ess = null;
                } finally {
                        wL.unlock();
                }
                
                if(event_schedule != null) {
                        event_schedule.cancel(false);
                        event_schedule = null;
                }
                
                killCount.clear();
                mapIds.clear();
                props.clear();
                objectProps.clear();
                
                disposeExpedition();
                
                sL.lock();
                try {
                        if(!eventCleared) em.disposeInstance(name);
                } finally {
                        sL.unlock();
                }
                
                TimerManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                                mapManager.dispose();   // issues from instantly disposing some event objects found thanks to MedicOP
                                wL.lock();
                                try {
                                        mapManager = null;
                                        em = null;
                                } finally {
                                        wL.unlock();
                                }

                                disposeLocks();
                        }
                }, 60 * 1000);
	}
        
        private void disposeLocks() {
            LockCollector.getInstance().registerDisposeAction(new Runnable() {
                @Override
                public void run() {
                    emptyLocks();
                }
            });
        }
        
        private void emptyLocks() {
                pL = pL.dispose();
                sL = sL.dispose();
        }

	public MapleMapManager getMapFactory() {
		return mapManager;
	}

	public void schedule(final String methodName, long delay) {
                rL.lock();
                try {
                        if (ess != null) {
                                Runnable r = new Runnable() {
                                        @Override
                                        public void run() {
                                                try {
                                                        invokeScriptFunction(methodName, EventInstanceManager.this);
                                                } catch (ScriptException | NoSuchMethodException ex) {
                                                        ex.printStackTrace();
                                                }
                                        }
                                };

                                ess.registerEntry(r, delay);
                        }
                } finally {
                        rL.unlock();
                }
	}

	public String getName() {
		return name;
	}

	public MapleMap getMapInstance(int mapId) {
		MapleMap map = mapManager.getMap(mapId);
                map.setEventInstance(this);

		if (!mapManager.isMapLoaded(mapId)) {
                        sL.lock();
                        try {
                                if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true")) {
                                        map.shuffleReactors();
                                }
                        } finally {
                                sL.unlock();
                        }
		}
		return map;
	}

        public void setIntProperty(String key, Integer value) {
                setProperty(key, value);
        }
        
        public void setProperty(String key, Integer value) {
                setProperty(key, "" + value);
        }
        
	public void setProperty(String key, String value) {
                pL.lock();
                try {
                        props.setProperty(key, value);
                } finally {
                        pL.unlock();
                }
	}

	public Object setProperty(String key, String value, boolean prev) {
                pL.lock();
                try {
                        return props.setProperty(key, value);
                } finally {
                        pL.unlock();
                }
	}
        
        public void setObjectProperty(String key, Object obj) {
                pL.lock();
                try {
                        objectProps.put(key, obj);
                } finally {
                        pL.unlock();
                }
        }

	public String getProperty(String key) {
                pL.lock();
                try {
                        return props.getProperty(key);
                } finally {
                        pL.unlock();
                }
	}

        public int getIntProperty(String key) {
                pL.lock();
                try {
                        return Integer.parseInt(props.getProperty(key));
                } finally {
                        pL.unlock();
                }
        }
        
        public Object getObjectProperty(String key) {
                pL.lock();
                try {
                        return objectProps.get(key);
                } finally {
                        pL.unlock();
                }
        }
	
	public void leftParty(final MapleCharacter chr) {
                try {
                        invokeScriptFunction("leftParty", EventInstanceManager.this, chr);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
	}

	public void disbandParty() {
		try {
                        invokeScriptFunction("disbandParty", EventInstanceManager.this);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
	}

	public void clearPQ() {
                try {
                        invokeScriptFunction("clearPQ", EventInstanceManager.this);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
	}

	public void removePlayer(final MapleCharacter chr) {
                try {
                        invokeScriptFunction("playerExit", EventInstanceManager.this, chr);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
	}

	public boolean isLeader(MapleCharacter chr) {
		return (chr.getParty().getLeaderId() == chr.getId());
	}
        
        public boolean isEventLeader(MapleCharacter chr) {
		return (chr.getId() == getLeaderId());
	}
        
        public final MapleMap getInstanceMap(final int mapid) {
                if (disposed) {
                        return null;
                }
                mapIds.add(mapid);
                return getMapFactory().getMap(mapid);
        }
        
        public final boolean disposeIfPlayerBelow(final byte size, final int towarp) {
                if (disposed) {
                        return true;
                }
                if(chars == null) {
                        return false;
                }
                
                MapleMap map = null;
                if (towarp > 0) {
                        map = this.getMapFactory().getMap(towarp);
                }
                
                List<MapleCharacter> players = getPlayerList();

                try {
                        if (players.size() < size) {
                                for (MapleCharacter chr : players) {
                                        if (chr == null) {
                                                continue;
                                        }

                                        unregisterPlayer(chr);
                                        if (towarp > 0) {
                                                chr.changeMap(map, map.getPortal(0));
                                        }
                                }

                                dispose();
                                return true;
                        }
                } catch (Exception ex) {
                        ex.printStackTrace();
                }
                
                return false;
        }
        
        public void spawnNpc(int npcId, Point pos, MapleMap map) {
                MapleNPC npc = MapleLifeFactory.getNPC(npcId);
                if (npc != null) {
                        npc.setPosition(pos);
                        npc.setCy(pos.y);
                        npc.setRx0(pos.x + 50);
                        npc.setRx1(pos.x - 50);
                        npc.setFh(map.getFootholds().findBelow(pos).getId());
                        map.addMapObject(npc);
                        map.broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                }
        }
        
        public void dispatchRaiseQuestMobCount(int mobid, int mapid) {
            Map<Integer, MapleCharacter> mapChars = getInstanceMap(mapid).getMapPlayers();
            if(!mapChars.isEmpty()) {
                List<MapleCharacter> eventMembers = getPlayers();
                
                for (MapleCharacter evChr : eventMembers) {
                    MapleCharacter chr = mapChars.get(evChr.getId());

                    if(chr != null && chr.isLoggedinWorld()) {
                        chr.raiseQuestMobCount(mobid);
                    }
                }
            }
        }
        
        public MapleMonster getMonster(int mid) {
                return(MapleLifeFactory.getMonster(mid));
        }
        
        private List<Integer> convertToIntegerArray(List<Object> list) {
            List<Integer> intList = new ArrayList<>();
            
            if (ServerConstants.JAVA_8) {
                for (Object d: list) {
                    intList.add(((Integer) d).intValue());
                }
            } else {
                for (Object d: list) {
                    intList.add(((Double) d).intValue());
                }
            }
            
            return intList;
        }
        
        public void setEventClearStageExp(List<Object> gain) {
                onMapClearExp.clear();
                onMapClearExp.addAll(convertToIntegerArray(gain));
        }
        
        public void setEventClearStageMeso(List<Object> gain) {
                onMapClearMeso.clear();
                onMapClearMeso.addAll(convertToIntegerArray(gain));
        }
        
        public Integer getClearStageExp(int stage) {    //stage counts from ONE.
                if(stage > onMapClearExp.size()) return 0;
                return onMapClearExp.get(stage - 1);
        }
        
        public Integer getClearStageMeso(int stage) {   //stage counts from ONE.
                if(stage > onMapClearMeso.size()) return 0;
                return onMapClearMeso.get(stage - 1);
        }
        
        public List<Integer> getClearStageBonus(int stage) {
                List<Integer> list = new ArrayList<>();
                list.add(getClearStageExp(stage));
                list.add(getClearStageMeso(stage));
                
                return list;
        }
        
        private void dropExclusiveItems(MapleCharacter chr) {
                AbstractPlayerInteraction api = chr.getAbstractPlayerInteraction();
                
                for(Integer item: exclusiveItems) {
                        api.removeAll(item);
                }
        }
        
        public final void setExclusiveItems(List<Object> items) {
                List<Integer> exclusive = convertToIntegerArray(items);
                
                wL.lock();
                try {
                        for(Integer item: exclusive) {
                                exclusiveItems.add(item);
                        }
                } finally {
                        wL.unlock();
                }
        }
        
        public final void setEventRewards(List<Object> rwds, List<Object> qtys, int expGiven) {
                setEventRewards(1, rwds, qtys, expGiven);
        }
        
        public final void setEventRewards(List<Object> rwds, List<Object> qtys) {
                setEventRewards(1, rwds, qtys);
        }
        
        public final void setEventRewards(int eventLevel, List<Object> rwds, List<Object> qtys) {
                setEventRewards(eventLevel, rwds, qtys, 0);
        }
        
        public final void setEventRewards(int eventLevel, List<Object> rwds, List<Object> qtys, int expGiven) {
                // fixed EXP will be rewarded at the same time the random item is given

                if(eventLevel <= 0 || eventLevel > YamlConfig.config.server.MAX_EVENT_LEVELS) return;
                eventLevel--;    //event level starts from 1

                List<Integer> rewardIds = convertToIntegerArray(rwds);
                List<Integer> rewardQtys = convertToIntegerArray(qtys);

                //rewardsSet and rewardsQty hold temporary values
                wL.lock();
                try {
                        collectionSet.put(eventLevel, rewardIds);
                        collectionQty.put(eventLevel, rewardQtys);
                        collectionExp.put(eventLevel, expGiven);
                } finally {
                        wL.unlock();
                }
        }
        
        private byte getRewardListRequirements(int level) {
                if(level >= collectionSet.size()) return 0;

                byte rewardTypes = 0;
                List<Integer> list = collectionSet.get(level);

                for (Integer itemId : list) {
                        rewardTypes |= (1 << ItemConstants.getInventoryType(itemId).getType());
                }

                return rewardTypes;
        }
        
        private boolean hasRewardSlot(MapleCharacter player, int eventLevel) {
                byte listReq = getRewardListRequirements(eventLevel);   //gets all types of items present in the event reward list

                //iterating over all valid inventory types
                for(byte type = 1; type <= 5; type++) {
                        if((listReq >> type) % 2 == 1 && !player.hasEmptySlot(type))
                                return false;
                }

                return true;
        }
        
        public final boolean giveEventReward(MapleCharacter player) {
                return giveEventReward(player, 1);
        }
        
        //gives out EXP & a random item in a similar fashion of when clearing KPQ, LPQ, etc.
        public final boolean giveEventReward(MapleCharacter player, int eventLevel) {
                List<Integer> rewardsSet, rewardsQty;
                Integer rewardExp;
            
                rL.lock();
                try {
                        eventLevel--;       //event level starts counting from 1
                        if(eventLevel >= collectionSet.size()) return true;

                        rewardsSet = collectionSet.get(eventLevel);
                        rewardsQty = collectionQty.get(eventLevel);

                        rewardExp = collectionExp.get(eventLevel);
                } finally {
                        rL.unlock();
                }
                        
                if(rewardExp == null) rewardExp = 0;

                if(rewardsSet == null || rewardsSet.isEmpty()) {
                        if(rewardExp > 0) player.gainExp(rewardExp);
                        return true;
                }

                if(!hasRewardSlot(player, eventLevel)) return false;

                AbstractPlayerInteraction api = player.getAbstractPlayerInteraction();
                int rnd = (int)Math.floor(Math.random() * rewardsSet.size());

                api.gainItem(rewardsSet.get(rnd), rewardsQty.get(rnd).shortValue());
                if(rewardExp > 0) player.gainExp(rewardExp);
                return true;
        }
        
        private void disposeExpedition() {
                if (expedition != null) {
                        expedition.dispose(eventCleared);
                        
                        sL.lock();
                        try {
                                expedition.removeChannelExpedition(em.getChannelServer());
                        } finally {
                                sL.unlock();
                        }
                        
                        expedition = null;
                }
        }
        
        public final synchronized void startEvent() {
                eventStarted = true;
                
                try {
                        invokeScriptFunction("afterSetup", EventInstanceManager.this);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
        }
        
        public final void setEventCleared() {
                eventCleared = true;
                
                for (MapleCharacter chr : getPlayers()) {
                        chr.awardQuestPoint(YamlConfig.config.server.QUEST_POINT_PER_EVENT_CLEAR);
                }
                
                sL.lock();
                try {
                        em.disposeInstance(name);
                } finally {
                        sL.unlock();
                }
                
                disposeExpedition();
        }
        
        public final boolean isEventCleared() {
                return eventCleared;
        }
        
        public final boolean isEventDisposed() {
                return disposed;
        }
        
        private boolean isEventTeamLeaderOn() {
                for(MapleCharacter chr: getPlayers()) {
                        if(chr.getId() == getLeaderId()) return true;
                }
                
                return false;
        }
        
        public final boolean checkEventTeamLacking(boolean leavingEventMap, int minPlayers) {
                if(eventCleared && getPlayerCount() > 1) return false;
                
                if(!eventCleared && leavingEventMap && !isEventTeamLeaderOn()) return true;
                if(getPlayerCount() < minPlayers) return true;
                
                return false;
        }
        
        public final boolean isExpeditionTeamLackingNow(boolean leavingEventMap, int minPlayers, MapleCharacter quitter) {
                if(eventCleared) {
                        if(leavingEventMap && getPlayerCount() <= 1) return true;
                } else {
                        // thanks Conrad for noticing expeditions don't need to have neither the leader nor meet the minimum requirement inside the event
                        if(getPlayerCount() <= 1) return true;
                }
                
                return false;
        }
        
        public final boolean isEventTeamLackingNow(boolean leavingEventMap, int minPlayers, MapleCharacter quitter) {
                if(eventCleared) {
                        if(leavingEventMap && getPlayerCount() <= 1) return true;
                } else {
                        if(leavingEventMap && getLeaderId() == quitter.getId()) return true;
                        if(getPlayerCount() <= minPlayers) return true;
                }
                
                return false;
        }
        
        public final boolean isEventTeamTogether() {
                rL.lock();
                try {
                        if(chars.size() <= 1) return true;
                        
                        Iterator<MapleCharacter> iterator = chars.values().iterator();
                        MapleCharacter mc = iterator.next();
                        int mapId = mc.getMapId();
                        
                        for (; iterator.hasNext();) {
                                mc = iterator.next();
                                if(mc.getMapId() != mapId) return false;
                        }
                        
                        return true;
                } finally {
                        rL.unlock();
                }
        }
        
        public final void warpEventTeam(int warpFrom, int warpTo) {
                List<MapleCharacter> players = getPlayerList();
                
                for (MapleCharacter chr : players) {
                        if(chr.getMapId() == warpFrom)
                                chr.changeMap(warpTo);
                }
        }
        
        public final void warpEventTeam(int warpTo) {
                List<MapleCharacter> players = getPlayerList();
                
                for (MapleCharacter chr : players) {
                        chr.changeMap(warpTo);
                }
        }
        
        public final void warpEventTeamToMapSpawnPoint(int warpFrom, int warpTo, int toSp) {
                List<MapleCharacter> players = getPlayerList();
                
                for (MapleCharacter chr : players) {
                        if(chr.getMapId() == warpFrom)
                                chr.changeMap(warpTo, toSp);
                }
        }
        
        public final void warpEventTeamToMapSpawnPoint(int warpTo, int toSp) {
                List<MapleCharacter> players = getPlayerList();
                
                for (MapleCharacter chr : players) {
                        chr.changeMap(warpTo, toSp);
                }
        }
        
        public final int getLeaderId() {
                rL.lock();
                try {
                        return leaderId;
                } finally {
                        rL.unlock();
                }
        }
        
        public MapleCharacter getLeader() {
                rL.lock();
                try {
                        return chars.get(leaderId);
                } finally {
                        rL.unlock();
                }
        }
        
        public final void setLeader(MapleCharacter chr) {
                wL.lock();
                try {
                        leaderId = chr.getId();
                } finally {
                        wL.unlock();
                }
        }
        
        public final void showWrongEffect() {
                showWrongEffect(getLeader().getMapId());
        }
        
        public final void showWrongEffect(int mapId) {
                MapleMap map = getMapInstance(mapId);
                map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/wrong_kor"));
                map.broadcastMessage(MaplePacketCreator.playSound("Party1/Failed"));
        }
        
        public final void showClearEffect() {
                showClearEffect(false);
        }
        
        public final void showClearEffect(boolean hasGate) {
                MapleCharacter leader = getLeader();
                if(leader != null) showClearEffect(hasGate, leader.getMapId());
        }
        
        public final void showClearEffect(int mapId) {
                showClearEffect(false, mapId);
        }
        
        public final void showClearEffect(boolean hasGate, int mapId) {
                showClearEffect(hasGate, mapId, "gate", 2);
        }
        
        public final void showClearEffect(int mapId, String mapObj, int newState) {
                showClearEffect(true, mapId, mapObj, newState);
        }
        
        public final void showClearEffect(boolean hasGate, int mapId, String mapObj, int newState) {
                MapleMap map = getMapInstance(mapId);
                map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/clear"));
                map.broadcastMessage(MaplePacketCreator.playSound("Party1/Clear"));
                if(hasGate) {
                        map.broadcastMessage(MaplePacketCreator.environmentChange(mapObj, newState));
                        wL.lock();
                        try {
                                openedGates.put(map.getId(), new Pair<>(mapObj, newState));
                        } finally {
                                wL.unlock();
                        }
                }
        }
        
        public final void recoverOpenedGate(MapleCharacter chr, int thisMapId) {
                Pair<String, Integer> gateData = null;
            
                rL.lock();
                try {
                        if(openedGates.containsKey(thisMapId)) {
                                gateData = openedGates.get(thisMapId);
                        }
                } finally {
                        rL.unlock();
                }
                
                if(gateData != null) {
                        chr.announce(MaplePacketCreator.environmentChange(gateData.getLeft(), gateData.getRight()));
                }
        }
        
        public final void giveEventPlayersStageReward(int thisStage) {
                List<Integer> list = getClearStageBonus(thisStage);     // will give bonus exp & mesos to everyone in the event
                giveEventPlayersExp(list.get(0));
                giveEventPlayersMeso(list.get(1));
        }
        
        public final void linkToNextStage(int thisStage, String eventFamily, int thisMapId) {
                giveEventPlayersStageReward(thisStage);
                thisStage--;    //stages counts from ONE, scripts from ZERO
            
                MapleMap nextStage = getMapInstance(thisMapId);
                MaplePortal portal = nextStage.getPortal("next00");
                if (portal != null) {
                        portal.setScriptName(eventFamily + thisStage);
                }
        }
        
        public final void linkPortalToScript(int thisStage, String portalName, String scriptName, int thisMapId) {
                giveEventPlayersStageReward(thisStage);
                thisStage--;    //stages counts from ONE, scripts from ZERO
            
                MapleMap nextStage = getMapInstance(thisMapId);
                MaplePortal portal = nextStage.getPortal(portalName);
                if (portal != null) {
                        portal.setScriptName(scriptName);
                }
        }
        
        // registers a player status in an event
        public final void gridInsert(MapleCharacter chr, int newStatus) {
                wL.lock();
                try {
                        playerGrid.put(chr.getId(), newStatus);
                } finally {
                        wL.unlock();
                }
        }
        
        // unregisters a player status in an event
        public final void gridRemove(MapleCharacter chr) {
                wL.lock();
                try {
                        playerGrid.remove(chr.getId());
                } finally {
                        wL.unlock();
                }
        }
        
        // checks a player status
        public final int gridCheck(MapleCharacter chr) {
                rL.lock();
                try {
                        Integer i = playerGrid.get(chr.getId());
                        return (i != null) ? i : -1;
                } finally {
                        rL.unlock();
                }
        }
        
        public final int gridSize() {
                rL.lock();
                try {
                        return playerGrid.size();
                } finally {
                        rL.unlock();
                }
        }
        
        public final void gridClear() {
                wL.lock();
                try {
                        playerGrid.clear();
                } finally {
                        wL.unlock();
                }
        }
        
        public boolean activatedAllReactorsOnMap(int mapId, int minReactorId, int maxReactorId) {
                return activatedAllReactorsOnMap(this.getMapInstance(mapId), minReactorId, maxReactorId);
        }
        
        public boolean activatedAllReactorsOnMap(MapleMap map, int minReactorId, int maxReactorId) {
                if(map == null) return true;
            
                for(MapleReactor mr : map.getReactorsByIdRange(minReactorId, maxReactorId)) {
                        if(mr.getReactorType() != -1) {
                                return false;
                        }
                }

                return true;
        }
}
