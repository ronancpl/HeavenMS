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
 *jnr5_rp
 */

function enter(pi) {
    var mapplayer = "stage6_comb" + (pi.getMapId() % 10);
    var eim = pi.getEventInstance();
    
    if(eim.getProperty(mapplayer) == null) {
        var comb = "";

        for(var i = 0; i < 10; i++) {
            var r = Math.floor((Math.random() * 4));
            comb += r.toString();
        }

        eim.setProperty(mapplayer, comb);
    }

    var comb = eim.getProperty(mapplayer);
    
    var name = pi.getPortal().getName().substring(2, 4);
    var portalId = parseInt(name, 10);
    
    
    var pRow = Math.floor(portalId / 10);
    var pCol = (portalId % 10);
    
    if (pCol == parseInt(comb.substring(pRow, pRow + 1), 10)) {    //climb
        if(pRow < 9) {
            pi.playPortalSound(); pi.warp(pi.getMapId(), pi.getPortal().getId() + 4);
        } else {
            if(eim.getIntProperty("statusStg6") == 0) {
                eim.setIntProperty("statusStg6", 1);
                eim.giveEventPlayersStageReward(6);
            }
            
            pi.playPortalSound(); pi.warp(pi.getMapId(), 1);
        }
        
    } else {    //fail
        pi.playPortalSound(); pi.warp(pi.getMapId(), 2);
    }
    
    return true;
}