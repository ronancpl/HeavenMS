/* ===========================================================
			Resonance
	NPC Name: 		Head Patrol Officer
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  The Test
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
			qm.sendOk("Hmmm... you must be unsure of your combat skills. We'll be here waiting for you, so come see us when you're ready.");
		    qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendAcceptDecline("We need your help, noble explorer. Our kingdom is currently facing a big threat, and we are in desperate need of a courageous explorer willing to fight for us, and that's how you ended up here. Please understand, though, that since we need place our faith in you, we'll have to test your skills first before we can stand firmly behind you. Will it be okay for you to do this for us?");
	if (status == 1){
		qm.forceStartQuest();
		qm.sendOk("Keep moving forward, and you'll see #bRenegade Spores#k, the Spores that turned their backs on the Kingdom of Mushroom. We'd appreciate it if you can teach them a lesson or two, and bring back #b50 Mutated Spores#k in return.");
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
		qm.sendOk("Did you teach those Renegade Spores a lesson?");
	if (status == 1){
		qm.forceCompleteQuest();
		qm.gainExp(11500);
		qm.gainItem(4000499, -50);
		qm.sendOk("That was amazing. I apologize for doubting your abilities. Please save our Kingdom of Mushroom from this crisis!");
		qm.dispose();
	}
}
	