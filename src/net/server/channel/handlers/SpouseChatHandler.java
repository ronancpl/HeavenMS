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

//import client.MapleCharacter;
import client.MapleClient;
//import client.command.CommandProcessor;
import net.AbstractMaplePacketHandler;
//import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SpouseChatHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        System.out.println(slea.toString());
//        slea.readMapleAsciiString();//recipient
//        String msg = slea.readMapleAsciiString();
//        if (!CommandProcessor.processCommand(c, msg))
//            if (c.getPlayer().isMarried()) {
//                MapleCharacter wife = c.getChannelServer().getPlayerStorage().getCharacterById(c.getPlayer().getPartnerId());
//                if (wife != null) {
//                    wife.getClient().announce(MaplePacketCreator.sendSpouseChat(c.getPlayer(), msg));
//                    c.announce(MaplePacketCreator.sendSpouseChat(c.getPlayer(), msg));
//                } else
//                    try {
//                        if (c.getChannelServer().getWorldInterface().isConnected(wife.getName())) {
//                            c.getChannelServer().getWorldInterface().sendSpouseChat(c.getPlayer().getName(), wife.getName(), msg);
//                            c.announce(MaplePacketCreator.sendSpouseChat(c.getPlayer(), msg));
//                        } else
//                            c.getPlayer().message("You are either not married or your spouse is currently offline.");
//                    } catch (Exception e) {
//                        c.getPlayer().message("You are either not married or your spouse is currently offline.");
//                        c.getChannelServer().reconnectWorld();
//                    }
//            }
    }
}
