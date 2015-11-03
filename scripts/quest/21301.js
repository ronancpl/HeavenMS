var status = -1;

function end(mode, type, selection) {
	status++;
    if(mode == 0 && type == 0)
        status -= 2;
    else if (mode != 1) {
        //if (mode == 0)
            qm.sendNext("#b(You need to think about this for a second...)#k");
        qm.dispose();
        return;
    }
	
	if (status == 0) {
        qm.sendNext("Did you manage to slay #o9001013#? Hahaha... you're my master, indeed. Okay, now give me the Red Jade that you found there. I'll have to put it back on the body, and... wait, why aren't you saying a word? Don't tell me... that you didn't bring that back!");
    } else if (status == 1) {
		qm.sendNextPrev("What?! You really didn't bring back the Red Jade? Why? Did you just completely forget it? Ahh... even with the curse of the Black Mage, and the amount of time that has passed and all, never did I think my master would turn out to be stupid...");
	} else if (status == 2) {
		qm.sendNextPrev("No, no, I can't let this put me in despair. This is when I should remain calm and in control, unlike my master...\r\noosah..." );
	} else if (status == 3) {
		qm.sendNextPrev("Even if you go back there now, the thief probably made its way out of there. This means you'll have to make the Red Jade anew. You've made one before, so you do remember the materials required to make one, right? Now go...");
	} else if (status == 4) {
		qm.sendNextPrev("\r\n\r\n\r\nTHIS GUY HAS DEFINITELY LOST ALL MEMORIES!");
	} else if (status == 5) {
		qm.sendNextPrev("...No hope, no dreams... Nooooo!!");
	} else if (status == 6) {
		qm.sendNextPrev("#b(Maha is beginning to really get hysterical. I better leave right this minute. Maybe Lirin can do something about it.)", 2);
		qm.completeQuest();
		qm.dispose();
	}
}