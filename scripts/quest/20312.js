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
/* Author: 		ThreeStep
	NPC Name: 		Oz (1101004)
	Description: 	Blaze Wizard 3rd job advancement
	Quest: 			Shinsoo's Teardrop
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            qm.sendNext("Come back when you are ready.");
            qm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            qm.sendNext("The jewel you brought back from the Master of Disguise is Shinsoo's Teardrop. It is the crystalization of Shinsoo's powers. If the Black Mage gets his hands on this, then this spells doom for all of us.");
        } else if (status == 1) {
            qm.sendYesNo("As a token of her appreciation for your effort in preventing potentially serious matter, the Empress has decided to present you with a new title. Are you ready to accept it?");
        } else if (status == 2) {
            nPSP = (qm.getPlayer().getLevel() - 70) * 3;
            if (qm.getPlayer().getRemainingSp() > nPSP) {
                qm.sendNext("You still have way too much #bSP#k with you. You can't earn a new title like that, I strongly urge you to use more SP on your 1st and 2nd level skills.");
                qm.dispose();
            } else {
                if (!qm.canHold(1142068)) {
                    qm.sendNext("If you wish to receive the medal befitting the title, you may want to make some room in your equipment inventory.");
                    qm.dispose();
                } else {
                    qm.gainItem(1142068, 1);
                    qm.getPlayer().changeJob(Packages.client.MapleJob.BLAZEWIZARD3);
                    qm.sendOk("#h #, as of this moment, you are an Advanced Knight. From this moment on, you will train yourself with fervor as you will lead your group of Knights for the safety of this world. That fervor will provide you with plenty of courage.");
                    qm.completeQuest();
                    qm.dispose();
                }
            }

        }
    }
}