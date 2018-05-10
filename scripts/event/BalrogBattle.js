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
 * @event: Vs Balrog
*/

importPackage(Packages.server.life);

var isPq = true;
var minPlayers = 6, maxPlayers = 30;
var minLevel = 50, maxLevel = 255;
var entryMap = 105100300;
var exitMap = 105100100;
var recruitMap = 105100100;
var clearMap = 105100301;

var minMapId = 105100300;
var maxMapId = 105100301;

var minMobId = 8830000;
var maxMobId = 8830006;
var bossMobId = 8830003;

var eventTime = 60;         // 60 minutes
var releaseClawTime = 1;

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
        var eim = em.newInstance("Balrog" + lobbyid);
        eim.setProperty("level", level);
        eim.setProperty("boss", "0");
        
        eim.getInstanceMap(105100300).resetPQ(level);
        eim.getInstanceMap(105100301).resetPQ(level);
        eim.schedule("releaseLeftClaw", releaseClawTime * 60000);
        
        respawnStages(eim);
        eim.startEventTimer(eventTime * 60000);
        setEventRewards(eim);
        setEventExclusives(eim);
        return eim;
}

function afterSetup(eim) {
        spawnBalrog(eim);
}

function respawnStages(eim) {}

function releaseLeftClaw(eim) {
        eim.getInstanceMap(entryMap).killMonster(8830006);
}

function spawnBalrog(eim) {
        var mapObj = eim.getInstanceMap(entryMap);
    
        mapObj.spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8830000), new Packages.java.awt.Point(412, 258));
        mapObj.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8830002), new Packages.java.awt.Point(412, 258));
        mapObj.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8830006), new Packages.java.awt.Point(412, 258));
}

function spawnSealedBalrog(eim) {
        eim.getInstanceMap(entryMap).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(bossMobId), new Packages.java.awt.Point(412, 258));
}

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

function isUnsealedBalrog(mob) {
        var balrogid = mob.getId() - 8830000;
        return balrogid >= 0 && balrogid <= 2;
}

function isBalrogBody(mob) {
        return mob.getId() == minMobId;
}

function monsterKilled(mob, eim) {
        if(isUnsealedBalrog(mob)) {
                var count = eim.getIntProperty("boss");
            
                if(count == 2) {
                        eim.showClearEffect();
                        eim.clearPQ();

                        eim.dispatchUpdateQuestMobCount(bossMobId, entryMap);
                        mob.getMap().broadcastBalrogVictory(eim.getLeader().getName());
                } else {
                        if(count == 1) {
                                var mapobj = eim.getInstanceMap(entryMap);
                                mapobj.makeMonsterReal(mapobj.getMonsterById(8830000));
                        }
                    
                        eim.setIntProperty("boss", count + 1);
                }
                
                if(isBalrogBody(mob)) {
                        eim.schedule("spawnSealedBalrog", 10 * 1000);
                }
        }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}

