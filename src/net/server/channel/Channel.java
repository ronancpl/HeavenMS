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
package net.server.channel;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import tools.locks.MonitoredReentrantLock;
import tools.locks.MonitoredReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import net.MapleServerHandler;
import net.mina.MapleCodecFactory;
import net.server.PlayerStorage;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import provider.MapleDataProviderFactory;
import scripting.event.EventScriptManager;
import server.TimerManager;
import server.events.gm.MapleEvent;
import server.expeditions.MapleExpedition;
import server.expeditions.MapleExpeditionType;
import server.maps.MapleHiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMiniDungeon;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import constants.ServerConstants;
import server.maps.MapleMiniDungeonInfo;
import tools.locks.MonitoredLockType;

public final class Channel {

    private int port = 7575;
    private PlayerStorage players = new PlayerStorage();
    private int world, channel;
    private IoAcceptor acceptor;
    private String ip, serverMessage;
    private MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private Map<Integer, MapleHiredMerchant> hiredMerchants = new HashMap<>();
    private final Map<Integer, Integer> storedVars = new HashMap<>();
    private List<MapleExpedition> expeditions = new ArrayList<>();
    private List<MapleExpeditionType> expedType = new ArrayList<>();
    private MapleEvent event;
    private boolean finishedShutdown = false;
    private int usedDojo = 0;
    private int[] dojoStage;
    private long[] dojoFinishTime;
    private ScheduledFuture<?>[] dojoTask;
    private Map<Integer, Integer> dojoParty = new HashMap<>();
    private Map<Integer, MapleMiniDungeon> dungeons = new HashMap<>();
    
    private ReentrantReadWriteLock merchantLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.MERCHANT, true);
    private ReadLock merchRlock = merchantLock.readLock();
    private WriteLock merchWlock = merchantLock.writeLock();
    
    private Lock lock = new MonitoredReentrantLock(MonitoredLockType.CHANNEL, true);
    
    public Channel(final int world, final int channel) {
        this.world = world;
        this.channel = channel;
        this.mapFactory = new MapleMapFactory(null, MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")), world, channel);
        try {
            eventSM = new EventScriptManager(this, getEvents());
            port = 7575 + this.channel - 1;
            port += (world * 100);
            ip = ServerConstants.HOST + ":" + port;
            IoBuffer.setUseDirectBuffer(false);
            IoBuffer.setAllocator(new SimpleBufferAllocator());
            acceptor = new NioSocketAcceptor();
            TimerManager.getInstance().register(new respawnMaps(), ServerConstants.RESPAWN_INTERVAL);
            acceptor.setHandler(new MapleServerHandler(world, channel));
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
            acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
            acceptor.bind(new InetSocketAddress(port));
            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
            for (MapleExpeditionType exped : MapleExpeditionType.values()) {
            	expedType.add(exped);
            }
            eventSM.init();
            
            dojoStage = new int[20];
            dojoFinishTime = new long[20];
            dojoTask = new ScheduledFuture<?>[20];
            for(int i = 0; i < 20; i++) {
                dojoStage[i] = 0;
                dojoFinishTime[i] = 0;
                dojoTask[i] = null;
            }
            
            System.out.println("    Channel " + getId() + ": Listening on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void reloadEventScriptManager(){
    	eventSM.cancel();
    	eventSM = null;
    	eventSM = new EventScriptManager(this, getEvents());
    	eventSM.init();
    }

    public final void shutdown() {
        try {
            System.out.println("Shutting down Channel " + channel + " on World " + world);
            
            closeAllMerchants();
            players.disconnectAll();
            acceptor.unbind();
            
            finishedShutdown = true;
            System.out.println("Successfully shut down Channel " + channel + " on World " + world + "\r\n");          
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while shutting down Channel " + channel + " on World " + world + "\r\n" + e);
        }
    }

    public void closeAllMerchants() {
        merchWlock.lock();
        try {
            final Iterator<MapleHiredMerchant> hmit = hiredMerchants.values().iterator();
            while (hmit.hasNext()) {
                hmit.next().forceClose();
                hmit.remove();
            }
        } catch (Exception e) {
		e.printStackTrace();
        } finally {
                merchWlock.unlock();
        }
    }
    
    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public int getWorld() {
        return world;
    }

    public void addPlayer(MapleCharacter chr) {
        players.addPlayer(chr);
        chr.announce(MaplePacketCreator.serverMessage(serverMessage));
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public void removePlayer(MapleCharacter chr) {
        players.removePlayer(chr.getId());
    }
    
    public int getChannelCapacity() {
        return (int)(Math.ceil(((float) players.getAllCharacters().size() / ServerConstants.CHANNEL_LOAD) * 800));
    }

    public void broadcastPacket(final byte[] data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            chr.announce(data);
        }
    }
    
    public final int getId() {
        return channel;
    }

    public String getIP() {
        return ip;
    }

    public MapleEvent getEvent() {
        return event;
    }

    public void setEvent(MapleEvent event) {
        this.event = event;
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public void broadcastGMPacket(final byte[] data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr.isGM()) {
                chr.announce(data);
            }
        }
    }

    public List<MapleCharacter> getPartyMembers(MapleParty party) {
        List<MapleCharacter> partym = new ArrayList<>(8);
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar.getChannel() == getId()) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    partym.add(chr);
                }
            }
        }
        return partym;
    }
        
    public class respawnMaps implements Runnable {

        @Override
        public void run() {
            for (MapleMap map : mapFactory.getMaps().values()) {
                map.respawn();
            }
        }
    }

    public Map<Integer, MapleHiredMerchant> getHiredMerchants() {
        merchRlock.lock();
        try {
            return Collections.unmodifiableMap(hiredMerchants);
        } finally {
            merchRlock.unlock();
        }
    }

    public void addHiredMerchant(int chrid, MapleHiredMerchant hm) {
        merchWlock.lock();
        try {
            hiredMerchants.put(chrid, hm);
        } finally {
            merchWlock.unlock();
        }
    }

    public void removeHiredMerchant(int chrid) {
        merchWlock.lock();
        try {        
            hiredMerchants.remove(chrid);
        } finally {
            merchWlock.unlock();
        }
    }

    public int[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<Integer> ret = new ArrayList<>(characterIds.length);
        PlayerStorage playerStorage = getPlayerStorage();
        for (int characterId : characterIds) {
            MapleCharacter chr = playerStorage.getCharacterById(characterId);
            if (chr != null) {
                if (chr.getBuddylist().containsVisible(charIdFrom)) {
                    ret.add(characterId);
                }
            }
        }
        int[] retArr = new int[ret.size()];
        int pos = 0;
        for (Integer i : ret) {
            retArr[pos++] = i.intValue();
        }
        return retArr;
    }
    
    public List<MapleExpedition> getExpeditions() {
    	return expeditions;
    }
    
    public boolean isConnected(String name) {
        return getPlayerStorage().getCharacterByName(name) != null;
    }
    
    public boolean finishedShutdown() {
        return finishedShutdown;
    }
    
    public void setServerMessage(String message) {
        this.serverMessage = message;
        broadcastPacket(MaplePacketCreator.serverMessage(message));
    }
    
    private static String [] getEvents(){
    	List<String> events = new ArrayList<String>();
    	for (File file : new File("scripts/event").listFiles()){
            events.add(file.getName().substring(0, file.getName().length() - 3));
    	}
    	return events.toArray(new String[0]);
    }
	
    public int getStoredVar(int key) {
        if(storedVars.containsKey(key)) {
            return storedVars.get(key);
        }

        return 0;
    }
    
    public void setStoredVar(int key, int val) {
        this.storedVars.put(key, val);
    }
    
    public synchronized int lookupPartyDojo(MapleParty party) {
        if(party == null) return -1;
        
        Integer i = dojoParty.get(party.hashCode());
        return (i != null) ? i : -1;
    }
    
    public int getAvailableDojo(boolean isPartyDojo) {
        return getAvailableDojo(isPartyDojo, null);
    }
    
    public synchronized int getAvailableDojo(boolean isPartyDojo, MapleParty party) {
        int dojoList = this.usedDojo;
        int range, slot = 0;
        
        if(!isPartyDojo) {
            dojoList = dojoList >> 5;
            range = 15;
        } else {
            range = 5;
        }
        
        while((dojoList & 1) != 0) {
            dojoList = (dojoList >> 1);
            slot++;
        }
        
        if(slot < range) {
            if(party != null) {
                if(dojoParty.containsKey(party.hashCode())) return -2;
                dojoParty.put(party.hashCode(), slot);
            }
                
            this.usedDojo |= (1 << ((!isPartyDojo ? 5 : 0) + slot));
            return slot;
        } else {
            return -1;
        }
    }
    
    private void freeDojoSlot(int slot, MapleParty party) {
        int mask = 0b11111111111111111111;
        mask ^= (1 << slot);
        
        usedDojo &= mask;
        if(party != null) {
            if(dojoParty.remove(party.hashCode()) != null) return;
        }
        
        if(dojoParty.containsValue(slot)) {    // strange case, no party there!
            Set<Entry<Integer, Integer>> es = Collections.unmodifiableSet(dojoParty.entrySet());
            
            for(Entry<Integer, Integer> e: es) {
                if(e.getValue() == slot) {
                    dojoParty.remove(e.getKey());
                    break;
                }
            }
        }
    }
    
    private int getDojoSlot(int dojoMapId) {
        return (dojoMapId % 100) + ((dojoMapId / 10000 == 92502) ? 5 : 0);
    }
    
    public void resetDojoMap(int fromMapId) {
        for(int i = 0; i < (((fromMapId / 100) % 100 <= 36) ? 5 : 2); i++) {
            this.getMapFactory().getMap(fromMapId + (100 * i)).resetMapObjects();
        }
    }
    
    public void resetDojo(int dojoMapId) {
        resetDojo(dojoMapId, 0);
    }
    
    public void resetDojo(int dojoMapId, int thisStg) {
        int slot = getDojoSlot(dojoMapId);
        this.dojoStage[slot] = thisStg;
        
        if(this.dojoTask[slot] != null) {
            this.dojoTask[slot].cancel(false);
            this.dojoTask[slot] = null;
        }
    }
    
    public void freeDojoSectionIfEmpty(int dojoMapId) {
            final int slot = getDojoSlot(dojoMapId);
            final int delta = (dojoMapId) % 100;
            final int stage = (dojoMapId / 100) % 100;
            final int dojoBaseMap = (dojoMapId >= 925030000) ? 925030000 : 925020000;

            for (int i = 0; i < 5; i++) { //only 32 stages, but 38 maps
                MapleMap dojoMap = getMapFactory().getMap(dojoBaseMap + (100 * (stage + i)) + delta);
                if(!dojoMap.getAllPlayers().isEmpty()) return;
            }
            
            freeDojoSlot(slot, null);
    }
    
    public void startDojoSchedule(final int dojoMapId) {
        final int slot = getDojoSlot(dojoMapId);
        final int stage = (dojoMapId / 100) % 100;
        if(stage <= dojoStage[slot]) return;
        
        long clockTime = (stage > 36 ? 15 : (stage / 6) + 5) * 60000;
        this.dojoTask[slot] = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                final int delta = (dojoMapId) % 100;
                final int dojoBaseMap = (slot < 5) ? 925030000 : 925020000;
                MapleParty party = null;
                
                for (int i = 0; i < 5; i++) { //only 32 stages, but 38 maps
                    for(MapleCharacter chr: getMapFactory().getMap(dojoBaseMap + (100 * (stage + i)) + delta).getAllPlayers()) {
                        if(chr.getMap().isDojoMap()) {
                            chr.timeoutFromDojo();
                        }
                        party = chr.getParty();
                    }
                }
                
                freeDojoSlot(slot, party);
            }
        }, clockTime + 3000);   // let the TIMES UP display for 3 seconds, then warp
        
        dojoFinishTime[slot] = System.currentTimeMillis() + clockTime;
    }
    
    public void dismissDojoSchedule(int dojoMapId, MapleParty party) {
        int slot = getDojoSlot(dojoMapId);
        int stage = (dojoMapId / 100) % 100;
        if(stage <= dojoStage[slot]) return;
        
        if(this.dojoTask[slot] != null) {
            this.dojoTask[slot].cancel(false);
            this.dojoTask[slot] = null;
        }
        
        freeDojoSlot(slot, party);
    }
    
    public boolean setDojoProgress(int dojoMapId) {
        int slot = getDojoSlot(dojoMapId);
        int dojoStg = (dojoMapId / 100) % 100;
        
        if(this.dojoStage[slot] < dojoStg) {
            this.dojoStage[slot] = dojoStg;
            return true;
        } else {
            return false;
        }
    }
    
    public long getDojoFinishTime(int dojoMapId) {
        return dojoFinishTime[getDojoSlot(dojoMapId)];
    }
    
    public boolean addMiniDungeon(int dungeonid) {
        lock.lock();
        try {
            if(dungeons.containsKey(dungeonid)) return false;
            
            MapleMiniDungeonInfo mmdi = MapleMiniDungeonInfo.getDungeon(dungeonid);
            MapleMiniDungeon mmd = new MapleMiniDungeon(mmdi.getBase(), 30);    // all minidungeons timeout on 30 mins
            
            dungeons.put(dungeonid, mmd);
            return true;
        } finally {
            lock.unlock();
        }
    }
    
    public MapleMiniDungeon getMiniDungeon(int dungeonid) {
        lock.lock();
        try {
            return dungeons.get(dungeonid);
        } finally {
            lock.unlock();
        }
    }
    
    public void removeMiniDungeon(int dungeonid) {
        lock.lock();
        try {
            dungeons.remove(dungeonid);
        } finally {
            lock.unlock();
        }
    }
}