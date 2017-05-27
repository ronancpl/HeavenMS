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

var allianceCost = 2000000;
var increaseCost = 1000000;
var allianceLimit = 5;

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
    if (status == 0) {
        if(cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
            cm.sendNext("Hello there! I'm #bLenario#k. Just guild masters can attempt to form guild unions.");
            cm.dispose();
            return;
        }
        
        cm.sendSimple("Hello there! I'm #bLenario#k.\r\n#b#L0#Can you please tell me what Guild Union is all about?#l\r\n#L1#How do I make a Guild Union?#l\r\n#L2#I want to make a Guild Union.#l\r\n#L3#I want to add more guilds for the Guild Union.#l\r\n#L4#I want to break up the Guild Union.#l");
    }
    else if (status == 1) {
        choice = selection;
        if (selection == 0) {
            cm.sendNext("Guild Union is just as it says, a union of a number of guilds to form a super group. I am in charge of managing these Guild Unions.");
            cm.dispose();
        } else if (selection == 1) {
            cm.sendNext("To make a Guild Union, two and only two Guild Masters need to be in a party and both must be present on this room on the same channel. The leader of this party will be assigned as the Guild Union Master.");
            cm.dispose();
        } else if(selection == 2) {
            if(!cm.isLeader()) {
                cm.sendNext("If you want to form a guild union, please tell your party leader to talk to me. He/She will be assigned as the Leader of the Guild Union.");
                cm.dispose();
                return;
            }
            if(cm.getPlayer().getGuild().getAllianceId() > 0) {
                cm.sendOk("You can not create a Guild Union while your guild is already registered in another.");
                cm.dispose();
                return;
            }
            
            cm.sendYesNo("Oh, are you interested in forming a Guild Union? The current fee for this operation is #b" + allianceCost + " mesos#k.");
        } else if (selection == 3) {
            if(cm.getPlayer().getMGC() == null) {
                cm.sendOk("You can not expand a Guild Union if you don't own one.");
                cm.dispose();
                return;
            }
            
            var rank = cm.getPlayer().getMGC().getAllianceRank();
            if (rank == 1)
                cm.sendYesNo("Do you want to increase your Alliance by one guild slot? The fee for this procedure is #b" + increaseCost + " mesos#k.");
            else {
                cm.sendNext("Only the Guild Union Master can expand the number of guilds in the Union.");
                cm.dispose();
            }
        } else if(selection == 4) {
            if(cm.getPlayer().getMGC() == null) {
                cm.sendOk("You can not disband a Guild Union if you don't own one.");
                cm.dispose();
                return;
            }
            
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
            if(cm.getMeso() < allianceCost) {
                cm.sendOk("You don't have enough mesos for this request.");
                cm.dispose();
                return;
            }
            cm.sendGetText("Now please enter the name of your new Guild Union. (max. 12 letters)");
        } else if (choice == 3) {
            if(cm.getAllianceCapacity() == allianceLimit) {
                cm.sendOk("Your alliance already reached the maximum capacity for guilds.");
                cm.dispose();
                return;
            }
            if(cm.getMeso() < increaseCost) {
                cm.sendOk("You don't have enough mesos for this request.");
                cm.dispose();
                return;
            }
            
            cm.upgradeAlliance();
            cm.gainMeso(-increaseCost);
            cm.sendOk("Your alliance can now accept one more guild.");
            cm.dispose();
        } else if (choice == 4) {
            if (cm.getPlayer().getGuild() == null || cm.getPlayer().getGuild().getAllianceId() <= 0) {
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
        cm.sendYesNo("Will '"+ guildName + "' be the name of your Guild Union?");
    } else if (status == 4) {
        if (!cm.canBeUsedAllianceName(guildName)) {
            cm.sendNext("This name is unavailable, please choose another one."); //Not real text
            status = 1;
            choice = 2;
        } else {
            if (cm.createAlliance(guildName) == null)
                cm.sendOk("Please check if you and the other one guild leader in your party are both here on this room right now, and make sure both guilds are currently unregistered on unions. No other guild leaders should be present with you 2 on this process.");
            else {
                cm.gainMeso(-allianceCost);
                cm.sendOk("You have successfully formed a Guild Union.");
            }
            cm.dispose();
        }
    }
}