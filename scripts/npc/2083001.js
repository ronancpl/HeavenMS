/**
 * @author: Ronan
 * @npc: Mark of the Squad
 * @map: Cave of Life - Cave Entrance (240050000)
 * @func: Horntail PQ
*/

var status = 0;
var price = 100000;
var em = null;
var hasPass;

function isRecruitingMap(mapid) {
        return mapid == 240050000;
}

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
                
                if(isRecruitingMap(cm.getMapId())) {
                        if (status == 0) {
                                em = cm.getEventManager("HorntailPQ");
                                if(em == null) {
                                        cm.sendOk("The Horntail PQ has encountered an error.");
                                        cm.dispose();
                                        return;
                                }

                                cm.sendSimple("#e#b<Party Quest: Horntail Trial Grounds>\r\n#k#n" + em.getProperty("party") + "\r\n\r\nThis is the path to Horntail's lair. If you want to face him, you and your team shall be tested on the trial grounds ahead.#b\r\n#L0#Let us pass to the trial grounds.\r\n#L1#I would like to hear more details.");
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
                                                        cm.sendOk("Either I cannot accept some members of your party inside the cave or you team is lacking. Solve this problem then talk to me!");
                                                }

                                                cm.dispose();
                                        }
                                } else {
                                        cm.sendOk("#e#b<Party Quest: Horntail Trial Grounds>#k#n\r\nAs the gatekeeper of Horntail's lair, I will grant access #bjust to those worthy#k of his presence. Even for those people, the path inside is that of a maze, full of branches and trials. However, those #radept at fighting squad bosses#k have a better chance to stand to our leader, although those #rof our kind#k have a shabby chance as well.");
                                        cm.dispose();
                                }
                        }
                } else {
                        if(!cm.isEventLeader()) {
                                cm.sendOk("Only your event leader is allowed to interact with the Schedule.");
                        } else if(cm.getMapId() == 240050100) {
                                if(cm.haveItem(4001087) && cm.haveItem(4001088) && cm.haveItem(4001089) && cm.haveItem(4001090) && cm.haveItem(4001091)) {
                                        cm.gainItem(4001087, -1);
                                        cm.gainItem(4001088, -1);
                                        cm.gainItem(4001089, -1);
                                        cm.gainItem(4001090, -1);
                                        cm.gainItem(4001091, -1);
                                        
                                        cm.getEventInstance().warpEventTeam(240050200);
                                } else {
                                        cm.sendOk("You don't have all the keys needed to proceed.");
                                }
                        } else if(cm.getMapId() == 240050300) {
                                if(cm.haveItem(4001092, 1) && cm.haveItem(4001093, 6)) {
                                        cm.gainItem(4001092, -1);
                                        cm.gainItem(4001093, -6);
                                        cm.getEventInstance().clearPQ();
                                } else {
                                        cm.sendOk("Check if you have got all 6 Red keys and 1 Blue key with you.");
                                }
                        } else if(cm.getMapId() == 240050310) {
                                if(cm.haveItem(4001092, 1) && cm.haveItem(4001093, 6)) {
                                        cm.gainItem(4001092, -1);
                                        cm.gainItem(4001093, -6);
                                        cm.getEventInstance().clearPQ();
                                } else {
                                        cm.sendOk("Check if you have got all 6 Red keys and 1 Blue key with you.");
                                }
                        }
                        
                        cm.dispose();
                }
        }
}