/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.server.channel.handlers;

import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import net.server.channel.Channel;
import scripting.event.EventInstanceManager;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Wedding;

/**
 *
 * @author By Drago/Dragohe4rt
 */
public final class WeddingHandler extends AbstractMaplePacketHandler {
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final byte mode = slea.readByte();
        Channel cs = c.getChannelServer();
        
        if (mode == 6) { //additem
            short slot = slea.readShort();
            int itemid = slea.readInt();
            short quantity = slea.readShort();
            EventInstanceManager eim = c.getPlayer().getEventInstance();
            if (eim != null) {
                String name = eim.getProperty("brideId");
                MapleCharacter chrs = cs.getPlayerStorage().getCharacterById(Integer.parseInt(name));
                //MapleCharacter chrs = cs.getPlayerStorage().getCharacterById(3);
                MapleInventoryType type = ItemConstants.getInventoryType(itemid);
                Item item = chr.getInventory(type).getItem((byte) slot);
                if (itemid == item.getItemId() && quantity <= item.getQuantity()) {
                    if(!(item instanceof Equip)) {
                        item = new Item(itemid, slot, quantity);
                    }
                    chrs.setEquips(item);
                    MapleInventoryManipulator.removeById(chr.getClient(), type, itemid, quantity, false, false);
                    c.announce(Wedding.OnWeddingGiftResult((byte) 0xB, chrs.getItens(), chrs.getItem()));
                }
            }
        } else if (mode == 7) { // noiva abre e pega itens
            byte inventId = slea.readByte();
            int itemPos = slea.readByte();
            MapleInventoryType inv = MapleInventoryType.getByType(inventId);
            Item item = chr.getItemid(itemPos);
            c.getAbstractPlayerInteraction().gainItem(item.getItemId(), item.getQuantity());
            chr.removeItem(item);
            c.announce(Wedding.OnWeddingGiftResult((byte) 0xF, chr.getItens(), chr.getItem()));
        } else if (mode == 8) { // sair update?
            
            c.announce(MaplePacketCreator.enableActions());
        } else {
            System.out.println(mode);
        }
    }
}