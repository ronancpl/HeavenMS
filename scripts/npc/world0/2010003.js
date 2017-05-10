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

/* Neve
	Orbis: Orbis Park (200000200)
	
	Refining NPC: 
	* Gloves, level 70-80 all classes
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;

function start() {
    cm.getPlayer().setCS(true);
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        cm.dispose();
    if (status == 0 && mode == 1) {
        var selStr = "Hello there. I'm Orbis' number one glove maker. Would you like me to make you something?#b"
        var options = new Array("Create or upgrade a Warrior glove","Create or upgrade a Bowman glove","Create or upgrade a Magician glove","Create or upgrade a Thief glove");
        for (var i = 0; i < options.length; i++){
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }
			
        cm.sendSimple(selStr);
    }
    else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0){ //warrior glove
            var selStr = "Warrior glove? Okay, then which one?#b";
            var gloves = new Array ("Bronze Husk#k - Warrior Lv. 70#b","Mithril Husk#k - Warrior Lv. 70#b","Dark Husk#k - Warrior Lv. 70#b",
                "Sapphire Emperor#k - Warrior Lv. 80#b","Emerald Emperor#k - Warrior Lv. 80#b","Blood Emperor#k - Warrior Lv. 80#b","Dark Emperor#k - Warrior Lv. 80#b");
            for (var i = 0; i < gloves.length; i++){
                selStr += "\r\n#L" + i + "# " + gloves[i] + "#l";
            }
            cm.sendSimple(selStr);
        }
        else if (selectedType == 1){ //bowman glove
            var selStr = "Bowman glove? Okay, then which one?#b";
            var gloves = new Array ("Blue Eyes#k - Bowman Lv. 70#b","Gold Eyes#k - Bowman Lv. 70#b","Dark Eyes#k - Bowman Lv. 70#b",
                "Red Cordon#k - Bowman Lv. 80#b","Blue Cordon#k - Bowman Lv. 80#b","Green Cordon#k - Bowman Lv. 80#b","Dark Cordon#k - Bowman Lv. 80#b");
            for (var i = 0; i < gloves.length; i++){
                selStr += "\r\n#L" + i + "# " + gloves[i] + "#l";
            }
            cm.sendSimple(selStr);
        }
        else if (selectedType == 2){ //mage glove
            var selStr = "Magician glove? Okay, then which one?#b";
            var gloves = new Array ("Brown Lorin#k - Magician Lv. 70#b","Blue Lorin#k - Magician Lv. 70#b","Dark Lorin#k - Magician Lv. 70#b",
                "Green Clarity#k - Magician Lv. 80#b","Blue Clarity#k - Magician Lv. 80#b","Dark Clarity#k - Magician Lv. 80#b");
            for (var i = 0; i < gloves.length; i++){
                selStr += "\r\n#L" + i + "# " + gloves[i] + "#l";
            }
            cm.sendSimple(selStr);
        }
        else if (selectedType == 3){ //thief glove
            var selStr = "Thief glove? Okay, then which one?#b";
            var gloves = new Array ("Bronze Rover#k - Thief Lv. 70#b","Silver Rover#k - Thief Lv. 70#b","Gold Rover#k - Thief Lv. 70#b",
                "Green Larceny#k - Thief Lv. 80#b","Purple Larceny#k - Thief Lv. 80#b","Dark Larceny#k - Thief Lv. 80#b");
            for (var i = 0; i < gloves.length; i++){
                selStr += "\r\n#L" + i + "# " + gloves[i] + "#l";
            }
            cm.sendSimple(selStr);
        }
    }
    else if (status == 2 && mode == 1) {
        selectedItem = selection;

        if (selectedType == 0){ //warrior glove
            var itemSet = new Array(1082103,1082104,1082105,1082114,1082115,1082116,1082117,1082118);
            var matSet = new Array(new Array(4005000,4011000,4011006,4000030,4003000),new Array(1082103,4011002,4021006),new Array(1082103,4021006,4021008),new Array(4005000,4005002,4021005,4000030,4003000),new Array(1082114,4005000,4005002,4021003),new Array(1082114,4005002,4021000),new Array(1082114,4005000,4005002,4021008));
            var matQtySet = new Array(new Array(2,8,3,70,55),new Array(1,6,4),new Array(1,8,3),new Array(2,1,8,90,60),new Array(1,1,1,7),new Array(1,3,8),new Array(1,2,1,4));
            var costSet = new Array(90000,90000,100000,100000,110000,110000,120000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        else if (selectedType == 1){ //bowman glove
            var itemSet = new Array(1082106,1082107,1082108,1082109,1082110,1082111,1082112);
            var matSet = new Array(new Array(4005002,4021005,4011004,4000030,4003000),new Array(1082106,4021006,4011006),new Array(1082106,4021007,4021008),new Array(4005002,4005000,4021000,4000030,4003000),new Array(1082109,4005002,4005000,4021005),new Array(1082109,4005002,4005000,4021003),new Array(1082109,4005002,4005000,4021008));
            var matQtySet = new Array(new Array(2,8,3,70,55),new Array(1,5,3),new Array(1,2,3),new Array(2,1,8,90,60),new Array(1,1,1,7),new Array(1,1,1,7),new Array(1,2,1,4));
            var costSet = new Array(90000,90000,100000,100000,110000,110000,120000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        else if (selectedType == 2){ //mage glove
            var itemSet = new Array(1082098,1082099,1082100,1082121,1082122,1082123);
            var matSet = new Array(new Array(4005001,4011000,4011004,4000030,4003000),new Array(1082098,4021002,4021007),new Array(1082098,4021008,4011006),new Array(4005001,4005003,4021003,4000030,4003000),new Array(1082121,4005001,4005003,4021005),new Array(1082121,4005001,4005003,4021008));
            var matQtySet = new Array(new Array(2,6,6,70,55),new Array(1,6,2),new Array(1,3,3),new Array(2,1,8,90,60),new Array(1,1,1,7),new Array(1,2,1,4));
            var costSet = new Array(90000,90000,100000,100000,110000,120000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        else if (selectedType == 3){ //thief glove
            var itemSet = new Array (1082095,1082096,1082097,1082118,1082119,1082120);
            var matSet = new Array(new Array(4005003,4011000,4011003,4000030,4003000),new Array(1082095,4011004,4021007),new Array(1082095,4021007,4011006),new Array(4005003,4005002,4011002,4000030,4003000),new Array(1082118,4005003,4005002,4021001),new Array(1082118,4005003,4005002,4021000));
            var matQtySet = new Array(new Array(2,6,6,70,55),new Array(1,6,2),new Array(1,3,3),new Array(2,1,8,90,60),new Array(1,1,1,7),new Array(1,2,1,8));
            var costSet = new Array(90000,90000,100000,100000,110000,120000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
		
        var prompt = "You want me to make a #t" + item + "#? In that case, I'm going to need specific items from you in order to make it. Make sure you have room in your inventory, though!#b";

        if (mats instanceof Array){
            for(var i = 0; i < mats.length; i++){
                prompt += "\r\n#i"+mats[i]+"# " + matQty[i] + " #t" + mats[i] + "#";
            }
        }
        else {
            prompt += "\r\n#i"+mats+"# " + matQty + " #t" + mats + "#";
        }
		
        if (cost > 0)
            prompt += "\r\n#i4031138# " + cost + " meso";
		
        cm.sendYesNo(prompt);
    }
    else if (status == 3 && mode == 1) {
        var complete = true;
		
        if(!cm.canHold(item, 1)) {
            cm.sendOk("Check your inventory for a free slot first.");
            cm.dispose();
            return;
        }
        else if (cm.getMeso() < cost)
        {
            cm.sendOk("I'm afraid you cannot afford my services.");
            cm.dispose();
            return;
        }
        else
        {
            if (mats instanceof Array) {
                for(var i = 0; complete && i < mats.length; i++)
                    if (!cm.haveItem(mats[i], matQty[i]))
                        complete = false;
            }
            else if (!cm.haveItem(mats, matQty))
                complete = false;
        }
			
        if (!complete)
            cm.sendOk("I'm afraid that substitute items are unacceptable, if you want your gloves made properly.");
        else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++){
                    cm.gainItem(mats[i], -matQty[i]);
                }
            }
            else
                cm.gainItem(mats, -matQty);
					
            cm.gainMeso(-cost);
            cm.gainItem(item, 1);
            cm.sendOk("Done. If you need anything else, just ask again.");
        }
        cm.dispose();
    }
}