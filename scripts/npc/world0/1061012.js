/*
	NPC Name: 		Insiginificant Being
	Map(s): 		Dungeon : Another Entrance
	Description: 		Takes you to another Dimension
*/

function start() {
    if (cm.getQuestStatus(6108) == 1) {
	var ret = checkJob();
	if (ret == -1) {
	    cm.sendOk("Please form a party and talk to me again.");
	} else if (ret == 0) {
	    cm.sendOk("Please make sure that your party is a size of 2.");
	} else if (ret == 1) {
	    cm.sendOk("One of your party member's job is not eligible for entering the other world.");
	} else if (ret == 2) {
	    cm.sendOk("One of your party member's level is not eligible for entering the other world.");
	} else {
	    var em = cm.getEventManager("s4aWorld");
	    if (em == null) {
		cm.sendOk("You're not allowed to enter with unknown reason. Try again." );
	    } else if (em.getProperty("started").equals("true")) {
		cm.sendOk("Someone else is already attempting to defeat the Jr.Balrog in another world." );
	    } else {
		if(!em.startInstance(cm.getParty(), cm.getMap())) {
                    cm.sendOk("A party in your name is already registered in this event.");
                }
	    }
	}
    }
    else {
        cm.sendOk("You're not allowed to enter the other world with unknown reason.");
    }
    
    cm.dispose();
}

function action(mode, type, selection) {
}

function checkJob() {
    var party = cm.getParty();

    if (party == null) {
	return -1;
    }
    //    if (party.getMembers().size() != 2) {
    //	return 0;
    //    }
    var it = party.getMembers().iterator();

    while (it.hasNext()) {
	var cPlayer = it.next();

	if (cPlayer.getJobId() == 312 || cPlayer.getJobId() == 322 || cPlayer.getJobId() == 900) {
	    if (cPlayer.getLevel() < 120) {
		return 2;
	    }
	} else {
	    return 1;
	}
    }
    return 3;
}