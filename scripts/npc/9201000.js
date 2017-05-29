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
 *Moony - 9201000.js
 *@author Jvlaple
 *For HurricaneMS v.59
 */
//var numberOfLoves = 0;
//var ringSelection = -1;
 
function start() {
    //    status = -1;
    //    action(1, 0, 0);
    //}

    //function action(mode, type, selection) {
    //    if (mode == -1) {
    //        cm.dispose();
    //    } else {
    //        if (mode == 0) {
    //            cm.dispose();
    //            return;
    //        }
    //        if (mode == 1) {
    //            status++;
    //        } else {
    //            status--;
    //        }
    //        if (status == 0) {
    //            if (cm.getPlayer().getMarriageQuestLevel() == 0 && cm.getPlayer().getLevel() >= 10) {
    //    cm.sendNext("Hey, I'm Moony, and I make engagement rings for marriage.");
    //            } else if (cm.getPlayer().getMarriageQuestLevel() == 1) {
    //                for (var i = 4031367; i < 4031373; i++)
    //                    numberOfLoves += cm.getPlayer().countItem(i);
    //                if (numberOfLoves >= 4) {
    //                    cm.sendNext("Wow, you're back pretty early. Got the #bProof of Loves#k? Lets see...");
    //                } else {
    //                    cm.sendOk("Please come back when you got 4 different #bProof of Loves#k.");
    //                    cm.dispose();
    //                }
    //            } else if (cm.getPlayer().getMarriageQuestLevel() == 2) {
    //                cm.sendSimple("Hey, your'e back! Ready to choose your ring?\r\n#b#L0#Moonstone Ring#l\r\n#L1#Star Gem Ring#l\r\n#L2#Golden Heart Ring#l\r\n#L3#Silver Swan Ring#l#k");
    //            } else {
    cm.sendOk("I hate making rings...");
    cm.dispose();
//            }
//        } else if (status == 1) {
//            if (cm.getPlayer().getMarriageQuestLevel() == 0 && cm.getPlayer().getLevel() >= 10) {
//                cm.sendYesNo("Hey, you look like you might want to be married! Want to make an engagement ring?");
//            } else if (cm.getPlayer().getMarriageQuestLevel() == 1) {
//                cm.sendNext("Great work getting the #bProof of Loves#k! Now we can make the #bEngagement Ring#k.");
//            } else if (cm.getPlayer().getMarriageQuestLevel() == 2) {
//                ringSelection = selection;
//                if (ringSelection == 0) {
//                    if (cm.haveItem(4011007, 1) && cm.haveItem(4021007, 1) && cm.getPlayer().getMeso() >= 3000000) {
//                        cm.gainItem(4011007, -1);
//                        cm.gainItem(4021007, -1);
//                        cm.gainMeso(-3000000);
//                        cm.gainItem(2240000, 1);
//                        cm.sendOk("Here's the ring as promised! Have fun!");
//                        cm.getPlayer().setMarriageQuestLevel(50);
//                        cm.dispose();
//                    } else {
//                        cm.sendNext("You did not get all the right materials. To make an engagement ring, I need one of the following:\r\n\r\n#e#dMoonstone Ring:#k\r\n#v4011007#Moon Rock 1,#v4021007#Diamond 1, 3,000,000 Meso\r\n#dStar Gem Ring:#k\r\n#v4021009#Star Rock 1,#v4021007#Diamond 1, 2,000,000 Meso\r\n#dGolden Heart Ring:#k\r\n#v4011006#Gold Plate 1,#v4021007#Diamond 1, 1,000,000 Meso\r\n#dSilver Swan Ring:#k\r\n#v4011004#Silver Plate 1,#v4021007#Diamond 1, 500,000 Meso\r\n");
//                        cm.dispose();
//                    }
//                } else if (ringSelection == 1) {
//                    if (cm.haveItem(4021009, 1) && cm.haveItem(4021007, 1) && cm.getPlayer().getMeso() >= 2000000) {
//                        cm.gainItem(4021009, -1);
//                        cm.gainItem(4021007, -1);
//                        cm.gainMeso(-2000000);
//                        cm.gainItem(2240001, 1);
//                        cm.sendOk("Here's the ring as promised! Have fun!");
//                        cm.getPlayer().setMarriageQuestLevel(50);
//                        cm.dispose();
//                    } else {
//                        cm.sendNext("You did not get all the right materials. To make an engagement ring, I need one of the following:\r\n\r\n#e#dMoonstone Ring:#k\r\n#v4011007#Moon Rock 1,#v4021007#Diamond 1, 3,000,000 Meso\r\n#dStar Gem Ring:#k\r\n#v4021009#Star Rock 1,#v4021007#Diamond 1, 2,000,000 Meso\r\n#dGolden Heart Ring:#k\r\n#v4011006#Gold Plate 1,#v4021007#Diamond 1, 1,000,000 Meso\r\n#dSilver Swan Ring:#k\r\n#v4011004#Silver Plate 1,#v4021007#Diamond 1, 500,000 Meso\r\n");
//                        cm.dispose();
//                    }
//                } else if (ringSelection == 2) {
//                    if (cm.haveItem(4011006, 1) && cm.haveItem(4021007, 1) && cm.getPlayer().getMeso() >= 1000000) {
//                        cm.gainItem(4011006, -1);
//                        cm.gainItem(4021007, -1);
//                        cm.gainMeso(-1000000);
//                        cm.gainItem(2240002, 1);
//                        cm.sendOk("Here's the ring as promised! Have fun!");
//                        cm.getPlayer().setMarriageQuestLevel(50);
//                        cm.dispose();
//                    } else {
//                        cm.sendNext("You did not get all the right materials. To make an engagement ring, I need one of the following:\r\n\r\n#e#dMoonstone Ring:#k\r\n#v4011007#Moon Rock 1,#v4021007#Diamond 1, 3,000,000 Meso\r\n#dStar Gem Ring:#k\r\n#v4021009#Star Rock 1,#v4021007#Diamond 1, 2,000,000 Meso\r\n#dGolden Heart Ring:#k\r\n#v4011006#Gold Plate 1,#v4021007#Diamond 1, 1,000,000 Meso\r\n#dSilver Swan Ring:#k\r\n#v4011004#Silver Plate 1,#v4021007#Diamond 1, 500,000 Meso\r\n");
//                        cm.dispose();
//                    }
//                } else if (ringSelection == 3) {
//                    if (cm.haveItem(4011004, 1) && cm.haveItem(4021007, 1) && cm.getPlayer().getMeso() >= 500000) {
//                        cm.gainItem(4011004, -1);
//                        cm.gainItem(4021007, -1);
//                        cm.gainMeso(-500000);
//                        cm.gainItem(2240003, 1);
//                        cm.sendOk("Here's the ring as promised! Have fun!");
//                        cm.getPlayer().setMarriageQuestLevel(50);
//                        cm.dispose();
//                    } else {
//                        cm.sendNext("You did not get all the right materials. To make an engagement ring, I need one of the following:\r\n\r\n#e#dMoonstone Ring:#k\r\n#v4011007#Moon Rock 1,#v4021007#Diamond 1, 3,000,000 Meso\r\n#dStar Gem Ring:#k\r\n#v4021009#Star Rock 1,#v4021007#Diamond 1, 2,000,000 Meso\r\n#dGolden Heart Ring:#k\r\n#v4011006#Gold Plate 1,#v4021007#Diamond 1, 1,000,000 Meso\r\n#dSilver Swan Ring:#k\r\n#v4011004#Silver Plate 1,#v4021007#Diamond 1, 500,000 Meso\r\n");
//                        cm.dispose();
//                    }
//                }
//            }
//        } else if (status == 2) {
//            if (cm.getPlayer().getMarriageQuestLevel() == 0 && cm.getPlayer().getLevel() >= 10) {
//                cm.getPlayer().addMarriageQuestLevel();
//                cm.sendOk("Okay, first bring me back any four colored #bProof of Loves#k. You can get them from talking to #bNana the Love Fairy#k in any town. Also, only one of you, either the Groom or Bride will do this quest.");
//                cm.dispose();
//            } else if (cm.getPlayer().getMarriageQuestLevel() == 1) {
//                for (var j = 4031367; j < 4031373; j++)
//                    cm.removeAll(j);
//                cm.getPlayer().addMarriageQuestLevel();
//                cm.sendNextPrev("You need the following raw materials to make an\r\n#bEngagement Ring#k.\r\n\r\n#e#dMoonstone Ring:#k\r\n#v4011007#Moon Rock 1,#v4021007#Diamond 1, 3,000,000 Meso\r\n#dStar Gem Ring:#k\r\n#v4021009#Star Rock 1,#v4021007#Diamond 1, 2,000,000 Meso\r\n#dGolden Heart Ring:#k\r\n#v4011006#Gold Plate 1,#v4021007#Diamond 1, 1,000,000 Meso\r\n#dSilver Swan Ring:#k\r\n#v4011004#Silver Plate 1,#v4021007#Diamond 1, 500,000 Meso\r\n");
//    cm.dispose();
//            }
//        }
//    }
}