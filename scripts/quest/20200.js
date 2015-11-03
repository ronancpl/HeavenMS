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
/* Author: Xterminator 
	NPC Name: 		Neinheart
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - The End of Knight-in-Training
*/
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            qm.sendNext("Hmmm... do you feel like you still have missions to take care of as a trainee? I commend your level of patience, but this has gone too far. Cygnus Knights is in dire need of new, more powerful knights.");
            qm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            qm.sendAcceptDecline("#h0#? Wow, your level has sky-rocketed since the last time I saw you. You also look like you've taken care of a number of missions as well... You seem much more ready to move on now than the last time I saw you. What do you think? Are you interested in taking the #bKnighthood Exam#k? It's time for you to grow out of the Knight-in-Training and become a bonafide Knight, right?");
        } else if (status == 1) {
            qm.startQuest();
            qm.completeQuest();
            qm.sendOk("If you wish to take the Knighthood Exam, please come to Ereve. Each Chief Knight will test your abilities, and if you meet their standards, then you will officially become a Knight.");
            qm.dispose();
        }
    }
}