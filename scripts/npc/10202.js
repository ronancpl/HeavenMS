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

function start() {
    cm.sendNext("战士拥有强大的力量和支撑它的体力，在乱斗中最为耀眼。 普通攻击已然强大，在复杂技能的加持下，这个职业是爆炸性输出的完美选择。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0)
           cm.sendNext("如果你想体验成为战士的的感觉，再来找我吧！");
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendYesNo("你想体验成为战士的感觉吗？");
    } else if (status == 1){
	cm.lockUI();
        cm.warp(1020100, 0);
	cm.dispose();
    }
}