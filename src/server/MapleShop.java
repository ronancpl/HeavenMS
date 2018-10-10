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
package server;

import client.inventory.manipulator.MapleInventoryManipulator;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 */
public class MapleShop {
    private static final Set<Integer> rechargeableItems = new LinkedHashSet<>();
    private int id;
    private int npcId;
    private List<MapleShopItem> items;
    private int tokenvalue = 1000000000;
    private int token = 4000313;

    static {
        for (int i = 2070000; i < 2070017; i++) {
            rechargeableItems.add(i);
        }
        rechargeableItems.add(2331000);//Blaze Capsule
        rechargeableItems.add(2332000);//Glaze Capsule
        rechargeableItems.add(2070018);
        rechargeableItems.remove(2070014); // doesn't exist
        for (int i = 2330000; i <= 2330005; i++) {
            rechargeableItems.add(i);
        }
    }

    private MapleShop(int id, int npcId) {
        this.id = id;
        this.npcId = npcId;
        items = new ArrayList<>();
    }

    private void addItem(MapleShopItem item) {
        items.add(item);
    }

    public void sendShop(MapleClient c) {
        c.getPlayer().setShop(this);
        c.announce(MaplePacketCreator.getNPCShop(c, getNpcId(), items));
    }

    public void buy(MapleClient c, short slot, int itemId, short quantity) {
        MapleShopItem item = findBySlot(slot);
        if (item != null) {
            if (item.getItemId() != itemId) {
                System.out.println("Wrong slot number in shop " + id);
                return;
            }
        } else {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (item.getPrice() > 0) {
            int amount = (int)Math.min((float) item.getPrice() * quantity, Integer.MAX_VALUE);
            if (c.getPlayer().getMeso() >= amount) {
                if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                    if (!ItemConstants.isRechargeable(itemId)) { //Pets can't be bought from shops
                        MapleInventoryManipulator.addById(c, itemId, quantity, "", -1);          
                        c.getPlayer().gainMeso(-amount, false);
                    } else {
                        short slotMax = ii.getSlotMax(c, item.getItemId());
                        quantity = slotMax;
                        MapleInventoryManipulator.addById(c, itemId, quantity, "", -1);
                        c.getPlayer().gainMeso(-item.getPrice(), false);
                    }
                    c.announce(MaplePacketCreator.shopTransaction((byte) 0));
                } else 
                    c.announce(MaplePacketCreator.shopTransaction((byte) 3));
                
            } else
                c.announce(MaplePacketCreator.shopTransaction((byte) 2));

        } else if (item.getPitch() > 0) {
            int amount = (int)Math.min((float) item.getPitch() * quantity, Integer.MAX_VALUE);
            
            if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4310000) >= amount) {
                if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                    if (!ItemConstants.isRechargeable(itemId)) {
                        MapleInventoryManipulator.addById(c, itemId, quantity, "", -1);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4310000, amount, false, false);
                    } else {
                        short slotMax = ii.getSlotMax(c, item.getItemId());
                        quantity = slotMax;
                        MapleInventoryManipulator.addById(c, itemId, quantity, "", -1);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4310000, amount, false, false);
                    }
                    c.announce(MaplePacketCreator.shopTransaction((byte) 0));
                } else
                    c.announce(MaplePacketCreator.shopTransaction((byte) 3));
            }

        } else if (c.getPlayer().getInventory(MapleInventoryType.CASH).countById(token) != 0) {
            int amount = c.getPlayer().getInventory(MapleInventoryType.CASH).countById(token);
            int value = amount * tokenvalue;
            int cost = item.getPrice() * quantity;
            if (c.getPlayer().getMeso() + value >= cost) {
                int cardreduce = value - cost;
                int diff = cardreduce + c.getPlayer().getMeso();
                if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                    if (ItemConstants.isPet(itemId)) {
                        int petid = MaplePet.createPet(itemId);
                        MapleInventoryManipulator.addById(c, itemId, quantity, "", petid, -1);
                    } else {
                        MapleInventoryManipulator.addById(c, itemId, quantity, "", -1, -1);
                    }
                    c.getPlayer().gainMeso(diff, false);
                } else {
                    c.announce(MaplePacketCreator.shopTransaction((byte) 3));
                }
                c.announce(MaplePacketCreator.shopTransaction((byte) 0));
            } else {
                c.announce(MaplePacketCreator.shopTransaction((byte) 2));
            }
        }
    }

    private static boolean canSell(Item item, short quantity) {
        if (item == null) { //Basic check
            return false;
        }
        
        short iQuant = item.getQuantity();
        if (iQuant == 0xFFFF) {
            iQuant = 1;
        } else if(iQuant < 0) {
            return false;
        }
        
        if (!ItemConstants.isRechargeable(item.getItemId())) {
            if (iQuant == 0 || quantity > iQuant) {
                return false;
            }
        }
        
        return true;
    }
    
    private static short getSellingQuantity(Item item, short quantity) {
        if (ItemConstants.isRechargeable(item.getItemId())) {
            quantity = item.getQuantity();
            if (quantity == 0xFFFF) {
                quantity = 1;
            }
        }
        
        return quantity;
    }

    public void sell(MapleClient c, MapleInventoryType type, short slot, short quantity) {
        if (quantity == 0xFFFF || quantity == 0) {
            quantity = 1;
        } else if (quantity < 0) {
            return;
        }
        
        Item item = c.getPlayer().getInventory(type).getItem((short) slot);
        if(canSell(item, quantity)) {
            quantity = getSellingQuantity(item, quantity);
            MapleInventoryManipulator.removeFromSlot(c, type, (byte) slot, quantity, false);
            
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int recvMesos = ii.getPrice(item.getItemId(), quantity);
            if (recvMesos > 0) {
                c.getPlayer().gainMeso(recvMesos, false);
            }
            c.announce(MaplePacketCreator.shopTransaction((byte) 0x8));
        } else {
            c.announce(MaplePacketCreator.shopTransaction((byte) 0x5));
        }
    }

    public void recharge(MapleClient c, short slot) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (item == null || !ItemConstants.isRechargeable(item.getItemId())) {
            return;
        }
        short slotMax = ii.getSlotMax(c, item.getItemId());
        if (item.getQuantity() < 0) {
            return;
        }
        if (item.getQuantity() < slotMax) {
            int price = (int) Math.ceil(ii.getUnitPrice(item.getItemId()) * (slotMax - item.getQuantity()));
            if (c.getPlayer().getMeso() >= price) {
                item.setQuantity(slotMax);
                c.getPlayer().forceUpdateItem(item);
                c.getPlayer().gainMeso(-price, false, true, false);
                c.announce(MaplePacketCreator.shopTransaction((byte) 0x8));
            } else {
                c.announce(MaplePacketCreator.shopTransaction((byte) 0x2));
            }
        }
    }

    private MapleShopItem findBySlot(short slot) {
        return items.get(slot);
    }

    public static MapleShop createFromDB(int id, boolean isShopId) {
        MapleShop ret = null;
        int shopId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (isShopId) {
                ps = con.prepareStatement("SELECT * FROM shops WHERE shopid = ?");
            } else {
                ps = con.prepareStatement("SELECT * FROM shops WHERE npcid = ?");
            }
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                shopId = rs.getInt("shopid");
                ret = new MapleShop(shopId, rs.getInt("npcid"));
                rs.close();
                ps.close();
            } else {
                rs.close();
                ps.close();
                con.close();
                return null;
            }
            ps = con.prepareStatement("SELECT itemid, price, pitch FROM shopitems WHERE shopid = ? ORDER BY position DESC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            List<Integer> recharges = new ArrayList<>(rechargeableItems);
            while (rs.next()) {
                if (ItemConstants.isRechargeable(rs.getInt("itemid"))) {
                    MapleShopItem starItem = new MapleShopItem((short) 1, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("pitch"));
                    ret.addItem(starItem);
                    if (rechargeableItems.contains(starItem.getItemId())) {
                        recharges.remove(Integer.valueOf(starItem.getItemId()));
                    }
                } else {
                    ret.addItem(new MapleShopItem((short) 1000, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("pitch")));
                }
            }
            for (Integer recharge : recharges) {
                ret.addItem(new MapleShopItem((short) 1000, recharge.intValue(), 0, 0));
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getId() {
        return id;
    }
}
