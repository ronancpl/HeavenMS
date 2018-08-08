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
/* Author: PurpleMadness
 * The sorcerer who sells emotions
*/

importPackage(Packages.client);

var status = -1;

function start(mode, type, selection) {
        if(qm.getPlayer().getMeso() >= 1000000) {
            if(qm.canHold(2022337, 1)) {
                qm.gainItem(2022337, 1);
                qm.gainMeso(-1000000);
                
                //qm.sendOk("Nice doing business with you~~.");
                qm.startQuest(3514);
            } else {
                qm.sendOk("Check out for a slot on your USE inventory first.");
            }
        } else {
            qm.sendOk("Oy, you don't have the money. I charge #r1,000,000 mesos#k for the emotion potion. No money, no deal.");
        }
        
        qm.dispose();
}

function usedPotion(ch) {
        return ch.getBuffSource(MapleBuffStat.HPREC) == 2022337;
}

function end(mode, type, selection) {
        if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
    
        if(status == 0) {
                if(!usedPotion(qm.getPlayer())) {
                        if(qm.haveItem(2022337)) {
                                qm.sendOk("Are you scared to drink the potion? I can assure you it has only a minor #rside effect#k.");
                        } else {
                                if(qm.canHold(2022337)) {
                                        qm.gainItem(2022337, 1);
                                        qm.sendOk("Lost it? Luckily for you I managed to recover it back. Take it.");
                                } else {
                                        qm.sendOk("Lost it? Luckily for you I managed to recover it back. Make a room to get it.");
                                }
                        }
                        
                        qm.dispose();
                        return;
                } else {
                        qm.sendOk("It seems the potion worked and your emotions are no longer frozen. And, oh, my... You're ailing bad, #bpurge#k that out quickly.");
                }
        } else if(status == 1) {
                qm.gainExp(891500);
                qm.completeQuest(3514);
                qm.dispose();
        }
}