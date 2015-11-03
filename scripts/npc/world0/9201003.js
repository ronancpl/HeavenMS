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
/**
 *9201003.js - Mom and Dad
 *@author Jvlaple
 */
var numberOfLoves = 0;
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (cm.getPlayer().getMarriageQuestLevel() == 51) {
            if (status == 0) {
                if (cm.getPlayer().getGender() == 0) {
                    cm.sendYesNo("Hello my child. Are you sure that you want to get married to this girl? I believe in love at first sight, but this is rather sudden... I don't think we are ready for this. Lets think about it. Do you really love this girl?");
                } else {
                    cm.sendYesNo("Hello my child. Are you sure that you want to get married to this man? I believe in love at first sight, but this is rather sudden... I don't think we are ready for this. Lets think about it. Do you really love this man?");
                }
            } else if (status == 1) {
                cm.getPlayer().addMarriageQuestLevel();
                cm.sendNext("Okay then. Go back to town and collect two more #bProof of Loves#k to prove it.");
                cm.dispose();
            }
        } else if (cm.getPlayer().getMarriageQuestLevel() == 52) {
            if (status == 0) {
                numberOfLoves += cm.getPlayer().countItem(4031367);
                numberOfLoves += cm.getPlayer().countItem(4031368);
                numberOfLoves += cm.getPlayer().countItem(4031369);
                numberOfLoves += cm.getPlayer().countItem(4031370);
                numberOfLoves += cm.getPlayer().countItem(4031371);
                numberOfLoves += cm.getPlayer().countItem(4031372);
                if (numberOfLoves >= 2) {
                    cm.sendNext("Wow, you really are serious! Okay then, here is our blessing.");
                } else {
                    cm.sendNext("Come back when you get two #bProof of Loves#k.");
                    cm.dispose();
                }
            } else if (status == 1) {
                cm.getPlayer().addMarriageQuestLevel();
                cm.removeAll(4031367);
                cm.removeAll(4031368);
                cm.removeAll(4031369);
                cm.removeAll(4031370);
                cm.removeAll(4031371);
                cm.removeAll(4031372);
                cm.gainItem(4031373, 1);
                cm.dispose();
            }
        } else {
            cm.sendOk("Hello we're Mom and Dad...");
            cm.dispose();
        }
    }
}