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
 * @event: Pirate PQ
*/

var isPq = true;
var isGrindMode = true;     // stages done after breaking all boxes on maps

var minPlayers = 3, maxPlayers = 6;
var minLevel = 55, maxLevel = 100;
var entryMap = 925100000;
var exitMap = 925100700;
var recruitMap = 251010404;
var clearMap = 925100600;

var minMapId = 925100000;
var maxMapId = 925100500;

var eventTime = 4;     // 4 minutes

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
        var itemSet = [4001117, 4001120, 4001121, 4001122];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {}

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
        var eim = em.newInstance("Pirate" + lobbyid);
        eim.setProperty("level", level);
        
	eim.setProperty("stage2", "0");
        eim.setProperty("stage2a", "0");
	eim.setProperty("stage3a", "0");
        eim.setProperty("stage2b", "0");
	eim.setProperty("stage3b", "0");
	eim.setProperty("stage4", "0");
	eim.setProperty("stage5", "0");
        
        eim.setProperty("curStage", "1");
        eim.setProperty("grindMode", isGrindMode ? "1" : "0");
        
        eim.setProperty("openedChests", "0");
        eim.setProperty("openedBoxes", "0");
        eim.getInstanceMap(925100000).resetPQ(level);
        eim.getInstanceMap(925100000).shuffleReactors();
        
	eim.getInstanceMap(925100100).resetPQ(level);
	var map = eim.getInstanceMap(925100200);
	map.resetPQ(level);
        map.shuffleReactors();
	for (var i = 0; i < 5; i++) {
		var mob = em.getMonster(9300124);
		var mob2 = em.getMonster(9300125);
		var mob3 = em.getMonster(9300124);
		var mob4 = em.getMonster(9300125);
		eim.registerMonster(mob);
		eim.registerMonster(mob2);
		eim.registerMonster(mob3);
		eim.registerMonster(mob4);
		mob.changeDifficulty(level,isPq);
		mob2.changeDifficulty(level,isPq);
		mob3.changeDifficulty(level,isPq);
		mob4.changeDifficulty(level,isPq);
		map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(430, 75));
		map.spawnMonsterOnGroundBelow(mob2, new java.awt.Point(1600, 75));
		map.spawnMonsterOnGroundBelow(mob3, new java.awt.Point(430, 238));
		map.spawnMonsterOnGroundBelow(mob4, new java.awt.Point(1600, 238));
	}
	map = eim.getInstanceMap(925100201);
	map.resetPQ(level);
	for (var i = 0; i < 10; i++) {
		var mob = em.getMonster(9300112);
		var mob2 = em.getMonster(9300113);
		eim.registerMonster(mob);
		eim.registerMonster(mob2);
		mob.changeDifficulty(level,isPq);
		mob2.changeDifficulty(level,isPq);
		map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(0, 238));
		map.spawnMonsterOnGroundBelow(mob2, new java.awt.Point(1700, 238));
	}
	eim.getInstanceMap(925100202).resetPQ(level);
	map = eim.getInstanceMap(925100300);
	map.resetPQ(level);
        map.shuffleReactors();
	for (var i = 0; i < 5; i++) {
		var mob = em.getMonster(9300124);
		var mob2 = em.getMonster(9300125);
		var mob3 = em.getMonster(9300124);
		var mob4 = em.getMonster(9300125);
		eim.registerMonster(mob);
		eim.registerMonster(mob2);
		eim.registerMonster(mob3);
		eim.registerMonster(mob4);
		mob.changeDifficulty(level,isPq);
		mob2.changeDifficulty(level,isPq);
		mob3.changeDifficulty(level,isPq);
		mob4.changeDifficulty(level,isPq);
		map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(430, 75));
		map.spawnMonsterOnGroundBelow(mob2, new java.awt.Point(1600, 75));
		map.spawnMonsterOnGroundBelow(mob3, new java.awt.Point(430, 238));
		map.spawnMonsterOnGroundBelow(mob4, new java.awt.Point(1600, 238));
	}
	map = eim.getInstanceMap(925100301);
	map.resetPQ(level);
	for (var i = 0; i < 10; i++) {
		var mob = em.getMonster(9300112);
		var mob2 = em.getMonster(9300113);
		eim.registerMonster(mob);
		eim.registerMonster(mob2);
		mob.changeDifficulty(level,isPq);
		mob2.changeDifficulty(level,isPq);
		map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(0, 238));
		map.spawnMonsterOnGroundBelow(mob2, new java.awt.Point(1700, 238));
	}
	eim.getInstanceMap(925100302).resetPQ(level);
	eim.getInstanceMap(925100400).resetPQ(level);
	eim.getInstanceMap(925100500).resetPQ(level);
        
        respawnStg4(eim);
        
        eim.startEventTimer(eventTime * 60000);
        setEventRewards(eim);
        setEventExclusives(eim);
        return eim;
}

function afterSetup(eim) {}

function respawnStg4(eim) {
        eim.getMapInstance(925100400).instanceMapRespawn();
        eim.schedule("respawnStg4", 10 * 1000);
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

function changedMapInside(eim, mapid) {
        var stage = eim.getIntProperty("curStage");
    
        if(stage == 1) {
                if(mapid == 925100100) {
                        eim.restartEventTimer(6 * 60 * 1000);
                        eim.setIntProperty("curStage", 2);
                }
        } else if(stage == 2) {
                if(mapid == 925100200) {
                        eim.restartEventTimer(6 * 60 * 1000);
                        eim.setIntProperty("curStage", 3);
                }
        } else if(stage == 3) {
                if(mapid == 925100300) {
                        eim.restartEventTimer(6 * 60 * 1000);
                        eim.setIntProperty("curStage", 4);
                }
        } else if(stage == 4) {
                if(mapid == 925100400) {
                        eim.restartEventTimer(6 * 60 * 1000);
                        eim.setIntProperty("curStage", 5);
                }
        } else if(stage == 5) {
                if(mapid == 925100500) {
                        eim.restartEventTimer(8 * 60 * 1000);
                        eim.setIntProperty("curStage", 6);
                }
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
        } else {
                changedMapInside(eim, mapid);
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
                end(eim);
        }
        else
                playerExit(eim, player);
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

function clearPQ(eim) {
        eim.stopEventTimer();
        eim.setEventCleared();
        
        var chests = parseInt(eim.getProperty("openedChests"));
        var expGain = (chests == 0 ? 28000 : (chests == 1 ? 35000 : 42000));
        eim.giveEventPlayersExp(expGain);
        
        eim.warpEventTeam(925100600);
}

function isLordPirate(mob) {
        var mobid = mob.getId();
        return (mobid == 9300105) || (mobid == 9300106) || (mobid == 9300107) || (mobid == 9300119);
}

function passedGrindMode(map, eim) {
        if(eim.getIntProperty("grindMode") == 0) return true;
        return eim.activatedAllReactorsOnMap(map, 2511000, 2517999);
}

function monsterKilled(mob, eim) {
        var map = mob.getMap();
    
        if(isLordPirate(mob)) {  // lord pirate defeated, spawn the little fella!
            map.broadcastStringMessage(5, "As Lord Pirate dies, Wu Yang is released!");
            eim.spawnNpc(2094001, new java.awt.Point(777, 140), mob.getMap());
        }
        
        if(map.countMonsters() == 0) {
                var stage = ((map.getId() % 1000) / 100) + 1;
        
                if((stage == 1 || stage == 3 || stage == 4) && passedGrindMode(map, eim)) {
                        eim.showClearEffect(map.getId());
                } else if(stage == 5) {
                        if(map.getReactorByName("sMob1").getState() >= 1 && map.getReactorByName("sMob2").getState() >= 1 && map.getReactorByName("sMob3").getState() >= 1 && map.getReactorByName("sMob4").getState() >= 1) {
                                eim.showClearEffect(map.getId());
                        }
                }
        }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
