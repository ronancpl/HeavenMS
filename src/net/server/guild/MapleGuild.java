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
package net.server.guild;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.locks.Lock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

import net.server.Server;
import net.server.channel.Channel;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import net.server.audit.locks.MonitoredLockType;

public class MapleGuild {
    
    private enum BCOp {
        NONE, DISBAND, EMBLEMCHANGE
    }
    
    private final List<MapleGuildCharacter> members;
    private final Lock membersLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.GUILD, true);
    
    private String rankTitles[] = new String[5]; // 1 = master, 2 = jr, 5 = lowest member
    private String name, notice;
    private int id, gp, logo, logoColor, leader, capacity, logoBG, logoBGColor, signature, allianceId;
    private int world;
    private Map<Integer, List<Integer>> notifications = new LinkedHashMap<>();
    private boolean bDirty = true;

    public MapleGuild(int guildid, int world) {
        this.world = world;
        members = new ArrayList<>();
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = " + guildid);
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                id = -1;
                ps.close();
                rs.close();
                return;
            }
            id = guildid;
            name = rs.getString("name");
            gp = rs.getInt("GP");
            logo = rs.getInt("logo");
            logoColor = rs.getInt("logoColor");
            logoBG = rs.getInt("logoBG");
            logoBGColor = rs.getInt("logoBGColor");
            capacity = rs.getInt("capacity");
            for (int i = 1; i <= 5; i++) {
                rankTitles[i - 1] = rs.getString("rank" + i + "title");
            }
            leader = rs.getInt("leader");
            notice = rs.getString("notice");
            signature = rs.getInt("signature");
            allianceId = rs.getInt("allianceId");
            ps.close();
            rs.close();
            ps = con.prepareStatement("SELECT id, name, level, job, guildrank, allianceRank FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            if (!rs.first()) {
                rs.close();
                ps.close();
                return;
            }
            do {
                members.add(new MapleGuildCharacter(null, rs.getInt("id"), rs.getInt("level"), rs.getString("name"), (byte) -1, world, rs.getInt("job"), rs.getInt("guildrank"), guildid, false, rs.getInt("allianceRank")));
            } while (rs.next());
            
            ps.close();
            rs.close();
            con.close();
        } catch (SQLException se) {
            se.printStackTrace();
            System.out.println("Unable to read guild information from sql" + se);
        }
    }

    public void buildNotifications() {
        if (!bDirty) {
            return;
        }
        Set<Integer> chs = Server.getInstance().getOpenChannels(world);
        synchronized (notifications) {
            if (notifications.keySet().size() != chs.size()) {
                notifications.clear();
                for (Integer ch : chs) {
                    notifications.put(ch, new LinkedList<Integer>());
                }
            } else {
                for (List<Integer> l : notifications.values()) {
                    l.clear();
                }
            }
        }
        
        membersLock.lock();
        try {
            for (MapleGuildCharacter mgc : members) {
                if (!mgc.isOnline()) {
                    continue;
                }
                
                List<Integer> chl;
                synchronized (notifications) {
                    chl = notifications.get(mgc.getChannel());
                }
                if (chl != null) chl.add(mgc.getId());
                //Unable to connect to Channel... error was here
            }
        } finally {
            membersLock.unlock();
        }
        
        bDirty = false;
    }

    public void writeToDB(boolean bDisband) {
        try {
            Connection con = DatabaseConnection.getConnection();
            
            if (!bDisband) {
                StringBuilder builder = new StringBuilder();
                builder.append("UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");
                for (int i = 0; i < 5; i++) {
                    builder.append("rank").append(i + 1).append("title = ?, ");
                }
                builder.append("capacity = ?, notice = ? WHERE guildid = ?");
                try (PreparedStatement ps = con.prepareStatement(builder.toString())) {
                    ps.setInt(1, gp);
                    ps.setInt(2, logo);
                    ps.setInt(3, logoColor);
                    ps.setInt(4, logoBG);
                    ps.setInt(5, logoBGColor);
                    for (int i = 6; i < 11; i++) {
                        ps.setString(i, rankTitles[i - 6]);
                    }
                    ps.setInt(11, capacity);
                    ps.setString(12, notice);
                    ps.setInt(13, this.id);
                    ps.execute();
                }
            } else {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?");
                ps.setInt(1, this.id);
                ps.execute();
                ps.close();
                ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                ps.setInt(1, this.id);
                ps.execute();
                ps.close();
                
                membersLock.lock();
                try {
                    this.broadcast(MaplePacketCreator.guildDisband(this.id));
                } finally {
                    membersLock.unlock();
                }
            }
            
            con.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getLeaderId() {
        return leader;
    }
    
    public int setLeaderId(int charId) {
        return leader = charId;
    }

    public int getGP() {
        return gp;
    }

    public int getLogo() {
        return logo;
    }

    public void setLogo(int l) {
        logo = l;
    }

    public int getLogoColor() {
        return logoColor;
    }

    public void setLogoColor(int c) {
        logoColor = c;
    }

    public int getLogoBG() {
        return logoBG;
    }

    public void setLogoBG(int bg) {
        logoBG = bg;
    }

    public int getLogoBGColor() {
        return logoBGColor;
    }

    public void setLogoBGColor(int c) {
        logoBGColor = c;
    }

    public String getNotice() {
        if (notice == null) {
            return "";
        }
        return notice;
    }

    public String getName() {
        return name;
    }

    public java.util.Collection<MapleGuildCharacter> getMembers() {
        membersLock.lock();
        try {
            return java.util.Collections.unmodifiableCollection(members);
        } finally {
            membersLock.unlock();
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSignature() {
        return signature;
    }

    public void broadcast(final byte[] packet) {
        broadcast(packet, -1, BCOp.NONE);
    }

    public void broadcast(final byte[] packet, int exception) {
        broadcast(packet, exception, BCOp.NONE);
    }

    public void broadcast(final byte[] packet, int exceptionId, BCOp bcop) {
        membersLock.lock(); // membersLock awareness thanks to ProjectNano dev team
        try {
            synchronized (notifications) {
                if (bDirty) {
                    buildNotifications();
                }
                try {
                    for (Integer b : Server.getInstance().getOpenChannels(world)) {
                        if (notifications.get(b).size() > 0) {
                            if (bcop == BCOp.DISBAND) {
                                Server.getInstance().getWorld(world).setGuildAndRank(notifications.get(b), 0, 5, exceptionId);
                            } else if (bcop == BCOp.EMBLEMCHANGE) {
                                Server.getInstance().getWorld(world).changeEmblem(this.id, notifications.get(b), new MapleGuildSummary(this));
                            } else {
                                Server.getInstance().getWorld(world).sendPacket(notifications.get(b), packet, exceptionId);
                            }
                        }
                    }
                } catch (Exception re) {
                    re.printStackTrace();
                    System.out.println("Failed to contact channel(s) for broadcast.");//fu?
                }
            }
        } finally {
            membersLock.unlock();
        }
    }

    public void guildMessage(final byte[] serverNotice) {
        membersLock.lock();
        try {
            for (MapleGuildCharacter mgc : members) {
                for (Channel cs : Server.getInstance().getChannelsFromWorld(world)) {
                    if (cs.getPlayerStorage().getCharacterById(mgc.getId()) != null) {
                        cs.getPlayerStorage().getCharacterById(mgc.getId()).getClient().announce(serverNotice);
                        break;
                    }
                }
            }
        } finally {
            membersLock.unlock();
        }
    }
    
    public void dropMessage(String message) {
        dropMessage(5, message);
    }
    
    public void dropMessage(int type, String message) {
        membersLock.lock();
        try {
            for (MapleGuildCharacter mgc : members) {
                if(mgc.getCharacter() != null) {
                    mgc.getCharacter().dropMessage(type, message);
                }
            }
        } finally {
            membersLock.unlock();
        }
    }
    
    public void broadcastMessage(byte[] packet) {
        Server.getInstance().guildMessage(id, packet);
    }

    public final void setOnline(int cid, boolean online, int channel) {
        membersLock.lock();
        try {
            boolean bBroadcast = true;
            for (MapleGuildCharacter mgc : members) {
                if (mgc.getId() == cid) {
                    if (mgc.isOnline() && online) {
                        bBroadcast = false;
                    }
                    mgc.setOnline(online);
                    mgc.setChannel(channel);
                    break;
                }
            }
            if (bBroadcast) {
                this.broadcast(MaplePacketCreator.guildMemberOnline(id, cid, online), cid);
            }
            bDirty = true;
        } finally {
            membersLock.unlock();
        }
    }

    public void guildChat(String name, int cid, String message) {
        membersLock.lock();
        try {
            this.broadcast(MaplePacketCreator.multiChat(name, message, 2), cid);
        } finally {
            membersLock.unlock();
        }
    }

    public String getRankTitle(int rank) {
        return rankTitles[rank - 1];
    }

    public static int createGuild(int leaderId, String name) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                ps.close();
                rs.close();
                return 0;
            }
            ps.close();
            rs.close();
            
            ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `signature`) VALUES (?, ?, ?)");
            ps.setInt(1, leaderId);
            ps.setString(2, name);
            ps.setInt(3, (int) System.currentTimeMillis());
            ps.execute();
            ps.close();
            
            ps = con.prepareStatement("SELECT guildid FROM guilds WHERE leader = ?");
            ps.setInt(1, leaderId);
            rs = ps.executeQuery();
            rs.first();
            int guildId = rs.getInt("guildid");
            rs.close();
            ps.close();
            
            ps = con.prepareStatement("UPDATE characters SET guildid = ? WHERE id = ?");
            ps.setInt(1, guildId);
            ps.setInt(2, leaderId);
            ps.executeUpdate();
            ps.close();
            
            con.close();
            return guildId;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int addGuildMember(MapleGuildCharacter mgc, MapleCharacter chr) {
        membersLock.lock();
        try {
            if (members.size() >= capacity) {
                return 0;
            }
            for (int i = members.size() - 1; i >= 0; i--) {
                if (members.get(i).getGuildRank() < 5 || members.get(i).getName().compareTo(mgc.getName()) < 0) {
                    mgc.setCharacter(chr);
                    members.add(i + 1, mgc);
                    bDirty = true;
                    break;
                }
            }
            
            this.broadcast(MaplePacketCreator.newGuildMember(mgc));
            return 1;
        } finally {
            membersLock.unlock();
        }
    }

    public void leaveGuild(MapleGuildCharacter mgc) {
        membersLock.lock();
        try {
            this.broadcast(MaplePacketCreator.memberLeft(mgc, false));
            members.remove(mgc);
            bDirty = true;
        } finally {
            membersLock.unlock();
        }
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) {
        membersLock.lock();
        try {
            java.util.Iterator<MapleGuildCharacter> itr = members.iterator();
            MapleGuildCharacter mgc;
            while (itr.hasNext()) {
                mgc = itr.next();
                if (mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()) {
                    this.broadcast(MaplePacketCreator.memberLeft(mgc, true));
                    itr.remove();
                    bDirty = true;
                    try {
                        if (mgc.isOnline()) {
                            Server.getInstance().getWorld(mgc.getWorld()).setGuildAndRank(cid, 0, 5);
                        } else {
                            try {
                                Connection con = DatabaseConnection.getConnection();
                                try (PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)")) {
                                    ps.setString(1, mgc.getName());
                                    ps.setString(2, initiator.getName());
                                    ps.setString(3, "You have been expelled from the guild.");
                                    ps.setLong(4, System.currentTimeMillis());
                                    ps.executeUpdate();
                                }
                                
                                con.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                                System.out.println("expelMember - MapleGuild " + e);
                            }
                            Server.getInstance().getWorld(mgc.getWorld()).setOfflineGuildStatus((short) 0, (byte) 5, cid);
                        }
                    } catch (Exception re) {
                        re.printStackTrace();
                        return;
                    }
                    return;
                }
            }
            System.out.println("Unable to find member with name " + name + " and id " + cid);
        } finally {
            membersLock.unlock();
        }
    }

    public void changeRank(int cid, int newRank) {
        membersLock.lock();
        try {
            for (MapleGuildCharacter mgc : members) {
                if (cid == mgc.getId()) {
                    changeRank(mgc, newRank);
                    return;
                }
            }
        } finally {
            membersLock.unlock();
        }
    }
    
    public void changeRank(MapleGuildCharacter mgc, int newRank) {
        try {
            if (mgc.isOnline()) {
                Server.getInstance().getWorld(mgc.getWorld()).setGuildAndRank(mgc.getId(), this.id, newRank);
                mgc.setGuildRank(newRank);
            } else {
                Server.getInstance().getWorld(mgc.getWorld()).setOfflineGuildStatus((short) this.id, (byte) newRank, mgc.getId());
                mgc.setOfflineGuildRank(newRank);
            }
        } catch (Exception re) {
            re.printStackTrace();
            return;
        }
        
        membersLock.lock();
        try {
            this.broadcast(MaplePacketCreator.changeRank(mgc));
        } finally {
            membersLock.unlock();
        }
    }
    
    public void setGuildNotice(String notice) {
        this.notice = notice;
        writeToDB(false);
        
        membersLock.lock();
        try {
            this.broadcast(MaplePacketCreator.guildNotice(this.id, notice));
        } finally {
            membersLock.unlock();
        }
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        membersLock.lock();
        try {
            for (MapleGuildCharacter member : members) {
                if (mgc.equals(member)) {
                    member.setJobId(mgc.getJobId());
                    member.setLevel(mgc.getLevel());
                    this.broadcast(MaplePacketCreator.guildMemberLevelJobUpdate(mgc));
                    break;
                }
            }
        } finally {
            membersLock.unlock();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MapleGuildCharacter)) {
            return false;
        }
        MapleGuildCharacter o = (MapleGuildCharacter) other;
        return (o.getId() == id && o.getName().equals(name));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 89 * hash + this.id;
        return hash;
    }

    public void changeRankTitle(String[] ranks) {
        System.arraycopy(ranks, 0, rankTitles, 0, 5);
        
        membersLock.lock();
        try {
            this.broadcast(MaplePacketCreator.rankTitleChange(this.id, ranks));
        } finally {
            membersLock.unlock();
        }
        
        this.writeToDB(false);
    }

    public void disbandGuild() {
        if(allianceId > 0) {
            if (!MapleAlliance.removeGuildFromAlliance(allianceId, id, world)) {
                MapleAlliance.disbandAlliance(allianceId);
            }
        }
        
        membersLock.lock();
        try {
            this.writeToDB(true);
            this.broadcast(null, -1, BCOp.DISBAND);
        } finally {
            membersLock.unlock();
        }
    }

    public void setGuildEmblem(short bg, byte bgcolor, short logo, byte logocolor) {
        this.logoBG = bg;
        this.logoBGColor = bgcolor;
        this.logo = logo;
        this.logoColor = logocolor;
        this.writeToDB(false);
        
        membersLock.lock();
        try {
            this.broadcast(null, -1, BCOp.EMBLEMCHANGE);
        } finally {
            membersLock.unlock();
        }
    }

    public MapleGuildCharacter getMGC(int cid) {
        membersLock.lock();
        try {
            for (MapleGuildCharacter mgc : members) {
                if (mgc.getId() == cid) {
                    return mgc;
                }
            }
            return null;
        } finally {
            membersLock.unlock();
        }
    }

    public boolean increaseCapacity() {
        if (capacity > 99) {
            return false;
        }
        capacity += 5;
        this.writeToDB(false);
        
        membersLock.lock();
        try {
            this.broadcast(MaplePacketCreator.guildCapacityChange(this.id, this.capacity));
        } finally {
            membersLock.unlock();
        }
        
        return true;
    }

    public void gainGP(int amount) {
        this.gp += amount;
        this.writeToDB(false);
        this.guildMessage(MaplePacketCreator.updateGP(this.id, this.gp));
        this.guildMessage(MaplePacketCreator.getGPMessage(amount));
    }
    
    public void removeGP(int amount){
        this.gp -= amount;
        this.writeToDB(false);
        this.guildMessage(MaplePacketCreator.updateGP(this.id, this.gp));
    }

    public static MapleGuildResponse sendInvite(MapleClient c, String targetName) {
        MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
        if (mc == null) {
            return MapleGuildResponse.NOT_IN_CHANNEL;
        }
        if (mc.getGuildId() > 0) {
            return MapleGuildResponse.ALREADY_IN_GUILD;
        }
        mc.getClient().announce(MaplePacketCreator.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getName()));
        return null;
    }

    public static void displayGuildRanks(MapleClient c, int npcid) {
        try {
            ResultSet rs;
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT `name`, `GP`, `logoBG`, `logoBGColor`, `logo`, `logoColor` FROM guilds WHERE NOT `guildid` = '1' ORDER BY `GP` DESC LIMIT 50")) {
                rs = ps.executeQuery();
                c.announce(MaplePacketCreator.showGuildRanks(npcid, rs));
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("failed to display guild ranks. " + e);
        }
    }

    public int getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(int aid) {
        this.allianceId = aid;
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE guilds SET allianceId = ? WHERE guildid = ?")) {
                ps.setInt(1, aid);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void resetAllianceGuildPlayersRank() {
        try {
            membersLock.lock();
            try {
                for(MapleGuildCharacter mgc: members) {
                    if(mgc.isOnline()) {
                        mgc.setAllianceRank(5);
                    }
                }
            } finally {
                membersLock.unlock();
            }
            
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET allianceRank = ? WHERE guildid = ?")) {
                ps.setInt(1, 5);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getIncreaseGuildCost(int size) {
        int cost = ServerConstants.EXPAND_GUILD_BASE_COST + Math.max(0, (size - 15) / 5) * ServerConstants.EXPAND_GUILD_TIER_COST;
        
        if (size > 30) {
            return Math.min(ServerConstants.EXPAND_GUILD_MAX_COST, Math.max(cost, 5000000));
        } else {
            return cost;
        }
    }
}
