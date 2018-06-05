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
-- Odin JavaScript --------------------------------------------------------------------------------
	Arwen the Fairy - Victoria Road : Ellinia (101000000)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var item;
var selected;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.dispose();
            return;
        } else if (status == 2 && mode == 0) {
            cm.sendNext("It's not easy making " + item + ". Please get the materials ready.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getLevel() >= 40) {
                cm.sendNext("Yeah... I am the master alchemist of the fairies. But the fairies are not supposed to be in contact with a human being for a long period of time... A strong person like you will be fine, though. If you get me the materials, I'll make you a special item.");
            } else {
                cm.sendOk("I can make rare, valuable items but unfortunately I can't make it to a stranger like you.");
                cm.dispose();
            }
        } else if (status == 1) {
            cm.sendSimple("What do you want to make?#b\r\n#L0#Moon Rock#l\r\n#L1#Star Rock#l\r\n#L2#Black Feather#l");
        } else if (status == 2) {
            selected = selection;
            if (selection == 0) {
                item = "Moon Rock";
                cm.sendYesNo("So you want to make a Moon Rock? To do that you need to refine one of each of these: #bBronze Plate#k, #bSteel Plate#k,\r\n#bMithril Plate#k, #bAdamantium Plate#k, #bSilver Plate#k, #bOrihalcon Plate#k and #bGold Plate#k. Throw in 10,000 mesos and I'll make it for you.");
            } else if (selection == 1) {
                item = "Star Rock";
                cm.sendYesNo("So you want to make a Star Rock? To do that you need to refine one of each of these: #bGarnet#k, #bAmethyst#k, #bAquaMarine#k, #bEmerald#k, #bOpal#k, #bSapphire#k, #bTopaz#k, #bDiamond#k and #bBlack Crystal#k. Throw in 15,000 mesos and I'll make it for you.");
            } else if (selection == 2) {
                item = "Black Feather";
                cm.sendYesNo("So you want to make a Black Feather? To do that you need #b1 Flaming Feather#k, #b1 Moon Rock#k and #b1 Black Crystal#k. Throw in 30,000 mesos and I'll make it for you. Oh yeah, this piece of feather is a very special item, so if you drop it by any chance, it'll disappear, as well as you won't be able to give it away to someone else.");
            }
        } else if (status == 3) {
            if (selected == 0) {
                if (cm.haveItem(4011000) && cm.haveItem(4011001) && cm.haveItem(4011002) && cm.haveItem(4011003) && cm.haveItem(4011004) && cm.haveItem(4011005) && cm.haveItem(4011006) && cm.getMeso() >= 10000) {
                    cm.gainMeso(-10000);
                    for(var i = 4011000; i<4011007; i++) {
                        cm.gainItem(i,-1);
                    }
                    cm.gainItem(4011007, 1);
                    cm.sendNext("Ok here, take " + item + ". It's well-made, probably because I'm using good materials. If you need my help down the road, feel free to come back.");
                } else {
                    cm.sendNext("Are you sure you have enough mesos? Please check and see if you have the refined #bBronze Plate#k, #bSteel Plate#k,\r\n#bMithril Plate#k, #bAdamantium Plate#k, #bSilver Plate#k, #bOrihalcon Plate#k and #bGold Plate#k, one of each.");
                }
            } else if (selected == 1) {
                if (cm.haveItem(4021000) && cm.haveItem(4021001) && cm.haveItem(4021002) && cm.haveItem(4021003) && cm.haveItem(4021004) && cm.haveItem(4021005) && cm.haveItem(4021006) && cm.haveItem(4021007) && cm.haveItem(4021008) && cm.getMeso() >= 15000) {
                    cm.gainMeso(-15000);
                    for(var j = 4021000; j<4021009; j++) {
                        cm.gainItem(j,-1);
                    }
                    cm.gainItem(4021009, 1);
                    cm.sendNext("Ok here, take " + item + ". It's well-made, probably because I'm using good materials. If you need my help down the road, feel free to come back.");
                } else {
                    cm.sendNext("Are you sure you have enough mesos? Please check and see if you have the refined #bGarnet#k, #bAmethyst#k, #bAquaMarine#k, #bEmerald#k, #bOpal#k, #bSapphire#k, #bTopaz#k, #bDiamond#k and #bBlack Crystal#k, one of each.");
                }
            } else if (selected == 2) {
                if (cm.haveItem(4001006) && cm.haveItem(4011007) && cm.haveItem(4021008) && cm.getMeso() >= 30000) {
                    cm.gainMeso(-30000);
                    for(var k = 4001006; k<4021009; k+=10001) {
                        cm.gainItem(k,-1);
                    }
                    cm.gainItem(4031042, 1);
                    cm.sendNext("Ok here, take " + item + ". It's well-made, probably because I'm using good materials. If you need my help down the road, feel free to come back.");
                } else {
                    cm.sendNext("Are you sure you have enough mesos? Please check and see if you have the refined #bGarnet#k, #bAmethyst#k, #bAquaMarine#k, #bEmerald#k, #bOpal#k, #bSapphire#k, #bTopaz#k, #bDiamond#k and #bBlack Crystal#k, one of each.");
                }
            }
            cm.dispose();
        }
    }
}