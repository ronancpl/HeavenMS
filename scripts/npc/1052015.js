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

/**
 * @author: Ronan
 * @npc: Billy
 * @map: 193000000 - Premium Road - Kerning City Internet Cafe
 * @func: Cafe PQ Reward Announcer
*/

var status;

var itemSet_lv6 = [1442046, 1432018, 1102146, 1102145, 2022094, 2022544, 2022123, 2022310, 2040727, 2041058, 2040817, 4000030, 4003005, 4003000, 4011007, 4021009, 4011008, 3010098];
var itemQty_lv6 = [1, 1, 1, 1, 35, 15, 20, 20, 1, 1, 1, 30, 30, 30, 1, 1, 3, 1];

var itemSet_lv5 = [1382015, 1382016, 1442044, 1382035, 2022310, 2022068, 2022069, 2022190, 2022047, 2040727, 2040924, 2040501, 4000030, 4003005, 4003000, 4011003, 4011006, 4021004, 3010099];
var itemQty_lv5 = [1, 1, 1, 1, 20, 40, 40, 30, 30, 1, 1, 1, 20, 20, 25, 3, 2, 3, 1];

var itemSet_lv4 = [1332029, 1472027, 1462032, 1492019, 2022045, 2022048, 2022094, 2022123, 2022058, 2041304, 2041019, 2040826, 2040758, 4000030, 4003005, 4003000, 4010007, 4011003, 4021003, 3010016, 3010017];
var itemQty_lv4 = [1, 1, 1, 1, 45, 40, 25, 20, 60, 1, 1, 1, 1, 10, 10, 20, 5, 1, 1, 1, 1];

var itemSet_lv3 = [1302058, 1372008, 1422030, 1422031, 1022082, 2022279, 2022120, 2001001, 2001002, 2022071, 2022189, 2040914, 2041001, 2041041, 2041308, 4031203, 4000030, 4003005, 4003000, 4010004, 4010006, 4020000, 4020006, 3010002, 3010003];
var itemQty_lv3 = [1, 1, 1, 1, 1, 65, 40, 40, 40, 25, 25, 1, 1, 1, 1, 10, 7, 10, 8, 5, 5, 5, 5, 1, 1];

var itemSet_lv2 = [1022073, 1012098, 1012101, 1012102, 1012103, 2022055, 2022056, 2022103, 2020029, 2020032, 2020031, 2022191, 2022016, 2043300, 2043110, 2043800, 2041001, 2040903, 4031203, 4000021, 4003005, 4003000, 4003001, 4010000, 4010001, 4010003, 4010004, 4020004, 3010004, 3010005];
var itemQty_lv2 = [1, 1, 1, 1, 1, 40, 40, 40, 40, 60, 60, 60, 60, 1, 1, 1, 1, 1, 4, 6, 7, 5, 2, 4, 4, 3, 3, 4, 1, 1];

var itemSet_lv1 = [1302021, 1302024, 1302033, 1082150, 1002419, 2022053, 2022054, 2020032, 2022057, 2022096, 2022097, 2022192, 2020030, 2010005, 2022041, 2030000, 2040100, 2040004, 2040207, 2048004, 4031203, 4000021, 4003005, 4003000, 4003001, 4010000, 4010001, 4010002, 4010005, 4020004];
var itemQty_lv1 = [1, 1, 1, 1, 1, 20, 20, 20, 20, 20, 25, 25, 25, 50, 50, 12, 1, 1, 1, 1, 3, 4, 2, 2, 1, 2, 2, 2, 2, 2];

var levels = ["Tier 1", "Tier 2", "Tier 3", "Tier 4", "Tier 5", "Tier 6"];

function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && status == 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;

                if (status == 0) {
                        var sendStr = "The #bInternet Cafe Party Quest#k rewards players with ticket-like #bfigure erasers#k, that can be used on the vending machine to retrieve prizes. By further increasing the stakes, one can get better prizes, separated by #rtiers#k.\r\n\r\nThe possible rewards for each tier are depicted here:\r\n\r\n#b";
                        for(var i = 0; i < 6; i++) {
                            sendStr += "#L" + i + "#" + levels[i] + "#l\r\n";
                        }
                        
                        cm.sendSimple(sendStr);
                } else if(status == 1) {
                        var lvTarget, lvQty;
                        
                        if(selection == 0) {
                                lvTarget = itemSet_lv1;
                                lvQty = itemQty_lv1;
                        } else if(selection == 1) {
                                lvTarget = itemSet_lv2;
                                lvQty = itemQty_lv2;
                        } else if(selection == 2) {
                                lvTarget = itemSet_lv3;
                                lvQty = itemQty_lv3;
                        } else if(selection == 3) {
                                lvTarget = itemSet_lv4;
                                lvQty = itemQty_lv4;
                        } else if(selection == 4) {
                                lvTarget = itemSet_lv5;
                                lvQty = itemQty_lv5;
                        } else {
                                lvTarget = itemSet_lv6;
                                lvQty = itemQty_lv6;
                        }
                        
                        var sendStr = "The following items are being awarded at #b" + levels[selection] + "#k:\r\n\r\n";
                        for(var i = 0; i < lvTarget.length; i++) {
                            sendStr += "  #L" + i + "# #i" + lvTarget[i] + "#  #t" + lvTarget[i] + "#";
                            if(lvQty[i] > 1) sendStr += " (" + lvQty[i] + ")";
                            sendStr += "#l\r\n";
                        }
                        
                        cm.sendPrev(sendStr);
                } else if(status == 2) {
                        cm.dispose();
                }
        }
}
