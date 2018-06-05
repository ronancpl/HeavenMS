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
 * @event: Amoria PQ
*/

var isPq = true;
var onlyMarriedPlayers = true;
var minPlayers = 6, maxPlayers = 6;
var minLevel = 40, maxLevel = 255;
var entryMap = 670010200;
var exitMap = 670011000;
var recruitMap = 670010100;
var clearMap = 670010800;

var minMapId = 670010200;
var maxMapId = 670010800;

var eventTime = 75;     // 75 minutes

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
        
        reqStr += "\r\n    At least 1 of both genders";
        if(onlyMarriedPlayers) reqStr += "\r\n    All married";
        
        reqStr += "\r\n    Time limit: ";
        reqStr += eventTime + " minutes";
        
        em.setProperty("party", reqStr);
}

function setEventExclusives(eim) {
        var itemSet = [4031594, 4031595, 4031596, 4031597];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [];
        itemQty = [];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [2000, 4000, 6000, 8000, 9000, 11000];    //bonus exp given on CLEAR stage signal
        eim.setEventClearStageExp(expStages);
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
        var eligible = [];
        var hasLeader = false, hasNotMarried = false;
        var mask = 0;
        
        if(party.size() > 0) {
                var partyList = party.toArray();

                for(var i = 0; i < party.size(); i++) {
                        var ch = partyList[i];

                        if(ch.getMapId() == recruitMap && ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel) {
                                if(ch.isLeader()) hasLeader = true;
                                if(!ch.getPlayer().isMarried()) hasNotMarried = true;
                                eligible.push(ch);
                                
                                mask |= (1 << ch.getPlayer().getGender());
                        }
                }
        }
        
        if(!(hasLeader && eligible.length >= minPlayers && eligible.length <= maxPlayers && mask == 3)) eligible = [];
        if(onlyMarriedPlayers && hasNotMarried) eligible = [];
        return eligible;
}

function setup(level, lobbyid) {
        var eim = em.newInstance("Amoria" + lobbyid);
        eim.setProperty("level", level);
        
        eim.setProperty("marriedGroup", 0);
        eim.setProperty("missCount", 0);
        eim.setProperty("statusStg1", -1);
        eim.setProperty("statusStg2", -1);
        eim.setProperty("statusStg3", -1);
        eim.setProperty("statusStg4", -1);
        eim.setProperty("statusStg5", -1);
        eim.setProperty("statusStg6", -1);
        eim.setProperty("statusStgBonus", 0);
        
        eim.getInstanceMap(670010200).resetPQ(level);
        eim.getInstanceMap(670010300).resetPQ(level);
        eim.getInstanceMap(670010301).resetPQ(level);
        eim.getInstanceMap(670010302).resetPQ(level);
        eim.getInstanceMap(670010400).resetPQ(level);
        eim.getInstanceMap(670010500).resetPQ(level);
        eim.getInstanceMap(670010600).resetPQ(level);
        eim.getInstanceMap(670010700).resetPQ(level);
        eim.getInstanceMap(670010750).resetPQ(level);
        eim.getInstanceMap(670010800).resetPQ(level);
        
        eim.getInstanceMap(670010200).toggleDrops();
        eim.getInstanceMap(670010300).toggleDrops();
        eim.getInstanceMap(670010301).toggleDrops();
        eim.getInstanceMap(670010302).toggleDrops();
        
        eim.getInstanceMap(670010200).instanceMapForceRespawn();
        eim.getInstanceMap(670010500).instanceMapForceRespawn();
        
        eim.getInstanceMap(670010750).shuffleReactors();
        eim.getInstanceMap(670010800).shuffleReactors();
        
        var mapObj = eim.getInstanceMap(670010700);
        var mobObj = Packages.server.life.MapleLifeFactory.getMonster(9400536);
        mapObj.spawnMonsterOnGroundBelow(mobObj, new Packages.java.awt.Point(942, 478));
        
        respawnStages(eim);
        
        eim.startEventTimer(eventTime * 60000);
        setEventRewards(eim);
        setEventExclusives(eim);
        
        return eim;
}

function isTeamAllCouple(eim) {     // everyone partner of someone on the team
        var eventPlayers = eim.getPlayers();
    
        for (var iterator = eventPlayers.iterator(); iterator.hasNext();) {
                var chr = iterator.next();
                
                var pid = chr.getPartnerId();
                if(pid <= 0 || eim.getPlayerById(pid) == null) {
                        return false;
                }
        }
        
        return true;
}

function afterSetup(eim) {
        if(isTeamAllCouple(eim)) {
                eim.setIntProperty("marriedGroup", 1);
        }
}

function respawnStages(eim) {}

function playerEntry(eim, player) {
        var map = eim.getMapInstance(entryMap);
        player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
        if(eim.getIntProperty("statusStg6") == 1) {
                eim.warpEventTeam(exitMap);
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
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
