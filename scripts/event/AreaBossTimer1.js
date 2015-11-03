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
-- Odin JavaScript --------------------------------------------------------------------------------
	Timer1 Spawner
-- Edited by --------------------------------------------------------------------------------------
	ThreeStep (based on xQuasar's King Clang spawner)
**/

importPackage(Packages.client);

function init() {
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule("start", 0);    //spawns upon server start. Each 3 hours an server event checks if boss exists, if not spawns it instantly.
}

function cancelSchedule() {
    if (setupTask != null)
        setupTask.cancel(true);
}

function start() {
    var whirlpoolOfTime = em.getChannelServer().getMapFactory().getMap(220050100);
    var timer1 = Packages.server.life.MapleLifeFactory.getMonster(5220003);
	
	if(whirlpoolOfTime.getMonsterById(5220003) != null) {
		em.schedule("start", 3 * 60 * 60 * 1000);
		return;
	}
	
    var posX;
    var posY = 1030;
    posX =  Math.floor((Math.random() * 770) - 770);
    whirlpoolOfTime.spawnMonsterOnGroundBelow(timer1, new Packages.java.awt.Point(posX, posY));
    whirlpoolOfTime.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "Tick-Tock Tick-Tock! Timer makes it's presence known."));
	em.schedule("start", 3 * 60 * 60 * 1000);
}