var minPlayers = 1;
var timeLimit = 15; //15 minutes
var eventTimer = 1000 * 60 * timeLimit;
var exitMap = 240070000;
var eventMap = 240070010;

function init(){}

function setup(difficulty, lobbyId){
	var eim = em.newInstance("Nex_" + lobbyId);
        eim.setIntProperty("nex", lobbyId);
        
	eim.getInstanceMap(eventMap + 10 * lobbyId).resetFully();
	eim.getInstanceMap(eventMap + 10 * lobbyId).allowSummonState(false);
	respawn(eim);
	eim.startEventTimer(eventTimer);
	return eim;
}

function afterSetup(eim){}

function respawn(eim){}

function playerEntry(eim, player){
	var cave = eim.getMapInstance(eventMap + 10 * eim.getIntProperty("nex"));
	player.changeMap(cave);
}

function scheduledTimeout(eim){
	var party = eim.getPlayers();

	for(var i = 0; i < party.size(); i++)
		playerExit(eim, party.get(i));

	eim.dispose();
}

function playerRevive(eim, player){
	player.respawn(eim, exitMap);
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

function leftParty(eim, player){}

function disbandParty(eim){}

function playerUnregistered(eim, player){}

function playerExit(eim, player){
	eim.unregisterPlayer(player);
	player.changeMap(exitMap);
}

function changedMap(eim, player, mapId){
	if(mapId != (eventMap + 10 * eim.getIntProperty("nex"))){
		removePlayer(eim, player);
		eim.stopEventTimer();
		eim.setEventCleared();
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