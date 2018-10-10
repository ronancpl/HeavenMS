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
 * Event - Balrog Quest
**/
importPackage(Packages.tools);

var entryMap = 910520000;
var exitMap = 105100100;

var minMapId = 910520000;
var maxMapId = 910520000;

var eventTime = 10;     //10 minutes

var lobbyRange = [0, 0];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
    em.setProperty("noEntry","false");
}

function respawnStages(eim) {}

function afterSetup(eim) {}

function playerEntry(eim, player) {
    var mapObj = eim.getInstanceMap(entryMap);
    
    mapObj.resetPQ(1);
    mapObj.instanceMapForceRespawn();
    mapObj.closeMapSpawnPoints();
    respawnStages(eim);
    
    player.changeMap(entryMap, 1);
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
    playerExit(eim, player);
    player.changeMap(exitMap);
}

function playerDisconnected(eim, player) {
    playerExit(eim, player);
}

function changedMap(eim, chr, mapid) {
    if(mapid < minMapId || mapid > maxMapId) playerExit(eim, chr);
}

function isBalrog(mob) {
        return mob.getId() == 9300326;
}

function monsterKilled(mob, eim) {
    if(isBalrog(mob)) {
        eim.spawnNpc(1061015, new java.awt.Point(0, 115), mob.getMap());
    }
}
function monsterValue(eim, mobId) {
        return 1;
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose() {}
