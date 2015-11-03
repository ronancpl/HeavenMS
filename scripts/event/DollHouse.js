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
/**
 *Dollhouse Event
**/
importPackage(Packages.tools);

var returnMap;
var map;
var eim;

function init() {
    em.setProperty("noEntry","false");
}

function playerEntry(eim, player) {
    returnMap = em.getChannelServer().getMapFactory().getMap(221024400);
    eim = em.getInstance("DollHouse");
    map = eim.getMapFactory().getMap(922000010);
    player.changeMap(map, map.getPortal(0));
    map.shuffleReactors();
    em.setProperty("noEntry","true");
    em.schedule("timeOut", 600000);
    player.getClient().getSession().write(MaplePacketCreator.getClock(600));
}



function playerExit(eim, player) {
    em.setProperty("noEntry","false");
    player.changeMap(returnMap, returnMap.getPortal(4));
    eim.unregisterPlayer(player);
    em.cancel();
    em.disposeInstance("DollHouse");
    eim.dispose();
}

function timeOut() {
    em.setProperty("noEntry","false");
    var player = eim.getPlayers().get(0);
    player.changeMap(returnMap, returnMap.getPortal(4));
    eim.unregisterPlayer(player);
    em.cancel();
    em.disposeInstance("DollHouse");
    eim.dispose();
}

function playerDisconnected(eim, player) {
    em.setProperty("noEntry","false");
    player.getMap().removePlayer(player);
    player.setMap(returnMap);
    eim.unregisterPlayer(player);
    em.cancel();
    em.disposeInstance("DollHouse");
    eim.dispose();
}

function clear(eim) {
    em.setProperty("noEntry","false");
    var player = eim.getPlayers().get(0);
    player.changeMap(returnMap, returnMap.getPortal(4));
    eim.unregisterPlayer(player);
    em.cancel();
    em.disposeInstance("DollHouse");
    eim.dispose();
}

function cancelSchedule() {
}

function dispose() {
}
