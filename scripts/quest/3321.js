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

importPackage(Packages.client);

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
            qm.sendNext("As you may know by now, I am Dr. De Lang. Once an influent alchemist amongst the ranks of the Alcadno society, I have been disconnected from their society due to the disaster of the aftermatch of my failed experiments, that can be seen now all over Magatia.");
        } else if (status == 1) {
            qm.sendNextPrev("Huroids, my creation, were originally engineered to fulfill both domestic, scientific and military affairs, however a critical malfunction on their main processing unit chips rendered them unstable and violent, rapidly causing upheavals and havoc all over the place. Due to that, I've been stripped of my status as Alcadno's alchemist and researcher and got myself an arrest warrant.");
        } else if (status == 2) {
            qm.sendAcceptDecline("Even so, I must not be stopped now! Creations of mine are still roaming around causing destruction and casualities every day, with no great hopes of repelling them from the city! They can replicate themselves too fast, normal weapons cannot stop them. I've been since relentlessly researching a way to shut them down all at once, trying to find a way to stop this insanity. Surely you can understand my situation?");
        } else if (status == 3) {
            qm.sendNext("My gratitude for understanding my point. You must have met Parwen, since you know where I am. Make him aware of the current situation.");
        } else if (status == 4) {
            qm.sendNext("Oh, and I have a personal favor to ask, if it's not too much. I am worried about my wife, #b#p2111004##k. Since the incident with the Huroids I could send a word to her, that must have made a toll on her... Please, if you could, could you get the #bSilver Pendant#k I left #bback at home#k, and give it to her in my stead? I regret not giving the item right away to her, it was her birthday... Maybe giving it now to her can get her a good sleeping night, at least.");
        } else if (status == 5) {
            qm.sendNext("#rMake sure to remember this pattern!#k I've hid the pendant in my house, in a container #bbehind the water pipes#k. The pipes must be turned #bin order#k: top, bottom, middle. And then, enter the secret password: '#rmy love Phyllia#k'.");
            
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}