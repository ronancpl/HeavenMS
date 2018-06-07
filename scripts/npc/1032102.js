/*
	Made by RMZero213 of RaGEZONE forums.
	Just keep this header here and don't claim that you made it.
*/

/*
	1032102.js
	Mar the Fairy
	Dragon Evolver
*/

importPackage(Packages.client.inventory);
importPackage(Packages.client.inventory.manipulator);
importPackage(Packages.server);

var status;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && type > 0) {
			cm.sendOk("Alright, see you next time.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
                    
		if (status == 0) {
			cm.sendYesNo("I am Mar the Fairy. If you have a dragon at level 15 or higher and a rock of evolution, I can evolve your dragon. If you are lucky, you may even get a black one! Would you like me to do so?");
		} else if (status == 1) {
			if (cm.haveItem(5000028, 1)) {
				cm.gainItem(5000028, -1);
				cm.gainItem(5000029, 1);
				cm.sendOk("I don't know how you got that egg, but it has hatched, apparently!");
				cm.dispose();
			} else if (cm.getPlayer().getPet(0) == null) {
				cm.sendOk("Make sure your pet is equipped on slot 1.");
				cm.dispose();
			} else if (cm.getPlayer().getPet(0).getItemId() < 5000029 || cm.getPlayer().getPet(0).getItemId() > 5000033 || !cm.haveItem(5380000,1)) {
				cm.sendOk("You do not meet the requirements. You need #i5380000##t5380000#, as well as either one of #d#i5000029##t5000029##k, #g#i5000030##t5000030##k, #r#i5000031##t5000031##k, #b#i5000032##t5000032##k, or #e#i5000033##t5000033##n equipped on slot 1. Please come back when you do.");
				cm.dispose();
			} else if (cm.getPlayer().getPet(0).getLevel() < 15) {
				cm.sendOk("Your pet must be level 15 or above to evolve.");
				cm.dispose();
			} else if (cm.haveItem(5000029,2) || cm.haveItem(5000030,2) || cm.haveItem(5000031,2) || cm.haveItem(5000032,2) || cm.haveItem(5000033,2)) {
				cm.sendSimple("You have a dragon which isn't out, and as well as a dragon which is out. I can remove one for you. Remember that the data for the dragon I am removing will be lost.\r\n#r#L0#Remove my CASH first slot.#l#k\r\n#b#L1#Remove the first dragon in my inventory.#l#k\r\n#g#L2#No thanks.#l#k");
			} else {
                                var i;
                            
                                for(i = 0; i < 3; i++) {
                                    if(cm.getPlayer().getPet(i) != null && cm.getPlayer().getPet(i).getItemId() == 5000029) {
                                        pet = cm.getPlayer().getPet(i);
                                        break;
                                    }
                                }
                                if(i == 3) {
                                    cm.sendOk("You either don't have a pet dragon ready to evolve or you lack #b#t5380000##k.");
                                    cm.dispose();
                                    return;
                                }
                            
				var id = cm.getPlayer().getPet(i).getItemId();
				//var name = cm.getPlayer().getPet(i).getName();
				//var level = cm.getPlayer().getPet(i).getLevel();
				//var closeness = cm.getPlayer().getPet(i).getCloseness();
				//var fullness = cm.getPlayer().getPet(i).getFullness();
				//MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
				if (id < 5000029 || id > 5000033) {
					cm.sendOk("Something wrong, try again.");
					cm.dispose();
				}
				var rand = 1 + Math.floor(Math.random() * 10);
				var after = 0;
				if (rand >= 1 && rand <= 3) {
					after = 5000030;
				} else if (rand >= 4 && rand <= 6) {
					after = 5000031;
				} else if (rand >= 7 && rand <= 9) {
					after = 5000032;
				} else if (rand == 10) {
					after = 5000033;
				} else {
					cm.sendOk("Something wrong. Try again.");
					cm.dispose();
				}
				
                                /*if (name.equals(MapleItemInformationProvider.getInstance().getName(id))) {
				 	name = MapleItemInformationProvider.getInstance().getName(after);
				}*/
                
				cm.gainItem(5380000, -1);
				cm.evolvePet(i, after);
                                
				cm.sendOk("Your dragon has now evolved!! It used to be a #i" + id + "# #t" + id + "#, and now it's a #i" + after + "# #t" + after + "#!");
				cm.dispose();
			}
		} else if (status == 2) {
			if (selection == 0) {
				MapleInventoryManipulator.removeFromSlot(cm.getClient(), MapleInventoryType.CASH, 1, 1, true);
				cm.sendOk("Your cash first slot is removed.");
			} else if (selection == 1) {
				if (cm.haveItem(5000029, 2)) {
					cm.gainItem(5000029, -1);
				} else if (cm.haveItem(5000030, 2)) {
					cm.gainItem(5000030, -1);
				} else if (cm.haveItem(5000031, 2)) {
					cm.gainItem(5000031, -1);
				} else if (cm.haveItem(5000032, 2)) {
					cm.gainItem(5000032, -1);
				} else if (cm.haveItem(5000033, 2)) {
					cm.gainItem(5000033, -1);
				}
				cm.sendOk("The first dragon in your inventory is removed.");
			} else if (selection == 2) {
				cm.sendOk("Okay, come back next time.");
			}
			cm.dispose();
		}
	}
}