/*
 * Cygnus Skill -
 */

var status = -1;

function start(mode, type, selection) {
    status++;

    if (status == 0) {
    	qm.sendAcceptDecline("Have you been mastering your skills? I am sure you've mastered all your skills, which means... it's time for you to learn a #bnew skill#k, right?");
    } else if (status == 1) {
	if (mode == 0) {
	    qm.sendOk("Well, what you're doing right now doesn't make you look like someone that's humble. You just look complacent by doing that, and that's never a good thing.");
	} else {
	    qm.forceStartQuest();
	}
	qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}