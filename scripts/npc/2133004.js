var status = -1;

function start() {
    action(1,0,0);
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
                    if(!cm.haveItem(4001163) || !cm.isEventLeader()) {
                        cm.sendYesNo("Let your party leader show me the Purple Stone of Magic from here.\r\n\r\nOr maybe you want to #rleave this forest#k? Leaving now means to abandon your partners here, take that in mind.");
                    } else {
                        cm.sendNext("Great, you have the Purple Stone of Magic. I shall show you guys #bthe path leading to the Stone Altar#k. Come this way.");
                    }                        
                } else if(status == 1) {
                        if (!cm.haveItem(4001163)) {
                                cm.warp(930000800);
                        } else {
                                cm.getEventInstance().warpEventTeam(930000600);
                        }
                        
                        cm.dispose();
                }
        }
}