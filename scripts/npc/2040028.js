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

/* Mark the Toy Soldier
*/

var greeting;
var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        
        if(status == 0) {
            var greeting = "Thank you for finding the pendulum. Are you ready to return to Eos Tower?";
            if (cm.isQuestStarted(3230)) {
                if (cm.haveItem(4031094)) {
                    cm.completeQuest(3230);
                    cm.gainItem(4031094, -1);
                } else
                    greeting = "You haven't found the pendulum yet. Do you want to go back to Eos Tower?";
            }
            cm.sendYesNo(greeting);
        } else if(status == 1) {
            cm.warp(221024400, 4);
            cm.dispose();
        }
    }
}