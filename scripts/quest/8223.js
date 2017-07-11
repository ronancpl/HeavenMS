/* ===========================================================
			Ronan Lana
	NPC Name: 		Lukan
	Description: 	Quest - Storming the Castle
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
		qm.sendAcceptDecline("Oh, Jack sent you here? Good timing, I'm planning alongside Jack and others to storm the Keep and retake it from the Twisted Masters what is ours by right. You seem ready to fight alongside us, right?");
	if (status == 1){
		qm.sendOk("Great! Your mission now is to rack down some numbers of their army and weaken their defenses by all effects. Defeat 75 of each: Windraider, Firebrand and Nightshadow, then return to me to report.");
		qm.forceStartQuest();
		qm.dispose();
	}
}
