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

import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import net.server.audit.locks.MonitoredLockType;

/**
 *
 * @author Matze
 */
public class MapleStorage {
    private static Map<Integer, Integer> trunkGetCache = new HashMap<>();
    private static Map<Integer, Integer> trunkPutCache = new HashMap<>();
    
    private int id;
    private int currentNpcid;
    private int meso;
    private byte slots;
    private Map<MapleInventoryType, List<Item>> typeItems = new HashMap<>();
    private List<Item> items;
    private Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.STORAGE, true);

    private MapleStorage(int id, byte slots, int meso) {
        this.id = id;
        this.slots = slots;
        this.items = new LinkedList<>();
        this.meso = meso;
    }

    private static MapleStorage create(int id, int world) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO storages (accountid, world, slots, meso) VALUES (?, ?, 4, 0)")) {
                ps.setInt(1, id);
                ps.setInt(2, world);
                ps.executeUpdate();
            }
            
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loadOrCreateFromDB(id, world);
    }

    public static MapleStorage loadOrCreateFromDB(int id, int world) {
        MapleStorage ret = null;
        int storeId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT storageid, slots, meso FROM storages WHERE accountid = ? AND world = ?");
            ps.setInt(1, id);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                return create(id, world);
            } else {
                storeId = rs.getInt("storageid");
                ret = new MapleStorage(storeId, (byte) rs.getInt("slots"), rs.getInt("meso"));
                rs.close();
                ps.close();
                for (Pair<Item, MapleInventoryType> item : ItemFactory.STORAGE.loadItems(ret.id, false)) {
                    ret.items.add(item.getLeft());
                }
            }
            
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public byte getSlots() {
        return slots;
    }

    public synchronized boolean gainSlots(int slots) {
        slots += this.slots;

        if (slots <= 48) {
            this.slots = (byte) slots;
            return true;
        }

        return false;
    }
    
    public void saveToDB(Connection con) {
        try {
            try (PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?")) {
                ps.setInt(1, slots);
                ps.setInt(2, meso);
                ps.setInt(3, id);
                ps.executeUpdate();
            }
            List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

            List<Item> list = getItems();
            for (Item item : list) {
                itemsWithType.add(new Pair<>(item, item.getInventoryType()));
            }

            ItemFactory.STORAGE.saveItems(itemsWithType, id, con);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Item getItem(byte slot) {
        lock.lock();
        try {
            return items.get(slot);
        } finally {
            lock.unlock();
        }
    }

    public Item takeOut(byte slot) {
        Item ret;
        
        lock.lock();
        try {
            ret = items.remove(slot);
            
            MapleInventoryType type = ret.getInventoryType();
            typeItems.put(type, new ArrayList<>(filterItems(type)));
        } finally {
            lock.unlock();
        }
        
        return ret;
    }

    public void store(Item item) {
        lock.lock();
        try {
            items.add(item);
            
            MapleInventoryType type = item.getInventoryType();
            typeItems.put(type, new ArrayList<>(filterItems(type)));
        } finally {
            lock.unlock();
        }
    }

    public List<Item> getItems() {
        lock.lock();
        try {
            return Collections.unmodifiableList(items);
        } finally {
            lock.unlock();
        }
    }

    private List<Item> filterItems(MapleInventoryType type) {
        List<Item> storageItems = getItems();
        List<Item> ret = new LinkedList<>();
        
        for (Item item : storageItems) {
            if (item.getInventoryType() == type) {
                ret.add(item);
            }
        }
        return ret;
    }

    public byte getSlot(MapleInventoryType type, byte slot) {
        lock.lock();
        try {
            byte ret = 0;
            List<Item> storageItems = getItems();
            for (Item item : storageItems) {
                if (item == typeItems.get(type).get(slot)) {
                    return ret;
                }
                ret++;
            }
            return -1;
        } finally {
            lock.unlock();
        }
    }

    public void sendStorage(MapleClient c, int npcId) {
        if (c.getPlayer().getLevel() < 15){
            c.getPlayer().dropMessage(1, "You may only use the storage once you have reached level 15.");
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        lock.lock();
        try {
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item o1, Item o2) {
                    if (o1.getInventoryType().getType() < o2.getInventoryType().getType()) {
                        return -1;
                    } else if (o1.getInventoryType() == o2.getInventoryType()) {
                        return 0;
                    }
                    return 1;
                }
            });
            
            List<Item> storageItems = getItems();
            for (MapleInventoryType type : MapleInventoryType.values()) {
                typeItems.put(type, new ArrayList<>(storageItems));
            }
            
            currentNpcid = npcId;
            c.announce(MaplePacketCreator.getStorage(npcId, slots, storageItems, meso));
        } finally {
            lock.unlock();
        }
    }

    public void sendStored(MapleClient c, MapleInventoryType type) {
        lock.lock();
        try {
            c.announce(MaplePacketCreator.storeStorage(slots, type, typeItems.get(type)));
        } finally {
            lock.unlock();
        }
    }

    public void sendTakenOut(MapleClient c, MapleInventoryType type) {
        lock.lock();
        try {
            c.announce(MaplePacketCreator.takeOutStorage(slots, type, typeItems.get(type)));
        } finally {
            lock.unlock();
        }
    }
    
    public void arrangeItems(MapleClient c) {
        lock.lock();
        try {
            MapleStorageInventory msi = new MapleStorageInventory(c, items);
            msi.mergeItems();
            items = msi.sortItems();
            
            for (MapleInventoryType type : MapleInventoryType.values()) {
                typeItems.put(type, new ArrayList<>(items));
            }

            c.announce(MaplePacketCreator.arrangeStorage(slots, items));
        } finally {
            lock.unlock();
        }
    }

    public int getMeso() {
        return meso;
    }

    public void setMeso(int meso) {
        if (meso < 0) {
            throw new RuntimeException();
        }
        this.meso = meso;
    }

    public void sendMeso(MapleClient c) {
        c.announce(MaplePacketCreator.mesoStorage(slots, meso));
    }
    
    public int getStoreFee() {  // thanks to GabrielSin
        int npcId = currentNpcid;
        Integer fee = trunkPutCache.get(npcId);
        if(fee == null) {
            fee = 100;
            
            MapleDataProvider npc = MapleDataProviderFactory.getDataProvider(new File("wz/Npc.wz"));
            MapleData npcData = npc.getData(npcId + ".img");
            if(npcData != null) {
                fee = MapleDataTool.getIntConvert("info/trunkPut", npcData, 100);
            }
            
            trunkPutCache.put(npcId, fee);
        }
        
        return fee;
    }
    
    public int getTakeOutFee() {
        int npcId = currentNpcid;
        Integer fee = trunkGetCache.get(npcId);
        if(fee == null) {
            fee = 0;
            
            MapleDataProvider npc = MapleDataProviderFactory.getDataProvider(new File("wz/Npc.wz"));
            MapleData npcData = npc.getData(npcId + ".img");
            if(npcData != null) {
                fee = MapleDataTool.getIntConvert("info/trunkGet", npcData, 0);
            }
            
            trunkGetCache.put(npcId, fee);
        }
        
        return fee;
    }

    public boolean isFull() {
        lock.lock();
        try {
            return items.size() >= slots;
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();
        try {
            typeItems.clear();
        } finally {
            lock.unlock();
        }
    }
}