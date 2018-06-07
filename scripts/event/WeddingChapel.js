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
 * @event: Chapel Wedding
*/

var entryMap = 680000100;
var exitMap = 680000500;
var recruitMap = 680000000;
var clearMap = 680000500;

var minMapId = 680000100;
var maxMapId = 680000401;

var startMsgTime = 4;
var blessMsgTime = 5;

var eventTime = 10;    // 10 minutes gathering
var ceremonyTime = 20; // 20 minutes ceremony
var blessingsTime = 15;// blessings are held until the 15th minute from the ceremony start
var partyTime = 45;    // 45 minutes party

var forceHideMsgTime = 10;  // unfortunately, EIM weddings don't send wedding talk packets to the server... this will need to suffice

var eventBoss = true;   // spawns a Cake boss at the hunting ground
var isCathedral = false;

var lobbyRange = [0, 0];

function init() {}

function setLobbyRange() {
        return lobbyRange;
}

function setEventExclusives(eim) {
        var itemSet = [4031217, 4000313];    // golden key, golden maple leaf
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

function spawnCakeBoss(eim) {
        var mapObj = eim.getMapInstance(680000400);
        var mobObj = Packages.server.life.MapleLifeFactory.getMonster(9400606);

        mapObj.spawnMonsterOnGroundBelow(mobObj, new Packages.java.awt.Point(777, -177));
}

function setup(level, lobbyid) {
        var eim = em.newInstance("Wedding" + lobbyid);
        eim.setProperty("weddingId", "0");
        eim.setProperty("weddingStage", "0");   // 0: gathering time, 1: wedding time, 2: ready to fulfill the wedding, 3: just married
        eim.setProperty("guestBlessings", "0");
        eim.setProperty("isPremium", "1");
        eim.setProperty("canJoin", "1");
        eim.setProperty("groomId", "0");
        eim.setProperty("brideId", "0");
        eim.setProperty("confirmedVows", "-1");
        
        eim.getInstanceMap(680000400).resetPQ(level);
        if(eventBoss) spawnCakeBoss(eim);
        
        respawnStages(eim);
        eim.startEventTimer(eventTime * 60000);
        setEventRewards(eim);
        setEventExclusives(eim);
        return eim;
}

function afterSetup(eim) {}

function respawnStages(eim) {    
        eim.getMapInstance(680000400).instanceMapRespawn();
        eim.schedule("respawnStages", 15 * 1000);
}

function playerEntry(eim, player) {
        var map = eim.getMapInstance(entryMap);
        
        player.getClient().getAbstractPlayerInteraction().gainItem(4000313, 1);
        player.changeMap(map, map.getPortal(0));
}

function stopBlessings(eim) {
        var mapobj = eim.getMapInstance(entryMap + 10);
        mapobj.dropMessage(6, "Wedding Assistant: Alright people, our couple are preparing their vows to each other right now.");
        
        eim.setIntProperty("weddingStage", 2);
}

function sendWeddingAction(eim, type) {
        var chr = eim.getLeader();
        if(chr.getGender() == 0) {
                chr.getMap().broadcastMessage(Packages.tools.packets.Wedding.OnWeddingProgress(type == 2, eim.getIntProperty("groomId"), eim.getIntProperty("brideId"), type + 1));
        } else {
                chr.getMap().broadcastMessage(Packages.tools.packets.Wedding.OnWeddingProgress(type == 2, eim.getIntProperty("brideId"), eim.getIntProperty("groomId"), type + 1));
        }
}

function hidePriestMsg(eim) {
        sendWeddingAction(eim, 2);
}

function showStartMsg(eim) {
        eim.getMapInstance(entryMap + 10).broadcastMessage(Packages.tools.packets.Wedding.OnWeddingProgress(false, 0, 0, 0));
        eim.schedule("hidePriestMsg", forceHideMsgTime * 1000);
}

function showBlessMsg(eim) {
        eim.getMapInstance(entryMap + 10).broadcastMessage(Packages.tools.packets.Wedding.OnWeddingProgress(false, 0, 0, 1));
        eim.setIntProperty("guestBlessings", 1);
        eim.schedule("hidePriestMsg", forceHideMsgTime * 1000);
}

function showMarriedMsg(eim) {
        sendWeddingAction(eim, 1);
        eim.schedule("hidePriestMsg", 10 * 1000);
        
        eim.restartEventTimer(partyTime * 60000);
}

function scheduledTimeout(eim) {
        if(eim.getIntProperty("canJoin") == 1) {
                em.getChannelServer().closeOngoingWedding(isCathedral);
                eim.setIntProperty("canJoin", 0);
                
                var mapobj = eim.getMapInstance(entryMap);
                var chr = mapobj.getCharacterById(eim.getIntProperty("groomId"));
                if(chr != null) {
                        chr.changeMap(entryMap + 10, "we00");
                }
                
                chr = mapobj.getCharacterById(eim.getIntProperty("brideId"));
                if(chr != null) {
                        chr.changeMap(entryMap + 10, "we00");
                }
                
                mapobj.dropMessage(6, "Wedding Assistant: The couple are heading to the altar, hurry hurry talk to me to arrange your seat.");

                eim.setIntProperty("weddingStage", 1);
                eim.schedule("showStartMsg", startMsgTime * 60 * 1000);
                eim.schedule("showBlessMsg", blessMsgTime * 60 * 1000);
                eim.schedule("stopBlessings", blessingsTime * 60 * 1000);
                eim.startEventTimer(ceremonyTime * 60000);
        } else {
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

function isMarrying(eim, player) {
        var playerid = player.getId();
        return playerid == eim.getIntProperty("groomId") || playerid == eim.getIntProperty("brideId");
}

function changedMap(eim, player, mapid) {
        if (mapid < minMapId || mapid > maxMapId) {
                if (isMarrying(eim, player)) {
                        eim.unregisterPlayer(player);
                        end(eim);
                }
                else
                        eim.unregisterPlayer(player);
        }
}

function changedLeader(eim, leader) {}

function playerDead(eim, player) {}

function playerRevive(eim, player) { // player presses ok on the death pop up.
        if (isMarrying(eim, player)) {
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                eim.unregisterPlayer(player);
}

function playerDisconnected(eim, player) {
        if (isMarrying(eim, player)) {
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                eim.unregisterPlayer(player);
}

function leftParty(eim, player) {}

function disbandParty(eim) {}

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

function isCakeBoss(mob) {
        return mob.getId() == 9400606;
}

function monsterKilled(mob, eim) {
        if(isCakeBoss(mob)) {
                eim.showClearEffect();
                eim.clearPQ();
        }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}

