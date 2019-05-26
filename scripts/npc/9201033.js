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
/*
-- HeavenMS 083 Script ----------------------------------------------------------------------------
    NPC - Simon
-- By ---------------------------------------------------------------------------------------------
    Jayd
-- Version Info -----------------------------------------------------------------------------------
    1.0 - First Version by Jayd
---------------------------------------------------------------------------------------------------    
 */

var status;
var smap = 681000000;
var hv = 209000000;
var tst, b2h;
 
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
			cm.sendNext("Let me know if you've changed your mind!");
			cm.dispose();
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
        	if (cm.getMapId() == hv) {
                tst = 1; //to shalom temple
				cm.sendYesNo("The Shalom Temple is unlike any other place in Happyville, would you like to head to #bShalom Temple#k?"); //not GMS lol
			} else if (cm.getMapId() == smap) {
                b2h = 1; //back to happyville
				cm.sendYesNo("Would you like to head back to Happyville?");
			}
		} else if (status == 1) {
            if (tst == 1) {
                cm.warp(smap, 0);
                cm.dispose();
            } else if (b2h == 1) {
                cm.warp(hv, 0);
                cm.dispose();
            }
        }
    }
}