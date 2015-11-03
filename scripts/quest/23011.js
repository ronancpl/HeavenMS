var status = -1;

function end(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		qm.sendYesNo("So you've finally decided to become a Battle Mage, eh? Well, you can still change your mind. Just stop our conversation, forfeit this quest, and talk to another class trainer. So, you sure you want to become a Battle Mage? I'm not interested in teaching you unless you're a hundred percent sure...");
	} else if (status == 1) {
		if (mode == 0) {
			qm.sendNext("Think carefully before you make your decision.");
		} else {
			if (!qm.isQuestCompleted(23011)) {
				qm.gainItem(1382100);
				qm.gainItem(1142242);
				qm.forceCompleteQuest();
				qm.changeJobById(3200);
				qm.showItemGain(1382100, 1142242);
			}
			qm.sendNext("Okay, okay. Welcome to the Resistance, kid. From now on, you will play the role of a Battle Mage, a fierce Magician always ready to lead your party into battle.");
		}
	} else if (status == 2) {
		qm.sendNextPrev("But don't go spreading it around that you're a Battle Mage, hm? No need to tempt the Black Wings to come after you. From now on, I'll be your teacher. If anyone asks, you're visiting me just as a regular student, not as a member of the Resistance. I'll give you special lessons now and then. You better not fall asleep in class, hear? ");
	} else if (status == 3) {
		qm.dispose();
	}
}