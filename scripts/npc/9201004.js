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
/* Amos the Wise
	Amoria (680000000)
	Wedding info.
 */

importPackage(Packages.net.server.channel.handlers);

var status;

var rings = [1112806, 1112803, 1112807, 1112809];
var divorceFee = 500000;
var ringObj;

function getWeddingRingItemId(player) {
    for (var i = 0; i < rings.length; i++) {
        if (player.haveItemWithId(rings[i], false)) {
            return rings[i];
        }
    }
    
    return null;
}

function hasEquippedWeddingRing(player) {
    for (var i = 0; i < rings.length; i++) {
        if (player.haveItemEquipped(rings[i])) {
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
            var questionStr = ["How can I engage someone?", "How can I marry?", "How can I divorce?"]
            
            if(!(!cm.getPlayer().isMarried() && getWeddingRingItemId(cm.getPlayer()))) questionStr.push("I want a divorce...");
            else questionStr.push("I wanna remove my old wedding ring...");
            
            cm.sendSimple("Hello, welcome to #bAmoria#k, a beautiful land where maplers can find love and, if inspired enough, even marry. Do you have any questions about Amoria? Talk it to me.#b\r\n\r\n" + generateSelectionMenu(questionStr));
        } else if(status == 1) {
            switch(selection) {
                case 0:
                    cm.sendOk("The #bengagement process#k is as straightforward as it can be. Firstly one must start a prequest from the #bring maker, #p9201000##k. They must gather #b#t4031367#'s#k thoughout the Maple world.\r\nFrom the completion of the quest, the player will gain an engagement ring. With that in hand, declare yourself to someone you become fond of. Then, hope the person accepts your proposal.");
                    cm.dispose();
                    break;
                    
                case 1:
                    cm.sendOk("For the #bmarriage process#k you must be already engaged. The loving couple must choose a venue they want to hold their marriage. Amoria offers two: the #rCathedral#k and the #rChapel#k.\r\nThen, one of the partners must buy a #bWedding Ticket#k, available through the Cash Shop, and book their ceremony with the Wedding Assistant. Each partner will receive #rguest tickets#k to be distributed to their acquaintances.");
                    cm.dispose();
                    break;
                    
                case 2:
                    cm.sendOk("Unfortunately the love of long may fizzle someday. Well, I hope that's not the case for any loving couple that once married, is marrying today or is going to do so tomorrow. But, if that ever happens, I myself will be at service to make a safe divorce, by the fee of #r" + divorceFee + "#k mesos.");
                    cm.dispose();
                    break;
                    
                case 3:
                    ringObj = cm.getPlayer().getMarriageRing();
                    if(ringObj == null) {
                        var itemid = getWeddingRingItemId(cm.getPlayer());
                        
                        if(itemid != null) {
                            cm.sendOk("There you go, I've removed your old wedding ring.");
                            cm.gainItem(itemid, -1);
                        } else if(hasEquippedWeddingRing(cm.getPlayer())) {
                            cm.sendOk("If you want your old wedding ring removed, please unequip it before talking to me.");
                        } else {
                            cm.sendOk("You're not married to require a divorce from it.");
                        }
                        
                        cm.dispose();
                        return;
                    }
                    
                    cm.sendYesNo("So, you want to divorce from your partner? Be sure, this process #bcannot be rollbacked#k by any means, it's supposed to be an ultimatum from which your ring will be destroyed as consequence. That said, do you #rreally want to divorce#k?");
                    break;
            }
        } else if(status == 2) {
            if(cm.getMeso() < divorceFee) {
                cm.sendOk("You don't have the required amount of #r" + divorceFee + " mesos#k for the divorce fee.");
                cm.dispose();
                return;
            } else if(ringObj.equipped()) {
                cm.sendOk("Please unequip your ring before trying to divorce.");
                cm.dispose();
                return;
            }
            
            cm.gainMeso(-divorceFee);
            RingActionHandler.breakMarriageRing(cm.getPlayer(), ringObj.getItemId());
            cm.gainItem(ringObj.getItemId(), -1);
            
            cm.sendOk("You have divorced from your partner.");
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