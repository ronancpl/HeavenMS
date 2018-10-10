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
package net.server.channel.handlers;

import client.MapleClient;
import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.manipulator.MapleInventoryManipulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import server.CashShop;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Penguins (Acrylic)
 * @author Ronan (HeavenMS)
 */
public final class CouponCodeHandler extends AbstractMaplePacketHandler {
    
    private static List<Pair<Integer, Pair<Integer, Integer>>> getNXCodeItems(MapleCharacter chr, Connection con, int codeid) throws SQLException {
        Map<Integer, Integer> couponItems = new HashMap<>();
        Map<Integer, Integer> couponPoints = new HashMap<>(5);
        
        PreparedStatement ps = con.prepareStatement("SELECT * FROM nxcode_items WHERE codeid = ?");
        ps.setInt(1, codeid);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int type = rs.getInt("type"), item = rs.getInt("item");
            
            if (type < 5) {
                Integer i = couponPoints.get(type);
                if (i != null) {
                    couponPoints.put(type, i + item);
                } else {
                    couponPoints.put(type, item);
                }
            } else {
                int quantity = rs.getInt("quantity");
                
                Integer i = couponItems.get(item);
                if (i != null) {
                    couponItems.put(item, i + quantity);
                } else {
                    couponItems.put(item, quantity);
                }
            }
        }
        
        rs.close();
        ps.close();
        
        List<Pair<Integer, Pair<Integer, Integer>>> ret = new LinkedList<>();
        if (!couponItems.isEmpty()) {
            for (Entry<Integer, Integer> e : couponItems.entrySet()) {
                int item = e.getKey(), qty = e.getValue();
                
                if (MapleItemInformationProvider.getInstance().getName(item) == null) {
                    item = 4000000;
                    qty = 1;
                    
                    FilePrinter.printError(FilePrinter.UNHANDLED_EVENT, "Error trying to redeem itemid " + item + " from codeid " + codeid + ".");
                }
                
                if (!chr.canHold(item, qty)) {
                    return null;
                }
                
                ret.add(new Pair<>(5, new Pair<>(item, qty)));
            }
        }
        
        if (!couponPoints.isEmpty()) {
            for (Entry<Integer, Integer> e : couponPoints.entrySet()) {
                ret.add(new Pair<>(e.getKey(), new Pair<>(e.getValue(), 777)));
            }
        }
        
        return ret;
    }
    
    private static Pair<Integer, List<Pair<Integer, Pair<Integer, Integer>>>> getNXCodeResult(MapleCharacter chr, String code) {
        MapleClient c = chr.getClient();
        List<Pair<Integer, Pair<Integer, Integer>>> ret = new LinkedList<>();
        try {
            if (!c.attemptCsCoupon()) {
                return new Pair<>(-5, null);
            }
            
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM nxcode WHERE code = ?");
            ps.setString(1, code);
            
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return new Pair<>(-1, null);
            }
            
            if (rs.getString("retriever") != null) {
                return new Pair<>(-2, null);
            }
            
            if (rs.getLong("expiration") < Server.getInstance().getCurrentTime()) {
                return new Pair<>(-3, null);
            }
            
            int codeid = rs.getInt("id");
            rs.close();
            ps.close();
            
            ret = getNXCodeItems(chr, con, codeid);
            if (ret == null) {
                return new Pair<>(-4, null);
            }
            
            ps = con.prepareStatement("UPDATE nxcode SET retriever = ? WHERE code = ?");
            ps.setString(1, chr.getName());
            ps.setString(2, code);
            ps.executeUpdate();
            
            ps.close();
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        c.resetCsCoupon();
        return new Pair<>(0, ret);
    }
    
    private static int parseCouponResult(int res) {
        switch (res) {
            case -1:
                return 0xB0;
                
            case -2:
                return 0xB3;
            
            case -3:
                return 0xB2;
            
            case -4:
                return 0xBB;
                
            default:
                return 0xB1;
        }
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(2);
        String code = slea.readMapleAsciiString();
        
        if (c.tryacquireClient()) {
            try {
                Pair<Integer, List<Pair<Integer, Pair<Integer, Integer>>>> codeRes = getNXCodeResult(c.getPlayer(), code.toUpperCase());
                int type = codeRes.getLeft();
                if (type < 0) {
                    c.announce(MaplePacketCreator.showCashShopMessage((byte) parseCouponResult(type)));
                } else {
                    List<Item> couponPackage = new LinkedList<>();

                    for (Pair<Integer, Pair<Integer, Integer>> p : codeRes.getRight()) {
                        type = p.getLeft();
                        int item = p.getRight().getLeft();

                        CashShop cs = c.getPlayer().getCashShop();
                        switch (type) {
                            case 0:
                            case 4:
                                cs.gainCash(1, item);   //nxCredit
                                break;
                            case 1:
                                cs.gainCash(2, item);   //maplePoint
                                break;
                            case 2:
                                cs.gainCash(4, item);   //nxPrepaid
                                break;
                            case 3:
                                cs.gainCash(1, item);
                                cs.gainCash(4, (item / 5000));
                                break;

                            default:
                                short qty = p.getRight().getRight().shortValue();
                                if (MapleItemInformationProvider.getInstance().isCash(item)) {
                                    Item it = CashShop.generateCouponItem(item, qty);

                                    cs.addToInventory(it);
                                    couponPackage.add(it);
                                } else {
                                    MapleInventoryManipulator.addById(c, item, qty, "", -1);
                                }

                                //c.announce(MaplePacketCreator.showCouponRedeemedItem(item));
                                break;
                        }
                    }

                    if (!couponPackage.isEmpty()) {
                        c.announce(MaplePacketCreator.showBoughtCashPackage(couponPackage, c.getAccID()));
                    } else {
                        c.announce(MaplePacketCreator.showBoughtQuestItem(0));
                    }

                    c.enableCSActions();
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
