/**
 * @author: Eric
 * @npc: Red Sign
 * @map: 101st Floor Eos Tower (221024500)
 * @func: Ludi PQ
*/

var status = 0;
var minLevel = 35; // according to Nexon it's 30, but it's actually a 50 requirement.
var maxLevel = 200;
var minPartySize = 5;
var maxPartySize = 6;

var brokenGlassesCount = 0; // code custom quest data is on the todo list

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else if (mode == 0) {
		if (status == 1) {
			cm.sendNext((cm.getParty() == null ? "Remember that using the Party Search function (Hotkey O) will allow you to find a party anytime, anywhere." : "Send an invite to friends nearby. Remember that using the Party Search function (Hotkey O) will allow you to find a party anytime, anywhere."));
			cm.dispose();
			return;
		} else {
			status--;
		}
	}
	if (status == 0) {
		cm.sendSimple("#e<Party Quest: Dimensional Schism>#n\r\n\r\nYou can't go any higher because of the extremely dangerous creatures above. Would you like to collaborate with party members to complete the quest? If so, please have your #bparty leader#k talk to me.#b\r\n#L0#I want to participate in the party quest.\r\n#L1#I want to find party members.\r\n#L2#I want to receive the Broken Glasses.\r\n#L3#I would like to hear more details.");
	} else if (status == 1) {
		if (selection == 0) {
			if (cm.getParty() == null) {
				cm.sendOk("You can participate in the party quest only if you are in a party.");
				cm.dispose();
				return;
			} else if (!cm.getPlayer().isGM() && (cm.getParty().getMembers().size() < minPartySize || !cm.isLeader())) {
				cm.sendOk("You cannot participate in the quest, because you do not have at least 3 party members.");
				cm.dispose();
				return;
			} else {
				// Check if all party members are within PQ levels
				var party = cm.getParty().getMembers();
				var mapId = cm.getMapId();
				var next = true;
				var levelValid = 0;
				var inMap = 0;
				var it = party.iterator();

				while (it.hasNext()) {
					var cPlayer = it.next();
					if ((cPlayer.getLevel() >= minLevel) && (cPlayer.getLevel() <= maxLevel)) {
						levelValid += 1;
					} else {
						next = false;
					}
					if (cPlayer.getMapId() == mapId) {
						inMap += (cPlayer.getJobId() == 910 ? 6 : 1);
					}
				}
				if (party.size() > maxPartySize || inMap < minPartySize) {
					next = false;
				}
				if(cm.getPlayer().isGM())
					next = true;
				if (next) {
					var em = cm.getEventManager("LudiPQ");
					if (em == null) {
						cm.sendOk("The Ludibrium PQ has encountered an error. Please report this on the forums, and with a screenshot.");
					} else {
						var prop = em.getProperty("LPQOpen");
						if (prop == null || prop.equals("true")) { 
							em.startInstance(cm.getParty(), cm.getPlayer().getMap());
							cm.removeAll(4001022);
							cm.removeAll(4001023);
							cm.dispose();
							return;
						} else {
							cm.sendOk("Another party has already entered the #rParty Quest#k in this channel. Please try another channel, or wait for the current party to finish.");
						}
					}
				} else {
					cm.sendYesNo("You cannot participate in the quest, because you do not have at least 3 party members. If you're having trouble finding party members, try Party Search.");
				}
			}
		} else if (selection == 1) {
			cm.sendOk("Try using a Super Megaphone or asking your buddies or guild to join!");
			cm.dispose();
		} else if (selection == 2) { // todo
			cm.sendNext("I am offering 1 #i1022073:# #bBroken Glasses#k for every 20 times you help me. If you help me #b" + brokenGlassesCount + " more times, you can receive Broken Glasses.#k");
			cm.dispose();
		} else {
			cm.sendOk("#e<Party Quest: Dimensional Schism>#n\r\nA Dimensional Schism has appeared in #b#m220000000#!#k We desperately need brave adventurers who can defeat the intruding monsters. Please, party with some dependable allies to save #m220000000#! You must pass through various stages by defeating monsters and solving quizzes, and ultimately defeat #r#o9300012##k.\r\n - #eLevel#n: 30 or above #r(Recommended Level: 60 ~ 69)#k\r\n - #eTime Limit#n: 20 min\r\n - #eNumber of Players#n: 3 to 6\r\n - #eReward#n: #i1022073:# Broken Glasses #b(obtained every 20 time(s) you participate)#k\r\n                      Various Use, Etc, and Equip items");
			cm.dispose();
		}
	} else if (status == 2) {
		if (mode > 0) {
			//cm.findParty();
		}
		cm.dispose();
	}
}