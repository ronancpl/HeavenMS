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
 *9201026 - Nana(L)
 *@author Jvlaple
 */
 
function start() {
    cm.sendOk("Hi, I'm Nana the love fairy... How's it going?");
    cm.dispose();
}
//
//function action(mode, type, selection) {
//    if (mode == -1) {
//        cm.dispose();
//    } else {
//        if (mode == 0 && status == 0) {
//            cm.dispose();
//            return;
//        }
//        if (mode == 1)
//            status++;
//        else
//            status--;
//        if (cm.getPlayer().getMarriageQuestLevel() == 1 || cm.getPlayer().getMarriageQuestLevel() == 52) {
//            if (!cm.haveItem(4000106, 30)) {
//                if (status == 0) {
//                    cm.sendNext("Hey, you look like you need proofs of love? I can get them for you.");
//                } else if (status == 1) {
//                    cm.sendNext("All you have to do is bring me 30 #bTeddy Cotton#k.");
//                    cm.dispose();
//                }
//            } else {
//                if (status == 0) {
//                    cm.sendNext("Wow, you were quick! Heres the proof of love...");
//                    cm.gainItem(4000106, -30)
//                    cm.gainItem(4031370, 1);
//                    cm.dispose();
//                }
//            }
//        } else {
//            cm.sendOk("Hi, I'm Nana the love fairy... Hows it going?");
//            cm.dispose();
//        }
//    }
//}