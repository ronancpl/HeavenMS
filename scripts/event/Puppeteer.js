var minPlayers = 1;
var timeLimit = 1; //10 minutes
var eventTimer = 1000 * 60 * timeLimit;
var exitMap = 105070300;
var eventMap = 910510000;

function init(){}

function setup(difficulty, lobbyId){
	var eim = em.newInstance("Puppeteer_" +lobbyId);
	eim.getInstanceMap(eventMap).resetFully();
	eim.getInstanceMap(eventMap).allowSummonState(false);
	respawn(eim);
	eim.startEventTimer(eventTimer);
	return eim;
}

function afterSetup(eim){}

function respawn(eim){}

function playerEntry(eim, player){
	var cave = eim.getMapInstance(eventMap);
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

function changedMap(eim, player){
	if(player.getMap().getId() < eventMap || player.getMap().getId() > next){
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