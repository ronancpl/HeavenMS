var minPlayers = 1;
var timeLimit = 15; //15 minutes
var eventTimer = 1000 * 60 * timeLimit;
var exitMap = 240070000;
var eventMap = 240070010;
var eventBossIds = [7120100, 7120101, 7120102, 8120100, 8120101, 8140510];

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
	player.changeMap(cave, 1);
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

function playerDisconnected(eim, player) {
        if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                eim.unregisterPlayer(player);
}

function monsterValue(eim, mobId){
	return -1;
}

function end(eim) {
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
                playerExit(eim, party.get(i));
        }
        eim.dispose();
}

function leftParty(eim, player){}

function disbandParty(eim){}

function playerUnregistered(eim, player){}

function playerExit(eim, player){
	eim.unregisterPlayer(player);
	player.changeMap(exitMap);
}

function changedMap(eim, player, mapid){
	if (mapid != (eventMap + 10 * eim.getIntProperty("nex"))) {
                if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                        eim.unregisterPlayer(player);
                        end(eim);
                }
                else
                        eim.unregisterPlayer(player);
        }
}

function cancelSchedule(){}

function dispose(){}

function clearPQ(eim){
        eim.stopEventTimer();
        eim.setEventCleared();
}

function monsterKilled(mob, eim){
        if (mob.getId() == eventBossIds[eim.getIntProperty("nex")]) {
                eim.showClearEffect();
                eim.clearPQ();
        }
}

function allMonstersDead(eim){}

// ---------- FILLER FUNCTIONS ----------

function changedLeader(eim, leader) {}

