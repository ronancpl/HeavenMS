/* ===========================================================
        @author Resonance
	NPC Name: 		Scarrs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Killer Mushroom Spores(2)
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
			qm.sendOk("I understand it's not an easy task, but I can't make #bKiller Mushroom Spores#k without them. Please reconsider.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendAcceptDecline("Hmmm... I looked into the making of the Spores while you were gathering up the Poison Mushroom Caps, and realised that we'll need more materials for it. I want you to gather up one more set of items. Can you do it?");
	else if (status == 1){
		qm.forceStartQuest();
		qm.sendOk("Okay, I want you to defeat the Regenade Spores and bring back #b50 Mutated Spores#k in return.");
        } else if (status == 2){
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
		qm.sendOk("Did you gather up all the necessary ingredients for it?")
	else if (status == 1){
                if (!qm.haveItem(4000499, 50)) {
                        qm.sendOk("Please gather all the ingredients first.");
                        status = 2;
                        return;
                }

                qm.sendNext("These should be enough for me to make the #bKiller Mushroom Spores.#k Please hold on for a bit.");
	} else if(status == 2){
                qm.sendOk("Okay, here are the Killer Mushroom Spores. Hopefully this will be enough for you to save our princess and help regain our kingdom. Good luck!");
	} else if(status == 3) {
                qm.forceCompleteQuest();
                qm.gainExp(11500);
		qm.gainItem(4000499, -50);
		qm.gainItem(2430014, 1);
                qm.dispose();
        }
}
	