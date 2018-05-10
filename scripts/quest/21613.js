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
            qm.sendNext("We're a pack of wolves looking for our lost child. I hear you are taking care of our baby. We appreciate your kindness, but it's time to return our baby to us.", 9);
        } else if (status == 1) {
            qm.sendNextPrev("Werewolf is my friend, I can't just hand over a friend.", 3);
        } else if (status == 2) {
            qm.sendAcceptDecline("We understand, but we won't leave without our pup. Tell you what, we'll test you to see if you are worthy of raising a wolf. #rGet ready to be tested by wolves.#k");
        } else if (status == 3) {
            var em = qm.getEventManager("Aran_3rdmount");
            if (em == null) {
                qm.sendOk("Sorry, but the 3rd mount quest (Wolves) is closed.");
                qm.dispose();
                return;
            }
            else {
                if (em.getProperty("noEntry") == "false") {
                    var eim = em.newInstance("Aran_3rdmount");
                    eim.registerPlayer(qm.getPlayer());
                    
                    qm.forceStartQuest();
                    qm.dispose();
                }
                else {
                    qm.sendOk("There is currently someone in this map, come back later.");
                    qm.dispose();
                }
            }
        }
    }
}
