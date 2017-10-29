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
	Author : kevintjuh93
*/
var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
	if(type == 1 && mode == 0) {
		qm.sendNext("I'm sure it will come in handy during your journey. Please, don't decline my offer.");
		qm.dispose();
		return;
 	}else{
		qm.dispose();
		return;
	}
    }

    if (status == 0) 
	qm.sendSimple("Ah, you're the hero. I've been dying to meet you. \r\n#b#L0#(Seems a bit shy...)#l");		
    else if (status == 1) {
	qm.sendAcceptDecline("I have something I've been wanting to give you as a gift for a very long time... I know you're busy, especially since you're on your way to town, but will you accept my gift?");
	} else if (status == 2) {
	qm.forceStartQuest();
	qm.sendNext("The parts of the gift have been packed inside a box nearby. Sorry to trouble you, but could you break the box and bring me a #b#t4032309##k and some #b#t4032310##k? I'll assemble them for you right away.", 9);
    } else if (status == 3) { 
	qm.guideHint(18); 
	qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0) {
			qm.sendNext("What? You don't want the potion?");
            qm.dispose();
            return;
        }else{
            qm.dispose();
            return;
        }
    }
    if (status == 0)
	qm.sendYesNo("Ah, you've brought all the components. Give me a few seconds to assemble them.. Like this.. And like that.. and...\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v3010062# 1 #t3010062#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 95 exp");
    else if (status == 1) {   
        if(qm.isQuestCompleted(21013)) {
            qm.dropMessage(1,"Unknown Error");	
		}	
            qm.forceCompleteQuest();		
            qm.gainExp(95);
			qm.gainItem(4032309, -1);
			qm.gainItem(4032310, -1);
            qm.gainItem(3010062, 1);
	    qm.sendNextPrev("Here, a fully-assembled chair, just for you! I've always wanted to give you a chair as a gift, because I know a hero can occasionally use some good rest. Tee hee.", 9);	
    } else if (status == 2) { 
	qm.sendNext("A hero is not invincible. A hero is human. I'm sure you will face challenges and even falter at times. But you are a hero because you have what it takes to overcome any obstacles you may encounter.", 9);
    } else if (status == 3) { 
	qm.guideHint(19);
	qm.dispose();
    }
}