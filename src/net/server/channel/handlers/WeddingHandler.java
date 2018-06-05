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
import constants.ItemConstants;
import tools.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Wedding;

/**
 *
 * @author Eric
 */
public final class WeddingHandler extends AbstractMaplePacketHandler {
    /*
    public static final void OnWeddingProgress(byte action, MapleClient c) {
        // -- Pelvis Bebop: 
        // 0x00: "We are gathered here today..."
        // 0x01: "Very well! I pronounce you..."
        // 0x02: "You two truly are a sight to..."
        // 0x03: Wedding Ceremony Ended, initialize the Wedding Effect upon the two married characters
        // -- High Priest John: (Unknown action bytes)
        // 0x00: " "
        // 0x01: " "
        // 0x02: "Do you wish to bless this couple?..."
        // 0x03: Wedding Ceremony Ended, initialize the Wedding Effect upon the two married characters
        if (c.getPlayer().getWedding() != null) {
            if (c.getPlayer().getGender() == 0 ? c.getPlayer().getWedding().isExistantGroom(c.getPlayer().getId()) : c.getPlayer().getWedding().isExistantBride(c.getPlayer().getId())) {
                c.getPlayer().getMap().broadcastMessage(Wedding.OnWeddingProgress(action == 2, c.getPlayer().getId(), c.getPlayer().getPartnerId(), (byte)(action+1)));
                c.getPlayer().getWedding().incrementStage();
                c.getPlayer().getPartner().getWedding().incrementStage(); // pls don't b a bitch and throw npe ):<
                if (action == 2) {
                    c.getPlayer().setMarried(true);
                    c.getChannelServer().getPlayerStorage().getCharacterById(c.getPlayer().getPartnerId()).setMarried(true);
                }
            }
        }
        c.announce(MaplePacketCreator.enableActions());
    }
    
    public static final void OnWeddingGiftResult(SeekableLittleEndianAccessor slea, MapleClient c) {
        System.out.println("New WEDDING_GIFT_RESULT: " + slea.toString());
        byte mode = slea.readByte();
        switch(mode) {
            case 0x06: // "SEND ITEM"
                short slot = slea.readShort(); // isn't this a byte? o.O
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                if (c.getPlayer().getInventory(ItemConstants.getInventoryType(itemId)).getItem((byte)slot).getItemId() == itemId && c.getPlayer().getInventory(InventoryConstants.getInventoryType(itemId)).getItem((byte)slot).getQuantity() >= quantity) {
                    if (c.getPlayer().getWedding() == null) {
                        c.getPlayer().startWedding(); // TODO
                    }
                    List<String> itemnames = new ArrayList<>();
                    Item item = c.getPlayer().getInventory(ItemConstants.getInventoryType(itemId)).getItem((byte)slot);
                    boolean bride = false;
                    c.getPlayer().getWedding().registerWishlistItem(item, bride);
                    c.announce(Wedding.OnWeddingGiftResult((byte)11, itemnames, c.getPlayer().getWedding().getWishlistItems(bride))); // todo: remove item from inventory if success
                }
            case 0x08: // "EXIT"
                if (slea.available() != 0) {
                    System.out.println("WEDDING_GIFT_RESULT: " + slea.toString());
                }
                c.announce(MaplePacketCreator.enableActions());
                break;
            default: {
                System.out.println("Unknown Mode Found: " + mode + " : " + slea.toString());
            }
        }
    }
    */
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.announce(MaplePacketCreator.enableActions());
    }
}