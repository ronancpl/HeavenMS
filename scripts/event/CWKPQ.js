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
 * @event: Crimsonwood Keep PQ
*/

var isPq = true;
var minPlayers = 6, maxPlayers = 30;
var minLevel = 100, maxLevel = 255;
var entryMap = 610030100;
var exitMap = 610030020;
var recruitMap = 610030020;
var clearMap = 610030020;

var minMapId = 610030100;
var maxMapId = 610030800;

var eventTime = 2;     // 2 minutes for first stg

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
        var itemSet = [4001256, 4001257, 4001258, 4001259, 4001260];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages, mesoStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [];
        itemQty = [];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [2500, 8000, 18000, 25000, 30000, 40000];    //bonus exp given on CLEAR stage signal
        eim.setEventClearStageExp(expStages);
        
        mesoStages = [500, 1000, 2000, 5000, 8000, 20000];    //bonus meso given on CLEAR stage signal
        eim.setEventClearStageMeso(mesoStages);
}

function afterSetup(eim) {}

function getNameFromList(index, array) {
    return array[index];
}

function generateMapReactors(map) {
    
    var jobReactors = [ [0, 0, -1, -1, 0],
                        [-1, 4, 3, 3, 3],
                        [1, 3, 4, 2, 2],
                        [2, -1, 0, 1, -1], 
                        [3, 2, 1, 0, -1],
                        [4, 1, -1, 4, 1],
                        [-1, 2, 4],
                        [-1, -1]
                      ];
                      
    var rndIndex;
    var jobFound;
    while(true) {
        jobFound = {};
        rndIndex = [];
        
        for(var i = 0; i < jobReactors.length; i++) {
            var jobReactorSlot = jobReactors[i];
            
            var idx = Math.floor(Math.random() * jobReactorSlot.length);
            jobFound["" + jobReactorSlot[idx]] = 1;
            rndIndex.push(idx);
        }
        
        if(Object.keys(jobFound).length == 6) break;
    }
    
    var toDeploy = [];
    
    toDeploy.push(getNameFromList(rndIndex[0], ["4skill0a", "4skill0b", "4fake1c", "4fake1d", "4skill0e"]));
    toDeploy.push(getNameFromList(rndIndex[1], ["4fake0a", "4skill4b", "4skill3c", "4skill3d", "4skill3e"]));
    toDeploy.push(getNameFromList(rndIndex[2], ["4skill1a", "4skill3b", "4skill4c", "4skill2d", "4skill2e"]));
    toDeploy.push(getNameFromList(rndIndex[3], ["4skill2a", "4fake1b", "4skill0c", "4skill1d", "4fake1e"]));
    toDeploy.push(getNameFromList(rndIndex[4], ["4skill3a", "4skill2b", "4skill1c", "4skill0d", "4fake0e"]));
    toDeploy.push(getNameFromList(rndIndex[5], ["4skill4a", "4skill1b", "4fake0c", "4skill4d", "4skill1e"]));
    toDeploy.push(getNameFromList(rndIndex[6], ["4fake1a", "4skill2c", "4skill4e"]));
    toDeploy.push(getNameFromList(rndIndex[7], ["4fake0b", "4fake0d"]));
    
    var toRandomize = new Array();
    
    for(var i = 0; i < toDeploy.length; i++) {
        var react = map.getReactorByName(toDeploy[i]);
        
        react.setState(1);
        toRandomize.push(react);
    }
    
    map.shuffleReactors(toRandomize);
}

function setup(channel) {
    var eim = em.newInstance("CWKPQ" + channel);
    
    eim.setProperty("current_instance", "0");
    eim.setProperty("glpq1", "0");
    eim.setProperty("glpq2", "0");
    eim.setProperty("glpq3", "0");
    eim.setProperty("glpq3_p", "0");
    eim.setProperty("glpq4", "0");
    eim.setProperty("glpq5", "0");
    eim.setProperty("glpq5_room", "0");
    eim.setProperty("glpq6", "0");
    
    eim.setProperty("glpq_f0", "0");
    eim.setProperty("glpq_f1", "0");
    eim.setProperty("glpq_f2", "0");
    eim.setProperty("glpq_f3", "0");
    eim.setProperty("glpq_f4", "0");
    eim.setProperty("glpq_f5", "0");
    eim.setProperty("glpq_f6", "0");
    eim.setProperty("glpq_f7", "0");
    eim.setProperty("glpq_s", "0");

    var level = 1;
    eim.getInstanceMap(610030100).resetPQ(level);
    eim.getInstanceMap(610030200).resetPQ(level);
    eim.getInstanceMap(610030300).resetPQ(level);
    eim.getInstanceMap(610030400).resetPQ(level);
    eim.getInstanceMap(610030500).resetPQ(level);
    eim.getInstanceMap(610030510).resetPQ(level);
    eim.getInstanceMap(610030520).resetPQ(level);
    eim.getInstanceMap(610030521).resetPQ(level);
    eim.getInstanceMap(610030522).resetPQ(level);
    eim.getInstanceMap(610030530).resetPQ(level);
    eim.getInstanceMap(610030540).resetPQ(level);
    eim.getInstanceMap(610030550).resetPQ(level);
    eim.getInstanceMap(610030600).resetPQ(level);
    eim.getInstanceMap(610030700).resetPQ(level);
    eim.getInstanceMap(610030800).resetPQ(level);
    
    generateMapReactors(eim.getInstanceMap(610030400));
    eim.getInstanceMap(610030550).shuffleReactors();
    
    //add environments
    var a = Array("a", "b", "c", "d", "e", "f", "g", "h", "i");
    var map = eim.getInstanceMap(610030400);
    for (var x = 0; x < a.length; x++) {
        for (var y = 1; y <= 7; y++) {
            if (x == 1 || x == 3 || x == 4 || x == 6 || x == 8) {
                if (y != 2 && y != 4 && y != 5 && y != 7) {
                    map.moveEnvironment(a[x] + "" + y, 1);
                }
            } else {
                map.moveEnvironment(a[x] + "" + y, 1);
            }
        }
    }
    
    var pos_x = Array(944,401,28,-332,-855);
    var pos_y = Array(-204,-384,-504,-384,-204);
    var map = eim.getInstanceMap(610030540);
    for (var z = 0; z < pos_x.length; z++) {
        var mob = em.getMonster(9400594);
        eim.registerMonster(mob);
        map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(pos_x[z], pos_y[z]));
    }
    
    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);
    
    eim.schedule("spawnGuardians", 60000);
    return eim;
}

function playerEntry(eim, player) {
    eim.dropMessage(5, "[Expedition] " + player.getName() + " has entered the map.");
    var map = eim.getMapInstance(610030100 + (eim.getIntProperty("current_instance") * 100));
    player.changeMap(map, map.getPortal(0));
}

function spawnGuardians(eim) {
    var map = eim.getMapInstance(610030100);
    if (map.countPlayers() <= 0) {
	return;
    }
    map.broadcastStringMessage(5, "The Master Guardians have detected you.");
    for (var i = 0; i < 20; i++) { //spawn 20 guardians
	var mob = eim.getMonster(9400594);
	eim.registerMonster(mob);
	map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(1000, 336));
    }
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
    } else {
	switch(mapid) {
	    case 610030200:
		if (eim.getIntProperty("current_instance") == 0) {
		    eim.restartEventTimer(600000); //10 mins
		    eim.setIntProperty("current_instance", 1);
		}
		break;
	    case 610030300:
		if (eim.getIntProperty("current_instance") == 1) {
		    eim.restartEventTimer(600000); //10 mins
		    eim.setIntProperty("current_instance", 2);
		}
		break;
	    case 610030400:
		if (eim.getIntProperty("current_instance") == 2) {
		    eim.restartEventTimer(600000); //10 mins
		    eim.setIntProperty("current_instance", 3);
		}
		break;
	    case 610030500:
		if (eim.getIntProperty("current_instance") == 3) {
		    eim.restartEventTimer(1200000); //20 mins
		    eim.setIntProperty("current_instance", 4);
		}
		break;
	    case 610030600:
		if (eim.getIntProperty("current_instance") == 4) {
		    eim.restartEventTimer(3600000); //1 hr
		    eim.setIntProperty("current_instance", 5);
		}
		break;
	    case 610030800:
		if (eim.getIntProperty("current_instance") == 5) {
		    eim.restartEventTimer(60000); //1 min
		    eim.setIntProperty("current_instance", 6);
		}
		break;
        }
    }
}

function changedLeader(eim, leader) {}

function playerDead(eim, player) {}

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
    eim.stopEventTimer();
    eim.setEventCleared();
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}