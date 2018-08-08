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
/* John JQ Flower pile #1
*/

var repeatablePrizes = [[4010000, 3], [4010001, 3], [4010002, 3], [4010003, 3], [4010004, 3], [4010005, 3]];

function start() {
    if (cm.isQuestStarted(2052) && !cm.haveItem(4031025,10)) {
        if(!cm.canHold(4031025,10)) {
            cm.sendNext("Check for a available slot on your ETC inventory.");
            cm.dispose();
            return;
        }
        
        cm.gainItem(4031025,10);
    } else {
        if(cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.ETC).getNumFreeSlot() < 1) {
            cm.sendNext("Check for a available slot on your ETC inventory.");
            cm.dispose();
            return;
        }
        
        var itemPrize = repeatablePrizes[Math.floor((Math.random() * repeatablePrizes.length))];
        cm.gainItem(itemPrize[0], itemPrize[1]);
    }
    
    cm.warp(105040300, 0);
    cm.dispose();
}