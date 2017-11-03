/*
NPC:        Muirhat - Nautilus' Port
Created By: Kevin
Function:   When on the quest, he warps player to Black Magician's Disciple
*/

var status;

function start() {
    
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection){
    if (mode == -1){
        cm.dispose();
    }
    else{
        if (mode == 0 && status == 0){
            cm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0){
            if (cm.getQuestStatus(2175) == 1){
                if (cm.getPlayer().canHold(2030019)){
                    cm.sendOk("Please take this #b#t2030019##k, it will make your life a lot easier.  #i2030019#");
                    cm.gainItem(2030019, 1);
                }
                else{
                    cm.sendOk("No free inventory spot available. Please make room in your USE inventory first.");
                    cm.dipose();
                }
            }
            else{
                cm.sendOk("The Black Magician and his followers. Kyrin and the Crew of Nautilus. \n They'll be chasing one another until one of them doesn't exist, that's for sure.");
                cm.dispose();
            }
        }
        else if (status == 1){
            cm.warp(100000006, 0);
            cm.dispose();
        }
    }
}