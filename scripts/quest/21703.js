
importPackage(Packages.client);
var status = -1;

function start(mode, type, selection) {
    if(mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }
    
    if (mode == 1) {
	status++;
    } else {
	if (status == 6) {
	    qm.sendNext("I know it takes an incredible amount of strength and will to outdo your instructor, but you weren't meant to let yourself wither away. You must move on to bigger and better things! You must do everything you can to embrace your heroic nature!");
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("Your abilities are really beginning to take shape. I am surprised that an old man like me was able to help you. I'm tearing up just thinking about how happy it makes me to have been of assistance to you. *Sniff sniff*");
    } else if (status == 1) {
    	qm.sendNextPrev("#b(You didn't even train that long with him... Why is he crying?)#k", 2);
    } else if (status == 2) {
    	qm.sendNextPrev("Alright, here's the third and the final stage of training. Your last opponent is... #r#o9300343#s#k! Do you know anything about #o1210100#s?");
    } else if (status == 3) {
    	qm.sendNextPrev('Well, a little bit...', 2);
    } else if (status == 4) {
    	qm.sendNextPrev("They are natural warriors! They're born with a voracious appetite for food. They devour any food that's visible the moment they sweep by. Terrifying, isn't it?");
    } else if (status == 5) {
    	qm.sendNextPrev("#b(Is that really true?)#k", 2);
    } else if (status == 6) {
    	qm.sendAcceptDecline("Okay, now... #bEnter the Training Center again#k, defeat #r30#k #o9300343#s, and show me what you're made of! You'll have to exert all your energy to defeat them! Go, go, go! Rise above me!");
    } else if (status == 7) {
        qm.forceStartQuest();
        qm.sendOk("Now go and take on those monstrous #o9300343#s!");
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if(mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }
    
    if (mode == 1) {
    	status++;
    } else {
	if (status == 2) {
	    qm.sendNext("Are you reluctant to leave your instructor? *Sniff sniff* I'm so moved, but you can't stop here. You are destined for bigger and better things!");
	    qm.dispose();
	    return;
	}
        status--;
    }
    if (status == 0) {
    	qm.sendNext("Ah, you've come back after defeating all 30 #o9300343#s. I knew you had it in you... Even though you have no memories and few abilities, I could see that you were different! How? Because you're carrying around a Polearm, obviously!");
    } else if (status == 1) {
    	qm.sendNextPrev("#b(Is he pulling your leg?)#k'", 2);
    } else if (status == 2) {
    	qm.sendYesNo("I have nothing more to teach you, as you've surpassed my level of skill. Go now! Don't look back! This old man is happy to have served as your instructor.");
    } else if (status == 3) {
	if (qm.isQuestStarted(21703)) {
	    qm.forceCompleteQuest();
	    qm.teachSkill(21000000, qm.getPlayer().getSkillLevel(21000000), 10, -1);   // Combo Ability Skill
	    qm.gainExp(2800);
	}
		qm.sendNext("(You remembered the #bCombo Ability#k skill! You were skeptical of the training at first, since the old man suffers from Alzheimer's and all, but boy, was it effective!)", 2);
		qm.showInfo("Effect/BasicEff.img/AranGetSkill");
    } else if (status == 4) {
		qm.sendPrev("Now report back to #p1201000#. I know she'll be ecstatic when she sees the progress you've made!");
		qm.dispose();
    }
}