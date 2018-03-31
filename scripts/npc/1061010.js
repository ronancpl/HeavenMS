var status = 0;
var summon;
var nthtext = "bonus";

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();//ExitChat
    else if (mode == 0)
        cm.dispose();//No
    else{		    //Regular Talk
        if (mode == 1)
            status++;
        else
            status--;
		
        if(status == 0){
                cm.sendYesNo("Would you like to leave?");
        }else if(status == 1){
                var mapid = cm.getMapId();
                if(mapid == 108010101) cm.getPlayer().changeMap(105040305);
                else if(mapid == 108010201) cm.getPlayer().changeMap(100040106);
                else if(mapid == 108010301) cm.getPlayer().changeMap(105070001);
                else if(mapid == 108010401) cm.getPlayer().changeMap(107000402);
                else if(mapid == 108010501) cm.getPlayer().changeMap(105070200);

                var em = cm.getEventManager("3rdjob");
                em.getInstance(cm.getPlayer().getName()).unregisterPlayer(cm.getPlayer());
                cm.dispose();
        }
    }
}