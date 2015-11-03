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
//import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
//import scripting.npc.NPCScriptManager;
import tools.MaplePacketCreator;

/**
 * @author Jvlaple
 */
public final class RingActionHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        MapleCharacter player = c.getPlayer();
        switch (mode) {
            case 0: //Send
                String partnerName = slea.readMapleAsciiString();
                MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(partnerName);
                if (partnerName.equalsIgnoreCase(player.getName())) {
                    c.getPlayer().dropMessage(1, "You cannot put your own name in it.");
                    return;
                } else if (partner == null) {
                    c.getPlayer().dropMessage(1, partnerName + " was not found on this channel. If you are both logged in, please make sure you are in the same channel.");
                    return;
                } else if (partner.getGender() == player.getGender()) {
                    c.getPlayer().dropMessage(1, "Your partner is the same gender as you.");
                    return;
                } //else if (player.isMarried() && partner.isMarried())
                // NPCScriptManager.getInstance().start(partner.getClient(), 9201002, "marriagequestion", player);
                break;
            case 1: //Cancel send
                c.getPlayer().dropMessage(1, "You've cancelled the request.");
                boolean accepted = slea.readByte() > 0;
                String proposerName = slea.readMapleAsciiString();
                if (accepted) {
                    c.announce(MaplePacketCreator.sendEngagementRequest(proposerName));
                }
                break;
            case 2:
                slea.readByte(); //type
            case 3: //Drop Ring
                /*
                if (player.getPartner() != null) {
                try {
                Connection con = DatabaseConnection.getConnection();
                int pid = 0;
                if (player.getGender() == 0)
                pid = player.getId();
                else
                pid = player.getPartner().getId();//we have an engagements SQL?
                PreparedStatement ps = con.prepareStatement("DELETE FROM engagements WHERE husbandid = ?");
                ps.setInt(1, pid);
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("UPDATE characters SET marriagequest = 0 WHERE id = ?, and WHERE id = ?");
                ps.setInt(1, player.getId());
                ps.setInt(2, player.getPartner().getId());
                ps.executeUpdate();
                ps.close();
                } catch (Exception ex) {
                }
                c.getPlayer().dropMessage(1, "Your engagement has been broken up.");
                break;
                }*/
                break;
            case 9: // groom's wishlist
                int amount = slea.readShort();
                if (amount > 10) {
                    amount = 10;
                }
                String[] items = new String[10];
                for (int i = 0; i < amount; i++) {
                    items[i] = slea.readMapleAsciiString();
                }
                c.announce(MaplePacketCreator.sendGroomWishlist()); //WTF<
                break;
            default:
                System.out.println("NEW RING ACTION " + mode);
                break;
        }
    }
}
