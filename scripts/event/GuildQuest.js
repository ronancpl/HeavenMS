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
 * Guild Quest 
 */

var exitMap;
var waitingListCurrent = 0;
 
importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(java.lang);

function init() {
    em.setProperty("shuffleReactors","false");
	em.setProperty("canEnter", "true");
	em.setProperty("gpqOpen", "true");
}

function monsterValue(eim, mobId) { //should only trigger on ergoth
    if (mobId == 9300028) { //but, just to be safe...
        var rubian = new Packages.client.inventory.Item(4001024, 0, 1);
        var map = eim.getMapInstance(990000900);
        var reactor = map.getReactorByName("boss");
        map.spawnItemDrop(reactor, eim.getPlayers().get(0), rubian, reactor.getPosition(), true, false);
    }
    return -1;
}



function setup(eim) {
    exitMap = em.getChannelServer().getMapFactory().getMap(990001100); //returning path
        
    //shuffle reactors in two maps for stage 3
    eim.getMapInstance(990000501).shuffleReactors();
    eim.getMapInstance(990000502).shuffleReactors();
        
    //force no-respawn on certain map reactors
    eim.getMapInstance(990000611).getReactorByName("").setDelay(-1);
    eim.getMapInstance(990000620).getReactorByName("").setDelay(-1);
    eim.getMapInstance(990000631).getReactorByName("").setDelay(-1);
    eim.getMapInstance(990000641).getReactorByName("").setDelay(-1);
        
    //activate three minutes after start
	eim.setProperty("entryTimestamp", Packages.java.lang.System.currentTimeMillis());
    eim.setProperty("canEnter","true");
    eim.schedule("begin", 60000);
	eim.startEventTimer(60000);
}

function begin(eim) {
    eim.setProperty("canEnter","false");
    var party = eim.getPlayers();
    //if (party.size() < 6) { //not enough to start
    //        end(eim,"There are no longer enough players to continue, and those remaining shall be warped out.");
    //} else {
    var iter = party.iterator();
    while (iter.hasNext()) {
        iter.next().dropMessage(6,"The quest has begun.");
    }
	
	eim.startEventTimer(1000 * 60 * 90);
	eim.schedule("timeOut", 1000 * 60 * 90);
//}
}

function timeOut(eim) {
	end(eim, "Your allotted time to finish the quest has passed.");
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(990000000);
    player.changeMap(map, map.getPortal(0));
}

function playerRevive(eim, player) {
    var returnMap = 990000200;
    if (eim.getProperty("canEnter").equals("true")) {
        returnMap = 990000000;
    }
    player.setHp(50);
    player.setStance(0);
    player.changeMap(eim.getMapInstance(returnMap), eim.getMapInstance(returnMap).getPortal(0));
    return false;
}

function playerDead(eim, player) {
}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
    if (player.getName().equals(eim.getProperty("leader"))) { //check for party leader
        //boot all players and end
        var iter = party.iterator();
        while (iter.hasNext()) {
            var pl = iter.next();
            pl.dropMessage(6,"The leader of the instance has disconnected, and the remaining players shall be warped out.");
            if (pl.equals(player)) {
                removePlayer(eim, pl);
            }
            else {
                eim.unregisterPlayer(pl);
                pl.changeMap(exitMap, exitMap.getPortal(0));
            }
        }
        eim.dispose();
    }
    else { //boot d/ced player and check if enough players left
        removePlayer(eim, player);
        if (party.size() < 6) { //five after player booted
            end(eim,"There are no longer enough players to continue, and those remaining shall be warped out.");
        }
    }
}

function leftParty(eim, player) { //ignore for GQ
}

function disbandParty(eim) { //ignore for GQ
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
    var party = eim.getPlayers();
    if (party.size() < 6) { //five after player booted
        end(eim,"There are no longer enough players to continue, and those remaining shall be warped out.");
    }
}

function end(eim, msg) {
    var iter = eim.getPlayers().iterator();
    while (iter.hasNext()) {
        var player = iter.next();
        player.dropMessage(6,msg);
        eim.unregisterPlayer(player);
        player.changeMap(exitMap, exitMap.getPortal(0));
    }
    eim.dispose();
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    var iter = eim.getPlayers().iterator();
    var bonusMap = eim.getMapInstance(990001000);
	eim.startEventTimer(40000);
    while (iter.hasNext()) {
        var player = iter.next();
        player.changeMap(bonusMap, bonusMap.getPortal(0));
    }
    eim.schedule("finish", 40000)
}

function finish(eim) {
    var iter = eim.getPlayers().iterator();
    while (iter.hasNext()) {
        var player = iter.next();
        eim.unregisterPlayer(player);
        player.changeMap(exitMap, exitMap.getPortal(0));
    }
    eim.dispose();
}

function allMonstersDead(eim) {
//do nothing; GQ has nothing to do with monster killing
}

function cancelSchedule() {
}

function dispose(eim) {
	em.schedule("openGPQ", 5000);
}

function openGPQ() {
	em.setProperty("gpqOpen", "true");
}


function timeOut() {
	
}