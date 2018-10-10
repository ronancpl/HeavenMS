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

status = -1;
var job;
var sel;
actionx = {"Mental" : false, "Physical" : false};

function start() {
    if(cm.isQuestStarted(6192)) {
        if(cm.getParty() == null) {
            cm.sendOk("Form a party to start this instance.");
            cm.dispose();
            return;
        }
        
        var em = cm.getEventManager("ElnathPQ");
        if(em == null) {
            cm.sendOk("The El Nath PQ has encountered an error.");
            cm.dispose();
            return;
        }
        
        var eli = em.getEligibleParty(cm.getParty());
        if(eli.size() > 0) {
            if(!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                cm.sendOk("Another party is already challenging this instance. Please try another channel, or wait for the current party to finish.");
            }
        }
        else {
            cm.sendOk("You cannot start this instance yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. If you're having trouble finding party members, try Party Search.");
        }
            
        cm.dispose();
        return;
    }
    
    var jobBase = parseInt(cm.getJobId() / 100);
    var jobStyle = 1;
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
    cm.sendSimple("Can I help you?#b" + (cm.getJobId() % 10 == 0 ? "\r\n#L0#I want to make the 3th job advancement." : "") + "\r\n#L1#Please allow me to do the Zakum Dungeon Quest.");
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
            cm.sendYesNo("Okay! Now, you'll be transformed into a much more powerful warrior through me. Before doing that, though, please make sure your SP has been thoroughly used, You'll need to use up at least all of SP's gained until level 70 to make the 3rd job advancement. Oh, and since you have already chosen your path of the occupation by the 2nd job adv., you won't have to choose again for the 3rd job adv. Do you want to do it right now?");
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

            if(Math.floor(cm.getJobId() / 10) == 11) cm.sendNext("You have just become the #bCrusader#k. A number of new attacking skills such as #bShout#k and #bCombo Attack#k are devastating, while #bArmor Crash#k will put a dent on the monsters' defensive abilities. It'll be best to concentrate on acquiring skills with the weapon you mastered during the days as a Fighter.");
            else if(Math.floor(cm.getJobId() / 10) == 12) cm.sendNext("You have just become the #bWhite Knight#k. You'll be introduced to a new skill book featuring various new attacking skills as well as element-based attacks. It's recommended that the type of weapon complementary to the Page, whether it be a sword or a blunt weapon, should be continued as the White Knight. There's a skill called #bCharge#k, which adds an element of ice, fire and lightning to the weapon, making White Knight the only warrior that can perform element-based attacks. Charge up your weapon with an element that weakens the monster, and then apply massive damage with the #bCharged Blow#k. This will definitely make you a devastating force around here.");
            else cm.sendNext("You're #bDragon Knight#k from here on out. You'll be introduced to a range of new attacking skills for spears and pole arms, and whatever weapon was chosen as the Spearman should be continued as the Dragon Knigth. Skills such as #bCrusher#k (maximum damage to one monster) and #bDragon Fury#k (damage to multiple monsters) are recommended as main attacking skills of choice, while a skill called #bDragon Roar#k will damage everything on screen with devasting force. The downside is the fact that the skill uses up over half of the available HP.");
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
        cm.sendNext("Go, talk with #b#p1022000##k and bring me #b#t4031057##k.");
        cm.dispose();
    } else if (cm.getPlayer().gotPartyQuestItem("JBQ") && selection == 0){
        cm.sendNext("Go, talk with #b#p2030006##k and bring me #b#t4031058##k.");
        cm.dispose();
    } else {
        if (sel == undefined)
            sel = selection;
        if (sel == 0){
            if (cm.getPlayer().getLevel() >= 70 && cm.getJobId() % 10 == 0){
                if (status == 0)
                    cm.sendYesNo("Welcome. I'm #b#p2020008##k, the chief of all warriors, in charge of bringing out the best in each and every warrior that needs my guidance. You seem like the kind of warrior that wants to make the leap forward, the one ready to take on the challenges of the 3th job advancement. But I've seen countless warriors eager to make the jump just like you, only to see them fail. What about you? Are you ready to be tested and make the 3th job advancement?");
                else if (status == 1){
                    cm.getPlayer().setPartyQuestItemObtained("JB3");
                    cm.sendNext("Good. You will be tested on two important aspects of the warrior: strength and wisdom. I'll now explain to you the physical half of the test. Remember #b#p1022000##k from Perion? Go see him, and he'll give you the details on the first half of the test. Please complete the mission, and get #b#t4031057##k from #p1022000#.");
                } else if (status == 2)
                    cm.sendNextPrev("The mental half of the test can only start after you pass the physical part of the test. #b#t4031057##k will be the proof that you have indeed passed the test. I'll let #b#p1022000##k in advance that you're making your way there, so get ready. It won't be easy, but I have the utmost faith in you. Good luck.");
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