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
	Pila Present
-- By ---------------------------------------------------------------------------------------------
	Angel (get31720 ragezone)
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3] & [XotiCraze]
        Improved by [RonanLana]
---------------------------------------------------------------------------------------------------
**/

var bgPrizes = [[2022179,10], [2022282,10], [2210005,5], [2210003,5]];
var cmPrizes = [[2022011,10], [2000005,50], [2022273,10], [2022179,3]];

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) { 
    if (mode == -1 || mode == 0) {
        cm.sendOk("Goodbye then."); 
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }
		
    if (status == 0) {
        var msg = "Hello I exchange Onyx Chest for Bride and Groom and the Onyx Chest for prizes!#b";
        var choice1 = new Array("I have an Onyx Chest for Bride and Groom", "I have an Onyx Chest");
        for (var i = 0; i < choice1.length; i++) {
            msg += "\r\n#L" + i + "#" + choice1[i] + "#l";
        }
        cm.sendSimple(msg);
    } else if (status == 1) {
        if (selection == 0) {
            if (cm.haveItem(4031424)) {
                if (cm.isMarried()) {
                    if(cm.getInventory(2).getNextFreeSlot() >= 0) {
                        var rand = Math.floor(Math.random() * bgPrizes.length);
                        cm.gainItem(bgPrizes[rand][0], bgPrizes[rand][1]);

                        cm.gainItem(4031424,-1);
                        cm.dispose();
                    } else {
                        cm.sendOk("You don't have a free USE slot right now.");
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("You must be married to claim the prize for this box.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("You don't have an Onyx Chest for Bride and Groom.");
                cm.dispose();
            }
        } else if (selection == 1) {
            if (cm.haveItem(4031423)) {
                if(cm.getInventory(2).getNextFreeSlot() >= 0) {
                    var rand = Math.floor(Math.random() * cmPrizes.length);
                    cm.gainItem(cmPrizes[rand][0], cmPrizes[rand][1]);

                    cm.gainItem(4031423,-1);
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have a free USE slot right now.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("You don't have an Onyx Chest.");
                cm.dispose();
            }
        }
    }
} 
