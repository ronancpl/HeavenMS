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
/* Delli
	Looking for Delli 3 (925010200)
	Hypnotize skill quest NPC.
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
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if (status == 0) {
                        if (cm.getMapId() != 925010400) {
                                em = cm.getEventManager("DelliBattle");
                                if(em == null) {
                                        cm.sendOk("The Delli Battle has encountered an error.");
                                        cm.dispose();
                                        return;
                                } else if(cm.isUsingOldPqNpcStyle()) {
                                        action(1, 0, 0);
                                        return;
                                }

                                cm.sendSimple("#e#b<Party Quest: Save Delli>\r\n#k#n" + em.getProperty("party") + "\r\n\r\nAh, #r#p1095000##k sent you here? Is she worried about me? ... I'm terribly sorry to hear that, but I can't really go back just yet, some monsters are under the Black Mage's influence, and it's up to me to liberate them! ... It seems you're not going to accept that either, huh? Would you like to collaborate with party members to help me? If so, please have your #bparty leader#k talk to me.#b\r\n#L0#I want to participate in the party quest.\r\n#L1#I want to find party members.\r\n#L2#I would like to hear more details.");
                        } else {
                                cm.sendYesNo("The mission succeeded, thanks for escorting me! I can lead you to #b#m120000104##k, are you ready?");
                        }
                } else if (status == 1) {
                        if (cm.getMapId() != 925010400) {
                                if (selection == 0) {
                                        if (cm.getParty() == null) {
                                                cm.sendOk("You can participate in the party quest only if you are in a party.");
                                                cm.dispose();
                                        } else if(!cm.isLeader()) {
                                                cm.sendOk("Your party leader must talk to me to start this party quest.");
                                                cm.dispose();
                                        } else {
                                                var eli = em.getEligibleParty(cm.getParty());
                                                if(eli.size() > 0) {
                                                        if(!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                                                                cm.sendOk("Another party has already entered the #rParty Quest#k in this channel. Please try another channel, or wait for the current party to finish.");
                                                        }
                                                }
                                                else {
                                                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. If you're having trouble finding party members, try Party Search.");
                                                }

                                                cm.dispose();
                                        }
                                } else if (selection == 1) {
                                        cm.sendOk("Try using a Super Megaphone or asking your buddies or guild to join!");
                                        cm.dispose();
                                } else {
                                        cm.sendOk("#e#b<Party Quest: Save Delli>#k#n\r\n A ambush is under way! I must stand on the field for around 6 minutes to complete the liberation, please protect me during that time so that my mission is completed.");
                                        cm.dispose();
                                }
                        } else {
                                cm.warp(120000104);
                                cm.dispose();
                        }
                }
        }
}