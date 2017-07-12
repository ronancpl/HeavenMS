/* ===========================================================
			Ronan Lana
	NPC Name: 		Lita Lawless
	Description: 	Quest - Bounty Hunter - Fool's Gold
=============================================================
Version 1.0 - Script Done.(11/7/2017)
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
    if (status == 0) {
            var target = "are Leprechauns";
            qm.sendAcceptDecline("Hey, traveler! I need your help. A new threat has appeared to the citizens of the New Leaf City. I'm currently recruiting anyone, and this time's target #r" + target + "#k. Are you in?");
    }
    else if (status == 1) {
            var reqs = "#r30 #t4032031##k";
            qm.sendOk("Very well. Get me #r" + reqs + "#k, asap. The NLC is counting on you.");
            qm.forceStartQuest();
            qm.dispose();
    }
}