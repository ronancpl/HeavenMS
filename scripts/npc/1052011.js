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
/* Exit
	Warp NPC to Subway Ticketing Booth (103000100)
	located in B1 <Area 1> (103000900)
	located in B1 <Area 2> (103000901)
	located in B2 <Area 1> (103000903)
	located in B2 <Area 2> (103000904)
	located in B3 <Area 1> (103000906)
	located in B3 <Area 2> (103000907)
	located in B3 <Area 3> (103000908)
*/

var status = 0;

function start() {
    cm.sendNext("This device is connected to outside.");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        cm.sendOk("Alright, see you next time.");
        cm.dispose();
    }
    else {
        status++;
        if (status == 1)
            cm.sendNextPrev("Are you going to give up and leave this place?")
        else if (status == 2)
            cm.sendYesNo("You'll have to start from scratch the next time you come in...");
        else if (status == 3){
            cm.warp(103000100, 0);
            cm.dispose();
        }
    }
}