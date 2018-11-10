/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
/* Coco
        Refining NPC: 
	* Chaos scroll SYNTHETIZER (rofl)
        * 
        * @author RonanLana
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;
var equip;
var last_use; //last item is a use item

function start() {
    cm.getPlayer().setCS(true);
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else {
        cm.sendOk("Oh, ok... Talk back to us when you want to make business.");
        cm.dispose();
        return;
    }

    if (status == 0) {
        if (!Packages.constants.ServerConstants.USE_ENABLE_CUSTOM_NPC_SCRIPT) {
            cm.sendOk("Hi, I'm #b#p" + cm.getNpc() + "##k.");
            cm.dispose();
            return;
        }
        
        var selStr = "Hey traveler! Come, come closer... We offer a #bhuge opportunity of business#k to you. If you want to know what it is, keep listening...";
        cm.sendNext(selStr);
    }
    else if (status == 1) {
	var selStr = "We've got here the knowledge to synthetize the mighty #b#t2049100##k! Of course, making one is not an easy task... But worry not! Just gather some material to me and a fee of #b1,200,000 mesos#k for our services to #bobtain it#k. You still want to do it?";
        cm.sendYesNo(selStr);
    }

    else if (status == 2) {
        //selectedItem = selection;
        selectedItem = 0;

        var itemSet = new Array(2049100, 7777777);
        var matSet = new Array(new Array(4031203,4001356,4000136,4000082,4001126,4080100,4000021,4003005));
        var matQtySet = new Array(new Array(100,60,40,80,10,8,200,120));
        var costSet = new Array(1200000, 7777777);
        item = itemSet[selectedItem];
        mats = matSet[selectedItem];
        matQty = matQtySet[selectedItem];
        cost = costSet[selectedItem];
                
        var prompt = "So, you want us to make some #t" + item + "#? In that case, how many do you want us to make?";
        cm.sendGetNumber(prompt,1,1,100)
    }
        
    else if (status == 3) {
        qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        last_use = false;
                
        var prompt = "You want us to make ";
        if (qty == 1)
            prompt += "a #t" + item + "#?";
        else
            prompt += qty + " #t" + item + "#?";
                        
        prompt += " In that case, we're going to need specific items from you in order to make it. Make sure you have room in your inventory, though!#b";
                
        if (mats instanceof Array){
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i"+mats[i]+"# " + matQty[i] * qty + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i"+mats+"# " + matQty * qty + " #t" + mats + "#";
        }
                
        if (cost > 0) {
            prompt += "\r\n#i4031138# " + cost * qty + " meso";
        }
        cm.sendYesNo(prompt);
    }
    
    else if (status == 4) {
        var complete = true;
                
        if (cm.getMeso() < cost * qty) {
            cm.sendOk("Come on! We're not here doing you a favor! We all need money to live properly, so bring the cash so we make deal and start the synthesis.");
        }
        else if(!cm.canHold(item, qty)) {
            cm.sendOk("You didn't check if you got a slot to spare on your inventory before our business, no?");
        }
        else {
            if (mats instanceof Array) {
                for (var i = 0; complete && i < mats.length; i++) {
                    if (matQty[i] * qty == 1) {
                        complete = cm.haveItem(mats[i]);
                    } else {
                        complete = cm.haveItem(mats[i], matQty[i] * qty);
                    }
                }
            } else {
                complete = cm.haveItem(mats, matQty * qty);
            }
            
            if (!complete)
                cm.sendOk("You kidding, right? We won't be able to start the process without all the ingredients at hands. Go get all of them and then talk to us!");
            else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++){
                        cm.gainItem(mats[i], -matQty[i] * qty);
                    }
                } else {
                    cm.gainItem(mats, -matQty * qty);
                }
                cm.gainMeso(-cost * qty);
                cm.gainItem(item, qty);
                cm.sendOk("Wow... can't believe it worked! To think for a moment that it could f... Ahem. Of course it worked, all work of ours are very efficient! Nice doing business with you.");
            }
        }
        cm.dispose();
    }
}