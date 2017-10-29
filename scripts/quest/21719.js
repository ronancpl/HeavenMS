var status = -1;

function start(mode, type, selection) {
    if(mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }
    
    if (mode == 1) {
	status++;
    } else {
	if (status == 2) {
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("Aren't you the one that used to be in #m101000000# until not too long ago? I finally found you! Do you know how long it took for me to finally find you?", 8);
    } else if (status == 1) {
    	qm.sendNextPrev("Who are you?", 2);
    } else if (status == 2) {
    	qm.sendAcceptDecline("Me? If you want to know, stop by my cave. I'll even send you an invitation. You'll be directly sent to my cave as soon as you accept. Look forward to seeing you there.");
    } else if (status == 3) {
        qm.forceCompleteQuest();
        qm.warp(910510200, 0);
        qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}