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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ViewAllCharSelectedHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int charId = slea.readInt();
        slea.readInt(); // please don't let the client choose which world they should login
        
        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            c.getSession().close(true);
            return;
        }
        
        c.setWorld(server.getCharacterWorld(charId));
        if(c.getWorldServer().isWorldCapacityFull()) {
            c.announce(MaplePacketCreator.getAfterLoginError(10));
            return;
        }
        
        String macs = slea.readMapleAsciiString();
        c.updateMacs(macs);
        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
        }
        
        try {
            int channel = Randomizer.rand(1, c.getWorldServer().getChannelsSize());
            c.setChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
            c.setChannel(1);
        }
        
        server.unregisterLoginState(c);
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
        server.setCharacteridInTransition((InetSocketAddress) c.getSession().getRemoteAddress(), charId);
        
        String[] socket = server.getIP(c.getWorld(), c.getChannel()).split(":");
        try {
            c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
