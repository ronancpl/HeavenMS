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
/* Author: Xterminator
	NPC Name: 		Pison
	Map(s): 		Victoria Road : Lith Harbor (104000000)
	Description: 		Florina Beach Tour Guide
 */
var status = 0;

function start() {
    cm.sendSimple("Have you heard of the beach with a spectacular view of the ocean called #bFlorina Beach#k, located near Lith Harbor? I can take you there right now for either #b1500 mesos#k, or if you have a #bVIP Ticket to Florina Beach#k with you, in which case you'll be there for free.\r\n\r\n#L0##b I'll pay 1500 mesos.#l\r\n#L1# I have a VIP Ticket to Florina Beach.#l\r\n#L2# What is a VIP Ticket to Florina Beach#k?#l");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1)
        if((mode == 0 && type == 1) || mode == -1 || (mode == 0 && status == 1)){
            if(type == 1)
                cm.sendNext("You must have some business to take care of here. You must be tired from all that traveling and hunting. Go take some rest, and if you feel like changing your mind, then come talk to me.");
            cm.dispose();
            return;
        } else
            status -= 2;
    if (selection == 0)
        status++;
    if(status == 1){
        if(selection == 1)
            cm.sendYesNo("So you have a #bVIP Ticket to Florina Beach#k? You can always head over to Florina Beach with that. Alright then, but just be aware that you may be running into some monsters there too. Okay, would you like to head over to Florina Beach right now?");
        else if (selection == 2)
            cm.sendNext("You must be curious about a #bVIP Ticket to Florina Beach#k. Haha, that's very understandable. A VIP Ticket to Florina Beach is an item where as long as you have in possession, you may make your way to Florina Beach for free. It's such a rare item that even we had to buy those, but unfortunately I lost mine a few weeks ago during my precious summer break.");
    } else if (status == 2){
        if(type != 1 && selection != 0) {
            cm.sendNextPrev("I came back without it, and it just feels awful not having it. Hopefully someone picked it up and put it somewhere safe. Anyway, this is my story and who knows, you may be able to pick it up and put it to good use. If you have any questions, feel free to ask.");
			cm.dispose();
		} else{
            if (cm.getMeso() < 1500 && selection == 0)
                cm.sendNext("I think you're lacking mesos. There are many ways to gather up some money, you know, like... selling your armor... defeating monsters... doing quests... you know what I'm talking about.");
            else if(!cm.haveItem(4031134) && selection != 0){
                cm.sendNext("Hmmm, so where exactly is your #bVIP Ticket to Florina\r\nBeach#k? Are you sure you have one? Please double-check.");
            }else{
                if(selection == 0)
                    cm.gainMeso(-1500);
                cm.getPlayer().saveLocation("FLORINA");
                cm.warp(110000000, "st00");
            }
            cm.dispose();
        }
    }
}