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

/*
-- JavaScript -----------------
Jack - Nautilus' Port
-- Created By --
Cody/Cyndicate, totally recoded by Moogra
-- Function --
No specific function, useless text.
-- GMS LIKE --
*/

function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if(status == 0) {
                        if(!cm.haveItem(4220153)) {
                                cm.sendOk("(Scratch scratch...)");
                                cm.dispose();
                        } else {
                                cm.sendYesNo("Hey, nice #bTreasure Map#k you have there? #rCan I keep it#k for the Nautilus crew, if you don't need it any longer?");
                        }
                } else if(status == 1) {
                        cm.gainItem(4220153, -1);
                        cm.dispose();
                }
        }
}
