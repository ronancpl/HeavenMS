importPackage(Packages.client);

var item;
var stance;
var status = -1;
var vecItem;

function end(mode, type, selection) {
	if(mode == 0) {
		qm.dispose();
		return;
	}
        status++;

	if(status == 0) {
		qm.sendNext("THIS is the deed to the land that my son lost! And you even brought all the necessary materials to build the house! Thank you so much ... my relatives can all move in and live in #m102000000#! As a sign of appreciation ...");
	}

	else if(status == 1) {
	    if(qm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.USE).getNumFreeSlot() < 1) {
		qm.getPlayer().dropMessage(1, "USE inventory full.");
		qm.dispose();
		return;
	    }

            var talkStr = "Okay, now choose the scroll of your liking ... The odds of winning are 10% each. \r\n\r\n#rSELECT A ITEM\r\n#b";
            stance = qm.getPlayer().getJobStyle();
            
            if(stance == Packages.client.MapleJob.WARRIOR || stance == Packages.client.MapleJob.BEGINNER) vecItem = new Array(2043002, 2043102, 2043202, 2044002, 2044102, 2044202, 2044402, 2044302);
            else if(stance == Packages.client.MapleJob.MAGICIAN) vecItem = new Array(2043702, 2043802);
            else if(stance == Packages.client.MapleJob.BOWMAN || stance == Packages.client.MapleJob.CROSSBOWMAN) vecItem = new Array(2044502, 2044602);
            else if(stance == Packages.client.MapleJob.THIEF) vecItem = new Array(2043302, 2044702);
            else vecItem = new Array(2044802, 2044902);
            
            for (var i = 0; i < vecItem.length; i++)
            talkStr += "\r\n#L" + i + "# #i" + vecItem[i] + "# #t" + vecItem[i] + "#";
            qm.sendSimple(talkStr);
	}

        else if(status == 2) {
            item = vecItem[selection];
            qm.gainItem(item, 1);
            qm.gainItem(4000022, -100);
            qm.gainItem(4003000, -30);
            qm.gainItem(4003001, -30);
            qm.gainItem(4001004, -1);
            qm.gainExp(20000);
            qm.gainMeso(15000);
            qm.gainFame(2);
            qm.completeQuest();
                    
            qm.dispose();
        }
}