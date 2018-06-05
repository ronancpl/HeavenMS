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

/**
 * @author: Ronan
 * @npc: Wonky
 * @map: 200080101 - Orbis - The Unknown Tower
 * @func: Orbis PQ
*/

var status = 0;
var em = null;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && status == 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;

                if(cm.getMapId() == 200080101) {
                        if (status == 0) {
                                em = cm.getEventManager("OrbisPQ");
                                if(em == null) {
                                        cm.sendOk("The Orbis PQ has encountered an error.");
                                        cm.dispose();
                                        return;
                                } else if(cm.isUsingOldPqNpcStyle()) {
                                        action(1, 0, 0);
                                        return;
                                }

                                cm.sendSimple("#e#b<Party Quest: Tower of Goddess>\r\n#k#n" + em.getProperty("party") + "\r\n\r\nWould you like to assemble or join a team to solve the puzzles of the #bTower of Goddess#k? Have your #bparty leader#k talk to me or make yourself a party.#b\r\n#L0#I want to participate in the party quest.\r\n#L1#I want to find party members.\r\n#L2#I would like to hear more details.\r\n#L3#I would like to reclaim a prize.");
                        } else if (status == 1) {
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
                                } else if (selection == 2) {
                                        cm.sendOk("#e#b<Party Quest: Tower of Goddess>#k#n\r\nOur goddess has been missing since some time ago, rumor has it She has been seen last time inside the Tower of Goddess. Furthermore, our sanctuary has been seized by the overwhelming forces of the pixies, those beings that are recently wandering at the outskirts of Orbis. Their leader, Papa Pixie, currently holds the throne and may know Her whereabouts, so we urge to find a composition of brave heroes to charge into and claim back our sanctuary and rescue Her. If your team is able to be a composite of every job niche available (Warrior, Magician, Bowman, Thief and Pirate), you guys will receive my blessings to aid you in battle. Will you aid us?\r\n");
                                        cm.dispose();
                                }
                                else {
                                        cm.sendSimple("So, what prize do you want to obtain?\r\n#b#L0#Give me Goddess Wristband.\r\n");
                                }
                        } else if (status == 2) {
                                if (selection == 0) {
                                        if (!cm.haveItem(1082232) && cm.haveItem(4001158, 10)) {
                                                cm.gainItem(1082232, 1);
                                                cm.gainItem(4001158, -10);
                                                cm.dispose();
                                        } else {
                                                cm.sendOk("You either have Goddess Wristband already or you do not have 10 #t4001158#.");
                                                cm.dispose();
                                        }
                                }
                        }
                } else {
                        if(status == 0) {
                                cm.sendYesNo("Are you going to drop out from this rescue mission?");
                        } else if(status == 1) {
                                cm.warp(920011200);
                                cm.dispose();
                        }
                }
        }
}