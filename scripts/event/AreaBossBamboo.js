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
	Bamboo Warrior Spawner
-- Edited by --------------------------------------------------------------------------------------
	Ronan (based on xQuasar's King Clang spawner)

**/
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
    var mapObj = em.getChannelServer().getMapFactory().getMap(800020120);   // original mapid was 251010101
    var mobObj = Packages.server.life.MapleLifeFactory.getMonster(6090002);
	
	if(mapObj.getMonsterById(6090002) != null) {
		em.schedule("start", 3 * 60 *60 * 1000);
		return;
	}
	
    mapObj.spawnMonsterOnGroundBelow(mobObj, new Packages.java.awt.Point(560, 50));
    mapObj.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "From amongst the ruins shrouded by the mists, Bamboo Warrior appears."));
	em.schedule("start", 3 * 60 *60 * 1000);
}