/**
 * @author: kevintjuh93
 * @author: Ronan
*/

importPackage(Packages.tools);

var isPq = true;
var minPlayers = 1, maxPlayers = 1;
var minLevel = 12, maxLevel = 255;
var entryMap = 922240000;
var exitMap = 922240200;
var recruitMap = 922240200;

var minMapId = 922240000;
var maxMapId = 922240100;

var eventTime = 3;         // 3 minutes

var lobbyRange = [0, 19];

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
        var eim = em.newInstance("RescueGaga_" + lobbyid);
        eim.setProperty("level", level);
        eim.setProperty("stage", "0");
        eim.setProperty("falls", "0");
        
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
        
        player.announce(MaplePacketCreator.showEffect("event/space/start"));
        player.startMapEffect("Please rescue Gaga within the time limit.", 5120027);
}

function scheduledTimeout(eim) {
        end(eim);
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
        eim.unregisterPlayer(player);
        player.changeMap(exitMap, 0);
}

function changedMap(eim, player, mapid) {
        if (mapid < minMapId || mapid > maxMapId) {
                if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                        eim.unregisterPlayer(player);
                        
                        player.changeMap(mapid, 0);
                        player.cancelEffect(2360002);
                        
                        end(eim);
                } else {
                        eim.unregisterPlayer(player);
                        
                        player.changeMap(mapid, 0);
                        player.cancelEffect(2360002);
                }
        } else if (mapid == maxMapId) {
                eim.clearPQ();
                
                var rgaga = player.getEvents().get("rescueGaga");
                rgaga.complete();
        }
}

function afterChangedMap(eim, player, mapid) {
        if (mapid == minMapId) {
                player.getAbstractPlayerInteraction().useItem(2360002);//HOORAY <3
        } else {
                player.cancelEffect(2360002);
        }
}

function changedLeader(eim, leader) {}

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
        
        eim.schedule("spawnGrandpaBunny", 10 * 1000);
}

function spawnGrandpaBunny(eim) {
        eim.spawnNpc(9001105, new java.awt.Point(175, -20), eim.getInstanceMap(maxMapId));
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
