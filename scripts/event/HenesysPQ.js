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
 * @author: Ronan
 * @event: Henesys PQ
*/

var isPq = true;
var minPlayers = 3, maxPlayers = 6;
var minLevel = 10, maxLevel = 255;
var entryMap = 910010000;
var exitMap = 910010300;
var recruitMap = 100000200;
var clearMap = 910010100;

var minMapId = 910010000;
var maxMapId = 910010400;

var eventTime = 10;     // 10 minutes

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
        var itemSet = [4001095, 4001096, 4001097, 4001098, 4001099, 4001100, 4001101];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [4001158];
        itemQty = [1];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [1600];    //bonus exp given on CLEAR stage signal
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
        var eim = em.newInstance("Henesys" + lobbyid);
        eim.setProperty("level", level);
        eim.setProperty("stage", "0");
        eim.setProperty("bunnyCake", "0");
        eim.setProperty("bunnyDamaged", "0");
        
        eim.getInstanceMap(910010000).resetPQ(level);
        eim.getInstanceMap(910010000).allowSummonState(false);
        
        eim.getInstanceMap(910010200).resetPQ(level);
        
        respawnStages(eim);
        eim.startEventTimer(eventTime * 60000);
        setEventRewards(eim);
        setEventExclusives(eim);
        return eim;
}

function afterSetup(eim) {}

function respawnStages(eim) {
        eim.getInstanceMap(910010000).instanceMapRespawn();
        eim.getInstanceMap(910010200).instanceMapRespawn();
        
        eim.schedule("respawnStages", 15 * 1000);
}

function playerEntry(eim, player) {
        var map = eim.getMapInstance(entryMap);
        player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
        if(eim.getProperty("1stageclear") != null) {
                var curStage = 910010200, toStage = 910010400;
                eim.warpEventTeam(curStage, toStage);
        }
        else {
                end(eim);
        }
}

function bunnyDefeated(eim) {
        eim.dropMessage(5, "Due to your failure to protect the Moon Bunny, you have been transported to the Exile Map.");
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
        if (mapid < minMapId || mapid > maxMapId || mapid == 910010300) {
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
        
        eim.warpEventTeam(910010100);
}

function monsterKilled(mob, eim) {}

function friendlyKilled(mob, eim) {
        if (mob.getId() == 9300061) {
                eim.schedule("bunnyDefeated", 5 * 1000);
        }
}

function friendlyItemDrop(eim, mob) {
        if (mob.getId() == 9300061) {
                var cakes = eim.getIntProperty("bunnyCake") + 1;
                eim.setIntProperty("bunnyCake", cakes);
                
                mob.getMap().broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "The Moon Bunny made rice cake number " + cakes + "."));
        }
}

function friendlyDamaged(eim, mob) {
        if (mob.getId() == 9300061) {
                var bunnyDamage = eim.getIntProperty("bunnyDamaged") + 1;
                if (bunnyDamage > 5) {
                        broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "The Moon Bunny is feeling sick. Please protect it so it can make delicious rice cakes."));
                        eim.setIntProperty("bunnyDamaged", 0);
                }
        }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}

