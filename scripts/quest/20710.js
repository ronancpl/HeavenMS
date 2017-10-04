/*
	Author: DietStory dev team
	NPC: Matthias
	Quest: Hidden Inside the Trash Can
*/


var status = -1;

function start(mode, type, selection){
	if(mode == -1){
		qm.dispose();
		return;
	}
	else if(mode == 0 && status == 0){
		qm.sendOk("What? Are you declining the mission? Fine, do it like that. I'll just report it straight to #p1101002#.");
		qm.dispose();
		return;
	}
	else if(mode == 0){
		status--;
	}
	else{
		status++;
	}


	if(status == 0){
		qm.sendAcceptDecline("You don't really instill confidence in me, but since you're a Cygnus Knight and all... and since no one else can go on a search right now... Okay, let me explain to you what this mission is about.");
	}
	else if(status == 1){
		qm.forceStartQuest();
		qm.dispose();
	}
}