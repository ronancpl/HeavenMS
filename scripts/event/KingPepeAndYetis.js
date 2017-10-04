var minPlayers = 1;
var timeLimit = 20; //20 minutes
var eventTimer = 1000 * 60 * timeLimit;
var exitMap = 106021400;

function init(){}

function setup(difficulty, lobbyId){
	var eim = em.newInstance("KingPepe_" +lobbyId);
	eim.getInstanceMap(106021500).resetFully();
	eim.getInstanceMap(106021500).allowSummonState(false);
	respawn(eim);
	eim.startEventTimer(eventTimer);
	return eim;
}

function afterSetup(eim){}

function respawn(eim){
	var map = eim.getMapInstance(exitMap);
	map.allowSummonState(true);
	map.instanceMapRespawn();
	eim.schedule("respawn", 10000);
}

function playerEntry(eim, player){
	var yetiMap = eim.getMapInstance(106021500);
	player.changeMap(yetiMap, yetiMap.getPortal(1));
}

function scheduledTimeout(eim){
	var party = eim.getPlayers();

	for(var i = 0; i < party.size(); i++)
		playerExit(eim, party.get(i));

	eim.dispose();
}

function playerRevive(eim, player){
	player.setHp(50);
	player.setStance(0);
	eim.unregisterPlayer(player);
	player.changeMap(exitMap);
	return false;
}

function playerDead(eim, player){}

function playerDisconnected(eim, player){
	var party = eim.getPlayers();

	for(var i = 0; i < party.size(); i++){
		if(party.get(i).equals(player))
			removePlayer(eim, player);
		else
			playerExit(eim, party.get(i));
	}
	eim.dispose();
}

function monsterValue(eim, mobId){
	return -1;
}

function leftParty(eim, player){
	var party = eim.getPlayers();

	if(party.size() < minPlayers){
		for(var i = 0; i < party.size(); i++){
			playerExit(eim, party.get(i));
		}
		eim.dispose();
	}
	else{
		playerExit(eim, player);
	}
}

function disbandParty(eim){}

function playerUnregistered(eim, player){}

function playerExit(eim, player){
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, 2);
}

function moveMap(eim, player){
	if(player.getMap().getId() == exitMap){
		removePlayer(eim, player);
		eim.dispose();
	}
}

function removePlayer(eim, player){
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function cancelSchedule(){}

function dispose(){}

function clearPQ(eim){}

function monsterKilled(mob, eim){}

function allMonstersDead(eim){}