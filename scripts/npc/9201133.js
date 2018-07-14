var map = 677000010;
var quest = 28283;
var status = -1;
var inHuntingGround;

function start(mode, type, selection) {
        inHuntingGround = (cm.getMapId() >= 677000010 && cm.getMapId() <= 677000012);
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
        if(!inHuntingGround) {
            if (cm.isQuestStarted(quest)) {
                if(!cm.getPlayer().haveItemEquipped(1003036)) {
                    cm.sendOk("The path ahead has a weird stench... Equip the #rgas mask#k before entering.");
                    cm.dispose();
                    return;
                }

                cm.sendYesNo("Would you like to move to #b#m" + map + "##k?");
            } else {
                cm.sendOk("The entrance is blocked by a strange force.");
                cm.dispose();
            }
        } else {
            if(cm.getMapId() == 677000011) {
                map = 677000012;
                cm.sendYesNo("Would you like to move to #b#m" + map + "##k?");
            } else {
                map = 105050400;
                cm.sendYesNo("Would you like to #bexit this place#k?");
            }
        }
    } else {
        cm.warp(map, 0);
	cm.dispose();
    }
}