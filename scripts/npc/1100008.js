/**
----------------------------------------------------------------------------------
	Skyferry Between Victoria Island, Ereve and Orbis.

	1100008 Kiru (Orbis Station)

-------Credits:-------------------------------------------------------------------
	*MapleSanta 
----------------------------------------------------------------------------------
**/

var menu = new Array("Ereve");
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
                                display += "\r\n#L"+i+"##b Ereve (1000 mesos)#k";
                        }			
                        cm.sendSimple("This ship will head towards #bEreve#k, an island where you'll find crimson leaves soaking up the sun, the gentle breeze that glides past the stream, and the Empress of Maple Cygnus. If you're interested in joining the Cygnus Knights, Then you should definitly pay a visit here. Are you interested in visiting Ereve?, The Trip will cost you #b1000#k Mesos\r\n"+display);

                } else if(status == 1) {
                        if(cm.getMeso() < 1000) {
                                cm.sendNext("Hmm... Are you sure you have #b1000#k Mesos? Check your Inventory and make sure you have enough. You must pay the fee or I can't let you get on...");
                                cm.dispose();
                        } else {
                                cm.gainMeso(-1000);
                                cm.warp(200090020);
                                cm.dispose();
                        }
                }
        }
}