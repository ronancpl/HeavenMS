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
	Author : 		Ronan
	NPC Name: 	        Yulete
	Map(s): 		Magatia
	Description: 		Quest - Yulete's Reward
	Quest ID: 		3382
*/

function end(mode, type, selection) {
        var itemid;
        if(qm.getQuestStatus(3302) == 2) {
            itemid = 4001159;
        } else if(qm.getQuestStatus(3304) == 2) {
            itemid = 4001160;
        } else {
            qm.sendNext("You must join one of the Magatia's factions before claiming a prize.");
            qm.dispose();
            return;
        }
    
        if(qm.haveItem(itemid, 25) && qm.getPlayer().getItemQuantity(1122010, true) == 0) {
            if(qm.canHold(1122010)) {
                qm.gainItem(itemid, -25);
                qm.gainItem(1122010, 1);
                
                qm.sendOk("Thank you for retrieving the marbles. Accept this pendant as a token of my appreciation.");
            } else {
                qm.sendNext("Free a slot on your EQUIP tab before claiming a prize.");
                qm.dispose();
                return;
            }
        } else if(qm.haveItem(itemid, 10)) {
            if(qm.canHold(2041212)) {
                qm.gainItem(itemid, -10);
                qm.gainItem(2041212, 1);
                
                qm.sendOk("Thank you for retrieving the marbles. This rock, that I am giving to you, can be used to improve the stats on the #b#t1122010##k. Take it as a token of my appreciation and use it wisely.");
            } else {
                qm.sendNext("Free a slot on your USE tab before claiming a prize.");
                qm.dispose();
                return;
            }
        } else {
            qm.sendNext("I need at least #b10 #t" + itemid + "##k to reward you appropriately. If you happen to come with #b25 of these#k instead, I can reward you with a valuable gear. Fare well.");
            qm.dispose();
            return;
        }

	qm.forceCompleteQuest();
	qm.dispose();
}
