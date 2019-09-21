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
package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.server.Server;
import net.server.world.World;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author Jay Estrella - Mr.Trash :3
 * @author Ubaware
 */
public class MapleFamily {

    private static final AtomicInteger familyIDCounter = new AtomicInteger();

    private final int id, world;
    private final Map<Integer, MapleFamilyEntry> members = new ConcurrentHashMap<Integer, MapleFamilyEntry>();
    private MapleFamilyEntry leader;
    private String name;
    private String preceptsMessage = "";
    private int totalGenerations;

    public MapleFamily(int id, int world) {
        int newId = id;
        if(id == -1) {
            // get next available family id
            while(idInUse(newId = familyIDCounter.incrementAndGet())) {
            }
        }
        this.id = newId;
        this.world = world;
    }

    private static boolean idInUse(int id) {
        for(World world : Server.getInstance().getWorlds()) {
            if(world.getFamily(id) != null) return true;
        }
        return false;
    }

    public int getID() {
        return id;
    }

    public int getWorld() {
        return world;
    }

    public void setLeader(MapleFamilyEntry leader) {
        this.leader = leader;
        setName(leader.getName());
    }

    public MapleFamilyEntry getLeader() {
        return leader;
    }

    private void setName(String name) {
        this.name = name;
    }

    public int getTotalMembers() {
        return members.size();
    }
    
    public int getTotalGenerations() {
        return totalGenerations;
    }
    
    public void setTotalGenerations(int generations) {
        this.totalGenerations = generations;
    }

    public String getName() {
        return this.name;
    }

    public void setMessage(String message, boolean save) {
        this.preceptsMessage = message;
        if(save) {
            try (Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE family_character SET precepts = ? WHERE cid = ?")) {
                ps.setString(1, message);
                ps.setInt(2, getLeader().getChrId());
                ps.executeUpdate();
            } catch(SQLException e) {
                FilePrinter.printError(FilePrinter.FAMILY_ERROR, e, "Could not save new precepts for family " + getID() + ".");
                e.printStackTrace();
            }
        }
    }

    public String getMessage() {
        return preceptsMessage;
    }

    public void addEntry(MapleFamilyEntry entry) {
        members.put(entry.getChrId(), entry);
    }

    public void removeEntryBranch(MapleFamilyEntry root) {
        members.remove(root.getChrId());
        for(MapleFamilyEntry junior : root.getJuniors()) {
            if(junior != null) removeEntryBranch(junior);
        }
    }
    
    public void addEntryTree(MapleFamilyEntry root) {
        members.put(root.getChrId(), root);
        for(MapleFamilyEntry junior : root.getJuniors()) {
            if(junior != null) addEntryTree(junior);
        }
    }

    public MapleFamilyEntry getEntryByID(int cid) {
        return members.get(cid);
    }

    public void broadcast(byte[] packet) {
        broadcast(packet, -1);
    }

    public void broadcast(byte[] packet, int ignoreID) {
        for(MapleFamilyEntry entry : members.values()) {
            MapleCharacter chr = entry.getChr();
            if(chr != null) {
                if(chr.getId() == ignoreID) continue;
                chr.getClient().announce(packet);
            }
        }
    }
    
    public void broadcastFamilyInfoUpdate() {
        for(MapleFamilyEntry entry : members.values()) {
            MapleCharacter chr = entry.getChr();
            if(chr != null) {
                chr.getClient().announce(MaplePacketCreator.getFamilyInfo(entry));
            }
        }
    }
    
    public void resetDailyReps() {
        for(MapleFamilyEntry entry : members.values()) {
            entry.setTodaysRep(0);
            entry.setRepsToSenior(0);
            entry.resetEntitlementUsages();
        }
    }

    public static void loadAllFamilies() {
        try(Connection con = DatabaseConnection.getConnection()) {
            List<Pair<Pair<Integer, Integer>, MapleFamilyEntry>> unmatchedJuniors = new ArrayList<Pair<Pair<Integer, Integer>, MapleFamilyEntry>>(200); // <<world, seniorid> familyEntry>
            try(PreparedStatement psEntries = con.prepareStatement("SELECT * FROM family_character")) {
                ResultSet rsEntries = psEntries.executeQuery();
                while(rsEntries.next()) { // can be optimized
                    int cid = rsEntries.getInt("cid");
                    String name = null;
                    int level = -1;
                    int jobID = -1;
                    int world = -1;
                    try(PreparedStatement ps = con.prepareStatement("SELECT world, name, level, job FROM characters WHERE id = ?")) {
                        ps.setInt(1, cid);
                        ResultSet rs = ps.executeQuery();
                        if(rs.next()) {
                            world = rs.getInt("world");
                            name = rs.getString("name");
                            level = rs.getInt("level");
                            jobID = rs.getInt("job");
                        } else {
                            FilePrinter.printError(FilePrinter.FAMILY_ERROR, "Could not load character information of " + cid + " in loadAllFamilies(). (RECORD DOES NOT EXIST)");
                            continue;
                        }
                    } catch(SQLException e) {
                        FilePrinter.printError(FilePrinter.FAMILY_ERROR, e, "Could not load character information of " + cid + " in loadAllFamilies(). (SQL ERROR)");
                        continue;
                    }
                    int familyid = rsEntries.getInt("familyid");
                    int seniorid = rsEntries.getInt("seniorid");
                    int reputation = rsEntries.getInt("reputation");
                    int todaysRep = rsEntries.getInt("todaysrep");
                    int totalRep = rsEntries.getInt("totalreputation");
                    int repsToSenior = rsEntries.getInt("reptosenior");
                    String precepts = rsEntries.getString("precepts");
                    //Timestamp lastResetTime = rsEntries.getTimestamp("lastresettime"); //taken care of by FamilyDailyResetTask
                    World wserv = Server.getInstance().getWorld(world);
                    if (wserv == null) {
                        continue;
                    }
                    MapleFamily family = wserv.getFamily(familyid);
                    if(family == null) {
                        family = new MapleFamily(familyid, world);
                        Server.getInstance().getWorld(world).addFamily(familyid, family);
                    }
                    MapleFamilyEntry familyEntry = new MapleFamilyEntry(family, cid, name, level, MapleJob.getById(jobID));
                    family.addEntry(familyEntry);
                    if(seniorid <= 0) {
                        family.setLeader(familyEntry);
                        family.setMessage(precepts, false);
                    }
                    MapleFamilyEntry senior = family.getEntryByID(seniorid);
                    if(senior != null) {
                        familyEntry.setSenior(family.getEntryByID(seniorid), false);
                    } else {
                        if(seniorid > 0) unmatchedJuniors.add(new Pair<Pair<Integer, Integer>, MapleFamilyEntry>(new Pair<Integer, Integer>(world, seniorid), familyEntry));
                    }
                    familyEntry.setReputation(reputation);
                    familyEntry.setTodaysRep(todaysRep);
                    familyEntry.setTotalReputation(totalRep);
                    familyEntry.setRepsToSenior(repsToSenior);
                    //load used entitlements
                    try (PreparedStatement ps = con.prepareStatement("SELECT entitlementid FROM family_entitlement WHERE charid = ?")) {
                        ps.setInt(1, familyEntry.getChrId());
                        ResultSet rs = ps.executeQuery();
                        while(rs.next()) {
                            familyEntry.setEntitlementUsed(rs.getInt("entitlementid"));
                        }
                    }
                }
            } catch(SQLException e) {
                FilePrinter.printError(FilePrinter.FAMILY_ERROR, e, "Could not get family_character entries.");
                e.printStackTrace();
            }
            // link missing ones (out of order)
            for(Pair<Pair<Integer, Integer>, MapleFamilyEntry> unmatchedJunior : unmatchedJuniors) {
                int world = unmatchedJunior.getLeft().getLeft();
                int seniorid = unmatchedJunior.getLeft().getRight();
                MapleFamilyEntry junior = unmatchedJunior.getRight();
                MapleFamilyEntry senior = Server.getInstance().getWorld(world).getFamily(junior.getFamily().getID()).getEntryByID(seniorid);
                if(senior != null) {
                    junior.setSenior(senior, false);
                } else {
                    FilePrinter.printError(FilePrinter.FAMILY_ERROR, "Missing senior for character " + junior.getName() + " in world " + world);
                }
            }
        } catch(SQLException e) {
            FilePrinter.printError(FilePrinter.FAMILY_ERROR, e, "Could not get DB connection.");
            e.printStackTrace();
        }
        for(World world : Server.getInstance().getWorlds()) {
            for(MapleFamily family : world.getFamilies()) {
                family.getLeader().doFullCount();
            }
        }
    }

    public void saveAllMembersRep() { //was used for autosave task, but character autosave should be enough
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            boolean success = true;
            for(MapleFamilyEntry entry : members.values()) {
                success = entry.saveReputation(con);
                if(!success) break;
            }
            if(!success) {
                con.rollback();
                FilePrinter.printError(FilePrinter.FAMILY_ERROR, "Family rep autosave failed for family " + getID() + " on " + Calendar.getInstance().getTime().toString() + ".");
            }
            con.setAutoCommit(true);
            //reset repChanged after successful save
            for(MapleFamilyEntry entry : members.values()) {
                entry.savedSuccessfully();
            }
        } catch(SQLException e) {
            FilePrinter.printError(FilePrinter.FAMILY_ERROR, e, "Could not get connection to DB.");
            e.printStackTrace();
        }
    }
}
