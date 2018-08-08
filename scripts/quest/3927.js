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
/*	
	Author : Ronan Lana
*/

var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            qm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;
        
        
        if(qm.getQuestProgress(3927) == 0) {    // didn't find the wall yet, eh?
            qm.sendOk("Did you find the wall? Look closely, the wall is more near than you think!");
            qm.dispose();
            return;
        }

        if (status == 0) {
            qm.sendSimple("Did you find the wall?\r\n#L0##b I did, but... I have no idea what it's talking about.#l");
        } else if (status == 1) {
            qm.sendSimple("What did it say?\r\n#L0##b 'If I had an iron hammer and a dagger, a bow and an arrow...'#l\r\n#L1# 'Byron S2 Sirin'#l\r\n#L2# 'Ahhh I forgot.'");
        } else if (status == 2) {
            if(selection == 0) {
                qm.sendOk("If I had an iron hammer and a dagger... a bow and an arrow... what does that mean? Do you want me to tell you? I don't know myself. It's something you should think about. If you need a clue... it would go something like... a weapon is just an item... until someone uses it...?");
            } else if(selection == 1) {
                qm.sendOk("Man, Jiyur wrote on the wall again! Arrgh!!");
                qm.dispose();
                return;
            } else {
                qm.sendOk("What? You forgot? Do you remember where it was written?");
                qm.dispose();
                return;
            }
        } else if (status == 3) {
            qm.gainExp(1000);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}