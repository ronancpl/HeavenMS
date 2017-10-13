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
2511000- Reactor for PPQ [Pirate PQ]
@author Jvlaple
*/

function act() {
    var eim = rm.getPlayer().getEventInstance();
    var now = eim.getIntProperty("openedBoxes");
    var nextNum = now + 1;
    eim.setIntProperty("openedBoxes", nextNum);
    
    rm.spawnMonster(9300109, 3);
    rm.spawnMonster(9300110, 5);
}