status = -1;
oldSelection = -1;

function start() {
    cm.sendSimple("Hello, I am Shalon from Singapore Airport. I can assist you in getting you to Singapore in no time. Do you want to go to Singapore?\r\n#b#L0#I would like to buy a plane ticket to Kerning City\r\n#b#L1#Let me go in to the departure point.");
}

function action(mode, type, selection) {
	status++;
    if (mode <= 0){
		oldSelection = -1;
		cm.dispose();
	}
	
	if(status == 0){
		if(selection == 0){
			cm.sendYesNo("The ticket will cost you 5,000 mesos. Will you purchase the ticket?");
		}else if(selection == 1){
			cm.sendYesNo("Would you like to go in now? You will lose your ticket once you go in! Thank you for choosing Wizet Airline.");
		}
		oldSelection = selection;
	}else if(status == 1){
		if(oldSelection == 0){
			cm.gainMeso(-5000);
			cm.gainItem(4031732);
		}else if(oldSelection == 1){
			if(cm.itemQuantity(4031732) > 0){
				var em = cm.getEventManager("AirPlane");
				if(em.getProperty("entry") == "true"){
					cm.warp(540010001);
					cm.gainItem(4031732, -1);
				}else{
					cm.sendOk("Sorry the plane has taken off, please wait a few minutes.");
				}
			}else{
				cm.sendOk("You need a #b#t4031732##k to get on the plane!");
			}
		}
		cm.dispose();
	}
}