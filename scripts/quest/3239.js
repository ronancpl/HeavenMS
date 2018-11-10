var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            if(qm.haveItem(4031092, 10)) {
		if(qm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                    qm.sendOk("Well done! You brought back all the #t4031092# that were missing. Here, take this scroll as a token of my gratitude...");
		} else {
                    qm.sendOk("Free a space on your USE inventory before receiving your prize.");
                    qm.dispose();
                    return;
		}
            } else {
                qm.sendOk("Please return me 10 #t4031092# that went missing on this room.");
                qm.dispose();
                return;
            }
        } else if (status == 1) {
            qm.gainItem(4031092, -10);
            
            rnd = Math.floor(Math.random() * 4);
            if(rnd == 0) qm.gainItem(2040704, 1);
            else if(rnd == 1) qm.gainItem(2040705, 1);
            else if(rnd == 2) qm.gainItem(2040707, 1);
            else qm.gainItem(2040708, 1);

            qm.gainExp(2700);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}