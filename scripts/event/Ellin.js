importPackage(Packages.client.inventory);
importPackage(Packages.tools);

var minPlayers = 2;
var entryMap = 930000000;
var exitMap = 930000800;

function init() {
        em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function setup(level, leaderid) {
        em.setProperty("state", "1");
	em.setProperty("leader", "true");
        var eim = em.newInstance("Ellin" + leaderid);

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
    em.schedule("respawnStg2", eim, 4 * 1000);
}

function changedMap(eim, player, mapid) {
    if (mapid < 930000000 || mapid > 930000800) {
	eim.unregisterPlayer(player);
        if(eim.getPlayers().isEmpty()) end(eim);
    }
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

function dispose(eim) {
    em.cancelSchedule();
    
    em.setProperty("state", "0");
    em.setProperty("leader", "true");
}
