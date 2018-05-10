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
        
        if(status == 0) {
            qm.sendNext("The Orbis seal has been stolen by the Black Wings? Hmm, that has gone awry. Go tell #bLilin#k about this, she must have something in mind on this situation.");
        } else {
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
        
        if(status == 0) {
            qm.sendNext("Oh, hi #h0#! You won't believe what I just uncovered. It's one of your lost skills... What, the seal of Orbis got stolen by the Black Wings? Oh my...");
        } else if(status == 1) {
            qm.sendNext("For now, let me teach you the #bCombo Smash#k, with it you will be able to deal massive amount of damage to many monsters at once. We will need to use it if we want to stand a chance against the Black Wings now, so don't forget it!");
        } else {
            qm.teachSkill(21100004, 0, 20, -1); // combo smash
            
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}
