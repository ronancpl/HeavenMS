/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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

import constants.ItemConstants;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.newyear.NewYearCardRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Ronan - header layout courtesy of Eric 
 */
public final class NewYearCardHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {        
        final MapleCharacter player = c.getPlayer();
        byte reqMode = slea.readByte();                 //[00] -> NewYearReq (0 = Send) 
        
        if(reqMode == 0) {  // card has been sent
            if(player.haveItem(2160101)) {  // new year's card
                short slot = slea.readShort();                      //[00 2C] -> nPOS (Item Slot Pos) 
                int itemid = slea.readInt();                        //[00 20 F5 E5] -> nItemID (item id) 

                int status = getValidNewYearCardStatus(itemid, player, slot);
                if(status == 0) {
                    if(player.canHold(4300000, 1)) {
                        String receiver = slea.readMapleAsciiString();  //[04 00 54 65 73 74] -> sReceiverName (person to send to) 

                        int receiverid = getReceiverId(receiver, c.getWorld());
                        if(receiverid != -1) {
                            if(receiverid != c.getPlayer().getId()) {
                                String message = slea.readMapleAsciiString();   //[06 00 4C 65 74 74 65 72] -> sContent (message)  

                                NewYearCardRecord newyear = new NewYearCardRecord(player.getId(), player.getName(), receiverid, receiver, message);
                                NewYearCardRecord.saveNewYearCard(newyear);
                                player.addNewYearRecord(newyear);

                                player.getClient().getAbstractPlayerInteraction().gainItem(2160101, (short)-1);
                                player.getClient().getAbstractPlayerInteraction().gainItem(4300000, (short) 1);

                                Server.getInstance().setNewYearCard(newyear);
                                newyear.startNewYearCardTask();
                                player.announce(MaplePacketCreator.onNewYearCardRes(player, newyear, 4, 0));    // successfully sent
                            } else {
                                player.announce(MaplePacketCreator.onNewYearCardRes(player, -1, 5, 0xF));   // cannot send to yourself
                            }
                        } else {
                            player.announce(MaplePacketCreator.onNewYearCardRes(player, -1, 5, 0x13));  // cannot find such character
                        }
                    } else {
                        player.announce(MaplePacketCreator.onNewYearCardRes(player, -1, 5, 0x10));  // inventory full
                    }
                } else {
                    player.announce(MaplePacketCreator.onNewYearCardRes(player, -1, 5, status));  // item and inventory errors
                }
            } else {
                player.announce(MaplePacketCreator.onNewYearCardRes(player, -1, 5, 0x11));  // have no card to send
            }
        } else {    //receiver accepted the card
            int cardid = slea.readInt();
            
            NewYearCardRecord newyear = NewYearCardRecord.loadNewYearCard(cardid);
            
            if(newyear != null && newyear.getReceiverId() == player.getId() && !newyear.isReceiverCardReceived()) {
                if(!newyear.isSenderCardDiscarded()) {
                    if(player.canHold(4301000, 1)) {
                        newyear.stopNewYearCardTask();
                        NewYearCardRecord.updateNewYearCard(newyear);

                        player.getClient().getAbstractPlayerInteraction().gainItem(4301000, (short)1);
                        if(!newyear.getMessage().isEmpty()) player.dropMessage(6, "[NEW YEAR] " + newyear.getSenderName() + ": " + newyear.getMessage());

                        player.addNewYearRecord(newyear);
                        player.announce(MaplePacketCreator.onNewYearCardRes(player, newyear, 6, 0));    // successfully rcvd

                        player.getMap().broadcastMessage(MaplePacketCreator.onNewYearCardRes(player, newyear, 0xD, 0));

                        MapleCharacter sender = c.getWorldServer().getPlayerStorage().getCharacterById(newyear.getSenderId());
                        if(sender != null && sender.isLoggedinWorld()) {
                            sender.getMap().broadcastMessage(MaplePacketCreator.onNewYearCardRes(sender, newyear, 0xD, 0));
                            sender.dropMessage(6, "[NEW YEAR] Your addressee successfully received the New Year card.");
                        }
                    } else {
                        player.announce(MaplePacketCreator.onNewYearCardRes(player, -1, 5, 0x10));  // inventory full
                    }
                } else {
                    player.dropMessage(6, "[NEW YEAR] The sender of the New Year card already dropped it. Nothing to receive.");
                }
            } else {
                if(newyear == null) {
                    player.dropMessage(6, "[NEW YEAR] The sender of the New Year card already dropped it. Nothing to receive.");
                }
            }
        }
    }
    
    private static int getReceiverId(String receiver, int world) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, world FROM characters WHERE name LIKE ?")) {
                ps.setString(1, receiver);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if(rs.getInt("world") == world) {
                            return rs.getInt("id");
                        }
                    }
                }
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        
        return -1;
    }
    
    private static int getValidNewYearCardStatus(int itemid, MapleCharacter player, short slot) {
        if(!ItemConstants.isNewYearCardUse(itemid)) return 0x14;
        
        Item it = player.getInventory(ItemConstants.getInventoryType(itemid)).getItem(slot);        
        return (it != null && it.getItemId() == itemid) ? 0 : 0x12;
    } 
}
