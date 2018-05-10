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
 * @event: Magatia PQ (Zenumist)
*/

importPackage(Packages.server.life);

var isPq = true;
var minPlayers = 4, maxPlayers = 4;
var minLevel = 71, maxLevel = 85;
var entryMap = 926100000;
var exitMap = 926100700;
var recruitMap = 261000011;
var clearMap = 926100700;

var minMapId = 926100000;
var maxMapId = 926100600;

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
        var itemSet = [4001130, 4001131, 4001132, 4001133, 4001134, 4001135];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [2000003, 2000002, 2000004, 2000005, 2022003, 1032016, 1032015, 1032014, 2041212, 2041020, 2040502, 2041016, 2044701, 2040301, 2043201, 2040501, 2040704, 2044001, 2043701, 2040803, 1102026, 1102028, 1102029];
        itemQty = [100, 100, 20, 10, 50, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [0, 10000, 20000, 0, 20000, 20000, 0, 0];    //bonus exp given on CLEAR stage signal
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
        var eim = em.newInstance("MagatiaZ" + lobbyid);
        eim.setProperty("level", level);
        
        eim.setIntProperty("isAlcadno", 0);
        
        eim.setIntProperty("escortFail", 0);
        eim.setIntProperty("yuleteTimeout", 0);
        eim.setIntProperty("yuleteTalked", 0);
        eim.setIntProperty("yuletePassed", 0);
        eim.setIntProperty("npcShocked", 0);
        eim.setIntProperty("normalClear", 0);
        
        eim.setIntProperty("statusStg1", 0);
        eim.setIntProperty("statusStg2", 0);
        eim.setIntProperty("statusStg3", 0);
        eim.setIntProperty("statusStg4", 0);
        eim.setIntProperty("statusStg5", 0);
        eim.setIntProperty("statusStg6", 0);
        eim.setIntProperty("statusStg7", 0);
        
        eim.getInstanceMap(926100000).resetPQ(level);
        eim.getInstanceMap(926100001).resetPQ(level);
        eim.getInstanceMap(926100100).resetPQ(level);
        eim.getInstanceMap(926100200).resetPQ(level);
        eim.getInstanceMap(926100201).resetPQ(level);
        eim.getInstanceMap(926100202).resetPQ(level);
        eim.getInstanceMap(926100203).resetPQ(level);
        eim.getInstanceMap(926100300).resetPQ(level);
        eim.getInstanceMap(926100301).resetPQ(level);
        eim.getInstanceMap(926100302).resetPQ(level);
        eim.getInstanceMap(926100303).resetPQ(level);
        eim.getInstanceMap(926100304).resetPQ(level);
        eim.getInstanceMap(926100400).resetPQ(level);
        eim.getInstanceMap(926100401).resetPQ(level);
        eim.getInstanceMap(926100500).resetPQ(level);
        eim.getInstanceMap(926100600).resetPQ(level);
        eim.getInstanceMap(926100700).resetPQ(level);
        
        eim.getInstanceMap(926100201).shuffleReactors(2518000, 2612004);
        eim.getInstanceMap(926100202).shuffleReactors(2518000, 2612004);
        
        eim.spawnNpc(2112000, new java.awt.Point(252, 243), eim.getInstanceMap(926100203));
        eim.spawnNpc(2112000, new java.awt.Point(200, 100), eim.getInstanceMap(926100401));
        eim.spawnNpc(2112001, new java.awt.Point(200, 100), eim.getInstanceMap(926100500));
        eim.spawnNpc(2112018, new java.awt.Point(200, 100), eim.getInstanceMap(926100600));
        
        respawnStages(eim);
        eim.startEventTimer(eventTime * 60000);
        setEventRewards(eim);
        setEventExclusives(eim);
        return eim;
}

function shuffle(array) {
    var currentIndex = array.length, temporaryValue, randomIndex;

    // While there remain elements to shuffle...
    while (0 !== currentIndex) {

        // Pick a remaining element...
        randomIndex = Math.floor(Math.random() * currentIndex);
        currentIndex -= 1;

        // And swap it with the current element.
        temporaryValue = array[currentIndex];
        array[currentIndex] = array[randomIndex];
        array[randomIndex] = temporaryValue;
    }

    return array;
}

function afterSetup(eim) {
        eim.setIntProperty("escortFail", 0);    // refresh friendly status
    
        var books = [-1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 2, 3];
        shuffle(books);
    
        eim.setIntProperty("stg1_b0",  books[0]);
        eim.setIntProperty("stg1_b1",  books[1]);
        eim.setIntProperty("stg1_b2",  books[2]);
        eim.setIntProperty("stg1_b3",  books[3]);
        eim.setIntProperty("stg1_b4",  books[4]);
        eim.setIntProperty("stg1_b5",  books[5]);
        eim.setIntProperty("stg1_b6",  books[6]);
        eim.setIntProperty("stg1_b7",  books[7]);
        eim.setIntProperty("stg1_b8",  books[8]);
        eim.setIntProperty("stg1_b9",  books[9]);
        eim.setIntProperty("stg1_b10", books[10]);
        eim.setIntProperty("stg1_b11", books[11]);
        eim.setIntProperty("stg1_b12", books[12]);
        eim.setIntProperty("stg1_b13", books[13]);
        eim.setIntProperty("stg1_b14", books[14]);
        eim.setIntProperty("stg1_b15", books[15]);
        eim.setIntProperty("stg1_b16", books[16]);
        eim.setIntProperty("stg1_b17", books[17]);
        eim.setIntProperty("stg1_b18", books[18]);
        eim.setIntProperty("stg1_b19", books[19]);
        eim.setIntProperty("stg1_b20", books[20]);
        eim.setIntProperty("stg1_b21", books[21]);
        eim.setIntProperty("stg1_b22", books[22]);
        eim.setIntProperty("stg1_b23", books[23]);
        eim.setIntProperty("stg1_b24", books[24]);
        eim.setIntProperty("stg1_b25", books[25]);
}

function respawnStages(eim) {    
        eim.getMapInstance(926100100).instanceMapRespawn();
        eim.getMapInstance(926100200).instanceMapRespawn();
        
        if(!eim.isEventCleared()) {
            var mapobj = eim.getMapInstance(926100401);
            var mobcount = mapobj.countMonster(9300150);
            var mobobj;
            if(mobcount == 0) {
                mobobj = MapleLifeFactory.getMonster(9300150);
                mapobj.spawnMonsterOnGroundBelow(mobobj, new Packages.java.awt.Point(-278, -126));

                mobobj = MapleLifeFactory.getMonster(9300150);
                mapobj.spawnMonsterOnGroundBelow(mobobj, new Packages.java.awt.Point(-542, -126));
            } else if(mobcount == 1) {
                mobobj = MapleLifeFactory.getMonster(9300150);
                mapobj.spawnMonsterOnGroundBelow(mobobj, new Packages.java.awt.Point(-542, -126));
            }
        }
        
        eim.schedule("respawnStages", 15 * 1000);
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
                    
        } else if(mapid == 926100203 && eim.getIntProperty("yuleteTimeout") == 0) {
                eim.setIntProperty("yuleteTimeout", 1);
                eim.schedule("yuleteAction", 10 * 1000);
        }
}

function yuleteAction(eim) {
        if(eim.getIntProperty("yuleteTalked") == 1) {
                eim.setIntProperty("yuletePassed", 1);
                
                eim.dropMessage(5, "Yulete: Ugh, you guys disgust me. All I desired was to make this nation the greatest alchemy powerhouse of the entire world. If they won't accept this, I will make it true by myself, at any costs!!!");
        } else {
                eim.dropMessage(5, "Yulete: Hahaha... Did you really think I was going to be so disprepared knowing that the Magatia societies' dogs would be coming in my pursuit after my actions? Fools!");
        }
        eim.setIntProperty("yuleteTalked", -1);
        
        var mapobj = eim.getMapInstance(926100203);
        var mob1 = 9300143, mob2 = 9300144;
        
        mapobj.destroyNPC(2112000);
        
        var mobobj1, mobobj2;
        for(var i = 0; i < 5; i++) {
                mobobj1 = MapleLifeFactory.getMonster(mob1);
                mobobj2 = MapleLifeFactory.getMonster(mob2);
            
                mapobj.spawnMonsterOnGroundBelow(mobobj1, new Packages.java.awt.Point(-455, 135));
                mapobj.spawnMonsterOnGroundBelow(mobobj2, new Packages.java.awt.Point(-455, 135));
        }
        

        for(var i = 0; i < 5; i++) {
                mobobj1 = MapleLifeFactory.getMonster(mob1);
                mobobj2 = MapleLifeFactory.getMonster(mob2);
            
                mapobj.spawnMonsterOnGroundBelow(mobobj1, new Packages.java.awt.Point(0, 135));
                mapobj.spawnMonsterOnGroundBelow(mobobj2, new Packages.java.awt.Point(0, 135));
        }
        
        
        for(var i = 0; i < 5; i++) {
                mobobj1 = MapleLifeFactory.getMonster(mob1);
                mobobj2 = MapleLifeFactory.getMonster(mob2);
            
                mapobj.spawnMonsterOnGroundBelow(mobobj1, new Packages.java.awt.Point(360, 135));
                mapobj.spawnMonsterOnGroundBelow(mobobj2, new Packages.java.awt.Point(360, 135));
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

function monsterKilled(mob, eim) {
        var map = mob.getMap();
    
        if(map.getId() == 926100001 && eim.getIntProperty("statusStg1") == 1) {
                if(map.countMonsters() == 0) {
                        eim.showClearEffect();
                        eim.giveEventPlayersStageReward(2);
                        eim.setIntProperty("statusStg2", 1);
                }
        }
        else if(map.getId() == 926100203 && eim.getIntProperty("statusStg1") == 1) {
                if(map.countMonsters() == 0) {
                        eim.showClearEffect();
                        eim.giveEventPlayersStageReward(5);
                        
                        map.getReactorByName("rnj6_out").forceHitReactor(1);
                }
        } else if(mob.getId() == 9300139 || mob.getId() == 9300140) {
                eim.showClearEffect();
                eim.giveEventPlayersStageReward(7);

                eim.spawnNpc(2112006, new java.awt.Point(-370, -150), map);

                var gain = (eim.getIntProperty("escortFail") == 1) ? 90000 : ((mob.getId() == 9300139) ? 105000 : 140000);
                eim.giveEventPlayersExp(gain);
                
                map.killAllMonstersNotFriendly();
                
                if(mob.getId() == 9300139) {
                        eim.setIntProperty("normalClear", 1);
                }
                
                eim.clearPQ();
        }
}

function friendlyKilled(mob, eim) {
        eim.setIntProperty("escortFail", 1);
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
