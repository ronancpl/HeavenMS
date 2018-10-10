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

var entryMap = 922000010;
var exitMap = 221024400;
var eventTime = 10;     //10 minutes

function init() {
    em.setProperty("noEntry","false");
}

function playerEntry(eim, player) {
    eim.getInstanceMap(entryMap).shuffleReactors();
    eim.setExclusiveItems([4031094]);
    
    player.changeMap(entryMap, 0);
    em.setProperty("noEntry","true");
    
    player.getClient().announce(MaplePacketCreator.getClock(eventTime * 60));
    eim.startEventTimer(eventTime * 60000);
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    eim.dispose();
    em.setProperty("noEntry","false");
}

function scheduledTimeout(eim) {
    var player = eim.getPlayers().get(0);
    playerExit(eim, eim.getPlayers().get(0));
    player.changeMap(exitMap, 4);
}

function playerDisconnected(eim, player) {
    playerExit(eim, player);
}

function clear(eim) {
    var player = eim.getPlayers().get(0);
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 4);
    
    eim.dispose();
    em.setProperty("noEntry","false");
}

function changedMap(eim, chr, mapid) {
    if(mapid != entryMap) playerExit(eim, chr);
}

function cancelSchedule() {}

function dispose() {}
