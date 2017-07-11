/* ===========================================================
			Ronan Lana
	NPC Name: 		Taggrin
	Description: 	Quest - The Right Path
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
		qm.sendAcceptDecline("Now that you are part of our team, listen to what I have to say. We, Raven Clan of Ninjas, are hired to take care of many issues, and to do so each one works on different sectors of the continent, solving problems for our employers. I'm about to talk about your mission, are you ready?");
	else if (status == 1){
		qm.sendOk("Your next mission is: defeat the Elderwraiths that roam this forest. These are a tough bunch though, so stay alert. I need you to bring me 100 #t4032010# as proof of your duty.");
		qm.forceStartQuest();
		qm.dispose();
	}
}
