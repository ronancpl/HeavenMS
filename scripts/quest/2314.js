/* ===========================================================
			Resonance
	NPC Name: 		Minister of Home Affairs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Exploring Mushroom Forest(1)
=============================================================
Version 1.0 - Script Done.(18/7/2010)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendNext("Please do not lose faith in our Kingdom of Mushroom.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendAcceptDecline("In order to rescue the princess, you must first navigate the Mushroom Forest. King Pepe set up a powerful barrier forbidding anyone from entering the castle. Please investigate this matter for us.");
	if (status == 1)
		qm.sendNext("You'll run into the barrier at the Mushroom Forest by heading east of where you are standing right now. Please be careful. I hear that the area is infested with crazy, fear-inducing monsters.");
	if(status == 2){
		//qm.forceStartQuest();
		//qm.forceStartQuest(2314,"1");
		qm.gainExp(8300);
		qm.sendOk("I see, so it was indeed not a regular barrier by any means. Great work there. If not for you help, we wouldn't have had a clue as to what that was all about.");
		qm.forceCompleteQuest(); 
		qm.dispose();
	}
}

function end(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendOk("I see that you have thoroughly investigated the barrier at the Mushroom Forest. What was it like?");
	if (status == 1){
		qm.gainExp(8300);
		qm.sendOk("I see, so it was indeed not a regular barrier by any means. Great work there. If not for you help, we wouldn't have had a clue as to what that was all about.");
		qm.forceCompleteQuest(); 
		qm.dispose();
		}
	}
	