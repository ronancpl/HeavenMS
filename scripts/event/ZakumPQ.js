/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @Author Lerk
 * 
 * Zakum Party Quest 
 */

var exitMap;
var instanceId;

function init() {
    instanceId = 1;
    em.setProperty("shuffleReactors","true");
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    exitMap = em.getChannelServer().getMapFactory().getMap(280090000); //room of tragedy
    var instanceName = "ZakumPQ" + instanceId;
	
    //ZPQ maps, center area then 1-1 through 16-6 increasing gradually
    //var instanceMaps = new Array(280010000, 280010010, 280010011, 280010020, 280010030, 280010031, 280010040, 280010041, 280010050, 280010060,
    //	280010070, 280010071, 280010080, 280010081, 280010090, 280010091, 280010100, 280010101, 280010110, 280010120, 280010130, 280010140,
    //	280010150, 280011000, 280011001, 280011002, 280011003, 280011004, 280011005, 280011006);
    var eim = em.newInstance(instanceName);
	
    var mf = eim.getMapFactory();
	
    instanceId++;
	
    var map = mf.getMap(280010000);
    map.shuffleReactors();
	
    //no time limit yet until clock can be visible in all maps
    //em.schedule("timeOut", 30 * 60000);
	
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(280010000);
    player.changeMap(map, map.getPortal(0));
	
//TODO: hold time across map changes
//player.getClient().getSession().write(tools.MaplePacketCreator.getClock(1800));
}

function playerRevive(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim, party.get(i));
        }
        eim.dispose();
    }
    else { //boot dead player
        playerExit(eim, player);
    }
    return false; // don't execute the standard reviving code
}

function playerDead(eim, player) {
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            if (party.get(i).equals(player)) {
                removePlayer(eim, player);
            }
            else {
                playerExit(eim, party.get(i));
            }
        }
        eim.dispose();
    }
    else { //boot d/ced player
        removePlayer(eim, player);
    }
}

function leftParty(eim, player) {
    playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    //ZPQ does nothing special with winners
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function allMonstersDead(eim) {
//do nothing; ZPQ has nothing to do with monster killing
}

function cancelSchedule() {
}

function timeOut() {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerExit(eim, pIter.next());
            }
        }
        eim.dispose();
    }
}