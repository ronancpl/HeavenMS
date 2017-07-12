/* ===========================================================
			Ronan Lana
	NPC Name: 		John, Elpam
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
		qm.sendAcceptDecline("Hm, that's no good. I can't seem to make these Hyper Glyphs work, dang it. ... Ah, yea, the outsider! He may know the language this paper is written on. Let Elpam try to read this, maybe he knows something.");
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
            qm.sendOk("Hello, native of this world. So you have a message that needs translation? My people back in Versal is known for mastering many foreign languages, this one may very well be some we know. Please stand by...");
            qm.gainItem(4032032, -1);
            qm.forceCompleteQuest();
        } else {
            qm.sendOk("I'm afraid you don't have the letter you claimed to have with you.");
        }

        qm.dispose();
    }
}
