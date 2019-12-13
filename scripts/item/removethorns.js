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

var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        im.dispose();
    } else {
        if (mode == 0 && type > 0) {
            im.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if(status == 0) {
            if (im.getMapId() == 106020400 && im.isQuestActive(2324)) {
                var player = im.getPlayer();
                
                var portal = im.getMap().getPortal("right00");
                if (portal != null && portal.getPosition().distance(player.getPosition()) < 210) {
                    player.gainExp(3300 * player.getExpRate());

                    im.forceCompleteQuest(2324);
                    im.removeAll(2430015);
                    im.playerMessage(5, "You have used the Thorn Remover to clear the path.");
                }
            }
            
            im.dispose();
        }
    }
}
