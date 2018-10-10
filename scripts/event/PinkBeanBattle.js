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
 * @event: Pink Bean Battle
*/

importPackage(Packages.server.life);
importPackage(Packages.client.inventory);

var isPq = true;
var minPlayers = 6, maxPlayers = 30;
var minLevel = 120, maxLevel = 255;
var entryMap = 270050100;
var exitMap = 270050300;
var recruitMap = 270050000;
var clearMap = 270050300;

var minMapId = 270050100;
var maxMapId = 270050300;

var eventTime = 140;     // 140 minutes

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
        itemSet = [];
        itemQty = [];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [];    //bonus exp given on CLEAR stage signal
        eim.setEventClearStageExp(expStages);
        
        mesoStages = [];    //bonus meso given on CLEAR stage signal
        eim.setEventClearStageMeso(mesoStages);
}

function afterSetup(eim) {
    eim.dropMessage(5, "The first wave will start within 15 seconds, prepare yourselves.");
    eim.schedule("startWave", 15 * 1000);
}

function setup(channel) {
    var eim = em.newInstance("PinkBean" + channel);
    eim.setProperty("canJoin", 1);
    eim.setProperty("defeatedBoss", 0);
    eim.setProperty("fallenPlayers", 0);
    
    eim.setProperty("stage", 1);
    eim.setProperty("channel", channel);

    var level = 1;
    eim.getInstanceMap(270050100).resetPQ(level);
    eim.getInstanceMap(270050200).resetPQ(level);
    eim.getInstanceMap(270050300).resetPQ(level);
    
    var mob = MapleLifeFactory.getMonster(8820000);
    mob.disableDrops();
    eim.getInstanceMap(270050100).spawnMonsterOnGroundBelow(mob, new java.awt.Point(0, -42));
    
    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);
    
    return eim;
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
            eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the expedition.");
            eim.unregisterPlayer(player);
        }
    }
}

function changedLeader(eim, leader) {}

function playerDead(eim, player) {
    var count = eim.getIntProperty("fallenPlayers");
    count = count + 1;
    
    eim.setIntProperty("fallenPlayers", count);
    
    if(count == 5) {
        eim.dropMessage(5, "[Expedition] Too many players have fallen, Pink Bean is now deemed undefeatable; the expedition is over.");
        end(eim);
    } else if(count == 4) {
        eim.dropMessage(5, "[Expedition] Pink Bean is growing stronger than ever, last stand mode everyone!");
    } else if(count == 3) {
        eim.dropMessage(5, "[Expedition] Casualty count is starting to get out of control. Battle with care.");
    }
}

function playerRevive(eim, player) {
    return true;
}

function monsterRevive(eim, mob) {
        if(isPinkBean(mob)) {
                mob.enableDrops();
        }
}

function playerDisconnected(eim, player) {
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
        eim.dropMessage(5, "[Expedition] Either the leader has quit the expedition or there is no longer the minimum number of members required to continue it.");
        eim.unregisterPlayer(player);
        end(eim);
    }
    else {
        eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the expedition.");
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

function isPinkBean(mob) {
    var mobid = mob.getId();
    return (mobid == 8820001);
}

function isJrBoss(mob) {
    var mobid = mob.getId();
    return (mobid >= 8820002 && mobid <= 8820006);
}

function noJrBossesLeft(map) {
    return map.countMonster(8820002, 8820006) == 0;
}

function spawnJrBoss(mobObj, gotKilled) {
    if(gotKilled) {
        spawnid = mobObj.getId() + 17;
        
    } else {
        mobObj.getMap().killMonster(mobObj.getId());
        spawnid = mobObj.getId() - 17;
    }
    
    var mob = MapleLifeFactory.getMonster(spawnid);
    mobObj.getMap().spawnMonsterOnGroundBelow(mob, mobObj.getPosition());
}

function monsterKilled(mob, eim) {
    if(isPinkBean(mob)) {
        eim.setIntProperty("defeatedBoss", 1);
        eim.showClearEffect(mob.getMap().getId());
        mob.getMap().killAllMonsters();
        eim.clearPQ();
        
        var ch = eim.getIntProperty("channel");
        mob.getMap().broadcastPinkBeanVictory(ch);
    } else if(isJrBoss(mob)) {
        if(noJrBossesLeft(mob.getMap())) {
            var stage = eim.getIntProperty("stage");
            
            if(stage == 5) {
                var iid = 4001193;
                var itemObj = new Item(iid, 0, 1);
                var mapObj = eim.getMapFactory().getMap(270050100);
                var reactObj = mapObj.getReactorById(2708000);
                var dropper = eim.getPlayers().get(0);
                mapObj.spawnItemDrop(dropper, dropper, itemObj, reactObj.getPosition(), true, true);


                eim.dropMessage(6, "With the last of its guardians fallen, Pink Bean loses its invulnerability. The real fight starts now!");
            } else {
                stage++;
                eim.setIntProperty("stage", stage);
                
                eim.dropMessage(5, "The next wave will start within 15 seconds, prepare yourselves.");
                eim.schedule("startWave", 15 * 1000);
            }
        }
    } 
}

function startWave(eim) {
    var mapObj = eim.getMapInstance(270050100);
    var stage = eim.getProperty("stage");
    
    for(var i = 1; i <= stage; i++) {
        spawnJrBoss(mapObj.getMonsterById(8820019 + (i % 5)), false);
    }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
