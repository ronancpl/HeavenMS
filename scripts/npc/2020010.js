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

//** Archer 3rd Job Instructor Rene

status = -1;
var job;
var sel;
actionx = {"Mental" : false, "Physical" : false};

function start() {
    var jobBase = parseInt(cm.getJobId() / 100);
    var jobStyle = 3;
    if (!(cm.getPlayer().getLevel() >= 70 && jobBase == jobStyle && cm.getJobId() % 10 == 0)){
        if(cm.getPlayer().getLevel() >= 50 && jobBase % 10 == jobStyle) {
            status++;
            action(1, 0, 1);
            return;
        }
        
        cm.sendNext("Hi there.");
        cm.dispose();
        return;
    }
    if (cm.haveItem(4031058))
	    actionx["Mental"] = true;
	else if (cm.haveItem(4031057))
	    actionx["Physical"] = true;
    cm.sendSimple("Anything you want from me?#b" + (cm.getJobId() % 10 == 0 ? "\r\n#L0#I want to make the 3th job advancement." : "") + "\r\n#L1#Please allow me to do the Zakum Dungeon Quest.");
}

function action(mode, type, selection){
    status++;
	if (mode == 0 && type == 0) {
	    status -= 2;
	} else if(mode != 1 || (status > 2 && !actionx["Mental"]) || status > 3){
	    if (mode == 0 && type == 1)
		    cm.sendNext("Make up your mind.");
	    cm.dispose();
		return;
	}
	if (actionx["Mental"]){
	    if (status == 0)
		    cm.sendNext("Great job completing the mental part of the test. You have wisely answered all the questions correctly. I must say, I am quite impressed with the level of wisdom you have displayed there. Please hand me the necklace first, before we takeon the next step.");
		else if (status == 1)
			cm.sendYesNo("Okay! Now, you'll be transformed into a much more powerful adventurer through me. Before doing that, though, please make sure your SP has been thoroughly used, You'll need to use up at least all of SP's gained until level 70 to make the 3rd job advancement. Oh, and since you have already chosen your path of the occupation by the 2nd job adv., you won't have to choose again for the 3rd job adv. Do you want to do it right now?");
		else if (status == 2) {
		    if (cm.getPlayer().getRemainingSp() > 0)
			    if (cm.getPlayer().getRemainingSp() > (cm.getLevel() - 70) * 3) {
				    cm.sendNext("Please, use all your SP before continuing.");
					cm.dispose();
					return;
				}
		    if (cm.getJobId() % 10 == 0) {
		        cm.gainItem(4031058, -1);
		        cm.changeJobById(cm.getJobId() + 1);
				cm.getPlayer().removePartyQuestItem("JBQ");
			}
                        
			if(Math.floor(cm.getJobId() / 10) == 31) cm.sendNext("You have officially become the #bRanger#k. One of the skills that you'll truly embrace is a skill called #bMortal Blow#k that allows Rangers to fire arrows from close-range. #bInferno#k allows Rangers to temporarily perform fire-based attacks on monsters, while skills like #bPuppet#k (summons a scarecrow which attracts the monsters' attention) and #bSilver Hawk#k (summons a Silver Hawk that attacks monsters) solidify the Bowman's status as a long-range attack extraordinaire.");
                        else cm.sendNext("You have officially become the #bSniper#k. One of the skills that you'll truly embrace is a skill called #bMortal Blow#k that allows Snipers to fire arrows from close-range. #bBlizzard#k allows Snipers to temporarily perform ice-based attacks on monsters, while skills like #bPuppet#k (summons a scarecrow which attracts the monsters' attention) and #bGolden Eagle#k (summons a Golden Eagle that attacks monsters) solidify the Bowman's status as a long-range attack extraordinaire.");
		} else if (status == 3) {
		    cm.sendNextPrev("I've also given you some SP and AP, which will help you get started. You have now become a powerful, powerful warrior, indeed. Remember, though, that the real world will be awaiting your arrival with even tougher obstacles to overcome. Once you feel like you cannot train yourself to reach a higher place, then, and only then, come see me. I'll be here waiting.");
		}
	}else if (actionx["Physical"]){
	    if (status == 0)
	        cm.sendNext("Great job completing the physical part of the test. I knew you could do it. Now that you have passed the first half of the test, here's the second half. Please give me the necklace first.");
		else if (status == 1){
		    if (cm.haveItem(4031057)){
		        cm.gainItem(4031057, -1);
				cm.getPlayer().setPartyQuestItemObtained("JBQ");
			}
			cm.sendNextPrev("Here's the 2nd half of the test. This test will determine whether you are smart enough to take the next step towards greatness. There is a dark, snow-covered area called the Holy Ground at the snowfield in Ossyria, where even the monsters can't reach. On the center of the area lies a huge stone called the Holy Stone. You'll need to offer a special item as the sacrifice, then the Holy Stone will test your wisdom right there on the spot.");
		} else if (status == 2)
		    cm.sendNextPrev("You'll need to answer each and every question given to you with honesty and conviction. If you correctly answer all the questions, then the Holy Stone will formally accept you and hand you #b#t4031058##k. Bring back the necklace, and I will help you to the next step forward. Good luck.");
	} else if (cm.getPlayer().gotPartyQuestItem("JB3") && selection == 0){
	    cm.sendNext("Go, talk with #b#p1012100##k and bring me #b#t4031057##k.");
		cm.dispose();
	} else if (cm.getPlayer().gotPartyQuestItem("JBQ") && selection == 0){
	    cm.sendNext("Go, talk with #b#p2030006##k and bring me #b#t4031058##k.");
		cm.dispose();
	} else {
	    if (sel == undefined)
		    sel = selection;
	    if (sel == 0){
	        if (cm.getPlayer().getLevel() >= 70 && cm.getJobId() % 10 == 0) {
	            if (status == 0)
	                cm.sendYesNo("Welcome. I'm #b#p2020010##k, the chief of all archers, ready to share my bow knowledge and hard knock life to those willing to listen. You seem ready to make the leap forward, the one ready to take on the challenges of the 3rd job advancement. Too many archers have come and gone, unable to meet the standards of achieving the 3rd job advancement. What about you? Are you ready to be tested and make the 3th job advancement?");
	            else if (status == 1){
		            cm.getPlayer().setPartyQuestItemObtained("JB3");
	                cm.sendNext("Good. You will be tested on two important aspects of the archer: strength and wisdom. I'll now explain to you the physical half of the test. Remember #b#p1012100##k from Henesys? Go see him, and he'll give you the details on the first half of the test. Please complete the mission, and get #b#t4031057##k from #p1012100#.");
	            } else if (status == 2)
	                cm.sendNextPrev("The mental half of the test can only start after you pass the physical part of the test. #b#t4031057##k will be the proof that you have indeed passed the test. I'll let #b#p1012100##k in advance that you're making your way there, so get ready. It won't be easy, but I have the utmost faith in you. Good luck.");
			}
            } else {
            	if (cm.getPlayer().getLevel() >= 50){
            		cm.sendNext("Ok, go.");
                	if(!cm.isQuestStarted(100200)) cm.startQuest(100200);
                        if(Packages.constants.ServerConstants.USE_ENABLE_SOLO_EXPEDITIONS && !cm.isQuestCompleted(100201)) cm.completeQuest(100201);
            	}else
                	cm.sendNext("You're weak.");
            	cm.dispose();
            }
	}
}