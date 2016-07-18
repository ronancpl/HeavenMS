function start() {
    if (cm.c.getPlayer().getMapId() == 103000100 || cm.c.getPlayer().getMapId() == 600010001)
        cm.sendYesNo("The ride to " + (cm.c.getPlayer().getMapId() == 103000100 ? "New Leaf City of Masteria" : "Kerning City of Victoria Island") + " takes off every minute, beginning on the hour, and it'll cost you #b5000 mesos#k. Are you sure you want to purchase #b#t" + (4031711 + parseInt(cm.c.getPlayer().getMapId() / 300000000)) + "##k?");
    else if (cm.c.getPlayer().getMapId() == 600010002 || cm.c.getPlayer().getMapId() == 600010004)
        cm.sendYesNo("Do you want to leave before the train start? There will be no refund.");
}

function action(mode, type, selection) {
    if(mode != 1){
        cm.dispose();
        return;
    }
    if (cm.c.getPlayer().getMapId() == 103000100 || cm.c.getPlayer().getMapId() == 600010001){
	var item = 4031711 + parseInt(cm.c.getPlayer().getMapId() / 300000000);

        if(!cm.canHold(item)) {
            cm.sendNext("You don't have a etc. slot available.");
	}
	else if(cm.getMeso() >= 5000){
            cm.gainMeso(-5000);
            cm.gainItem(item, 1);
            cm.sendNext("There you go.");
        }else
            cm.sendNext("You don't have enough mesos.");
    }else{
        cm.warp(cm.c.getPlayer().getMapId() == 600010002 ? 600010001 : 103000100);
    }
    cm.dispose();
}