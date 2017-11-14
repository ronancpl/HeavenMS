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
    cm.sendYesNo("你完成新手训练了吗? 如果想要离开这里, 我可以带你离开。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0)
            cm.sendOk("还没完成新手训练嘛? 如果想要离开这里, 请不要犹豫地告诉我");
        cm.dispose();
        return;
    }
    if (status == 0)
        cm.sendNext("那么我就带你离开这里了。干得好！");
    else{
        cm.warp(40000, 0);
        cm.dispose();
    }
}