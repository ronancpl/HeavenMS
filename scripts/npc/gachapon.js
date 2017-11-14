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
/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

var status;
var ticketId = 5220000;
var mapName = ["射手村", "魔法密林", "勇士部落", "废弃都市", "林中之城", "蘑菇神社", "昭和村澡堂 (M)", "昭和村澡堂 (F)", "新叶城", "诺特勒斯"];
var curMapName = "";

function start() {
    status = -1;
	curMapName = mapName[(cm.getNpc() != 9100117 && cm.getNpc() != 9100109) ? (cm.getNpc() - 9100100) : cm.getNpc() == 9100109 ? 8 : 9];
	
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0 && mode == 1) {
			if (cm.haveItem(ticketId)) {
				cm.sendYesNo("您将使用 " + curMapName + " 扭蛋机. 你想使用扭蛋券吗?");
			} else {
				cm.sendSimple("欢迎来到 " + curMapName + " 扭蛋机. 我有什么可以帮到您的吗?\r\n\r\n#L0#什么是扭蛋机?#l\r\n#L1#我在哪里可以买到扭蛋券?#l");
			}
		} else if(status == 1 && cm.haveItem(ticketId)) {
			if(cm.canHold(1302000) && cm.canHold(2000000) && cm.canHold(3010001) && cm.canHold(4000000)) { // One free slot in every inventory.
				cm.gainItem(ticketId, -1);
				cm.doGachapon();
			} else {
				cm.sendOk("请确认在 #r装备, 消耗, 设置, #k和 #r其他#k 栏中至少有一个空格.");
			}
			cm.dispose();
		} else if(status == 1) {
			if (selection == 0) {
                cm.sendNext("通过扭蛋获取稀有卷轴, 装备, 椅子, 技能书, 以及其他绝赞物品! 你需要做的仅仅是使用一张 #b扭蛋券#k 来成为抽选随机物品的欧皇.");
            } else {
                cm.sendNext("扭蛋券可以在 #r现金商城#k 中通过点券或抵用券购买. 点击右下角中红色商店按钮即可进入 #r现金商城#k.");
            }
		} else if(status == 2) {
			cm.sendNextPrev("你会通过 " + curMapName + " 扭蛋机发掘各种物品, 但是大多数物品和卷轴都会与 " + curMapName + "有关.");
		} else {
			cm.dispose();
		}
    }
}