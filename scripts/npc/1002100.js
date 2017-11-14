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
// Jane the Alchemist
var status = -1;
var amount = -1;
var items = [[2000002,310],[2022003,1060],[2022000,1600],[2001000,3120]];
var item;

function start() {
    if (cm.isQuestCompleted(2013))
        cm.sendNext("是你...谢谢你，我已经完成了不少。今天我已经制作了一堆物品。如果你需要什么请告诉我。");
    else {
        if (cm.isQuestCompleted(2010))
            cm.sendNext("你看起来没有强大到能够购买我的药水...");
        else
            cm.sendOk("我的梦想是四处旅游，就像你一样。我的父亲却不允许我那样做，因为他认为那太危险。他也许会同意，不过，如果我能给他展现一些我不是一个柔弱女孩的证据的话，他会认为我...");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0 && type == 1)
            cm.sendNext("我还有很多以前你带给我的材料。物品都在这，花点时间挑一挑吧。");
        cm.dispose();
        return;
    }
    if (status == 0){
        var selStr = "你想购买哪个物品?#b";
        for (var i = 0; i < items.length; i++)
            selStr += "\r\n#L" + i + "##i" + items[i][0] + "# (价格 : " + items[i][1] + " 金币)#l";
        cm.sendSimple(selStr);
    } else if (status == 1) {
        item = items[selection];
        var recHpMp = ["300 HP.","1000 HP.","800 MP","1000 HP and MP."];
        cm.sendGetNumber("你想要 #b#t" + item[0] + "##k吗？ #t" + item[0] + "# 可以帮你恢复 " + recHpMp[selection] + " 你想购买多少？", 1, 1, 100);
    } else if (status == 2) {
        cm.sendYesNo("你要购买 #r" + selection + "#k #b#t" + item[0] + "#(s)#k吗? #t" + item[0] + "# 每个需要 " + item[1] + " 金币，总共需要 #r" + (item[1] * selection) + "#k 金币。");
        amount = selection;
    } else if (status == 3) {
        if (cm.getMeso() < item[1] * amount)
            cm.sendNext("你的金币是不是不太够？请确认你的其他栏中有空位，并且至少有 #r" + (item[1] * selectedItem) + "#k 金币。");
        else {
            if (cm.canHold(item[0])) {
                cm.gainMeso(-item[1] * amount);
                cm.gainItem(item[0], amount);
                cm.sendNext("欢迎光临。这里的物品随时可以打造，只要你需要随时欢迎。");
            } else
                cm.sendNext("请确定你的背包中的其他栏有剩余空间。");
        }
        cm.dispose();
    }
}