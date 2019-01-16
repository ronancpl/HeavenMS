/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2017 RonanLana

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
/* Moony
	Amoria (680000000)
	Engagement ring NPC.
 */

var status;
var state;

var item;
var mats;
var matQty;
var cost;

var options;

function hasEngagementBox(player) {
    for(var i = 2240000; i <= 2240003; i++) {
        if(player.haveItem(i)) {
            return true;
        }
    }
    
    return false;
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if(status == 0) {
            options = ["I want to make a ring.", "I want to discard the ring box I have."];
            cm.sendSimple("I'm #p9201000#, the #bengagement ring maker#k. How can I help you?\r\n\r\n#b" + generateSelectionMenu(options));
        } else if(status == 1) {
            if(selection == 0) {
                if(!cm.isQuestCompleted(100400)) {
                    if(!cm.isQuestStarted(100400)) {
                        state = 0;
                        cm.sendNext("So you want to make a engagement ring, huh? Very well, I can provide one after you receive #rblessings#k from your #b#p9201003##k.");
                    } else {
                        cm.sendOk("Take the blessings from your #b#p9201003##k before trying to craft an engagement ring. They must be waiting for you at home, beyond #rHenesys hunting grounds#k.");
                        cm.dispose();
                    }
                } else {
                    if(hasEngagementBox(cm.getPlayer())) {
                        cm.sendOk("Sorry, you already have an engagement box. I cannot provide you more than one box per time.");
                        cm.dispose();
                        return;
                    }
                    if(cm.getPlayer().getGender() != 0) {
                        cm.sendOk("Sorry, but the ring box is currently available only for males.");
                        cm.dispose();
                        return;
                    }

                    state = 1;
                    options = ["Moonstone","Star Gem","Golden Heart", "Silver Swan"];
                    var selStr = "So, what kind of engagement ring you want me to craft?\r\n\r\n#b" + generateSelectionMenu(options);
                    cm.sendSimple(selStr);
                }
            } else {
                if(hasEngagementBox(cm.getPlayer())) {
                    for(var i = 2240000; i <= 2240003; i++) {
                        cm.removeAll(i);
                    }
                    
                    cm.sendOk("Your ring box has been discarded.");
                } else {
                    cm.sendOk("You have no ring box to discard.");
                }
                
                cm.dispose();
            }
        } else if(status == 2) {
            if(state == 0) {
                cm.sendOk("Where do they live, you ask? My, it goes way back... you see, I'm a friend of theirs, and I was the one who crafted and personally delivered their engagement ring. They live beyond #rHenesys Hunting Grounds#k, I'm sure you know where it is.");
                cm.startQuest(100400);
                cm.dispose();
            } else {
                var itemSet = new Array(2240000,2240001,2240002,2240003);
                var matSet = new Array(new Array(4011007,4021007),new Array(4021009,4021007),new Array(4011006,4021007),new Array(4011004,4021007));
                var matQtySet = new Array(new Array(1,1),new Array(1,1),new Array(1,1),new Array(1,1));
                var costSet = new Array (30000,20000,10000,5000);

                item = itemSet[selection];
                mats = matSet[selection];
                matQty = matQtySet[selection];
                cost = costSet[selection];

                var prompt = "Then I'm going to craft you a #b#t" + item + "##k, is that right?";
                prompt += " In that case, I'm going to need specific items from you in order to make it. Make sure you have room in your inventory, though!#b";

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
        } else if(status == 3) {
            var complete = true;
            var recvItem = item, recvQty = 1, qty = 1;

            if(!cm.canHold(recvItem, recvQty)) {
                cm.sendOk("Check your inventory for a free slot first.");
                cm.dispose();
                return;
            }
            else if (cm.getMeso() < cost * qty)
            {
                cm.sendOk("I'm sorry but there's a fee for my services. Please bring me the right amount of mesos here before trying to forge a ring.");
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
                cm.sendOk("Hm, it seems you're lacking some ingredients for the engagement ring. Please provide them first, will you?");
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
                cm.sendOk("All done, the engagement ring came out just right. I wish you a happy engagement.");
            }
            cm.dispose();
        }
    }
}

function generateSelectionMenu(array) {
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "#" + array[i] + "#l\r\n";
    }
    return menu;
}