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
    cm.sendNext("魔法师拥有华丽的元素魔法并且有着帮助整个团队的辅助魔法。 在二次转职之后，元素魔法将会给相克元素的敌人以充足的伤害。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0)
           cm.sendNext("如果你想体验成为魔法师的感觉，再来找我吧。");
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendYesNo("你想体验成为魔法师的感觉吗？");
    } else if (status == 1){
	cm.lockUI();
        cm.warp(1020200, 0);
        cm.dispose();
    }
}