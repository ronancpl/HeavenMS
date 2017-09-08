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
/* Rydole
	Ludibrium : Toy Factory <Aparatus Room> (220020600)
	
	Refining NPC: 
	* Level 30-50 weapons - Stimulator allowed
*/
var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var stimulator = false;
var stimID;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        cm.dispose();
    if (status == 0 && mode == 1) {
        var selStr = "Ah, you've found me! I spend most of my time here, working on weapons to make for travellers like yourself. Did you have a request?#b"
        var options = new Array("What's a stimulator?","Create a Warrior weapon","Create a Bowman weapon","Create a Magician weapon","Create a Thief weapon",
            "Create a Warrior weapon with a Stimulator","Create a Bowman weapon with a Stimulator","Create a Magician weapon with a Stimulator","Create a Thief weapon with a Stimulator");
        for (var i = 0; i < options.length; i++){
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }
			
        cm.sendSimple(selStr);
    }
    else if (status == 1 && mode == 1) {
        selectedType = selection;
        var selStr;
        var weapon;
        if (selectedType > 4)
        {
            stimulator = true;
            selectedType -= 4;
        }
        else
            stimulator = false;
        if (selectedType == 0) { //What's a stim?
            cm.sendNext("A stimulator is a special potion that I can add into the process of creating certain items. It gives it stats as though it had dropped from a monster. However, it is possible to have no change, and it is also possible for the item to be below average. There's also a 10% chance of not getting any item when using a stimulator, so please choose wisely.")
            cm.dispose();
        }
        else if (selectedType == 1){ //warrior weapon
            selStr = "Very well, then which Warrior weapon shall I work on?#b";
            weapon = new Array ("Gladius#k - Lv. 30 One-Handed Sword#b","Cutlus#k - Lv. 35 One-Handed Sword#b","Traus#k - Lv. 40 One-Handed Sword#b","Jeweled Katar#k - Lv. 50 One-Handed Sword#b","Fireman's Axe#k - Lv. 30 One-Handed Axe#b","Dankke#k - Lv. 35 One-Handed Axe#b","Blue Counter#k - Lv. 40 One-Handed Axe#b","Buck#k - Lv. 50 One-Handed Axe#b",
                "War Hammer#k - Lv. 30 One-Handed BW#b","Heavy Hammer#k - Lv. 35 One-Handed BW#b","Jacker#k - Lv. 40 One-Handed BW#b","Knuckle Mace#k - Lv. 50 One-Handed BW#b","Scimitar#k - Lv. 30 Two-Handed Sword#b","Lionheart#k - Lv. 35 Two-Handed Sword#b","Zard#k - Lv. 40 Two-Handed Sword#b","Lion's Fang#k - Lv. 50 Two-Handed Sword#b",
                "Blue Axe#k - Lv. 30 Two-Handed Axe#b","Niam#k - Lv. 35 Two-Handed Axe#b","Sabretooth#k - Lv. 40 Two-Handed Axe#b","The Rising#k - Lv. 50 Two-Handed Axe#b","Mithril Maul#k - Lv. 30 Two-Handed BW#b","Sledgehammer#k - Lv. 35 Two-Handed BW#b","Titan#k - Lv. 40 Two-Handed BW#b","Golden Mole#k - Lv. 50 Two-Handed BW#b",
                "Forked Spear#k - Lv. 30 Spear#b","Nakimaki#k - Lv. 35 Spear#b","Zeco#k - Lv. 40 Spear#b","Serpent's Tongue#k - Lv. 50 Spear#b","Mithril Polearm#k - Lv. 30 Polearm#b","Axe Polearm#k - Lv. 35 Polearm#b","Crescent Polearm#k - Lv. 40 Polearm#b","The Nine Dragons#k - Lv. 50 Polearm#b");
        }
        else if (selectedType == 2){ //bowman weapon
            selStr = "Very well, then which Bowman weapon shall I work on?#b";
            weapon = new Array ("Ryden#k - Lv. 30 Bow#b","Red Viper#k - Lv. 35 Bow#b","Vaulter 2000#k - Lv. 40 Bow#b","Olympus#k - Lv. 50 Bow#b","Eagle Crow#k - Bowman Lv. 32#b","Heckler#k - Bowman Lv. 38#b","Silver Crow#k - Bowman Lv. 42#b","Rower#k - Bowman Lv. 50#b");
        }
        else if (selectedType == 3){ //magician weapon
            selStr = "Very well, then which Magician weapon shall I work on?#b";
            weapon = new Array ("Mithril Wand#k - Lv. 28 Wand#b","Wizard Wand#k - Lv. 33 Wand#b","Fairy Wand#k - Lv. 38 Wand#b","Cromi#k - Lv. 48 Wand#b","Wizard Staff#k - Lv. 25 Staff#b","Arc Staff#k - Lv. 45 Staff#b","Thorns#k - Lv. 55 Staff#b");
        }
        else if (selectedType == 4){ //thief weapon; claws vary depending if stimulator is being used
            selStr = "Very well, then which Thief weapon shall I work on?#b";
            if (!stimulator)
                weapon = new Array ("Reef Claw#k - Lv. 30 LUK Dagger#b","Cass#k - Lv. 30 STR Dagger#b","Gephart#k - Lv. 35 LUK Dagger#b","Bazlud#k - Lv. 40 STR Dagger#b","Sai#k - Lv. 50 STR Dagger#b","Shinkita#k - Lv. 50 LUK Dagger#b",
                    "Steel Guards#k - Lv. 30 Claw#b","Bronze Guardian#k - Lv. 35 Claw#b","Steel Avarice#k - Lv. 40 Claw#b","Steel Slain#k - Lv. 50 Claw#b");
            else
                weapon = new Array ("Reef Claw#k - Lv. 30 LUK Dagger#b","Cass#k - Lv. 30 STR Dagger#b","Gephart#k - Lv. 35 LUK Dagger#b","Bazlud#k - Lv. 40 STR Dagger#b","Sai#k - Lv. 50 STR Dagger#b","Shinkita#k - Lv. 50 LUK Dagger#b",
                    "Mithril Guards#k - Lv. 30 Claw#b","Adamantium Guards#k - Lv. 30 Claw#b","Silver Guardian#k - Lv. 35 Claw#b","Dark Guardian#k - Lv. 35 Claw#b","Blood Avarice#k - Lv. 40 Claw#b","Adamantium Avarice#k - Lv. 40 Claw#b",
                    "Dark Avarice#k - Lv. 40 Claw#b","Blood Slain#k - Lv. 50 Claw#b","Sapphire Slain#k - Lv. 50 Claw#b","Dark Slain#k - Lv. 50 Claw#b");
        }
		
        if (selectedType != 0)
        {
            for (var i = 0; i < weapon.length; i++){
                selStr += "\r\n#L" + i + "# " + weapon[i] + "#l";
            }
            cm.sendSimple(selStr);
        }
    }
    else if (status == 2 && mode == 1) {
        selectedItem = selection;
        if (selectedType == 1){ //warrior weapon
            var itemSet = new Array(1302008,1302004,1302009,1302010,1312005,1312006,1312007,1312008,1322014,1322015,1322016,1322017,1402002,1402006,1402007,1402003,1412006,1412004,1412005,1412003,1422001,1422008,1422007,1422005,1432002,1432003,1432005,1432004,1442001,1442003,1442009,1442005);
            var matSet = new Array(new Array(4131000,4011001,4011004,4003000),new Array(4131000,4011006,4011001,4021006,4003000),new Array(4131000,4011006,4011001,4021000,4003000),new Array(4131000,4005000,4021008,4011006,4021003,4003000),
                new Array(4131001,4011001,4021000,4003000),new Array(4131001,4011001,4021000,4011004,4003000),new Array(4131001,4021005,4011001,4021001,4003000),new Array(4131001,4005000,4021008,4011004,4011001,4003000),
                new Array(4131002,4011001,4011000,4003000),new Array(4131002,4011001,4011000,4011003,4003000),new Array(4131002,4011003,4011001,4011004,4003000),new Array(4131002,4005000,4021008,4011006,4011001,4003000),
                new Array(4131003,4011001,4021000,4021004,4003000),new Array(4131003,4011006,4011001,4021004,4003000),new Array(4131003,4021003,4011000,4011001,4003000),new Array(4131003,4005000,4021007,4011006,4011001,4003000),
                new Array(4131004,4021005,4011001,4003001,4003000),new Array(4131004,4011004,4011000,4021003,4003000),new Array(4131004,4011006,4011004,4011001,4003000),new Array(4131004,4005000,4021007,4011006,4021006,4003000),
                new Array(4131005,4011001,4011004,4003000),new Array(4131005,4011001,4011000,4003001,4003000),new Array(4131005,4011001,4011004,4011006,4003000),new Array(4131005,4005000,4021008,4021006,4011006,4003000),
                new Array(4131006,4011000,4011004,4003000),new Array(4131006,4011001,4011002,4021000,4003000),new Array(4131006,4011004,4011001,4011000,4003000),new Array(4131006,4005000,4021008,4011000,4021000,4003000),
                new Array(4131007,4011000,4011002,4003000),new Array(4131007,4011001,4011002,4003000),new Array(4131007,4011006,4011002,4011001,4003000),new Array(4131007,4005000,4021007,4011001,4011002,4003000));
            var matQtySet = new Array(new Array(1,2,2,30),new Array(1,1,5,3,35),new Array(1,3,5,5,40),new Array(1,1,2,4,10,50),
                new Array(1,2,2,30),new Array(1,5,5,3,35),new Array(1,7,5,5,40),new Array(1,1,2,8,10,50),
                new Array(1,2,2,30),new Array(1,5,5,3,35),new Array(1,7,5,5,40),new Array(1,1,2,4,10,50),
                new Array(1,2,1,2,35),new Array(1,1,5,5,40),new Array(1,7,5,5,45),new Array(1,1,2,4,10,55),
                new Array(1,2,2,5,35),new Array(1,5,5,3,40),new Array(1,3,5,5,45),new Array(1,1,2,5,7,55),
                new Array(1,2,3,35),new Array(1,5,5,10,40),new Array(1,5,5,3,45),new Array(1,1,2,7,5,55),
                new Array(1,2,3,40),new Array(1,5,5,3,45),new Array(1,3,5,5,50),new Array(1,1,2,7,5,60),
                new Array(1,2,3,40),new Array(1,5,5,40),new Array(1,3,5,5,50),new Array(1,1,2,7,5,60));
            var costSet = new Array(18000,35000,70000,200000,18000,35000,70000,200000,18000,35000,70000,200000,20000,37000,72000,220000,20000,37000,72000,220000,20000,37000,72000,220000,22000,39000,74000,240000,22000,39000,74000,240000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        else if (selectedType == 2){ //bowman weapon
            var itemSet = new Array(1452005,1452006,1452007,1452008,1462004,1462005,1462006,1462007);
            var matSet = new Array(new Array(4131010,4011001,4011006,4021003,4021006,4003000),new Array(4131010,4011004,4021000,4021004,4003000),new Array(4131010,4021008,4011001,4011006,4003000,4000112),new Array(4131010,4005002,4021008,4011001,4021005,4003000),
                new Array(4131011,4011001,4011005,4021006,4003001,4003000),new Array(4131011,4021008,4011001,4011006,4021006,4003000),new Array(4131011,4021008,4011004,4003001,4003000),new Array(4131011,4021008,4011006,4021006,4003001,4003000));
            var matQtySet = new Array(new Array(1,5,5,3,3,30),new Array(1,7,6,3,35),new Array(1,1,10,3,40,100),new Array(1,1,2,10,6,50),new Array(1,5,5,3,50,15),new Array(1,1,8,4,2,30),new Array(1,2,6,30,30),new Array(1,2,5,3,40,40));
            var costSet = new Array(15000,20000,40000,100000,15000,25000,41000,100000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        else if (selectedType == 3){ //magician weapon
            var itemSet = new Array(1372003,1372001,1372000,1372007,1382002,1382001,1382006);
            var matSet = new Array(new Array(4131008,4011002,4021002,4003000),new Array(4131008,4021006,4011002,4011001,4003000),new Array(4131008,4021006,4021005,4021007,4003003,4003000),new Array(4131008,4011006,4021003,4021007,4021002,4003000),
                new Array(4131009,4021006,4021001,4011001,4003000),new Array(4131009,4011001,4021006,4021001,4021005,4003000),new Array(4131009,4005001,4021008,4011006,4011004,4003000));
            var matQtySet = new Array(new Array(1,3,1,10),new Array(1,5,3,1,15),new Array(1,5,5,1,1,20),new Array(1,4,3,2,1,30),new Array(1,2,1,1,15),new Array(1,8,5,5,5,30),new Array(1,2,2,5,10,40));
            var costSet = new Array(15000,30000,60000,100000,10000,80000,200000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        else if (selectedType == 4){ //thief weapon; claws vary depending if stimulator is being used
            if (!stimulator){
                var itemSet = new Array(1332012,1332009,1332014,1332011,1332016,1332003,1472008,1472011,1472014,1472018);
                var matSet = new Array(new Array(4131012,4011002,4011001,4003000),new Array(4131012,4021005,4011001,4003000),new Array(4131012,4021005,4011001,4011002,4003000),new Array(4131012,4011001,4011006,4021006,4003000),new Array(4131012,4005003,4021008,4011004,4011001,4003000),new Array(4131012,4005003,4021007,4011006,4011001,4003000),
                    new Array(4131013,4011000,4011001,4000021,4003000),new Array(4131013,4011000,4011001,4000021,4003000),new Array(4131013,4011000,4011001,4000021,4003000),new Array(4131013,4011000,4011001,4000030,4003000));
                var matQtySet = new Array(new Array(1,2,3,30),new Array(1,2,3,30),new Array(1,1,5,3,35),new Array(1,7,3,6,40),new Array(1,1,2,7,10,50),new Array(1,1,2,5,10,50),new Array(1,3,2,50,20),new Array(1,4,2,80,25),new Array(1,3,2,100,30),new Array(1,4,2,40,35));
                var costSet = new Array(20000,20000,33000,73000,230000,230000,15000,30000,40000,50000);
            }
            else{
                var itemSet = new Array(1332012,1332009,1332014,1332011,1332016,1332003,1472009,1472010,1472012,1472013,1472015,1472016,1472017,1472019,1472020,1472021);
                var matSet = new Array(new Array(4131012,4011002,4011001,4003000),new Array(4131012,4021005,4011001,4003000),new Array(4131012,4021005,4011001,4011002,4003000),new Array(4131012,4011001,4011006,4021006,4003000),new Array(4131012,4005003,4021008,4011004,4011001,4003000),new Array(4131012,4005003,4021007,4011006,4011001,4003000),
                    new Array(4131013,1472008,4011002),new Array(4131013,1472008,4011003),new Array(4131013,1472011,4011004),new Array(4131013,1472011,4021008),new Array(4131013,1472014,4021000),new Array(4131013,1472014,4011003),new Array(4131013,1472014,4021008),new Array(4131013,1472018,4021000),new Array(4131013,1472018,4021005),
                    new Array(4131013,1472018,4005003,4021008));
                var matQtySet = new Array(new Array(1,2,3,30),new Array(1,2,3,30),new Array(1,1,5,3,35),new Array(1,7,3,6,40),new Array(1,1,2,7,10,50),new Array(1,1,2,5,10,50),new Array(1,1,3),new Array(1,1,3),new Array(1,1,4),new Array(1,1,1),new Array(1,1,5),new Array(1,1,5),new Array(1,1,2),new Array(1,1,6),new Array(1,1,6),new Array(1,1,1,3));
                var costSet = new Array(20000,20000,33000,73000,230000,230000,10000,15000,20000,25000,30000,30000,35000,40000,40000,50000);
            }
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
		
        var prompt = "You want me to make a #t" + item + "#? In that case, I'm going to need specific items from you in order to make it. Make sure you have room in your inventory, though!#b";

        if(stimulator){
            stimID = mats[0] - 998; //stim ID for a weapon = manual ID for weapon - 998
            prompt += "\r\n#i"+stimID+"# 1 #t" + stimID + "#";
        }

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
            cm.sendOk("Verify for a slot in your inventory first.");
            cm.dispose();
            return;
        }
        else if (cm.getMeso() < cost)
        {
            cm.sendOk("I'm afraid my fees are non-negotiable.");
            cm.dispose();
            return;
        }
        else
        {
            if (mats instanceof Array) {
                for(var i = 0; complete && i < mats.length; i++)
                {
                    if (matQty[i] * selection == 1) {
                        if (!cm.haveItem(mats[i]))
                        {
                            complete = false;
                        }
                    }
                    else {
                        if (!cm.haveItem(mats[i],matQty[i] * selection)) complete=false;
                    }
                }
            }
            else {
                if (!cm.haveItem(mats,matQty * selection)) complete=false;
            }
        }
			
        if (stimulator){ //check for stimulator
            if (!cm.haveItem(stimID))
            {
                complete = false;
            }
        }
			
        if (!complete)
            cm.sendOk("Sorry, but you're missing a required item. Possibly a manual? Or one of the ores?");
        else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++){
                    cm.gainItem(mats[i], -matQty[i]);
                }
            }
            else
                cm.gainItem(mats, -matQty);
					
            cm.gainMeso(-cost);
            if (stimulator){ //check for stimulator
                cm.gainItem(stimID, -1);
                var deleted = Math.floor(Math.random() * 10);
                if (deleted != 0)
                {
                    cm.gainItem(item, 1, true, true);
                    cm.sendOk("Heeere you go! What do you think? Marvellous, isn't it?");
                }
                else
                {
                    cm.sendOk("...ACK! My attention wandered, and before I knew it... Uh, sorry, but there's nothing I can do for you now.");
                }
            }
            else //just give basic item
            {
                cm.gainItem(item, 1);
                cm.sendOk("Heeere you go! What do you think? Marvellous, isn't it?");
            }
        }
        cm.dispose();
    }
}