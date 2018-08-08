var minPlayers = 1;
var timeLimit = 20; //20 minutes
var eventTimer = 1000 * 60 * timeLimit;
var exitMap = 106021400;
var eventMap = 106021500;

function init(){}

function setup(difficulty, lobbyId){
	var eim = em.newInstance("KingPepe_" +lobbyId);
	eim.getInstanceMap(eventMap).resetFully();
	eim.getInstanceMap(eventMap).allowSummonState(false);
	
	eim.startEventTimer(eventTimer);
	return eim;
}

function afterSetup(eim){}

function playerEntry(eim, player){
	var yetiMap = eim.getMapInstance(eventMap);
	player.changeMap(yetiMap, yetiMap.getPortal(1));
}

function scheduledTimeout(eim){
	var party = eim.getPlayers();

	for(var i = 0; i < party.size(); i++)
		playerExit(eim, party.get(i));

	eim.dispose();
}

function playerDead(eim, player){}

function playerDisconnected(eim, player){
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
	player.changeMap(exitMap, 2);
}

function changedMap(eim, chr, mapid) {
        if (mapid != eventMap) {
                if (eim.isEventTeamLackingNow(true, minPlayers, chr)) {
                        eim.unregisterPlayer(chr);
                        end(eim);
                }
                else
                        eim.unregisterPlayer(chr);
        }
}

function cancelSchedule(){}

function dispose(){}

function clearPQ(eim){}

function monsterKilled(mob, eim){}

function allMonstersDead(eim){}