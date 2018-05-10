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
importPackage(Packages.server.life);

function enter(pi) {
	if(pi.isQuestStarted(21301) && pi.getQuestProgress(21301, 9001013) == 0) {
		if(pi.getPlayerCount(108010700) != 0) {
			pi.message("The portal is blocked from the other side. I wonder if someone is already fighting the Thief Crow?");
			return false;
		} else {
			var map = pi.getClient().getChannelServer().getMapFactory().getMap(108010700);
			spawnMob(2732, 3, 9001013, map);
			
			pi.playPortalSound();
			pi.warp(108010700, "west00");
		}
	} else {
		pi.playPortalSound(); pi.warp(140020300, 1);
	}
	return true;
}

function spawnMob(x, y, id, map) {
	if(map.getMonsterById(id) != null)
		return;
		
	var mob = MapleLifeFactory.getMonster(id);
	map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(x, y));
}