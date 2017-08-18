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
/* Fiona
        Phantom Forest - Dead Man's Gorge (610010004)
        
        Refining NPC: 
        * Raven Claw items
        * Raven Claw upgrades
        
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;

function start() {
    if (cm.getQuestStatus(8225) != 2) {
        cm.sendOk("Step aside, novice, we're doing business here.");
        cm.dispose();
        return;
    }
    
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
        var selStr = "Hey, partner! If you have the right goods, I can turn it into something very nice...#b"
        var options = new Array("Weapon Forging","Weapon Upgrading");
        for (var i = 0; i < options.length; i++)
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        cm.sendSimple(selStr);
    }
    else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0){ //weapon forge
            var selStr = "So, what kind of weapon would you like me to forge?#b";
            var weapon = new Array ("#t2070018#","#t1382060#","#t1442068#","#t1452060#");
            for (var i = 0; i < weapon.length; i++)
                selStr += "\r\n#L" + i + "# " + weapon[i] + "#l";
            
            cm.sendSimple(selStr);
        }
        else if (selectedType == 1){ //weapon upgrade
            var selStr = "An upgraded weapon? Of course, but note that upgrades won't carry over to the new item... #b";
            var weapon = new Array ("#t1472074#","#t1472073#","#t1472075#","#t1332079#","#t1332078#","#t1332080#","#t1462054#","#t1462053#","#t1462055#","#t1402050#","#t1402049#","#t1402051#");
            for (var i = 0; i < weapon.length; i++)
                selStr += "\r\n#L" + i + "# " + weapon[i] + "#l";
            
            cm.sendSimple(selStr);
        }
        
    }
    else if (status == 2 && mode == 1) {
        qty = 1;
        selectedItem = selection;

        if (selectedType == 0){ // weapon forge
            var itemSet = new Array(2070018,1382060,1442068,1452060);
            var matSet = new Array(new Array(4032015, 4032016, 4032017, 4021008, 4032005), new Array(4032016,4032017,4032004,4032005,4032012,4005001), new Array(4032015,4032017,4032004,4032005,4032012,4005000), new Array(4032015,4032016,4032004,4032005,4032012,4005002));
            var matQtySet = new Array(new Array(1,1,1,100,30), new Array(1,1,400,10,30,4), new Array(1,1,500,40,20,4), new Array(1,1,300,75,10,4));
            var costSet = new Array(70000,70000,70000,70000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        else if (selectedType == 1){ // weapon upgrade
            var itemSet = new Array(1472074,1472073,1472075,1332079,1332078,1332080,1462054,1462053,1462055,1402050,1402049,1402051);
            var matSet = new Array(new Array(4032017,4005001,4021008), new Array(4032015,4005002,4021008), new Array(4032016,4005000,4021008),new Array(4032017,4005001,4021008), new Array(4032015,4005002,4021008), new Array(4032016,4005000,4021008), new Array(4032017,4005001,4021008), new Array(4032015,4005002,4021008), new Array(4032016,4005000,4021008), new Array(4032017,4005001,4021008), new Array(4032015,4005002,4021008), new Array(4032016,4005000,4021008));
            var matQtySet = new Array(new Array(1,10,20),new Array(1,10,30),new Array(1,5,20),new Array(1,10,20),new Array(1,10,30),new Array(1,5,20),new Array(1,10,20),new Array(1,10,30),new Array(1,5,20),new Array(1,10,20),new Array(1,10,30),new Array(1,5,20));
            var costSet = new Array (75000,50000,50000,75000,50000,50000,75000,50000,50000,75000,50000,50000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
                
        var prompt = "You want me to make ";
        if (qty == 1)
            prompt += "a #t" + item + "#?";
        else
            prompt += qty + " #t" + item + "#?";
                        
        prompt += " In that case, I'm going to need specific items from you in order to make it. Make sure you have room in your inventory, though!#b";
                
        if (mats instanceof Array){
            for(var i = 0; i < mats.length; i++){
                prompt += "\r\n#i"+mats[i]+"# " + matQty[i] * qty + " #t" + mats[i] + "#";
            }
        }
        else {
            prompt += "\r\n#i"+mats+"# " + matQty * qty + " #t" + mats + "#";
        }
                
        if (cost > 0)
            prompt += "\r\n#i4031138# " + cost * qty + " meso";
                
        cm.sendYesNo(prompt);
    }
    else if (status == 3 && mode == 1) {
        var complete = true;
        var recvItem = item, recvQty;
        
        recvQty = qty;
        
        if(!cm.canHold(recvItem, recvQty)) {
            cm.sendOk("Check your inventory for a free slot first.");
            cm.dispose();
            return;
        }
        else if (cm.getMeso() < cost * qty)
        {
            cm.sendOk("I am afraid you don't have enough to pay me, partner. Please check this out first, ok?");
            cm.dispose();
            return;
        }
        else
        {
            if (mats instanceof Array) {
                for(var i = 0; complete && i < mats.length; i++)
                    if (!cm.haveItem(mats[i], matQty[i] * qty))
                        complete = false;
            }
            else if (!cm.haveItem(mats, matQty * qty))
                complete = false;
        }
                        
        if (!complete)
            cm.sendOk("Hey, I need those items to craft properly, you know?");
        else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++){
                    cm.gainItem(mats[i], -matQty[i] * qty);
                }
            }
            else
                cm.gainItem(mats, -matQty * qty);
            if (cost > 0)
                cm.gainMeso(-cost * qty);
            
            cm.gainItem(recvItem, recvQty);
            cm.sendOk("All done. If you need anything else... Well, I'm not going anywhere.");
        }
        cm.dispose();
    }
}