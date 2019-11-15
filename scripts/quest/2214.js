/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
	NPC Name: 		Knocked Trash Can
	Map(s): 		Hut in the Swamp
	Description: 		Quest - The Run-down Huts in the Swamp
	Quest ID: 		2214
*/

var status = -1;
var canComplete;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            var hourDay = qm.getHourOfDay();
            if(!(hourDay >= 17 && hourDay < 20)) {
                qm.sendNext("(Hmm, I'm searching the trash can but can't find the #t4031894# JM was talking about, maybe it's not time yet...)");
                canComplete = false;
                return;
            }
            
            if(!qm.canHold(4031894, 1)) {
                qm.sendNext("(Eh, I can't hold the #t4031894# right now, I need an ETC slot available.)");
                canComplete = false;
                return;
            }
            
            canComplete = true;
            qm.sendNext("(Ah, there is a crumbled note here... Hm, it contains details about some scheme that is about to happen, that must be what #r#p1052002##k was talking about.)");
        } else if (status == 1) {
            if (canComplete) {
                qm.forceCompleteQuest();
                qm.gainItem(4031894, 1);
                qm.gainExp(20000);
            }
            
            qm.dispose();
        }
    }
}