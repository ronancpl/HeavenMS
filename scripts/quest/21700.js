var status = -1;

function start(mode, type, selection) {
    if(mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }
    
    if (mode == 1) {
	status++;
    } else {
	if (status == 4) {
	    qm.sendNext("No? Are you saying you can train on your own? I'm just letting you know that you'll get better results if you train with an instructor. You can't live in this world alone. You must learn to get along with other people.");
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("It seems like you've started to remember things. Your Polearm must have recognized you. This means you are surely #bAran, the wielder of Polearms#k. Is there anything else you remember? Skills you used with the Polearm perhaps? Anything?");
    } else if (status == 1) {
    	qm.sendNextPrev("#b(You tell her that you remember a few skills.)#k", 2);
    } else if (status == 2) {
    	qm.sendNextPrev("That's not a lot, but it's progress. Our focus, then, should be to get you back to the state before you were frozen. You may have lost your memory, but I'm sure it won't take long for you to recover the abilities that your body remembers.");
    } else if (status == 3) {
    	qm.sendNextPrev('How do I recover my abilities?', 2);
    } else if (status == 4) {
    	qm.sendAcceptDecline("There is only one way to do that. Train! Train! Train! Train! If you continue to train, your body will instinctively remember its abilities. To help you through the process, I'll introduce you to an instructor.");
    } else if (status == 5) {
		qm.sendNext("I gave you a #bPolearm#k because I figured it would be best for you to use a weapon you're familiar with. It will be useful in your training.");
		if (!qm.isQuestStarted(21700) && !qm.isQuestCompleted(21700)) {
			qm.gainItem(1442000,1);
			qm.forceStartQuest();
		}
    } else if (status == 6) {
		qm.sendPrev("You'll find a Training Center if you exit to the #bleft#k. There, you'll meet #b#p1202006##k. I'm a bit worried because I think he may be struggling with bouts of Alzheimer's, but he spent a long time researching skills to help you. I'm sure you'll learn a thing or two from him.");
		qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}