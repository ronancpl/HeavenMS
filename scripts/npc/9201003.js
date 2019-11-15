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
 *9201003.js - Mom and Dad
 *@author Jvlaple
 *@author Ronan
 */
var numberOfLoves = 0;
var status = -1;
var state = 0;

function hasProofOfLoves(player) {
    var count = 0;
    
    for(var i = 4031367; i <= 4031372; i++) {
        if(player.haveItem(i)) {
            count++;
        }
    }
    
    return count >= 4;
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            if (!cm.isQuestStarted(100400)) {
                cm.sendOk("Hello we're Mom and Dad...");
                cm.dispose();
            } else {
                if (cm.getQuestProgressInt(100400, 1) == 0) {
                    cm.sendNext("Mom, dad, I have a request to do to both of you... I wanna know more about the path you've already been walking since always, the path of loving and caring for someone dear to me.", 2);
                } else {
                    if(!hasProofOfLoves(cm.getPlayer())) {
                        cm.sendOk("Dear, we need to make sure you are really ready to fall in love with whoever you choose to be your partner, please bring here #b4 #t4031367#'s#k.");
                        cm.dispose();
                    } else {
                        cm.sendNext("#b#h0##k, you made us proud today. You may now have #rour blessings#k to choose whoever you like to be your fiancee. You may now consult #p9201000#, the Wedding Jeweler. Have a sooth, loving and caring journey ahead~~");
                        state = 1;
                    }
                }
            }
        } else if (status == 1) {
            if (state == 0) {
                cm.sendNextPrev("My dear! How thoughtful of you asking our help. Surely we will help you out!");
            } else {
                cm.sendOk("Mom... Dad... Thanks a lot for your tender support!!!", 2);
                
                cm.completeQuest(100400);
                cm.gainExp(20000 * cm.getPlayer().getExpRate());
                for(var i = 4031367; i <= 4031372; i++) {
                    cm.removeAll(i);
                }
                
                cm.dispose();
            }
        } else if (status == 2) {
            cm.sendNextPrev("Certainly you must have already seen #rNanas, the fairies of Love#k, around the Maple world. From 4 of them, collect #b4 #t4031367#'s#k and bring them here. This journey shall clear some questions you may have about love...");
        } else if (status == 3) {
            cm.setQuestProgress(100400, 1, 1);
            cm.dispose();
        }
    }
}