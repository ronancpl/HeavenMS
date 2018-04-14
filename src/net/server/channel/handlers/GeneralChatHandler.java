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

import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.command.Commands;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class GeneralChatHandler extends net.AbstractMaplePacketHandler {
        private static boolean isCommandIssue(char heading, MapleCharacter chr) {
                if(chr.gmLevel() > 1 && heading == '!') {
                        return true;
                } else {
                        return heading == '@';
                }
        }
    
	@Override
        public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
                String s = slea.readMapleAsciiString();
                MapleCharacter chr = c.getPlayer();
                if(chr.getAutobanManager().getLastSpam(7) + 200 > System.currentTimeMillis()) {
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                }
                if (s.length() > Byte.MAX_VALUE && !chr.isGM()) {
                        AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit in General Chat.");
                        FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to send text with length of " + s.length() + "\r\n");
                        c.disconnect(true, false);
                        return;
                }
                char heading = s.charAt(0);
                if (isCommandIssue(heading, chr)) {
                        String[] sp = s.split(" ");
                        sp[0] = sp[0].toLowerCase().substring(1);

                        if(Commands.executeSolaxiaPlayerCommand(c, sp, heading)) {
                            String command = "";
                            for (String used : sp) {
                                    command += used + " ";
                            }

                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                            FilePrinter.print(FilePrinter.USED_COMMANDS, c.getPlayer().getName() + " used: " + heading + command + "on " + sdf.format(Calendar.getInstance().getTime()) + "\r\n");
                        }
                } else if (heading != '/') {
                        int show = slea.readByte();
                        if(chr.getMap().isMuted() && !chr.isGM()) {
                                chr.dropMessage(5, "The map you are in is currently muted. Please try again later.");
                                return;
                        }

                        if (!chr.isHidden()) {
                                chr.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), s, chr.getWhiteChat(), show));	
                        } else {
                                chr.getMap().broadcastGMMessage(MaplePacketCreator.getChatText(chr.getId(), s, chr.getWhiteChat(), show));
                        }

                        chr.getAutobanManager().spam(7);
                }
        }
}

