importPackage(Packages.tools);

var status = -1;

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        qm.dispose();
    }
    else {
        if (status == 0) {
            qm.sendNext("Take these #bMana Elixir Pills#k as a token of my gratitude.");
        }
	else if (status == 1) {
		if(qm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.USE).getNumFreeSlot() >= 1) {
			qm.gainItem(4000099, -1);
			qm.gainItem(2000011, 50);
			qm.gainExp(8000);
			qm.forceCompleteQuest();
		}
		else {
			qm.sendNext("Hm? It looks like your inventory is full.");
		}
                
                qm.dispose();
	}
    }    
}