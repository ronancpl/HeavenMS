/*
	QUEST: Recovered Royal Seal.
	NPC: Violetta
*/

var status = -1;

function start(mode, type, selection){
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            if(!qm.hasItem(4001318) && qm.isQuestStarted(2331) && !qm.isQuestCompleted(2331)){
                if(qm.canHold(4001318)){
                    qm.forceStartQuest();
                    qm.gainItem(4001318, 1);
                    qm.forceCompleteQuest();
                    qm.sendOk("Looks like you forgot to pick up the #b#t4001318##k when you fought with the #bPrime Minister#k. This is very important to our kingdom, so please deliver this to my father as soon as possible.");
                }
                else{
                    qm.sendOk("Please free up one spot in your ETC inventory");
                }
            }
            else{
                qm.dispose();
            }
        } else if (status == 1) {
            qm.dispose();
        }
    }
}

function end(mode, type, selection){
}