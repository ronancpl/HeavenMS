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
		qm.sendNext("Yo, master. Now that I've shown you what I can do, it's your turn. Prove to me...that you can find food! I'm starving. You can use my power now, so you have to take care of me.");
	} else if (status == 1) {
		qm.sendNextPrev("Eh, I still don't get what's going on, but I can't let a poor little critter like you starve, right? Food, you say? What do you want to eat?", 2);
	} else if (status == 2) {
		qm.sendNextPrev("Hi, I was just born a few minutes ago. How would I know what I eat? All I know is that I'm a Dragon... I'm YOUR Dragon. And you're my master. You have to treat me well!");
	} else if (status == 3) {
		qm.sendAcceptDecline("I guess we're supposed to learn together. But I'm hungry. Master, I want food. Remember, I'm a baby! I'll start crying soon!");
	} else if (status == 4) {
		if (mode == 0) {
			qm.sendNext("*gasp* How can you refuse to feed your Dragon? This is child abuse! ");
			qm.dispose();
		} else {
			qm.forceStartQuest();
			qm.sendOk("#b#b(#p1013000# the baby Dragon appears to be extremely hungry. You must feed him. Maybe your Dad can give you advice on what dragons eat.)");
			qm.dispose();
		}
	}
}