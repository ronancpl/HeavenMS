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
	Description: 		Quest - Veteran Hunter
	Quest ID : 		29400
*/

var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
			qm.sendNext("Come back when you're ready.");
		
		    qm.dispose();
		    return;
	}
		if (status == 0)
			qm.sendAcceptDecline("#v1142004# #e#b#t1142004##k\r\n\r - Time Limit 30 Days\r - Hunt 100,000 Monsters\r #n *Only monsters that are at your level or higher are approved.\r\nDo you want to test your skills to see if you're worthy of this title?");
		else if (status == 1) {
			qm.sendNext("Current Ranking \r\n1. #bMoople#k : #r538,673#k monsters\r\n2. #bZeroQuanta#k : #r111,421#k monsters\r\nDon't forget that the record resets at the beginning of each month.");//TODO
	        } else if (status == 2) {
			qm.sendNextPrev("I'll give you 30 days to reach your hunting goal. Once you are finished, come back and see me. Remember, you have to come back and see me within the time limit in order to be approved. Also, you are prohibited from trying out for another title unless you first complete or forfeit this challenge.");
		} else if (status == 3) {
			qm.forceStartQuest();
			qm.dispose();
		}
}


function end(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0) return;	    
		else{
		    qm.dispose();
			return;
		}		
		if (status == 0) {
			qm.sendOk("Not coded yet.");
			qm.dispose();
		}
	}
}