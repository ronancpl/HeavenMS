/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Kevin
 */
public class WeddingHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        //System.out.println("Wedding Packet: " + slea);
        MapleCharacter chr = c.getPlayer();
        byte operation = slea.readByte();
        switch (operation) {
            case 0x06://Add an item to the Wedding Registry
                short slot = slea.readShort();
                int itemid = slea.readInt();
                short quantity = slea.readShort();
                MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
                Item item = chr.getInventory(type).getItem(slot);
                if (itemid == item.getItemId() && quantity <= item.getQuantity()) {
                    c.announce(MaplePacketCreator.addItemToWeddingRegistry(chr, item));
                }
        }
    }
}
