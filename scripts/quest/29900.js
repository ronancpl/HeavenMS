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
	Author : 		kevintjuh93
	Description: 		Quest - Beginner Adventurer
	Quest ID : 		29900
*/

var status = -1;

function start(mode, type, selection) {
		if (qm.forceStartQuest()) qm.showInfoText("You have earned the <Beginner Adventurer> title. You can receive a Medal from NPC Dalair.");
		qm.dispose();
}


function end(mode, type, selection) {
    status++;
    if (mode != 1) 
        qm.dispose();
    else {
        if (status == 0) 
            qm.sendNext("Congratulations on earning your honorable #b<Beginner Adventurer>#k title. I wish you the best of luck in your future endeavors! Keep up the good work.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n #v1142107:# #t1142107# 1");
        else if (status == 1) {
			if (qm.canHold(1142107)) {
				qm.gainItem(1142107);
				qm.forceCompleteQuest();
			} else 
				qm.sendNext("Please make room in your inventory");//NOT GMS LIKE
			
			qm.dispose();        
		}
    }
	
}