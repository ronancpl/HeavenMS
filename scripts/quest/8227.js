/* ===========================================================
			Ronan Lana
	NPC Name: 		Jack, John
	Description: 	Quest - Lost in Translation
=============================================================
Version 1.0 - Script Done.(10/7/2017)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
                status -= 2;
        else {
            qm.sendOk("Come on, the city really needs you cooperating on this one!");
            qm.dispose();
            return;
        }
    }
    if (status == 0)
            qm.sendAcceptDecline("Hey buddy! Nice timing. There is this communique I've been able to swipe from the officials at the Keep, however it's information is encrypted. I have no use for this as it is like this. So, will you transport this to John and see if he can decode this?");
    else if (status == 1){
        if(qm.canHold(4032032, 1)) {
            qm.gainItem(4032032, 1);
            qm.sendOk("Very well, I'm counting on you on this one.");
            qm.forceStartQuest();
        } else {
            qm.sendOk("Hey. There's no slot on your ETC.");
        }

        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
                status -= 2;
        else {
            qm.dispose();
            return;
        }
    }
    if (status == 0){
        if(qm.haveItem(4032032, 1)) {
            qm.gainItem(4032032, -1);
            qm.sendOk("Oh you brought a letter from the Keep?! Neat! Let me check if I can decode that right now.");
            qm.forceCompleteQuest();
        } else {
            qm.sendOk("You don't brought the coded letter Jack said? Come on, kid, we need that to decipher our enemies' next step!");
        }

        qm.dispose();
    }
}
