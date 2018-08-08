var status = -1;

function start(mode, type, selection) {
    qm.dispose();
}

function end(mode, type, selection) {
    if(mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }
    
    if (mode == 1) {
	status++;
    } else {
	if (status == 6) {
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("What can I do for you? Tru sent me a message saying that you've been training diligently in Victoria Island while helping him with his work. What is it? What? The Black Wings?", 8);
    } else if (status == 1) {
    	qm.sendNextPrev("#b(You tell her about the Puppeteer and the Black Wings, and about their mission.)#k", 2);
    } else if (status == 2) {
    	qm.sendNextPrev("I see... I didn't know there was a group called the Black Wings... They must be fools if they're trying to revive the Black Mage, knowing how dangerous he is.", 8);
    } else if (status == 3) {
    	qm.sendNextPrev("That...that's true...\r\r#b(She's definitely not afraid to speak her mind.)#k", 2);
    } else if (status == 4) {
    	qm.sendNextPrev("The Book of Prophecy states that the hero will revive and fight against the Black Mage. I wasn't sure if that was true, but this confirms that the Black Mage is still around.", 8);
    } else if (status == 5) {
    	qm.sendNextPrev("Aren't you scared?", 2);
    } else if (status == 6) {
    	qm.sendYesNo("Scared? Pfft. Who cares if the Black Mage appears. You'll be here to protect us. If anything, this makes me want to prepare you for the big battle. Ah, that reminds me, I found a #bskill#k. Would you like to see it?");
    } else if (status == 7) {
	if (qm.getQuestStatus(21720) == 1) {
	    qm.forceCompleteQuest();
	    qm.teachSkill(21001003, qm.getPlayer().getSkillLevel(21001003), 20, -1);
	    qm.gainExp(3900);
	}
        qm.showIntro("Effect/BasicEff.img/AranGetSkill");
        qm.sendNext('#b(You remembered the Polearm Booster skill!)#k', 2);
    } else if (status == 8) {
    	qm.sendNextPrev("This skill was found in an ancient incomprehensible script. I had a hunch it might be a skill you used in the past, and I think I was right. You're not as strong as you used to be, but you'll get there, in time.", 8);
    } else if (status == 9) {
    	qm.sendNextPrev("You are steadily becoming more powerful, and I'll be here to keep motivating you. You have nothing to be afraid of. You will not lose the battle. You didn't emerge from ice only to lose to the Black Mage, did you? This time, you'll finish him, once and for all!", 8);
    } else if (status == 10) {
    	qm.sendPrev("To do so, there's only one thing you can do. Train, train, train. Head to Victoria Island and continue training. Let's make sure you become so powerful that the Black Mage doesn't stand a chance!", 8);
    } else if (status == 11) {
    	qm.dispose();
    }
}