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
@Author Jvlaple
*/

function enter(pi) {
    var nextMap = 240050101;
    var eim = pi.getPlayer().getEventInstance()
    var target = eim.getMapInstance(nextMap);
    var targetPortal = target.getPortal("sp");
    // only let people through if the eim is ready
    var avail = eim.getProperty("1stageclear");
    if (!pi.haveItem(4001092, 1)) {
        // do nothing; send message to player
        pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
        return false;
    }else {
        pi.gainItem(4001092, -1);
        pi.getPlayer().dropMessage(6, "The key disentegrates as Horntail\'s Seal is broken for a flash...");
        pi.getPlayer().changeMap(target, targetPortal);
        return true;
    }
}