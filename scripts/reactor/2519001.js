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
/*2519001.js - Reactor used at the door on stage 4.
 *@author Ronan
 */
 
importPackage(Packages.tools);
importPackage(java.awt);
 
function act() {
    var denyWidth = 320, denyHeight = 150;
    var denyPos = rm.getReactor().getPosition();
    var denyArea = new Rectangle(denyPos.getX() - denyWidth / 2, denyPos.getY() - denyHeight / 2, denyWidth, denyHeight);
    
    rm.getReactor().getMap().setAllowSpawnPointInBox(false, denyArea);
    
    var map = rm.getReactor().getMap();
    if(map.getReactorByName("sMob1").getState() >= 1 && map.getReactorByName("sMob3").getState() >= 1 && map.getReactorByName("sMob4").getState() >= 1 && map.countMonsters() == 0) {
        rm.getEventInstance().showClearEffect(map.getId());
    }
}
