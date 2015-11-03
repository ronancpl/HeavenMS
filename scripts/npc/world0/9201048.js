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
/* @Author Jvlaple */

var minLevel = 40;
var maxLevel = 255;
var minPlayers = 0;
var maxPlayers = 6;
//var minMarried = 6;
//var minGirls = 1;
//var minBoys = 1;

function start() {
    if (cm.getParty() == null) {
        cm.sendOk("Please come back to me after you've formed a party.");
        cm.dispose();
        return;
    }
    if (!cm.isLeader()) {
        cm.sendOk("You are not the party leader.");
        cm.dispose();
    } else {
        var party = cm.getParty().getMembers();
        var next = true;
        var levelValid = 0;
        var inMap = 0;
        if (party.size() < minPlayers || party.size() > maxPlayers)
            next = false;
        else {
            for (var i = 0; i < party.size() && next; i++) {
                if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                    levelValid++;
                if (party.get(i).getMapid() == cm.getPlayer().getMapId())
                    inMap++;
            }
            if (levelValid < minPlayers || inMap < minPlayers)
                next = false;
        }
        if (next) {
            var em = cm.getEventManager("AmoriaPQ");
            if (em == null)
                cm.dispose();
            else
                em.startInstance(cm.getParty(),cm.getPlayer().getMap());
            cm.dispose();
        }
        else {
            cm.sendOk("Your party is not a party of six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
            cm.dispose();
        }
    }
}