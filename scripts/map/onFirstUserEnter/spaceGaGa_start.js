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
/*
 *@Author:     kevintjuh93
*/

importPackage(Packages.tools); 
var player;

function start(ms) { 
	player = ms.getPlayer();
        player.resetEnteredScript(); 
        ms.getClient().announce(MaplePacketCreator.showEffect("event/space/start")); 
        player.startMapEffect("Please rescue Gaga within the time limit.", 5120027); 
	var map = player.getMap();
	if (map.getTimeLeft() > 0) {
		ms.getClient().announce(MaplePacketCreator.getClock(map.getTimeLeft()));
	} else {
		map.addMapTimer(180);
	}
	ms.useItem(2360002);//HOORAY <3
}  