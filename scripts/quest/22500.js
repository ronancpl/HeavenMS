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
		qm.sendNext("I'm finally here! *inhales* Ah, this must be air I'm breathing. And that, that must be the sun! And that, a tree! And that, a plant! And that, a flower! Woohahahaha! This is incredible! This is much better than I imagined the world to be while I was trapped inside the egg. And you... Are you my master? Hm, I pictured you differently.");
	} else if (status == 1) {
		qm.sendNextPrev("#bWhoooooa, it talks!", 2);
	} else if (status == 2) {
		qm.sendNextPrev("My master is strange. I guess I can't do anything about it now, since the pact has been made. *sigh* Well, good to meet you. We'll be seeing a lot of each other.");
	} else if (status == 3) {
		qm.sendNextPrev("#bEh? What do you mean? We'll be seeing a lot of each other? What pact?", 2);
	} else if (status == 4) {
		qm.sendNextPrev("What do you mean what do I mean?! You woke me from the Egg. You're my master! So of course it's your responsibility to take care of me and train me and help me become a strong Dragon. Obviously!");
	} else if (status == 5) {
		qm.sendNextPrev("#bWhaaat? A Dragon? You're a Dragon?! I don't get it... Why am I your master? What are you talking about?", 2);
	} else if (status == 6) {
		qm.sendNextPrev("What are YOU talking about? Your spirit made a pact with my spirit! We're pretty much the same person now. Do I really have to explain? As a result, you've become my master. We're bound by the pact. You can't change your mind... The pact cannot be broken.");		
	} else if (status == 7) {
		qm.sendNextPrev("#bWait, wait, wait. Let me get this straight. You're saying I have no choice but to help you?", 2);
	} else if (status == 8) {
		qm.sendNextPrev("Yuuup! Heeeey...! What's with the face? You...don't want to be my master?");
	} else if (status == 9) {
		qm.sendNextPrev("#bNo... It's not that... I just don't know if I'm ready for a pet.", 2);
	} else if (status == 10) {
		qm.sendNextPrev("A p-p-pet?! Did you just call me a pet?! How dare... Why, I'm a Dragon! The strongest being in the world!");
	} else if (status == 11) {
		qm.sendNextPrev("#b...#b(You stare at him skeptically. He looks like a lizard. A puny little one, at that.)#k", 2);
	} else if (status == 12) {
		qm.sendAcceptDecline("Why are you looking at me like that?! Just watch! See what I can do with my power. Ready?");
	} else if (status == 13) {
		if (mode == 0 && type == 15) {
			qm.sendNext("You don't believe me? Grrrrr, you're getting me mad!");
			qm.dispose();
		} else {
			if (!qm.isQuestStarted(22500)) {
				qm.forceStartQuest();
			}
			qm.sendNext("Command me to slay the #r#o1210100##ks! Do it now! I'll show you how fast a Dragon can defeat the #o1210100#s! Goooo, charge!");
		}
	} else if (status == 14) {
		qm.sendNextPrev("Wait a minute! Did you distribute your AP? I'm heavily affected by my master's #bINT and LUK#k! If you really want to see what I can do, distribute your AP and #bequip your Magician equipment#k before you use the skill!");
	} else if (status == 15) {
		qm.sendImage("UI/tutorial/evan/11/0");
		qm.dispose();
	}
}