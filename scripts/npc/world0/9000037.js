/**
 * @author: Ronan
 * @npc: Agent Meow
 * @map: 970030000 - Hidden Street - Exclusive Training Center
 * @func: Boss Rush PQ
*/

var status = 0;
var minLevel = 1;
var maxLevel = 200;
var minPartySize = 1;
var maxPartySize = 6;
var state;

function start() {
	status = -1;
        state = (cm.getMapId() >= 970030001 && cm.getMapId() <= 970042711) ? 1 : 0;
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

                if (status == 0) {
                        if(state) {
                                cm.sendYesNo("Do you wish to abandon this event?");
                        }
                        else {
                                cm.sendSimple("#b<Party Quest: Boss Rush>#k\r\n\r\nWould you like to collaborate with party members to complete the expedition, or are you brave enough to take it on all by yourself? Have your #bparty leader#k talk to me or make yourself a party.#b\r\n#L0#I want to participate in the party quest.\r\n#L1#I want to find party members.\r\n#L2#I would like to hear more details.");
                        }
                } else if (status == 1) {
                        if(state) {
                                cm.warp(970030000);
                                cm.dispose();
                        }
                        else {
                                if (selection == 0) {
                                        if (cm.getParty() == null) {
                                                cm.sendOk("You can participate in the party quest only if you are in a party.");
                                                cm.dispose();
                                        } else if(!cm.isLeader()) {
                                                cm.sendOk("Your party leader must talk to me to start this party quest.");
                                                cm.dispose();
                                        } else {
                                                var em = cm.getEventManager("BossRushPQ");
                                                if(em == null) {
                                                        cm.sendOk("The Boss Rush PQ has encountered an error.");
                                                        cm.dispose();
                                                }

                                                var eli = em.getEligibleParty(cm.getParty());
                                                if(eli.size() > 0) {
                                                        var prop = em.getProperty("state");
                                                        if (prop != null && prop.equals("0")) { 
                                                                em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1);
                                                                cm.dispose();
                                                        } else {
                                                                cm.sendOk("Another party has already entered the #rParty Quest#k in this channel. Please try another channel, or wait for the current party to finish.");
                                                                cm.dispose();
                                                        }
                                                }
                                                else {
                                                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. If you're having trouble finding party members, try Party Search.");
                                                        cm.dispose();
                                                }
                                        }
                                } else if (selection == 1) {
                                        cm.sendOk("Try using a Super Megaphone or asking your buddies or guild to join!");
                                        cm.dispose();
                                } else {
                                        cm.sendOk("#b<Party Quest: Boss Rush PQ>#k\r\nBrave adventurers from all over the places travels here to test their skills and abilities in combat, as they face even more powerful bosses from MapleStory. Join forces with fellow adventurers or face all the burden by yourself and receive all the glory, it is up to you. REWARDS are given accordingly to how far the adventurers reach and extra prizes may are given to a random member of the party, all attributed at the end of an expedition.");
                                        cm.dispose();
                                }
                        }
                }
        }
}