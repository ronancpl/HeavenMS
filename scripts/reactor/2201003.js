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
/* Ludi PQ Crack Reactor ^_^
 *@Author Jvlaple
  *2201003.js
 */
 
function act() {
    if (rm.getPlayer().getMapId() == 922010900) {
        rm.mapMessage(5, "Alishar has been summoned.");
        rm.spawnMonster(9300012, 941, 184);
    } else if(rm.getPlayer().getMapId() == 922010700) {
        rm.mapMessage(5, "Rombard has been summoned somewhere in the map.");
        rm.spawnMonster(9300010, 1, -211);
    }
}