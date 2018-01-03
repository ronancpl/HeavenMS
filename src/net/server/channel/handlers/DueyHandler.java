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
package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import constants.ServerConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import net.AbstractMaplePacketHandler;
import net.server.channel.Channel;
import server.DueyPackages;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DueyHandler extends AbstractMaplePacketHandler {
    private enum Actions {
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

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	if (!ServerConstants.USE_DUEY){
            c.announce(MaplePacketCreator.enableActions());
            return;
    	}
            
        byte operation = slea.readByte();
        if (operation == Actions.TOSERVER_SEND_ITEM.getCode()) {
            final int fee = 5000;
            byte inventId = slea.readByte();
            short itemPos = slea.readShort();
            short amount = slea.readShort();
            int mesos = slea.readInt();
            String recipient = slea.readMapleAsciiString();
            
            if (mesos < 0 || ((long) mesos + fee + getFee(mesos)) > Integer.MAX_VALUE || (amount < 1 && mesos == 0)) {
            	AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with duey.");
            	FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to use duey with mesos " + mesos + " and amount " + amount + "\r\n");           	
            	c.disconnect(true, false);
            	return;
            }
            int finalcost = mesos + fee + getFee(mesos);
            boolean send = false;
            if (c.getPlayer().getMeso() >= finalcost) {
                int accid = getAccIdFromCNAME(recipient, true);
                if (accid != -1) {
                    if (accid != c.getAccID()) {
                        send = true;
                    } else {
                        c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_SEND_SAMEACC_ERROR.getCode()));
                    }
                } else {
                    c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_SEND_NAME_DOES_NOT_EXIST.getCode()));
                }
            } else {
                c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_SEND_NOT_ENOUGH_MESOS.getCode()));
            }
            
            MapleClient rClient = null;
                int channel = c.getWorldServer().find(recipient);
                if (channel > -1) {
                    Channel rcserv = c.getWorldServer().getChannel(channel);
                    rClient = rcserv.getPlayerStorage().getCharacterByName(recipient).getClient();
                }
            if (send) {
                if (inventId > 0) {
                    MapleInventoryType inv = MapleInventoryType.getByType(inventId);
                    Item item = c.getPlayer().getInventory(inv).getItem(itemPos);
                    if (item != null && c.getPlayer().getItemQuantity(item.getItemId(), false) >= amount) {
                        c.getPlayer().gainMeso(-finalcost, false);
                        c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_SEND_SUCCESSFULLY_SENT.getCode()));
                        
                        if (ItemConstants.isRechargable(item.getItemId())) {
                            MapleInventoryManipulator.removeFromSlot(c, inv, itemPos, item.getQuantity(), true);
                        } else {
                            MapleInventoryManipulator.removeFromSlot(c, inv, itemPos, amount, true, false);
                        }
                        
                        addItemToDB(item, amount, mesos, c.getPlayer().getName(), getAccIdFromCNAME(recipient, false));
                    } else {
                        if (item != null) {
                            c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_SEND_INCORRECT_REQUEST.getCode()));
                        }
                        return;
                    }
                } else {
                    c.getPlayer().gainMeso(-finalcost, false);
                    c.announce(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_SEND_SUCCESSFULLY_SENT.getCode()));    
                    
                    addMesoToDB(mesos, c.getPlayer().getName(), getAccIdFromCNAME(recipient, false));
                }
                
                if (rClient != null && rClient.isLoggedIn() && !rClient.getPlayer().isAwayFromWorld()) {
                    showDueyNotification(rClient, rClient.getPlayer());
                }
            }
        } else if (operation == Actions.TOSERVER_REMOVE_PACKAGE.getCode()) {
            int packageid = slea.readInt();
            removeItemFromDB(packageid);
            c.announce(MaplePacketCreator.removeItemFromDuey(true, packageid));
        } else if (operation == Actions.TOSERVER_CLAIM_PACKAGE.getCode()) {
            int packageid = slea.readInt();
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
                
                long gainmesos = 0;
                long totalmesos = (long) dp.getMesos() + (long) c.getPlayer().getMeso();

                if (totalmesos < 0 || dp.getMesos() < 0) gainmesos = 0;
                else {
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
        }
    }

    private void addMesoToDB(int mesos, String sName, int recipientID) {
        addItemToDB(null, 1, mesos, sName, recipientID);
    }

    private void addItemToDB(Item item, int quantity, int mesos, String sName, int recipientID) {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO dueypackages (RecieverId, SenderName, Mesos, TimeStamp, Checked, Type) VALUES (?, ?, ?, ?, ?, ?)")) {
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
                            ps2 = con.prepareStatement("INSERT INTO dueyitems (PackageId, itemid, quantity, upgradeslots, level, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            Equip eq = (Equip) item;
                            ps2.setInt(2, eq.getItemId());
                            ps2.setInt(3, 1);
                            ps2.setInt(4, eq.getUpgradeSlots());
                            ps2.setInt(5, eq.getLevel());
                            ps2.setInt(6, eq.getStr());
                            ps2.setInt(7, eq.getDex());
                            ps2.setInt(8, eq.getInt());
                            ps2.setInt(9, eq.getLuk());
                            ps2.setInt(10, eq.getHp());
                            ps2.setInt(11, eq.getMp());
                            ps2.setInt(12, eq.getWatk());
                            ps2.setInt(13, eq.getMatk());
                            ps2.setInt(14, eq.getWdef());
                            ps2.setInt(15, eq.getMdef());
                            ps2.setInt(16, eq.getAcc());
                            ps2.setInt(17, eq.getAvoid());
                            ps2.setInt(18, eq.getHands());
                            ps2.setInt(19, eq.getSpeed());
                            ps2.setInt(20, eq.getJump());
                            ps2.setString(21, eq.getOwner());
                        } else {
                            ps2 = con.prepareStatement("INSERT INTO dueyitems (PackageId, itemid, quantity, owner) VALUES (?, ?, ?, ?)");
                            ps2.setInt(2, item.getItemId());
                            ps2.setInt(3, quantity);
                            ps2.setString(4, item.getOwner());
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

    public static List<DueyPackages> loadItems(MapleCharacter chr) {
        List<DueyPackages> packages = new LinkedList<>();
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages dp LEFT JOIN dueyitems di ON dp.PackageId=di.PackageId WHERE RecieverId = ?")) {
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

    private String getCurrentDate() {
        String date = "";
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE) - 1; // instant duey ?
        int month = cal.get(Calendar.MONTH) + 1; // its an array of months.
        int year = cal.get(Calendar.YEAR);
        date += day < 9 ? "0" + day + "-" : "" + day + "-";
        date += month < 9 ? "0" + month + "-" : "" + month + "-";
        date += year;
        
        return date;
    }

    private static int getFee(int meso) {
        int fee = 0;
        if (meso >= 10000000) {
            fee = meso / 25;
        } else if (meso >= 5000000) {
            fee = meso * 3 / 100;
        } else if (meso >= 1000000) {
            fee = meso / 50;
        } else if (meso >= 100000) {
            fee = meso / 100;
        } else if (meso >= 50000) {
            fee = meso / 200;
        }
        return fee;
    }

    private void removeItemFromDB(int packageid) {
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
                eq.setOwner(rs.getString("owner"));
                dueypack = new DueyPackages(rs.getInt("PackageId"), eq);
            } else if (rs.getInt("type") == 2) {
                Item newItem = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
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
            ps = con.prepareStatement("SELECT Mesos FROM dueypackages WHERE RecieverId = ? and Checked = 1");
            ps.setInt(1, player.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    Connection con2 = DatabaseConnection.getConnection();
                    pss = con2.prepareStatement("UPDATE dueypackages SET Checked = 0 where RecieverId = ?");
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
}
