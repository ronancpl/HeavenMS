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

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.script.ScriptException;

import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import provider.MapleDataProviderFactory;
import server.MaplePortal;
import server.TimerManager;
import server.expeditions.MapleExpedition;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.DatabaseConnection;
import client.MapleCharacter;
import constants.ItemConstants;
import constants.ServerConstants;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import scripting.AbstractPlayerInteraction;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze, Ronan
 */
public class EventInstanceManager {
	private List<MapleCharacter> chars = new ArrayList<>();
	private List<MapleMonster> mobs = new LinkedList<>();
	private Map<MapleCharacter, Integer> killCount = new HashMap<>();
	private EventManager em;
	private MapleMapFactory mapFactory;
	private String name;
	private Properties props = new Properties();
	private long timeStarted = 0;
	private long eventTime = 0;
	private MapleExpedition expedition = null;
        private List<Integer> mapIds = new LinkedList<Integer>();
        private List<Boolean> isInstanced = new LinkedList<Boolean>();
        private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
        private final ReadLock rL = mutex.readLock();
        private final WriteLock wL = mutex.writeLock();
        private ScheduledFuture<?> event_schedule = null;
        private boolean disposed = false;
        private boolean eventCleared = false;
        
        // multi-leveled PQ rewards!
        private Map<Integer, List<Integer>> collectionSet = new HashMap<>(ServerConstants.MAX_EVENT_LEVELS);
        private Map<Integer, List<Integer>> collectionQty = new HashMap<>(ServerConstants.MAX_EVENT_LEVELS);
        private Map<Integer, Integer> collectionExp = new HashMap<>(ServerConstants.MAX_EVENT_LEVELS);
        
        // Exp/Meso rewards by CLEAR on a stage
        private List<Integer> onMapClearExp = new ArrayList<>();
        private List<Integer> onMapClearMeso = new ArrayList<>();
        
        // registers player status on an event (null on this Map structure equals to 0)
        private Map<Integer, Integer> playerGrid = new HashMap<>();
        
        // registers all opened gates on the event. Will help late characters to encounter next stages gates already opened
        private Set<Integer> openedGates = new HashSet<>();
        
	public EventInstanceManager(EventManager em, String name) {
		this.em = em;
		this.name = name;
		mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")), (byte) 0, (byte) 1);//Fk this
		mapFactory.setChannel(em.getChannelServer().getId());
	}

	public EventManager getEm() {
		return em;
	}
        
        public void giveEventPlayersExp(int gain) {
                giveEventPlayersExp(gain, -1);
        }
        
        public void giveEventPlayersExp(int gain, int mapId) {
                if(gain == 0) return;
            
                rL.lock();
                try {
                        if(mapId == -1) {
                                for(MapleCharacter mc: chars) {
                                        mc.gainExp(gain * mc.getExpRate(), true, true);
                                }
                        }
                        else {
                                for(MapleCharacter mc: chars) {
                                        if(mc.getMapId() == mapId) mc.gainExp(gain * mc.getExpRate(), true, true);
                                }
                        }
                } finally {
                        rL.unlock();
                }
	}
        
        public void giveEventPlayersMeso(int gain) {
                giveEventPlayersMeso(gain, -1);
        }
        
        public void giveEventPlayersMeso(int gain, int mapId) {
                if(gain == 0) return;
            
                rL.lock();
                try {
                        if(mapId == -1) {
                                for(MapleCharacter mc: chars) {
                                        mc.gainMeso(gain * mc.getMesoRate());
                                }
                        }
                        else {
                                for(MapleCharacter mc: chars) {
                                        if(mc.getMapId() == mapId) mc.gainMeso(gain * mc.getMesoRate());
                                }
                        }
                } finally {
                        rL.unlock();
                }
	}

	public void registerPlayer(MapleCharacter chr) {
		if (chr == null || !chr.isLoggedin()){
			return;
		}
                
                try {
                        wL.lock();
                        try {
                                chars.add(chr);
                        }
                        finally {
                                wL.unlock();
                        }
                        
			chr.setEventInstance(this);
			em.getIv().invokeFunction("playerEntry", this, chr);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}  
        
        public void exitPlayer(MapleCharacter chr) {
		if (chr == null || !chr.isLoggedin()){
			return;
		}
		try {
			unregisterPlayer(chr);
                        em.getIv().invokeFunction("playerExit", this, chr);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	public void startEventTimer(long time) {
                timeStarted = System.currentTimeMillis();
		eventTime = time;
                
                event_schedule = TimerManager.getInstance().schedule(new Runnable() {
                        public void run() {
                                try {
                                        em.getIv().invokeFunction("scheduledTimeout", EventInstanceManager.this);
                                        dismissEventTimer();
                                } catch (ScriptException | NoSuchMethodException ex) {
                                        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                }, time);
	}
        
        public void addEventTimer(long time) {
                if(event_schedule != null) {
                        if(event_schedule.cancel(false)) {
                                long nextTime = getTimeLeft() + time;
                                eventTime += time;

                                event_schedule = TimerManager.getInstance().schedule(new Runnable() {
                                        public void run() {
                                                try {
                                                        em.getIv().invokeFunction("scheduledTimeout", EventInstanceManager.this);
                                                        dismissEventTimer();
                                                } catch (ScriptException | NoSuchMethodException ex) {
                                                        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                        }
                                }, nextTime);
                        }
                }
                else {
                        startEventTimer(time);
                }
        }
        
        public void stopEventTimer() {
                if(event_schedule != null) {
                    event_schedule.cancel(false);
                    event_schedule = null;
                }
                dismissEventTimer();
        }
        
        private void dismissEventTimer() {
                for(MapleCharacter chr: getPlayers()) {
                    chr.getClient().getSession().write(MaplePacketCreator.removeClock());
                }
                
                event_schedule = null;
                eventTime = 0;
                timeStarted = 0;
        }
        
	public boolean isTimerStarted() {
		return eventTime > 0 && timeStarted > 0;
	}

	public long getTimeLeft() {
		return eventTime - (System.currentTimeMillis() - timeStarted);
	}

	public void registerParty(MapleParty party, MapleMap map) {
		for (MaplePartyCharacter pc : party.getEligibleMembers()) {
			MapleCharacter c = map.getCharacterById(pc.getId());
			registerPlayer(c);
		}
	}

	public void registerExpedition(MapleExpedition exped) {
		expedition = exped;
		registerPlayer(exped.getLeader());
	}

	public void unregisterPlayer(MapleCharacter chr) {
                wL.lock();
                try {
                        chars.remove(chr);
                        gridRemove(chr);
                } finally {
                        wL.unlock();
                }
            
		chr.setEventInstance(null);
	}
	
	public int getPlayerCount() {
                rL.lock();
                try {
                        return chars.size();
                }
                finally {
                        rL.unlock();
                }
	}

	public List<MapleCharacter> getPlayers() {
                rL.lock();
                try {
                        return new ArrayList<>(chars);
                }
                finally {
                        rL.unlock();
                }
	}

	public void registerMonster(MapleMonster mob) {
		if (!mob.getStats().isFriendly()) { //We cannot register moon bunny
			mobs.add(mob);
			mob.setEventInstance(this);
		}
	}

	public void movePlayer(MapleCharacter chr) {
		try {
			em.getIv().invokeFunction("moveMap", this, chr);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}
        
        public void changedMap(MapleCharacter chr, int mapId) {
		try {
			em.getIv().invokeFunction("changedMap", this, chr, mapId);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}
	
	public void monsterKilled(MapleMonster mob) {
		mobs.remove(mob);
                try {
                        em.getIv().invokeFunction("monsterKilled", mob, this);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }
		if (mobs.isEmpty()) {
			try {
				em.getIv().invokeFunction("allMonstersDead", this);
			} catch (ScriptException | NoSuchMethodException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void playerKilled(MapleCharacter chr) {
		try {
			em.getIv().invokeFunction("playerDead", this, chr);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	public boolean revivePlayer(MapleCharacter chr) {
		try {
			Object b = em.getIv().invokeFunction("playerRevive", this, chr);
			if (b instanceof Boolean) {
				return (Boolean) b;
			}
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	public void playerDisconnected(MapleCharacter chr) {
		try {
			em.getIv().invokeFunction("playerDisconnected", this, chr);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 *
	 * @param chr
	 * @param mob
	 */
	public void monsterKilled(MapleCharacter chr, MapleMonster mob) {
		try {
			Integer kc = killCount.get(chr);
			int inc = ((Double) em.getIv().invokeFunction("monsterValue", this, mob.getId())).intValue();
			if (kc == null) {
				kc = inc;
			} else {
				kc += inc;
			}
			killCount.put(chr, kc);
			if (expedition != null){
				expedition.monsterKilled(chr, mob);
			}
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	public int getKillCount(MapleCharacter chr) {
		Integer kc = killCount.get(chr);
		return (kc == null) ? 0 : kc;
	}
        
        public void cancelSchedule() {
            if(event_schedule != null) {
                event_schedule.cancel(false);
                event_schedule = null;
            }
        }

	public void dispose() {
                try {
                        em.getIv().invokeFunction("dispose", this);
                } catch (ScriptException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                }

                wL.lock();
                try {
                        chars.clear();
                } finally {
                        wL.unlock();
                }
                
                cancelSchedule();

                mobs.clear();
                killCount.clear();
                mapFactory = null;
                if (expedition != null) {
                        em.getChannelServer().getExpeditions().remove(expedition);
                }
                em.disposeInstance(name);
                em = null;
	}

	public MapleMapFactory getMapFactory() {
		return mapFactory;
	}

	public void schedule(final String methodName, long delay) {
		TimerManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
                                if(em == null) return;
                                
				try {
					em.getIv().invokeFunction(methodName, EventInstanceManager.this);
				} catch (ScriptException | NoSuchMethodException ex) {
					ex.printStackTrace();
				}
			}
		}, delay);
	}

	public String getName() {
		return name;
	}

	public void saveWinner(MapleCharacter chr) {
		try {
			try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO eventstats (event, instance, characterid, channel) VALUES (?, ?, ?, ?)")) {
				ps.setString(1, em.getName());
				ps.setString(2, getName());
				ps.setInt(3, chr.getId());
				ps.setInt(4, chr.getClient().getChannel());
				ps.executeUpdate();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public MapleMap getMapInstance(int mapId) {
		MapleMap map = mapFactory.getMap(mapId);

		if (!mapFactory.isMapLoaded(mapId)) {
			if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true")) {
				map.shuffleReactors();
			}
		}
		return map;
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	public Object setProperty(String key, String value, boolean prev) {
		return props.setProperty(key, value);
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

        public Properties getProperties() {
                return props;
        }
	
	public void leftParty(MapleCharacter chr) {
		try {
			em.getIv().invokeFunction("leftParty", this, chr);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	public void disbandParty() {
		try {
			em.getIv().invokeFunction("disbandParty", this);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	public void clearPQ() {
		try {
			em.getIv().invokeFunction("clearPQ", this);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	public void removePlayer(MapleCharacter chr) {
		try {
			em.getIv().invokeFunction("playerExit", this, chr);
		} catch (ScriptException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	public boolean isLeader(MapleCharacter chr) {
		return (chr.getParty().getLeaderId() == chr.getId());
	}
        
        public final MapleMap setInstanceMap(final int mapid) { //gets instance map from the channelserv
                if (disposed) {
                        return this.getMapFactory().getMap(mapid);
                }
                mapIds.add(mapid);
                isInstanced.add(false);
                return this.getMapFactory().getMap(mapid);
        }
        
        public final boolean disposeIfPlayerBelow(final byte size, final int towarp) {
                if (disposed) {
                        return true;
                }
                MapleMap map = null;
                if (towarp > 0) {
                        map = this.getMapFactory().getMap(towarp);
                }

                rL.lock();
                try {
                        if (chars != null && chars.size() < size) {
                                for (MapleCharacter chr : chars) {
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
                } finally {
                        rL.unlock();
                }
                return false;
        }
        
        private List<Integer> convertToIntegerArray(List<Double> list) {
                List<Integer> intList = new ArrayList<>();
                for(Double d: list) intList.add(d.intValue());

                return intList;
        }
        
        public void setEventClearStageExp(List<Double> gain) {
                onMapClearExp.clear();
                onMapClearExp.addAll(convertToIntegerArray(gain));
        }
        
        public void setEventClearStageMeso(List<Double> gain) {
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
        
        public final void setEventRewards(List<Double> rwds, List<Double> qtys, int expGiven) {
                setEventRewards(1, rwds, qtys, expGiven);
        }
        
        public final void setEventRewards(List<Double> rwds, List<Double> qtys) {
                setEventRewards(1, rwds, qtys);
        }
        
        public final void setEventRewards(int eventLevel, List<Double> rwds, List<Double> qtys) {
                setEventRewards(eventLevel, rwds, qtys, 0);
        }
        
        public final void setEventRewards(int eventLevel, List<Double> rwds, List<Double> qtys, int expGiven) {
                // fixed EXP will be rewarded at the same time the random item is given

                if(eventLevel <= 0 || eventLevel > ServerConstants.MAX_EVENT_LEVELS) return;
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
                rL.lock();
                try {
                        eventLevel--;       //event level starts counting from 1
                        if(eventLevel >= collectionSet.size()) return true;

                        List<Integer> rewardsSet = collectionSet.get(eventLevel);
                        List<Integer> rewardsQty = collectionQty.get(eventLevel);

                        Integer rewardExp = collectionExp.get(eventLevel);
                        if(rewardExp == null) rewardExp = 0;

                        if(rewardsSet == null || rewardsSet.isEmpty()) {
                                if(rewardExp > 0) player.gainExp(rewardExp);
                                return true;
                        }

                        if(!hasRewardSlot(player, eventLevel)) return false;

                        AbstractPlayerInteraction api = new AbstractPlayerInteraction(player.getClient());
                        int rnd = (int)Math.floor(Math.random() * rewardsSet.size());

                        api.gainItem(rewardsSet.get(rnd), rewardsQty.get(rnd).shortValue());
                        if(rewardExp > 0) player.gainExp(rewardExp);
                        return true;
                } finally {
                        rL.unlock();
                }
        }
        
        public final void setEventCleared() {
                eventCleared = true;
        }
        
        public final boolean isEventTeamLackingNow(boolean testLeader, int minPlayers, MapleCharacter quitter) {
                if(eventCleared && getPlayerCount() > 1) return false;
                
                if(!eventCleared && testLeader && getLeader().getId() == quitter.getId()) return true;
                if(getPlayerCount() <= minPlayers) return true;
                
                return false;
        }
        
        public final boolean isEventTeamTogether() {
                rL.lock();
                try {
                        if(chars.size() <= 1) return true;

                        int mapId = chars.get(0).getMapId();
                        for(int i = 1; i < chars.size(); i++) {
                                if(chars.get(i).getMapId() != mapId) return false;
                        }

                        return true;
                } finally {
                        rL.unlock();
                }
        }
        
        public final void warpEventTeam(int warpTo) {
                rL.lock();
                try {
                        for (MapleCharacter chr : chars) {
                                chr.changeMap(warpTo);
                        }
                } finally {
                        rL.unlock();
                }
        }
        
        public final MapleCharacter getLeader() {
                rL.lock();
                try {
                        for (MapleCharacter chr : chars) {
                                if(chr.isPartyLeader()) return chr;
                        }
                        
                        return null;
                } finally {
                        rL.unlock();
                }
        }
        
        public final void showWrongEffect() {
                MapleMap map = getMapInstance(getLeader().getMapId());
                map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/wrong_kor"));
                map.broadcastMessage(MaplePacketCreator.playSound("Party1/Failed"));
        }
        
        public final void showClearEffect() {
                showClearEffect(false);
        }
        
        public final void showClearEffect(boolean hasGate) {
                MapleMap map = getMapInstance(getLeader().getMapId());
                map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/clear"));
                map.broadcastMessage(MaplePacketCreator.playSound("Party1/Clear"));
                if(hasGate) {
                        map.broadcastMessage(MaplePacketCreator.environmentChange("gate", 2));
                        wL.lock();
                        try {
                                openedGates.add(map.getId());
                        } finally {
                                wL.unlock();
                        }
                }
        }
        
        public final void recoverOpenedGate(MapleCharacter chr, int thisMapId) {
                rL.lock();
                try {
                        if(openedGates.contains(thisMapId)) {
                                chr.announce(MaplePacketCreator.environmentChange("gate", 2));
                        }
                } finally {
                        rL.unlock();
                }
        }
        
        public final void linkToNextStage(int thisStage, String eventFamily, int thisMapId) {
                List<Integer> list = getClearStageBonus(thisStage);     // will give bonus exp & mesos to everyone in the event
                giveEventPlayersExp(list.get(0));
                giveEventPlayersMeso(list.get(1));
            
                thisStage--;    //stages counts from ONE, scripts from ZERO
            
                MapleMap nextStage = getMapInstance(thisMapId);
                MaplePortal portal = nextStage.getPortal("next00");
                if (portal != null) {
                        portal.setScriptName(eventFamily + thisStage);
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
        
        public final void gridClear() {
                wL.lock();
                try {
                        playerGrid.clear();
                } finally {
                        wL.unlock();
                }
        }
}
