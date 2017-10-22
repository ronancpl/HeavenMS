/*
	QUEST: Before the Mission in Perion Begins
	NPC: Neinheart
*/

var status = -1;

function start(mode, type, selection){
	if(mode == -1 || (mode == 0 && status == 0)){
		qm.dispose();
		return;
	}
	else if(mode == 0)
		status--;
	else
		status++;

	if(status == 0){
		qm.sendAcceptDecline("How's the leveling up so far? By this time, you might be able to participate in the Party Quest at #m103000000#. Leveling up is important, yes, but we need you now to take on a mission as a Cygnus Knight. We just received a new information that may help us.");
	}
	else if(status == 1){
		qm.forceStartQuest();
		qm.dispose();
	}
}

function end(mode, type, selection){}