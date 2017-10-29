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
        qm.sendNext("There isn't much record left of the heroes that fought against the Black Mage. Even in the Book of Prophecy, the only information available is that there were five of them. There is nothing about who they were or what they looked like. Is there anything you remember? Anything at all?", 8);
    } else if (status == 1) {
		qm.sendNextPrev("I don't remember a thing...", 2);
	} else if (status == 2) {
		qm.sendNextPrev("As I expected. Of course, the curse of the Black Mage was strong enough to wipe out all of your memory. But even if that's the case, there has got to be a point where the past will uncover, especially now that we are certain you are one of the heroes. I know you've lost your armor and weapon during the battle but... Oh, yes, yes. I almost forgot! Your #bweapon#k!", 8);
	} else if (status == 3) {
		qm.sendNextPrev("My weapon?", 2);
	} else if (status == 4) {
		qm.sendNextPrev("I found an incredible weapon while digging through blocks of ice a while back. I figured the weapon belonged to a hero, so I brought it to town and placed it somewhere in the center of the town. Haven't you seen it? #bThe #p1201001##k... \r\r#i4032372#\r\rIt looks like this...", 8);
	} else if (status == 5) {
		qm.sendNextPrev("Come to think of it, I did see a #p1201001# in town.", 2);
	} else if (status == 6) {
		qm.sendAcceptDecline("Yes, that's it. According to what's been recorded, the weapon of a hero will recognize its rightful owner, and if you're the hero that used the #p1201001#, the #p1201001# will react when you grab the #p1201001#. Please go find the #b#p1201001# and click on it.#k");
	} else if (status == 7) {
		if (mode == 0 && type == 15) {
			qm.sendNext("What's stopping you? I promise, I won't be disappointed even if the #p1201001# shows no reaction to you. Please, rush over there and grab the #p1201001#. Just #bclick#k on it.", 8);
		} else {
			qm.forceCompleteQuest();
			qm.sendOk("If the #p1201001# reacts to you, then we'll know that you're #bAran#k, the hero that wielded a #p1201001#.", 8);
			qm.showIntro("Effect/Direction1.img/aranTutorial/ClickPoleArm");
		}
		qm.dispose();
	}
}