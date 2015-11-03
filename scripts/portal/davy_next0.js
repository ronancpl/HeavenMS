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
/***********
@Author Jvlaple
***********/

function enter(pi) {
	var nextMap = 925100100;
	var eim = pi.getPlayer().getEventInstance();
	var party = eim.getPlayers();
	var target = eim.getMapInstance(nextMap);
	var targetPortal = target.getPortal("sp");
	var mobCount = pi.countMonster();
	var playerS = pi.isLeader();
	// only let people through if the eim is ready
	if (playerS == false) {
		// do nothing; send message to player
		pi.getPlayer().dropMessage(6, "Only the party leader may enter this portal.");
		return false;
	}else if (mobCount < 1) {
		eim.setProperty("entryTimeStamp", 1000 * 60 * 6);
		for(var g=0; g<party.size(); g++) {
			party.get(g).changeMap(target, targetPortal);
			party.get(g).getClient().sendClock(party.get(g).getClient(), 300);
		}
		return true;
	}else {
		pi.getPlayer().dropMessage(6, "Please kill all monsters before proceeding.");
		return false;
	}
}