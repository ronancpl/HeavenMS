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
/**
	Ames the Wise
-- By ---------------------------------------------------------------------------------------------
	Xelkin
-- Edited by --------------------------------------------------------------------------------------
	Angel (get31720 ragezone
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3 (BENG)] & [XotiCraze]
-- Fixed Dispose ----------------------------------------------------------------------------------
        Fixed by Moogra
---------------------------------------------------------------------------------------------------
**/
var status = -1;

function start() {
    var rings = new Array(1112806, 1112803, 1112807, 1112809);
    var hasRing = false;
    for (var x = 0; x < rings.length && !hasRing; x++)
        if (cm.haveItem(rings[x])) {
            hasRing = true;
            break;
        }
    if (hasRing)
        cm.sendNext("You've reached the end of the wedding. You will recieve an Onyx Chest for Bride and Groom and an Onyx Chest. Exchange them at Pila, she is at the top of Amoria.");
    else if (cm.haveItem(4000313)) {
        cm.sendNext("Wow the end of the wedding already ? Good bye then.!");
        status = 20;
    } else {
        cm.sendNext("You do not have the Gold Maple Leaf and you do not have a wedding ring so I will take you to Amoria.");
        status = 21;
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.sendOk("Goodbye then");
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            cm.gainItem(4031424,1);
            cm.gainItem(4031423,1);
            cm.dispose();
        } else if (status == 21) {
            cm.gainItem(4000313,-1);
            cm.gainItem(4031423,1);
        }
        cm.warp(680000000);
        cm.dispose();
    }
}
