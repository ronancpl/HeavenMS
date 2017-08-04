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

importPackage(Packages.server.life);

function spawnJrBoss(mobObj) {
    mobObj.getMap().killMonster(mobObj.getId());
    var spawnid = mobObj.getId() - 17;
    
    var mob = MapleLifeFactory.getMonster(spawnid);
    mobObj.getMap().spawnMonsterOnGroundBelow(mob, mobObj.getPosition());
}

function hit() {
    var mapObj = rm.getMap();
    
    //spawnJrBoss(mapObj.getMonsterById(8820019));
    //spawnJrBoss(mapObj.getMonsterById(8820020));
    //spawnJrBoss(mapObj.getMonsterById(8820021));
    //spawnJrBoss(mapObj.getMonsterById(8820022));
    //spawnJrBoss(mapObj.getMonsterById(8820023));
    
    mapObj.killMonster(8820000);
}