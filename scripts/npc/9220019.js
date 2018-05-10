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

var status;
 
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
    
                if(status == 0) {
                        var mapid = cm.getMapId();
                        if(mapid == 674030100) {
                                cm.sendNext("Hi, I'm #p9220019#.");
                                cm.dispose();
                                return;
                        } else if(mapid == 674030300) {
                                cm.sendNext("Hi there, #h0#. This is the MV's treasure room. Use the time you have here to do whatever you want, there are a lot of things to uncover here, actually. Or else you can use the portal here to #rgo back#k to the entrance.");
                                cm.dispose();
                                return;
                        }
                    
                        cm.sendYesNo("Are you sure you want to return? By returning now you are leaving your partners behind, do you really want to do it?");
                } else if(status == 1) {
                        cm.warp(674030100);
                        cm.dispose();
                }
        }
}