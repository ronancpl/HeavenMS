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

var toMap = new Array(550000000, 551000000, 540000000,540000000);
var inMap = new Array(540000000, 540000000, 551000000, 550000000);
var cost = new Array(10000, 50000, 50000, 10000);
var location;
var text = "Where would you like to travel?\n\n";
var status = 0;

function start() {
	if (cm.getPlayer().getMap().getId() != 540000000) {
		for (var i = 0; i < toMap.length; i ++) {
			if (inMap[i] == cm.getPlayer().getMap().getId()) {
				location = i;
				break;
			}
		}
		text +="\t\r\n#b#L0##m" + toMap[location] + "# (" + cost[location] + "mesos)#l#k";
	} else {
    	text += "\t\r\n#b#L0##m" + toMap[0] + "# (" + cost[0] + "mesos)#l\n\t\r\n#L1##m" + toMap[1] + "# (" + cost[1] + "mesos)#l#k";
	}
    cm.sendSimple(text);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    } else if (mode == 0) {
    	cm.sendNext("You know where to come if you need a ride!");
        cm.dispose();
        return;
    } else {
        status++;
     }
    if (status == 1) {
        if (cm.getPlayer().getMap().getId() == 540000000) {
            location = selection;
        }
        if (toMap[location] == null) {
            cm.dipose();
            return;
        }
        cm.sendYesNo("Would you like to travel to #b#m"+toMap[location]+"##k? To head over to #b#m"+toMap[location]+"##k, it'll cost you cost you #b" + cost[location] + "#k. Would you like to go right now?");
    } else if (status == 2) {
        if (cm.getMeso() < cost[location]) {
            cm.sendNext("You do not seem to have enough mesos.");
        } else {
            cm.warp(toMap[location]);
            cm.gainMeso(-cost[location]);
        }
        cm.dispose();
    }
}
