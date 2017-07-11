/* ===========================================================
			Ronan Lana
	NPC Name: 		Jack
	Description: 	Quest - Stemming the Tide
=============================================================
Version 1.0 - Script Done.(10/7/2017)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
                status -= 2;
        else{
                qm.sendOk("Okay, then. See you around.");
                qm.dispose();
                return;
        }
    }
    if (status == 0)
            qm.sendAcceptDecline("Hey, traveler! I need your help. A great threat is about to endanger the folks down there at the New Leaf City, the way I can see it right now. These creatures roaming around here suddenly... That can be no good. Care if you listen to what I have to say?");
    else if (status == 1) {
            qm.sendOk("That's the thing: the Twisted Masters, great figures that currently holds seize of the Crimsonwood Keep, have planned a large-scale attack to the New Leaf City, that may be happening on the next few days. I can't just stay here observing while they prepare for this attack. However, I can't just leave this position, I must keep an eye on their moves at all costs. There's where you enter: go find Lukan, knight of the past Crimsonwood Keep, that is currently wandering around the woods, and receive from him further orders, he knows what to do.");
            qm.forceStartQuest();
            qm.dispose();
    }
}

function end(mode, type, selection) {
	status++;

	if(status == 0) {
		if(qm.haveItem(3992041)) {
			qm.sendOk("Ah, you did accomplish the task I handed to you. Nicely done, now those guys are busy recovering from this offensive. Now, remember: #bthat key must be used to access#k the Inner Sanctum inside the Keep. Hold that with you at all times if you ever want to enter there.");
			qm.forceCompleteQuest();
                } else if(qm.getQuestStatus(8223) == 2) {
                        qm.sendOk("You completed the mission but lost the key? That's bad, you NEED this key to enter the inner rooms of the Keep. Check out there with Lukan what you should be doing next, we need you inside the Keep.");
		} else {
			qm.sendOk("The folks back there on the city are counting on you on this one. Please hurry up.");
		}
	
		qm.dispose();
	}
}