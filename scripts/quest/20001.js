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
	Author : 		Generic
	NPC Name: 		Neinheart
	Map(s): 		Ereve: Empress' Road
	Description: 		Quest - Neinheart the Tactician
	Quest ID: 		20001
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0)
            qm.sendNext("Hello, #h #. I formally welcome you to the Cygnus Knights. My name is Neinheart Von Rubistein, the Head Tactician for the young Empress. I will be seeing you often from here on out, so I suggest you remember my name. Haha...");
        else if (status == 1)
            qm.sendNextPrev("I understand that you didn't have enough time and exposure to figure out what you really need to do as a Cygnus Knight. I will eplain it to you in detail, one by one. I will explain where you are, who the young Empress is, and what our duties are...");
        else if (status == 2)
            qm.sendNextPrev("You're standing on an island called Ereve, the only land that's governed by the young Empress that also happens to float in the air. Yes, we're floating in the air as we speak. We stay here out of necessity, but it usually works as a ship that floats all over Maple World, for the sake of the young Empress...");
        else if (status == 3)
            qm.sendNextPrev("The young Empress is indeed the ruler of Maple World, the one and only governer of this world. What? You've never heard of such a thing? Ahhh, that's understandable. The young Empress may govern this world, but she's not a dictator that looms over everyone. She uses Ereve as a way for her to oversee the world as an observer without having to be too hands-on. That's how it usually is, anyway...");
        else if (status == 4)
            qm.sendNextPrev("But situations arise every now and then where she'll have to take control. The evil Black Mage has been showing signs of resurrection all over the world. The very king of destruction that threatened to destroy the world as we know it is trying to reappear into our lives.");
        else if (status == 5)
            qm.sendNextPrev("The problem is, no one is aware of it. It's been so long since the Black Mage disappeared, that people have become used to peace in the world, not necessarily knowing what to do if a crisis like this reaches. If this keeps up, our world will be in grave danger in no time.");
        else if (status == 6)
            qm.sendNextPrev("That's when the young Empress decided to step forward and take control of this potential crisis before it revealed itself. She decided to create a group of Knights that will prevent the Black Mage from being fully resurrected. I'm sure you know of what happens afterwards since you volunteered to become a Knight yourself.");
        else if (status == 7)
            qm.sendNextPrev("Our duties are simple. We need to make ourselves more powerful; much more powerful than the state we're in right now, so that when the Black Mage returns, we'll battle him and eliminate him once and for all before he puts the whole world in grave danger. That is our goal, our mission, and therefore yours as well");
        else if (status == 8)
            qm.sendAcceptDecline("This is the basic overview of this situation. Understood?");
        else if (status == 9) {
            if (qm.isQuestCompleted(20001)) {
                qm.gainExp(40);
                qm.gainItem(1052177, 1); // fancy noblesse robe
            }
            qm.forceStartQuest();
            qm.forceCompleteQuest();
            qm.sendNext("I'm glad you understand what I've told you but... did you know? Based on your current level, you won't be able to face the Black Mage. Heck you won't be able to face off his disciple's slave's monster's pet's dummy! Are you sure you are ready to protect Maple World like that?");
        } else if (status == 10)
            qm.sendNextPrev("You may be a member of the Cygnus Knights, but that doesn't mean you're a knight. Forget being the official knight. You're not even a Knight-in-Training, yet. A lot of time will pass where you will just sit around here, doing paperwork for the Cygnus Knights, but...");
        else if (status == 11)
            qm.sendNextPrev("But then again, no one is born strong, anyway. The Empress also prefers that she creates an enviroment where a string of powerful knights can be nurtured and created, as opposed to finding a supernaturally-gifted knight. For now, you'll have to become a Knight-in-Training, and make yourself much more powerful so you'll become useful later on. We'll talk about the duties of being a Cygnus Knight once you reach that level of competency.");
        else if (status == 12)
            qm.sendPrev("Take the portal on the left side and go straight, and you'll head towards #b Training Forest I # . There, you'll find the training instructor for the Knights, Kiku. The next time I see you, I'd like for you to be atleast at level 10.");
    }
}