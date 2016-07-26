var status = -1;

function start(mode, type, selection) {
	if(mode == 0) {
		qm.dispose();
		return;
	}
        status++;

	if(status == 0) {
		if(!qm.canHold(4220046)) {
			qm.sendNext("You need a slot on your ETC inventory to receive the egg to hatch.");
			qm.dispose();
			return;
		}

                qm.gainItem(4220046, 1);
		qm.sendNext("Go bring me 30 #b#i4031992# #t4031992# #kASAP!");
                qm.startQuest();
	}

        qm.dispose();
}

function end(mode, type, selection) {
	if(mode == 0) {
		qm.dispose();
		return;
	}
        status++;

	if(status == 0) {
		qm.sendNext("Alright, it seems you brought the items I needed. Great job!");
	}

	else if(status == 1) {
	    qm.gainFame(11);
            qm.gainItem(4220046, -1);
	    qm.gainItem(4031992, -30);
            
            qm.completeQuest();
            qm.dispose();
	}
}