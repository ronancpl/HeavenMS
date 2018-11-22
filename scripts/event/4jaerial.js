/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
/**
 * @Author Ronan
 * Event - Jonathan's Test Quest
**/

var entryMap = 912020000;
var exitMap = 120000102;

var minMapId = 912020000;
var maxMapId = 912020000;

var eventTime = 2; //2 minutes

var lobbyRange = [0, 0];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
    em.setProperty("noEntry","false");
}

function setup(level, lobbyid) {
    var eim = em.newInstance("4jaerial_" + lobbyid);
    eim.setProperty("level", level);
    eim.setProperty("boss", "0");
    eim.setProperty("canLeave", "0");

    eim.getInstanceMap(entryMap).resetPQ(level);
    eim.getInstanceMap(entryMap).shuffleReactors();

    respawnStages(eim);
    eim.startEventTimer(eventTime * 60000);
    return eim;
}

function afterSetup(eim) {}

function respawnStages(eim) {}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    eim.dispose();
    em.setProperty("noEntry","false");
}

function playerLeft(eim, player) {}

function scheduledTimeout(eim) {
    var player = eim.getPlayers().get(0);
    playerExit(eim, player);
    player.changeMap(exitMap);
}

function playerDisconnected(eim, player) {
    playerExit(eim, player);
}

function changedMap(eim, chr, mapid) {
    if(mapid < minMapId || mapid > maxMapId) playerExit(eim, chr);
}

function clearPQ(eim) {
    eim.stopEventTimer();
    eim.setEventCleared();
}

function monsterKilled(mob, eim) {}

function leftParty(eim, player) {}

function disbandParty(eim) {}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose() {}
