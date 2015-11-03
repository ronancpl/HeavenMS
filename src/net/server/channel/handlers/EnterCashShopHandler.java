/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Flav
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler {
    @Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
        	MapleCharacter mc = c.getPlayer();

        	if (mc.getCashShop().isOpened()) return;
        	
			Server.getInstance().getPlayerBuffStorage().addBuffsToStorage(mc.getId(), mc.getAllBuffs());
	        mc.cancelBuffEffects();
	        mc.cancelExpirationTask();
			c.announce(MaplePacketCreator.openCashShop(c, false));
			mc.saveToDB();
			mc.getCashShop().open(true);
			mc.getMap().removePlayer(mc);
			c.getChannelServer().removePlayer(mc);
			
			c.announce(MaplePacketCreator.showCashInventory(c));                
			c.announce(MaplePacketCreator.showGifts(mc.getCashShop().loadGifts()));
			c.announce(MaplePacketCreator.showWishList(mc, false));
			c.announce(MaplePacketCreator.showCash(mc));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
