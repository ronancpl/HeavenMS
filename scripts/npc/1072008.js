/**
	Author: xQuasar
	NPC: Kyrin - Pirate Job Advancer
	Inside Test Room
**/

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
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if(status == 0) {
            if (cm.getMapId() == 108000502) {
                if (!(cm.haveItem(4031856,15))) {
                    cm.sendSimple("You haven't brought me all the crystals yet. I'm looking forward for your progress, mate! \r\n#b#L1#I would like to leave#l");
                } else {
                    status++;
                    cm.sendNext("Wow, you have brought me 15 #b#t4031856##k! Congratulations. Let me warp you out now.");
                }
            } else if (cm.getMapId() == 108000501) {
                if (!(cm.haveItem(4031857,15))) {
                    cm.sendSimple("You haven't brought me all the crystals yet. I'm looking forward for your progress, mate! \r\n#b#L1#I would like to leave#l");
                } else {
                    status++;
                    cm.sendNext("Wow, you have brought me 15 #b#t4031857##k! Congratulations. Let me warp you out now.");
                }
            } else {
                cm.sendNext("Error. Please report this.");
                cm.dispose();
            }
        } else if (status == 1) {   // thanks Lame for noticing players getting stuck in area in certain scenarios
            cm.removeAll(4031856);
            cm.removeAll(4031857);
            cm.warp(120000101,0);
            cm.dispose();
        } else if (status == 2) {
            cm.warp(120000101,0);
            cm.dispose();
        }
    }
}
