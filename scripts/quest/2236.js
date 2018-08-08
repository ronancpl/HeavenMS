/* ===========================================================
			Ronan Lana
	NPC Name: 		Chrishrama
	Description: 	Quest -  How to Shoo Away the Evil
=============================================================
Version 1.0 - Script Done.(20/3/2017)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendOk("If we don't place these Charms on the Shaman Rocks, evil might awaken...");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendAcceptDecline("I can feel the forces of evil. They're deep inside the dungeon and they're very, very powerful. If we want to drive the evil away from this place, we must place Charms on the Shaman Rocks inside the dungeon. Will you do that for me?");
	if (status == 1){
		if(qm.haveItem(4032263)) qm.gainItem(4032263, -6);
		qm.gainItem(4032263, 6);

		qm.sendOk("Take these Charms and place them on the Shaman Rocks in the dungeon. I'm giving you a total of 6 Charms.");
		qm.forceStartQuest();
		qm.dispose();
	}
}

function end(mode, type, selection) {
	status++;

	if(status == 0) {
		if(qm.getQuestProgress(2236) == 63) {	//111111
			qm.sendOk("I, too, felt it. The force of the Shaman Rocks began to overpower the forces of evil. I think Sleepywood is safe now. The evil has been eliminated.");
			qm.gainExp(60000);
			qm.forceCompleteQuest();
		}
		else {
			if(qm.haveItem(4032263)) qm.gainItem(4032263, -6);
			qm.gainItem(4032263, 6);

			qm.sendOk("Oh, not good. I still sense bad omens coming from the interior. Here, take these charms and seal them at the Shaman Rocks. We are counting on you.");
			qm.updateQuest(2236, 0);
		}
	
		qm.dispose();
	}
}