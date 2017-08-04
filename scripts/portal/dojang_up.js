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
 * @author:   Moogra
 * @function: Warp character up and award player with dojo points
 * @maps:     All Dojo fighting maps
*/


function enter(pi) {
    try {
        if (pi.getPlayer().getMap().getMonsterById(9300216) != null) {
            pi.goDojoUp();
            pi.getPlayer().getMap().setReactorState();
            var stage = Math.floor(pi.getPlayer().getMapId() / 100) % 100;
            if ((stage - (stage / 6) | 0) == pi.getPlayer().getVanquisherStage() && !pi.getPlayer().getDojoParty()) // we can also try 5 * stage / 6 | 0 + 1
                pi.getPlayer().setVanquisherKills(pi.getPlayer().getVanquisherKills() + 1);
        } else {
            pi.getPlayer().message("There are still some monsters remaining.");
        }
        pi.enableActions();
        return true;
    } catch(err) {
        pi.getPlayer().dropMessage(err);
    }
}
