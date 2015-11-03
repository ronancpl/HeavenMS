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
Moose
Warps to exit map etc.
*/

var status;
var exitMap = 221000300;
var exitPortal = "mid00";

function start() {
	status = -1
	action(1,0,0);
}

function action(mode, type, selection){
	if (mode <= 0 && status == 0)//I think I messed something up here, maybe.
		cm.dispose();
	else {
		if (mode == 1)
			status++;
		else
			status--;
		var mapId = cm.getPlayer().getMapId();
		if (mapId == exitMap) {
			if (status == 0) 
				cm.sendNext("See you next time.");
			else {
				cm.warp(103000000,"mid00");
				cm.dispose();
			}
		}
		else {
			var outText = "Would you like to leave, " +  cm.getPlayer().getName()  +  "? Once you leave the map, you'll have to restart the whole quest if you want to try it again, and Juudai will be sad.  Do you still want to leave this map?";
			if (status == 0)
				cm.sendYesNo(outText);
			else if (mode == 1) {
				var eim = cm.getPlayer().getEventInstance();
				if (eim == null)
					cm.warp(221000300,0);
				else if (cm.isLeader())
					eim.disbandParty();
				else
					eim.leftParty(cm.getPlayer());
				cm.dispose();
			} else
				cm.dispose();
		}
	}
}
