/**
----------------------------------------------------------------------------------
	Whale Between Lith harbor and Rien.

	1200004 Puro

-------Credits:-------------------------------------------------------------------
	*MapleSanta 
----------------------------------------------------------------------------------
**/

var menu = new Array("Rien");
var method;

function start() {
	status = -1;
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
			cm.sendNext("OK. If you ever change your mind, please let me know.");
			cm.dispose();
			return;
		}
		status++;
		if (status == 0) {
                        var display = "";
			for(var i=0; i < menu.length; i++) {
                                display += "\r\n#L"+i+"##b Rien (800 mesos)#k";
                        }
                        cm.sendSimple("Are you thinking about leaving Victoria Island and heading to our town? If you board this ship, I can take you from #bLith Harbor#k to #bRien#k and back. But you must pay a #bfee of 800#k Mesos. Would you like to go to Rien? It'll take about a minute to get there.\r\n"+display);
			
                } else if(status == 1) {
                        if(cm.getMeso() < 800) {
                                cm.sendNext("Hmm... Are you sure you have #b800#k Mesos? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on...");
                                cm.dispose();
                        } else {
                                cm.gainMeso(-800);
                                cm.warp(200090060);
                                cm.dispose();
                        }
                }
        }
}