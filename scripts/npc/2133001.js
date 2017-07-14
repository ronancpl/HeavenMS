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
	case 930000000:
	    cm.sendNext("Welcome. Please enter the portal.");
	    break;
	case 930000100:
	    cm.sendNext("We have to eliminate all these contaminated monsters!");
	    break;
	case 930000200:
	    cm.sendNext("We have to eliminate all these contaminated reactors!");
	    break;
	case 930000300:
            cm.getEventInstance().warpEventTeam(930000400);
	    break;
	case 930000400:
	    if (cm.haveItem(4001169,20)) {
                cm.getEventInstance().warpEventTeam(930000500);
		cm.gainItem(4001169,-20);
	    } else if (!cm.haveItem(2270004)) {
                if(cm.canHold(2270004,10)) {
                    cm.gainItem(2270004,10);
                    cm.sendOk("Good luck in purifying these monsters!");
                }
		else {
                    cm.sendOk("Make space on your USE inventory before receiving the purifiers!");
                }
	    } else {
		cm.sendOk("We have to purify all these contaminated monsters! Get me 20 Monster Marbles from them!");
	    }
	    break;
	case 930000600:
	    cm.sendNext("This is it! Place the Magic Stone on the Altar!");
	    break;
	case 930000700:
	    cm.warp(930000800,0);
	    break;
    }
    cm.dispose();
}