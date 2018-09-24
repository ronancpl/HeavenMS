var minPlayers = 1;
var entryMap = 106021402;
var exitMap = 106021600;

var minMapId = 106021601;
var maxMapId = 106021601;

function init(){}

function setup(difficulty, lobbyId){
	var eim = em.newInstance("MK_PrimeMinister_" +lobbyId);
	eim.getInstanceMap(106021601).resetFully();
	eim.getInstanceMap(106021601).allowSummonState(false);
	respawn(eim);
	return eim;
}

function afterSetup(eim){}

function respawn(eim){
	var map = eim.getMapInstance(entryMap);
	map.allowSummonState(true);
	map.instanceMapRespawn();
	eim.schedule("respawn", 10000);
}

function playerEntry(eim, player){
	var weddinghall = eim.getMapInstance(106021601);
	player.changeMap(weddinghall, weddinghall.getPortal(1));
}

function scheduledTimeout(eim){
	var party = eim.getPlayers();

	for(var i = 0; i < party.size(); i++)
		playerExit(eim, party.get(i));

	eim.dispose();
}

function playerRevive(eim, player){
	player.respawn(eim, entryMap);
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
	player.changeMap(entryMap, 2);
}

function changedMap(eim, chr, mapid) {
        if(mapid < minMapId || mapid > maxMapId) playerExit(eim, chr);
}

function removePlayer(eim, player){
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(entryMap);
}

function cancelSchedule(){}

function dispose(){}

function clearPQ(eim){}

function monsterKilled(mob, eim){}

function allMonstersDead(eim){}