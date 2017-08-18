var status = 0;
var entry;
function start() {
	if(cm.getPlayer().getMapId() == 922000000) {
		entry = 0;
		cm.sendYesNo("Do you wish to quit this stage?");
		status++;
	}

	else if(cm.isQuestStarted(3239)) {
		entry = 1;
		cm.sendYesNo("Do you want to enter #bToy Factory<Sector 4>#k?");
		status++;
	}
	else {
		cm.sendOk("Access to #bToy Factory<Sector 4>#k is restricted to the public.");
	}
}

function action(mode, type, selection) {
	if(status == 1) {
		if(entry == 0) {
			if(mode <= 0) {
				cm.sendOk("Ok. Call me if you urge to exit, then.");
				cm.dispose();
				return;
			}
			
			cm.warp(922000009, 0);
			if(!(cm.isQuestStarted(3239) && cm.haveItem(4031092, 10))) cm.removeAll(4031092);
			cm.dispose();
		}

		else {
			if(mode <= 0) {
				cm.dispose();
				return;
			}
                        
                        if(cm.getWarpMap(922000000).countPlayers() == 0) {
                                cm.warp(922000000, 0);
                                if(!(cm.isQuestStarted(3239) && cm.haveItem(4031092, 10))) cm.removeAll(4031092);
                        } else {
                                cm.sendOk("Someone else is already attempting the parts. Wait for them to finish before you enter.");
                        }
	
			cm.dispose();		
		}
	}
}