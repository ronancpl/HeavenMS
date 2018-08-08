/* ===========================================================
			Ronan Lana
	NPC Name: 		Lukan
	Description: 	Quest - Storming the Castle
=============================================================
Version 1.0 - Script Done.(10/7/2017)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
            if(type == 1 && mode == 0)
                    status -= 2;
            else{
                    qm.sendOk("Okay, then. See you around.");
                    qm.dispose();
                    return;
            }
    }
    if (status == 0)
            qm.sendAcceptDecline("The time is now, kid. We have all the preparations complete to further research for why all these oddities have been happening lately. I also must introduce you to my brother, Jack. ");
    if (status == 1){
            qm.sendOk("He is currently wandering around the Crimsonwood Mountain, past the sinister Phantom Forest, in the track to the Crimsonwood Keep. Your next destination is there, may your journey be a safe one.");
            qm.forceStartQuest();
            qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendOk("Okay, then. See you around.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendNext("Who are you? Oh, you came here by my brother John's stead? Great.");
            
        else if (status == 1){
		qm.sendOk("It seems you helped the folks at the city at some errands, don't you? I shall appraise you nicely. Take a look on this: this is a map of the Phantom Forest, which I made myself after enough exploration. Take possession of that, and you #bwill be granted passage#k by paths other times undiscoverable. Remember well to #rnever lose it#k, you won't be having that again!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i3992040# #t3992040#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 175000 EXP");
	}
	else if (status == 2){
		if(qm.canHold(3992040, 1)) {
                    qm.gainItem(3992040, 1);
                    qm.gainExp(175000);
                    qm.forceCompleteQuest();
                }
		else {
                    qm.sendOk("Hey, you don't have a slot in your SETUP inventory for what I have to give to you. Solve that minor issue of yours then talk to me.");
                }
                
                qm.dispose();
	}
}
