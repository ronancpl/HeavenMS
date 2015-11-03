/* ===========================================================
			Resonance
	NPC Name: 		Scarrs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Killer Mushroom Spores(1)
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
			qm.sendOk("Breaking through the barrier will require the Poison Mushroom Cap. Talk to me when you change your mind.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendAcceptDecline("Ah! If I am not mistaken, I saw the #bKiller Mushroom Spores#k way back when I was a kid in a book. Now I remember... it's made out of extracts of powerful poisons from Poison Mushrooms, which means you'll need some Poison Mushroom Caps. If you can get me those, I think I'll be able to make it.");
	if (status == 1){
		qm.forceStartQuest();
		qm.sendOk("Please defeat #bPoison Mushrooms#k and bring back #b100 Poison Mushroom Caps#k in return.");
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
		qm.sendOk("Have you gathered up the 100 Poison Mushroom Caps like I asked you to get?");
	if (status == 1){
		qm.gainExp(13500);
		qm.gainItem(4000500, -100);
		qm.sendOk("I am amazed that you were able to gather up these 100 Poison Mushroom Caps, which is considered a difficult feat. I think I'll be able to make #bKiller Mushroom Spores#k our of these.");
		qm.forceCompleteQuest(); 
		qm.dispose();
	}
}
	