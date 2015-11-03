/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
					   Matthias Butz <matze@odinms.de>
					   Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * @Author Jvlaple
 * 
 * Wedding for odinMS
 */
importPackage(java.lang);

importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);

var exitMap;
var altarMap;
var cakeMap;
var instanceId;
var minPlayers = 1;

function init() {
    exitMap = em.getChannelServer().getMapFactory().getMap(680000500); //Teh exit map :) <---------t
    altarMap = em.getChannelServer().getMapFactory().getMap(680000210); //Teh altar map
    cakeMap = em.getChannelServer().getMapFactory().getMap(680000300); //Teh cake
    instanceId = 1;
}

function monsterValue(eim, mobId) {
    return 1;
}



function setup(eim) {
    var instanceName = "CathedralWedding" + instanceId;
    var eim = em.newInstance(instanceName);
    instanceId++;

    var eim = em.newInstance(instanceName);
	
    var mf = eim.getMapFactory();
	
	
    var map = mf.getMap(680000200);//wutt
    //Lets make the clock continue through all maps xD
    em.schedule("playerAltar", 3 * 60000);
    eim.setProperty("hclicked", 0);
    eim.setProperty("wclicked", 0);
    eim.setProperty("entryTimestamp",System.currentTimeMillis() + (3 * 60000));
	
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(680000200);
    player.changeMap(map, map.getPortal(0));
	
    //1st - 20 min 2nd - 5 min 3rd 5 min xD
    //player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(1200));
    //player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(180));
    player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
}

//lets forget this bullshit...
function playerDead(eim, player) {
}

function playerRevive(eim, player) {
//how the fuck can this happen? o.O
}

function playerDisconnected(eim, player) {
    playerExit(eim, player);//kick him/her
}

function leftParty(eim, player) {	//this doesnt fucking matter...		
}

function disbandParty(eim) {
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}

function playerWarpAltar(eim, player) {
    if ((player.getName() != eim.getProperty("husband")) && (player.getName() != eim.getProperty("wife"))){
        player.changeMap(altarMap, altarMap.getPortal(0));
        player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(300));
    }else{
        player.changeMap(altarMap, altarMap.getPortal(2));
        player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(300));
        player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.serverNotice(6, "Please talk to High Priest John now!"));
    }
}

function playerWarpCake(eim, player) {
    player.changeMap(cakeMap, cakeMap.getPortal(0));
    player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(300));
}

function playerAltar(eim, player) {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerWarpAltar(eim, pIter.next());
            }
        }
        em.schedule("playerCake", 5 * 60000);
    //eim.dispose();
    }
}

function playerCake(eim, player) {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerWarpCake(eim, pIter.next());
            }
        }
        em.schedule("timeOut", 5 * 60000);
    //eim.dispose();
    }
}

//Those offline cuntts
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    //Wedding? IDK about gifts o.O
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function allMonstersDead(eim) {
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


function dispose() {

}