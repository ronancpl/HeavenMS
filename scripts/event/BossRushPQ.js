var isPq = true;
var minPlayers = 1, maxPlayers = 6;
var minLevel = 1, maxLevel = 200;
var entryMap = 970030100;
var exitMap = 970030000;
var recruitMap = 970030000;
var clearMap = 970030000;

function init() {
        em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function setEventRewards(eim) {
        var itemSet, itemQty, evLevel;

        evLevel = 6;    //Rewards at event completion
        itemSet = [1122018, 1122005, 1022088, 1402013, 1032048, 1032070, 1102046, 2330004, 2041013, 2041016, 2041019, 2041022, 2049100, 2049003, 2020012, 2020013, 2020014, 2020015, 2022029, 2022045, 2022068, 2022069, 2022179, 2022180, 4004000, 4004001, 4004002, 4004003, 4004004, 4003000];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 25, 25, 25, 25, 25, 25, 25, 25, 4, 4, 12, 12, 12, 12, 12, 25];
        eim.setEventRewards(evLevel, itemSet, itemQty);

        evLevel = 5;    //Rewards at Rest Spot V
        itemSet = [1122018, 1122005, 1022088, 1402013, 1032048, 1032070, 1102046, 2330004, 2041013, 2041016, 2041019, 2041022, 2049100, 2049003, 2020012, 2020013, 2020014, 2020015, 2022029, 2022045, 2022068, 2022069, 2022179, 2022180, 4004000, 4004001, 4004002, 4004003, 4004004, 4003000];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 15, 15, 15, 15, 15, 15, 15, 15, 15, 2, 8, 8, 8, 8, 8, 12];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        evLevel = 4;    //Rewards at Rest Spot IV
        itemSet = [1122001, 1122006, 1022103, 1442065, 1032042, 1032021, 1102168, 2070005, 2040025, 2040029, 2040301, 2040413, 2040701, 2040817, 2002028, 2020009, 2020010, 2020011, 2022004, 2022005, 2022025, 2022027, 2022048, 2022049, 4020000, 4020001, 4020002, 4020003, 4020004, 4020005, 4020006, 4020007, 4020008, 4003000];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        evLevel = 3;    //Rewards at Rest Spot III
        itemSet = [1122002, 1022088, 1012076, 1402029, 1032041, 1032044, 1102167, 2070011, 2040026, 2040030, 2040302, 2040412, 2040702, 2040818, 2002028, 2020009, 2020010, 2020011, 2022004, 2022005, 2022025, 2022027, 2022048, 2022049, 4010000, 4010001, 4010002, 4010003, 4010004, 4010005, 4010006, 4010007, 4003000];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 5, 5, 5, 5, 5, 5, 5, 5, 5];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        evLevel = 2;    //Rewards at Rest Spot II
        itemSet = [1122003, 1012077, 1012079, 1432014, 1032059, 1032002, 1102191, 2330002, 2040001, 2040311, 2040401, 2040601, 2040824, 2040901, 2010000, 2010001, 2010002, 2010003, 2010004, 2020001, 2020002, 2020003, 2022020, 2022022, 4020000, 4020001, 4020002, 4020003, 4020004, 4020005, 4020006, 4020007, 4020008, 4003000];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3];
        eim.setEventRewards(evLevel, itemSet, itemQty);
        
        evLevel = 1;    //Rewards at Rest Spot I
        itemSet = [1122004, 1012078, 1432008, 1432009, 1032040, 1032009, 1102166, 2070001, 2040002, 2040310, 2040400, 2040600, 2040825, 2040902, 2010000, 2010001, 2010002, 2010003, 2010004, 2020001, 2020002, 2020003, 2022020, 2022022, 4010000, 4010001, 4010002, 4010003, 4010004, 4010005, 4010006, 4010007, 4003000];
        itemQty = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 2, 2, 2, 2, 2, 2, 2, 2, 2];
        eim.setEventRewards(evLevel, itemSet, itemQty);
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
        
        var eim = em.newInstance("BossRush" + leaderid);
        eim.setProperty("level", level);
        
        eim.startEventTimer(45 * 60000); //45 mins
        setEventRewards(eim);
        return eim;
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
        if (mapid < 970030001 || mapid > 970042711) {
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
        if (eim.isEventTeamLackingNow(true, minPlayers, player))
                end(eim);
        else
                playerExit(eim, player);
}

function leftParty(eim, player) {
        if (eim.isEventTeamLackingNow(false, minPlayers, player))
                end(eim);
        else
                playerExit(eim, player);
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

function clearPQ(eim) {
        eim.stopEventTimer();
        eim.setEventCleared();      // from now on event just finishes when ALL players gets out of the range defined inside changedMap function.
        
        em.schedule("reopenEvent", 10 * 1000);     // leaders have 10 seconds cooldown to reach recruit map and retry for a new PQ.
}

function giveRandomEventReward(eim, player) {
        eim.giveEventReward(player);
}

function reopenEvent() {
        em.setProperty("state", "0");
        em.setProperty("leader", "true");
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
