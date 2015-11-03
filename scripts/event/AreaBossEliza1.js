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
	Eliza1 Spawner
-- Edited by --------------------------------------------------------------------------------------
	ThreeStep (based on xQuasar's King Clang spawner)

**/

importPackage(Packages.client);

var setupTask;

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
    var stairwayToTheSky2 = em.getChannelServer().getMapFactory().getMap(200010300);
    var eliza = Packages.server.life.MapleLifeFactory.getMonster(8220000);
	
	if(stairwayToTheSky2.getMonsterById(8220000) != null) {
		em.schedule("start", 3 * 60 *60 * 1000);
		return;
	}
	
    stairwayToTheSky2.spawnMonsterOnGroundBelow(eliza, new Packages.java.awt.Point(208, 83));
    stairwayToTheSky2.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "Eliza has appeared with a black whirlwind."));
	em.schedule("start", 3 * 60 *60 * 1000);
}