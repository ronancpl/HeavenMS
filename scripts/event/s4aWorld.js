/*
 * 4th Job Snipe / Concentration
 */

var minPlayers = 1;

function init() {
    em.setProperty("started", "false");
}

function monsterValue(eim, mobId) {
    return 1;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
        var eligible = [];
        var hasLeader = false;
        
        if(party.size() > 0) {
                var partyList = party.toArray();

                for(var i = 0; i < party.size(); i++) {
                        var ch = partyList[i];

                        if(ch.getMapId() == 105090200 && ch.getLevel() >= 120) {
                                if(ch.isLeader()) hasLeader = true;
                                eligible.push(ch);
                        }
                }
        }
        
        if(!(hasLeader && eligible.length >= minPlayers)) eligible = [];
        return eligible;
}

function setup() {
    var eim = em.newInstance("s4aWorld");

    eim.getInstanceMap(910500000).resetPQ(1);
    respawnStages(eim);
    eim.getMapInstance(910500000).instanceMapForceRespawn();
    eim.startEventTimer(1200000);

    em.setProperty("started", "true");

    return eim;
}

function afterSetup(eim) {}

function respawnStages(eim) {    
        eim.getMapInstance(910500000).instanceMapRespawn();
        eim.schedule("respawnStages", 15 * 1000);
}

function playerEntry(eim, player) {
    var map = eim.getMapFactory().getMap(910500000);
    player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
}

function scheduledTimeout(eim) {
    eim.disposeIfPlayerBelow(100, 105090200);

    em.setProperty("started", "false");
}

function changedMap(eim, player, mapid) {
    if (mapid != 910500000) {
	eim.unregisterPlayer(player);

	if (eim.disposeIfPlayerBelow(minPlayers, 105090200)) {
	    em.setProperty("started", "false");
	}
    }
}

function playerDisconnected(eim, player) {
    return 0;
}

function leftParty(eim, player) {
    // If only 2 players are left, uncompletable:
    playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    eim.disposeIfPlayerBelow(100, 105090200);

    em.setProperty("started", "false");
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    var map = eim.getMapFactory().getMap(105090200);
    player.changeMap(map, map.getPortal(0));
}

function clearPQ(eim) {
    eim.disposeIfPlayerBelow(100, 105090200);

    em.setProperty("started", "false");
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose() {}