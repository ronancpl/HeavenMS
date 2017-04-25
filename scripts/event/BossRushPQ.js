var isPq = true;
var minPlayers = 1, maxPlayers = 6;
var minLevel = 1, maxLevel = 200;
var entryMap = 970030100;
var exitMap = 970030000;
var recruitMap = 970030000;
var clearMap = 970030000;

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
            
                if(ch.getMapId() == recruitMap && ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel) {
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
        var eim = em.newInstance("BossRush" + leaderid);
	
        em.setProperty("level", level);
        eim.startEventTimer(45 * 60000); //45 mins
        return eim;
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

function playerExit(eim, player) {
        eim.unregisterPlayer(player);
        player.changeMap(exitMap, 0);
}

function changedMap(eim, player, mapid) {
    if (mapid < 970030001 || mapid > 970042711) {
            var party = eim.getPlayers();
            if (eim.isLeader(player) || party.size() <= minPlayers) {
                    eim.unregisterPlayer(player);
                    end(eim);
            }
            else
                    eim.unregisterPlayer(player);
    }
}

function playerDead(eim, player) {}

function playerRevive(eim, player) { // player presses ok on the death pop up.
        var party = eim.getPlayers();
        if (eim.isLeader(player) || party.size() <= minPlayers) {
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                eim.unregisterPlayer(player);
}


function playerDisconnected(eim, player) {
        var party = eim.getPlayers();
        if (eim.isLeader(player) || party.size() <= minPlayers) {
                eim.unregisterPlayer(player);
                end(eim);
        }
        else
                removePlayer(eim, player);
}

function leftParty(eim, player) {
        var party = eim.getPlayers();
        if (party.size() <= minPlayers)
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
}

function playerClear(eim, player, toMap) {
    eim.unregisterPlayer(player);
    
    if(toMap != null) player.changeMap(toMap);
    else player.changeMap(clearMap, 0);
}

function complete(eim, toMap) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerClear(eim, party.get(i), toMap);
    }
    eim.dispose();
}

function clearPQ(eim, toMap) {
    complete(eim, toMap);
}

function monsterKilled(mob, eim) {}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {
    em.cancelSchedule();
    
    em.setProperty("state", "0");
    em.setProperty("leader", "true");
}
