/**
 * @author: Eric
 * @npc: Sgt. Anderson
 * @maps: Ludibrium PQ Maps
 * @func: Ludi PQ (Warps you out)
*/

var status = -1;

function start() {
    if (cm.getMapId() != 922010000 && cm.getMapId() != 922010800) {
		cm.sendYesNo("You'll have to start over from scratch if you want to take a crack at this quest after leaving this stage. Are you sure you want to leave this map?");
    } else if (cm.getMapId() == 922010800) {
		cm.sendSimple("Do you need some help?#b\r\n#L0#I need Platform Puppet.#l\r\n#L1#I want to get out of here.#l#k");
	} else {
		cm.removeAll(4001022); // pass of dimension
	    cm.removeAll(4001023);
		cm.removeAll(4001454); // platform puppet
		cm.warp(221024500, 0);
		cm.dispose();
    }
}

function action(mode, type, selection) {
	if (mode == 1)
		status++;
	else if (mode == 0 && (status == 0 || status == -1)) {
		cm.sendNext("I see. Gather up the strength of your party members and try harder!");
		cm.dispose();
		return;
	} else
		status--;
	if (status == 0) {
		if (cm.getMapId() == 922010800) {
			if (selection == 0) {
				cm.sendNext("You have received a Platform Puppet. If you place it on the platform, it will have the same effect as someone standing there.\r\nRemember, though, this is an item that can only be used in here.");
				cm.gainItem(4001454, 1);
				cm.dispose();
			} else {
				cm.sendYesNo("You'll have to start over from scratch if you want to take a crack at this quest after leaving this stage. Are you sure you want to leave this map?");
			}
		} else {
			var eim = cm.getPlayer().getEventInstance();
			if(eim != null) {
				eim.removePlayer(cm.getPlayer());
			} else {
				cm.warp(922010000, 0);
			}
			cm.dispose();
		}
	} else if (status == 1) {
		var eim = cm.getPlayer().getEventInstance();
		if(eim != null) {
			eim.removePlayer(cm.getPlayer());
		} else {
			cm.warp(922010000, 0);
		}
		cm.dispose();
	}
}