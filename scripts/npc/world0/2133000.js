var status = -1;
var minPlayers = 0;

var minLevel = 44, maxLevel = 170;

function start() {
    action(1,0,0);
}

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
	    cm.dispose();
	}
	status--;
    }
    if (status == 0) {
	cm.sendSimple("#b#L0#Give me Altaire Earrings.#l\r\n#L1#Give me Glittering Altaire Earrings.#l\r\n#L3#Give me Brilliant Altaire Earrings.#l\r\n#L2#Attempt Forest of Poison Haze.#l#k");
    } else if (status == 1) {
	if (selection == 0) {
	    if (!cm.haveItem(1032060) && cm.haveItem(4001198, 10)) {
		cm.gainItem(1032060,1);
		cm.gainItem(4001198, -10);
	    } else {
		cm.sendOk("You either have Altair Earrings already or you do not have 10 Altair Fragments");
	    }
	} else if (selection == 1){
	    if (cm.haveItem(1032060) && !cm.haveItem(1032061) && cm.haveItem(4001198, 10)) {
		cm.gainItem(1032060,-1);
		cm.gainItem(1032061, 1);
		cm.gainItem(4001198, -10);
	    } else {
		cm.sendOk("You either don't have Altair Earrings already or you do not have 10 Altair Fragments");
	    }
	} else if (selection == 1){
	    if (cm.haveItem(1032061) && !cm.haveItem(1032101) && cm.haveItem(4001198, 10)) {
		cm.gainItem(1032061,-1);
		cm.gainItem(1032101, 1);
		cm.gainItem(4001198, -10);
	    } else {
		cm.sendOk("You either don't have Glittering Altair Earrings already or you do not have 10 Altair Fragments");
	    }
	} else if (selection == 2) {
	    if (cm.getPlayer().getParty() == null || !cm.isLeader()) {
		cm.sendOk("The leader of the party must be here.");
	    } else {
		var party = cm.getPlayer().getParty().getMembers();
		var mapId = cm.getPlayer().getMapId();
		var next = true;
		var size = 0;
		var it = party.iterator();
		while (it.hasNext()) {
			var cPlayer = it.next();
			var ccPlayer = cm.getPlayer().getMap().getCharacterById(cPlayer.getId());
			if (ccPlayer == null || ccPlayer.getLevel() < minLevel || ccPlayer.getLevel() > maxLevel) {
				next = false;
				break;
			}
			size += (ccPlayer.isGM() ? 4 : 1);
		}	
		if (next && size >= minPlayers) {
			var em = cm.getEventManager("Ellin");
			if (em == null) {
				cm.sendOk("Please try again later.");
			} else {
				em.startInstance(cm.getPlayer().getParty(), cm.getPlayer().getMap(), 1);    //common level only
			}
		} else {
			cm.sendOk("All 2+ members of your party must be here and between level " + minLevel + " and " + maxLevel + ".");
		}
	    }
	}
	cm.dispose();
    }
}