var map = 677000006;
var quest = 28256;
var status = -1;

function start(mode, type, selection) {
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	cm.dispose();
	return;
    }
    if (status == 0) {
        if (cm.isQuestStarted(quest)) {
            cm.sendYesNo("Would you like to move to #b#m" + map + "##k?");
        } else {
            cm.sendOk("The entrance is blocked by a strange force.");
            cm.dispose();
        }
    } else {
	if(cm.haveItem(4001362, 1)) cm.gainItem(4001362, -cm.getItemQuantity(4001362));
        if(cm.haveItem(4001363, 1)) cm.gainItem(4001363, -cm.getItemQuantity(4001363));
        if(cm.haveItem(4032486, 1)) cm.gainItem(4032486, -1);
        if(cm.haveItem(4032488, 1)) cm.gainItem(4032488, -1);
        if(cm.haveItem(4032489, 1)) cm.gainItem(4032489, -1);
        if(cm.haveItem(4220153, 1)) cm.gainItem(4220153, -1);
        
	cm.warp(map, 0);
	cm.dispose();
    }
}