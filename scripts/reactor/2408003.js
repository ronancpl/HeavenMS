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
 *2408003.js
 *Horntail Head spawn
 *@author Even
*/
//240060000 for 8810000
//240060100 for 8810001

function touch() {
	if (rm.getPlayer().getEventInstance() != null) {
		rm.getPlayer().getEventInstance().setProperty("summoned", "true");
		rm.getPlayer().getEventInstance().setProperty("canEnter", "false");
	}
    rm.spawnFakeMonster(8800000);
    rm.mapMessage(6, "A gigantic creature is approaching from the deep cave.");	
    //rm.createMapMonitor(rm.getPlayer().getMap().getId(),"ps00");
	switch (rm.getPlayer().getMap().getId()) {
		case 240060000: 
			rm.spawnMonster(8810000, 960, 0); 
		break;
		case 240060100:
			rm.spawnMonster(8810001, 0, 0); //needs correct positions
		break;
	}
}
	
	