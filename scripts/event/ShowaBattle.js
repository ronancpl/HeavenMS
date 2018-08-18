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
 * @author: Ronan
 * @event: Showa Boss Battle
*/

var isPq = true;
var minPlayers = 3, maxPlayers = 30;
var minLevel = 100, maxLevel = 255;
var entryMap = 801040100;
var exitMap = 801040004;
var recruitMap = 801040004;
var clearMap = 801040101;

var minMapId = 801040100;
var maxMapId = 801040101;

var eventTime = 60;     // 60 minutes for boss stg

var lobbyRange = [0, 0];

function init() {
        setEventRequirements();
}

function setLobbyRange() {
        return lobbyRange;
}

function setEventRequirements() {
        var reqStr = "";
        
        reqStr += "\r\n    Number of players: ";
        if(maxPlayers - minPlayers >= 1) reqStr += minPlayers + " ~ " + maxPlayers;
        else reqStr += minPlayers;
        
        reqStr += "\r\n    Level range: ";
        if(maxLevel - minLevel >= 1) reqStr += minLevel + " ~ " + maxLevel;
        else reqStr += minLevel;
        
        reqStr += "\r\n    Time limit: ";
        reqStr += eventTime + " minutes";
        
        em.setProperty("party", reqStr);
}

function setEventExclusives(eim) {
        var itemSet = [];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages, mesoStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [1102145, 1102084, 1102085, 1102086, 1102087, 1052165, 1052166, 1052167, 1402013, 1332030, 1032030, 1032070, 4003000, 4000030, 4006000, 4006001, 4005000, 4005001, 4005002, 4005003, 4005004, 2022016, 2022263, 2022264, 2022015, 2022306, 2022307, 2022306, 2022113];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 40, 40, 100, 100, 2, 2, 2, 2, 1, 100, 100, 100, 40, 40, 40, 40, 40];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [];    //bonus exp given on CLEAR stage signal
        eim.setEventClearStageExp(expStages);
        
        mesoStages = [];    //bonus meso given on CLEAR stage signal
        eim.setEventClearStageMeso(mesoStages);
}

function afterSetup(eim) {}

function setup(channel) {
    var eim = em.newInstance("Showa" + channel);
    eim.setProperty("canJoin", 1);
    eim.setProperty("playerDied", 0);

    var level = 1;
    eim.getInstanceMap(801040100).resetPQ(level);
    
    respawnStages(eim);
    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);
    
    return eim;
}

function respawnStages(eim) {
    eim.getInstanceMap(801040100).instanceMapRespawn();

    eim.schedule("respawnStages", 15 * 1000);
}

function playerEntry(eim, player) {
    eim.dropMessage(5, "[Expedition] " + player.getName() + " has entered the map.");
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    end(eim);
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
	if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
            eim.dropMessage(5, "[Expedition] Either the leader has quit the expedition or there is no longer the minimum number of members required to continue it.");
            eim.unregisterPlayer(player);
            end(eim);
        }
        else {
            eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the instance.");
            eim.unregisterPlayer(player);
        }
    }
}

function changedLeader(eim, leader) {}

function playerDead(eim, player) {
    eim.setIntProperty("playerDied", 1);
}

function playerRevive(eim, player) {
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
        eim.unregisterPlayer(player);
        eim.dropMessage(5, "[Expedition] Either the leader has quit the expedition or there is no longer the minimum number of members required to continue it.");
        end(eim);
    }
    else {
        eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the instance.");
        eim.unregisterPlayer(player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
        eim.dropMessage(5, "[Expedition] Either the leader has quit the expedition or there is no longer the minimum number of members required to continue it.");
        eim.unregisterPlayer(player);
        end(eim);
    }
    else {
        eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the instance.");
        eim.unregisterPlayer(player);
    }
}

function leftParty (eim, player) {}

function disbandParty (eim) {}

function monsterValue(eim, mobId) {
    return 1;
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
}

function end(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function giveRandomEventReward(eim, player) {
    eim.giveEventReward(player);
}

function clearPQ(eim) {
    eim.getInstanceMap(801040100).killAllMonsters();
    
    eim.stopEventTimer();
    eim.setEventCleared();
    
    if(eim.getIntProperty("playerDied") == 0) {
        var mob = eim.getMonster(9400114);
        eim.getMapInstance(801040101).spawnMonsterOnGroundBelow(mob, new java.awt.Point(500, -50));
        eim.dropMessage(5, "Konpei: The Boss has been defeated with no casualties, well done! We found a suspicious machine inside, we're moving it out.");
    }
}

function isTheBoss(mob) {
        return mob.getId() == 9400300;
}

function monsterKilled(mob, eim) {
    if(isTheBoss(mob)) {
        eim.showClearEffect();
        eim.clearPQ();
    }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}