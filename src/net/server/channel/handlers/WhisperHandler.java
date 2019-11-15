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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import config.YamlConfig;
import net.AbstractMaplePacketHandler;
import net.server.world.World;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.LogHelper;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import java.sql.Connection;

/**
 *
 * @author Matze
 */
public final class WhisperHandler extends AbstractMaplePacketHandler {
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        if (mode == 6) { // whisper
            String recipient = slea.readMapleAsciiString();
            String text = slea.readMapleAsciiString();
            MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
            if (c.getPlayer().getAutobanManager().getLastSpam(7) + 200 > currentServerTime()) {
                return;
            }
            if (text.length() > Byte.MAX_VALUE && !player.isGM()) {
            	AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with whispers.");
            	FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to send text with length of " + text.length());
            	c.disconnect(true, false);
            	return;
            }
            if (player != null) {
                player.getClient().announce(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
                if (YamlConfig.config.server.USE_ENABLE_CHAT_LOG) {
                    LogHelper.logChat(c, "Whisper To " + player.getName(), text);
                }
                if(player.isHidden() && player.gmLevel() >= c.getPlayer().gmLevel()) {
                    c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                } else {
                    c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
                }
            } else {// not found
                World world = c.getWorldServer();
                    if (world.isConnected(recipient)) {
                        world.whisper(c.getPlayer().getName(), recipient, c.getChannel(), text);
                        if (YamlConfig.config.server.USE_ENABLE_CHAT_LOG) {
                            LogHelper.logChat(c, "Whisper To " + recipient, text);
                        }
                        player = world.getPlayerStorage().getCharacterByName(recipient);
                        if(player.isHidden() && player.gmLevel() >= c.getPlayer().gmLevel())
                            c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                        else
                            c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
                    } else {
                        c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                    }
            }
			c.getPlayer().getAutobanManager().spam(7);
        } else if (mode == 5) { // - /find
            String recipient = slea.readMapleAsciiString();
            MapleCharacter victim = c.getWorldServer().getPlayerStorage().getCharacterByName(recipient);
            if (victim != null && c.getPlayer().gmLevel() >= victim.gmLevel()) {
                if (victim.getCashShop().isOpened()) {  // in CashShop
                    c.announce(MaplePacketCreator.getFindReply(victim.getName(), -1, 2));
                } else if (victim.isAwayFromWorld()) {  // in MTS
                    c.announce(MaplePacketCreator.getFindReply(victim.getName(), -1, 0));
                } else if (victim.getClient().getChannel() != c.getChannel()) { // in another channel, issue detected thanks to MedicOP
                    c.announce(MaplePacketCreator.getFindReply(victim.getName(), victim.getClient().getChannel() - 1, 3));
                } else {
                    c.announce(MaplePacketCreator.getFindReply(victim.getName(), victim.getMap().getId(), 1));
                }
            } else if (c.getPlayer().isGM()) { // not found
                try {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("SELECT gm FROM characters WHERE name = ?");
                    ps.setString(1, recipient);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        if (rs.getInt("gm") >= c.getPlayer().gmLevel()) {
                            c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                            return;
                        }
                    }
                    rs.close();
                    ps.close();
                    con.close();
                    byte channel = (byte) (c.getWorldServer().find(recipient) - 1);
                    if (channel > -1) {
                        c.announce(MaplePacketCreator.getFindReply(recipient, channel, 3));
                    } else {
                        c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
            }
        } else if (mode == 0x44) {
            //Buddy find, thanks to Atoot
            
            String recipient = slea.readMapleAsciiString();
            MapleCharacter player = c.getWorldServer().getPlayerStorage().getCharacterByName(recipient);
            if (player != null && c.getPlayer().gmLevel() >= player.gmLevel()) {
                if (player.getCashShop().isOpened()) {  // in CashShop
                    c.announce(MaplePacketCreator.getBuddyFindReply(player.getName(), -1, 2));
                } else if (player.isAwayFromWorld()) {  // in MTS
                    c.announce(MaplePacketCreator.getBuddyFindReply(player.getName(), -1, 0));
                } else if (player.getClient().getChannel() != c.getChannel()) { // in another channel
                    c.announce(MaplePacketCreator.getBuddyFindReply(player.getName(), player.getClient().getChannel() - 1, 3));
                } else {
                    c.announce(MaplePacketCreator.getBuddyFindReply(player.getName(), player.getMap().getId(), 1));
                }
            }
        }
    }
}
