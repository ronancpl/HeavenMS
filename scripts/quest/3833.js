importPackage(Packages.tools);

var status = -1;

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        qm.dispose();
    } else {
        if (status == 0) {
            qm.sendOk("Great! You managed to get the herb I need. As a #btoken of gratitude#k, take this item to help on your journey.");
        } else if (status == 1) {
		if(qm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.USE).getNumFreeSlot() >= 2) {
			if(qm.haveItem(4000294, 1000)) {
				qm.gainItem(4000294, -1000);
				qm.gainItem(2040501, 1);
				qm.gainItem(2000005, 50);
				qm.gainExp(54000);
				qm.forceCompleteQuest();
			}
		
			else if(qm.haveItem(4000294, 600)) {
				qm.gainItem(4000294, -600);
				qm.gainItem(2020013, 50);
				qm.gainExp(54000);
				qm.forceCompleteQuest();
			}

			else if(qm.haveItem(4000294, 500)) {
				qm.gainItem(4000294, -500);
				qm.gainExp(54000);
				qm.forceCompleteQuest();
			}

			else if(qm.haveItem(4000294, 100)) {
				qm.gainItem(4000294, -100);
				qm.gainExp(45000);
				qm.forceCompleteQuest();
			}

			else if(qm.haveItem(4000294, 50)) {
				qm.gainItem(4000294, -50);
				qm.gainItem(2020007, 50);
				qm.gainExp(10000);
				qm.forceCompleteQuest();
			}

			else if(qm.haveItem(4000294, 1)) {
				qm.gainItem(4000294, -1);
				qm.gainItem(2000000, 1);
				qm.gainExp(10);
				qm.forceCompleteQuest();
			}
		}
		else {
			qm.sendOk("Could you make #b2 slots available#k on your USE inventory before receiving your reward?");
		}	
                
                qm.dispose();
	}
    }
}