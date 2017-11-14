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
/* Author: Xterminator
	NPC Name: 		Sera
	Map(s): 		Maple Road : Entrance - Mushroom Town Training Camp (0), Maple Road: Upper level of the Training Camp (1), Maple Road : Entrance - Mushroom Town Training Camp (3)
	Description: 		First NPC
*/

var status = -1;

function start() {
    if (cm.c.getPlayer().getMapId() == 0 || cm.c.getPlayer().getMapId() == 3)
        cm.sendYesNo("欢迎来到冒险岛世界。 这个训练营是为了帮助新手上路。 你想进入训练营吗？ 有些人不接受训练就开始旅程，不过我强烈建议你先进行一次训练。");
    else
        cm.sendNext("这是你第一个训练过程开始的影像室。 在这个房间里，你会提前看到你选择的职业。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(mode == 0 && status == 0){
            cm.sendYesNo("你真的要马上开始你的路程吗？");
            return;
        }else if(mode == 0 && status == 1 && type == 0){
            status -= 2;
            start();
            return;
        }else if(mode == 0 && status == 1 && type == 1)
            cm.sendNext("请在你做好最终决定之后再和我对话.");
        cm.dispose();
        return;
    }
    if (cm.c.getPlayer().getMapId() == 0 || cm.c.getPlayer().getMapId() == 3){
        if(status == 0){
            cm.sendNext("那么，我会让你进入训练营。请遵循教官的引导。");
        }else if(status == 1 && type == 1){
            cm.sendNext("看起来你想不接受训练就开始旅程。那么我会将你送到训练场上。小心哦~");
        }else if(status == 1){
            cm.warp(1, 0);
            dispose();
        }else{
            cm.warp(40000, 0);
            dispose();
        }
    }else
    if(status == 0)
        cm.sendPrev("当你训练足够时，你将会被任命为一个职业。 你可以成为射手村的弓箭手，魔法密林的魔法师，勇士部落的战士，废弃都市的飞侠...");
    else
        cm.dispose();
}