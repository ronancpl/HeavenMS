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
package server.life;

import server.life.positioner.MaplePlayerNPCPositioner;
import server.life.positioner.MaplePlayerNPCPodium;
import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConstants;
import net.server.Server;
import net.server.channel.Channel;
import net.server.world.World;
import server.maps.AbstractMapleMapObject;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author XoticStory
 * @author Ronan
 */
public class MaplePlayerNPC extends AbstractMapleMapObject {
    private static final Map<Byte, List<Integer>> availablePlayerNpcScriptIds = new HashMap<>();
    private static final AtomicInteger runningOverallRank = new AtomicInteger();
    private static final List<AtomicInteger> runningWorldRank = new ArrayList<>();
    private static final Map<Pair<Integer, Integer>, AtomicInteger> runningWorldJobRank = new HashMap<>();
    
    private Map<Short, Integer> equips = new HashMap<>();
    private int scriptId, face, hair, gender, job;
    private byte skin;
    private String name = "";
    private int dir, FH, RX0, RX1, CY;
    private int worldRank, overallRank, worldJobRank, overallJobRank;
    
    static {
        getRunningMetadata();
    }
    
    public MaplePlayerNPC(String name, int scriptId, int face, int hair, int gender, byte skin, Map<Short, Integer> equips, int dir, int FH, int RX0, int RX1, int CX, int CY, int oid) {
        this.equips = equips;
        this.scriptId = scriptId;
        this.face = face;
        this.hair = hair;
        this.gender = gender;
        this.skin = skin;
        this.name = name;
        this.dir = dir;
        this.FH = FH;
        this.RX0 = RX0;
        this.RX1 = RX1;
        this.CY = CY;
        this.job = 7777;    // supposed to be developer
        
        setPosition(new Point(CX, CY));
        setObjectId(oid);
    }
    
    public MaplePlayerNPC(ResultSet rs) {
        try {
            CY = rs.getInt("cy");
            name = rs.getString("name");
            hair = rs.getInt("hair");
            face = rs.getInt("face");
            skin = rs.getByte("skin");
            gender = rs.getInt("gender");
            dir = rs.getInt("dir");
            FH = rs.getInt("fh");
            RX0 = rs.getInt("rx0");
            RX1 = rs.getInt("rx1");
            scriptId = rs.getInt("scriptid");
            
            worldRank = rs.getInt("worldrank");
            overallRank = rs.getInt("overallrank");
            worldJobRank = rs.getInt("worldjobrank");
            overallJobRank = GameConstants.getOverallJobRankByScriptId(scriptId);
            job = rs.getInt("job");
            
            setPosition(new Point(rs.getInt("x"), CY));
            setObjectId(rs.getInt("id"));
            
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT equippos, equipid FROM playernpcs_equip WHERE npcid = ?");
            ps.setInt(1, rs.getInt("id"));
            ResultSet rs2 = ps.executeQuery();
            while (rs2.next()) {
                equips.put(rs2.getShort("equippos"), rs2.getInt("equipid"));
            }
            rs2.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Short, Integer> getEquips() {
        return equips;
    }

    public int getScriptId() {
        return scriptId;
    }

    public int getJob() {
        return job;
    }
    
    public int getDirection() {
        return dir;
    }
    
    public int getFH() {
        return FH;
    }

    public int getRX0() {
        return RX0;
    }

    public int getRX1() {
        return RX1;
    }

    public int getCY() {
        return CY;
    }

    public byte getSkin() {
        return skin;
    }

    public String getName() {
        return name;
    }

    public int getFace() {
        return face;
    }

    public int getHair() {
        return hair;
    }
    
    public int getGender() {
        return gender;
    }

    public int getWorldRank() {
        return worldRank;
    }
    
    public int getOverallRank() {
        return overallRank;
    }
    
    public int getWorldJobRank() {
        return worldJobRank;
    }
    
    public int getOverallJobRank() {
        return overallJobRank;
    }
    
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER_NPC;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(MaplePacketCreator.spawnPlayerNPC(this));
        client.announce(MaplePacketCreator.getPlayerNPC(this));
    }
    
    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removeNPCController(this.getObjectId()));
        client.announce(MaplePacketCreator.removePlayerNPC(this.getObjectId()));
    }
    
    private static void getRunningMetadata() {
        try {
            Connection con = DatabaseConnection.getConnection();
            
            getRunningOverallRanks(con);
            getRunningWorldRanks(con);
            getRunningWorldJobRanks(con);
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void getRunningOverallRanks(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT max(overallrank) FROM playernpcs");
        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            runningOverallRank.set(rs.getInt(1) + 1);
        } else {
            runningOverallRank.set(1);
        }
        
        rs.close();
        ps.close();
    }

    private static void getRunningWorldRanks(Connection con) throws SQLException {
        int numWorlds = Server.getInstance().getWorldsSize();
        for(int i = 0; i < numWorlds; i++) {
            runningWorldRank.add(new AtomicInteger(1));
        }
        
        PreparedStatement ps = con.prepareStatement("SELECT world, max(worldrank) FROM playernpcs GROUP BY world ORDER BY world");
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            int wid = rs.getInt(1);
            if(wid < numWorlds) {
                runningWorldRank.get(wid).set(rs.getInt(2) + 1);
            }
        }
        
        rs.close();
        ps.close();
    }
    
    private static void getRunningWorldJobRanks(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT world, job, max(worldjobrank) FROM playernpcs GROUP BY world, job ORDER BY world, job");
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            runningWorldJobRank.put(new Pair<>(rs.getInt(1), rs.getInt(2)), new AtomicInteger(rs.getInt(3) + 1));
        }
        
        rs.close();
        ps.close();
    }
    
    private static int getAndIncrementRunningWorldJobRanks(int world, int job) {
        AtomicInteger wjr = runningWorldJobRank.get(new Pair<>(world, job));
        if(wjr == null) {
            wjr = new AtomicInteger(1);
            runningWorldJobRank.put(new Pair<>(world, job), wjr);
        }
        
        return wjr.getAndIncrement();
    }
    
    public static boolean canSpawnPlayerNpc(String name, int mapid) {
        boolean ret = true;
        
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT name FROM playernpcs WHERE name LIKE ? AND map = ?");
            ps.setString(1, name);
            ps.setInt(2, mapid);
            
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                ret = false;
            }
            
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public void updatePlayerNPCPosition(MapleMap map, Point newPos) {
        setPosition(newPos);
        RX0 = newPos.x + 50;
        RX1 = newPos.x - 50;
        CY = newPos.y;
        FH = map.getFootholds().findBelow(newPos).getId();
        
        try {
            Connection con = DatabaseConnection.getConnection();
            
            PreparedStatement ps = con.prepareStatement("UPDATE playernpcs SET x = ?, cy = ?, fh = ?, rx0 = ?, rx1 = ? WHERE id = ?");
            ps.setInt(1, newPos.x);
            ps.setInt(2, CY);
            ps.setInt(3, FH);
            ps.setInt(4, RX0);
            ps.setInt(5, RX1);
            ps.setInt(6, getObjectId());
            ps.executeUpdate();
            
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void fetchAvailableScriptIdsFromDb(byte branch, List<Integer> list) {
        try {
            int branchLen = (branch < 26) ? 100 : 400;
            int branchSid = 9900000 + (branch * 100);
            int nextBranchSid = branchSid + branchLen;
            Set<Integer> usedScriptIds = new HashSet<>();
            
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT scriptid FROM playernpcs WHERE scriptid >= ? AND scriptid < ? ORDER BY scriptid");
            ps.setInt(1, branchSid);
            ps.setInt(2, nextBranchSid);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                usedScriptIds.add(rs.getInt(1));
            }
            
            List<Integer> availables = new ArrayList<>(20);
            int j = 0;
            for(int i = branchSid; i < nextBranchSid; i++) {
                if(!usedScriptIds.contains(i)) {
                    availables.add(i);
                    j++;
                    
                    if(j == 20) {
                        break;
                    }
                }
            }

            rs.close();
            ps.close();
            con.close();
            
            for(int i = availables.size() - 1; i >= 0; i--) {
                list.add(availables.get(i));
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    private static int getNextScriptId(byte branch) {
        List<Integer> availablesBranch = availablePlayerNpcScriptIds.get(branch);
        
        if(availablesBranch == null) {
            availablesBranch = new ArrayList<>(20);
            availablePlayerNpcScriptIds.put(branch, availablesBranch);
        }
        
        if(availablesBranch.isEmpty()) {
            fetchAvailableScriptIdsFromDb(branch, availablesBranch);
            
            if(availablesBranch.isEmpty()) {
                return -1;
            }
        }
        
        return availablesBranch.remove(availablesBranch.size() - 1);
    }
    
    private static MaplePlayerNPC createPlayerNPCInternal(MapleMap map, Point pos, MapleCharacter chr) {
        int mapId = map.getId();
        
        if(!canSpawnPlayerNpc(chr.getName(), mapId)) {
            return null;
        }
        
        byte branch = GameConstants.getHallOfFameBranch(chr.getJob(), mapId);
        
        int scriptId = getNextScriptId(branch);
        if (scriptId == -1) {
            return null;
        }
        
        if(pos == null) {
            if(GameConstants.isPodiumHallOfFameMap(map.getId())) {
                pos = MaplePlayerNPCPodium.getNextPlayerNpcPosition(map);
            } else {
                pos = MaplePlayerNPCPositioner.getNextPlayerNpcPosition(map);
            }
            
            if(pos == null) {
                return null;
            }
        }
        
        if(ServerConstants.USE_DEBUG) System.out.println("GOT SID " + scriptId + " POS " + pos);
        
        int worldId = chr.getWorld();
        int jobId = (chr.getJob().getId() / 100) * 100;
        
        MaplePlayerNPC ret;
        int npcId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE scriptid = ?");
            ps.setInt(1, scriptId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {   // creates new playernpc if scriptid doesn't exist
                rs.close();
                ps.close();

                ps = con.prepareStatement("INSERT INTO playernpcs (name, hair, face, skin, gender, x, cy, world, map, scriptid, dir, fh, rx0, rx1, worldrank, overallrank, worldjobrank, job) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, chr.getName());
                ps.setInt(2, chr.getHair());
                ps.setInt(3, chr.getFace());
                ps.setInt(4, chr.getSkinColor().getId());
                ps.setInt(5, chr.getGender());
                ps.setInt(6, pos.x);
                ps.setInt(7, pos.y);
                ps.setInt(8, worldId);
                ps.setInt(9, mapId);
                ps.setInt(10, scriptId);
                ps.setInt(11, 1);    // default direction
                ps.setInt(12, map.getFootholds().findBelow(pos).getId());
                ps.setInt(13, pos.x + 50);
                ps.setInt(14, pos.x - 50);                
                ps.setInt(15, runningWorldRank.get(worldId).getAndIncrement());
                ps.setInt(16, runningOverallRank.getAndIncrement());
                ps.setInt(17, getAndIncrementRunningWorldJobRanks(worldId, jobId));
                ps.setInt(18, jobId);
                
                ps.executeUpdate();

                rs = ps.getGeneratedKeys();
                rs.next();
                npcId = rs.getInt(1);
                rs.close();
                ps.close();

                ps = con.prepareStatement("INSERT INTO playernpcs_equip (npcid, equipid, equippos) VALUES (?, ?, ?)");
                ps.setInt(1, npcId);
                for (Item equip : chr.getInventory(MapleInventoryType.EQUIPPED)) {
                    int position = Math.abs(equip.getPosition());
                    if ((position < 12 && position > 0) || (position > 100 && position < 112)) {
                        ps.setInt(2, equip.getItemId());
                        ps.setInt(3, equip.getPosition());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                ps.close();

                ps = con.prepareStatement("SELECT * FROM playernpcs WHERE id = ?");
                ps.setInt(1, npcId);
                rs = ps.executeQuery();

                rs.next();
                ret = new MaplePlayerNPC(rs);
            } else {
                ret = null;
            }

            rs.close();
            ps.close();
            con.close();

            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static List<Integer> removePlayerNPCInternal(MapleMap map, MapleCharacter chr) {
        Set<Integer> updateMapids = new HashSet<>();
        
        List<Integer> mapids = new LinkedList<>();
        mapids.add(chr.getWorld());
        
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id, map FROM playernpcs WHERE name LIKE ?" + (map != null ? " AND map = ?" : ""));
            ps.setString(1, chr.getName());
            if(map != null) ps.setInt(2, map.getId());
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                updateMapids.add(rs.getInt("map"));
                int npcId = rs.getInt("id");
                
                PreparedStatement ps2 = con.prepareStatement("DELETE FROM playernpcs WHERE id = ?");
                ps2.setInt(1, npcId);
                ps2.executeUpdate();
                ps2.close();

                ps2 = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ?");
                ps2.setInt(1, npcId);
                ps2.executeUpdate();
                ps2.close();
            }
            
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        for(Integer i : updateMapids) {
            mapids.add(i);
        }
        
        return mapids;
    }
    
    private static synchronized Pair<MaplePlayerNPC, List<Integer>> processPlayerNPCInternal(MapleMap map, Point pos, MapleCharacter chr, boolean create) {
        if(create) {
            return new Pair<>(createPlayerNPCInternal(map, pos, chr), null);
        } else {
            return new Pair<>(null, removePlayerNPCInternal(map, chr));
        }
    }
    
    public static boolean spawnPlayerNPC(int mapid, MapleCharacter chr) {
        return spawnPlayerNPC(mapid, null, chr);
    }
    
    public static boolean spawnPlayerNPC(int mapid, Point pos, MapleCharacter chr) {
        if(chr == null) return false;
        
        MaplePlayerNPC pn = processPlayerNPCInternal(chr.getClient().getChannelServer().getMapFactory().getMap(mapid), pos, chr, true).getLeft();
        if(pn != null) {
            for (Channel channel : Server.getInstance().getChannelsFromWorld(chr.getWorld())) {
                MapleMap m = channel.getMapFactory().getMap(mapid);
                
                m.addPlayerNPCMapObject(pn);
                m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
                m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
            }
            
            return true;
        } else {
            return false;
        }
    }
    
    private static MaplePlayerNPC getPlayerNPCFromWorldMap(String name, int world, int map) {
        World wserv = Server.getInstance().getWorld(world);
        for(MapleMapObject pnpcObj : wserv.getChannel(1).getMapFactory().getMap(map).getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER_NPC))) {
            MaplePlayerNPC pn = (MaplePlayerNPC) pnpcObj;
            
            if(name.contentEquals(pn.getName()) && pn.getScriptId() < 9977777) {
                return pn;
            }
        }
        
        return null;
    }
    
    public static void removePlayerNPC(MapleCharacter chr) {
        if(chr == null) return;
        
        List<Integer> updateMapids = processPlayerNPCInternal(null, null, chr, false).getRight();
        int worldid = updateMapids.remove(0);
        
        for (Integer mapid : updateMapids) {
            MaplePlayerNPC pn = getPlayerNPCFromWorldMap(chr.getName(), worldid, mapid);
            
            if(pn != null) {
                for (Channel channel : Server.getInstance().getChannelsFromWorld(worldid)) {
                    MapleMap m = channel.getMapFactory().getMap(mapid);
                    m.removeMapObject(pn);

                    m.broadcastMessage(MaplePacketCreator.removeNPCController(pn.getObjectId()));
                    m.broadcastMessage(MaplePacketCreator.removePlayerNPC(pn.getObjectId()));
                }
            }
        }
    }
    
    public static void multicastSpawnPlayerNPC(int mapid, int world) {
        World wserv = Server.getInstance().getWorld(world);
        for(MapleCharacter mc : wserv.getAllCharactersView()) {
            spawnPlayerNPC(mapid, mc);
        }
    }
    
    public static void removeAllPlayerNPC() {
        try {
            Connection con = DatabaseConnection.getConnection();
            
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT world, map FROM playernpcs");
            ResultSet rs = ps.executeQuery();
            
            int wsize = Server.getInstance().getWorldsSize();
            while(rs.next()) {
                int world = rs.getInt("world"), map = rs.getInt("map");
                if(world >= wsize) continue;
                
                World w = Server.getInstance().getWorld(world);
                for (Channel channel : w.getChannels()) {
                    MapleMap m = channel.getMapFactory().getMap(map);
                    
                    for(MapleMapObject pnpcObj : m.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER_NPC))) {
                        MaplePlayerNPC pn = (MaplePlayerNPC) pnpcObj;

                        m.removeMapObject(pn);
                        m.broadcastMessage(MaplePacketCreator.removeNPCController(pn.getObjectId()));
                        m.broadcastMessage(MaplePacketCreator.removePlayerNPC(pn.getObjectId()));
                    }
                }
            }
            
            rs.close();
            ps.close();
            
            ps = con.prepareStatement("DELETE FROM playernpcs");
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("DELETE FROM playernpcs_equip");
            ps.executeUpdate();
            ps.close();
            
            ps = con.prepareStatement("DELETE FROM playernpcs_field");
            ps.executeUpdate();
            ps.close();
            
            for(World w : Server.getInstance().getWorlds()) {
                w.resetPlayerNpcMapData();
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}