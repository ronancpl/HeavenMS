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
            qm.sendAcceptDecline("Who would have thought that the hero's successor would reappear after hundred of years...? Will you bring prosperity to Maple World or will you end its existence? I suppose it really doesn't matter. Alright, I'll tell you what I know about the Seal Stone of Mu Lung.");
        } else if (status == 1) {
            qm.sendNext("The Seal Stone of Mu Lung is located at the Sealed Temple. You will find the entrance deep inside the Mu Lung Temple. You can enter the Sealed Temple if you find the pillar with the word 'Entrance' wtritten on it. The password is: #bActions speak better than words#k. Maybe you will find the Shadow Knight there, as he probably is waiting for me there. I think the Hero's sucessor is more able to face him than myself, so prepare yourself.");
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
        
        if (status == 0) {
            qm.sendNext("So you have defeated the Shadow Knight. I have never doubted of your handiwork, and you handled the task well.");
        } else if (status == 1) {
            qm.sendNext("But yet, something made you unhappy. What could it be? ... No... Black Wings took away the Seal stone? I'm afraid nothing can be done anymore. I suggest you return to your group tactician, Tru is it?, and tell him about the situation now. Tell him about the loss here in Mu Lung. There's no time to lose, hurry!");
        } else if (status == 2) {
            qm.gainExp(16000);
            qm.forceCompleteQuest();
            
            qm.dispose();
        }
    }
}