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
 * @Name         Tangyoon
 * @Author       xXOsirisXx (BubblesDev)
 */

var status;

function start() {
	status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection){
	if (mode == -1 || !cm.isQuestStarted(2180)){
		cm.dispose();
		return;
	}
	else{
		if (mode == 1) status++;
		else status --;

		if (status == 0){
			cm.sendNext("Okay, I'll now send you to the stable where my cows are. Watch out for the calves that drink all the milk. You don't want your effort to go to waste.");
		}
		else if (status == 1){
			cm.sendNextPrev("It won't be easy to tell at a glance between a calf and a cow. Those calves may only be a month or two old, but they have already grown to the size of their mother. They even look alike...even I get confused at times! Good luck!");
		}
		else if (status == 2){
			if (cm.canHold(4031847)){
				cm.gainItem(4031847, 1);
				cm.warp(912000100, 0);
			}
			else{
				cm.sendOk("I can't give you the empty bottle because your inventory is full. Please make some room in your Etc window.");
			}
			cm.dispose();
		}
	}
}