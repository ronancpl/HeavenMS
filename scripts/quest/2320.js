/* ===========================================================
			Resonance
	NPC Name: 		Scarrs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  A Friendship with Bruce
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
			qm.sendOk("I wanted you to personally give this piece of good news to #bBruce#k, but I understand if you're busy.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendAcceptDecline("I have just one more request for you. Would you like to take a listen?");
	if (status == 1){
		qm.forceStartQuest();
		qm.gainItem(4032389, 1);
		qm.sendOk("To be honest, these #bKiller Mushroom Spores#k are not completely out of my own work. Do you remember #bBruce#k from #bHenesys#k? I have been friends with him since childhood, and #bKiller Mushroom Spores#k was completed after he shared the results of his studies with me. This was all thanks to him, so I'd like for you to give this to him for me.");
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
		qm.sendOk("Oh! You're here on behalf of #bScarrs#k? \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#fUI/UIWindow.img/QuestIcon/8/0# 8800 exp");
	if (status == 1){
		qm.gainExp(8800);
		qm.gainItem(4032389, -1);
		qm.sendOk("Ahh, so this is the #bKiller Mushroom Spores#k that I was working on in the past. I had a tough time gathering up the ingredients, so I left it in theory only, but he was able to complete it, with a sample to show for as well. Please tell him I appreciate his good work.");
		qm.forceCompleteQuest();
		qm.dispose();
	}
}
	