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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import net.server.Server;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.DatabaseConnection;
import tools.Pair;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import constants.ServerConstants;
import java.util.Collections;
import net.server.audit.locks.MonitoredLockType;

/*
 * @author Flav
 */
public class CashShop {
    public static class CashItem {

        private int sn, itemId, price;
        private long period;
        private short count;
        private boolean onSale;

        private CashItem(int sn, int itemId, int price, long period, short count, boolean onSale) {
            this.sn = sn;
            this.itemId = itemId;
            this.price = price;
            this.period = (period == 0 ? 90 : period);
            this.count = count;
            this.onSale = onSale;
        }

        public int getSN() {
            return sn;
        }

        public int getItemId() {
            return itemId;
        }

        public int getPrice() {
            return price;
        }

        public short getCount() {
            return count;
        }

        public boolean isOnSale() {
            return onSale;
        }

        public Item toItem() {
            Item item;

            int petid = -1;
            if (ItemConstants.isPet(itemId)) {
                petid = MaplePet.createPet(itemId);
            }
            
            if (ItemConstants.getInventoryType(itemId).equals(MapleInventoryType.EQUIP)) {
                item = MapleItemInformationProvider.getInstance().getEquipById(itemId);
            } else {
                item = new Item(itemId, (byte) 0, count, petid);
            }

            if (ItemConstants.EXPIRING_ITEMS) {
                    if(period == 1) {
                            if(itemId == 5211048 || itemId == 5360042) { // 4 Hour 2X coupons, the period is 1, but we don't want them to last a day.
                                    item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 4));
                            /*
                            } else if(itemId == 5211047 || itemId == 5360014) { // 3 Hour 2X coupons, unused as of now
                                    item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 3));
                            */
                            } else if(itemId == 5211060) { // 2 Hour 3X coupons.
                                    item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 2));
                            } else {
                                    item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 24));
                            }
                    } else {
                            item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 24 * period));
                    }
            }
            
            item.setSN(sn);
            return item;
        }
    }
    
    public static class SpecialCashItem {
        private int sn, modifier;
        private byte info; //?

        public SpecialCashItem(int sn, int modifier, byte info) {
            this.sn = sn;
            this.modifier = modifier;
            this.info = info;
        }

        public int getSN() {
            return sn;
        }

        public int getModifier() {
            return modifier;
        }

        public byte getInfo() {
            return info;
        }
    }

    public static class CashItemFactory {

        private static final Map<Integer, CashItem> items = new HashMap<>();
        private static final Map<Integer, List<Integer>> packages = new HashMap<>();
        private static final List<SpecialCashItem> specialcashitems = new ArrayList<>();
        private static final List<Integer> randomitemids = new ArrayList<>();

        static {
            MapleDataProvider etc = MapleDataProviderFactory.getDataProvider(new File("wz/Etc.wz"));

            for (MapleData item : etc.getData("Commodity.img").getChildren()) {
                int sn = MapleDataTool.getIntConvert("SN", item);
                int itemId = MapleDataTool.getIntConvert("ItemId", item);
                int price = MapleDataTool.getIntConvert("Price", item, 0);
                long period = MapleDataTool.getIntConvert("Period", item, 1);
                short count = (short) MapleDataTool.getIntConvert("Count", item, 1);
                boolean onSale = MapleDataTool.getIntConvert("OnSale", item, 0) == 1;
                items.put(sn, new CashItem(sn, itemId, price, period, count, onSale));
            }

            for (MapleData cashPackage : etc.getData("CashPackage.img").getChildren()) {
                List<Integer> cPackage = new ArrayList<>();

                for (MapleData item : cashPackage.getChildByPath("SN").getChildren()) {
                    cPackage.add(Integer.parseInt(item.getData().toString()));
                }

                packages.put(Integer.parseInt(cashPackage.getName()), cPackage);
            }
            
            for(Entry<Integer, CashItem> e : items.entrySet()) {
                if(e.getValue().isOnSale()) {
                    randomitemids.add(e.getKey());
                }
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection con = null;
            try {
                con = DatabaseConnection.getConnection();
                ps = con.prepareStatement("SELECT * FROM specialcashitems");
                rs = ps.executeQuery();
                while (rs.next()) {
                    specialcashitems.add(new SpecialCashItem(rs.getInt("sn"), rs.getInt("modifier"), rs.getByte("info")));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.close();
                    if (ps != null && !ps.isClosed()) ps.close();
                    if (con != null && !con.isClosed()) con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public static CashItem getRandomCashItem() {
            if(randomitemids.isEmpty()) return null;
            
            int rnd = (int)(Math.random() * randomitemids.size());
            return items.get(randomitemids.get(rnd));
        }
        
        public static CashItem getItem(int sn) {
            return items.get(sn);
        }

        public static List<Item> getPackage(int itemId) {
            List<Item> cashPackage = new ArrayList<>();

            for (int sn : packages.get(itemId)) {
                cashPackage.add(getItem(sn).toItem());
            }

            return cashPackage;
        }

        public static boolean isPackage(int itemId) {
            return packages.containsKey(itemId);
        }

        public static List<SpecialCashItem> getSpecialCashItems() {
            return specialcashitems;
        }
        
        public static void reloadSpecialCashItems() {//Yay?
            specialcashitems.clear();
            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection con = null;
            try {
                con = DatabaseConnection.getConnection();
                ps = con.prepareStatement("SELECT * FROM specialcashitems");
                rs = ps.executeQuery();
                while (rs.next()) {
                    specialcashitems.add(new SpecialCashItem(rs.getInt("sn"), rs.getInt("modifier"), rs.getByte("info")));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.close();
                    if (ps != null && !ps.isClosed()) ps.close();
                    if (con != null && !con.isClosed()) con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }            
        }
    }
    
    private int accountId, characterId, nxCredit, maplePoint, nxPrepaid;
    private boolean opened;
    private ItemFactory factory;
    private List<Item> inventory = new ArrayList<>();
    private List<Integer> wishList = new ArrayList<>();
    private int notes = 0;
    private Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CASHSHOP);

    public CashShop(int accountId, int characterId, int jobType) throws SQLException {
        this.accountId = accountId;
        this.characterId = characterId;

        if (!ServerConstants.USE_JOINT_CASHSHOP_INVENTORY) {
            if (jobType == 0) {
                factory = ItemFactory.CASH_EXPLORER;
            } else if (jobType == 1) {
                factory = ItemFactory.CASH_CYGNUS;
            } else if (jobType == 2) {
                factory = ItemFactory.CASH_ARAN;
            }
        } else {
            factory = ItemFactory.CASH_OVERALL;
        }

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT `nxCredit`, `maplePoint`, `nxPrepaid` FROM `accounts` WHERE `id` = ?");
            ps.setInt(1, accountId);
            rs = ps.executeQuery();

            if (rs.next()) {
                this.nxCredit = rs.getInt("nxCredit");
                this.maplePoint = rs.getInt("maplePoint");
                this.nxPrepaid = rs.getInt("nxPrepaid");
            }

            rs.close();
            ps.close();

            for (Pair<Item, MapleInventoryType> item : factory.loadItems(accountId, false)) {
                inventory.add(item.getLeft());
            }

            ps = con.prepareStatement("SELECT `sn` FROM `wishlists` WHERE `charid` = ?");
            ps.setInt(1, characterId);
            rs = ps.executeQuery();

            while (rs.next()) {
                wishList.add(rs.getInt("sn"));
            }

            rs.close();
            ps.close();
            con.close();
        } finally {
            if (ps != null && !ps.isClosed()) ps.close();
            if (rs != null && !rs.isClosed()) rs.close();
            if (con != null && !con.isClosed()) con.close();
        }
    }

    public int getCash(int type) {
        switch (type) {
            case 1:
                return nxCredit;
            case 2:
                return maplePoint;
            case 4:
                return nxPrepaid;
        }

        return 0;
    }

    public void gainCash(int type, int cash) {
        switch (type) {
            case 1:
                nxCredit += cash;
                break;
            case 2:
                maplePoint += cash;
                break;
            case 4:
                nxPrepaid += cash;
                break;
        }
    }
    
    public void gainCash(int type, CashItem buyItem, int world) {
        gainCash(type, -buyItem.getPrice());
        if(!ServerConstants.USE_ENFORCE_ITEM_SUGGESTION) Server.getInstance().getWorld(world).addCashItemBought(buyItem.getSN());
    }

    public boolean isOpened() {
        return opened;
    }

    public void open(boolean b) {
        opened = b;
    }

    public List<Item> getInventory() {
        lock.lock();
        try {
            return Collections.unmodifiableList(inventory);
        } finally {
            lock.unlock();
        }
    }

    public Item findByCashId(int cashId) {
        boolean isRing = false;
        Equip equip = null;
        for (Item item : getInventory()) {
            if (item.getInventoryType().equals(MapleInventoryType.EQUIP)) {
                equip = (Equip) item;
                isRing = equip.getRingId() > -1;
            }
            if ((item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId()) == cashId) {
                return item;
            }
        }

        return null;
    }

    public void addToInventory(Item item) {
        lock.lock();
        try {
            inventory.add(item);
        } finally {
            lock.unlock();
        }
    }

    public void removeFromInventory(Item item) {
        lock.lock();
        try {
            inventory.remove(item);
        } finally {
            lock.unlock();
        }
    }

    public List<Integer> getWishList() {
        return wishList;
    }

    public void clearWishList() {
        wishList.clear();
    }

    public void addToWishList(int sn) {
        wishList.add(sn);
    }

    public void gift(int recipient, String from, String message, int sn) {
        gift(recipient, from, message, sn, -1);
    }

    public void gift(int recipient, String from, String message, int sn, int ringid) {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("INSERT INTO `gifts` VALUES (DEFAULT, ?, ?, ?, ?, ?)");
            ps.setInt(1, recipient);
            ps.setString(2, from);
            ps.setString(3, message);
            ps.setInt(4, sn);
            ps.setInt(5, ringid);
            ps.executeUpdate();
            con.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (ps != null && !ps.isClosed()) ps.close();
                if (con != null && !con.isClosed()) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<Pair<Item, String>> loadGifts() {
        List<Pair<Item, String>> gifts = new ArrayList<>();
        Connection con = null;

        try {
            con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `gifts` WHERE `to` = ?");
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                notes++;
                CashItem cItem = CashItemFactory.getItem(rs.getInt("sn"));
                Item item = cItem.toItem();
                Equip equip = null;
                item.setGiftFrom(rs.getString("from"));
                if (item.getInventoryType().equals(MapleInventoryType.EQUIP)) {
                    equip = (Equip) item;
                    equip.setRingId(rs.getInt("ringid"));
                    gifts.add(new Pair<Item, String>(equip, rs.getString("message")));
                } else
                    gifts.add(new Pair<>(item, rs.getString("message")));

                if (CashItemFactory.isPackage(cItem.getItemId())) { //Packages never contains a ring
                    for (Item packageItem : CashItemFactory.getPackage(cItem.getItemId())) {
                        packageItem.setGiftFrom(rs.getString("from"));
                        addToInventory(packageItem);
                    }
                } else {
                    addToInventory(equip == null ? item : equip);
                }
            }

            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM `gifts` WHERE `to` = ?");
            ps.setInt(1, characterId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return gifts;
    }

    public int getAvailableNotes() {
        return notes;
    }

    public void decreaseNotes() {
        notes--;
    }

    public void save(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `nxCredit` = ?, `maplePoint` = ?, `nxPrepaid` = ? WHERE `id` = ?");
        ps.setInt(1, nxCredit);
        ps.setInt(2, maplePoint);
        ps.setInt(3, nxPrepaid);
        ps.setInt(4, accountId);
        ps.executeUpdate();
        ps.close();
        List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

        List<Item> inv = getInventory();
        for (Item item : inv) {
            itemsWithType.add(new Pair<>(item, item.getInventoryType()));
        }

        factory.saveItems(itemsWithType, accountId, con);
        ps = con.prepareStatement("DELETE FROM `wishlists` WHERE `charid` = ?");
        ps.setInt(1, characterId);
        ps.executeUpdate();
        ps.close();
        ps = con.prepareStatement("INSERT INTO `wishlists` VALUES (DEFAULT, ?, ?)");
        ps.setInt(1, characterId);

        for (int sn : wishList) {
            ps.setInt(2, sn);
            ps.executeUpdate();
        }

        ps.close();
    }
    
    private Item getCashShopItemByItemid(int itemid) {
        lock.lock();
        try {
            for(Item it : inventory) {
                if(it.getItemId() == itemid) {
                    return it;
                }
            }
        } finally {
            lock.unlock();
        }
        
        return null;
    }
    
    public synchronized Item openCashShopSurprise() {
        Item css = getCashShopItemByItemid(5222000);
        
        if(css != null) {
            CashItem cItem = CashItemFactory.getRandomCashItem();
            
            if(cItem != null) {
                if(css.getQuantity() > 1) {
                    /* if(NOT ENOUGH SPACE) { looks like we're not dealing with cash inventory limit whatsoever, k then
                        return null;
                    } */
                    
                    css.setQuantity((short) (css.getQuantity() - 1));
                } else {
                    removeFromInventory(css);
                }
                
                Item item = cItem.toItem();
                addToInventory(item);

                return item;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public static Item generateCouponItem(int itemId, short quantity) {
        CashItem it = new CashItem(77777777, itemId, 7777, ItemConstants.isPet(itemId) ? 30 : 0, quantity, true);
        return it.toItem();
    }
}
