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
		qm.sendAcceptDecline("It's strange. The chickens are acting funny. They used to hatch way more #t4032451#s. Do you think the Foxes have something to do with it? If so, we better hurry up and do something.");
	} else if (status == 1) {
		if (mode == 0) {//decline
			qm.sendNext("Oh what... Are you scared of the #o9300385#es? Don't tell anyone you're related to me. That's shameful.");
			qm.dispose();
		} else {
			qm.forceStartQuest();
			qm.sendNext("Right? Let us go and defeat those Foxes. Go on ahead and defeat #r10 #o9300385#es#k in #b#m100030103##k first. I'll follow you and take care of what's left behind. Now, hurry over to #m100030103#!");
		}
	} else if (status == 2) {
		qm.sendImage("UI/tutorial/evan/10/0");
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
		qm.sendNext("Did you defeat the #o9300385#es?");
	} else if (status == 1) {
		qm.sendNextPrev("#bWhat happened to slaying the Foxes left behind?", 2);
	} else if (status == 2) {
		qm.sendNextPrev("Oh, that? Haha. I did chase them, sort of, but I wanted to make sure that they catch up to you. I wouldn't want you eaten by a #o9300385# or anything. So I just let them be.");
	} else if (status == 3) {
		qm.sendNextPrev("#bAre you sure you weren't just hiding because you were scared of the Foxes?", 2);
	} else if (status == 4) {
		qm.sendNextPrev("What? No way! Sheesh, I fear nothing!");
	} else if (status == 5) {
		qm.sendNextPrev("#bWatch out! There's a #o9300385# right behind you!", 2);
	} else if (status == 6) {
		qm.sendNextPrev("Eeeek! Mommy!");	
	} else if (status == 7) {
		qm.sendNextPrev("#b...", 2);	
	} else if (status == 8) {
		qm.sendNextPrev("...");	
	} else if (status == 9) {
		qm.sendNextPrev("You little brat! I'm your older brother. Don't you mess with me! Your brother has a weak heart, you know. Don't surprise me like that!");	
	} else if (status == 10) {
		qm.sendNextPrev("#b(This is why I don't want to call you Older Brother...)", 2);
	} else if (status == 11) {
		qm.sendNextPrev("Hmph! Anyway, I'm glad you were able to defeat the #o9300385#es. As a reward, I'll give you something an adventurer gave me a long time ago. Here you are. \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i1372043# 1 #t1372043# \r\n#i2022621# 25 #t2022621# \r\n#i2022622# 25 #t2022622#s \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 910 exp");
	} else if (status == 12) {
		if (!qm.isQuestCompleted(22008)) {
			qm.gainItem(1372043, true);
			qm.gainItem(2022621, 25, true);
			qm.gainItem(2022622, 25, true);
			qm.forceCompleteQuest();
			qm.gainExp(910);
		}
		qm.sendNextPrev("#bThis is a weapon that Magicians use. It's a Wand#k. You probably won't really need it, but it'll make you look important if you carry it around. Hahahahaha.");
	} else if (status == 13) {
		qm.sendPrev("Anyway, the Foxes have increased, right? How weird is that? Why are they growing day by day? We should really look into it and get to the bottom of this.");
		qm.dispose();
	}
}