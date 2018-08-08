/* ===========================================================
			Ronan Lana
	NPC Name: 		John, Jack
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
		qm.sendAcceptDecline("I knew we could rely on the outsider on this matter! Now that we have the letter translated by him, head it to Jack, he knows what to do.");
	else if (status == 1){
            if(qm.haveItem(4032018, 1)) {
                qm.forceStartQuest();
            } else if(qm.canHold(4032018, 1)) {
                qm.gainItem(4032018, 1);
                qm.forceStartQuest();
            } else {
                qm.sendOk("Oy, you need a slot in your ETC to get the communique.");
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
        if(qm.haveItem(4032018, 1)) {
            qm.sendOk("Oh, you brought it. Nicely done, the countermeasure process will be much easier now.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 50000 EXP");
        } else {
            qm.sendOk("What's wrong? Why you didn't retrieved the translated message yet? Please bring me the letter's content for me to strategize a countermeasure ASAP.");
            qm.dispose();
        }
    } else if (status == 1){
        qm.gainItem(4032018, -1);
        qm.gainExp(50000);
        qm.forceCompleteQuest();
        
        qm.dispose();
    }
}
