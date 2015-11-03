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
 * @Author Moogra
 */
function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("door").getState() == 1) {
        if ((pi.getPlayer().getMapId() / 100) % 100 != 38) {
            if (pi.getPlayer().getMap().getCharacters().size() == 1) {
                pi.resetMap(pi.getPlayer().getMapId());
            }
            pi.getPlayer().message("You received " + pi.getPlayer().addDojoPointsByMap() + " training points. Your total training points score is now " + pi.getPlayer().getDojoPoints() + ".");
            pi.warp(pi.getPlayer().getMap().getId() + 100, 0);
        } else {
            pi.warp(925020003, 0);
            pi.getPlayer().gainExp(2000 * pi.getPlayer().getDojoPoints(), true, true, true);
        }
        return true;
    } else {
        pi.getPlayer().message("The door is not open yet.");
        return false;
    }
}
