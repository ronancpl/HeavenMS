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
/*
 * @author AngelSL
 * 
 * 4th Job Rush Quest.
 * Based on Kerning City PQ script by Stereo
 */

var exitMap;
var instanceId;
var minPlayers = 3;

function init() {
    instanceId = 1;
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    exitMap = em.getChannelServer().getMapFactory().getMap(105090700); // <exit>
    var instanceName = "4jrush" + instanceId;
    var eim = em.newInstance(instanceName);
    var mf = eim.getMapFactory();
    instanceId++;
    var map = mf.getMap(910500100);
    map.addMapTimer(20*60);
    em.schedule("timeOut", 20 * 60000);
    return eim;
}

function afterSetup(eim) {}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(910500100);
    player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        if (party.get(i).equals(player)) {
            removePlayer(eim, player);
        }
        else {
            playerExit(eim, party.get(i));
        }
    }
    eim.dispose();
}

function leftParty(eim, player) {			
    // If only 2 players are left, uncompletable:
    var party = eim.getPlayers();
    if (true) {
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim,party.get(i));
        }
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    //KPQ does nothing special with winners
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function timeOut() {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerExit(eim, pIter.next());
            }
        }
        eim.dispose();
    }
}
