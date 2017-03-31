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
var status = -1;

function activateShamanRock(slot,progress) {
    var active = (progress >> slot) % 2;
    if(!active) {
        progress |= (1 << slot);
        
        cm.updateQuest(2236, progress);
        cm.gainItem(4032263, -1);
        cm.sendOk("The seal took it's place, repelling the evil in the area.");
        return 1;
    }
    
    return 0;
}

function start() {
    if(cm.isQuestStarted(2236) && cm.haveItem(4032263, 1)) {
        var progress = cm.getQuestProgress(2236);
        var map = cm.getMapId();
        
        if(map == 105050200) activateShamanRock(0,progress);
        else if(map == 105060000) activateShamanRock(1,progress);
        else if(map == 105070000) activateShamanRock(2,progress);
        
        else if(map == 105090000) { // workaround... TWO SAME NPC ID ON SAME MAP
            if(!activateShamanRock(3,progress)) {
                activateShamanRock(4,progress);
            }
        }
        
        else if(map == 105090100) activateShamanRock(5,progress);
    }
    
    cm.dispose();
}
