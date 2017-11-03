/**
----------------------------------------------------------------------------------
	Skyferry Between Victoria Island, Ereve and Orbis.

	1100003 Kiriru (To Victoria Island From Ereve)

-------Credits:-------------------------------------------------------------------
	*MapleSanta 
----------------------------------------------------------------------------------
**/

var menu = new Array("Victoria Island");
var method;

var hasCoupon = false;

function start() {
	status = -1;
        if(cm.haveItem(4032288)) hasCoupon = true;
        
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if(mode == -1) {
		cm.dispose();
		return;
	} else {
		if(mode == 0 && status == 0) {
			cm.dispose();
			return;
		} else if(mode == 0) {
			cm.sendNext("If you're not interested, then oh well...");
			cm.dispose();
			return;
		}
		status++;
		if (status == 0) {
                        if(!hasCoupon) {
                                for(var i=0; i < menu.length; i++) {
					var display = "\r\n#L"+i+"##b Victoria Island (1000 mesos)#k";
				}			
				cm.sendSimple("Eh, Hello...again. Do you want to leave Ereve and go somewhere else? If so, you've come to the right place. I operate a ferry that goes from #bEreve#k to #bVictoria Island#k, I can take you to #bVictoria Island#k if you want... You'll have to pay a fee of #b1000#k Mesos.\r\n"+display);
                        } else {
                                cm.sendYesNo("Hmm, hi there. I see you have been recommended by Neinheart to go to Victoria Island to improve your knightly skills. Well, just this time the ride will be free of charges. Will you embark?");
                        }
			
		} else if(status == 1) {
                        if(hasCoupon) {
                                cm.gainItem(4032288, -1);
				cm.warp(200090031);
				cm.dispose();
                        } else if(cm.getMeso() < 1000) {
				cm.sendNext("Hmm... Are you sure you have #b1000#k Mesos? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on...");
				cm.dispose();
			} else {
				cm.gainMeso(-1000);
				cm.warp(200090031);
				cm.dispose();
                        }
                }
	}
}