/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
 * @npc: Pio
 * @func: Gachapon Loot Announcer
*/

var status;
var gachaMessages;
 
function start() {
        gachaMessages = Packages.server.gachapon.MapleGachapon.Gachapon.getLootInfo();
        gachas = Packages.server.gachapon.MapleGachapon.Gachapon.values();
    
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
                        var sendStr = "Hi, #r#p" + cm.getNpc() + "##k here! I'm announcing all obtainable loots from the Gachapons. Which Gachapon machine would you like to look?\r\n\r\n#b" + gachaMessages[0] + "#k";
                        cm.sendSimple(sendStr);
                } else if(status == 1) {
                        var sendStr = "Loots from #b" + gachas[selection].name() + "#k:\r\n\r\n" + gachaMessages[selection + 1];
                        cm.sendPrev(sendStr);
                } else if(status == 2) {
                        cm.dispose();
                }
        }
}