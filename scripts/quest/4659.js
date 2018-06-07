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
/* 	Author: Moogra
	NPC Name: 		?????????????
	Map(s): 		New Leaf City
	Description: 		Quest - Pet Evolution
*/
importPackage(Packages.client.inventory.manipulator);
importPackage(Packages.server);

var status = -1;

function start(mode, type, selection) {
//nothing here?
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
            status -= 2;
        else{
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        if(qm.getMeso() < 10000) {
            qm.sendOk("Hey! I need #b10,000 mesos#k to do your pet's evolution!");
            qm.dispose();
            return;
        }
        qm.sendNext("Great job on finding your evolution materials. I will now give you a robot.");
    }
    else if (status == 1) {
        if (qm.isQuestCompleted(4659))
            qm.dropMessage(1, "how did this get here?");
        else if (qm.canHold(5000048)){
            var pet = 0;
            var after;
            var i;
            
            for(i = 0; i < 3; i++) {
                if(qm.getPlayer().getPet(i) != null && qm.getPlayer().getPet(i).getItemId() == 5000048) {
                    pet = qm.getPlayer().getPet(i);
                    break;
                }
            }
            if(i == 3) {
                qm.getPlayer().message("Pet could not be evolved.");
                qm.dispose();
                return;
            }
            
            var closeness = pet.getCloseness();
            if(closeness < 1642) {
                qm.sendOk("It looks like your pet is not grown enough to be evolved yet. Train it a bit more, util it reaches #blevel 15#k.");
                qm.dispose();
                return;
            }
            
            var level = pet.getLevel();
            var fullness = pet.getFullness();
            var name = pet.getName();

            var rand = 1 + Math.floor(Math.random() * 9);
            
            if (rand >= 1 && rand <= 2) {
		after = 5000049;
            } else if (rand >= 3 && rand <= 4) {
		after = 5000050;
            } else if (rand >= 5 && rand <= 6) {
		after = 5000051;
            } else if (rand >= 7 && rand <= 8) {
		after = 5000052;
            } else if (rand == 9) {
		after = 5000053;
            } else {
		qm.sendOk("Something wrong. Try again.");
            	qm.dispose();
                return;
	    }
            
            //qm.gainItem(5000048 + rand);
            qm.gainItem(5380000, -1);
            qm.gainMeso(-10000);
            
            qm.evolvePet(i, after);
//            var petId = MaplePet.createPet(rand + 5000049, level, closeness, fullness);
//            if (petId == -1) return;
//            MapleInventoryManipulator.addById(qm.getClient(), rand+5000049, 1, "", petId);
        } else
            qm.dropMessage(1,"Your inventory is full");
        qm.dispose();
    }
}