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
	Assistant Nancy
-- By ---------------------------------------------------------------------------------------------
	Angel (get31720 ragezone)
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3] & [XotiCraze]
---------------------------------------------------------------------------------------------------
**/

var status;
var i;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || mode == 0) {
        cm.sendOk("Goodbye then"); 
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }
	
    var engagementRings = Array(4031360, 4031358, 4031362, 4031364);
    var hasEngage = false;
    for (i = 0; i < engagementRings.length && !hasEngage; i++) {
        if (cm.haveItem(engagementRings[i]))
            hasEngage = true;
    }
    var Rings = Array(1112806, 1112803, 1112807, 1112809);
    var hasRing = false;
    for (i = 0; i < Rings.length; i++) {
        if (cm.haveItem(Rings[i])) {
            hasRing = true;
        }
    }

    if (status == 0) {
        if (cm.haveItem(4000313) && hasEngage) {
            cm.sendOk("You can't leave yet! You need to click High Priest John and get married before I can let you leave.");
            cm.dispose();
        } else if (cm.haveItem(4000313) && hasRing) {
            var choice = Array("Go to the Cherished Visage Photos", "What should I be doing");
            var msg = "What can I help you with?";
            for (i = 0; i < choice.length; i++) {
                msg += "\r\n#L" + i + "#" + choice[i] + "#l";
            }
            cm.sendSimple(msg);
        } else {
            cm.sendNext("You don't seem to have a Gold Maple Leaf, engagement ring, or wedding ring. You must not belong here, so I will take you to Amoria.");
            selection = 20; // Random.
        }
    } else if (status == 1) {
        switch(selection) {
            case 0:
                cm.warp(680000300);
                cm.sendOk("Enjoy! Cherish your Photos Forever!");
                cm.dispose();
                break;
            case 1:
                cm.sendOk("The Bride and Groom must click High Priest John to be wed. When you are ready you can click me to go to the Cherished Visage Photos");
                cm.dispose();
                break;
            default:
                cm.warp(680000000,0);
                cm.dispose();
                break;
        }
    }
}
