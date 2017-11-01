/*
    This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

importPackage(Packages.net.sf.odinms.server.maps);

/*
Return from MCPQ map.
*/

function enter(pi) {
    var returnMap = pi.getPlayer().getSavedLocation(SavedLocationType.MONSTER_CARNIVAL);
    if (returnMap < 0) {
        returnMap = 100000000; // to fix people who entered the fm trough an unconventional way
    }
    var target = pi.getPlayer().getClient().getChannelServer().getMapFactory().getMap(returnMap);
    var targetPortal;

    if (returnMap == 230000000) {
        targetPortal = target.getPortal("market01");
    } else {
        targetPortal = target.getPortal("market00");
    }

    if (targetPortal == null)
        targetPortal = target.getPortal(0);

    if (pi.getPlayer().getMapId() != target) {
        pi.getPlayer().clearSavedLocation(SavedLocationType.MONSTER_CARNIVAL);
        pi.getPlayer().changeMap(target, targetPortal);
        return true;
    }
    return false;
}