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
/*@author Ronan
 *Nependeath Pot - Spawns Nependeath or Dark Nependeath
 */
 
function act() {
    if(rm.getMap().getSummonState()) {
        var count = Number(rm.getEventInstance().getIntProperty("statusStg7_c"));

        if(count < 7) {
            var nextCount = (count + 1);

            rm.spawnMonster(Math.random() >= .6 ? 9300049 : 9300048);
            rm.getEventInstance().setProperty("statusStg7_c", nextCount);
        }
        else {
            rm.spawnMonster(9300049);
        }
    }
}