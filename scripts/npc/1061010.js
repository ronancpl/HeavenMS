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
                var mapid = cm.getMapId(), exitid = mapid;
                if(mapid == 108010101) exitid = 105040305;
                else if(mapid == 108010201) exitid = 100040106;
                else if(mapid == 108010301) exitid = 105070001;
                else if(mapid == 108010401) exitid = 107000402;
                else if(mapid == 108010501) exitid = 105070200;

                if (mapid != exitid) cm.getPlayer().changeMap(exitid);
                cm.dispose();
        }
    }
}