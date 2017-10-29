/*
	NPC Name: 		Kisan
	Description: 		Quest - Cygnus tutorial helper
*/

var status = -1;

function start(mode, type, selection) {
    if(mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }
    
    if (mode == 1) {
	status++;
    } else {
	if (status == 2) {
	    qm.sendNext("You don't want to? It's not even that hard, and you'll receive special equipment as a reward! Well, give it some thought and let me know if you change your mind.");
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("There are a number of ways to hunt, but the most basic way is with your #bRegular Attack#k. All you need is a weapon in your hand, since it's a simple matter of just swinging your weapon at monsters.");
    } else if (status == 1) {
    	qm.sendNextPrev("Press the #bC#k to use your Regular Attack. Usually the C is located #bat the bottom left of the keyboard#k, but you don't need me to tell you that, right? Find the C and try it out!");
    } else if (status == 2) {
    	qm.sendAcceptDecline("Now that you've tried it, we've got to test it out. In this area, you can find the weakest #r#o100120##ks in Ereve, which is perfect for you. Try hunting #r1#k. I'll give you a reward when you get back.");
    } else if (status == 3) {
		qm.forceStartQuest();
		qm.guideHint(4);
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
	status--;
    }
    if (status == 0) {
    	qm.sendNext("Ah, it seems like you've successfully hunted a #o100120#. Pretty simple, right? Regular Attacks may be easy to use, but they are pretty weak. Don't worry, though. #p1102006# will teach you how to use more powerful skills. Wait, let me give you a well-deserved quest reward before you go.");
    } else if (status == 1) {
    	qm.sendNextPrev("This equipment is for Noblesses. It's much cooler than what you're wearing right now, isn't it? Follow the arrows to your left to meet my younger brother #b#p1102006##k. How about you change into your new Noblesse outfit before you go? \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i1002869# #t1002869# - 1 \r\n#i1052177# #t1052177# - 1 \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 30 exp");
    } else if (status == 2) {
        qm.gainItem(1002869, 1);
        qm.gainItem(1052177, 1);
        qm.forceCompleteQuest();
        qm.gainExp(30);
        qm.guideHint(6);
        qm.dispose();
    }
}