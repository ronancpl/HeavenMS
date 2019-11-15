/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2019 RonanLana (HeavenMS)

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
package client.processor.npc;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import client.inventory.manipulator.MapleInventoryManipulator;
import java.util.Collections;
import net.server.Server;
import net.server.world.World;
import server.MapleItemInformationProvider;
import server.maps.MapleHiredMerchant;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author RonanLana - synchronization of Fredrick modules and operation results
 */
public class FredrickProcessor {
    
    private static int[] dailyReminders = new int[]{2, 5, 10, 15, 30, 60, 90, Integer.MAX_VALUE};
    
    private static byte canRetrieveFromFredrick(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items) {
        if (!MapleInventory.checkSpotsAndOwnership(chr, items)) {
            List<Integer> itemids = new LinkedList<>();
            for (Pair<Item, MapleInventoryType> it : items) {
                itemids.add(it.getLeft().getItemId());
            }
            
            if (chr.canHoldUniques(itemids)) {
                return 0x22;
            } else {
                return 0x20;
            }
        }
        
        int netMeso = chr.getMerchantNetMeso();
        if (netMeso > 0) {
            if (!chr.canHoldMeso(netMeso)) {
                return 0x1F;
            }
        } else {
            if (chr.getMeso() < -1 * netMeso) {
                return 0x21;
            }
        }
        
        return 0x0;
    }
    
    public static int timestampElapsedDays(Timestamp then, long timeNow) {
        return (int) ((timeNow - then.getTime()) / (1000 * 60 * 60 * 24));
    }
    
    private static String fredrickReminderMessage(int daynotes) {
        String msg;
        
        if (daynotes < 4) {
            msg = "Hi customer! I am Fredrick, the Union Chief of the Hired Merchant Union. A reminder that " + dailyReminders[daynotes] + " days have passed since you used our service. Please reclaim your stored goods at FM Entrance.";
        } else {
            msg = "Hi customer! I am Fredrick, the Union Chief of the Hired Merchant Union. " + dailyReminders[daynotes] + " days have passed since you used our service. Consider claiming back the items before we move them away for refund.";
        }
        
        return msg;
    }
    
    public static void removeFredrickLog(int cid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            removeFredrickLog(con, cid);
            con.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    private static void removeFredrickLog(Connection con, int cid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM `fredstorage` WHERE `cid` = ?")) {
            ps.setInt(1, cid);
            ps.execute();
        }
    }
    
    public static void insertFredrickLog(int cid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            
            removeFredrickLog(con, cid);
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO `fredstorage` (`cid`, `daynotes`, `timestamp`) VALUES (?, 0, ?)")) {
                ps.setInt(1, cid);
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                ps.execute();
            }
            
            con.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    public static void removeFredrickReminders(int cid) {
        removeFredrickReminders(Collections.singletonList(new Pair<>(cid, 0)));
    }
    
    private static void removeFredrickReminders(List<Pair<Integer, Integer>> expiredCids) {
        List<String> expiredCnames = new LinkedList<>();
        for (Pair<Integer, Integer> id : expiredCids) {
            String name = MapleCharacter.getNameById(id.getLeft());
            if (name != null) {
                expiredCnames.add(name);
            }
        }
        
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM `notes` WHERE `from` LIKE ? AND `to` LIKE ?")) {
                ps.setString(1, "FREDRICK");
                
                for (String cname : expiredCnames) {
                    ps.setString(2, cname);
                    ps.executeBatch();
                }
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void runFredrickSchedule() {
        try {
            Connection con = DatabaseConnection.getConnection();
            
            List<Pair<Integer, Integer>> expiredCids = new LinkedList<>();
            List<Pair<Pair<Integer, String>, Integer>> notifCids = new LinkedList<>();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM fredstorage f LEFT JOIN (SELECT id, name, world, lastLogoutTime FROM characters) AS c ON c.id = f.cid")) {
                try (ResultSet rs = ps.executeQuery()) {
                    long curTime = System.currentTimeMillis();
                    
                    while (rs.next()) {
                        int cid = rs.getInt("cid");
                        int world = rs.getInt("world");
                        Timestamp ts = rs.getTimestamp("timestamp");
                        int daynotes = Math.min(dailyReminders.length - 1, rs.getInt("daynotes"));
                        
                        int elapsedDays = timestampElapsedDays(ts, curTime);
                        if (elapsedDays > 100) {
                            expiredCids.add(new Pair<>(cid, world));
                        } else {
                            int notifDay = dailyReminders[daynotes];
                            
                            if (elapsedDays >= notifDay) {
                                do {
                                    daynotes++;
                                    notifDay = dailyReminders[daynotes];
                                } while (elapsedDays >= notifDay);
                                
                                Timestamp logoutTs = rs.getTimestamp("lastLogoutTime");
                                int inactivityDays = timestampElapsedDays(logoutTs, curTime);
                                
                                if (inactivityDays < 7 || daynotes >= dailyReminders.length - 1) {  // don't spam inactive players
                                    String name = rs.getString("name");
                                    notifCids.add(new Pair<>(new Pair<>(cid, name), daynotes));
                                }
                            }
                        }
                    }
                }
            }
            
            if (!expiredCids.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM `inventoryitems` WHERE `type` = ? AND `characterid` = ?")) {
                    ps.setInt(1, ItemFactory.MERCHANT.getValue());

                    for (Pair<Integer, Integer> cid : expiredCids) {
                        ps.setInt(2, cid.getLeft());
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                }
                
                try (PreparedStatement ps = con.prepareStatement("UPDATE `characters` SET `MerchantMesos` = 0 WHERE `id` = ?")) {
                    for (Pair<Integer, Integer> cid : expiredCids) {
                        ps.setInt(1, cid.getLeft());
                        ps.addBatch();
                        
                        World wserv = Server.getInstance().getWorld(cid.getRight());
                        if (wserv != null) {
                            MapleCharacter chr = wserv.getPlayerStorage().getCharacterById(cid.getLeft());
                            if (chr != null) {
                                chr.setMerchantMeso(0);
                            }
                        }
                    }
                    
                    ps.executeBatch();
                }
                
                removeFredrickReminders(expiredCids);
                
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM `fredstorage` WHERE `cid` = ?")) {
                    for (Pair<Integer, Integer> cid : expiredCids) {
                        ps.setInt(1, cid.getLeft());
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                }
            }
            
            if (!notifCids.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE `fredstorage` SET `daynotes` = ? WHERE `cid` = ?")) {
                    for (Pair<Pair<Integer, String>, Integer> cid : notifCids) {
                        ps.setInt(1, cid.getRight());
                        ps.setInt(2, cid.getLeft().getLeft());
                        ps.addBatch();
                        
                        String msg = fredrickReminderMessage(cid.getRight() - 1);
                        MapleCharacter.sendNote(cid.getLeft().getRight(), "FREDRICK", msg, (byte) 0);
                    }
                    
                    ps.executeBatch();
                }
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteFredrickItems(int cid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM `inventoryitems` WHERE `type` = ? AND `characterid` = ?")) {
                ps.setInt(1, ItemFactory.MERCHANT.getValue());
                ps.setInt(2, cid);
                ps.execute();
            }
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static void fredrickRetrieveItems(MapleClient c) {     // thanks Gustav for pointing out the dupe on Fredrick handling
        if (c.tryacquireClient()) {
            try {
                MapleCharacter chr = c.getPlayer();

                List<Pair<Item, MapleInventoryType>> items;
                try {
                    items = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
                    
                    byte response = canRetrieveFromFredrick(chr, items);
                    if (response != 0) {
                        chr.announce(MaplePacketCreator.fredrickMessage(response));
                        return;
                    }
                    
                    chr.withdrawMerchantMesos();
                    
                    if (deleteFredrickItems(chr.getId())) {
                        MapleHiredMerchant merchant = chr.getHiredMerchant();

                        if(merchant != null)
                            merchant.clearItems();

                        for (Pair<Item, MapleInventoryType> it : items) {
                            Item item = it.getLeft();
                            MapleInventoryManipulator.addFromDrop(chr.getClient(), item, false);
                            String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
                            FilePrinter.print(FilePrinter.FREDRICK + chr.getName() + ".txt", chr.getName() + " gained " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")");
                        }

                        chr.announce(MaplePacketCreator.fredrickMessage((byte) 0x1E));
                        removeFredrickLog(chr.getId());
                    } else {
                        chr.message("An unknown error has occured.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
