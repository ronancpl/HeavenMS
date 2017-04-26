/*  
      
    Copyright (C) This file is part of the OdinMS Maple Story Server  
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
   
/**  
Rooney - Happyville Warp NPC
**/   
   
var status = 0;   
   
function start() {   
    action(1, 0, 0);   
}   
   
function action(mode, type, selection) {   
    if (status == 0) {   
        cm.sendYesNo("Santa told me to go to here, only he didn't told me when...  I hope i'm here on the right time! Oh! By the way, I'm Rooney, I can take you to #bHappyVille#k. Are you ready to go?");
        status++;   
    } else {   
        if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) {   
            cm.dispose();   
        } else {
            cm.getPlayer().saveLocation("HAPPYVILLE");
            cm.warp(209000000, 0);
            cm.dispose();   
        }   
    }   
}