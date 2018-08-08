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

function start(mode, type, selection) {
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
            qm.sendNext("Well, I'm not really busy of anything, bit I don't feel like concocting medicine. Can you come back later? If you don't mind, move.", 9);
        } else if (status == 1) {
            qm.sendNextPrev("I heard you met the Shadow Knight of the Black Wings.", 3);
        } else if (status == 2) {
            qm.sendNextPrev("Ah, you mean that guy dressed in black with a menacing wrinkle in his forehead? Why, yes I did. I did meet him. I event got an item for him. He asked me to deliver it to that old man, Mu Gong.", 9);
        } else if (status == 3) {
            qm.sendNextPrev("An item?", 3);
        } else if (status == 4) {
            qm.sendNextPrev("Yes, a big #bHanging Scroll#k. He gave it to me without saying much. He just asked me to deliver it. He looked scary, as if he would chase me down if I didn't do as he said. Wheeeeeew, that was an experience.", 9);
        } else if (status == 5) {
            qm.sendNextPrev("So did you deliver the Hanging Scroll to him?", 3);
        } else if (status == 6) {
            qm.sendAcceptDecline("Well, the thing is... There is a slight problem, care to listen?");
        } else if (status == 7) {
            qm.sendNext("So what happened was... I was making a new type of medicine, so I filled a pot with water and started boiling some herbs. That's when I made the mistake of... dropping the Hanging Scroll, right into the pot. Oh gosh, I pulled it out as fast as I could, but the Hanging Scroll, was already soaked and the writing on it had already disappeared.", 9);
        } else if (status == 8) {
            qm.sendNextPrev("So then I thought, well what's the point of delivering it to Mu Gong? I must first restore the writing on Hanging Scroll. That's why I need you to do something for me. The guy down there writing on Hanging Scroll... is #bJin Jin#k, the greatest artist on all of Mu Lung. I'm sure he'd be able to restore the writing on Hanging Scroll.", 9);
        } else {
            if(!qm.haveItem(4220151, 1)) {
                if(!qm.canHold(4220151, 1)) {
                    qm.sendOk("Please free a room on your ETC inventory.", 9);
                    qm.dispose();
                    return;
                }
                
                qm.gainItem(4220151, 1);
            }
            
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}

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
            qm.sendNext("Oh, you brought the ink. Now let me pour it, cautiously.... Almost there, almost. ... ..... Kyaaa! Th-the letter. It says: 'I'll be there to take your Seal Rock of Mu Lung.'");
        } else if (status == 1) {
            qm.gainItem(4032342, -8);
            qm.gainItem(4220151, -1);
            qm.gainExp(10000);
            
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}