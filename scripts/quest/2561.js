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
		qm.sendNext("Ook, ook! Oook! Ook! Ook! Ook?!");
	} else if (status == 1) {
		qm.sendNextPrev("I remember...I was on my way to Maple Island, to become an Explorer... What happened? What's going on?", 2);
	} else if (status == 2) {
		qm.sendNextPrev("Oook! Ook! Ook! Oooook!");
	} else if (status == 3) {
		qm.sendNextPrev("I was talking to the captain, and admiring the scenery, and... Balrog! Balrog attacked the ship! So... Did I fall overboard? Then, why am I alive? I know #bI can swim#k, but can I swim while unconscious? Maybe I can. Maybe I'm a natural swimmer!", 2);
	} else if (status == 4) {
		qm.sendNext("Ooook! Ook! Ook! (Huh, a little monkey...tapping its foot angrily. Actually, when I first woke up, that monkey was the only thing I saw...)");
	} else if (status == 5) {
		qm.sendNextPrev("Huh? Why are you waving your arms like that? Are you trying to tell me something? (The monkey took an apple out of the nearby chest. It looks delicious. But, what is he trying to tell you?)\r\n\r\n#i2010000#", 2);
	} else if (status == 6) {
		qm.sendAcceptDecline("Ook ook! Om nom nom! (The monkey looks frustrated that you don't understand him. He pretends to eat the apple. Wait, does he want YOU to eat it? That must be it! What a nice monkey.)");
	} else if (status == 7) {
		if (mode == 0) {//decline
			qm.sendNext("The thing is, I don't like apples... Sorry, but no thanks.", 2);
			qm.dispose();
		} else {
			if (!qm.isQuestStarted(2561)) {//seems that hp is not changed o.o
				qm.gainItem(2010000, true);
				qm.forceStartQuest();
			}
			qm.sendNext("(You have received a delicious-looking apple. You should eat it. Now...how do you open your Inventory? Was it the #bI#k key...?)", 2);
		}
	} else if (status == 8) {
		qm.showInfo("UI/tutorial.img/28");
		qm.dispose();
	}
}
	