var status = -1;

function start(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		qm.sendAcceptDecline("Your #bDad#k forgot his Lunch Box when he left for the farm this morning. Will you #bdeliver this Lunch Box#k to your Dad in #b#m100030300##k, honey?");
	} else if (status == 1) {
		if (mode == 0 && type == 15) {//decline
			qm.sendNext("Good kids listen to their mothers. Now, Evan, be a good kid and talk to me again.");
			qm.dispose();
		} else {
			if (!qm.isQuestStarted(22003)) {
				if (!qm.haveItem(4032448)) {
					qm.gainItem(4032448, true);
				}
				qm.forceStartQuest();
			}
			qm.sendNext("Heehee, my Evan is such a good kid! Head #bleft after you exit the house#k. Rush over to your dad. I'm sure he's starving.");
		}
	} else if (status == 2) {
		qm.sendNextPrev("Come back to me if you happen to lose the Lunch Box. I'll make his lunch again.");
	} else if (status == 3) {
		qm.sendImage("UI/tutorial/evan/5/0");
		qm.dispose();
	}
}