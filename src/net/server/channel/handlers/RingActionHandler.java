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
package net.server.channel.handlers;

//import java.sql.Connection;
//import java.sql.PreparedStatement;
import client.MapleClient;
import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import client.processor.DueyProcessor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
//import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
//import scripting.npc.NPCScriptManager;
import tools.Pair;
import tools.MaplePacketCreator;
import tools.packets.Wedding;
import net.server.world.World;
import net.server.channel.Channel;
import server.MapleItemInformationProvider;
import client.MapleRing;
import client.inventory.Equip;
import client.inventory.Item;

/**
 * @author Jvlaple
 * @author Ronan (major overhaul on Ring handling mechanics)
 */
public final class RingActionHandler extends AbstractMaplePacketHandler {
    private static int getBoxId(int useItemId) {
        return useItemId == 2240000 ? 4031357 : (useItemId == 2240001 ? 4031359 : (useItemId == 2240002 ? 4031361 : (useItemId == 2240003 ? 4031363 : (1112300 + (useItemId - 2240004)))));
    }
    
    public static void sendEngageProposal(final MapleClient c, final String name, final int itemid) {
        final int newBoxId = getBoxId(itemid);
        final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
        final MapleCharacter source = c.getPlayer();
        
        // TODO: get the correct packet bytes for these popups
        if (source.isMarried()) {
            source.dropMessage(1, "You're already married!");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (source.getPartnerId() > 0) {
            source.dropMessage(1, "You're already engaged!");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (source.getMarriageItemId() > 0) {
            source.dropMessage(1, "You're already engaging someone!");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (target == null) {
            source.dropMessage(1, "Unable to find " + name + " on this channel.");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (target == source) {
            source.dropMessage(1, "You can't engage yourself.");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if(target.getLevel() < 50) {
            source.dropMessage(1, "You can only propose to someone level 50 or higher.");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if(source.getLevel() < 50) {
            source.dropMessage(1, "You can only propose being level 50 or higher.");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (!target.getMap().equals(source.getMap())) {
            source.dropMessage(1, "Make sure your partner is on the same map!");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (!source.haveItem(itemid) || itemid < 2240000 || itemid > 2240015) {
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (target.isMarried()) {
            source.dropMessage(1, "The player is already married!");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (target.getPartnerId() > 0 || target.getMarriageItemId() > 0) {
            source.dropMessage(1, "The player is already engaged!");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (target.getGender() != 1) {
            source.dropMessage(1, "You may only propose to a girl!");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (!MapleInventoryManipulator.checkSpace(c, newBoxId, 1, "")) {
            source.dropMessage(5, "You don't have a ETC slot available right now!");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        } else if (!MapleInventoryManipulator.checkSpace(target.getClient(), newBoxId + 1, 1, "")) {
            source.dropMessage(5, "The girl you proposed doesn't have a ETC slot available right now.");
            source.announce(Wedding.OnMarriageResult((byte) 0));
            return;
        }
        
        source.setMarriageItemId(itemid);
        target.announce(Wedding.OnMarriageRequest(source.getName(), source.getId()));
    }
    
    private static void eraseEngagementOffline(int characterId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            eraseEngagementOffline(characterId, con);
            con.close();
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    private static void eraseEngagementOffline(int characterId, Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("UPDATE characters SET marriageItemId=-1, partnerId=-1 WHERE id=?");
        ps.setInt(1, characterId);
        ps.executeUpdate();

        ps.close();
    }
    
    private static void breakEngagementOffline(int characterId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT marriageItemId FROM characters WHERE id=?");
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int marriageItemId = rs.getInt("marriageItemId");
                
                if (marriageItemId > 0) {
                    PreparedStatement ps2 = con.prepareStatement("UPDATE inventoryitems SET expiration=0 WHERE itemid=? AND characterid=?");
                    ps2.setInt(1, marriageItemId);
                    ps2.setInt(2, characterId);
                    
                    ps2.executeUpdate();
                    ps2.close();
                }
            }
            rs.close();
            ps.close();
            
            eraseEngagementOffline(characterId, con);
            
            con.close();
        } catch (SQLException ex) {
            System.out.println("Error updating offline breakup " + ex.getMessage());
        }
    }
    
    private synchronized static void breakMarriage(MapleCharacter chr) {
        int partnerid = chr.getPartnerId();
        if(partnerid <= 0) return;
        
        chr.getClient().getWorldServer().deleteRelationship(chr.getId(), partnerid);
        MapleRing.removeRing(chr.getMarriageRing());
        
        MapleCharacter partner = chr.getClient().getWorldServer().getPlayerStorage().getCharacterById(partnerid);
        if(partner == null) {
            eraseEngagementOffline(partnerid);
        } else {
            partner.dropMessage(5, chr.getName() + " has decided to break up the marriage.");
            
            //partner.announce(Wedding.OnMarriageResult((byte) 0)); ok, how to gracefully unengage someone without the need to cc?
            partner.announce(Wedding.OnNotifyWeddingPartnerTransfer(0, 0));
            resetRingId(partner);
            partner.setPartnerId(-1);
            partner.setMarriageItemId(-1);
            partner.addMarriageRing(null);
        }
        
        chr.dropMessage(5, "You have successfully break the marriage with " + MapleCharacter.getNameById(partnerid) + ".");
        
        //chr.announce(Wedding.OnMarriageResult((byte) 0));
        chr.announce(Wedding.OnNotifyWeddingPartnerTransfer(0, 0));
        resetRingId(chr);
        chr.setPartnerId(-1);
        chr.setMarriageItemId(-1);
        chr.addMarriageRing(null);
    }
    
    private static void resetRingId(MapleCharacter player) {
        int ringitemid = player.getMarriageRing().getItemId();
        
        Item it = player.getInventory(MapleInventoryType.EQUIP).findById(ringitemid);
        if(it == null) {
            it = player.getInventory(MapleInventoryType.EQUIPPED).findById(ringitemid);
        }

        if(it != null) {
            Equip eqp = (Equip) it;
            eqp.setRingId(-1);
        }
    }
    
    private synchronized static void breakEngagement(MapleCharacter chr) {
        int partnerid = chr.getPartnerId();
        int marriageitemid = chr.getMarriageItemId();
        
        chr.getClient().getWorldServer().deleteRelationship(chr.getId(), partnerid);
        
        MapleCharacter partner = chr.getClient().getWorldServer().getPlayerStorage().getCharacterById(partnerid);
        if(partner == null) {
            breakEngagementOffline(partnerid);
        } else {
            partner.dropMessage(5, chr.getName() + " has decided to break up the engagement.");
            
            int partnerMarriageitemid = marriageitemid + ((chr.getGender() == 0) ? 1 : -1);
            if(partner.haveItem(partnerMarriageitemid)) {
                MapleInventoryManipulator.removeById(partner.getClient(), MapleInventoryType.ETC, partnerMarriageitemid, (short) 1, false, false);
            }
            
            //partner.announce(Wedding.OnMarriageResult((byte) 0)); ok, how to gracefully unengage someone without the need to cc?
            partner.announce(Wedding.OnNotifyWeddingPartnerTransfer(0, 0));
            partner.setPartnerId(-1);
            partner.setMarriageItemId(-1);
        }

        if(chr.haveItem(marriageitemid)) {
            MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.ETC, marriageitemid, (short) 1, false, false);
        }
        chr.dropMessage(5, "You have successfully break the engagement with " + MapleCharacter.getNameById(partnerid) + ".");
        
        //chr.announce(Wedding.OnMarriageResult((byte) 0));
        chr.announce(Wedding.OnNotifyWeddingPartnerTransfer(0, 0));
        chr.setPartnerId(-1);
        chr.setMarriageItemId(-1);
    }
    
    public static void breakMarriageRing(MapleCharacter chr, final int wItemId) {
        final MapleInventoryType type = MapleInventoryType.getByType((byte) (wItemId / 1000000));
        final Item wItem = chr.getInventory(type).findById(wItemId);
        final boolean weddingToken = (wItem != null && type == MapleInventoryType.ETC && wItemId / 10000 == 403);
        final boolean weddingRing = (wItem != null && wItemId / 10 == 111280);

        if (weddingRing) {
            if(chr.getPartnerId() > 0) {
                breakMarriage(chr);
            }

            chr.getMap().disappearingItemDrop(chr, chr, wItem, chr.getPosition());
        } else if (weddingToken) {
            if (chr.getPartnerId() > 0) {
                breakEngagement(chr);
            }

            chr.getMap().disappearingItemDrop(chr, chr, wItem, chr.getPosition());
        }
    }
    
    public static void giveMarriageRings(MapleCharacter player, MapleCharacter partner, int marriageRingId) {
        Pair<Integer, Integer> rings = MapleRing.createRing(marriageRingId, player, partner);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        Item ringObj = ii.getEquipById(marriageRingId);
        Equip ringEqp = (Equip) ringObj;
        ringEqp.setRingId(rings.getLeft());
        player.addMarriageRing(MapleRing.loadFromDb(rings.getLeft()));
        MapleInventoryManipulator.addFromDrop(player.getClient(), ringEqp, false, -1);
        player.broadcastMarriageMessage();

        ringObj = ii.getEquipById(marriageRingId);
        ringEqp = (Equip) ringObj;
        ringEqp.setRingId(rings.getRight());
        partner.addMarriageRing(MapleRing.loadFromDb(rings.getRight()));
        MapleInventoryManipulator.addFromDrop(partner.getClient(), ringEqp, false, -1);
        partner.broadcastMarriageMessage();
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        String name;
        byte slot;
        switch(mode) {
            case 0: // Send Proposal
                sendEngageProposal(c, slea.readMapleAsciiString(), slea.readInt());
                break;
                
            case 1: // Cancel Proposal
                if(c.getPlayer().getMarriageItemId() / 1000000 != 4) {
                    c.getPlayer().setMarriageItemId(-1);
                }
                break;
                
            case 2: // Accept/Deny Proposal
                final boolean accepted = slea.readByte() > 0;
                name = slea.readMapleAsciiString();
                final int id = slea.readInt();
                
                final MapleCharacter source = c.getWorldServer().getPlayerStorage().getCharacterByName(name);
                final MapleCharacter target = c.getPlayer();
                
                if (source == null) {
                    target.announce(MaplePacketCreator.enableActions());
                    return;
                }
                
                final int itemid = source.getMarriageItemId();
                if (target.getPartnerId() > 0 || source.getId() != id || itemid <= 0 || !source.haveItem(itemid) || source.getPartnerId() > 0 || !source.isAlive() || !target.isAlive()) {
                    target.announce(MaplePacketCreator.enableActions());
                    return;
                }
                
                if (accepted) {
                    final int newItemId = getBoxId(itemid);
                    if (!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "") || !MapleInventoryManipulator.checkSpace(source.getClient(), newItemId, 1, "")) {
                        target.announce(MaplePacketCreator.enableActions());
                        return;
                    }
                    
                    try {
                        MapleInventoryManipulator.removeById(source.getClient(), MapleInventoryType.USE, itemid, 1, false, false);
                        
                        int marriageId = c.getWorldServer().createRelationship(source.getId(), target.getId());
                        source.setPartnerId(target.getId()); // engage them (new marriageitemid, partnerid for both)
                        target.setPartnerId(source.getId());
                        
                        source.setMarriageItemId(newItemId);
                        target.setMarriageItemId(newItemId + 1);
                        
                        MapleInventoryManipulator.addById(source.getClient(), newItemId, (short) 1);
                        MapleInventoryManipulator.addById(c, (newItemId + 1), (short) 1);
                        
                        source.announce(Wedding.OnMarriageResult(marriageId, source, false));
                        target.announce(Wedding.OnMarriageResult(marriageId, source, false));
                        
                        source.announce(Wedding.OnNotifyWeddingPartnerTransfer(target.getId(), target.getMapId()));
                        target.announce(Wedding.OnNotifyWeddingPartnerTransfer(source.getId(), source.getMapId()));
                    } catch (Exception e) {
                        System.out.println("Error with engagement " + e.getMessage());
                    }
                } else {
                    source.dropMessage(1, "She has politely declined your engagement request.");
                    source.announce(Wedding.OnMarriageResult((byte) 0));
                    
                    source.setMarriageItemId(-1);
                }
                break;
                
            case 3: // Break Engagement
                breakMarriageRing(c.getPlayer(), slea.readInt());
                break;
                
            case 5: // Invite %s to Wedding
                name = slea.readMapleAsciiString();
                int marriageId = slea.readInt();
                slot = slea.readByte(); // this is an int
                
                int itemId;
                try {
                    itemId = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot).getItemId();
                } catch(NullPointerException npe) {
                    c.announce(MaplePacketCreator.enableActions());
                    return;
                }
                
                if((itemId != 4031377 && itemId != 4031395) || !c.getPlayer().haveItem(itemId)) {
                    c.announce(MaplePacketCreator.enableActions());
                    return;
                }
                
                String groom = c.getPlayer().getName(), bride = MapleCharacter.getNameById(c.getPlayer().getPartnerId());
                int guest = MapleCharacter.getIdByName(name);
                if (groom == null || bride == null || groom.equals("") || bride.equals("") || guest <= 0) {
                    c.getPlayer().dropMessage(5, "Unable to find " + name + "!");
                    return;
                }
                
                try {
                    World wserv = c.getWorldServer();
                    Pair<Boolean, Boolean> registration = wserv.getMarriageQueuedLocation(marriageId);
                    
                    if(registration != null) {
                        if(wserv.addMarriageGuest(marriageId, guest)) {
                            boolean cathedral = registration.getLeft();
                            int newItemId = cathedral ? 4031407 : 4031406;
                            
                            Channel cserv = c.getChannelServer();
                            int resStatus = cserv.getWeddingReservationStatus(marriageId, cathedral);
                            if(resStatus > 0) {
                                long expiration = cserv.getWeddingTicketExpireTime(resStatus + 1);

                                MapleCharacter guestChr = c.getWorldServer().getPlayerStorage().getCharacterById(guest);
                                if(guestChr != null && MapleInventoryManipulator.checkSpace(guestChr.getClient(), newItemId, 1, "") && MapleInventoryManipulator.addById(guestChr.getClient(), newItemId, (short) 1, expiration)) {
                                    guestChr.dropMessage(6, "[WEDDING] You've been invited to " + groom + " and " + bride + "'s Wedding!");
                                } else {
                                    if(guestChr != null && guestChr.isLoggedinWorld()) {
                                        guestChr.dropMessage(6, "[WEDDING] You've been invited to " + groom + " and " + bride + "'s Wedding! Receive your invitation from Duey!");
                                    } else {
                                        c.getPlayer().sendNote(name, "You've been invited to " + groom + " and " + bride + "'s Wedding! Receive your invitation from Duey!", (byte) 0);
                                    }
                                    
                                    Item weddingTicket = new Item(newItemId, (short) 0, (short) 1);
                                    weddingTicket.setExpiration(expiration);

                                    DueyProcessor.addItemToDB(weddingTicket, 1, 0, groom, guest);
                                }
                            } else {
                                c.getPlayer().dropMessage(5, "Wedding is already under way. You cannot invite any more guests for the event.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "'" + name + "' is already invited for your marriage.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Invitation was not sent to '" + name + "'. Either the time for your marriage reservation already came or it was not found.");
                    }
                    
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return;
                }
                
                c.getAbstractPlayerInteraction().gainItem(itemId, (short) -1);
                break;
                
            case 6: // Open Wedding Invitation
                slot = (byte) slea.readInt();
                int invitationid = slea.readInt();
                
                if(invitationid == 4031406 || invitationid == 4031407) {
                    Item item = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
                    if(item == null || item.getItemId() != invitationid) {
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                    }

                    // collision case: most soon-to-come wedding will show up
                    Pair<Integer, Integer> coupleId = c.getWorldServer().getWeddingCoupleForGuest(c.getPlayer().getId(), invitationid == 4031407);
                    if (coupleId != null) {
                        int groomId = coupleId.getLeft(), brideId = coupleId.getRight();
                        c.announce(Wedding.sendWeddingInvitation(MapleCharacter.getNameById(groomId), MapleCharacter.getNameById(brideId)));
                    }
                }
                
                break;
                
            case 9: // Groom and Bride's Wishlist
                short size = slea.readShort();
                List<String> itemnames = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    itemnames.add(slea.readMapleAsciiString());
                }
                
                //System.out.println("G&B WISHLIST: " + itemnames);
                
                /*
                if (c.getPlayer().getMarriageItemId() > -1) {
                    switch(c.getPlayer().getMarriageItemId()) {
                        case 10: // Premium Cathedral
                            c.getAbstractPlayerInteraction().gainItem(4031375, (short)1);
                            c.getAbstractPlayerInteraction().gainItem(4031395, (short)15);
                            break;
                        case 11: // Normal Cathedral
                            c.getAbstractPlayerInteraction().gainItem(4031480, (short)1);
                            c.getAbstractPlayerInteraction().gainItem(4031395, (short)15);
                            break;
                        case 20: // Premium Chapel
                            c.getAbstractPlayerInteraction().gainItem(4031376, (short)1);
                            c.getAbstractPlayerInteraction().gainItem(4031377, (short)15);
                            break;
                        case 21: // Normal Chapel
                            c.getAbstractPlayerInteraction().gainItem(4031481, (short)1);
                            c.getAbstractPlayerInteraction().gainItem(4031377, (short)15);
                            break;
                        default: {
                            System.out.println("Invalid Wedding Type for player " + c.getPlayer().getName() + "!");
                            break;
                        }
                    }
                    
                    //c.getPlayer().setMarriageItemId(-1); ?????
                }
                
                if (c.getPlayer().getWishlist() == null) {
                    c.getPlayer().registerWishlist(itemnames);
                }
                
                if (c.getPlayer().getWedding() != null) {
                    if (c.getPlayer().getGender() == 0 ? c.getPlayer().getWedding().isExistantGroom(c.getPlayer().getId()) : c.getPlayer().getWedding().isExistantBride(c.getPlayer().getId())) {
                        c.getPlayer().getWedding().registerWishlist(c.getPlayer().getGender() == 1, itemnames);
                    }
                }
                */
                break;
                
            default:
                System.out.println("Unhandled RING_ACTION Mode: " + slea.toString());
                break;
        }
        
        c.announce(MaplePacketCreator.enableActions());
    }
}
