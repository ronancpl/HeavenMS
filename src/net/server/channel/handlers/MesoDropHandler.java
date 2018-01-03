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

import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;

/**
 *
 * @author Matze
 */
public final class MesoDropHandler extends AbstractMaplePacketHandler {
        @Override
        public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
                MapleCharacter player = c.getPlayer();
                if (!player.isAlive()) {
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                }
                if (!player.canDropMeso()){
                        player.announce(MaplePacketCreator.serverNotice(5, "Fast meso drop has been patched, cut that out. ;)"));
                        return;
                }
                slea.skip(4);
                int meso = slea.readInt();
                if (meso <= player.getMeso() && meso > 9 && meso < 50001) {
                        player.gainMeso(-meso, false, true, false);
                        player.getMap().spawnMesoDrop(meso, player.getPosition(), player, player, true, (byte) 2);
                }
        }
}