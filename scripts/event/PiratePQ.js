var isPq = true;
var minPlayers = 1;
var entryMap = 925100000;
var exitMap = 925100700;

function init() {
        em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function setup(level, leaderid) {
        em.setProperty("state", "1");
	em.setProperty("leader", "true");
        var eim = em.newInstance("Pirate" + leaderid);
	em.setProperty("stage2", "0");
        em.setProperty("stage2a", "0");
	em.setProperty("stage3a", "0");
        em.setProperty("stage2b", "0");
	em.setProperty("stage3b", "0");
	em.setProperty("stage4", "0");
	em.setProperty("stage5", "0");
        em.setProperty("level", level);
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

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    end(eim);
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
}

function changedMap(eim, player, mapid) {
    if (mapid < 925100000 || mapid > 925100500) {
	eim.unregisterPlayer(player);

	if (eim.disposeIfPlayerBelow(0, 0)) {
		em.setProperty("state", "0");
		em.setProperty("leader", "true");
	}
    }
}

function playerDead(eim, player) {}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    if (eim.isLeader(player) || party.size() <= minPlayers) { // Check for party leader
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            playerExit(eim, party.get(i));
        eim.dispose();
    } else
        playerExit(eim, player);
}


function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
    if (eim.isLeader(player) || party.size() < minPlayers) {
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            if (party.get(i).equals(player))
                removePlayer(eim, player);
            else
                playerExit(eim, party.get(i));
        eim.dispose();
    } else
        removePlayer(eim, player);
}

function leftParty(eim, player) {
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    } else
        playerExit(eim, player);
}

function disbandParty(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
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
    end(eim);
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function respawnStg4(eim) {    
    eim.getMapInstance(925100400).instanceMapRespawn();
    em.schedule("respawnStg4", eim, 10 * 1000);
}

function dispose(eim) {
    em.cancelSchedule();
    
    em.setProperty("state", "0");
    em.setProperty("leader", "true");
}
