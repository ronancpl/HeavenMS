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
 * @event: Zakum PQ
*/

var isPq = true;
var minPlayers = 1, maxPlayers = 6;
var minLevel = 50, maxLevel = 255;
var entryMap = 280010000;
var exitMap = 211042300;
var recruitMap = 211042300;
var clearMap = 211042300;

var minMapId = 280010000;
var maxMapId = 280011006;

var eventTime = 30;     // 30 minutes

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
        var itemSet = [4001015, 4001016, 4001018];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [];
        itemQty = [];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [];    //bonus exp given on CLEAR stage signal
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
        var eim = em.newInstance("PreZakum" + lobbyid);
        eim.setProperty("level", level);
        eim.setProperty("gotDocuments", 0);
        
        eim.getInstanceMap(280010000).resetPQ(level);
        eim.getInstanceMap(280010010).resetPQ(level);
        eim.getInstanceMap(280010011).resetPQ(level);
        eim.getInstanceMap(280010020).resetPQ(level);
        eim.getInstanceMap(280010030).resetPQ(level);
        eim.getInstanceMap(280010031).resetPQ(level);
        eim.getInstanceMap(280010040).resetPQ(level);
        eim.getInstanceMap(280010041).resetPQ(level);
        eim.getInstanceMap(280010050).resetPQ(level);
        eim.getInstanceMap(280010060).resetPQ(level);
        eim.getInstanceMap(280010070).resetPQ(level);
        eim.getInstanceMap(280010071).resetPQ(level);
        eim.getInstanceMap(280010080).resetPQ(level);
        eim.getInstanceMap(280010081).resetPQ(level);
        eim.getInstanceMap(280010090).resetPQ(level);
        eim.getInstanceMap(280010091).resetPQ(level);
        eim.getInstanceMap(280010100).resetPQ(level);
        eim.getInstanceMap(280010101).resetPQ(level);
        eim.getInstanceMap(280010110).resetPQ(level);
        eim.getInstanceMap(280010120).resetPQ(level);
        eim.getInstanceMap(280010130).resetPQ(level);
        eim.getInstanceMap(280010140).resetPQ(level);
        eim.getInstanceMap(280010150).resetPQ(level);
        eim.getInstanceMap(280011000).resetPQ(level);
        eim.getInstanceMap(280011001).resetPQ(level);
        eim.getInstanceMap(280011002).resetPQ(level);
        eim.getInstanceMap(280011003).resetPQ(level);
        eim.getInstanceMap(280011004).resetPQ(level);
        eim.getInstanceMap(280011005).resetPQ(level);
        eim.getInstanceMap(280011006).resetPQ(level);
        
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
        end(eim);
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
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
