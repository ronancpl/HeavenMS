/*
 * Cygnus Skill - Training Never ends
 */

var status = -1;

function start(mode, type, selection) {
    status++;

    if (status == 0) {
    	qm.sendAcceptDecline("#h0#. Have you been slacking off on training since reaching Level 100? We all know how powerful you are, but the training is not complete. Take a look at these Knight Commanders. They train day and night, preparing themselves for the possible encounter with the Black Mage.");
    } else {
	if (mode == 1) {
	    qm.forceStartQuest();
	}
	qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}