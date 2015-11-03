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
function enter(pi) {
    var papuMap = pi.getClient().getChannelServer().getMapFactory().getMap(220080001);
    if (papuMap.getCharacters().size() == 0) {
        pi.getPlayer().dropMessage("The room is empty. A perfect opportunity to challenge the boss.");
        papuMap.resetReactors();
    } else { // someone is inside
        for (var i = 0; i < 3; i++) {
            if (papuMap.getMonsterById(8500000 + i) != null) {
                pi.getPlayer().dropMessage("Someone is fighting Papulatus.");
                return false;
            }
        }
    }
    pi.warp(220080001, "st00");
    return true;
}