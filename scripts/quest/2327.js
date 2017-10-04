/*
	QUEST: James's Whereabouts (3)
	NPC: James
	Why tf does this quest exist?!
*/

var status = -1;

function start(mode, type, selection){
	if(mode == -1){
		qm.dispose();
		return;
	}
	else if(mode == 0 && status == 0){
		qm.dispose();
		return;
	}
	else if(mode == 0)
		status--;
	else
		status++;

	if(status == 0){
		qm.sendNext("Hey! Thank you for bringing me a #b#t4001317##k.");
	}
	else if(status == 1){
		qm.sendNextPrev("I plan to escape from here wearing the #b#t4001317##k. Give me a minute to put it on. Talk to you soon...");
	}
	else if(status == 2){
		qm.forceStartQuest();
		qm.dispose();
	}
}