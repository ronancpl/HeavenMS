/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2018 RonanLana (HeavenMS)

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
package client.processor;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import client.inventory.manipulator.MapleKarmaManipulator;
import constants.ItemConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import net.server.channel.Channel;
import server.DueyPackages;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;

/**
 *
 * @author RonanLana (synchronization of Duey modules)
 */
public class DueyProcessor {
    
    public enum Actions {
        TOSERVER_SEND_ITEM(0x02),
        TOSERVER_CLAIM_PACKAGE(0x04),
        TOSERVER_REMOVE_PACKAGE(0x05),
        TOSERVER_CLOSE_DUEY(0x07),
        TOCLIENT_OPEN_DUEY(0x08),
        TOCLIENT_SEND_ENABLE_ACTIONS(0x09),
        TOCLIENT_SEND_NOT_ENOUGH_MESOS(0x0A),
        TOCLIENT_SEND_INCORRECT_REQUEST(0x0B),
        TOCLIENT_SEND_NAME_DOES_NOT_EXIST(0x0C),
        TOCLIENT_SEND_SAMEACC_ERROR(0x0D),
        TOCLIENT_SEND_RECEIVER_STORAGE_FULL(0x0E),
        TOCLIENT_SEND_RECEIVER_UNABLE_TO_RECV(0x0F),
        TOCLIENT_SEND_RECEIVER_STORAGE_WITH_UNIQUE(0x10),
        TOCLIENT_SEND_MESO_LIMIT(0x11),
        TOCLIENT_SEND_SUCCESSFULLY_SENT(0x12),
        TOCLIENT_RECV_UNKNOWN_ERROR(0x13),
        TOCLIENT_RECV_ENABLE_ACTIONS(0x14),
        TOCLIENT_RECV_NO_FREE_SLOTS(0x15),
        TOCLIENT_RECV_RECEIVER_WITH_UNIQUE(0x16),
        TOCLIENT_RECV_SUCCESSFUL_MSG(0x17),
        TOCLIENT_RECV_PACKAGE_MSG(0x1B);
        final byte code;

        private Actions(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }
    }
    
    private static int getAccIdFromCNAME(String name, boolean accountid) {
        try {
            PreparedStatement ps;
            String text = "SELECT id,accountid FROM characters WHERE name = ?";
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement(text);
            ps.setString(1, name);
            int id_;
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    return -1;
                }
                id_ = accountid ? rs.getInt("accountid") : rs.getInt("id");
            }
            ps.close();
            con.close();
            return id_;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private static String getCurrentDate() {
        String date = "";
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE) - 1; // instant duey ?
        int month = cal.get(Calendar.MONTH) + 1; // its an array of months.
        int year = cal.get(Calendar.YEAR);
        date += day <= 9 ? "0" + day + "-" : "" + day + "-";
        date += month <= 9 ? "0" + month + "-" : "" + month + "-";
        date += year;
        
        return date;
    }

    private static void removeItemFromDB(int packageid) {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            
            PreparedStatement ps = con.prepareStatement("DELETE FROM dueypackages WHERE PackageId = ?");
            ps.setInt(1, packageid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM dueyitems WHERE PackageId = ?");
            ps.setInt(1, packageid);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static DueyPackages getItemByPID(ResultSet rs) {
        try {
            DueyPackages dueypack;
            if (rs.getInt("type") == 1) {
                Equip eq = new Equip(rs.getInt("itemid"), (byte) 0, -1);
                eq.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                eq.setLevel((byte) rs.getInt("level"));
                eq.setItemLevel((byte) rs.getInt("itemlevel"));
                eq.setItemExp(rs.getInt("itemexp"));
                eq.setStr((short) rs.getInt("str"));
                eq.setDex((short) rs.getInt("dex"));
                eq.setInt((short) rs.getInt("int"));
                eq.setLuk((short) rs.getInt("luk"));
                eq.setHp((short) rs.getInt("hp"));
                eq.setMp((short) rs.getInt("mp"));
                eq.setWatk((short) rs.getInt("watk"));
                eq.setMatk((short) rs.getInt("matk"));
                eq.setWdef((short) rs.getInt("wdef"));
                eq.setMdef((short) rs.getInt("mdef"));
                eq.setAcc((short) rs.getInt("acc"));
                eq.setAvoid((short) rs.getInt("avoid"));
                eq.setHands((short) rs.getInt("hands"));
                eq.setSpeed((short) rs.getInt("speed"));
                eq.setJump((short) rs.getInt("jump"));
                eq.setFlag((byte) rs.getInt("flag"));
                eq.setOwner(rs.getString("owner"));
                dueypack = new DueyPackages(rs.getInt("PackageId"), eq);
            } else if (rs.getInt("type") == 2) {
                Item newItem = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                newItem.setFlag((byte) rs.getInt("flag"));
                newItem.setOwner(rs.getString("owner"));
                dueypack = new DueyPackages(rs.getInt("PackageId"), newItem);
            } else {
                dueypack = new DueyPackages(rs.getInt("PackageId"));
            }
            return dueypack;
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        }
    }
    
    private static void showDueyNotification(MapleClient c, MapleCharacter player) {
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pss = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT Mesos FROM dueypackages WHERE ReceiverId = ? and Checked = 1");
            ps.setInt(1, player.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    Connection con2 = DatabaseConnection.getConnection();
                    pss = con2.prepareStatement("UPDATE dueypackages SET Checked = 0 where ReceiverId = ?");
                    pss.setInt(1, player.getId());
                    pss.executeUpdate();
                    pss.close();
                    con2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                c.announce(MaplePacketCreator.sendDueyNotification(false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pss != null) {
                    pss.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static int getFee(long meso) {
        long fee = 0;
        if (meso >= 100000000) {
            fee = (meso * 6) / 100;
        } else if (meso >= 25000000) {
            fee = (meso * 5) / 100;
        } else if (meso >= 10000000) {
            fee = (meso * 4) / 100;
        } else if (meso >= 5000000) {
            fee = (meso * 3) / 100;
        } else if (meso >= 1000000) {
            fee = (meso * 18) / 1000;
        } else if (meso >= 100000) {
            fee = (meso * 8) / 1000;
        }
        return (int) fee;
    }
    
    private static void addMesoToDB(int mesos, String sName, int recipientID) {
        addItemToDB(null, 1, mesos, sName, recipientID);
    }

    public static void addItemToDB(Item item, int quantity, int mesos, String sName, int recipientID) {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO dueypackages (ReceiverId, SenderName, Mesos, TimeStamp, Checked, Type) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, recipientID);
                ps.setString(2, sName);
                ps.setInt(3, mesos);
                ps.setString(4, getCurrentDate());
                ps.setInt(5, 1);
                if (item == null) {
                    ps.setInt(6, 3);
                    ps.executeUpdate();
                } else {
                    ps.setInt(6, item.getItemType());
                    
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        PreparedStatement ps2;
                        if (item.getInventoryType().equals(MapleInventoryType.EQUIP)) {
                            ps2 = con.prepareStatement("INSERT INTO dueyitems (PackageId, itemid, quantity, upgradeslots, level, itemlevel, itemexp, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, flag, owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            Equip eq = (Equip) item;
                            ps2.setInt(2, eq.getItemId());
                            ps2.setInt(3, 1);
                            ps2.setInt(4, eq.getUpgradeSlots());
                            ps2.setInt(5, eq.getLevel());
                            ps2.setInt(6, eq.getItemLevel());
                            ps2.setInt(7, eq.getItemExp());
                            ps2.setInt(8, eq.getStr());
                            ps2.setInt(9, eq.getDex());
                            ps2.setInt(10, eq.getInt());
                            ps2.setInt(11, eq.getLuk());
                            ps2.setInt(12, eq.getHp());
                            ps2.setInt(13, eq.getMp());
                            ps2.setInt(14, eq.getWatk());
                            ps2.setInt(15, eq.getMatk());
                            ps2.setInt(16, eq.getWdef());
                            ps2.setInt(17, eq.getMdef());
                            ps2.setInt(18, eq.getAcc());
                            ps2.setInt(19, eq.getAvoid());
                            ps2.setInt(20, eq.getHands());
                            ps2.setInt(21, eq.getSpeed());
                            ps2.setInt(22, eq.getJump());
                            ps2.setInt(23, eq.getFlag());
                            ps2.setString(24, eq.getOwner());
                        } else {
                            ps2 = con.prepareStatement("INSERT INTO dueyitems (PackageId, itemid, quantity, flag, owner) VALUES (?, ?, ?, ?, ?)");
                            ps2.setInt(2, item.getItemId());
                            ps2.setInt(3, quantity);
                            ps2.setInt(4, item.getFlag());
                            ps2.setString(5, item.getOwner());
                        }
                        ps2.setInt(1, rs.getInt(1));
                        ps2.executeUpdate();
                        ps2.close();
                    }
                }
            }
            
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<DueyPackages> loadItems(MapleCharacter chr) {
        List<DueyPackages> packages = new LinkedList<>();
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages dp LEFT JOIN dueyitems di ON dp.PackageId=di.PackageId WHERE ReceiverId = ?")) {
                ps.setInt(1, chr.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DueyPackages dueypack = getItemByPID(rs);
                        dueypack.setSender(rs.getString("SenderName"));
                        dueypack.setMesos(rs.getInt("Mesos"));
                        dueypack.setSentTime(rs.getString("TimeStamp"));
                        packages.add(dueypack);
                    }
                }
            }
            
            con.close();
            return packages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void dueySendItem(MapleClient c, byte inventId, short itemPos, short amount, int mesos, String recipient) {
        if (c.tryacquireClient()) {
            try {
                final int fee = 5000;
                final long sendMesos = (long) mesos + fee;
                if (mesos < 0 || sendMesos > Integer.MAX_VALUE || (amount < 1 && mesos == 0)) {
                    AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with duey.");
                    FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to use duey with mesos " + mesos + " and amount " + amount + "\r\n");           	
                    c.disconnect(true, false);
                    return;
                }

                int finalcost = mesos + fee;
                if (c.getPlayer().getMeso() >= finalcost) {
                    int accid = getAccIdFromCNAME(recipient, true);
                    if (accid != -1) {
                        if (accid == c.getAccID()) {
                            c.announce(MaplePacketCreator.sendDueyMSG(DueyProcessor.Actions.TOCLIENT_SEND_SAMEACC_ERROR.getCode()));
                            return;
                        }
                    } else {
                        c.announce(MaplePacketCreator.sendDueyMSG(DueyProcessor.Actions.TOCLIENT_SEND_NAME_DOES_NOT_EXIST.getCode()));
                        return;
                    }
                } else {
                    c.announce(MaplePacketCreator.sendDueyMSG(DueyProcessor.Actions.TOCLIENT_SEND_NOT_ENOUGH_MESOS.getCode()));
                    return;
                }

                MapleClient rClient = null;
                int channel = c.getWorldServer().find(recipient);
                if (channel > -1) {
                    Channel rcserv = c.getWorldServer().getChannel(channel);
                    if(rcserv != null) {
                        MapleCharacter rChr = rcserv.getPlayerStorage().getCharacterByName(recipient);
                        if(rChr != null) {
                            rClient = rChr.getClient();
                        }
                    }
                }

                if (inventId > 0) {
                    MapleInventoryType inv = MapleInventoryType.getByType(inventId);
                    Item item = c.getPlayer().getInventory(inv).getItem(itemPos);
                    if (item != null && c.getPlayer().getItemQuantity(item.getItemId(), false) >= amount) {
                        c.getPlayer().gainMeso(-finalcost, false);
                        c.announce(MaplePacketCreator.sendDueyMSG(DueyProcessor.Actions.TOCLIENT_SEND_SUCCESSFULLY_SENT.getCode()));

                        if (ItemConstants.isRechargeable(item.getItemId())) {
                            MapleInventoryManipulator.removeFromSlot(c, inv, itemPos, item.getQuantity(), true);
                        } else {
                            MapleInventoryManipulator.removeFromSlot(c, inv, itemPos, amount, true, false);
                        }

                        MapleKarmaManipulator.toggleKarmaFlagToUntradeable(item);
                        addItemToDB(item, amount, mesos - getFee(mesos), c.getPlayer().getName(), getAccIdFromCNAME(recipient, false));
                    } else {
                        if (item != null) {
                            c.announce(MaplePacketCreator.sendDueyMSG(DueyProcessor.Actions.TOCLIENT_SEND_INCORRECT_REQUEST.getCode()));
                        }
                        return;
                    }
                } else {
                    c.getPlayer().gainMeso(-finalcost, false);
                    c.announce(MaplePacketCreator.sendDueyMSG(DueyProcessor.Actions.TOCLIENT_SEND_SUCCESSFULLY_SENT.getCode()));    

                    addMesoToDB(mesos - getFee(mesos), c.getPlayer().getName(), getAccIdFromCNAME(recipient, false));
                }

                if (rClient != null && rClient.isLoggedIn() && !rClient.getPlayer().isAwayFromWorld()) {
                    showDueyNotification(rClient, rClient.getPlayer());
                }
            } finally {
                c.releaseClient();
            }
        }
    }
    
    public static void dueyRemovePackage(MapleClient c, int packageid) {
        if (c.tryacquireClient()) {
            try {
                removeItemFromDB(packageid);
                c.announce(MaplePacketCreator.removeItemFromDuey(true, packageid));
            } finally {
                c.releaseClient();
            }
        }
    }
    
    public static void dueyClaimPackage(MapleClient c, int packageid) {
        if (c.tryacquireClient()) {
            try {
                List<DueyPackages> packages = new LinkedList<>();
                DueyPackages dp = null;
                Connection con = null;
                try {
                    con = DatabaseConnection.getConnection();
                    DueyPackages dueypack;
                    try (PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages LEFT JOIN dueyitems USING (PackageId) WHERE PackageId = ?")) {
                        ps.setInt(1, packageid);
                        try (ResultSet rs = ps.executeQuery()) {
                            dueypack = null;
                            if (rs.next()) {
                                dueypack = getItemByPID(rs);
                                dueypack.setSender(rs.getString("SenderName"));
                                dueypack.setMesos(rs.getInt("Mesos"));
                                dueypack.setSentTime(rs.getString("TimeStamp"));

                                packages.add(dueypack);
                            }
                        }
                    }
                    dp = dueypack;
                    if(dp == null) {
                        c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_RECV_UNKNOWN_ERROR.getCode()));
                        FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to receive package from duey with id " + packageid + "\r\n");
                        return;
                    }

                    if (dp.getItem() != null) {
                        if (!MapleInventoryManipulator.checkSpace(c, dp.getItem().getItemId(), dp.getItem().getQuantity(), dp.getItem().getOwner())) {
                            int itemid = dp.getItem().getItemId();
                            if(MapleItemInformationProvider.getInstance().isPickupRestricted(itemid) && c.getPlayer().getInventory(ItemConstants.getInventoryType(itemid)).findById(itemid) != null) {
                                c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_RECV_RECEIVER_WITH_UNIQUE.getCode()));
                            } else {
                                c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_RECV_NO_FREE_SLOTS.getCode()));
                            }

                            return;
                        } else {
                            MapleInventoryManipulator.addFromDrop(c, dp.getItem(), false);
                        }
                    }

                    long gainmesos;
                    long totalmesos = (long) dp.getMesos() + c.getPlayer().getMeso();

                    if (totalmesos < 0 || dp.getMesos() < 0) {
                        gainmesos = 0;
                    } else {
                        totalmesos = Math.min(totalmesos, Integer.MAX_VALUE);
                        gainmesos = totalmesos - c.getPlayer().getMeso();
                    }

                    c.getPlayer().gainMeso((int)gainmesos, false);

                    removeItemFromDB(packageid);
                    c.announce(MaplePacketCreator.removeItemFromDuey(false, packageid));

                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } finally {
                c.releaseClient();
            }
        }
    }
    
    public static void dueySendTalk(MapleClient c) {
        if (c.tryacquireClient()) {
            try {
                c.announce(MaplePacketCreator.sendDuey((byte) 8, loadItems(c.getPlayer())));
            } finally {
                c.releaseClient();
            }
        }
    }
}
