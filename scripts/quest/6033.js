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
/* Maker Skill
	Moren's Second round of teaching
	2nd skill level
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
            qm.sendNext("Hm, so you claim to have brought the #b#t4260003##k? Ok, let's take a look into it.");
        } else if (status == 1) {
            if(qm.getQuestProgress(6033) == 1 && qm.haveItem(4260003, 1)) {
                qm.sendNext("You indeed have crafted a fine piece of Monster Crystal, I see. You passed! Now, I shall teach you the next steps of the Maker skill. Keep the monster crystal with you as well, it's your work.");
            } else {
                qm.sendNext("Hey, what's wrong? I did tell you to make a monster crystal to pass my test, didn't I? Buying one or crafting before the start of the test is NOT part of the deal. Go craft me an #b#t4260003##k.");
                qm.dispose();
                return;
            }
        } else {
            var skillid = Math.floor(qm.getPlayer().getJob().getId() / 1000) * 10000000 + 1007;
            qm.teachSkill(skillid, 2, 3, -1);

            qm.gainExp(230000);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}