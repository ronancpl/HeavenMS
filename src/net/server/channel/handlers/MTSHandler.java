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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.channel.Channel;
import server.MTSItemInfo;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;

public final class MTSHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // TODO add karma-to-untradeable flag on sold items here
        
        if (!c.getPlayer().getCashShop().isOpened()) {
            return;
        }
        if (slea.available() > 0) {
            byte op = slea.readByte();
            if (op == 2) { //put item up for sale
                byte itemtype = slea.readByte();
                int itemid = slea.readInt();
                slea.readShort();
                slea.skip(7);
                short stars = 1;
                if (itemtype == 1) {
                    slea.skip(32);
                } else {
                    stars = slea.readShort();
                }
                slea.readMapleAsciiString(); //another useless thing (owner)
                if (itemtype == 1) {
                    slea.skip(32);
                } else {
                    slea.readShort();
                }
                short slot;
                short quantity;
                if (itemtype != 1) {
                    if (itemid / 10000 == 207 || itemid / 10000 == 233) {
                        slea.skip(8);
                    }
                    slot = (short) slea.readInt();
                } else {
                    slot = (short) slea.readInt();
                }
                if (itemtype != 1) {
                    if (itemid / 10000 == 207 || itemid / 10000 == 233) {
                        quantity = stars;
                        slea.skip(4);
                    } else {
                        quantity = (short) slea.readInt();
                    }
                } else {
                    quantity = (byte) slea.readInt();
                }
                int price = slea.readInt();
                if (itemtype == 1) {
                    quantity = 1;
                }
                if (quantity < 0 || price < 110 || c.getPlayer().getItemQuantity(itemid, false) < quantity) {
                    return;
                }
                MapleInventoryType invType = ItemConstants.getInventoryType(itemid);
                Item i = c.getPlayer().getInventory(invType).getItem(slot).copy();
                if (i != null && c.getPlayer().getMeso() >= 5000) {
                    Connection con = null;
                    try {
                        con = DatabaseConnection.getConnection();
                        
                        PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE seller = ?");
                        ps.setInt(1, c.getPlayer().getId());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            if (rs.getInt(1) > 10) { //They have more than 10 items up for sale already!
                                c.getPlayer().dropMessage(1, "You already have 10 items up for auction!");
                                c.announce(getMTS(1, 0, 0));
                                c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                                c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
                                rs.close();
                                ps.close();
                                return;
                            }
                        }
                        rs.close();
                        ps.close();
                        Calendar calendar = Calendar.getInstance();
                        int year;
                        int month;
                        int day;
                        int oldmax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        int oldday = calendar.get(Calendar.DAY_OF_MONTH) + 7;
                        if (oldmax < oldday) {
                            if (calendar.get(Calendar.MONTH) + 2 > 12) {
                                year = calendar.get(Calendar.YEAR) + 1;
                                month = 1;
                                calendar.set(year, month, 1);
                                day = oldday - oldmax;
                            } else {
                                month = calendar.get(Calendar.MONTH) + 2;
                                year = calendar.get(Calendar.YEAR);
                                calendar.set(year, month, 1);
                                day = oldday - oldmax;
                            }
                        } else {
                            day = calendar.get(Calendar.DAY_OF_MONTH) + 7;
                            month = calendar.get(Calendar.MONTH);
                            year = calendar.get(Calendar.YEAR);
                        }
                        String date = year + "-";
                        if (month < 10) {
                            date += "0" + month + "-";
                        } else {
                            date += month + "-";
                        }
                        if (day < 10) {
                            date += "0" + day;
                        } else {
                            date += day + "";
                        }
                        if (!i.getInventoryType().equals(MapleInventoryType.EQUIP)) {
                            Item item = (Item) i;
                            ps = con.prepareStatement("INSERT INTO mts_items (tab, type, itemid, quantity, seller, price, owner, sellername, sell_ends) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            ps.setInt(1, 1);
                            ps.setInt(2, (int) invType.getType());
                            ps.setInt(3, item.getItemId());
                            ps.setInt(4, quantity);
                            ps.setInt(5, c.getPlayer().getId());
                            ps.setInt(6, price);
                            ps.setString(7, item.getOwner());
                            ps.setString(8, c.getPlayer().getName());
                            ps.setString(9, date);
                        } else {
                            Equip equip = (Equip) i;
                            ps = con.prepareStatement("INSERT INTO mts_items (tab, type, itemid, quantity, seller, price, upgradeslots, level, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, locked, owner, sellername, sell_ends, vicious, flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            ps.setInt(1, 1);
                            ps.setInt(2, (int) invType.getType());
                            ps.setInt(3, equip.getItemId());
                            ps.setInt(4, quantity);
                            ps.setInt(5, c.getPlayer().getId());
                            ps.setInt(6, price);
                            ps.setInt(7, equip.getUpgradeSlots());
                            ps.setInt(8, equip.getLevel());
                            ps.setInt(9, equip.getStr());
                            ps.setInt(10, equip.getDex());
                            ps.setInt(11, equip.getInt());
                            ps.setInt(12, equip.getLuk());
                            ps.setInt(13, equip.getHp());
                            ps.setInt(14, equip.getMp());
                            ps.setInt(15, equip.getWatk());
                            ps.setInt(16, equip.getMatk());
                            ps.setInt(17, equip.getWdef());
                            ps.setInt(18, equip.getMdef());
                            ps.setInt(19, equip.getAcc());
                            ps.setInt(20, equip.getAvoid());
                            ps.setInt(21, equip.getHands());
                            ps.setInt(22, equip.getSpeed());
                            ps.setInt(23, equip.getJump());
                            ps.setInt(24, 0);
                            ps.setString(25, equip.getOwner());
                            ps.setString(26, c.getPlayer().getName());
                            ps.setString(27, date);
                            ps.setInt(28, equip.getVicious());
                            ps.setInt(29, equip.getFlag());
                        }
                        ps.executeUpdate();
                        ps.close();
                        MapleInventoryManipulator.removeFromSlot(c, invType, slot, quantity, false);
                        
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    c.getPlayer().gainMeso(-5000, false);
                    c.announce(MaplePacketCreator.MTSConfirmSell());
                    c.announce(getMTS(1, 0, 0));
                    c.enableCSActions();
                    c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                    c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
                }
            } else if (op == 3) { //send offer for wanted item
            } else if (op == 4) { //list wanted item
                slea.readInt();
                slea.readInt();
                slea.readInt();
                slea.readShort();
                slea.readMapleAsciiString();
            } else if (op == 5) { //change page
                int tab = slea.readInt();
                int type = slea.readInt();
                int page = slea.readInt();
                c.getPlayer().changePage(page);
                if (tab == 4 && type == 0) {
                    c.announce(getCart(c.getPlayer().getId()));
                } else if (tab == c.getPlayer().getCurrentTab() && type == c.getPlayer().getCurrentType() && c.getPlayer().getSearch() != null) {
                    c.announce(getMTSSearch(tab, type, c.getPlayer().getCurrentCI(), c.getPlayer().getSearch(), page));
                } else {
                    c.getPlayer().setSearch(null);
                    c.announce(getMTS(tab, type, page));
                }
                c.getPlayer().changeTab(tab);
                c.getPlayer().changeType(type);
                c.enableCSActions();
                c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
            } else if (op == 6) { //search
                int tab = slea.readInt();
                int type = slea.readInt();
                slea.readInt();
                int ci = slea.readInt();
                String search = slea.readMapleAsciiString();
                c.getPlayer().setSearch(search);
                c.getPlayer().changeTab(tab);
                c.getPlayer().changeType(type);
                c.getPlayer().changeCI(ci);
                c.enableCSActions();
                c.announce(MaplePacketCreator.enableActions());
                c.announce(getMTSSearch(tab, type, ci, search, c.getPlayer().getCurrentPage()));
                c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
                c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
            } else if (op == 7) { //cancel sale
                int id = slea.readInt(); //id of the item
                Connection con = null;
                try {
                    con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE mts_items SET transfer = 1 WHERE id = ? AND seller = ?");
                    ps.setInt(1, id);
                    ps.setInt(2, c.getPlayer().getId());
                    ps.executeUpdate();
                    ps.close();
                    ps = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    ps.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                c.enableCSActions();
                c.announce(getMTS(c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
                c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
                c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
            } else if (op == 8) { //transfer item from transfer inv.
                int id = slea.readInt(); //id of the item
                Connection con = null;
                PreparedStatement ps;
                ResultSet rs;
                try {
                    con = DatabaseConnection.getConnection();
                    ps = con.prepareStatement("SELECT * FROM mts_items WHERE seller = ? AND transfer = 1  AND id= ? ORDER BY id DESC");
                    ps.setInt(1, c.getPlayer().getId());
                    ps.setInt(2, id);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        Item i;
                        if (rs.getInt("type") != 1) {
                            Item ii = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                            ii.setOwner(rs.getString("owner"));
                            ii.setPosition(c.getPlayer().getInventory(ItemConstants.getInventoryType(rs.getInt("itemid"))).getNextFreeSlot());
                            i = ii.copy();
                        } else {
                            Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                            equip.setOwner(rs.getString("owner"));
                            equip.setQuantity((short) 1);
                            equip.setAcc((short) rs.getInt("acc"));
                            equip.setAvoid((short) rs.getInt("avoid"));
                            equip.setDex((short) rs.getInt("dex"));
                            equip.setHands((short) rs.getInt("hands"));
                            equip.setHp((short) rs.getInt("hp"));
                            equip.setInt((short) rs.getInt("int"));
                            equip.setJump((short) rs.getInt("jump"));
                            equip.setLuk((short) rs.getInt("luk"));
                            equip.setMatk((short) rs.getInt("matk"));
                            equip.setMdef((short) rs.getInt("mdef"));
                            equip.setMp((short) rs.getInt("mp"));
                            equip.setSpeed((short) rs.getInt("speed"));
                            equip.setStr((short) rs.getInt("str"));
                            equip.setWatk((short) rs.getInt("watk"));
                            equip.setWdef((short) rs.getInt("wdef"));
                            equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                            equip.setLevel((byte) rs.getInt("level"));
                            equip.setVicious((byte) rs.getInt("vicious"));
                            equip.setFlag((byte) rs.getInt("flag"));
                            equip.setPosition(c.getPlayer().getInventory(ItemConstants.getInventoryType(rs.getInt("itemid"))).getNextFreeSlot());
                            i = equip.copy();
                        }
                        try (PreparedStatement pse = con.prepareStatement("DELETE FROM mts_items WHERE id = ? AND seller = ? AND transfer = 1")) {
                            pse.setInt(1, id);
                            pse.setInt(2, c.getPlayer().getId());
                            pse.executeUpdate();
                        }
                        MapleInventoryManipulator.addFromDrop(c, i, false);
                        c.enableCSActions();
                        c.announce(getCart(c.getPlayer().getId()));
                        c.announce(getMTS(c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
                        c.announce(MaplePacketCreator.MTSConfirmTransfer(i.getQuantity(), i.getPosition()));
                        c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                    }
                    rs.close();
                    ps.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("MTS Transfer error: " + e);
                }
            } else if (op == 9) { //add to cart
                int id = slea.readInt(); //id of the item
                Connection con;
                try {
                    con = DatabaseConnection.getConnection();
                    try (PreparedStatement ps1 = con.prepareStatement("SELECT id FROM mts_items WHERE id = ? AND seller <> ?")) {
                        ps1.setInt(1, id);  //Dummy query, prevents adding to cart self owned items
                        ps1.setInt(2, c.getPlayer().getId());
                        try (ResultSet rs1 = ps1.executeQuery()) {
                            if (rs1.next()) {
                                PreparedStatement ps = con.prepareStatement("SELECT cid FROM mts_cart WHERE cid = ? AND itemid = ?");
                                ps.setInt(1, c.getPlayer().getId());
                                ps.setInt(2, id);
                                try (ResultSet rs = ps.executeQuery()) {
                                    if (!rs.next()) {
                                        try (PreparedStatement pse = con.prepareStatement("INSERT INTO mts_cart (cid, itemid) VALUES (?, ?)")) {
                                            pse.setInt(1, c.getPlayer().getId());
                                            pse.setInt(2, id);
                                            pse.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                c.announce(getMTS(c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
                c.enableCSActions();
                c.announce(MaplePacketCreator.enableActions());
                c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
            } else if (op == 10) { //delete from cart
                int id = slea.readInt(); //id of the item
                Connection con = null;
                try {
                    con = DatabaseConnection.getConnection();
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ? AND cid = ?")) {
                        ps.setInt(1, id);
                        ps.setInt(2, c.getPlayer().getId());
                        ps.executeUpdate();
                    }
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                c.announce(getCart(c.getPlayer().getId()));
                c.enableCSActions();
                c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
            } else if (op == 12) { //put item up for auction
            } else if (op == 13) { //cancel wanted cart thing
            } else if (op == 14) { //buy auction item now
            } else if (op == 16) { //buy
                int id = slea.readInt(); //id of the item
                Connection con = null;
                PreparedStatement ps;
                ResultSet rs;
                try {
                    con = DatabaseConnection.getConnection();
                    ps = con.prepareStatement("SELECT * FROM mts_items WHERE id = ? ORDER BY id DESC");
                    ps.setInt(1, id);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        int price = rs.getInt("price") + 100 + (int) (rs.getInt("price") * 0.1); //taxes
                        if (c.getPlayer().getCashShop().getCash(4) >= price) { //FIX
                            boolean alwaysnull = true;
                            for (Channel cserv : Server.getInstance().getAllChannels()) {
                                MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(rs.getInt("seller"));
                                if (victim != null) {
                                    victim.getCashShop().gainCash(4, rs.getInt("price"));
                                    alwaysnull = false;
                                }
                            }
                            if (alwaysnull) {
                                ResultSet rse;
                                try (PreparedStatement pse = con.prepareStatement("SELECT accountid FROM characters WHERE id = ?")) {
                                    pse.setInt(1, rs.getInt("seller"));
                                    rse = pse.executeQuery();
                                    if (rse.next()) {
                                        try (PreparedStatement psee = con.prepareStatement("UPDATE accounts SET nxPrepaid = nxPrepaid + ? WHERE id = ?")) {
                                            psee.setInt(1, rs.getInt("price"));
                                            psee.setInt(2, rse.getInt("accountid"));
                                            psee.executeUpdate();
                                        }
                                    }
                                }
                                rse.close();
                            }
                            PreparedStatement pse = con.prepareStatement("UPDATE mts_items SET seller = ?, transfer = 1 WHERE id = ?");
                            pse.setInt(1, c.getPlayer().getId());
                            pse.setInt(2, id);
                            pse.executeUpdate();
                            pse.close();
                            pse = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ?");
                            pse.setInt(1, id);
                            pse.executeUpdate();
                            pse.close();
                            c.getPlayer().getCashShop().gainCash(4, -price);
                            c.enableCSActions();
                            c.announce(getMTS(c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
                            c.announce(MaplePacketCreator.MTSConfirmBuy());
                            c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
                            c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                            c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
                            c.announce(MaplePacketCreator.enableActions());
                        } else {
                            c.announce(MaplePacketCreator.MTSFailBuy());
                        }
                    }
                    rs.close();
                    ps.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    c.announce(MaplePacketCreator.MTSFailBuy());
                }
            } else if (op == 17) { //buy from cart
                int id = slea.readInt(); //id of the item
                Connection con = null;
                PreparedStatement ps;
                ResultSet rs;
                try {
                    con = DatabaseConnection.getConnection();
                    ps = con.prepareStatement("SELECT * FROM mts_items WHERE id = ? ORDER BY id DESC");
                    ps.setInt(1, id);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        int price = rs.getInt("price") + 100 + (int) (rs.getInt("price") * 0.1);
                        if (c.getPlayer().getCashShop().getCash(4) >= price) {
                            for (Channel cserv : Server.getInstance().getAllChannels()) {
                                MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(rs.getInt("seller"));
                                if (victim != null) {
                                    victim.getCashShop().gainCash(4, rs.getInt("price"));
                                } else {
                                    ResultSet rse;
                                    try (PreparedStatement pse = con.prepareStatement("SELECT accountid FROM characters WHERE id = ?")) {
                                        pse.setInt(1, rs.getInt("seller"));
                                        rse = pse.executeQuery();
                                        if (rse.next()) {
                                            try (PreparedStatement psee = con.prepareStatement("UPDATE accounts SET nxPrepaid = nxPrepaid + ? WHERE id = ?")) {
                                                psee.setInt(1, rs.getInt("price"));
                                                psee.setInt(2, rse.getInt("accountid"));
                                                psee.executeUpdate();
                                            }
                                        }
                                    }
                                    rse.close();
                                }
                            }
                            PreparedStatement pse = con.prepareStatement("UPDATE mts_items SET seller = ?, transfer = 1 WHERE id = ?");
                            pse.setInt(1, c.getPlayer().getId());
                            pse.setInt(2, id);
                            pse.executeUpdate();
                            pse.close();
                            pse = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ?");
                            pse.setInt(1, id);
                            pse.executeUpdate();
                            pse.close();
                            c.getPlayer().getCashShop().gainCash(4, -price);
                            c.announce(getCart(c.getPlayer().getId()));
                            c.enableCSActions();
                            c.announce(MaplePacketCreator.MTSConfirmBuy());
                            c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
                            c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
                            c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
                        } else {
                            c.announce(MaplePacketCreator.MTSFailBuy());
                        }
                    }
                    rs.close();
                    ps.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    c.announce(MaplePacketCreator.MTSFailBuy());
                }
            } else {
                System.out.println("Unhandled OP(MTS): " + op + " Packet: " + slea.toString());
            }
        } else {
            c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
        }
    }

    public List<MTSItemInfo> getNotYetSold(int cid) {
        List<MTSItemInfo> items = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps;
        ResultSet rs;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM mts_items WHERE seller = ? AND transfer = 0 ORDER BY id DESC");
            ps.setInt(1, cid);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("type") != 1) {
                    Item i = new Item(rs.getInt("itemid"), (byte) 0, (short) rs.getInt("quantity"));
                    i.setOwner(rs.getString("owner"));
                    items.add(new MTSItemInfo((Item) i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                    equip.setOwner(rs.getString("owner"));
                    equip.setQuantity((short) 1);
                    equip.setAcc((short) rs.getInt("acc"));
                    equip.setAvoid((short) rs.getInt("avoid"));
                    equip.setDex((short) rs.getInt("dex"));
                    equip.setHands((short) rs.getInt("hands"));
                    equip.setHp((short) rs.getInt("hp"));
                    equip.setInt((short) rs.getInt("int"));
                    equip.setJump((short) rs.getInt("jump"));
                    equip.setVicious((short) rs.getInt("vicious"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLevel((byte) rs.getInt("level"));
                    equip.setFlag((byte) rs.getInt("flag"));
                    items.add(new MTSItemInfo((Item) equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                }
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public byte[] getCart(int cid) {
        List<MTSItemInfo> items = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps;
        ResultSet rs;
        int pages = 0;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM mts_cart WHERE cid = ? ORDER BY id DESC");
            ps.setInt(1, cid);
            rs = ps.executeQuery();
            while (rs.next()) {
                try (PreparedStatement pse = con.prepareStatement("SELECT * FROM mts_items WHERE id = ?")) {
                    pse.setInt(1, rs.getInt("itemid"));
                    ResultSet rse = pse.executeQuery();
                    if (rse.next()) {
                        if (rse.getInt("type") != 1) {
                            Item i = new Item(rse.getInt("itemid"), (short) 0, (short) rse.getInt("quantity"));
                            i.setOwner(rse.getString("owner"));
                            items.add(new MTSItemInfo((Item) i, rse.getInt("price"), rse.getInt("id"), rse.getInt("seller"), rse.getString("sellername"), rse.getString("sell_ends")));
                        } else {
                            Equip equip = new Equip(rse.getInt("itemid"), (byte) rse.getInt("position"), -1);
                            equip.setOwner(rse.getString("owner"));
                            equip.setQuantity((short) 1);
                            equip.setAcc((short) rse.getInt("acc"));
                            equip.setAvoid((short) rse.getInt("avoid"));
                            equip.setDex((short) rse.getInt("dex"));
                            equip.setHands((short) rse.getInt("hands"));
                            equip.setHp((short) rse.getInt("hp"));
                            equip.setInt((short) rse.getInt("int"));
                            equip.setJump((short) rse.getInt("jump"));
                            equip.setVicious((short) rse.getInt("vicious"));
                            equip.setLuk((short) rse.getInt("luk"));
                            equip.setMatk((short) rse.getInt("matk"));
                            equip.setMdef((short) rse.getInt("mdef"));
                            equip.setMp((short) rse.getInt("mp"));
                            equip.setSpeed((short) rse.getInt("speed"));
                            equip.setStr((short) rse.getInt("str"));
                            equip.setWatk((short) rse.getInt("watk"));
                            equip.setWdef((short) rse.getInt("wdef"));
                            equip.setUpgradeSlots((byte) rse.getInt("upgradeslots"));
                            equip.setLevel((byte) rse.getInt("level"));
                            equip.setFlag((byte) rs.getInt("flag"));
                            items.add(new MTSItemInfo((Item) equip, rse.getInt("price"), rse.getInt("id"), rse.getInt("seller"), rse.getString("sellername"), rse.getString("sell_ends")));
                        }
                    }
                }
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT COUNT(*) FROM mts_cart WHERE cid = ?");
            ps.setInt(1, cid);
            rs = ps.executeQuery();
            if (rs.next()) {
                pages = rs.getInt(1) / 16;
                if (rs.getInt(1) % 16 > 0) {
                    pages += 1;
                }
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return MaplePacketCreator.sendMTS(items, 4, 0, 0, pages);
    }

    public List<MTSItemInfo> getTransfer(int cid) {
        List<MTSItemInfo> items = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps;
        ResultSet rs;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM mts_items WHERE transfer = 1 AND seller = ? ORDER BY id DESC");
            ps.setInt(1, cid);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("type") != 1) {
                    Item i = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                    i.setOwner(rs.getString("owner"));
                    items.add(new MTSItemInfo((Item) i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                    equip.setOwner(rs.getString("owner"));
                    equip.setQuantity((short) 1);
                    equip.setAcc((short) rs.getInt("acc"));
                    equip.setAvoid((short) rs.getInt("avoid"));
                    equip.setDex((short) rs.getInt("dex"));
                    equip.setHands((short) rs.getInt("hands"));
                    equip.setHp((short) rs.getInt("hp"));
                    equip.setInt((short) rs.getInt("int"));
                    equip.setJump((short) rs.getInt("jump"));
                    equip.setVicious((short) rs.getInt("vicious"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLevel((byte) rs.getInt("level"));
                    equip.setFlag((byte) rs.getInt("flag"));
                    items.add(new MTSItemInfo((Item) equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                }
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private static byte[] getMTS(int tab, int type, int page) {
        List<MTSItemInfo> items = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps;
        ResultSet rs;
        int pages = 0;
        try {
            con = DatabaseConnection.getConnection();
            if (type != 0) {
                ps = con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? AND type = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16");
            } else {
                ps = con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16");
            }
            ps.setInt(1, tab);
            if (type != 0) {
                ps.setInt(2, type);
                ps.setInt(3, page * 16);
            } else {
                ps.setInt(2, page * 16);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("type") != 1) {
                    Item i = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                    i.setOwner(rs.getString("owner"));
                    items.add(new MTSItemInfo((Item) i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                    equip.setOwner(rs.getString("owner"));
                    equip.setQuantity((short) 1);
                    equip.setAcc((short) rs.getInt("acc"));
                    equip.setAvoid((short) rs.getInt("avoid"));
                    equip.setDex((short) rs.getInt("dex"));
                    equip.setHands((short) rs.getInt("hands"));
                    equip.setHp((short) rs.getInt("hp"));
                    equip.setInt((short) rs.getInt("int"));
                    equip.setJump((short) rs.getInt("jump"));
                    equip.setVicious((short) rs.getInt("vicious"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLevel((byte) rs.getInt("level"));
                    equip.setFlag((byte) rs.getInt("flag"));
                    items.add(new MTSItemInfo((Item) equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                }
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE tab = ? " + (type != 0 ? "AND type = ?" : "") + "AND transfer = 0");
            ps.setInt(1, tab);
            if (type != 0) {
                ps.setInt(2, type);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                pages = rs.getInt(1) / 16;
                if (rs.getInt(1) % 16 > 0) {
                    pages++;
                }
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return MaplePacketCreator.sendMTS(items, tab, type, page, pages); // resniff
    }

    public byte[] getMTSSearch(int tab, int type, int cOi, String search, int page) {
        List<MTSItemInfo> items = new ArrayList<>();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        String listaitems = "";
        if (cOi != 0) {
            List<String> retItems = new ArrayList<>();
            for (Pair<Integer, String> itemPair : ii.getAllItems()) {
                if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                    retItems.add(" itemid=" + itemPair.getLeft() + " OR ");
                }
            }
            listaitems += " AND (";
            if (retItems != null && retItems.size() > 0) {
                for (String singleRetItem : retItems) {
                    listaitems += singleRetItem;
                }
                listaitems += " itemid=0 )";
            }
        } else {
            listaitems = " AND sellername LIKE CONCAT('%','" + search + "', '%')";
        }
        Connection con = null;
        PreparedStatement ps;
        ResultSet rs;
        int pages = 0;
        try {
            con = DatabaseConnection.getConnection();
            if (type != 0) {
                ps = con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? " + listaitems + " AND type = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16");
            } else {
                ps = con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? " + listaitems + " AND transfer = 0 ORDER BY id DESC LIMIT ?, 16");
            }
            ps.setInt(1, tab);
            if (type != 0) {
                ps.setInt(2, type);
                ps.setInt(3, page * 16);
            } else {
                ps.setInt(2, page * 16);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("type") != 1) {
                    Item i = new Item(rs.getInt("itemid"), (short) 0, (short) rs.getInt("quantity"));
                    i.setOwner(rs.getString("owner"));
                    items.add(new MTSItemInfo((Item) i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                    equip.setOwner(rs.getString("owner"));
                    equip.setQuantity((short) 1);
                    equip.setAcc((short) rs.getInt("acc"));
                    equip.setAvoid((short) rs.getInt("avoid"));
                    equip.setDex((short) rs.getInt("dex"));
                    equip.setHands((short) rs.getInt("hands"));
                    equip.setHp((short) rs.getInt("hp"));
                    equip.setInt((short) rs.getInt("int"));
                    equip.setJump((short) rs.getInt("jump"));
                    equip.setVicious((short) rs.getInt("vicious"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLevel((byte) rs.getInt("level"));
                    equip.setFlag((byte) rs.getInt("flag"));
                    items.add(new MTSItemInfo((Item) equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                }
            }
            rs.close();
            ps.close();
            if (type == 0) {
                ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE tab = ? " + listaitems + " AND transfer = 0");
                ps.setInt(1, tab);
                if (type != 0) {
                    ps.setInt(2, type);
                }
                rs = ps.executeQuery();
                if (rs.next()) {
                    pages = rs.getInt(1) / 16;
                    if (rs.getInt(1) % 16 > 0) {
                        pages++;
                    }
                }
                rs.close();
                ps.close();
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return MaplePacketCreator.sendMTS(items, tab, type, page, pages);
    }
}
