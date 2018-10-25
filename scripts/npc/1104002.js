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

var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                var mapobj = cm.getMap();
            
                if (mode == 0 && type > 0) {
                        cm.getPlayer().dropMessage(5, "Eleanor: Oh, lost the Empress and still challenging us? Now you've done it! Prepare yourself!!!");
                        
                        mapobj.spawnMonsterOnGroundBelow(Packages.server.life.MapleLifeFactory.getMonster(9001010), new Packages.java.awt.Point(850, 0));
                        mapobj.destroyNPC(1104002);
                        
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if(status == 0) {
                        if(!cm.isQuestStarted(20407)) {
                                cm.sendOk("... Knight, you still #bseem unsure to face this fight#k, don't you? There's no grace in challenging someone when they are still not mentally ready for the battle. Talk your peace to that big clumsy bird of yours, maybe it'll put some guts on you.");
                                cm.dispose();
                                return;
                        }
                    
                        cm.sendAcceptDecline("Hahahahaha! This place's Empress is already under my domain, that's surely a great advance on the #bBlack Wings#k' overthrow towards Maple World... And you, there? Still wants to face us? Or, better yet, since you seem strong enough to be quite a supplementary reinforcement at our service, #rwill you meet our expectations and fancy joining us#k since there's nothing more you can do?");
                } else if (status == 1) {
                        cm.sendOk("Heh, cowards have no place on the #rBlack Mage's#k army. Begone!");
                        cm.dispose();
                }
        }
}
