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
/* @Author Aexr, Ronan
 * 2401000.js: Horntail's Cave - Summons Horntail.
*/

function act() {
    rm.changeMusic("Bgm14/HonTale");
    if (rm.getReactor().getMap().getMonsterById(8810026) == null) {
        rm.getReactor().getMap().spawnHorntailOnGroundBelow(new java.awt.Point(71,260));
        
        var eim = rm.getEventInstance();
        eim.restartEventTimer(60 * 60000);
    }
    rm.mapMessage(6, "From the depths of his cave, here comes Horntail!");
}