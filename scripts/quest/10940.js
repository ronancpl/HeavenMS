//credits to kevintjuh93

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode > 0)
            status++;
        else
            qm.dispose();
        if (status == 0)
            qm.sendAcceptDecline("Hello, #h0#. Welcome to Maple World. It's currently event season, and we're welcome new characters with a gift. Would you like your gift now?");
        else if (status == 1) {
            qm.forceStartQuest();
	    qm.forceCompleteQuest();
            qm.gainItem(2430191, 1, true);
	    qm.sendOk("Open your inventory and double-click on it! These gifts will make you look stylish. Oh, one more thing! You'll get another gift at level 30. Good luck!");
	    qm.dispose();
        }
    }
}