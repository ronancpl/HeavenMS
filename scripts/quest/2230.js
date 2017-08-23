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
	NPC Name: 		Mar the Fairy
	Map(s): 		Everywhere
	Description: 		Quest - A Mysterious Small Egg
	Quest ID: 		2230
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
            qm.sendNext("I put this small, precious life in your hands...Guard it with your life...");
        else if (status == 1)
            qm.sendYesNo("Looking after another life...That is the inevitable mission given to you...Follow the force that leads you to me.");
        else if (status == 2) {
            qm.sendOk("Put your hand in your pocket. I think your friend has already found you.\r\nThe purple bellflower that soaks in the sun in between the skyscraping trees...Follow the path to the unknown that leads you to the bellflower. I will wait for you here.");
            qm.forceStartQuest();
            qm.gainItem(4032086, 1); // Mysterious Egg * 1
        }
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0)
            qm.sendSimple("Hello, traveler... You have finally come to see me. Have you fulfilled your duties? \r\n #b#L0#What duties? Who are you?#l#k");
        else if (selection == 0 && status == 1) {
            qm.sendNext("Have you found a small egg in your pocket? That egg is your duty, your responsibility. Life is hard when you're all by yourself. In times like this, there's nothing quite like having a friend that will be there for you at all times. Have you heard of a #bpet#k?\r\nPeople raise pets to ease the burden, sorrow, and loneliness, because knowing that you have someone, or something in this matter, on your side will really bring a peace of mind. But everything has consequences, and with it comes responsibility...");
        } else if (status == 2) {
            qm.sendNextPrev("Raising a pet requires a huge amount of responsibility. Remember a pet is a form of life, as well, so you'll need to feed it, name it, share your thoughts with it, and ultimately form a bond. That's how the owners get attached to these pets.");
        } else if (status == 3) {
            qm.sendNextPrev("I wanted to instill this in you, and that's why I sent you a baby that I cherish. The egg you have brought is #bRune Snail#k, a creature that is born through the power of Mana. Since you took great care of it as you brought the egg here, the egg will hatch soon.");
        } else if (status == 4) {
            qm.sendNextPrev("Rune Snail is a pet of many skills. It'll pick up items, feed you with potions, and do other things that will astound you. The downside is that since Rune Snail was born out of power of Mana, it's lifespan is very short. Once it turns into a doll, it'll never be able to be revived.");
        } else if (status == 5) {
            qm.sendYesNo("Now do you understand? Every action comes with consequences, and pets are no exception. The egg of the snail shall hatch soon.");
        } else if (status == 6) {
            qm.gainItem(5000054, 1, false, true, 5 * 60 * 60 * 1000);  // rune snail (5hrs)
            
            qm.gainItem(4032086, -1); // Mysterious Egg * -1
            qm.forceCompleteQuest();
            qm.sendNext("This snail will only be alive for #b5 hours#k. Shower it with love. Your love will be reciprocated in the end.");
            qm.dispose();
        }
    }
}