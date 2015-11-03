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
var status;
var choice;
var guildName;
var partymembers;

function start() {
    partymembers = cm.getPartyMembers();
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else {
        cm.dispose();
        return;
    }
    if (status == 0)
        cm.sendSimple("Hello there! I'm #bLenario#k\r\n#b#L0#Can you please tell me what Guild Union is all about?#l\r\n#L1#How do I make a Guild Union?#l\r\n#L2#I want to make a Guild Union.#l\r\n#L3#I want to add more guilds for the Guild Union.#l\r\n#L4#I want to break up the Guild Union.#l");
    else if (status == 1) {
        choice = selection;
        if (selection == 0) {
            cm.sendNext("Guild Union is just as it says, a union of a number of guilds to form a super group. I am in charge of managing these Guild Unions.");
            cm.dispose();
        } else if (selection == 1) {
            cm.sendNext("To make a Guild Union, 2 Guild Masters need to be in a party. The leader of this party will be assigned as the Guild Union Master.");
            cm.dispose();
        } else if(selection == 2) {
                cm.sendYesNo("Oh, are you interested in forming a Guild Union?");
        } else if (selection == 3) {
            var rank = cm.getPlayer().getMGC().getAllianceRank();
            if (rank == 1)
                cm.sendOk("Not done yet"); //ExpandGuild Text
            else {
                cm.sendNext("Only the Guild Union Master can expand the number of guilds in the Union.");
                cm.dispose();
            }
        } else if(selection == 4) {
            var rank = cm.getPlayer().getMGC().getAllianceRank();
            if (rank == 1)
                cm.sendYesNo("Are you sure you want to disband your Guild Union?");
            else {
                cm.sendNext("Only the Guild Union Master may disband the Guild Union.");
                cm.dispose();
            }
        }
    } else if(status == 2) {
        if (choice == 2) {
            cm.sendGetText("Now please enter the name of your new Guild Union. (max. 12 letters)");
        } else if (choice == 4) {
            if (cm.getPlayer().getGuild() == null) {
                cm.sendNext("You cannot disband a non-existant Guild Union.");
                cm.dispose();
            } else if (cm.getPlayer().getGuild().getAllianceId() <= 0) {
                cm.sendNext("You cannot disband a non-existant Guild Union.");
                cm.dispose();
            } else {
                cm.disbandAlliance(cm.getClient(), cm.getPlayer().getGuild().getAllianceId());
                cm.sendOk("Your Guild Union has been disbanded.");
                cm.dispose();
            }
        }
    } else if (status == 3) {
        guildName = cm.getText();
        cm.sendYesNo("Will "+ guildName + " be the name of your Guild Union?");
    } else if (status == 4) {
        if (!cm.canBeUsedAllianceName(guildName)) {
            cm.sendNext("This name is unavailable, please choose another one"); //Not real text
            status = 1;
            choice = 2;
        } else {
            if (cm.createAlliance(partymembers.get(0), partymembers.get(1), guildName) == null)
                cm.sendOk("An unknown system error has occured.");
            else
                cm.sendOk("You have successfully formed a Guild Union.");
            cm.dispose();
        }
    }
}