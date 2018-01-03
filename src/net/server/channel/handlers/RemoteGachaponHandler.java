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
import client.autoban.AutobanFactory;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Generic
 */
public final class RemoteGachaponHandler extends AbstractMaplePacketHandler {
        @Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int ticket = slea.readInt();
		int gacha = slea.readInt();
		if (ticket != 5451000){
			AutobanFactory.GENERAL.alert(c.getPlayer(), " Tried to use RemoteGachaponHandler with item id: " + ticket);
			c.disconnect(false, false);
			return;
		} else if(gacha < 0 || gacha > 11) {
			AutobanFactory.GENERAL.alert(c.getPlayer(), " Tried to use RemoteGachaponHandler with mode: " + gacha);
			c.disconnect(false, false);
			return;
		} else if (c.getPlayer().getInventory(ItemConstants.getInventoryType(ticket)).countById(ticket) < 1) {
			AutobanFactory.GENERAL.alert(c.getPlayer(), " Tried to use RemoteGachaponHandler without a ticket.");
			c.disconnect(false, false);
			return;
		}
		int npcId = 9100100;
		if (gacha != 8 && gacha != 9) {
			npcId += gacha;
		} else {
			npcId = gacha == 8 ? 9100109 : 9100117;
		}
		NPCScriptManager.getInstance().start(c, npcId, "gachaponRemote", null);
	}
}
