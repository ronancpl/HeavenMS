/* ===========================================================
			Resonance
	NPC Name: 		Minister of Home Affairs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Over the Castle Wall (2)
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
			qm.sendNext("Really? Is there another way you can penetrate the castle? If you don't know of one, then just come see me.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendYesNo("Like I told you, just breaking the barrier cannot be a cause for celebration. That's because our castle for the Kingdom of Mushroom completely denies entry of anyone outside our kingdom, so it'll be hard for you to do that. Hmmm... to figure out a way to enter, can you...investigate the outer walls of the castle first?");
	if (status == 1)
		qm.sendNext("Walk past the Mushroom Forest and when you reach the #bSplit Road of Choice#k, just walk towards the castle. Good luck.");
	if (status == 2){
		//qm.forceStartQuest();
		//qm.forceStartQuest(2322, "1");
		qm.gainExp(11000);
		qm.sendOk("Good job navigating through the area.");
		qm.forceCompleteQuest();
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
		qm.sendOk("Hmmm I see... so they have completely shut off the entrance and everything.");
	if (status == 1){
		qm.gainExp(11000);
		qm.sendOk("Good job navigating through the area.");
		qm.forceCompleteQuest();
		qm.dispose();
	}
}
	