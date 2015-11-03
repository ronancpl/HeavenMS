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
	
	THIS  FILE WAS MADE BY JVLAPLE. REMOVING THIS NOTICE MEANS YOU CAN'T USE THIS SCRIPT OR ANY OTHER SCRIPT PROVIDED BY JVLAPLE.
 */

/*
 * @Author Jvlaple
 * 
 * Orbis Party Quest
 */

importPackage(java.lang);
importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(Packages.server.life);
importPackage(Packages.scripting.npc);

var exitMap;
var instanceId;
var minPlayers = 1;

function init() {
    instanceId = 1;
}



function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    instanceId = em.getChannelServer().getInstanceId();
    exitMap = em.getChannelServer().getMapFactory().getMap(920011200); //Teh exit map :) <---------t
    var instanceName = "OrbisPQ" + instanceId;
    var eim = em.newInstance(instanceName);
    var mf = eim.getMapFactory();
    em.getChannelServer().addInstanceId();
    var map = mf.getMap(920010000);//wutt
    //map.shuffleReactors();
    // eim.addMapInstance(920010000,map);
    //var firstPortal = eim.getMapInstance(920010000).getPortal("in00");
    //firstPortal.setScriptName("hontale_BtoB1");
    //Fuck this timer
    //eim.setProperty("bulbWay", 0);
    //Define all Maps and PortalScripts
    var centerMap = eim.getMapInstance(920010100);
    centerMap.getPortal(13).setScriptName("orbisPQSealedRoom");
    centerMap.getPortal(4).setScriptName("orbisPQWalkway");
    centerMap.getPortal(12).setScriptName("orbisPQStorage");
    centerMap.getPortal(5).setScriptName("orbisPQLobby");
    centerMap.getPortal(14).setScriptName("orbisPQOnTheWayUp");
    centerMap.getPortal(15).setScriptName("orbisPQLounge");
    centerMap.getPortal(16).setScriptName("orbisPQRoomOfDarkness");
    var walkwayMap = eim.getMapInstance(920010200);
    var storageMap = eim.getMapInstance(920010300);
    var lobbyMap = eim.getMapInstance(920010400);
    var sealedRoomMap = eim.getMapInstance(920010500);
    var loungeMap = eim.getMapInstance(920010600);
    var onTheWayUpMap = eim.getMapInstance(920010700);
    var bossMap = eim.getMapInstance(920010800);
    var jailMap = eim.getMapInstance(920010900);
    var roomOfDarknessMap = eim.getMapInstance(920011000);
    var bonusMap = eim.getMapInstance(920011100);
    var endMap = eim.getMapInstance(920011300);
    walkwayMap.getPortal(13).setScriptName("orbisPQWalkwayExit");
    storageMap.getPortal(1).setScriptName("orbisPQStorageExit");
    lobbyMap.getPortal(8).setScriptName("orbisPQLobbyExit");
    sealedRoomMap.getPortal(3).setScriptName("orbisPQSRExit");
    loungeMap.getPortal(17).setScriptName("orbisPQLoungeExit");
    onTheWayUpMap.getPortal(23).setScriptName("orbisPQOnTheWayUpExit");
    bossMap.getPortal(1).setScriptName("orbisPQGardenExit");
    roomOfDarknessMap.getPortal(1).setScriptName("orbisPQRoomOfDarknessExit");
    //-->Fuck we are done with portals -.-
    eim.setProperty("killedCellions", "0");
    eim.setProperty("papaSpawned", "no");
    em.schedule("timeOut", 60 * 60000);
    em.schedule("broadcastClock", 1500);
    eim.setProperty("entryTimestamp",System.currentTimeMillis() + (60 * 60000));
	
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(920010000);
    player.changeMap(map, map.getPortal(0));
    player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
    var texttt = "Hi, my name is Eak, the Chamberlain of the Goddess. Don't be alarmed; you won't be able to see me right now. Back when the Goddess turned into a block of stone, I simultaneously lost my own power. If you gather up the power of the Magic Cloud of Orbis, however, then I'll be able to recover my body and re-transform back to my original self. Please collect #b20#k Magic Clouds and bring them back to me. Right now, you'll only see me as a tiny, flickering light."
    player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getNPCTalk(2013001, /*(byte)*/ 0, texttt, "00 00"));
//player.getClient().getSession().write(net.sf.odinms.scripting.npc.NPCScriptManager.dispose(eim.getClient()));
//THE CLOCK IS SHIT
//player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(1800));
}

function playerDead(eim, player) {
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
        // If only 5 players are left, uncompletable:
        var party = eim.getPlayers();
        if (party.size() <= minPlayers) {
            for (var i = 0; i < party.size(); i++) {
                playerExit(eim,party.get(i));
            }
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //PWN THE PARTY (KICK OUT)
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
    else { //KICK THE D/CED CUNT
        // If only 5 players are left, uncompletable:
        var party = eim.getPlayers();
        if (party.size() < minPlayers) {
            for (var i = 0; i < party.size(); i++) {
                playerExit(eim,party.get(i));
            }
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function leftParty(eim, player) {			
    // If only 5 players are left, uncompletable:
    var party = eim.getPlayers();
    if (party.size() <= minPlayers) {
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim,party.get(i));
        }
        eim.dispose();
    }
    else
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
    player.cancelAllBuffs(); //We don't want people going out with wonky blessing >=(
    player.changeMap(exitMap, exitMap.getPortal(0));
}

//Those offline cuntts
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    // W00t! Bonus!!
    var iter = eim.getPlayers().iterator();
    var bonusMap = eim.getMapInstance(920011100);
    while (iter.hasNext()) {
        var player = iter.next();
        player.changeMap(bonusMap, bonusMap.getPortal(0));
        eim.setProperty("entryTimestamp",System.currentTimeMillis() + (1 * 60000));
        player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(60));
    }
    eim.schedule("finish", 60000)
}

function finish(eim) {
    var dMap = eim.getMapInstance(920011300);
    var iter = eim.getPlayers().iterator();
    while (iter.hasNext()) {
        var player = iter.next();
        eim.unregisterPlayer(player);
        player.changeMap(dMap, dMap.getPortal(0));
    }
    eim.dispose();
}

function allMonstersDead(eim) {
//Open Portal? o.O
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

function playerClocks(eim, player) {
    if (player.getMap().hasTimer() == false){
        player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
    //player.getMap().setTimer(true);
    }
}

function playerTimer(eim, player) {
    if (player.getMap().hasTimer() == false) {
        player.getMap().setTimer(true);
    }
}

function broadcastClock(eim, player) {
    //var party = eim.getPlayers();
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerClocks(eim, pIter.next());
            }
        }
    //em.schedule("broadcastClock", 1600);
    }
    // for (var kkl = 0; kkl < party.size(); kkl++) {
    // party.get(kkl).getMap().setTimer(true);
    // }
    var iterr = em.getInstances().iterator();
    while (iterr.hasNext()) {
        var eim = iterr.next();
        if (eim.getPlayerCount() > 0) {
            var pIterr = eim.getPlayers().iterator();
            while (pIterr.hasNext()) {
                //playerClocks(eim, pIter.next());
                playerTimer(eim, pIterr.next());
            }
        }
    //em.schedule("broadcastClock", 1600);
    }
    em.schedule("broadcastClock", 1600);
}

function dispose() {

}