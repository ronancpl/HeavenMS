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
var status = 0;
var fee = 15000;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode != 1) {
        if (mode == 0)
            cm.sendOk("Alright, see you next time.");
        cm.dispose();
    }
    else {
        status++;
        if (cm.getPlayer().getMapId() == 682000000) {
            if (status == 0) {
                if (selection == 0)
                    cm.sendYesNo("Would you like to return back to #bcivilization#k? The fee is " + fee + " mesos.");
            } else if (status == 1) {
                if(cm.getMeso() >= fee) {
                    cm.gainMeso(-fee);
                    cm.warp(600000000);
                } else {
                    cm.sendOk("Hey, what are you trying to pull on? You don't have enough meso to pay the fee.");
                }
                
                cm.dispose();
            }
        } else {
            if (status == 0) {
                cm.sendYesNo("Would you like to go to the #bHaunted Mansion#k? The fee is " + fee + " mesos.");
            } else if (status == 1) {
                if(cm.getMeso() >= fee) {
                    cm.gainMeso(-fee);
                    cm.warp(682000000, 0);
                } else {
                    cm.sendOk("Hey, what are you trying to pull on? You don't have enough meso to pay the fee.");
                }
                
                cm.dispose();
            }
        }
    }
}