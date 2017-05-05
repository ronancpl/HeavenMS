var isPq = true;
var minPlayers = 1, maxPlayers = 6;
var minLevel = 1, maxLevel = 200;
var entryMap = 925100000;
var exitMap = 925100700;
var recruitMap = 251010404;
var clearMap = 925100600;

function init() {
        em.setProperty("state", "0");
	em.setProperty("leader", "true");
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
        
        var eim = em.newInstance("Pirate" + leaderid);
        eim.setProperty("level", level);
        
	em.setProperty("stage2", "0");
        em.setProperty("stage2a", "0");
	em.setProperty("stage3a", "0");
        em.setProperty("stage2b", "0");
	em.setProperty("stage3b", "0");
	em.setProperty("stage4", "0");
	em.setProperty("stage5", "0");
        
        em.setProperty("openedChests", "0");
        eim.setInstanceMap(925100000).resetPQ(level);
        eim.setInstanceMap(925100000).shuffleReactors();
        
	eim.setInstanceMap(925100100).resetPQ(level);
	var map = eim.setInstanceMap(925100200);
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
	map = eim.setInstanceMap(925100201);
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
	eim.setInstanceMap(925100202).resetPQ(level);
	map = eim.setInstanceMap(925100300);
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
	map = eim.setInstanceMap(925100301);
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
	eim.setInstanceMap(925100302).resetPQ(level);
	eim.setInstanceMap(925100400).resetPQ(level);
	eim.setInstanceMap(925100500).resetPQ(level);
        
        respawnStg4(eim);
        eim.startEventTimer(20 * 60000); //20 mins
        return eim;
}

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

function playerExit(eim, player) {
        eim.unregisterPlayer(player);
        player.changeMap(exitMap, 0);
}

function changedMap(eim, player, mapid) {
        if (mapid < 925100000 || mapid > 925100500) {
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
        eim.setEventCleared();
        eim.warpEventTeam(toMap);
        
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
