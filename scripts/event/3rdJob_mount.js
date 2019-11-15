/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
 * 3rd Job Event - Kenta's Mount Quest
**/
importPackage(Packages.tools);

var entryMap = 923010000;
var exitMap = 923010100;

var minMapId = 923010000;
var maxMapId = 923010000;

var eventMaps = [923010000];

var eventTime = 5; //5 minutes

var lobbyRange = [0, 7];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
    em.setProperty("noEntry","false");
}

function checkHogHealth(eim) {
    var watchHog = eim.getInstanceMap(923010000).getMonsterById(9300102);
    if (watchHog != null) {
        var hp = watchHog.getHp();
        var oldHp = eim.getIntProperty("whog_hp");
        
        if (oldHp - hp > 1000) {    // or 800, if using mobHP / eventTime
            eim.dropMessage(6, "Please protect the pig from the aliens!");  // thanks Vcoc
        }
        eim.setIntProperty("whog_hp", hp);
    }
}

function respawnStages(eim) {
    var i;
    for (i = 0; i < eventMaps.length; i++) {
        eim.getInstanceMap(eventMaps[i]).instanceMapRespawn();
    }
    checkHogHealth(eim);
    
    eim.schedule("respawnStages", 10 * 1000);
}

function setup(level, lobbyid) {
    var eim = em.newInstance("3rdJob_mount_" + lobbyid);
    eim.setProperty("level", level);
    eim.setProperty("boss", "0");
    eim.setProperty("whog_hp", "0");
    
    return eim;
}

function playerEntry(eim, player) {
    var mapObj = eim.getInstanceMap(entryMap);
    
    mapObj.resetPQ(1);
    mapObj.instanceMapForceRespawn();
    respawnStages(eim);
    
    player.changeMap(entryMap, 0);
    em.setProperty("noEntry","true");
    
    player.getClient().announce(MaplePacketCreator.getClock(eventTime * 60));
    eim.startEventTimer(eventTime * 60000);
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    var api = player.getAbstractPlayerInteraction();
    api.removeAll(4031507);
    api.removeAll(4031508);
    
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


// ---------- FILLER FUNCTIONS ----------

function disbandParty(eim, player) {}

function afterSetup(eim) {}

function changedLeader(eim, leader) {}

function leftParty(eim, player) {}

