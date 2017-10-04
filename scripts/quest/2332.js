/*
	QUEST: Where's Violetta?
	NPC: none
*/

var status = -1;

function start(mode, type, selection){
	if(qm.hasItem(4032388) && !qm.isQuestStarted(2332)){
		qm.forceStartQuest();
		qm.getPlayer().showHint("I must find Violetta. (quest started)");
	}
	qm.dispose();
}

function end(mode, type, selection){
}