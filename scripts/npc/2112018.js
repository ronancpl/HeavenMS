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
 * @author: Ronan
 * @npc: Romeo & Juliet
 * @func: MagatiaPQ exit
*/

var status;
 
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
    
                var eim = cm.getEventInstance();
                
                if(status == 0) {
                        if(eim.getIntProperty("escortFail") == 1) {
                                cm.sendNext("Thanks to you, we were capable of reunion once again. Yulete will now be forwarded to jail for attempt against the Law of Magatia. Once again, thank you.");
                        } else {
                                cm.sendNext("Thanks to you, we were capable of reunion once again. Yulete will now pass through rehabilitation, as his studies are invaluable for the growth of our town, and all his doings were being made because he was blinded by the greed for power, although it was for the sake of Magatia. Once again, thank you.");
                        }
                } else {
                        if(eim.giveEventReward(cm.getPlayer())) {
                                cm.warp((eim.getIntProperty("isAlcadno") == 0) ? 261000011 : 261000021);
                        } else {
                                cm.sendOk("Please free a slot on one of your inventories before receiving your reward.");
                        }
                        
                        cm.dispose();
                }
        }
}