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
//Moose, Warps to exit

var status;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode <= 0) {
	cm.dispose();
        return;
    }
    
    status++;
    if(status == 0) {
        if(cm.isQuestStarted(6180)) {
            cm.sendYesNo("Pay attention: during the time you stay inside the training ground make sure you #bhave equipped your #t1092041##k, it is of the utmost importance. Are you ready to proceed to the training area?");
        }
        
        else {
            cm.sendOk("Only assigned personnel can access the training ground.");
            cm.dispose();
        }
    }
    
    else if(status == 1) {
	cm.warp(924000001, 0);
	cm.sendOk("Have your shield equipped until the end of the quest, or else you will need to start all over again!");
        
        cm.resetQuestProgress(6180,9300096);
        cm.dispose();
    }
}
