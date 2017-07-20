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
 *2408002.js
 *Key Warp for Horn Tail PQ [HTPQ]
 *@author Jvlaple
*/

importPackage(Packages.client.inventory);

function act() {
    var eim = rm.getPlayer().getEventInstance();
    var womanfred = eim.getMapFactory().getMap(240050100);
    var vvpMap = rm.getPlayer().getMapId();
    var vvpKey;
    var vvpOrig = 4001088;
    var vvpStage = -1;
    eim.showClearEffect(false, vvpMap);
    rm.mapMessage(6, "The key has been teleported somewhere...");
    switch (vvpMap) {
        case 240050101 : {
            vvpKey = vvpOrig;
            vvpStage = 1;
            break;
        }
        case 240050102 : {
            vvpKey = vvpOrig + 1;
            vvpStage = 2;
            break;
        }
        case 240050103 : {
            vvpKey = vvpOrig + 2;
            vvpStage = 3;
            break;
        }
        case 240050104 : {
            vvpKey = vvpOrig + 3;
            vvpStage = 4;
            break;
        }
        default : {
            vvpKey = -1;
            break;
        }
    }
    
    eim.setIntProperty(vvpStage + "stageclear", 1);
    
    var tehWomanfred = new Item(vvpKey, 0, 1);
    var theWomanfred = womanfred.getReactorByName("keyDrop1");
    var dropper = eim.getPlayers().get(0);
    womanfred.spawnItemDrop(theWomanfred, dropper, tehWomanfred, theWomanfred.getPosition(), true, true);
    eim.getMapInstance(240050100).dropMessage(6, "A bright flash of light, then a key suddenly appears somewhere in the map.");
}
	
	