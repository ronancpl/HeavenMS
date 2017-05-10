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

/* Sarah
	Ludibrium : Tara and Sarah's House (220000303)

	Refining NPC:
	* Gloves - All classes, 30-50, stimulator (4130000) available on upgrades
	* Price is 90% of locations on same items
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var stimulator = false;
var stimID = 4130000;

function start() {
    cm.getPlayer().setCS(true);

    var selStr = "Hello, and welcome to the Ludibrium Glove Store. How can I help you today?#b"
    var options = new Array("What's a stimulator?","Create a Warrior glove","Create a Bowman glove","Create a Magician glove","Create a Thief glove",
        "Create a Warrior glove with a Stimulator","Create a Bowman glove with a Stimulator","Create a Magician glove with a Stimulator","Create a Thief glove with a Stimulator");
    for (var i = 0; i < options.length; i++){
        selStr += "\r\n#L" + i + "# " + options[i] + "#l";
    }
    cm.sendSimple(selStr);
}

function action(mode, type, selection) {
    if (mode > 0)
        status++;
    else {
        cm.dispose();
        return;
    }
    if (status == 1) {
        selectedType = selection;
        if (selectedType > 4)
            stimulator = true;
        else
            stimulator = false;
        if (selectedType == 0) { //What's a stim?
            cm.sendNext("A stimulator is a special potion that I can add into the process of creating certain items. It gives it stats as though it had dropped from a monster. However, it is possible to have no change, and it is also possible for the item to be below average. There's also a 10% chance of not getting any item when using a stimulator, so please choose wisely.")
            cm.dispose();
        } else if (selectedType == 1){ //warrior glove
            var selStr = "Warrior glove? Sure thing, which kind?#b";
            var items = new Array ("Bronze Missel#k - Warrior Lv. 30#b","Steel Briggon#k - Warrior Lv. 35#b","Iron Knuckle#k - Warrior Lv. 40#b","Steel Brist#k - Warrior Lv. 50#b");
            for (var i = 0; i < items.length; i++){
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 2){ //bowman glove
            var selStr = "Bowman glove? Sure thing, which kind?#b";
            var items = new Array ("Brown Marker#k - Bowman Lv. 30#b","Bronze Scaler#k - Bowman Lv. 35#b","Aqua Brace#k - Bowman Lv. 40#b","Blue Willow#k - Bowman Lv. 50#b");
            for (var i = 0; i < items.length; i++){
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 3){ //magician glove
            var selStr = "Magician glove? Sure thing, which kind?#b";
            var items = new Array ("Red Lutia#k - Magician Lv. 30#b","Red Noel#k - Magician Lv. 35#b","Red Arten#k - Magician Lv. 40#b","Red Pennance#k - Magician Lv. 50#b");
            for (var i = 0; i < items.length; i++){
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 4){ //thief glove
            var selStr = "Thief glove? Sure thing, which kind?#b";
            var gloves = new Array ("Steel Sylvia#k - Thief Lv. 30#b","Steel Arbion#k - Thief Lv. 35#b","Red Cleave#k - Thief Lv. 40#b","Blue Moon Glove#k - Thief Lv. 50#b");
            for (var i = 0; i < gloves.length; i++){
                selStr += "\r\n#L" + i + "# " + gloves[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 5){ //warrior glove w/ Stim
            var selStr = "Warrior glove with a stimulator? Sure thing, which kind?#b";
            var crystals = new Array ("Steel Missel#k - Warrior Lv. 30#b","Orihalcon Missel#k - Warrior Lv. 30#b","Yellow Briggon#k - Warrior Lv. 35#b","Dark Briggon#k - Warrior Lv. 35#b",
                "Adamantium Knuckle#k - Warrior Lv. 40#b","Dark Knuckle#k - Warrior Lv. 40#b","Mithril Brist#k - Warrior Lv. 50#b","Gold Brist#k - Warrior Lv. 50#b");
            for (var i = 0; i < crystals.length; i++){
                selStr += "\r\n#L" + i + "# " + crystals[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 6){ //bowman glove w/ stim
            var selStr = "Bowman glove with a stimulator? Sure thing, which kind?#b";
            var crystals = new Array ("Green Marker#k - Bowman Lv. 30#b","Black Marker#k - Bowman Lv. 30#b","Mithril Scaler#k - Bowman Lv. 35#b","Gold Scaler#k - Bowman Lv. 35#b","Gold Brace#k - Bowman Lv. 40#b","Dark Brace#k - Bowman Lv. 40#b","Red Willow#k - Bowman Lv. 50#b","Dark Willow#k - Bowman Lv. 50#b");
            for (var i = 0; i < crystals.length; i++){
                selStr += "\r\n#L" + i + "# " + crystals[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 7){ //magician glove w/ stim
            var selStr = "Magician glove with a stimulator? Sure thing, which kind?#b";
            var items = new Array ("Blue Lutia#k - Magician Lv. 30#b","Black Lutia#k - Magician Lv. 30#b","Blue Noel#k - Magician Lv. 35#b","Dark Noel#k - Magician Lv. 35#b",
                "Blue Arten#k - Magician Lv. 40#b","Dark Arten#k - Magician Lv. 40#b","Blue Pennance#k - Magician Lv. 50#b","Dark Pennance#k - Magician Lv. 50#b");
            for (var i = 0; i < items.length; i++){
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 8){ //thief glove w/ stim
            var selStr = "Thief glove with a stimulator? Sure thing, which kind?#b";
            var gloves = new Array ("Silver Sylvia#k - Thief Lv. 30#b","Gold Sylvia#k - Thief Lv. 30#b","Orihalcon Arbion#k - Thief Lv. 35#b","Gold Arbion#k - Thief Lv. 35#b","Gold Cleave#k - Thief Lv. 40#b",
                "Dark Cleave#k - Thief Lv. 40#b","Red Moon Glove#k - Thief Lv. 50#b","Brown Moon Glove#k - Thief Lv. 50#b");
            for (var i = 0; i < gloves.length; i++){
                selStr += "\r\n#L" + i + "# " + gloves[i] + "#l";
            }
            cm.sendSimple(selStr);
        }
    }
    else if (status == 2) {
        selectedItem = selection;
        if (selectedType == 1){ //warrior glove
            var itemSet = new Array(1082007,1082008,1082023,1082009);
            var matSet = new Array(new Array(4011000,4011001,4003000),new Array(4000021,4011001,4003000),new Array(4000021,4011001,4003000),new Array(4011001,4021007,4000030,4003000));
            var matQtySet = new Array(new Array(3,2,15),new Array(30,4,15),new Array(50,5,40),new Array(3,2,30,45));
            var costSet = new Array(18000,27000,36000,45000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 2){ //bowman glove
            var itemSet = new Array(1082048,1082068,1082071,1082084);
            var matSet = new Array(new Array(4000021,4011006,4021001),new Array(4011000,4011001,4000021,4003000),new Array(4011001,4021000,4021002,4000021,4003000),new Array(4011004,4011006,4021002,4000030,4003000));
            var matQtySet = new Array(new Array(50,2,1),new Array(1,3,60,15),new Array(3,1,3,80,25),new Array(3,1,2,40,35));
            var costSet = new Array(18000,27000,36000,45000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 3){ //magician glove
            var itemSet = new Array(1082051,1082054,1082062,1082081);
            var matSet = new Array(new Array(4000021,4021006,4021000),new Array(4000021,4011006,4011001,4021000),new Array(4000021,4021000,4021006,4003000),new Array(4021000,4011006,4000030,4003000));
            var matQtySet = new Array(new Array(60,1,2),new Array(70,1,3,2),new Array(80,3,3,30),new Array(3,2,35,40));
            var costSet = new Array(22500,27000,36000,45000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 4){ //thief glove
            var itemSet = new Array(1082042,1082046,1082075,1082065);
            var matSet = new Array(new Array(4011001,4000021,4003000),new Array(4011001,4011000,4000021,4003000),new Array(4021000,4000101,4000021,4003000),new Array(4021005,4021008,4000030,4003000));
            var matQtySet = new Array(new Array(2,50,10),new Array(3,1,60,15),new Array(3,100,80,30),new Array(3,1,40,30));
            var costSet = new Array(22500,27000,36000,45000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 5){ //warrior glove w/stim
            var itemSet = new Array(1082005,1082006,1082035,1082036,1082024,1082025,1082010,1082011);
            var matSet = new Array(new Array(1082007,4011001),new Array(1082007,4011005),new Array(1082008,4021006),new Array(1082008,4021008),new Array(1082023,4011003),new Array(1082023,4021008),
                new Array(1082009,4011002),new Array(1082009,4011006));
            var matQtySet = new Array (new Array(1,1),new Array(1,2),new Array(1,3),new Array(1,1),new Array(1,4),new Array(1,2),new Array(1,5),new Array(1,4));
            var costSet = new Array (18000,22500,27000,36000,40500,45000,49500,54000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 6){ //bowman glove w/stim
            var itemSet = new Array (1082049,1082050,1082069,1082070,1082072,1082073,1082085,1082083);
            var matSet = new Array(new Array(1082048,4021003),new Array(1082048,4021008),new Array(1082068,4011002),new Array(1082068,4011006),new Array(1082071,4011006),new Array(1082071,4021008),new Array(1082084,4011000,4021000),new Array(1082084,4011006,4021008));
            var matQtySet = new Array (new Array(1,3),new Array(1,1),new Array(1,4),new Array(1,2),new Array(1,4),new Array(1,2),new Array(1,1,5),new Array(1,2,2));
            var costSet = new Array (13500,18000,19800,22500,27000,36000,49500,54000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 7){ //magician glove w/ stim
            var itemSet = new Array(1082052,1082053,1082055,1082056,1082063,1082064,1082082,1082080);
            var matSet = new Array(new Array(1082051,4021005),new Array(1082051,4021008),new Array(1082054,4021005),new Array(1082054,4021008),new Array(1082062,4021002),new Array(1082062,4021008),
                new Array(1082081,4021002),new Array(1082081,4021008));
            var matQtySet = new Array(new Array(1,3),new Array(1,1),new Array(1,3),new Array(1,1),new Array(1,4),new Array(1,2),new Array(1,5),new Array(1,3));
            var costSet = new Array (31500,36000,36000,40500,40500,45000,49500,54000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 8){ //thief glove w/ stim
            var itemSet = new Array(1082043,1082044,1082047,1082045,1082076,1082074,1082067,1082066);
            var matSet = new Array(new Array(1082042,4011004),new Array(1082042,4011006),new Array(1082046,4011005),new Array(1082046,4011006),new Array(1082075,4011006),new Array(1082075,4021008),new Array(1082065,4021000),new Array(1082065,4011006,4021008));
            var matQtySet = new Array(new Array(1,2),new Array(1,1),new Array(1,3),new Array(1,2),new Array(1,4),new Array(1,2),new Array(1,5),new Array(1,2,1));
            var costSet = new Array (13500,18000,19800,22500,36000,45000,49500,54000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        var prompt = "You want me to make a #t" + item + "#? In that case, I'm going to need specific items from you in order to make it. Make sure you have room in your inventory, though!#b";
        if(stimulator)
            prompt += "\r\n#i"+stimID+"# 1 #t" + stimID + "#";
        if (mats instanceof Array){
            for(var i = 0; i < mats.length; i++){
                prompt += "\r\n#i"+mats[i]+"# " + matQty[i] + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i"+mats+"# " + matQty + " #t" + mats + "#";
        }
        if (cost > 0)
            prompt += "\r\n#i4031138# " + cost + " meso";
        cm.sendYesNo(prompt);
    } else if (status == 3) {
        var complete = true;
        
        if(!cm.canHold(item, 1)) {
            cm.sendOk("Check your inventory for a free slot first.");
            cm.dispose();
            return;
        }
        else if (cm.getMeso() < cost) {
            cm.sendOk("Sorry, we only accept meso.");
            cm.dispose();
            return;
        }
        else {
            if (mats instanceof Array) {
                for(var i = 0; complete && i < mats.length; i++)
                    if (!cm.haveItem(mats[i], matQty[i]))
                        complete = false;
            }
            else if (!cm.haveItem(mats, matQty))
                complete = false;
        }
        if (stimulator){ //check for stimulator
            if (!cm.haveItem(stimID)) {
                complete = false;
            }
        }
        if (!complete)
            cm.sendOk("Sorry, but I have to have those items to get this exactly right. Perhaps next time.");
        else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++){
                    cm.gainItem(mats[i], -matQty[i]);
                }
            } else
                cm.gainItem(mats, -matQty);
            cm.gainMeso(-cost);
            if (stimulator){ //check for stimulator
                cm.gainItem(stimID, -1);
                var deleted = Math.floor(Math.random() * 10);
                if (deleted != 0) {
                    cm.gainRandomItem(newItem);
                    cm.sendOk("There, the gloves are ready. Be careful, they're still hot.");
                } else {
                    cm.sendOk("Eek! I think I accidently added too much stimulator and, well, the whole thing is unusable now... Sorry, but I can't offer a refund.");
                }
            } else  {
                cm.gainItem(item, 1);
                cm.sendOk("There, the gloves are ready. Be careful, they're still hot.");
            }
        }
        cm.dispose();
    }
}