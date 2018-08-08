/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* John JQ Flower pile #3
*/

var repeatablePrizes = [[4010006, 4], [4010007, 4], [4020007, 4]];

function start() {
    if (cm.isQuestStarted(2054) && !cm.haveItem(4031028,30)) {
        if(!cm.canHold(4031028,30)) {
            cm.sendNext("Check for a available slot on your ETC inventory.")
            cm.dispose();
            return;
        }
        
        cm.gainItem(4031028,30);
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