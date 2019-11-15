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
/* 9010021 - Wolf Spirit Ryko
    @author Ronan
 */
 var status;

function start() {
    status = -1;
    if (!Packages.config.YamlConfig.config.server.USE_REBIRTH_SYSTEM) {
        cm.sendOk("... I came from distant planes to assist the fight against the #rBlack Magician#k. Right now I search my master, have you seen him?");
        cm.dispose();
        return;
    }
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendNext("Come to me when you want to be reborn again. You currently have a total of #r" + cm.getChar().getReborns() + " #krebirths.");
    } else if (status == 1) {
        cm.sendSimple("What do you want me to do today: \r\n \r\n #L0##bI want to be rebirthed#l \r\n #L1##bMaybe next time#k#l");
    } else if (status == 2) {
        if (selection == 0) {
            if (cm.getChar().getLevel() == 200) {
                cm.sendYesNo("Are you sure you want to be rebirthed?");
            } else {
                cm.sendOk("You are not level 200, please come back when you hit level 200.");
                cm.dispose();
            }
        } else if (selection == 1) {
            cm.sendOk("Ok Bye")
            cm.dispose();
        }
    } else if (status == 3 && type == 1) {
        cm.getChar().executeReborn();
        cm.sendOk("You have now been reborn. That's a total of #r" + cm.getChar().getReborns() + "#k rebirths");
        cm.dispose();
    }


}