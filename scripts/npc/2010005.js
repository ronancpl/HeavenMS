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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Shuri the Tour Guide - Orbis (200000000)
-- By ---------------------------------------------------------------------------------------------
	Information & Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version
---------------------------------------------------------------------------------------------------
**/

var pay = 2000;
var ticket = 4031134;
var msg;
var check;

var status = 0;

function start() {
    cm.sendSimple("Have you heard of the beach with a spectacular view of the ocean called #b#m110000000##k, located a little far from #m"+cm.getPlayer().getMapId()+"#? I can take you there right now for either #b"+pay+" mesos#k, or if you have #b#t"+ticket+"##k with you, in which case you'll be in for free.\r\n\r\n#L0##bI'll pay "+pay+" mesos.#k#l\r\n#L1##bI have #t"+ticket+"##k#l\r\n#L2##bWhat is #t"+ticket+"#?#k#l");
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 1) {
            cm.sendNext("You must have some business to take care of here. You must be tired from all that travelling and hunting. Go take some rest, and if you feel like changing your mind, then come talk to me.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (selection == 0 || selection == 1) {
                check = selection;
                if (selection == 0)
                    msg = "You want to pay #b"+pay+" mesos#k and leave for #m110000000#?";
                else if (selection == 1)
                    msg = "So you have #b#t"+ticket+"##k? You can always head over to #m110000000# with that.";
                cm.sendYesNo(msg+" Okay!! Please beware that you may be running into some monsters around there though, so make sure not to get caught off-guard. Okay, would you like to head over to #m110000000# right now?");
            } else if (selection == 2) {
                cm.sendNext("You must be curious about #b#t"+ticket+"##k. Yeah, I can see that. #t"+ticket+"# is an item where as long as you have in possession, you may make your way to #m110000000# for free. It's such a rare item that even we had to buy those, but unfortunately I lost mine a few weeks ago during a long weekend.");
                status = 3;
            }
        } else if (status == 2) {
            if (check == 0) {
                if (cm.getMeso() < pay) {
                    cm.sendOk("I think you're lacking mesos. There are many ways to gather up some money, you know, like ... selling your armor ... defeating the monsters ... doing quests ... you know what I'm talking about.");
                    cm.dispose();
                } else {
                    cm.gainMeso(-pay);
                    access = true;
                }
            } else if (check == 1) {
                if (!cm.haveItem(ticket)) {
                    cm.sendOk("Hmmm, so where exactly is #b#t"+ticket+"##k?? Are you sure you have them? Please double-check.");
                    cm.dispose();
                } else
                    access = true;
            }
            if (access) {
                cm.getPlayer().saveLocation("FLORINA");
                cm.warp(110000000, "st00");
                cm.dispose();
            }
        } else if (status == 3) 
            cm.sendNext("You must be curious about #b#t"+ticket+"##k. Yeah, I can see that. #t"+ticket+"# is an item where as long as you have in possession, you may make your way to #m110000000# for free. It's such a rare item that even we had to buy those, but unfortunately I lost mine a few weeks ago during a long weekend.");
        else if (status == 4)
            cm.sendPrev("I came back without it, and it just feels awful not having it. Hopefully someone picked it up and put it somewhere safe. Anyway this is my story and who knows, you may be able to pick it up and put it to good use. If you have any questions, feel free to ask");
        else if (status == 5)
            cm.dispose();
        
    }
}