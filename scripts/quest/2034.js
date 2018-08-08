importPackage(Packages.client);

var item;
var stance;
var status = -1;
var item;

function end(mode, type, selection) {
	if(mode == 0) {
		qm.dispose();
		return;
	}
        status++;

	if(status == 0) {
		qm.sendNext("I knew it ... I knew you could get it done with, quickly! You did your job well last time, and here you are again, taking care of business!! Alright, since you have done it so well, I should reward you well. #b#p1051000##k is giving you a pair of shoes in hopes of helping you out on your future traveling.");
	}

	else if(status == 1) {
	    if(qm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP).getNumFreeSlot() < 1) {
		qm.sendOk("Please free a EQUIP inventory slot to receive the reward.");
		qm.dispose();
		return;
	    }

            stance = qm.getPlayer().getJobStyle();
            
            if(stance == Packages.client.MapleJob.WARRIOR) item = 1072003;
            else if(stance == Packages.client.MapleJob.MAGICIAN) item = 1072077;
            else if(stance == Packages.client.MapleJob.BOWMAN || stance == Packages.client.MapleJob.CROSSBOWMAN) item = 1072081;
            else if(stance == Packages.client.MapleJob.THIEF) item = 1072035;
            else if(stance == Packages.client.MapleJob.BRAWLER || stance == Packages.client.MapleJob.GUNSLINGER) item = 1072294;
            else item = 1072018;
         
            qm.gainItem(item, 1);
            qm.gainItem(4000007, -150);
            
            qm.gainExp(2200);
            qm.completeQuest();
            
            qm.sendOk("Alright, if you need work sometime down the road, feel free to come back and see me. This town sure can use a person like you for help~");
            qm.dispose();
	}
}