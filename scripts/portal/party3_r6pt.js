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
 *@author Ronan
 *party3_r4pt
 */

function enter(pi) {
    var eim = pi.getEventInstance();
    if(eim.getProperty("stage6_comb") == null) {
        var comb = "0";

        for(var i = 0; i < 16; i++) {
            var r = Math.floor((Math.random() * 4)) + 1;
            comb += r.toString();
        }

        eim.setProperty("stage6_comb", comb);
    }

    var comb = eim.getProperty("stage6_comb");
    
    var name = pi.getPortal().getName().substring(2, 5);
    var portalId = parseInt(name, 10);
    
    
    var pRow = Math.floor(portalId / 10);
    var pCol = portalId % 10;
    

    if (pCol == parseInt(comb.substring(pRow, pRow + 1), 10)) {    //climb
        pi.playPortalSound(); pi.warp(pi.getMapId(), (pRow % 4 != 0) ? pi.getPortal().getId() + 4 : (pRow / 4));
    } else {    //fail
        pi.playPortalSound(); pi.warp(pi.getMapId(), 5);
    }
    
    return true;
}