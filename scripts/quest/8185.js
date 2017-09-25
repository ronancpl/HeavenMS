/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
/* 	Author: 		Blue
	Name:	 		Garnox
	Map(s): 		New Leaf City : Town Center
	Description: 		Quest - Pet Evolution2
*/

importPackage (Packages.net.server.channel.handlers);

var status = -1;

function end(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
                        if(qm.getMeso() < 10000) {
                            qm.sendOk("Hey! I need #b10,000 mesos#k to do your pet's evolution!");
                            qm.dispose();
                            return;
                        }
                    
			qm.sendNextPrev("#e#bHey, you did it!#n#k \r\n#rWow!#k Now I could complete my studies on your pet!");
		} else if (status == 1) {
			if (mode == 0) {
				qm.sendOk("I see... Come back when you wish to do it. I'm really excited to do this.");
				qm.dispose();
			} else {
				qm.sendNextPrev("Just saying, your new dragon's color is gonna be #e#rrandom#k#n! It's either gonna be #ggreen, #bblue, #rred, #dor very rarely#k, black. \r\n\r\n#fUI/UIWindow.img/QuestIcon/5/0# \r\n\r If you happen to not like your pet's new color, or if you ever wish to change your pet color again, #eyou can change it!#n Simply just #dbuy another Rock of Evolution, 10,000 mesos, #kand #dequip your new pet#k before talking to me again, but of course, I cannot return your pet as a baby dragon, only to another adult dragon.");
			}
		} else if (status == 2) {
			qm.sendYesNo("Now let me try to evolve your pet. You ready? Wanna see your cute baby dragon turn into either a matured dark black, blue, calm green, or fiery red adult dragon? It'll still have the same closeness, level, name, fullness, hunger, and equipment in case you're worried. \r\n\r #b#eDo you wish to continue or do you have some last-minute things to do first?#k#n");
                } else if (status == 3) {
			qm.sendNextPrev("Alright, here we go...! #rHYAHH!#k");
		} else if (status == 4) {
			var rand = 1 + Math.floor(Math.random() * 10);
			var after = 0;
                        var i = 0;
                        
                        for(i = 0; i < 3; i++) {
                            if(qm.getPlayer().getPet(i) != null && qm.getPlayer().getPet(i).getItemId() == 5000029) {
                                var pet = qm.getPlayer().getPet(i);
                                break;
                            }
                        }
                        if(i == 3) {
                            qm.getPlayer().message("Pet could not be evolved.");
                            qm.dispose();
                            return;
                        }
                        
                        
			if (rand >= 1 && rand <= 3) {
				after = 5000030;
			} else if (rand >= 4 && rand <= 6) {
				after = 5000031;
			} else if (rand >= 7 && rand <= 9) {
				after = 5000032;
			} else if (rand == 10) {
				after = 5000033;
			} else {
				qm.sendOk("Something wrong. Try again.");
				qm.dispose();
                                return;
			}
                        
                        /* if (name.equals(MapleItemInformationProvider.getInstance().getName(id))) {
					name = MapleItemInformationProvider.getInstance().getName(after);
			} */
			
                        //qm.unequipPet(qm.getClient());
			qm.gainItem(5380000, -1);
                        qm.gainMeso(-10000);
                        qm.evolvePet(i, after);
			
                        //SpawnPetHandler.evolve(qm.getPlayer().getClient(), 5000029, after);
                        
                        qm.sendOk("#bSWEET! IT WORKED!#k Your dragon has grown beautifully! #rYou may find your new pet under your 'CASH' inventory.\r #kIt used to be a #b #i5000029##t5000029##k, and now it's \r a #b#i" + after + "##t" + after + "##k!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v"+after+"# #t"+after+"#");
			qm.dispose();
		}
	}
}