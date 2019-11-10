importPackage(Packages.tools);
importPackage(Packages.server.life);

var eventTime = 10 * 60 * 1000;     // 10 minutes
var entryMap = 106021600;
var exitMap = 106021402;
var recruitMap = 106021402;

var minPlayers = 1, maxPlayers = 3;
var minLevel = 30, maxLevel = 255;

var minMapId = 106021600;
var maxMapId = 106021600;

var mobId = 3300008; //Prime Minister

function init(){}

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

function setup(difficulty, lobbyId){
	var eim = em.newInstance("MK_PrimeMinister_" +lobbyId);
	respawn(eim);
        
	return eim;
}

function afterSetup(eim){}

function primeMinisterCheck(eim) {
        var map = eim.getMapInstance(entryMap);
        
        var pIter = map.getAllPlayers().iterator();
        while (pIter.hasNext()) {
                var player = pIter.next();
                if (player.getQuestStatus(2333) == 1 && player.getAbstractPlayerInteraction().getQuestProgressInt(2333, mobId) == 0) {
                        return true;
                }
        }

        return false;
}

function respawn(eim){
	if (primeMinisterCheck(eim)) {
                eim.startEventTimer(eventTime);

                var weddinghall = eim.getMapInstance(entryMap);
                weddinghall.getPortal(1).setPortalState(false);
                weddinghall.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new java.awt.Point(292, 143));
        } else {
                eim.schedule("respawn", 10000);
        }
}

function playerEntry(eim, player){
	var weddinghall = eim.getMapInstance(entryMap);
	player.changeMap(weddinghall, weddinghall.getPortal(1));
}

function scheduledTimeout(eim){
	var party = eim.getPlayers();

	for(var i = 0; i < party.size(); i++)
		playerExit(eim, party.get(i));

	eim.dispose();
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
        if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                eim.unregisterPlayer(player);
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

function changedMap(eim, player, mapid) {
        if (mapid < minMapId || mapid > maxMapId) {
                if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                        eim.unregisterPlayer(player);
                        end(eim);
                }
                else
                        eim.unregisterPlayer(player);
        }
}

function removePlayer(eim, player){
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(entryMap);
}

function cancelSchedule(){}

function dispose(){}

function clearPQ(eim){
        eim.stopEventTimer();
        eim.setEventCleared();
}

function monsterKilled(mob, eim){
        if (mob.getId() == mobId) {
                eim.getMapInstance(entryMap).getPortal(1).setPortalState(true);

                eim.showClearEffect();
                eim.clearPQ();
        }
}

function allMonstersDead(eim){}

// ---------- FILLER FUNCTIONS ----------

function changedLeader(eim, leader) {}

