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

/*The encrypted slate
 *@author Jvlaple <eat268@hotmail.com>
 */

var status = 0;
var minLevel = 80;
var maxLevel = 255;
var minPlayers = 1;
var maxPlayers = 6;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if(cm.haveItem(4001086)) {
                cm.sendYesNo("Do you want to access #b#m240050400##k right now?");
            } else {
                cm.sendOk("Those who don't have the #r#t4001086##k must prove their valor before challenging #bHorntail#k.");
                cm.dispose();
            }
        }
        else {
            cm.warp(240050400);
            cm.dispose();
        }
    }
}
					
					
