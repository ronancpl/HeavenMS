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
package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import com.mysql.jdbc.Statement;
import constants.ItemConstants;
import constants.ServerConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import tools.locks.MonitoredReentrantLock;
import net.server.Server;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.locks.MonitoredLockType;

/**
 *
 * @author XoticStory
 * @author Ronan (concurrency protection)
 */
public class MapleHiredMerchant extends AbstractMapleMapObject {
    private int ownerId, itemId, mesos = 0;
    private int channel, world;
    private long start;
    private String ownerName = "";
    private String description = "";
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private final List<MaplePlayerShopItem> items = new LinkedList<>();
    private List<Pair<String, Byte>> messages = new LinkedList<>();
    private List<SoldItem> sold = new LinkedList<>();
    private AtomicBoolean open = new AtomicBoolean();
    private MapleMap map;
    private Lock visitorLock = new MonitoredReentrantLock(MonitoredLockType.VISITOR_MERCH, true);

    public MapleHiredMerchant(final MapleCharacter owner, int itemId, String desc) {
        this.setPosition(owner.getPosition());
        this.start = System.currentTimeMillis();
        this.ownerId = owner.getId();
        this.channel = owner.getClient().getChannel();
        this.world = owner.getWorld();
        this.itemId = itemId;
        this.ownerName = owner.getName();
        this.description = desc;
        this.map = owner.getMap();
    }

    public void broadcastToVisitorsThreadsafe(final byte[] packet) {
        visitorLock.lock();
        try {
            broadcastToVisitors(packet);
        } finally {
            visitorLock.unlock();
        }
    }
    
    private void broadcastToVisitors(final byte[] packet) {
        for (MapleCharacter visitor : visitors) {
            if (visitor != null) {
                visitor.getClient().announce(packet);
            }
        }
    }

    public boolean addVisitor(MapleCharacter visitor) {
        visitorLock.lock();
        try {
            int i = this.getFreeSlot();
            if (i > -1) {
                visitors[i] = visitor;
                broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorAdd(visitor, i + 1));
                
                return true;
            }
            
            return false;
        } finally {
            visitorLock.unlock();
        }
    }

    public void removeVisitor(MapleCharacter visitor) {
        visitorLock.lock();
        try {
            int slot = getVisitorSlot(visitor);
            if (slot < 0) { //Not found
                return;
            }
            if (visitors[slot] != null && visitors[slot].getId() == visitor.getId()) {
                visitors[slot] = null;
                broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorLeave(slot + 1));
            }
        } finally {
            visitorLock.unlock();
        }
    }

    public int getVisitorSlotThreadsafe(MapleCharacter visitor) {
        visitorLock.lock();
        try {
            return getVisitorSlot(visitor);
        } finally {
            visitorLock.unlock();
        }
    }
    
    private int getVisitorSlot(MapleCharacter visitor) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null && visitors[i].getId() == visitor.getId()){
                return i;
            }
        }
        return -1; //Actually 0 because of the +1's.
    }

    public void removeAllVisitors() {
        visitorLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                if (visitors[i] != null) {
                    visitors[i].setHiredMerchant(null);
                    
                    visitors[i].getClient().announce(MaplePacketCreator.leaveHiredMerchant(i + 1, 0x11));
                    visitors[i].getClient().announce(MaplePacketCreator.hiredMerchantMaintenanceMessage());
                    
                    visitors[i] = null;
                }
            }
        } finally {
            visitorLock.unlock();
        }
    }

    private static boolean canBuy(MapleClient c, Item newItem) {
        return MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner()) && MapleInventoryManipulator.addFromDrop(c, newItem, false);
    }
    
    public void buy(MapleClient c, int item, short quantity) {
        synchronized (items) {
            MaplePlayerShopItem pItem = items.get(item);
            
            Item newItem = pItem.getItem().copy();
            newItem.setQuantity((short) ((pItem.getItem().getQuantity() * quantity)));
            if ((newItem.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA) {
                newItem.setFlag((byte) (newItem.getFlag() ^ ItemConstants.KARMA));
            }
            if (quantity < 1 || pItem.getBundles() < 1 || !pItem.isExist() || pItem.getBundles() < quantity) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            } else if (newItem.getInventoryType().equals(MapleInventoryType.EQUIP) && newItem.getQuantity() > 1) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            } else if (!pItem.isExist()) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            int price = (int) Math.min((long)pItem.getPrice() * quantity, Integer.MAX_VALUE);
            if (c.getPlayer().getMeso() >= price) {
                if (canBuy(c, newItem)) {
                    c.getPlayer().gainMeso(-price, false);
                    if(ServerConstants.USE_ANNOUNCE_SHOPITEMSOLD) announceItemSold(newItem, price);   // idea thanks to vcoc
                    
                    synchronized (sold) {
                        sold.add(new SoldItem(c.getPlayer().getName(), pItem.getItem().getItemId(), quantity, price));
                    }
                    
                    pItem.setBundles((short) (pItem.getBundles() - quantity));
                    if (pItem.getBundles() < 1) {
                        pItem.setDoesExist(false);
                    }
                    MapleCharacter owner = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterByName(ownerName);
                    if (owner != null) {
                        owner.addMerchantMesos(price);
                    } else {
                        try {
                            Connection con = DatabaseConnection.getConnection();
                            
                            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = MerchantMesos + " + price + " WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                                ps.setInt(1, ownerId);
                                ps.executeUpdate();
                            }
                            
                            con.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    c.getPlayer().dropMessage(1, "Your inventory is full. Please clean a slot before buying this item.");
                }
            } else {
                c.getPlayer().dropMessage(1, "You do not have enough mesos.");
            }
            try {
                this.saveItems(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void announceItemSold(Item item, int mesos) {
        String qtyStr = (item.getQuantity() > 1) ? " (qty. " + item.getQuantity() + ")" : "";
        
        MapleCharacter player = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterById(ownerId);
        if(player != null && player.isLoggedin() && !player.isAwayFromWorld()) {
            player.dropMessage(6, "[HIRED MERCHANT] Item '" + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "'" + qtyStr + " has been sold for " + mesos + " mesos.");
        }
    }

    public void forceClose() {
        Server.getInstance().getWorld(world).unregisterHiredMerchant(this);
        
        try {
            saveItems(true);
            synchronized (items) {
                items.clear();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        //Server.getInstance().getChannel(world, channel).removeHiredMerchant(ownerId);
        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(getOwnerId()));

        map.removeMapObject(this);
		
        MapleCharacter player = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterById(ownerId);
        if(player != null) {
                player.setHasMerchant(false);
        } else {
                try {
                        Connection con = DatabaseConnection.getConnection();
                        PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
                        ps.setInt(1, ownerId);
                        ps.executeUpdate();

                        ps.close();
                        con.close();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                }
        }

        map = null;
    }

	public void closeShop(MapleClient c, boolean timeout) {
            map.removeMapObject(this);
            map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(ownerId));
            c.getChannelServer().removeHiredMerchant(ownerId);
            
            try {
                MapleCharacter player = c.getWorldServer().getPlayerStorage().getCharacterById(ownerId);
                if(player != null) {
                        player.setHasMerchant(false);
                } else {
                        Connection con = DatabaseConnection.getConnection();
                        try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                                ps.setInt(1, ownerId);
                                ps.executeUpdate();
                        }
                        con.close();
                }

                List<MaplePlayerShopItem> copyItems = getItems();
                if (check(c.getPlayer(), copyItems) && !timeout) {
                    for (MaplePlayerShopItem mpsi : copyItems) {
                        if(mpsi.isExist()) {
                            if (mpsi.getItem().getInventoryType().equals(MapleInventoryType.EQUIP)) {
                                MapleInventoryManipulator.addFromDrop(c, mpsi.getItem(), false);
                            } else {
                                MapleInventoryManipulator.addById(c, mpsi.getItem().getItemId(), (short) (mpsi.getBundles() * mpsi.getItem().getQuantity()), null, -1, mpsi.getItem().getFlag(), mpsi.getItem().getExpiration());
                            }
                        }
                    }
                    
                    synchronized (items) {
                        items.clear();
                    }
                }

                try {
                    this.saveItems(timeout);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                synchronized (items) {
                    items.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            Server.getInstance().getWorld(world).unregisterHiredMerchant(this);
    }

    public String getOwner() {
        return ownerName;
    }
    
    public void clearItems() {
        synchronized (items) {
            items.clear();
        }
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getDescription() {
        return description;
    }

    public MapleCharacter[] getVisitors() {
        visitorLock.lock();
        try {
            MapleCharacter[] copy = new MapleCharacter[3];
            for(int i = 0; i < visitors.length; i++) copy[i] = visitors[i];
                    
            return copy;
        } finally {
            visitorLock.unlock();
        }
    }

    public List<MaplePlayerShopItem> getItems() {
        synchronized (items) {
            return Collections.unmodifiableList(items);
        }
    }
    
    public boolean hasItem(int itemid) {
        for(MaplePlayerShopItem mpsi : getItems()) {
            if(mpsi.getItem().getItemId() == itemid && mpsi.isExist() && mpsi.getBundles() > 0) {
                return true;
            }
        }
        
        return false;
    }

    public void addItem(MaplePlayerShopItem item) {
        synchronized (items) {
            items.add(item);
        }
        
        try {
            this.saveItems(false);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removeFromSlot(int slot) {
        synchronized (items) {
            items.remove(slot);
        }
        
        try {
            this.saveItems(false);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private int getFreeSlot() {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOpen() {
        return open.get();
    }

    public void setOpen(boolean set) {
        open.getAndSet(set);
    }

    public int getItemId() {
        return itemId;
    }

    public boolean isOwner(MapleCharacter chr) {
        return chr.getId() == ownerId;
    }
    
    public void sendMessage(MapleCharacter chr, String msg) {
        String message = chr.getName() + " : " + msg;
        byte slot = (byte) (getVisitorSlot(chr) + 1);
        
        synchronized (messages) {
            messages.add(new Pair<>(message, slot));
        }
        broadcastToVisitorsThreadsafe(MaplePacketCreator.hiredMerchantChat(message, slot));
    }
    
    public List<MaplePlayerShopItem> sendAvailableBundles(int itemid) {
        List<MaplePlayerShopItem> list = new LinkedList<>();
        List<MaplePlayerShopItem> all = new ArrayList<>();
        
        if(!open.get()) return list;
        
        synchronized (items) {
            for(MaplePlayerShopItem mpsi : items) all.add(mpsi);
        }
        
        for(MaplePlayerShopItem mpsi : all) {
            if(mpsi.getItem().getItemId() == itemid && mpsi.getBundles() > 0 && mpsi.isExist()) {
                list.add(mpsi);
            }
        }
        return list;
    }

    public void saveItems(boolean shutdown) throws SQLException {
        List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
        List<Short> bundles = new ArrayList<>();

        for (MaplePlayerShopItem pItems : getItems()) {
            Item newItem = pItems.getItem();
            short newBundle = pItems.getBundles();
            
            if (shutdown) { //is "shutdown" really necessary?
                newItem.setQuantity((short) (pItems.getItem().getQuantity()));
            } else {
                newItem.setQuantity((short) (pItems.getItem().getQuantity()));
            }
            if (newBundle > 0) {
                itemsWithType.add(new Pair<>(newItem, newItem.getInventoryType()));
                bundles.add(newBundle);
            }
        }
	
        Connection con = DatabaseConnection.getConnection();
        ItemFactory.MERCHANT.saveItems(itemsWithType, bundles, this.ownerId, con);
        con.close();
    }

    private static boolean check(MapleCharacter chr, List<MaplePlayerShopItem> items) {
        List<Pair<Item, MapleInventoryType>> li = new ArrayList<>();
        for (MaplePlayerShopItem item : items) {
            Item it = item.getItem().copy();
            it.setQuantity((short)(it.getQuantity() * item.getBundles()));
            
            li.add(new Pair<>(it, it.getInventoryType()));
        }
        
        return MapleInventory.checkSpotsAndOwnership(chr, li);
    }
    
    public int getChannel() {
        return channel;
    }

    public int getTimeLeft() {
        return (int) ((System.currentTimeMillis() - start) / 1000);
    }

    public void clearMessages() {
        synchronized (messages) {
            messages.clear();
        }
    }
    
    public List<Pair<String, Byte>> getMessages() {
        synchronized (messages) {
            List<Pair<String, Byte>> msgList = new LinkedList<>();
            for(Pair<String, Byte> m : messages) {
                msgList.add(m);
            }
            
            return msgList;
        }
    }

    public int getMapId() {
        return map.getId();
    }
    
    public MapleMap getMap() {
        return map;
    }

    public List<SoldItem> getSold() {
        synchronized (sold) {
            return Collections.unmodifiableList(sold);
        }
    }

    public int getMesos() {
        return mesos;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(MaplePacketCreator.spawnHiredMerchant(this));
    }

    public class SoldItem {

        int itemid, mesos;
        short quantity;
        String buyer;

        public SoldItem(String buyer, int itemid, short quantity, int mesos) {
            this.buyer = buyer;
            this.itemid = itemid;
            this.quantity = quantity;
            this.mesos = mesos;
        }

        public String getBuyer() {
            return buyer;
        }

        public int getItemId() {
            return itemid;
        }

        public short getQuantity() {
            return quantity;
        }

        public int getMesos() {
            return mesos;
        }
    }
}
