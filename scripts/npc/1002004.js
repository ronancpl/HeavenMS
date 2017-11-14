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
-- Odin JavaScript --------------------------------------------------------------------------------
	VIP Cab - Victoria Road : Lith Harbor (104000000)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var cost = 10000;

function start() {
    cm.sendNext("嗨，伙计！这个出租车只为VIP客户服务。不只是像普通出租车那样带你到另一个城镇，我们还提供称得上VIP级的服务。价格确实有点贵，不过... 只要 10,000 金币，我们可以将你安全送到 \r\n#b蚂蚁广场#k.");
}

function action(mode, type, selection) {
    status++;
    if (mode == -1){
        if(mode == 0)
            cm.sendNext("这个城镇还提供了很多服务。如果你想去蚂蚁广场请来找我们。");
        cm.dispose();
        return;
    }
    if (status == 1) {
        cm.sendYesNo(cm.getJobId() == 0 ? "我们对新手有90%的折扣。蚂蚁广场位于金银岛中心的迷宫深处，那里有24小时营业的移动商店。你想话费 #b1,000 mesos#k 到那里去吗？" : "对非新手玩家我们收取常规费用。蚂蚁广场位于金银岛中心的迷宫深处，那里有24小时营业的移动商店。你想话费 #b10,000 mesos#k 到那里去吗？");
        cost /= ((cm.getJobId() == 0) ? 10 : 1);
    } else if (status == 2) {
        if (cm.getMeso() < cost)
            cm.sendNext("看起来你没有带上足够的钱...")
        else {
            cm.gainMeso(-cost);
            cm.warp(105070001);
        }
        cm.dispose();
    }
}