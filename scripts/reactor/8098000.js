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
 * @author RMZero213 (base)
 * @author Moogra (fixed, clean up)
 */
function act() {
    var map = rm.getPlayer().getMapId();
    var b = Math.abs(rm.getPlayer().getMapId() - 809050005);
    if (map != 809050000 && map != 809050010 && map != 809050014) {
        rm.spawnMonster(9400217 - b, 2);
        rm.spawnMonster(9400218 - b, 3);
    } else {
        rm.spawnMonster(9400209, 6);
        rm.spawnMonster(9400210, 9);
    }
    rm.mapMessage(5, "Some monsters are summoned.");
}