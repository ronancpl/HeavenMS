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
        
        if (status == 0) {  // thanks ZERO傑洛 for noticing this quest shouldn't need a pw -- GMS-like string data thanks to skycombat
            qm.sendNext("What is it? I usually don't welcome uninvited guests, but you have a mysterious aura that makes me curious about what you have to say.", 9);
        } else if (status == 1) {
            qm.sendNext("(You tell her about Giant Nependeath.)", 3);
        } else if (status == 2) {
            qm.sendNext("Giant Nependeath? It's definitely a big problem, but I don't think it's enough to really affect Orbis. Wait, where did you say the Giant Nependeath was, again?", 9);
        } else if (status == 3) {
            qm.sendNext("Neglected Strolling Path.", 3);
        } else if (status == 4) {
            qm.sendNext("...Neglected Strolling Path? If Giant Nependeath is there, someone is trying to enter Sealed Garden! But why? And more importantly, who?", 9);
        } else if (status == 5) {
            qm.sendNext("Sealed Garden?", 3);
        } else if (status == 6) {
            qm.sendAcceptDecline("I can't tell you about Sealed Garden. If you want to find out, I must first see whether you are worthy of the information. Do you mind if I look into your fate?", 9);
        } else if (status == 7) {
            qm.sendOk("Well, now let's look into your fate. Give me a second.");
        } else if (status == 8) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}
