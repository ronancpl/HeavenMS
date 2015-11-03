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
		qm.sendNext("Did you sleep well, Evan?");
	} else if (status == 1) {
		qm.sendNextPrev("#bYes, what about you, Mom?", 2);
	} else if (status == 2) {
		qm.sendNextPrev("I did as well, but you seem so tired. Are you sure you slept okay? Did the thunder and lightning last night keep you up?");
	} else if (status == 3) {
		qm.sendNextPrev("#bOh, no. It's not that, Mom. I just had a strange dream last night.", 2);
	} else if (status == 4) {
		qm.sendNextPrev("A strange dream? What kind of strange dream?");
	} else if (status == 5) {
		qm.sendNextPrev("#bWell...", 2);
	} else if (status == 6) {
		qm.sendNextPrev("#b(You explain that you met a dragon in your dream.)", 2);
	} else if (status == 7) {
		qm.sendAcceptDecline("Hahaha, a dragon? That's incredible. I'm glad he didn't swallow you whole! You should tell #p1013101# about your dream. I'm sure he'll enjoy it.");
	} else if (status == 8) {
		if (mode == 0) {//decline
			qm.sendNext("Hm? Don't you want to tell #p1013101#? You have to be nice to your brother, dear.");//guess
			qm.dispose();//get the message xd
		} else {//accept
			qm.forceStartQuest();
			qm.sendNext("#b#p1013101##k went to the #b#m100030102##k to feed the Bull Dog. You'll see him right outside.");
		}
	} else if (status == 9) {
		qm.sendImage("UI/tutorial/evan/1/0");
		qm.dispose();
	}
}

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
		qm.sendNext("Hey, Evan. You up? What's with the dark circles under your eyes? Didn't sleep well? Huh? A strange dream? What was it about? Whoa? A dream about a dragon?");
	} else if (status == 1) {
		qm.sendNextPrev("Muahahahahaha, a dragon? Are you serious? I don't know how to interpret dreams, but that sounds like a good one! Did you see a dog in your dream, too? Hahaha! \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 20 exp");
	} else if (status == 2) {
		qm.forceCompleteQuest();
		qm.gainExp(20);
		qm.sendImage("UI/tutorial/evan/2/0");
		qm.dispose();
	}	
}
	