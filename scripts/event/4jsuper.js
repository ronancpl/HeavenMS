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
 * Event - Kyrin's Test Quest
**/

var entryMap = 912010100;
var exitMap = 120000101;

var minMapId = 912010100;
var maxMapId = 912010200;

var eventTime = 4; //4 minutes

var lobbyRange = [0, 0];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
    em.setProperty("noEntry","false");
}

function setup(level, lobbyid) {
    var eim = em.newInstance("4jsuper_" + lobbyid);
    eim.setProperty("level", level);
    eim.setProperty("boss", "0");
    eim.setProperty("canLeave", "0");

    eim.getInstanceMap(entryMap).resetPQ(level);

    respawnStages(eim);
    eim.startEventTimer(eventTime * 60000);
    eim.schedule("playerCanLeave", 1 * 60000);
    eim.schedule("playerSurvived", 2 * 60000);
    return eim;
}

function afterSetup(eim) {}

function respawnStages(eim) {}

function playerCanLeave(eim) {
    eim.setIntProperty("canLeave", 1);
}

function playerSurvived(eim) {
    if (eim.getLeader().isAlive()) {
        eim.setIntProperty("canLeave", 2);
        eim.dropMessage(5, "Kyrin: You have passed the test. Now for the closing part... Are you able reach the exit over there?");
    } else {
        eim.dropMessage(5, "Kyrin: You have failed the test. Aww, don't have such a sad face, just try it again later, ok?");
    }
}

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

function changedLeader(eim, leader) {}

function clearPQ(eim) {
    eim.stopEventTimer();
    eim.setEventCleared();
    
    var player = eim.getPlayers().get(0);
    eim.unregisterPlayer(player);
    player.changeMap(exitMap);
    
    eim.dispose();
    em.setProperty("noEntry","false");
}

function monsterKilled(mob, eim) {}

function leftParty(eim, player) {}

function disbandParty(eim) {}

function monsterValue(eim, mobId) {
    return 1;
}

function friendlyKilled(mob, eim) {
    if(em.getProperty("noEntry") != "false") {
        var player = eim.getPlayers().get(0);
        playerExit(eim, player);
        player.changeMap(exitMap);
    }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose() {}
