/*
	QUEST: Jame's Whereabouts (1)
	NPC: James
*/

var status = -1;

function end(mode, type, selection){
	if(mode == -1){
		qm.dispose();
		return;
	}
	else if(mode == 0)
		status--;
	else
		status++;

	if(status == 0){
		qm.sendNext("I... I am scared... Please... please help me...");
	}
	else if(status == 1){
		qm.sendNextPrev("Don't be afriad, #b#p1300005##k sent me here.", 2);
	}
	else if(status == 2){
		qm.sendOk("What? My brother sent you here? Ahhh... I am safe now. Thank you so much...");
		qm.forceCompleteQuest();
		qm.gainExp(6000);
		qm.dispose();
	}
}