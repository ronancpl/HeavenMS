/**
 * @author: Ronan
 * @npc: Shuang
 * @map: Victoria Road: Excavation Site<Camp> (101030104)
 * @func: Start Guild PQ
*/

var status = 0;
var sel;
var em = null;

function findLobby(guild) {
        for (var iterator = em.getInstances().iterator(); iterator.hasNext();) {
                var lobby = iterator.next();
                
                if(lobby.getIntProperty("guild") == guild) {
                        if(lobby.getIntProperty("canJoin") == 1) return lobby;
                        else return null;
                }
        }
        
        return null;
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
                
                if (status == 0) {
                        em = cm.getEventManager("GuildQuest");
                        if(em == null) {
                                cm.sendOk("The Guild Quest has encountered an error.");
                                cm.dispose();
                                return;
                        }
                    
                        cm.sendSimple("#e#b<Guild Quest: Sharenian Ruins>\r\n#k#n" + em.getProperty("party") + "\r\n\r\nThe path to Sharenian starts here. What would you like to do? #b\r\n#L0#Register your guild for Guild Quest#l\r\n#L1#Join your guild's Guild Quest#l\r\n#L2#I would like to hear more details.#l");
                } else if (status == 1) {
                        sel = selection;
                        if (selection == 0) {
                                if(!cm.isGuildLeader()) {
                                        cm.sendOk("Your guild master/jr.master must talk to me to register this guild quest.");
                                        cm.dispose();
                                } else {
                                        if(em.isQueueFull()) {
                                                cm.sendOk("The queue on this channel is already full. Please be patient and try again after a while, or try on another channel.");
                                                cm.dispose();
                                        } else {
                                                var qsize = em.getQueueSize();
                                                cm.sendYesNo(((qsize > 0) ? "There is currently #r" + qsize + "#k guilds queued on. " : "") + "Do you wish for your guild to join this queue?");
                                        }
                                }
                        } else if (selection == 1) {
                                if(cm.getPlayer().getGuildId() > 0) {
                                        var eim = findLobby(cm.getPlayer().getGuildId());
                                        if(eim == null) {
                                                cm.sendOk("You don't have a guild registered and currently on strategy time to assist on this channel.");
                                        } else {
                                                if(cm.isLeader()) {
                                                        em.getEligibleParty(cm.getParty());
                                                        eim.registerParty(cm.getPlayer());
                                                } else {
                                                        eim.registerPlayer(cm.getPlayer());
                                                }
                                        }
                                } else {
                                        cm.sendOk("You can't participate in the guild quest if you don't pertain on a guild yourself!");
                                }
                                
                                cm.dispose();
                        } else {
                                cm.sendOk("#e#b<Guild Quest: Sharenian Ruins>#k#n\r\n Team up with your guild members in an auspicious attempt to recover the Rubian from the skeleton's grasp, with teamwork overcoming many puzzles and challenges awaiting inside the Sharenian tombs. Great rewards can be obtained upon the instance completion, and Guild Points can be racked up for your Guild.");
                                cm.dispose();
                        }
                } else if (status == 2) {
                        if (sel == 0) {
                                var entry = em.addGuildToQueue(cm.getPlayer().getGuildId(), cm.getPlayer().getId());
                                if(entry > 0) {
                                        if(entry == 1) {
                                                cm.sendOk("Your guild has been registered successfully. A message will pop on your chat keeping your guild aware about the registration status. Now, #rimportant#k: as the leader of this instance, #ryou must already be present on this channel#k the right moment your guild is called for the strategy time. #bThe missubmission of this action will void#k your guild registration as a whole, and the next guild will be called immediately. Must be noted also, that if you become absent from the end of the strategy time to any point on the duration of the instance, it will render the instance interrupted, and your guild will be moved out instantly, moving again the queue.");
                                        }
                                } else if(entry == 0) {
                                        cm.sendOk("The queue on this channel is already full. Please be patient and try again after a while, or try on another channel.");
                                } else {
                                        cm.sendOk("Your guild is already queued on a channel. Please wait for your guild's turn.");
                                }
                        }
                        
                        cm.dispose();
                }
        }
}