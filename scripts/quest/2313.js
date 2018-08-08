/* ===========================================================
			Resonance
	NPC Name: 		Head Patrol Officer
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  The Story Behind the Case
=============================================================
Version 1.0 - Script Done.(18/7/2010)
=============================================================
*/

importPackage(Packages.client);

var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendOk("There's not much time. Please hurry.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendAcceptDecline("I have told our #bMinister of Home Affairs#k of your abilities. Please go pay a visit to him immediately.");
	if (status == 1){
		qm.forceStartQuest();
		qm.sendOk("Save our kingdom! We believe in you!");
		qm.dispose();
	}
}

function end(mode, type, selection) {
    status++;
	if (mode != 1) {
		if(type == 1 && mode == 0)
			status -= 2;
		else {
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.forceCompleteQuest(); 
		qm.gainExp(4000);
		qm.dispose();
	}
}
