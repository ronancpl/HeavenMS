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
import client.inventory.MapleInventoryType;
import com.mysql.jdbc.Statement;
import constants.ItemConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import net.server.Server;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePlayerShopItem;
import server.TimerManager;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author XoticStory
 */
public class HiredMerchant extends AbstractMapleMapObject {

    private int ownerId, itemId, mesos = 0;
    private int channel, world;
    private long start;
    private String ownerName = "";
    private String description = "";
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private List<MaplePlayerShopItem> items = new LinkedList<>();
    private List<Pair<String, Byte>> messages = new LinkedList<>();
    private List<SoldItem> sold = new LinkedList<>();
    private boolean open;
    public ScheduledFuture<?> schedule = null;
    private MapleMap map;

    public HiredMerchant(final MapleCharacter owner, int itemId, String desc) {
        this.setPosition(owner.getPosition());
        this.start = System.currentTimeMillis();
        this.ownerId = owner.getId();
        this.channel = owner.getClient().getChannel();
        this.world = owner.getWorld();
        this.itemId = itemId;
        this.ownerName = owner.getName();
        this.description = desc;
        this.map = owner.getMap();
        this.schedule = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                HiredMerchant.this.forceClose();
				Server.getInstance().getChannel(world, channel).removeHiredMerchant(ownerId);
            }
        }, 1000 * 60 * 60 * 24);
    }

    public void broadcastToVisitors(final byte[] packet) {
        for (MapleCharacter visitor : visitors) {
            if (visitor != null) {
                visitor.getClient().announce(packet);
            }
        }
    }

    public void addVisitor(MapleCharacter visitor) {
        int i = this.getFreeSlot();
        if (i > -1) {
            visitors[i] = visitor;
            broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorAdd(visitor, i + 1));
        }
    }

    public void removeVisitor(MapleCharacter visitor) {
        int slot = getVisitorSlot(visitor);
        if (slot < 0){ //Not found
        	return;
        }
        if (visitors[slot] != null && visitors[slot].getId() == visitor.getId()) {
            visitors[slot] = null;
            if (slot != -1) {
                broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorLeave(slot + 1));
            }
        }
    }

    public int getVisitorSlot(MapleCharacter visitor) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null && visitors[i].getId() == visitor.getId()){
                return i;
            }
        }
        return -1; //Actually 0 because of the +1's.
    }

    public void removeAllVisitors(String message) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                visitors[i].setHiredMerchant(null);
                visitors[i].getClient().announce(MaplePacketCreator.leaveHiredMerchant(i + 1, 0x11));
                if (message.length() > 0) {
                    visitors[i].dropMessage(1, message);
                }
                visitors[i] = null;
            }
        }
    }

    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = items.get(item);
        synchronized (items) {
            Item newItem = pItem.getItem().copy();
            newItem.setQuantity((short) ((pItem.getItem().getQuantity() * quantity)));
            if ((newItem.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA) {
                newItem.setFlag((byte) (newItem.getFlag() ^ ItemConstants.KARMA));
            }
            if (newItem.getType() == 2 && (newItem.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES) {
                newItem.setFlag((byte) (newItem.getFlag() ^ ItemConstants.SPIKES));
            }
            if (quantity < 1 || pItem.getBundles() < 1 || !pItem.isExist() || pItem.getBundles() < quantity) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            } else if (newItem.getType() == 1 && newItem.getQuantity() > 1) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            } else if (!pItem.isExist()) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            int price = pItem.getPrice() * quantity;
            if (c.getPlayer().getMeso() >= price) {
                if (MapleInventoryManipulator.addFromDrop(c, newItem, true)) {
                    c.getPlayer().gainMeso(-price, false);
                    sold.add(new SoldItem(c.getPlayer().getName(), pItem.getItem().getItemId(), quantity, price));
                    pItem.setBundles((short) (pItem.getBundles() - quantity));
                    if (pItem.getBundles() < 1) {
                        pItem.setDoesExist(false);
                    }
                    MapleCharacter owner = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterByName(ownerName);
                    if (owner != null) {
                        owner.addMerchantMesos(price);
                    } else {
                        try {
                            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = MerchantMesos + " + price + " WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                                ps.setInt(1, ownerId);
                                ps.executeUpdate();
                            }
                        } catch (Exception e) {
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
            }
        }
    }

    public void forceClose() {
        if (schedule != null) {
            schedule.cancel(false);
        }
        try {
            saveItems(true);
            items.clear();
        } catch (SQLException ex) {
        }
        //Server.getInstance().getChannel(world, channel).removeHiredMerchant(ownerId);
        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(getOwnerId()));

        map.removeMapObject(this);
		
		MapleCharacter player = Server.getInstance().getWorld(world).getPlayerStorage().getCharacterById(ownerId);
			if(player != null) {
				player.setHasMerchant(false);
			} else {
				try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
					ps.setInt(1, ownerId);
					ps.executeUpdate();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}

        map = null;
        schedule = null;
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
				try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
					ps.setInt(1, ownerId);
					ps.executeUpdate();
				}
			}
            if (check(c.getPlayer(), getItems()) && !timeout) {
                for (MaplePlayerShopItem mpsi : getItems()) {
                    if (mpsi.isExist() && (mpsi.getItem().getType() == MapleInventoryType.EQUIP.getType())) {
                        MapleInventoryManipulator.addFromDrop(c, mpsi.getItem(), false);
                    } else if (mpsi.isExist()) {
                        MapleInventoryManipulator.addById(c, mpsi.getItem().getItemId(), (short) (mpsi.getBundles() * mpsi.getItem().getQuantity()), null, -1, mpsi.getItem().getFlag(), mpsi.getItem().getExpiration());
                    }
                }
                items.clear();
            }
            try {
                this.saveItems(timeout);
            } catch (Exception e) {
            }
            items.clear();

        } catch (Exception e) {
        }
        schedule.cancel(false);
    }

    public String getOwner() {
        return ownerName;
    }
    
    public void clearItems() {
        items.clear();
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getDescription() {
        return description;
    }

    public MapleCharacter[] getVisitors() {
        return visitors;
    }

    public List<MaplePlayerShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(MaplePlayerShopItem item) {
        items.add(item);
        try {
            this.saveItems(false);
        } catch (SQLException ex) {
        }
    }

    public void removeFromSlot(int slot) {
        items.remove(slot);
        try {
            this.saveItems(false);
        } catch (SQLException ex) {
        }
    }
    
    
    public int getFreeSlot() {
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
        return open;
    }

    public void setOpen(boolean set) {
        this.open = set;
    }

    public int getItemId() {
        return itemId;
    }

    public boolean isOwner(MapleCharacter chr) {
        return chr.getId() == ownerId;
    }

    public void saveItems(boolean shutdown) throws SQLException {
        List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

        for (MaplePlayerShopItem pItems : items) {
            Item newItem = pItems.getItem();
            if (shutdown) {
                newItem.setQuantity((short) (pItems.getItem().getQuantity() * pItems.getBundles()));
            } else {
                newItem.setQuantity(pItems.getItem().getQuantity());
            }
            if (pItems.getBundles() > 0) {
                itemsWithType.add(new Pair<>(newItem, MapleInventoryType.getByType(newItem.getType())));
            }
        }
		
        ItemFactory.MERCHANT.saveItems(itemsWithType, this.ownerId, DatabaseConnection.getConnection());
    }

    private static boolean check(MapleCharacter chr, List<MaplePlayerShopItem> items) {
        byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
        List<MapleInventoryType> li = new LinkedList<>();
        for (MaplePlayerShopItem item : items) {
            final MapleInventoryType invtype = MapleItemInformationProvider.getInstance().getInventoryType(item.getItem().getItemId());
            if (!li.contains(invtype)) {
                li.add(invtype);
            }
            if (invtype == MapleInventoryType.EQUIP) {
                eq++;
            } else if (invtype == MapleInventoryType.USE) {
                use++;
            } else if (invtype == MapleInventoryType.SETUP) {
                setup++;
            } else if (invtype == MapleInventoryType.ETC) {
                etc++;
            } else if (invtype == MapleInventoryType.CASH) {
                cash++;
            }
        }
        for (MapleInventoryType mit : li) {
            if (mit == MapleInventoryType.EQUIP) {
                if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() <= eq) {
                    return false;
                }
            } else if (mit == MapleInventoryType.USE) {
                if (chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() <= use) {
                    return false;
                }
            } else if (mit == MapleInventoryType.SETUP) {
                if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() <= setup) {
                    return false;
                }
            } else if (mit == MapleInventoryType.ETC) {
                if (chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() <= etc) {
                    return false;
                }
            } else if (mit == MapleInventoryType.CASH) {
                if (chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() <= cash) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getChannel() {
        return channel;
    }

    public int getTimeLeft() {
        return (int) ((System.currentTimeMillis() - start) / 1000);
    }

    public List<Pair<String, Byte>> getMessages() {
        return messages;
    }

    public int getMapId() {
        return map.getId();
    }

    public List<SoldItem> getSold() {
        return sold;
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
