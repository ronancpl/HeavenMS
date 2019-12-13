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

/* Claudia
	Amoria Quest Hair Change.

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var mhair_q = Array(30270, 30240, 30020, 30000, 30132, 30192, 30032, 30112, 30162);
var fhair_q = Array(31150, 31250, 31310, 31050, 31050, 31030, 31070, 31091, 31001);
var hairnew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function start() {
    if (cm.isQuestCompleted(8860) && !cm.haveItem(4031528)) {
        cm.sendNext("I've already done your hair once as a trade-for-services, sport. You'll have to snag an EXP Hair coupon from the Cash Shop if you want to change it again!");
        cm.dispose();
    } else
        cm.sendYesNo("Ready for an awesome hairdo? I think you are! Just say the word, and we'll get started!");
}

function action(mode, type, selection) {
    if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
        if (type == 7) {
            cm.sendNext("Ok, I'll give you a minute.");
        }
        
        cm.dispose();
    }
    status++;
    if (status == 1) {
        hairnew = Array();
        if (cm.getPlayer().getGender() == 0)
            for(var i = 0; i < mhair_q.length; i++)
                pushIfItemExists(hairnew, mhair_q[i]);
        else
            for(var j = 0; j < fhair_q.length; j++)
                pushIfItemExists(hairnew, fhair_q[j]);
        cm.sendNext("Here we go!");
    } else {
        if (cm.haveItem(4031528)) {
            cm.gainItem(4031528, -1);
            cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
            cm.sendNextPrev("Not bad, if I do say so myself! I knew those books I studied would come in handy...");
            cm.dispose();
        } else {
            cm.sendNext("Hmmm...are you sure you have our designated free coupon? Sorry but no haircut without it.");
            cm.dispose();
        }
    }
}
