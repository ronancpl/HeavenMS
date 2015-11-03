/* ===========================================================
			Resonance
	NPC Name: 		Minister of Magic
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Over the Castle Wall (1)
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
			qm.sendOk("You don't seem to follow instructions well. Come see me when you are ready.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendAcceptDecline("Now you'll be able to penetrate the spiny vine barrier of Mushroom Forest, but before that, #bMinister of Home Affairs#k wants to have a word with you. Please go see him immediately.");
	if (status == 1){
		qm.forceStartQuest();
		qm.sendOk("Good luck.");
		qm.dispose();
	}
}

function end(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendOk("I have been keeping up on your fabulour work. I am aware that you have successfully created the #bKiller Mushroom Spores#k, which penetrates through the unpenetrable barrier of the forest. Congratulations!");
	if (status == 1){
		qm.gainExp(2500);
		qm.sendOk("The problem now is to figure out how to enter the castle.");
		qm.forceCompleteQuest();
		qm.dispose();
	}
}
	