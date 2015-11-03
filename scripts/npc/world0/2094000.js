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

var status = 0;
var minLevel = 55;
var maxLevel = 100;
var minPlayers = 0;
var maxPlayers = 6;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getParty() == null) {
                cm.sendOk("Please come back to me after you've formed a party.");
                cm.dispose();
                return;
            }
            if (!cm.isLeader()) {
                cm.sendSimple("You are not the party leader.");
                cm.dispose();
            } else {
                var party = cm.getParty().getMembers();
                var mapId = cm.getPlayer().getMapId();
                var next = true;
                var levelValid = 0;
                var inMap = 0;
                if (party.size() < minPlayers || party.size() > maxPlayers)
                    next = false;
                else {
                    for (var i = 0; i < party.size() && next; i++) {
                        if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                            levelValid += 1;
                        if (party.get(i).getMapid() == mapId)
                            inMap += 1;
                    }
                    if (levelValid < minPlayers || inMap < minPlayers)
                        next = false;
                }
                if (next) {
                    var em = cm.getEventManager("PiratePQ");
                    if (em == null) {
                        cm.sendOk("PiratePQ does not work.");
                        cm.dispose();
                    }
                    else {
                        em.startInstance(cm.getParty(),cm.getPlayer().getMap());
                        party = cm.getPlayer().getEventInstance().getPlayers();
                    }
                    cm.dispose();
                }
                else {
                    cm.sendOk("Your party is not a party of six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
                    cm.dispose();
                }
            }
        }
        else {
            cm.sendOk("PiratePQ does not exist.");
            cm.dispose();
        }
    }
}
					
					
