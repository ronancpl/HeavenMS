var status = 0;
var section = 0;
importPackage(java.lang);
//questid 29931, infoquest 7662

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (status == 1) {
	if (cm.getMapId() == 910320001) {
		cm.warp(910320000, 0);
		cm.dispose();
	} else if (cm.getMapId() == 910330001) {
		var itemid = 4001321;
		if (!cm.canHold(itemid)) {
			cm.sendOk("Please make room for 1 ETC slot.");
		} else {
			cm.gainItem(itemid,1);
			cm.warp(910320000, 0);
		}
		cm.dispose();
	} else if (cm.getMapId() >= 910320100 && cm.getMapId() <= 910320304) {
		cm.sendYesNo("Would you like to exit this place?");
		status = 99;
	} else {
		cm.sendSimple("My name is Mr.Lim.\r\n#b#e#L1#Enter the Dusty Platform.#l#n\r\n#L2#Head towards Train 999.#l\r\n#L3#Receive a medal of <Honorary Employee>.#l#k");
	}
    } else if (status == 2) {
		section = selection;
		if (selection == 1) {
			if (cm.getPlayer().getLevel() < 25 || cm.getPlayer().getLevel() > 30 || !cm.isLeader()) {
				cm.sendOk("You must be in the Level Range 25-30 and be the party leader.");
			} else {
				if (!cm.start_PyramidSubway(-1)) {
					cm.sendOk("The Dusty Platform is currently full at the moment.");
				}
			}
			//todo
		} else if (selection == 2) {
			if (cm.haveItem(4001321)) {
				if (cm.bonus_PyramidSubway(-1)) {
					cm.gainItem(4001321, -1);
				} else {
					cm.sendOk("The Train 999 is currently full at the moment");
				}
			} else {
				cm.sendOk("You do not have the Boarding Pass.");
			}
		} else if (selection == 3) {
			var record = cm.getQuestRecord(7662);
			var data = record.getCustomData();
			if (data == null) {
				record.setCustomData("0");
				data = record.getCustomData();
			}
			var mons = parseInt(data);
			if (mons < 10000) {
				cm.sendOk("Please defeat at least 10,000 monsters in the Station and look for me again. Kills : " + mons);
			} else if (cm.canHold(1142141) && !cm.haveItem(1142141)){
				cm.gainItem(1142141,1);
				cm.startQuest(29931);
				cm.completeQuest(29931);
			} else {
				cm.sendOk("Please make room.");
			}
		}
		cm.dispose();
	} else if (status == 100) {
		cm.warp(910320000,0);
		cm.dispose();
	}
}