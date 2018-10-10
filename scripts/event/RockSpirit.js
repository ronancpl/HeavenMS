/* 
 * This file is part of the OdinMS Maple Story Server
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

importPackage(Packages.tools);

var exitMap;
var entryMap;
var otherMap;
var minPlayers = 1;
var fightTime = 60;
var timer = 1000 * 60 * fightTime;

function init() {
	exitMap = em.getChannelServer().getMapFactory().getMap(103040400);
	entryMap = em.getChannelServer().getMapFactory().getMap(103040410);
	otherMap = em.getChannelServer().getMapFactory().getMap(103040420);
}

function setup() {
    var eim = em.newInstance("RockSpirit_" + em.getProperty("player"));
    respawn(eim);
    eim.startEventTimer(timer);    
	return eim;
}

function afterSetup(eim) {}

function respawn(eim) {
	var map = eim.getMapInstance(entryMap.getId());
	var map2 = eim.getMapInstance(otherMap.getId());
	map.allowSummonState(true);
	map2.allowSummonState(true);
	map.instanceMapRespawn();
	map2.instanceMapRespawn();
	eim.schedule("respawn", 10000);
}


function playerEntry(eim, player) {
	var amplifierMap = eim.getMapInstance(entryMap.getId());
	player.changeMap(amplifierMap);
    eim.schedule("timeOut", timer);
}

function playerRevive(eim, player) {
    player.respawn(eim, exitMap);
    return false;
}

function playerDead(eim, player) {}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
	    if (party.get(i).equals(player)) {
	        removePlayer(eim, player);
	    } else {
	        playerExit(eim, party.get(i));
	    }
	}
	eim.dispose();
}

function monsterValue(eim,mobId) { 
    return -1;
}

function leftParty(eim, player) {
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim) {}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}


function moveMap(eim, player) {
	if (player.getMap().getId() == exitMap.getId()) {
		removePlayer(eim, player);
		player.getClient().announce(MaplePacketCreator.removeClock());
		eim.dispose();
	}
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function cancelSchedule() {}

function dispose() {}

function clearPQ(eim) {}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function timeOut(eim) {
    if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()){
                var player = pIter.next();
                playerExit(eim, player);
            }
        }
        eim.dispose();
    }
}