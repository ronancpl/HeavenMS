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
/* Author: Xterminator <Modified by XxOsirisxX>
	NPC Name: 		Roger
	Map(s): 		Maple Road : Lower level of the Training Camp (2)
	Description: 		Quest - Roger's Apple
*/
importPackage(Packages.client);

var status = -1;

function start(mode, type, selection) {
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
        
        if (status == 0)
            qm.sendNext("Hey, Man~ What's up? Haha! I am Roger who can teach you adorable new Maplers lots of information.");
        else if (status == 1)
            qm.sendNextPrev("You are asking who made me do this? Ahahahaha!\r\nMyself! I wanted to do this and just be kind to you new travellers.");
        else if (status == 2)
            qm.sendAcceptDecline("So..... Let me just do this for fun! Abaracadabra~!");
        else if (status == 3) {
            if (qm.getPlayer().getHp() >= 50) {
                qm.getPlayer().updateHp(25);
            }
            
            if (!qm.haveItem(2010007)) {
                qm.gainItem(2010007, 1);
            }
            
            qm.forceStartQuest();
            qm.sendNext("Surprised? If HP becomes 0, then you are in trouble. Now, I will give you #rRoger's Apple#k. Please take it. You will feel stronger. Open the Item window and double click to consume. Hey, it's very simple to open the Item window. Just press #bI#k on your keyboard.");
        } else if (status == 4) {
            qm.sendNextPrev("Please take all Roger's Apples that I gave you. You will be able to see the HP bar increasing. Please talk to me again when you recover your HP 100%.");
        } else if (status == 5) {
            qm.showInfo("UI/tutorial.img/28");
            qm.dispose();
        }
    }
}

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
        
        if (status == 0)
            if (qm.c.getPlayer().getHp() < 50) {
                qm.sendNext("Hey, your HP is not fully recovered yet. Did you take all the Roger's Apple that I gave you? Are you sure?");
                qm.dispose();
            } else
                qm.sendNext("How easy is it to consume the item? Simple, right? You can set a #bhotkey#k on the right bottom slot. Haha you didn't know that! right? Oh, and if you are a beginner, HP will automatically recover itself as time goes by. Well it takes time but this is one of the strategies for the beginners.");
        else if (status == 1)
            qm.sendNextPrev("Alright! Now that you have learned alot, I will give you a present. This is a must for your travel in Maple World, so thank me! Please use this under emergency cases!");
        else if (status == 2)
            qm.sendNextPrev("Okay, this is all I can teach you. I know it's sad but it is time to say good bye. Well take care if yourself and Good luck my friend!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2010000# 3 #t2010000#\r\n#v2010009# 3 #t2010009#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 10 exp");
        else if (status == 3) {
            if(qm.isQuestCompleted(1021))
                qm.dropMessage(1,"Unknown Error");
            else if(qm.canHold(2010000) && qm.canHold(2010009)){
                qm.gainExp(10);
                qm.gainItem(2010000, 3);
                qm.gainItem(2010009, 3);
                qm.forceCompleteQuest();
            }else
                qm.dropMessage(1,"Your inventory is full");
            qm.dispose();
        }
    }
}