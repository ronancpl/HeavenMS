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
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleHiredMerchant;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author RonanLana (synchronization of Fredrick modules)
 */
public class FredrickProcessor {
    private static boolean canRetrieveFromFredrick(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items) {
        if (chr.getMeso() + chr.getMerchantMeso() < 0) {
            return false;
        }
        return MapleInventory.checkSpotsAndOwnership(chr, items);
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
                    if (!canRetrieveFromFredrick(chr, items)) {
                        chr.announce(MaplePacketCreator.fredrickMessage((byte) 0x21));
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
                            FilePrinter.print(FilePrinter.FREDRICK + chr.getName() + ".txt", chr.getName() + " gained " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")\r\n");
                        }

                        chr.announce(MaplePacketCreator.fredrickMessage((byte) 0x1E));
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
