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
	Chimera/Kimera Spawner
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
    var labSecretBasementPath = em.getChannelServer().getMapFactory().getMap(261030000);
    var chimera = Packages.server.life.MapleLifeFactory.getMonster(8220002);
	
	if(labSecretBasementPath.getMonsterById(8220002) != null) {
		em.schedule("start", 3 * 60 *60 * 1000);
		return;
	}
	
    var posX;
    var posY = 180;
    posX =  (Math.floor(Math.random() * 900) - 900);
    labSecretBasementPath.spawnMonsterOnGroundBelow(chimera, new Packages.java.awt.Point(posX, posY));
    labSecretBasementPath.broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, "Kimera has appeared out of the darkness of the underground with a glitter in her eyes."));
	em.schedule("start", 3 * 60 *60 * 1000);
}