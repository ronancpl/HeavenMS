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
 * 3rd Job Event - Warrior
**/
importPackage(Packages.tools);

var entryMap = 108010300;
var exitMap = 105070001;

var minMapId = 108010300;
var maxMapId = 108010301;

var eventTime = 20; //20 minutes

var lobbyRange = [0, 0];

function setLobbyRange() {
        return lobbyRange;
}

function init() {
    em.setProperty("noEntry","false");
}

function playerEntry(eim, player) {
    eim.getInstanceMap(maxMapId).resetPQ(1);
    
    player.changeMap(entryMap, 0);
    em.setProperty("noEntry","true");
    
    player.getClient().announce(MaplePacketCreator.getClock(eventTime * 60));
    eim.startEventTimer(eventTime * 60000);
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    eim.dispose();
    em.setProperty("noEntry","false");
}

function scheduledTimeout(eim) {
    var player = eim.getPlayers().get(0);
    playerExit(eim, eim.getPlayers().get(0));
    player.changeMap(exitMap);
}

function playerDisconnected(eim, player) {
    playerExit(eim, player);
}

function clear(eim) {
    var player = eim.getPlayers().get(0);
    eim.unregisterPlayer(player);
    player.changeMap(exitMap);
    
    eim.dispose();
    em.setProperty("noEntry","false");
}

function changedMap(eim, chr, mapid) {
    if(mapid < minMapId || mapid > maxMapId) playerExit(eim, chr);
}

function monsterKilled(mob, eim) {}

function monsterValue(eim, mobId) {
        return 1;
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose() {}
