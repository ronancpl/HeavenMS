/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
/* 
    9201126 - Thief Statue, Lith Harbor
    |- Warps you to 102000003 (Thieves Hideout)

    Version
    |- 1.0 by Jayd
    |- 1.1 by Ronan (check job requirements)
 */

var status;
var map = 103000003;
var job = "Thief";
var jobType = 4;
var no = "Come back to me if you decided to be a #b"+job+"#k.";

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.sendOk(no);
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.sendOk(no);
            cm.dispose();
        }

        if (mode == 1)
            status++;
        else
            status--;

        if(status == 0) {
            if (cm.getJob() == "BEGINNER") {
                if (cm.getLevel() >= 10 && cm.canGetFirstJob(jobType)) {
                    cm.sendYesNo("Hey #h #, I can send you to #b#m"+map+"##k if you want to be a #b"+job+"#k. Do you want to go now?");
                } else {
                    cm.sendOk("If you want to be a #b"+job+"#k, train yourself further until you reach #blevel 10, " + cm.getFirstJobStatRequirement(jobType) + "#k.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("You're much stronger now. Keep training!");
                cm.dispose();
            }
        } else if (status == 1) {
            cm.warp(map, 0);
            cm.dispose();
        }
    }
}