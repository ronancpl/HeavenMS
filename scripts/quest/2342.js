/*
	QUEST: Recovered Royal Seal.
	NPC: Violetta
*/

var status = -1;

function start(mode, type, selection){
	if(!qm.hasItem(4001318) && qm.isQuestStarted(2331) && !qm.isQuestCompleted(2331)){
		if(qm.canHold(4001318)){
			qm.forceStartQuest();
			qm.gainItem(4001318, 1);
			qm.forceCompleteQuest();
			qm.sendOk("Looks like you forgot to pick up the #b#t4001318##k when you fought with the #bPrime Minister#k. This is very important to our kingdom, so please deliver this to my father as soon as possible.");
			qm.dispose();
		}
		else{
			qm.sendOk("Please free up one spot in your ETC inventory");
			qm.dispose();
		}
	}
	else{
		qm.dispose();
	}
}

function end(mode, type, selection){
}