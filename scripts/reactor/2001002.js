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
/* @Author Ronan
 * 
 * 2001002.js: Either spawns a PQ mob or drops the Statue piece.
*/

function act() {
    if(rm.getEventInstance().getIntProperty("statusStg2") == -1) {
        var rnd = Math.max(Math.floor(Math.random() * 14), 4);
        
        rm.getEventInstance().setProperty("statusStg2", "" + rnd);
        rm.getEventInstance().setProperty("statusStg2_c", "0");
    }
    
    var limit = rm.getEventInstance().getIntProperty("statusStg2");
    var count = rm.getEventInstance().getIntProperty("statusStg2_c");
    if(count >= limit) {
        rm.dropItems();
        
        var eim = rm.getEventInstance();
        eim.giveEventPlayersExp(3500);
        
        eim.setProperty("statusStg2", "1");
        eim.showClearEffect(true);
    }
    else {
        count++;
        rm.getEventInstance().setProperty("statusStg2_c", count);
        
        var nextHashed = (11 * (count)) % 14;
        
        var nextPos = rm.getMap().getReactorById(2001002 + nextHashed).getPosition();
        rm.spawnMonster(9300040, 1, nextPos);
    }
}