/**
 * @author: Ronan
 * @npc: Juliet
 * @map: Magatia - Alcadno - Hidden Room (261000021)
 * @func: Magatia PQ (Alcadno)
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
                    
                if(cm.getMapId() != 261000021) {
                        if(status == 0) {
                                cm.sendYesNo("We must keep fighting to save Romeo, please keep your pace. If you are not feeling so well to continue, your companions and I will understand... So, are you going to retreat?");
                        } else if(status == 1) {
                                cm.warp(926110700);
                                cm.dispose();
                        }
                } else {
                        if (status == 0) {
                                em = cm.getEventManager("MagatiaPQ_A");
                                if(em == null) {
                                        cm.sendOk("The Magatia PQ (Alcadno) has encountered an error.");
                                        cm.dispose();
                                        return;
                                }

                                cm.sendSimple("#e#b<Party Quest: Romeo and Juliet>\r\n#k#n" + em.getProperty("party") + "\r\n\r\nMy beloved Romeo has been kidnapped! Although he is Zenumist's, I can't stand by and just see him suffer just because of this foolish clash. I need you and your colleagues help to save him! Please, help us!! Please have your #bparty leader#k talk to me.#b\r\n#L0#I want to participate in the party quest.\r\n#L1#I want to find party members.\r\n#L2#I would like to hear more details.");
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
                                } else {
                                        cm.sendOk("#e#b<Party Quest: Romeo and Juliet>#k#n\r\nNot long ago, a scientist named Yulete has been banished from this town because of his researches of combined alchemies of Alcadno's and Zenumist's. Because of the immensurable amount of power coming from this combination, it is forbidden by law to study both. Yet, he ignored this law and got hands in both researches. As a result, he has been exiled.\r\nHe is now retaliating, already took my beloved one and his next target is me, as we are big pictures of Magatia, successors of both societies. But I'm not afraid. We must recover him at all costs!\r\n");
                                        cm.dispose();
                                }
                        }
                }
        }
}