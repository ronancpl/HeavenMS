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
 * @event: Ludibrium PQ
*/

var isPq = true;
var minPlayers = 5, maxPlayers = 6;
var minLevel = 35, maxLevel = 50;
var entryMap = 922010100;
var exitMap = 922010000;
var recruitMap = 221024500;
var clearMap = 922011000;

var minMapId = 922010100;
var maxMapId = 922011100;

var eventTime = 45;     // 45 minutes

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
        var itemSet = [4001022, 4001023];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [2040602, 2040802, 2040002, 2040402, 2040505, 2040502, 2040601, 2044501, 2044701, 2044601, 2041019, 2041016, 2041022, 2041013, 2041007, 2043301, 2040301, 2040801, 2040001, 2040004, 2040504, 2040501, 2040513, 2043101, 2044201, 2044401, 2040701, 2044301, 2043801, 2040401, 2043701, 2040803, 2000003, 2000002, 2000004, 2000006, 2000005, 2022000, 2001001, 2001002, 2022003, 2001000, 2020014, 2020015, 4003000, 1102003, 1102004, 1102000, 1102002, 1102001, 1102011, 1102012, 1102013, 1102014, 1032011, 1032012, 1032013, 1032002, 1032008, 1032011, 2070011, 4010003, 4010000, 4010006, 4010002, 4010005, 4010004, 4010001, 4020001, 4020002, 4020008, 4020007, 4020003, 4020000, 4020004, 4020005, 4020006];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 85, 85, 10, 60, 2, 20, 15, 15, 20, 15, 10, 5, 35, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 10, 6, 10, 10, 10, 10, 10, 10, 4, 4, 10, 10, 10, 10, 10];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [210, 2520, 2940, 3360, 3770, 0, 4620, 5040, 5950];    //bonus exp given on CLEAR stage signal
        eim.setEventClearStageExp(expStages);
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
        var eligible = [];
        var hasLeader = false;
        
        if(party.size() > 0) {
                var partyList = party.toArray();

                for(var i = 0; i < party.size(); i++) {
                        var ch = partyList[i];

                        if(ch.getMapId() == recruitMap && ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel) {
                                if(ch.isLeader()) hasLeader = true;
                                eligible.push(ch);
                        }
                }
        }
        
        if(!(hasLeader && eligible.length >= minPlayers && eligible.length <= maxPlayers)) eligible = [];
        return eligible;
}

function setup(level, lobbyid) {
        var eim = em.newInstance("Ludi" + lobbyid);
        eim.setProperty("level", level);
        
        eim.setProperty("statusStg1", -1);
        eim.setProperty("statusStg2", -1);
        eim.setProperty("statusStg3", -1);
        eim.setProperty("statusStg4", -1);
        eim.setProperty("statusStg5", -1);
        eim.setProperty("statusStg6", -1);
        eim.setProperty("statusStg7", -1);
        eim.setProperty("statusStg8", -1);
        eim.setProperty("statusStg9", -1);
        
        eim.getInstanceMap(922010100).resetPQ(level);
        eim.getInstanceMap(922010200).resetPQ(level);
        eim.getInstanceMap(922010201).resetPQ(level);
        eim.getInstanceMap(922010300).resetPQ(level);
        eim.getInstanceMap(922010400).resetPQ(level);
        eim.getInstanceMap(922010401).resetPQ(level);
        eim.getInstanceMap(922010402).resetPQ(level);
        eim.getInstanceMap(922010403).resetPQ(level);
        eim.getInstanceMap(922010404).resetPQ(level);
        eim.getInstanceMap(922010405).resetPQ(level);
        eim.getInstanceMap(922010500).resetPQ(level);
        eim.getInstanceMap(922010500).resetPQ(level);
        eim.getInstanceMap(922010501).resetPQ(level);
        eim.getInstanceMap(922010502).resetPQ(level);
        eim.getInstanceMap(922010503).resetPQ(level);
        eim.getInstanceMap(922010504).resetPQ(level);
        eim.getInstanceMap(922010505).resetPQ(level);
        eim.getInstanceMap(922010506).resetPQ(level);
        eim.getInstanceMap(922010600).resetPQ(level);
        eim.getInstanceMap(922010700).resetPQ(level);
        eim.getInstanceMap(922010800).resetPQ(level);
        eim.getInstanceMap(922010900).resetPQ(level);
        eim.getInstanceMap(922011000).resetPQ(level);
        eim.getInstanceMap(922011100).resetPQ(level);
        
        respawnStages(eim);
        eim.startEventTimer(eventTime * 60000);
        setEventRewards(eim);
        setEventExclusives(eim);
        return eim;
}

function afterSetup(eim) {}

function respawnStages(eim) {}

function playerEntry(eim, player) {
        var map = eim.getMapInstance(entryMap);
        player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
        if(eim.getProperty("9stageclear") != null) {
                var curStage = 922011000, toStage = 922011100;
                eim.warpEventTeam(curStage, toStage);
        }
        else {
                end(eim);
        }
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
        eim.unregisterPlayer(player);
        player.changeMap(exitMap, 0);
}

function playerLeft(eim, player) {
        if(!eim.isEventCleared()) {
                playerExit(eim, player);
        }
}

function changedMap(eim, player, mapid) {
        if (mapid < minMapId || mapid > maxMapId) {
                if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                        eim.unregisterPlayer(player);
                        end(eim);
                }
                else
                        eim.unregisterPlayer(player);
        }
}

function changedLeader(eim, leader) {
        var mapid = leader.getMapId();
        if (!eim.isEventCleared() && (mapid < minMapId || mapid > maxMapId)) {
                end(eim);
        }
}

function playerDead(eim, player) {}

function playerRevive(eim, player) { // player presses ok on the death pop up.
        if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                eim.unregisterPlayer(player);
}

function playerDisconnected(eim, player) {
        if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                eim.unregisterPlayer(player);
}

function leftParty(eim, player) {
        if (eim.isEventTeamLackingNow(false, minPlayers, player)) {
                end(eim);
        }
        else
                playerLeft(eim, player);
}

function disbandParty(eim) {
        if (!eim.isEventCleared()) {
                end(eim);
        }
}

function monsterValue(eim, mobId) {
        return 1;
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
        eim.stopEventTimer();
        eim.setEventCleared();
        
        eim.startEventTimer(1 * 60000);
        eim.warpEventTeam(922011000);
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
