/*
	Author: Kevin
	Quest: Zombie Mushroom Signal 3 (2251)
	NPC: The Rememberer (1061011)
	Item: Recording Charm (4032399)
*/

function end(mode, type, selection) {
    
    if(!qm.haveItem(4032399, 20)) {
        qm.sendOk("Please bring me 20 #b#t4032399##k...  #i4032399#");
    }
    else {
        qm.gainItem(4032399, -20);
        qm.sendOk("Oh, you brought 20 #b#t4032399##k! Thank you.");
        qm.gainExp(8000);
        qm.forceCompleteQuest();
    }
    
    qm.dispose();
}