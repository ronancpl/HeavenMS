/**
	Author: xQuasar
	NPC: Kyrin - Pirate Job Advancer
	Inside Test Room
**/

var status;

function start() {
    status = -1;
    action(1,0,0);
}

function action(mode,type,selection) {
    if (status == -1) {
	if (cm.getMapId() == 108000502) {
	    if (!(cm.haveItem(4031856,15))) {
	    	cm.sendNext("Go, and get me 15 #b#t4031856##k.");
	    	cm.dispose();
	    } else {
	    	status = 2;
	    	cm.sendNext("Wow, you have brought me 15 #b#t4031856##k! Congratulations. Let me warp you out now.");
	    }
	} else if (cm.getMapId() == 108000501) {
	    if (!(cm.haveItem(4031857,15))) {
	    	cm.sendNext("Go, and get me 15 #b#t4031857##k.");
	    	cm.dispose();
	    } else {
	    	status = 2;
	    	cm.sendNext("Wow, you have brought me 15 #b#t4031857##k! Congratulations. Let me warp you out now.");
	    }
	} else {
	    cm.sendNext("Error. Please report this.");
	    cm.dispose();
	}
    } else if (status == 2) {
    	cm.warp(120000101,0);
		cm.dispose();
    }
}