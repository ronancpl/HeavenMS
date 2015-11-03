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
		qm.sendAcceptDecline("Wouldn't a lizard enjoy a #b#t4032452##k, like a cow? There are a lot of #bHaystacks#k nearby, so try feeding it that.");
	} else if (status == 1) {
		if (mode == 0) {
			qm.sendNext("Hm, you never know unless you try. That lizard is big enough to be on Maple's Believe It Or Not. It might eat hay.");
		} else {
			qm.forceStartQuest();
			qm.sendImage("UI/tutorial/evan/12/0");
		}
		qm.dispose();		
	}
}