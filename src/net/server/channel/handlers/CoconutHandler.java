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
import net.AbstractMaplePacketHandler;
import server.events.gm.MapleCoconut;
import server.events.gm.MapleCoconuts;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public final class CoconutHandler extends AbstractMaplePacketHandler {
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		/*CB 00 A6 00 06 01
		 * A6 00 = coconut id
		 * 06 01 = ?
		 */
		int id = slea.readShort();
		MapleMap map = c.getPlayer().getMap();
		MapleCoconut event = map.getCoconut();
		MapleCoconuts nut = event.getCoconut(id);
		if (!nut.isHittable()){
			return;
		}
		if (event == null){
			return;
		}
		if (currentServerTime() < nut.getHitTime()){
			return;
		}
		if (nut.getHits() > 2 && Math.random() < 0.4) {
			if (Math.random() < 0.01 && event.getStopped() > 0) {
				nut.setHittable(false);
				event.stopCoconut();
				map.broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 1));
				return;
			}
			nut.setHittable(false); // for sure :)
			nut.resetHits(); // For next event (without restarts)
			if (Math.random() < 0.05 && event.getBombings() > 0) {
				map.broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 2));
				event.bombCoconut();
			} else if (event.getFalling() > 0) {
				map.broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 3));
				event.fallCoconut();
				if (c.getPlayer().getTeam() == 0) {
					event.addMapleScore();
					map.broadcastMessage(MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " of Team Maple knocks down a coconut."));
				} else {
					event.addStoryScore();
					map.broadcastMessage(MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " of Team Story knocks down a coconut."));
				}
				map.broadcastMessage(MaplePacketCreator.coconutScore(event.getMapleScore(), event.getStoryScore()));
			}
		} else {
			nut.hit();
			map.broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 1));
		}
	}
}  
