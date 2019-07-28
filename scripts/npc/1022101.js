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

var status;
 
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
                        cm.sendYesNo("圣诞老人告诉我去这里，只有他没告诉我什么时候......我希望我能在正确的时间来到这里！哦!顺便说一下，我是鲁尼，我可以带你去#b快乐都市 #k。你准备好出发了吗？");
                } else {
                        cm.getPlayer().saveLocation("HAPPYVILLE");
                        cm.warp(209000000, 0);
                        cm.dispose();
                }
        }
}
