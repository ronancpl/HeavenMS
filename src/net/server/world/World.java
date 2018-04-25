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
package net.server.world;

import client.BuddyList;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import client.BuddylistEntry;
import client.MapleCharacter;
import client.MapleFamily;
import constants.ServerConstants;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import tools.locks.MonitoredReentrantLock;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ScheduledFuture;

import scripting.event.EventInstanceManager;
import server.TimerManager;
import server.maps.MapleHiredMerchant;
import server.maps.MapleMap;
import server.maps.MaplePlayerShop;
import server.maps.MaplePlayerShopItem;
import server.maps.AbstractMapleMapObject;
import net.server.worker.CharacterAutosaverWorker;
import net.server.worker.MountTirednessWorker;
import net.server.worker.PetFullnessWorker;
import net.server.PlayerStorage;
import net.server.Server;
import net.server.channel.Channel;
import net.server.channel.CharacterIdChannelPair;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.locks.MonitoredLockType;

/**
 *
 * @author kevintjuh93
 * @author Ronan (thread-oriented world schedules)
 */
public class World {

    private int id, flag, exprate, droprate, mesorate, questrate;
    private String eventmsg;
    private List<Channel> channels = new ArrayList<>();
    private Map<Integer, MapleMessenger> messengers = new HashMap<>();
    private AtomicInteger runningMessengerId = new AtomicInteger();
    private Map<Integer, MapleFamily> families = new LinkedHashMap<>();
    private Map<Integer, MapleGuildSummary> gsStore = new HashMap<>();
    private PlayerStorage players = new PlayerStorage();
    
    private Set<Integer> queuedGuilds = new HashSet<>();
    
    private Map<Integer, MapleParty> parties = new HashMap<>();
    private AtomicInteger runningPartyId = new AtomicInteger();
    private Lock partyLock = new MonitoredReentrantLock(MonitoredLockType.WORLD_PARTY, true);
    
    private Map<Integer, Integer> owlSearched = new LinkedHashMap<>();
    private Lock owlLock = new MonitoredReentrantLock(MonitoredLockType.WORLD_OWL);
    
    private Lock activePetsLock = new MonitoredReentrantLock(MonitoredLockType.WORLD_PETS, true);
    private Map<Integer, Byte> activePets = new LinkedHashMap<>();
    private ScheduledFuture<?> petsSchedule;
    private long petUpdate;
    
    private Lock activeMountsLock = new MonitoredReentrantLock(MonitoredLockType.WORLD_MOUNTS, true);
    private Map<Integer, Byte> activeMounts = new LinkedHashMap<>();
    private ScheduledFuture<?> mountsSchedule;
    private long mountUpdate;
    
    private Lock activePlayerShopsLock = new MonitoredReentrantLock(MonitoredLockType.WORLD_PSHOPS, true);
    private Map<Integer, MaplePlayerShop> activePlayerShops = new LinkedHashMap<>();
    
    private Lock activeMerchantsLock = new MonitoredReentrantLock(MonitoredLockType.WORLD_MERCHS, true);
    private Map<Integer, Pair<MapleHiredMerchant, Byte>> activeMerchants = new LinkedHashMap<>();
    private long merchantUpdate;
    
    private ScheduledFuture<?> charactersSchedule;
    
    public World(int world, int flag, String eventmsg, int exprate, int droprate, int mesorate, int questrate) {
        this.id = world;
        this.flag = flag;
        this.eventmsg = eventmsg;
        this.exprate = exprate;
        this.droprate = droprate;
        this.mesorate = mesorate;
        this.questrate = questrate;
        runningPartyId.set(1);
        runningMessengerId.set(1);
        
        petUpdate = System.currentTimeMillis();
        mountUpdate = petUpdate;
        
        petsSchedule = TimerManager.getInstance().register(new PetFullnessWorker(this), 60 * 1000, 60 * 1000);
        mountsSchedule = TimerManager.getInstance().register(new MountTirednessWorker(this), 60 * 1000, 60 * 1000);
        charactersSchedule = TimerManager.getInstance().register(new CharacterAutosaverWorker(this), 60 * 60 * 1000, 60 * 60 * 1000);
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public Channel getChannel(int channel) {
        return channels.get(channel - 1);
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }

    public void removeChannel(int channel) {
        channels.remove(channel);
    }

    public void setFlag(byte b) {
        this.flag = b;
    }

    public int getFlag() {
        return flag;
    }

    public String getEventMessage() {
        return eventmsg;
    }
    
    public int getExpRate() {
        return exprate;
    }

    public void setExpRate(int exp) {
        List<MapleCharacter> list = new LinkedList<>(getPlayerStorage().getAllCharacters());
        
        for(MapleCharacter chr : list) {
            if(!chr.isLoggedin()) continue;
            chr.revertWorldRates();
	}
        this.exprate = exp;
        for(MapleCharacter chr : list) {
            if(!chr.isLoggedin()) continue;
            chr.setWorldRates();
        }
    }

    public int getDropRate() {
        return droprate;
    }

    public void setDropRate(int drop) {
        List<MapleCharacter> list = new LinkedList<>(getPlayerStorage().getAllCharacters());
        
        for(MapleCharacter chr : list) {
            if(!chr.isLoggedin()) continue;
            chr.revertWorldRates();
	}
        this.droprate = drop;
        for(MapleCharacter chr : list) {
            if(!chr.isLoggedin()) continue;
            chr.setWorldRates();
        }
    }

    public int getMesoRate() {
        return mesorate;
    }

    public void setMesoRate(int meso) {
        List<MapleCharacter> list = new LinkedList<>(getPlayerStorage().getAllCharacters());
        
        for(MapleCharacter chr : list) {
            if(!chr.isLoggedin()) continue;
            chr.revertWorldRates();
	}
        this.mesorate = meso;
        for(MapleCharacter chr : list) {
            if(!chr.isLoggedin()) continue;
            chr.setWorldRates();
        }
    }

    public int getQuestRate() {
        return questrate;
    }
    
    public void setQuestRate(int quest) {
        this.questrate = quest;
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public void removePlayer(MapleCharacter chr) {
        channels.get(chr.getClient().getChannel() - 1).removePlayer(chr);
        players.removePlayer(chr.getId());
    }
    
    public int getId() {
        return id;
    }

    public void addFamily(int id, MapleFamily f) {
        synchronized (families) {
            if (!families.containsKey(id)) {
                families.put(id, f);
            }
        }
    }

    public MapleFamily getFamily(int id) {
        synchronized (families) {
            if (families.containsKey(id)) {
                return families.get(id);
            }
            return null;
        }
    }

    public MapleGuild getGuild(MapleGuildCharacter mgc) {
        if(mgc == null) return null;
        
        int gid = mgc.getGuildId();
        MapleGuild g = Server.getInstance().getGuild(gid, mgc.getWorld(), mgc.getCharacter());
        if (gsStore.get(gid) == null) {
            gsStore.put(gid, new MapleGuildSummary(g));
        }
        return g;
    }

    public boolean isWorldCapacityFull() {
        return getWorldCapacityStatus() == 2;
    }
    
    public int getWorldCapacityStatus() {
        int worldCap = channels.size() * ServerConstants.CHANNEL_LOAD;
        int num = players.getSize();
        
        int status;
        if (num >= worldCap) {
            status = 2;
        } else if (num >= worldCap * .8) { // More than 80 percent o___o
            status = 1;
        } else {
            status = 0;
        }
        
        return status;
    }
    
    public MapleGuildSummary getGuildSummary(int gid, int wid) {
        if (gsStore.containsKey(gid)) {
            return gsStore.get(gid);
        } else {
            MapleGuild g = Server.getInstance().getGuild(gid, wid, null);
            if (g != null) {
                gsStore.put(gid, new MapleGuildSummary(g));
            }
            return gsStore.get(gid);
        }
    }

    public void updateGuildSummary(int gid, MapleGuildSummary mgs) {
        gsStore.put(gid, mgs);
    }

    public void reloadGuildSummary() {
        MapleGuild g;
        Server server = Server.getInstance();
        for (int i : gsStore.keySet()) {
            g = server.getGuild(i, getId(), null);
            if (g != null) {
                gsStore.put(i, new MapleGuildSummary(g));
            } else {
                gsStore.remove(i);
            }
        }
    }

    public void setGuildAndRank(List<Integer> cids, int guildid, int rank, int exception) {
        for (int cid : cids) {
            if (cid != exception) {
                setGuildAndRank(cid, guildid, rank);
            }
        }
    }

    public void setOfflineGuildStatus(int guildid, int guildrank, int cid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ? WHERE id = ?")) {
                ps.setInt(1, guildid);
                ps.setInt(2, guildrank);
                ps.setInt(3, cid);
                ps.execute();
            }
            
            con.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void setGuildAndRank(int cid, int guildid, int rank) {
        MapleCharacter mc = getPlayerStorage().getCharacterById(cid);
        if (mc == null) {
            return;
        }
        boolean bDifferentGuild;
        if (guildid == -1 && rank == -1) {
            bDifferentGuild = true;
        } else {
            bDifferentGuild = guildid != mc.getGuildId();
            mc.getMGC().setGuildId(guildid);
            mc.getMGC().setGuildRank(rank);
            
            if(bDifferentGuild) mc.getMGC().setAllianceRank(5);
            
            mc.saveGuildStatus();
        }
        if (bDifferentGuild) {
            mc.getMap().broadcastMessage(mc, MaplePacketCreator.removePlayerFromMap(cid), false);
            mc.getMap().broadcastMessage(mc, MaplePacketCreator.spawnPlayerMapObject(mc), false);
        }
    }

    public void changeEmblem(int gid, List<Integer> affectedPlayers, MapleGuildSummary mgs) {
        updateGuildSummary(gid, mgs);
        sendPacket(affectedPlayers, MaplePacketCreator.guildEmblemChange(gid, mgs.getLogoBG(), mgs.getLogoBGColor(), mgs.getLogo(), mgs.getLogoColor()), -1);
        setGuildAndRank(affectedPlayers, -1, -1, -1);	//respawn player
    }

    public void sendPacket(List<Integer> targetIds, final byte[] packet, int exception) {
        MapleCharacter c;
        for (int i : targetIds) {
            if (i == exception) {
                continue;
            }
            c = getPlayerStorage().getCharacterById(i);
            if (c != null) {
                c.getClient().announce(packet);
            }
        }
    }

    public boolean isGuildQueued(int guildId) {
        return queuedGuilds.contains(guildId);
    }
    
    public void putGuildQueued(int guildId) {
        queuedGuilds.add(guildId);
    }
    
    public void removeGuildQueued(int guildId) {
        queuedGuilds.remove(guildId);
    }
    
    public MapleParty createParty(MaplePartyCharacter chrfor) {
        int partyid = runningPartyId.getAndIncrement();
        MapleParty party = new MapleParty(partyid, chrfor);
        
        partyLock.lock();
        try {
            parties.put(party.getId(), party);
        } finally {
            partyLock.unlock();
        }
        
        return party;
    }

    public MapleParty getParty(int partyid) {
        partyLock.lock();
        try {
            return parties.get(partyid);
        } finally {
            partyLock.unlock();
        }
    }

    public MapleParty disbandParty(int partyid) {
        partyLock.lock();
        try {
            return parties.remove(partyid);
        } finally {
            partyLock.unlock();
        }
    }

    public void updateParty(MapleParty party, PartyOperation operation, MaplePartyCharacter target) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
            if (chr != null) {
                if (operation == PartyOperation.DISBAND) {
                    chr.setParty(null);
                    chr.setMPC(null);
                } else {
                    chr.setParty(party);
                    chr.setMPC(partychar);
                }
                chr.getClient().announce(MaplePacketCreator.updateParty(chr.getClient().getChannel(), party, operation, target));
            }
        }
        switch (operation) {
            case LEAVE:
            case EXPEL:
                MapleCharacter chr = getPlayerStorage().getCharacterByName(target.getName());
                if (chr != null) {
                    chr.getClient().announce(MaplePacketCreator.updateParty(chr.getClient().getChannel(), party, operation, target));
                    chr.setParty(null);
                    chr.setMPC(null);
                }
            default:
                break;
        }
    }

    public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) {
        MapleParty party = getParty(partyid);
        if (party == null) {
            throw new IllegalArgumentException("no party with the specified partyid exists");
        }
        switch (operation) {
            case JOIN:
                party.addMember(target);
                break;
            case EXPEL:
            case LEAVE:
                party.removeMember(target);
                break;
            case DISBAND:
                disbandParty(partyid);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                party.updateMember(target);
                break;
            case CHANGE_LEADER:
                MapleCharacter mc = party.getLeader().getPlayer();
                EventInstanceManager eim = mc.getEventInstance();
                
                if(eim != null && eim.isEventLeader(mc)) {
                    eim.changedLeader(target.getPlayer());
                }
                party.setLeader(target);
                break;
            default:
                System.out.println("Unhandled updateParty operation " + operation.name());
        }
        updateParty(party, operation, target);
    }

    public void removeMapPartyMembers(int partyid) {
        MapleParty party = getParty(partyid);
        if(party == null) return;
        
        for(MaplePartyCharacter mpc : party.getMembers()) {
            MapleCharacter mc = mpc.getPlayer();
            if(mc != null) {
                MapleMap map = mc.getMap();
                if(map != null) {
                    map.removeParty(partyid);
                }
            }
        }
    }
    
    public int find(String name) {
        int channel = -1;
        MapleCharacter chr = getPlayerStorage().getCharacterByName(name);
        if (chr != null) {
            channel = chr.getClient().getChannel();
        }
        return channel;
    }

    public int find(int id) {
        int channel = -1;
        MapleCharacter chr = getPlayerStorage().getCharacterById(id);
        if (chr != null) {
            channel = chr.getClient().getChannel();
        }
        return channel;
    }

    public void partyChat(MapleParty party, String chattext, String namefrom) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (!(partychar.getName().equals(namefrom))) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    chr.getClient().announce(MaplePacketCreator.multiChat(namefrom, chattext, 1));
                }
            }
        }
    }

    public void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) {
        PlayerStorage playerStorage = getPlayerStorage();
        for (int characterId : recipientCharacterIds) {
            MapleCharacter chr = playerStorage.getCharacterById(characterId);
            if (chr != null) {
                if (chr.getBuddylist().containsVisible(cidFrom)) {
                    chr.getClient().announce(MaplePacketCreator.multiChat(nameFrom, chattext, 0));
                }
            }
        }
    }

    public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<CharacterIdChannelPair> foundsChars = new ArrayList<>(characterIds.length);
        for (Channel ch : getChannels()) {
            for (int charid : ch.multiBuddyFind(charIdFrom, characterIds)) {
                foundsChars.add(new CharacterIdChannelPair(charid, ch.getId()));
            }
        }
        return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
    }

    public MapleMessenger getMessenger(int messengerid) {
        return messengers.get(messengerid);
    }

    public void leaveMessenger(int messengerid, MapleMessengerCharacter target) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        int position = messenger.getPositionByName(target.getName());
        messenger.removeMember(target);
        removeMessengerPlayer(messenger, position);
    }

    public void messengerInvite(String sender, int messengerid, String target, int fromchannel) {
        if (isConnected(target)) {
            MapleMessenger messenger = getPlayerStorage().getCharacterByName(target).getMessenger();
            if (messenger == null) {
                getPlayerStorage().getCharacterByName(target).getClient().announce(MaplePacketCreator.messengerInvite(sender, messengerid));
                MapleCharacter from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(sender);
                from.getClient().announce(MaplePacketCreator.messengerNote(target, 4, 1));
            } else {
                MapleCharacter from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(sender);
                from.getClient().announce(MaplePacketCreator.messengerChat(sender + " : " + target + " is already using Maple Messenger"));
            }
        }
    }

    public void addMessengerPlayer(MapleMessenger messenger, String namefrom, int fromchannel, int position) {
    	for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
    		MapleCharacter chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
    		if(chr == null){
    			continue;
    		}
    		if (!messengerchar.getName().equals(namefrom)) {
    			MapleCharacter from = getChannel(fromchannel).getPlayerStorage().getCharacterByName(namefrom);
    			chr.getClient().announce(MaplePacketCreator.addMessengerPlayer(namefrom, from, position, (byte) (fromchannel - 1)));
    			from.getClient().announce(MaplePacketCreator.addMessengerPlayer(chr.getName(), chr, messengerchar.getPosition(), (byte) (messengerchar.getChannel() - 1)));           
    		} else {
    			chr.getClient().announce(MaplePacketCreator.joinMessenger(messengerchar.getPosition()));
    		}
    	}
    }

    public void removeMessengerPlayer(MapleMessenger messenger, int position) {
        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            MapleCharacter chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
            if (chr != null) {
                chr.getClient().announce(MaplePacketCreator.removeMessengerPlayer(position));
            }
        }
    }

    public void messengerChat(MapleMessenger messenger, String chattext, String namefrom) {
    	String from = "";
    	String to1 = "";
    	String to2 = "";
        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            if (!(messengerchar.getName().equals(namefrom))) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(messengerchar.getName());
                if (chr != null) {
                    chr.getClient().announce(MaplePacketCreator.messengerChat(chattext));
                    if (to1.equals("")){
                    	to1 = messengerchar.getName();
                    } else if (to2.equals("")){
                    	to2 = messengerchar.getName();
                    } 
                }
            } else {
            	from = messengerchar.getName();
            }
        }
    }

    public void declineChat(String target, String namefrom) {
        if (isConnected(target)) {
            MapleCharacter chr = getPlayerStorage().getCharacterByName(target);
            if (chr != null && chr.getMessenger() != null) {
                chr.getClient().announce(MaplePacketCreator.messengerNote(namefrom, 5, 0));
            }
        }
    }

    public void updateMessenger(int messengerid, String namefrom, int fromchannel) {
        MapleMessenger messenger = getMessenger(messengerid);
        int position = messenger.getPositionByName(namefrom);
        updateMessenger(messenger, namefrom, position, fromchannel);
    }

    public void updateMessenger(MapleMessenger messenger, String namefrom, int position, int fromchannel) {
        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            Channel ch = getChannel(fromchannel);
            if (!(messengerchar.getName().equals(namefrom))) {
                MapleCharacter chr = ch.getPlayerStorage().getCharacterByName(messengerchar.getName());
                if (chr != null) {
                    chr.getClient().announce(MaplePacketCreator.updateMessengerPlayer(namefrom, getChannel(fromchannel).getPlayerStorage().getCharacterByName(namefrom), position, (byte) (fromchannel - 1)));
                }
            }
        }
    }

    public void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.addMember(target, target.getPosition());
    }

    public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.addMember(target, target.getPosition());
        addMessengerPlayer(messenger, from, fromchannel, target.getPosition());
    }

    public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.addMember(target, position);
    }

    public MapleMessenger createMessenger(MapleMessengerCharacter chrfor) {
        int messengerid = runningMessengerId.getAndIncrement();
        MapleMessenger messenger = new MapleMessenger(messengerid, chrfor);
        messengers.put(messenger.getId(), messenger);
        return messenger;
    }

    public boolean isConnected(String charName) {
        return getPlayerStorage().getCharacterByName(charName) != null;
    }

    public void whisper(String sender, String target, int channel, String message) {
        if (isConnected(target)) {
            getPlayerStorage().getCharacterByName(target).getClient().announce(MaplePacketCreator.getWhisper(sender, channel, message));
        }
    }

    public BuddyAddResult requestBuddyAdd(String addName, int channelFrom, int cidFrom, String nameFrom) {
        MapleCharacter addChar = getPlayerStorage().getCharacterByName(addName);
        if (addChar != null) {
            BuddyList buddylist = addChar.getBuddylist();
            if (buddylist.isFull()) {
                return BuddyAddResult.BUDDYLIST_FULL;
            }
            if (!buddylist.contains(cidFrom)) {
                buddylist.addBuddyRequest(addChar.getClient(), cidFrom, nameFrom, channelFrom);
            } else if (buddylist.containsVisible(cidFrom)) {
                return BuddyAddResult.ALREADY_ON_LIST;
            }
        }
        return BuddyAddResult.OK;
    }

    public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation operation) {
        MapleCharacter addChar = getPlayerStorage().getCharacterById(cid);
        if (addChar != null) {
            BuddyList buddylist = addChar.getBuddylist();
            switch (operation) {
                case ADDED:
                    if (buddylist.contains(cidFrom)) {
                        buddylist.put(new BuddylistEntry(name, "Default Group", cidFrom, channel, true));
                        addChar.getClient().announce(MaplePacketCreator.updateBuddyChannel(cidFrom, (byte) (channel - 1)));
                    }
                    break;
                case DELETED:
                    if (buddylist.contains(cidFrom)) {
                        buddylist.put(new BuddylistEntry(name, "Default Group", cidFrom, (byte) -1, buddylist.get(cidFrom).isVisible()));
                        addChar.getClient().announce(MaplePacketCreator.updateBuddyChannel(cidFrom, (byte) -1));
                    }
                    break;
            }
        }
    }

    public void loggedOff(String name, int characterId, int channel, int[] buddies) {
        updateBuddies(characterId, channel, buddies, true);
    }

    public void loggedOn(String name, int characterId, int channel, int buddies[]) {
        updateBuddies(characterId, channel, buddies, false);
    }

    private void updateBuddies(int characterId, int channel, int[] buddies, boolean offline) {
        PlayerStorage playerStorage = getPlayerStorage();
        for (int buddy : buddies) {
            MapleCharacter chr = playerStorage.getCharacterById(buddy);
            if (chr != null) {
                BuddylistEntry ble = chr.getBuddylist().get(characterId);
                if (ble != null && ble.isVisible()) {
                    int mcChannel;
                    if (offline) {
                        ble.setChannel((byte) -1);
                        mcChannel = -1;
                    } else {
                        ble.setChannel(channel);
                        mcChannel = (byte) (channel - 1);
                    }
                    chr.getBuddylist().put(ble);
                    chr.getClient().announce(MaplePacketCreator.updateBuddyChannel(ble.getCharacterId(), mcChannel));
                }
            }
        }
    }
    
    private static Integer getPetKey(MapleCharacter chr, byte petSlot) {    // assuming max 3 pets
        return (chr.getId() << 2) + petSlot;
    }
    
    public void addOwlItemSearch(Integer itemid) {
        owlLock.lock();
        try {
            Integer cur = owlSearched.get(itemid);
            if(cur != null) {
                owlSearched.put(itemid, cur + 1);
            } else {
                owlSearched.put(itemid, 1);
            }
        } finally {
            owlLock.unlock();
        }
    }
    
    public List<Pair<Integer, Integer>> getOwlSearchedItems() {
        if(ServerConstants.USE_ENFORCE_OWL_SUGGESTIONS) {
            return new ArrayList<>(0);
        }
        
        owlLock.lock();
        try {
            List<Pair<Integer, Integer>> searchCounts = new ArrayList<>(owlSearched.size());
            
            for(Entry<Integer, Integer> e : owlSearched.entrySet()) {
                searchCounts.add(new Pair<>(e.getKey(), e.getValue()));
            }
            
            return searchCounts;
        } finally {
            owlLock.unlock();
        }
    }
    
    public void registerPetHunger(MapleCharacter chr, byte petSlot) {
        if(chr.isGM() && ServerConstants.GM_PETS_NEVER_HUNGRY || ServerConstants.PETS_NEVER_HUNGRY) {
            return;
        }
        
        Integer key = getPetKey(chr, petSlot);
        
        activePetsLock.lock();
        try {
            byte initProc;
            if(System.currentTimeMillis() - petUpdate > 55000) initProc = ServerConstants.PET_EXHAUST_COUNT - 2;
            else initProc = ServerConstants.PET_EXHAUST_COUNT - 1;
            
            activePets.put(key, initProc);
        } finally {
            activePetsLock.unlock();
        }
    }
    
    public void unregisterPetHunger(MapleCharacter chr, byte petSlot) {
        Integer key = getPetKey(chr, petSlot);
        
        activePetsLock.lock();
        try {
            activePets.remove(key);
        } finally {
            activePetsLock.unlock();
        }
    }
    
    public void runPetSchedule() {
        Map<Integer, Byte> deployedPets;
        
        activePetsLock.lock();
        try {
            petUpdate = System.currentTimeMillis();
            deployedPets = Collections.unmodifiableMap(activePets);
        } finally {
            activePetsLock.unlock();
        }
        
        for(Map.Entry<Integer, Byte> dp: deployedPets.entrySet()) {
            MapleCharacter chr = this.getPlayerStorage().getCharacterById(dp.getKey() / 4);
            if(chr == null || !chr.isLoggedin() || chr.isAwayFromWorld()) continue;
            
            Byte dpVal = (byte)(dp.getValue() + 1);
            if(dpVal == ServerConstants.PET_EXHAUST_COUNT) {
                chr.runFullnessSchedule(dp.getKey() % 4);
                dpVal = 0;
            }
            
            activePetsLock.lock();
            try {
                activePets.put(dp.getKey(), dpVal);
            } finally {
                activePetsLock.unlock();
            }
        }
    }
    
    public void registerMountHunger(MapleCharacter chr) {
        if(chr.isGM() && ServerConstants.GM_PETS_NEVER_HUNGRY || ServerConstants.PETS_NEVER_HUNGRY) {
            return;
        }
        
        Integer key = chr.getId();
        activeMountsLock.lock();
        try {
            byte initProc;
            if(System.currentTimeMillis() - mountUpdate > 45000) initProc = ServerConstants.MOUNT_EXHAUST_COUNT - 2;
            else initProc = ServerConstants.MOUNT_EXHAUST_COUNT - 1;
            
            activeMounts.put(key, initProc);
        } finally {
            activeMountsLock.unlock();
        }
    }
    
    public void unregisterMountHunger(MapleCharacter chr) {
        Integer key = chr.getId();
        
        activeMountsLock.lock();
        try {
            activeMounts.remove(key);
        } finally {
            activeMountsLock.unlock();
        }
    }
    
    public void runMountSchedule() {
        Map<Integer, Byte> deployedMounts;
        activeMountsLock.lock();
        try {
            mountUpdate = System.currentTimeMillis();
            deployedMounts = Collections.unmodifiableMap(activeMounts);
        } finally {
            activeMountsLock.unlock();
        }
        
        for(Map.Entry<Integer, Byte> dp: deployedMounts.entrySet()) {
            MapleCharacter chr = this.getPlayerStorage().getCharacterById(dp.getKey());
            if(chr == null || !chr.isLoggedin() || chr.isAwayFromWorld()) continue;
            
            Byte dpVal = (byte)(dp.getValue() + 1);
            if(dpVal == ServerConstants.MOUNT_EXHAUST_COUNT) {
                chr.runTirednessSchedule();
                dpVal = 0;
            }
            
            activeMountsLock.lock();
            try {
                activeMounts.put(dp.getKey(), dpVal);
            } finally {
                activeMountsLock.unlock();
            }
        }
    }
    
    public void registerPlayerShop(MaplePlayerShop ps) {
        activePlayerShopsLock.lock();
        try {
            activePlayerShops.put(ps.getOwner().getId(), ps);
        } finally {
            activePlayerShopsLock.unlock();
        }
    }
    
    public void unregisterPlayerShop(MaplePlayerShop ps) {
        activePlayerShopsLock.lock();
        try {
            activePlayerShops.remove(ps.getOwner().getId());
        } finally {
            activePlayerShopsLock.unlock();
        }
    }
    
    public List<MaplePlayerShop> getActivePlayerShops() {
        List<MaplePlayerShop> psList = new ArrayList<>();
        activePlayerShopsLock.lock();
        try {
            for(MaplePlayerShop mps : activePlayerShops.values()) {
                psList.add(mps);
            }
            
            return psList;
        } finally {
            activePlayerShopsLock.unlock();
        }
    }
    
    public MaplePlayerShop getPlayerShop(int ownerid) {
        activePlayerShopsLock.lock();
        try {
            return activePlayerShops.get(ownerid);
        } finally {
            activePlayerShopsLock.unlock();
        }
    }
    
    public void registerHiredMerchant(MapleHiredMerchant hm) {
        activeMerchantsLock.lock();
        try {
            byte initProc;
            if(System.currentTimeMillis() - merchantUpdate > 5 * 60 * 1000) initProc = 1;
            else initProc = 0;
            
            activeMerchants.put(hm.getOwnerId(), new Pair<>(hm, initProc));
        } finally {
            activeMerchantsLock.unlock();
        }
    }
    
    public void unregisterHiredMerchant(MapleHiredMerchant hm) {
        activeMerchantsLock.lock();
        try {
            activeMerchants.remove(hm.getOwnerId());
        } finally {
            activeMerchantsLock.unlock();
        }
    }
    
    public void runHiredMerchantSchedule() {
        Map<Integer, Pair<MapleHiredMerchant, Byte>> deployedMerchants;
        activeMerchantsLock.lock();
        try {
            merchantUpdate = System.currentTimeMillis();
            deployedMerchants = new LinkedHashMap<>(activeMerchants);
        
            for(Map.Entry<Integer, Pair<MapleHiredMerchant, Byte>> dm: deployedMerchants.entrySet()) {
                byte timeOn = dm.getValue().getRight();
                MapleHiredMerchant hm = dm.getValue().getLeft();
                
                if(timeOn <= 144) {   // 1440 minutes == 24hrs
                    activeMerchants.put(hm.getOwnerId(), new Pair<>(dm.getValue().getLeft(), (byte)(timeOn + 1)));
                } else {
                    hm.forceClose();
                    this.getChannel(hm.getChannel()).removeHiredMerchant(hm.getOwnerId());

                    activeMerchants.remove(dm.getKey());
                }
            }
        } finally {
            activeMerchantsLock.unlock();
        }
    }
    
    public List<MapleHiredMerchant> getActiveMerchants() {
        List<MapleHiredMerchant> hmList = new ArrayList<>();
        activeMerchantsLock.lock();
        try {
            for(Pair<MapleHiredMerchant, Byte> hmp : activeMerchants.values()) {
                MapleHiredMerchant hm = hmp.getLeft();
                if(hm.isOpen()) {
                    hmList.add(hm);
                }
            }
            
            return hmList;
        } finally {
            activeMerchantsLock.unlock();
        }
    }
    
    public MapleHiredMerchant getHiredMerchant(int ownerid) {
        activeMerchantsLock.lock();
        try {
            if(activeMerchants.containsKey(ownerid)) {
                return activeMerchants.get(ownerid).getLeft();
            }
            
            return null;
        } finally {
            activeMerchantsLock.unlock();
        }
    }

    public void setServerMessage(String msg) {
        for (Channel ch : channels) {
            ch.setServerMessage(msg);
        }
    }

    public void broadcastPacket(final byte[] data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            chr.announce(data);
        }
    }

    public List<Pair<MaplePlayerShopItem, AbstractMapleMapObject>> getAvailableItemBundles(int itemid) {
            List<Pair<MaplePlayerShopItem, AbstractMapleMapObject>> hmsAvailable = new ArrayList<>();

            for (MapleHiredMerchant hm : getActiveMerchants()) {
                    List<MaplePlayerShopItem> itemBundles = hm.sendAvailableBundles(itemid);
                    
                    for(MaplePlayerShopItem mpsi : itemBundles) {
                            hmsAvailable.add(new Pair<>(mpsi, (AbstractMapleMapObject) hm));
                    }
            }
            
            for (MaplePlayerShop ps : getActivePlayerShops()) {
                    List<MaplePlayerShopItem> itemBundles = ps.sendAvailableBundles(itemid);
                    
                    for(MaplePlayerShopItem mpsi : itemBundles) {
                            hmsAvailable.add(new Pair<>(mpsi, (AbstractMapleMapObject) ps));
                    }
            }

            Collections.sort(hmsAvailable, new Comparator<Pair<MaplePlayerShopItem, AbstractMapleMapObject>>() {
                    @Override
                    public int compare(Pair<MaplePlayerShopItem, AbstractMapleMapObject> p1, Pair<MaplePlayerShopItem, AbstractMapleMapObject> p2) {
                            return p1.getLeft().getPrice() - p2.getLeft().getPrice();
                    }
            });

            hmsAvailable.subList(0, Math.min(hmsAvailable.size(), 200));    //truncates the list to have up to 200 elements
            return hmsAvailable;
    }
    
    public final void shutdown() {
        for (Channel ch : getChannels()) {
            ch.shutdown();
        }
        
        if(petsSchedule != null) {
            petsSchedule.cancel(false);
            petsSchedule = null;
        }
        
        if(mountsSchedule != null) {
            mountsSchedule.cancel(false);
            mountsSchedule = null;
        }
        
        players.disconnectAll();
    }
}
