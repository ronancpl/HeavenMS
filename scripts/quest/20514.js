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

// @Author Ronan

importPackage(Packages.constants.game);
importPackage(Packages.server);

function raiseOpen() {
    var chr = qm.getPlayer();
    var questStatus = chr.getQuestStatus(qm.getQuest());

    if (questStatus == 0) {
        qm.setQuestProgress(20515, 0, chr.getLevel());
        qm.setQuestProgress(20515, 1, chr.getExp());
    } else if (questStatus == 1) {  // update mimiana progress...
        var diffExp = chr.getExp() - qm.getQuestProgressInt(20515, 1);

        var initLevel = qm.getQuestProgressInt(20515, 0);
        for (var i = initLevel; i < chr.getLevel(); i++) {
            diffExp += ExpTable.getExpNeededForLevel(i);
        }
        
        if (diffExp > 0) {  // thanks IxianMace for noticing Mimiana egg not following progress by EXP
            var consItem = MapleItemInformationProvider.getInstance().getQuestConsumablesInfo(4220137);
            var exp = consItem.exp;
            var grade = consItem.grade;
            
            qm.setQuestProgress(20514, 0, Math.min(diffExp, exp * grade));
        }
    }

    qm.dispose();
}

