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
	NPC Name: 		Oz
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - Knighthood Exam: Blaze Wizard
*/
var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            qm.sendNext("Eh? Why? Is there something wrong?");
            qm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            qm.sendYesNo("You managed to bring all of #t4032097#!!! Okay, I believe that your are now qualified to become an official knight! Do you want to become one?");
        } else if (status == 1) {
            if (qm.getPlayer().getJob().getId() == 1200 && qm.getPlayer().getRemainingSp() > ((qm.getPlayer().getLevel() - 30) * 3)) {
                qm.sendNext("Wa, wait... you have way too much #bSP#k with you. You'll need to spend more SP on 1st-level skills to become an official knight.");
                qm.dispose();
            } else {
                if (qm.getPlayer().getJob().getId() != 1210) {
					if (!qm.canHold(1142067)) {
						qm.sendNext("If you wish to receive the medal befitting the title, you may want to make some room in your equipment inventory.");
						qm.dispose();
						return;
					}
                    qm.gainItem(4032097, -30);
                    qm.gainItem(1142067, 1);
                    qm.getPlayer().changeJob(Packages.client.MapleJob.BLAZEWIZARD2);
                    qm.completeQuest();
                }
                qm.sendNext("You are no longer a Knight-in-Training. You are now an official Cygnus Knight!");
            }
        } else if (status == 2) {
            qm.sendNextPrev("I have given you some #bSP#k. I have also given you some skills of Blaze Wizards that are only available to official knights, so keep working!");
        } else if (status == 3) {
            qm.sendPrev("Now that you have officially become one, I want you to keep that fire in you that you had when you first started this journey, but this time, as a proud member of the Cygnus Knights!");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}