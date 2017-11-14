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
/* Changes the players name.
	Can only be accessed with the item 2430026.
 */

function start() {
    status = -1;
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
			if(cm.haveItem(2430026)) {
				cm.sendYesNo("如果你愿意的话我可以帮你更改名字.", 1);
			} else {
				cm.dispose();
			}
		} else if(status == 1) {
			cm.sendGetText("请输入你想要的名字.");
		} else if(status == 2) {
			var text = cm.getText();
			var canCreate = Packages.client.MapleCharacter.canCreateChar(text);
			if(canCreate) {
				cm.getPlayer().setName(text);
				cm.sendOk("你的名字已经被更改为 #b" + text + "#k. 你需要重新登陆才能生效.", 1);
				cm.gainItem(2430026, -1);
			} else {
				cm.sendNext("你不能使用名字 #b" + text + "#k 或者它已经被其他人使用了.", 1);
			}
		} else if(status == 3) {
			cm.dispose();
			cm.getClient().disconnect(false, false);
		}
    }
}