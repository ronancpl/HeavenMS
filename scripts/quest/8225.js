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
		qm.sendAcceptDecline("Hey, partner. Now that you make part of the Raven Claws team, I have a task for you. Are you up now?");
	else if (status == 1){
		qm.sendOk("Very well. To prove your valor among our ranks, you must first pass on a little challenge: you have to be able to move extraordinaly well around here, known of all secrets these woods holds. Trace a #bmap of the Phantom Forest#k, then come talk to me. I shall then evaluate if you're worth to be with us.");
		qm.forceStartQuest();
		qm.dispose();
	}
}
