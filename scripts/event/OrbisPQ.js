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
 * @event: Orbis PQ
*/

var isPq = true;
var minPlayers = 5, maxPlayers = 6;
var minLevel = 51, maxLevel = 70;
var entryMap = 920010000;
var exitMap = 920011200;
var recruitMap = 200080101;
var clearMap = 920011300;

var minMapId = 920010000;
var maxMapId = 920011300;

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
        var itemSet = [4001044, 4001045, 4001046, 4001047, 4001048, 4001049, 4001050, 4001051, 4001052, 4001053, 4001054, 4001055, 4001056, 4001057, 4001058, 4001059, 4001060, 4001061, 4001062, 4001063];
        eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [2040602, 2040802, 2040002, 2040402, 2040505, 2040502, 2040601, 2044501, 2044701, 2044601, 2041019, 2041016, 2041022, 2041013, 2041007, 2043301, 2040301, 2040801, 2040001, 2040004, 2040504, 2040501, 2040513, 2043101, 2044201, 2044401, 2040701, 2044301, 2043801, 2040401, 2043701, 2040803, 2000003, 2000002, 2000004, 2000006, 2000005, 2022000, 2001001, 2001002, 2022003, 2001000, 2020014, 2020015, 4003000, 1102015, 1102016, 1102017, 1102018, 1102021, 1102022, 1102023, 1102024, 1102084, 1102085, 1102086, 1032019, 1032020, 1032021, 1032014, 2070011, 4010003, 4010000, 4010006, 4010002, 4010005, 4010004, 4010001, 4020001, 4020002, 4020008, 4020007, 4020003, 4020000, 4020004, 4020005, 4020006, 2210000, 2210001, 2210002, 2070006, 2070005, 2070007, 2070004, 2061003, 2060003, 2060004, 2061004, 2100000, 2100001, 2100002, 2100003, 2100004, 2100005];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 100, 100, 15, 80, 5, 25, 20, 20, 25, 20, 15, 10, 45, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 15, 15, 10, 15, 15, 15, 15, 15, 15, 10, 10, 15, 15, 15, 15, 15, 5, 5, 5, 1, 1, 1, 1, 2000, 2000, 2000, 2000, 1, 1, 1, 1, 1, 1];
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
        var eim = em.newInstance("Orbis" + lobbyid);
        eim.setProperty("level", level);
        
        eim.setProperty("statusStg0", -1);
        eim.setProperty("statusStg1", -1);
        eim.setProperty("statusStg2", -1);
        eim.setProperty("statusStg3", -1);
        eim.setProperty("statusStg4", -1);
        eim.setProperty("statusStg5", -1);
        eim.setProperty("statusStg6", -1);
        eim.setProperty("statusStg7", -1);
        eim.setProperty("statusStg8", -1);
        eim.setProperty("statusStg2_c", 0);
        eim.setProperty("statusStg7_c", 0);
        eim.setProperty("statusStgBonus", 0);
        
        
        eim.getInstanceMap(920010000).resetPQ(level);
        eim.getInstanceMap(920010100).resetPQ(level);
        eim.getInstanceMap(920010200).resetPQ(level);
        eim.getInstanceMap(920010300).resetPQ(level);
        eim.getInstanceMap(920010400).resetPQ(level);
        eim.getInstanceMap(920010500).resetPQ(level);
        eim.getInstanceMap(920010600).resetPQ(level);
        eim.getInstanceMap(920010601).resetPQ(level);
        eim.getInstanceMap(920010602).resetPQ(level);
        eim.getInstanceMap(920010603).resetPQ(level);
        eim.getInstanceMap(920010604).resetPQ(level);
        eim.getInstanceMap(920010700).resetPQ(level);
        eim.getInstanceMap(920010800).resetPQ(level);
        eim.getInstanceMap(920010900).resetPQ(level);
        eim.getInstanceMap(920010910).resetPQ(level);
        eim.getInstanceMap(920010911).resetPQ(level);
        eim.getInstanceMap(920010912).resetPQ(level);
        eim.getInstanceMap(920010920).resetPQ(level);
        eim.getInstanceMap(920010921).resetPQ(level);
        eim.getInstanceMap(920010922).resetPQ(level);
        eim.getInstanceMap(920010930).resetPQ(level);
        eim.getInstanceMap(920010931).resetPQ(level);
        eim.getInstanceMap(920010932).resetPQ(level);
        eim.getInstanceMap(920011000).resetPQ(level);
        eim.getInstanceMap(920011100).resetPQ(level);
        eim.getInstanceMap(920011200).resetPQ(level);
        eim.getInstanceMap(920011300).resetPQ(level);
        
        respawnStages(eim);
        
        var d = new Date();
        eim.getInstanceMap(920010400).getReactorByName("music").setEventState(d.getDay());
        
        eim.startEventTimer(eventTime * 60000);
        setEventRewards(eim);
        setEventExclusives(eim);
        
        return eim;
}

function isTeamAllJobs(eim) {
        var eventJobs = eim.getEventPlayersJobs();
        var rangeJobs = parseInt('111110', 2);
        
        return ((eventJobs & rangeJobs) == rangeJobs);
}

function afterSetup(eim) {
        if(isTeamAllJobs(eim)) {
                var rnd = Math.floor(Math.random() * 4);
                eim.applyEventPlayersItemBuff(2022090 + rnd);
        }
}

function respawnStages(eim) {}

function playerEntry(eim, player) {
        var map = eim.getMapInstance(entryMap);
        player.changeMap(map, map.getPortal(0));
        
        var texttt = "Hi, my name is Eak, the Chamberlain of the Goddess. Don't be alarmed; you won't be able to see me right now. Back when the Goddess turned into a block of stone, I simultaneously lost my own power. If you gather up the power of the Magic Cloud of Orbis, however, then I'll be able to recover my body and re-transform back to my original self. Please collect #b20#k Magic Clouds and bring them back to me. Right now, you'll only see me as a tiny, flickering light.";
        player.getClient().announce(Packages.tools.MaplePacketCreator.getNPCTalk(2013001, /*(byte)*/ 0, texttt, "00 00", /*(byte)*/ 0));
}

function scheduledTimeout(eim) {
        if(eim.getIntProperty("statusStg8") == 1) {
                eim.warpEventTeam(920011300);
        }
        else {
                end(eim);
        }
}

function playerUnregistered(eim, player) {
        player.cancelEffect(2022090);
        player.cancelEffect(2022091);
        player.cancelEffect(2022092);
        player.cancelEffect(2022093);
}

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
