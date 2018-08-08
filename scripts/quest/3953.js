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

var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            qm.sendSimple("Eh, are you still saying Deo is a monster? No, Deo is not a monster, he is a peaceful leader of the Royal cactus from the region.\r\n\r\n#L0##bHave you heard that a group of merchants crossing through the desert were attacked by the monsters?#k");
        } else if (status == 1) {
            qm.sendSimple("Is that so? I wonder why these merchants were wandering so recklessly in the desert. They trespassed the territory of the Cactus'! They shouldn't be wandering around in the first place, they should first have the leave of the Ariant Counsel.\r\n\r\n#L0##bThis is all because of the Queen's negligence in maintaining the safety of the town.#k");
        } else if (status == 2) {
            qm.sendSimple("Ehh... Yeah, the city is not really doing well because of the currently ruling govern, that's indeed a fact. If only the Guardians of the Deserts returned to put order on this mess...\r\n\r\n#L0##bWhat is the Guardian of the Deserts doing when we're under the Queen's tyranny?#k");
        } else if (status == 3) {
            qm.sendSimple("They have departed on an expedition to get rid of some major threats in the desert that were ravaging Ariant, for quite some time now... It's strange, they should have already returned... Thinking about it now, the last attack on the merchants was around the direction the Guardians departed... No, that can't be... Can it?\r\n\r\n#L0##bPerhaps Deo has already turned into a monster.#k");
        } else if (status == 4) {
            qm.gainItem(4011008, -1);
            
            qm.sendNext("We're in great trouble, if it is like this. And it really seems like it. If the Royal Cactus Deo has gone insane, Ariant is done for. You, can you do something to defeat Deo? We really need your help now.");
            qm.gainExp(20000);
            
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}