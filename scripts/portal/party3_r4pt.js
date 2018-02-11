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
    if(eim.getProperty("stage4_comb") == null) {
        var r = Math.floor((Math.random() * 3)) + 1;
        var s = Math.floor((Math.random() * 3)) + 1;
        
        eim.setProperty("stage4_comb", "" + r + s);
    }

    var pname = Number(pi.getPortal().getName().substring(4, 6));
    var cname = Number(eim.getProperty("stage4_comb"));
    
    var secondPt = true;
    if(pi.getPortal().getId() < 14) {
        cname = Math.floor(cname / 10);
        secondPt = false;
    }
    
    if ((pname % 10) == (cname % 10)) {    //climb
        var nextPortal;
        if(secondPt) nextPortal = 1;
        else nextPortal = pi.getPortal().getId() + 3;
            
        pi.playPortalSound(); pi.warp(pi.getMapId(), nextPortal);
    } else {    //fail
        pi.playPortalSound(); pi.warp(pi.getMapId(), 2);
    }
    
    return true;
}