/* ===========================================================
			Ronan Lana
	NPC Name: 		Jack
	Description: 	Quest - Mark of Heroism
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
        if (status == 0){
		qm.sendAcceptDecline("It's about time! We need to make you a way to travel safely to the summit of the Crimsonwood Valley, or else all we've been doing was for naught. You have to lay hands on the #b#t3992039##k. Are you ready to go?");
	}
	else if (status == 1){
		qm.sendOk("Okay, I need you to have these items on hand first: #b10 #t4010006##k, #b4 #t4032005##k and #b1 #t4004000##k. Go!");
		qm.forceStartQuest();
		qm.dispose();
	}
}
