importPackage(Packages.tools);

function end(mode, type, selection) {
    var rnd;

    if (mode != 1) {
        qm.dispose();
    } else {
	if(qm.haveItem(4031092, 10)) {
		if(qm.canHold(4031092)) {
			qm.sendOk("Well done! You brought back all the #t4031092# that were missing. Here, get this scroll as a token of my gratitude...");
			qm.gainItem(4031092, -10);

			rnd = Math.floor(Math.random() * 4);
			if(rnd == 0) qm.gainItem(2040704, 1);
			else if(rnd == 1) qm.gainItem(2040705, 1);
			else if(rnd == 2) qm.gainItem(2040707, 1);
			else qm.gainItem(2040708, 1);

			qm.gainExp(2700 * qm.getPlayer().getExpRate());
			qm.forceCompleteQuest();
		}
		else {
			qm.sendOk("Free a space on your USE inventory before receiving your prize.");
		}
	}
	
        qm.dispose();
    }
}