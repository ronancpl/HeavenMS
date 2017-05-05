var isPq = true;
var minPlayers = 1, maxPlayers = 6;
var minLevel = 1, maxLevel = 200;
var entryMap = 103000800;
var exitMap = 103000890;
var recruitMap = 103000000;
var clearMap = 103000805;

function init() {
        em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel, expStages;

        evLevel = 1;    //Rewards at clear PQ
        itemSet = [2040505, 2040514, 2040502, 2040002, 2040602, 2040402, 2040802, 1032009, 1032004, 1032005, 1032006, 1032007, 1032010, 1032002, 1002026, 1002089, 1002090, 2000003, 2000001, 2000002, 2000006, 2022003, 2022000, 2000004, 4003000, 4010000, 4010001, 4010002, 4010003, 4010004, 4010005, 4010006, 4010007, 4020000, 4020001, 4020002, 4020003, 4020004, 4020005, 4020006, 4020007, 4020008];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 80, 80, 80, 50, 5, 15, 15, 30, 15, 15, 15, 15, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 3, 3];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        expStages = [100, 200, 400, 800, 1500];    //bonus exp given on CLEAR stage signal
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

function setup(level, leaderid) {
        em.setProperty("state", "1");
	em.setProperty("leader", "true");
        
        var eim = em.newInstance("Kerning" + leaderid);
        eim.setProperty("level", level);
        
        respawnStg1(eim);
        eim.startEventTimer(30 * 60000); //30 mins
        setEventRewards(eim);
        return eim;
}

function respawnStg1(eim) {    
        eim.getMapInstance(103000800).instanceMapRespawn();
        eim.schedule("respawnStg1", 10 * 1000);
}

function playerEntry(eim, player) {
        var map = eim.getMapInstance(entryMap);
        player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
        end(eim);
}

function playerExit(eim, player) {
        eim.unregisterPlayer(player);
        player.changeMap(exitMap, 0);
}

function changedMap(eim, player, mapid) {
        if (mapid < 103000800 || mapid > 103000805) {
                if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                        eim.unregisterPlayer(player);
                        end(eim);
                }
                else
                        eim.unregisterPlayer(player);
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
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                eim.unregisterPlayer(player);
}

function disbandParty(eim) {
        end(eim);
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
        
        em.schedule("reopenEvent", 10 * 1000);     // leaders have 10 seconds cooldown to reach recruit map and retry for a new PQ.
}

function giveRandomEventReward(eim, player) {
        eim.giveEventReward(player);
}

function clearPQ(eim) {
        eim.stopEventTimer();
        eim.setEventCleared();
        
        em.schedule("reopenEvent", 10 * 1000);     // leaders have 10 seconds cooldown to reach recruit map and retry for a new PQ.
}

function reopenEvent() {
        em.setProperty("state", "0");
        em.setProperty("leader", "true");
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
