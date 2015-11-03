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

var toMap = new Array(211040200, 220050300, 220000000,240030000);
var inMap = new Array(211000000, 220000000, 221000000, 240000000);
var cost = new Array(10000, 25000, 25000,55000);
var location;
var status = 0;

function start() {
	
	for (var i = 0; i < toMap.length; i ++) {
		if (inMap[i] == cm.getPlayer().getMap().getId()) {
			location = i;
			break;
		}
	}
    cm.sendNext("Hello there! This taxi will take you to dangerous places in Ossyria  faster than an arrow! We go from #m" + inMap[location] + "# to #b#m"+toMap[location]+"##k on this Ossyria Continent! It'll cost you #b"+ cost[location] +" meso#k. I know it's a bit expensive, but it's well worth passing all the dangerous areas!");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else if (mode == 0)
        cm.sendNext("Hmm, please think this over. It's not cheapr, but you will NOT be disappointed with our premier service!");
    else
        status++;
    if (status == 1)
        cm.sendYesNo("Would you like to pay meso#k to travel to the #b#m"+toMap[location]+"##k?");
    else if (status == 2) {
        if (cm.getMeso() < cost[location]) {
            cm.sendNext("You don't seem to have enough mesos. I am terribly sorry, but I cannot help you unless you pay up. Bring in the mesos by hunting more and come back when you have enough.");
        } else {
            cm.warp(toMap[location]);
            cm.gainMeso(-cost[location]);
        }
        cm.dispose();
    }
}
