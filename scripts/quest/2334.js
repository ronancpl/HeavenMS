/*
	QUEST: The Identity of the Princess
	NPC: Violetta
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
		qm.forceStartQuest();
		qm.sendNext("Thank you so much, #b#h ##k. You are the hero that has saved our empire from danger. I'm so grateful for what you've done. I dont know how to thank you. And please understand why I can't show you my face.");
	}
	else if(status == 1){
		qm.sendNextPrev("It's humiliating to say this, but ever since I was a baby, my family has kept my face veiled from the world. They feared of men falling hopelessly in love with me. I've grown so accustomed to it that I even shy away from women. I know, it's rude of me to have my back turned against the hero, but I'll need some time to muster my courage before I can greet you face to face.");
	}
	else if(status == 2){
		qm.sendNextPrev("I see...\r\n#b(Wow, how pretty could she be?)", 2);
	}
	else if(status == 3){
		qm.sendNextPrev("#b(What the--)", 2);
	}
	else if(status == 4){
		qm.sendNextPrev("#b(Is that what's considered pretty in the world of mushrooms?!)", 2);
	}
	else if(status == 5){
		qm.sendNextPrev("I'm so shy, I'm blushing. Anyways, thank you, #b#h ##k.");
	}
	else if(status == 6){
		qm.forceStartQuest();
		qm.gainExp(1000);
		qm.forceCompleteQuest();
		qm.dispose();
	}
}

function end(mode, type, selection){}