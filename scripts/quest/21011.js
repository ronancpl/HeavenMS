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
		qm.sendOk("Ah, okay. I understand. Heroes are very busy. *Sniff...* If you ever get any free time, though...");
		qm.dispose();
		return;
 	}else{
		qm.dispose();
		return;
	}
    }

    if (status == 0) 
	qm.sendNext("Wait, are you... No way.... Are you the hero that #p1201000# has been talking about all this time?! #p1201000#! Don't just nod... Tell me! Is this the hero you've been waiting for?! ")		
    else if (status == 1) {
	qm.sendNextPrev("   #i4001171#");
    } else if (status == 2) {
	qm.sendNextPrev("I'm sorry. I'm just so overcome with emotions... *Sniff sniff* My goodness, I'm starting to tear up. You must be so happy, #p1201000#.");
    } else if (status == 3) {
	qm.sendAcceptDecline("Wait a minute... You're not carrying any weapons. From what I've heard, each of the heroes had a special weapon. Oh, you must have lost it during the battle against the Black Mage.");
    } else if (status == 4) {
	qm.forceStartQuest();
	qm.sendOk("My brother #bPuir #kis just down the street, and he's been dying to meet you! I know you're busy, but could you please stop by and say hello to Puir? Please...");
	qm.dispose();
	}
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0) {
	    qm.sendNext("*sniff sniff* Isn't this sword good enough for you, just for now? I'd be so honored...");
	    qm.dispose();
        }else{
            qm.dispose();
            return;
        }  
	}		
    if (status == 0) 
	qm.sendNext("Wait, are you... No way... Are you the hero that Lilin has been talking about all this time?! Lilin! Don't just nod... Tell me! Is this the hero you've been waiting for?!");
    else if (status == 1) {   	
    qm.sendNextPrev("#i4001171#");
    } else if (status == 2) { 
	qm.sendNextPrev("I'm sorry. I'm just so overcome with emotions... *Sniff sniff* My goodness, I'm starting to tear up. You must be so happy, Lilin.");
    } else if (status == 3) { 
	qm.sendNextPrev("Wait a minute... You're not carrying any weapons. From what I've hear, each of the heroes had a special weapon. Oh, you must have lost it during the battle against the Black Mage.");    
    } else if (status == 4) {  
	qm.sendYesNo("This isn't good enough to replace your weapon, but #bcarry this sword with you for now#k. It's my gift to you. A hero can't be walking around empty-handed.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v1302000# 1 #t1302000#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 35 exp");	
    } else if (status == 5) {
        if(qm.isQuestCompleted(21011))
            qm.dropMessage(1,"Unknown Error");
        else if(qm.canHold(1302000)){	
		qm.gainItem(1302000, 1);
		qm.gainExp(35);
		qm.forceCompleteQuest();
		qm.sendNext("#b(Your skills are nowhere close to being hero-like... But a sword? Have you ever even held a sword in your lifetime? You can't remember... How do you even equip it?)", 3);
	}else
		qm.dropMessage(1,"Your inventory is full");   
	} else if (status == 6) {
		qm.guideHint(16); 
		qm.dispose();
    }
}