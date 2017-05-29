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
	Shane - Ellinia (101000000)
-- By ---------------------------------------------------------------------------------------------
	Unknown
-- Version Info -----------------------------------------------------------------------------------
	1.1 - Statement fix [Information]
	1.0 - First Version by Unknown
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var check = 0;

function start() {
    if (cm.getLevel() < 25) {
        cm.sendOk("You must be a higher level to enter the Forest of Patience.");
        cm.dispose();
        check = 1;
    }
    else
        cm.sendYesNo("Hi, i'm Shane. I can let you into the Forest of Patience for a small fee. Would you like to enter for #b5000#k mesos?");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendOk("Alright, see you next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (check != 1) {
                if (cm.getPlayer().getMeso() < 5000) {
                    cm.sendOk("Sorry but it doesn't like you have enough mesos!")
                    cm.dispose();
                }
                else {
                    if (cm.isQuestStarted(2050))
                        cm.warp(101000100, 0);
                    else if (cm.isQuestStarted(2051))
                        cm.warp(101000102, 0);
                    else if (cm.getLevel() >= 25 && cm.getLevel() < 50)
                        cm.warp(101000100, 0);
                    else if (cm.getLevel() >= 50)
                        cm.warp(101000102, 0);
                    cm.gainMeso(-5000);
                    cm.dispose();
                }
            }
        }
    }
}