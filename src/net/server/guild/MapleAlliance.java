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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import client.MapleCharacter;
import net.server.Server;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author XoticStory
 * @author Ronan
 */
public class MapleAlliance {
    final private List<Integer> guilds = new LinkedList<>();
    
    private int allianceId = -1;
    private int capacity;
    private String name;
    private String notice = "";
    private String rankTitles[] = new String[5];

    public MapleAlliance(String name, int id) {
        this.name = name;
        allianceId = id;
        String[] ranks = {"Master", "Jr.Master", "Member", "Member", "Member"};
        for (int i = 0; i < 5; i++) {
            rankTitles[i] = ranks[i];
        }
    }
    
    public static boolean canBeUsedAllianceName(String name) {
        if (name.contains(" ") || name.length() > 12) {
            return false;
        }
        try {
            ResultSet rs;
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT name FROM alliance WHERE name = ?")) {
                ps.setString(1, name);
                rs = ps.executeQuery();
                if (rs.next()) {
                    ps.close();
                    rs.close();
                    return false;
                }
            }
            rs.close();
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static List<MapleCharacter> getPartyGuildMasters(MapleParty party) {
        List<MapleCharacter> mcl = new LinkedList<>();

        for(MaplePartyCharacter mpc: party.getMembers()) {
            if(mpc.getPlayer().getGuildRank() == 1 && mpc.getPlayer().getMapId() == party.getLeader().getPlayer().getMapId())
                mcl.add(mpc.getPlayer());
        }

        if(!mcl.isEmpty() && !mcl.get(0).isPartyLeader()) {
            for(int i = 1; i < mcl.size(); i++) {
                if(mcl.get(i).isPartyLeader()) {
                    MapleCharacter temp = mcl.get(0);
                    mcl.set(0, mcl.get(i));
                    mcl.set(i, temp);
                }
            }
        }

        return mcl;
    }

    public static MapleAlliance createAlliance(MapleParty party, String name) {
        List<MapleCharacter> guildMasters = getPartyGuildMasters(party);
        if(guildMasters.size() != 2) return null;

        List<Integer> guilds = new LinkedList<>();
        for(MapleCharacter mc: guildMasters) guilds.add(mc.getGuildId());
        MapleAlliance alliance = MapleAlliance.createAllianceOnDb(guilds, name);
        if(alliance != null) {
            alliance.setCapacity(guilds.size());
            for(Integer g: guilds)
                alliance.addGuild(g);
            
            int id = alliance.getId();
            try {
                for(int i = 0; i < guildMasters.size(); i++) {
                    Server.getInstance().setGuildAllianceId(guilds.get(i), id);
                    Server.getInstance().resetAllianceGuildPlayersRank(guilds.get(i));

                    MapleCharacter chr = guildMasters.get(i);
                    chr.getMGC().setAllianceRank((i == 0) ? 1 : 2);
                    Server.getInstance().getGuild(chr.getGuildId()).getMGC(chr.getId()).setAllianceRank((i == 0) ? 1 : 2);
                    chr.saveGuildStatus();
                }

                Server.getInstance().addAlliance(id, alliance);
                
                Server.getInstance().allianceMessage(id, MaplePacketCreator.updateAllianceInfo(alliance, guildMasters.get(0).getClient()), -1, -1);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return alliance;
    }
    
    public static MapleAlliance createAllianceOnDb(List<Integer> guilds, String name) {
        // will create an alliance, where the first guild listed is the leader and the alliance name MUST BE already checked for unicity.
        
        int id = -1;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO `alliance` (`name`) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
            
            for(int i = 0; i < guilds.size(); i++) {
                int guild = guilds.get(i);
                
                ps = con.prepareStatement("INSERT INTO `allianceguilds` (`allianceid`, `guildid`) VALUES (?, ?)");
                ps.setInt(1, id);
                ps.setInt(2, guild);
                ps.executeUpdate();
                ps.close();
            }
            
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        
        return (new MapleAlliance(name, id));
    }

    public static MapleAlliance loadAlliance(int id) {
        if (id <= 0) {
            return null;
        }
        MapleAlliance alliance = new MapleAlliance(null, -1);
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM alliance WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                return null;
            }
            alliance.allianceId = id;
            alliance.capacity = rs.getInt("capacity");
            alliance.name = rs.getString("name");
            alliance.notice = rs.getString("notice");
            
            String ranks[] = new String[5];
            ranks[0] = rs.getString("rank1");
            ranks[1] = rs.getString("rank2");
            ranks[2] = rs.getString("rank3");
            ranks[3] = rs.getString("rank4");
            ranks[4] = rs.getString("rank5");
            alliance.rankTitles = ranks;
            
            ps.close();
            rs.close();
            
            ps = con.prepareStatement("SELECT guildid FROM allianceguilds WHERE allianceid = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            
            while(rs.next()) {
                alliance.addGuild(rs.getInt("guildid"));
            }
            
            ps.close();
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alliance;
    }

    public void saveToDB() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE `alliance` SET capacity = ?, notice = ?, rank1 = ?, rank2 = ?, rank3 = ?, rank4 = ?, rank5 = ? WHERE id = ?");
            ps.setInt(1, this.capacity);
            ps.setString(2, this.notice);
            
            ps.setString(3, this.rankTitles[0]);
            ps.setString(4, this.rankTitles[1]);
            ps.setString(5, this.rankTitles[2]);
            ps.setString(6, this.rankTitles[3]);
            ps.setString(7, this.rankTitles[4]);
            
            ps.setInt(8, this.allianceId);
            ps.executeUpdate();
            ps.close();
            
            ps = con.prepareStatement("DELETE FROM `allianceguilds` WHERE allianceid = ?");
            ps.setInt(1, this.allianceId);
            ps.executeUpdate();
            ps.close();
            
            for(int i = 0; i < guilds.size(); i++) {
                int guild = guilds.get(i);
                
                ps = con.prepareStatement("INSERT INTO `allianceguilds` (`allianceid`, `guildid`) VALUES (?, ?)");
                ps.setInt(1, this.allianceId);
                ps.setInt(2, guild);
                ps.executeUpdate();
                ps.close();
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void disbandAlliance(int allianceId) {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            
            ps = con.prepareStatement("DELETE FROM `alliance` WHERE id = ?");
            ps.setInt(1, allianceId);
            ps.executeUpdate();
            ps.close();
            
            ps = con.prepareStatement("DELETE FROM `allianceguilds` WHERE allianceid = ?");
            ps.setInt(1, allianceId);
            ps.executeUpdate();
            ps.close();
            
            con.close();
            Server.getInstance().allianceMessage(allianceId, MaplePacketCreator.disbandAlliance(allianceId), -1, -1);
            Server.getInstance().disbandAlliance(allianceId);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static void removeGuildFromAllianceOnDb(int guildId) {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            
            ps = con.prepareStatement("DELETE FROM `allianceguilds` WHERE guildid = ?");
            ps.setInt(1, guildId);
            ps.executeUpdate();
            ps.close();
            
            con.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static boolean removeGuildFromAlliance(int allianceId, int guildId, int worldId) {
        Server srv = Server.getInstance();
        MapleAlliance alliance = srv.getAlliance(allianceId);
    
        if (alliance.getLeader().getGuildId() == guildId) {
            return false;
        }
        
        srv.allianceMessage(alliance.getId(), MaplePacketCreator.removeGuildFromAlliance(alliance, guildId, worldId), -1, -1);
        srv.removeGuildFromAlliance(alliance.getId(), guildId);
        removeGuildFromAllianceOnDb(guildId);
        
        srv.allianceMessage(alliance.getId(), MaplePacketCreator.getGuildAlliances(alliance, worldId), -1, -1);
        srv.allianceMessage(alliance.getId(), MaplePacketCreator.allianceNotice(alliance.getId(), alliance.getNotice()), -1, -1);
        srv.guildMessage(guildId, MaplePacketCreator.disbandAlliance(alliance.getId()));
        
        alliance.dropMessage("[" + srv.getGuild(guildId, worldId).getName() + "] guild has left the union.");
        return true;
    }
    
    public void updateAlliancePackets(MapleCharacter chr) {
        if (allianceId > 0) {
            this.broadcastMessage(MaplePacketCreator.updateAllianceInfo(this, chr.getClient()));
            this.broadcastMessage(MaplePacketCreator.allianceNotice(this.getId(), this.getNotice()));
        }
    }
    
    public boolean removeGuild(int gid) {
        synchronized (guilds) {
            int index = getGuildIndex(gid);
            if(index == -1) return false;
            
            guilds.remove(index);
            return true;
        }
    }

    public boolean addGuild(int gid) {
        synchronized (guilds) {
            if(guilds.size() == capacity || getGuildIndex(gid) > -1) return false;
            
            guilds.add(gid);
            return true;
        }
    }

    private int getGuildIndex(int gid) {
        synchronized (guilds) {
            for (int i = 0; i < guilds.size(); i++) {
                if (guilds.get(i) == gid) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    public void setRankTitle(String[] ranks) {
        rankTitles = ranks;
    }

    public String getRankTitle(int rank) {
        return rankTitles[rank - 1];
    }
    
    public List<Integer> getGuilds() {
        synchronized(guilds) {
            List<Integer> guilds_ = new LinkedList<>();
            for (int guild : guilds) {
                if (guild != -1) {
                    guilds_.add(guild);
                }
            }
            return guilds_;
        }
    }
    
    public String getAllianceNotice() {
        return notice;
    }

    public String getNotice() {
        return notice;
    }
    
    public void setNotice(String notice) {
        this.notice = notice;
    }

    public void increaseCapacity(int inc) {
        this.capacity += inc;
    }

    public void setCapacity(int newCapacity) {
        this.capacity = newCapacity;
    }
    
    public int getCapacity() {
        return this.capacity;
    }
    
    public int getId() {
        return allianceId;
    }

    public String getName() {
        return name;
    }
    
    public MapleGuildCharacter getLeader() {
        synchronized(guilds) {
            for(Integer gId: guilds) {
                MapleGuild guild = Server.getInstance().getGuild(gId);
                MapleGuildCharacter mgc = guild.getMGC(guild.getLeaderId());

                if(mgc.getAllianceRank() == 1) return mgc;
            }

            return null;
        }
    }
    
    public void dropMessage(String message) {
        dropMessage(5, message);
    }
    
    public void dropMessage(int type, String message) {
        synchronized(guilds) {
            for(Integer gId: guilds) {
                MapleGuild guild = Server.getInstance().getGuild(gId);
                guild.dropMessage(type, message);
            }
        }
    }
    
    public void broadcastMessage(byte[] packet) {
        Server.getInstance().allianceMessage(allianceId, packet, -1, -1);
    }
}
