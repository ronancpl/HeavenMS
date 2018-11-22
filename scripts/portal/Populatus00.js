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

/* @author RonanLana */

function enter(pi) {
    if (!((pi.isQuestStarted(6361) && pi.haveItem(4031870, 1)) || (pi.isQuestCompleted(6361) && !pi.isQuestCompleted(6363)))) {
        var em = pi.getEventManager("PapulatusBattle");

        if (pi.getParty() == null) {
            pi.playerMessage(5, "You are currently not in a party, create one to attempt the boss.");
            return false;
        } else if(!pi.isLeader()) {
            pi.playerMessage(5, "Your party leader must enter the portal to start the battle.");
            return false;
        } else {
            var eli = em.getEligibleParty(pi.getParty());
            if(eli.size() > 0) {
                if(!em.startInstance(pi.getParty(), pi.getPlayer().getMap(), 1)) {
                    pi.playerMessage(5, "The battle against the boss has already begun, so you may not enter this place yet.");
                    return false;
                }
            }
            else {  //this should never appear
                pi.playerMessage(5, "You cannot start this battle yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. If you're having trouble finding party members, try Party Search.");
                return false;
            }

            pi.playPortalSound();
            return true;
        }
    } else {
        pi.playPortalSound();
        pi.warp(922020300);
        return true;
    }
}