/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* 
 * @Author Lerk
 * 
 * Shawn, Victoria Road: Excavation Site<Camp> (101030104)
 * 
 * Guild Quest Info
 */

var status;
var selectedOption;

function start() {
    selectedOption = -1;
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
        if (mode == 1 && status == 3) {
            status = 0;
        }
        if (status == 0) {
            var prompt = "\r\n#b#L0# What's Sharenian?#l\r\n#b#L1# #t4001024#? What's that?#l\r\n#b#L2# Guild Quest?#l\r\n#b#L3# No, I'm fine now.#l";
            if (selectedOption == -1) {
                prompt = "We, the Union of Guilds, have been trying to decipher 'Emerald Tablet,' a treasured old relic, for a long time. As a result, we have found out that Sharenian, the mysterious country from the past, lay asleep here. We also found out that clues of #t4001024#, a legendary, mythical jewelry, may be here at the remains of Sharenian. This is why the Union of Guilds have opened Guild Quest to ultimately find #t4001024#." + prompt;
            } else {
                prompt = "Do you have any other questions?" + prompt;
            }
            cm.sendSimple(prompt);
        }
        else if (status == 1) {
            selectedOption = selection;
            if (selectedOption == 0) {
                cm.sendNext("Sharenian was a literate civilization from the past that had control over every area of the Victoria Island. The Temple of Golem, the Shrine in the deep part of the Dungeon, and other old architectural constructions where no one knows who built it are indeed made during the Sharenian times.");
            }
            else if (selectedOption == 1) {
                cm.sendNext("#t4001024# is a legendary jewel that brings eternal youth to the one that possesses it. Ironically, it seems like everyone that had #t4001024# ended up downtrodden, which should explain the downfall of Sharenian.");
                status = -1;
            }
            else if (selectedOption == 2) {
                cm.sendNext("I've sent groups of explorers to Sharenian before, but none of them ever came back, which prompted us to start the Guild Quest. We've been waiting for guilds that are strong enough to take on tough challenges, guilds like yours.");
            }
            else if (selectedOption == 3) {
                cm.sendOk("Really? If you have anything else to ask, please feel free to talk to me.");
                cm.dispose();
            }
            else {
                cm.dispose();
            }
        }
        else if (status == 2) { //should only be available for options 0 and 2
            if (selectedOption == 0) {
                cm.sendNextPrev("The last king of Sharenian was a gentleman named Sharen III, and apparently he was a very wise and compassionate king. But one day, the whole kingdom collapsed, and there was no explanation made for it.");
            }
            else if (selectedOption == 2) {
                cm.sendNextPrev("The ultimate goal of this Guild Quest is to explore Sharenian and find #t4001024#. This is not a task where power solves everything. Teamwork is more important here.");
            }
            else {
                cm.dispose();
            }
        }
    }
}