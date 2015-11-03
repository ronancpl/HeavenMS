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
package net.server.handlers.login;

import client.MapleClient;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.channel.Channel;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ServerStatusRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte world = (byte) slea.readShort();//Wuuu? ):
        int status;
        int num = 0;
        for (Channel ch : Server.getInstance().getWorld(world).getChannels()) {
            num += ch.getConnectedClients();
        }
        if (num >= ServerConstants.CHANNEL_LOAD) {
            status = 2;
        } else if (num >= ServerConstants.CHANNEL_LOAD * .8) { // More than 80 percent o___o
            status = 1;
        } else {
            status = 0;
        }
        c.announce(MaplePacketCreator.getServerStatus(status));
    }
}
