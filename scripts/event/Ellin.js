var isPq = true;
var minPlayers = 1, maxPlayers = 6;
var minLevel = 1, maxLevel = 200;
var entryMap = 930000000;
var exitMap = 930000800;
var recruitMap = 300030100;
var clearMap = 930000800;

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
            
                if(ch.getMapId() == recruitMap && ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel && ch.getJob().getId() / 1000 == 0) {  //only adventurers
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
        
        var eim = em.newInstance("Ellin" + leaderid);
        eim.setProperty("level", level);
        
        eim.setInstanceMap(930000000).resetPQ(level);
	eim.setInstanceMap(930000100).resetPQ(level);
	eim.setInstanceMap(930000200).resetPQ(level);
	eim.setInstanceMap(930000300).resetPQ(level);
	eim.setInstanceMap(930000400).resetPQ(level);
	var map = eim.setInstanceMap(930000500);
	map.resetPQ(level);
        map.shuffleReactors();
	eim.setInstanceMap(930000600).resetPQ(level);
	eim.setInstanceMap(930000700).resetPQ(level);

        respawnStg2(eim);
        eim.startEventTimer(30 * 60000); //30 mins
        return eim;
}

function respawnStg2(eim) {    
    if(!eim.getMapInstance(930000200).getAllPlayer().isEmpty()) eim.getMapInstance(930000200).instanceMapRespawn();
    eim.schedule("respawnStg2", 4 * 1000);
}

function changedMap(eim, player, mapid) {
        if (mapid < 930000000 || mapid > 930000800) {
                if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
                        eim.unregisterPlayer(player);
                        end(eim);
                }
                else
                        eim.unregisterPlayer(player);
        }
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
