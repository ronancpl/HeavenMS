var status = -1;

function start() {
    action(1,0,0);
}

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    switch(cm.getPlayer().getMapId()) {
	case 930000500:
	    if (!cm.haveItem(4001163)) {
	    	cm.sendNext("Get me the Purple Stone of Magic from here.");
	    } else {
		cm.getEventInstance().warpEventTeam(930000600);
	    }
	    break;
    }
    cm.dispose();
}