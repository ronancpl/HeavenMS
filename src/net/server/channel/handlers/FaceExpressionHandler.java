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

import client.MapleClient;
import client.MapleCharacter;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class FaceExpressionHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int emote = slea.readInt();
        
        if (emote > 7) {
            int emoteid = 5159992 + emote;
            if (chr.getInventory(ItemConstants.getInventoryType(emoteid)).findById(emoteid) == null) {
                return;
            }
        }
        
        if(c.tryacquireClient()) {
            try {   // expecting players never intends to wear the emote 0 (default face, that changes back after 5sec timeout)
                if (emote != 0 && chr.isLoggedinWorld()) {
                    chr.changeFaceExpression(emote);
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
