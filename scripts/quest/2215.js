/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
	NPC Name: 		Find the Crumpled Piece of Paper Again
	Map(s): 		Hut in the Swamp
	Description: 		Quest - The Run-down Huts in the Swamp
	Quest ID: 		2215
*/

var status = -1;

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
                qm.dispose();
                return;
            }
            
            if(qm.getMeso() < 2000) {
                qm.sendNext("(Oh, I don't have the combined fee amount yet.)");
                qm.dispose();
                return;
            }
            
            if(!qm.canHold(4031894, 1)) {
                qm.sendNext("(Eh, I can't hold the #t4031894# right now, I need an ETC slot available.)");
                qm.dispose();
                return;
            }
            
            qm.sendNext("(Alright, now I will deposit the fee there and get the paper... That's it, yea, that's done.)");
            qm.gainItem(4031894, 1);
            qm.gainMeso(-2000);
            qm.forceCompleteQuest();
            
            qm.dispose();
        }
    }
}