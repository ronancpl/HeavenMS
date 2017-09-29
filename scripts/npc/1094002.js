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
    Author: Kevin (DietStory v1.02)
    NPC: Bush - Abel Glasses Quest
*/

var rolled = 0;

function start(mode, type, selection){
    if(!cm.isQuestStarted(2186)) {
        cm.sendOk("Just a pile of boxes, nothing special...");
        cm.dispose();
        return;
    }
    
    cm.sendNext("Do you want to obtain a glasses?");
}

function action(mode, type, selection) {
    if(!(cm.haveItem(4031853) || cm.haveItem(4031854) || cm.haveItem(4031855))) {
        rolled = Math.floor(Math.random() * 3);
        
        if(rolled == 0) cm.gainItem(4031853, 1);
        else if(rolled == 1) cm.gainItem(4031854, 1);
        else cm.gainItem(4031855, 1);
    }
    else cm.sendOk("You #balready have#k the glasses that was here!");
    
    cm.dispose();
}