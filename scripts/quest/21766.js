var status = -1;

function start(mode, type, selection) {
	status++;
	if (status == 0) {
		qm.sendNext("Hey! Can you do me a favor? #p20000# seems a bit strange these days...");
	} else if (status == 1) {
		qm.sendNext("He used to scowl and whine about his arthritis until just recently, but he''s suddenly become all happy and smiley!!");
	} else if (status == 2) {
		qm.sendNext("I have a feeling there is a secret behind that wooden box. Could you stealthily look into the wooden box next to #p20000#?");
	} else if (status == 3) {
		qm.sendNext("You know where #p20000# is, right? He's to the right. Just keep going until you see where Vikin is, then head down past the hanging shark and octopus, and you''ll see John. The box should be right next to him.");
	} else {
                qm.forceStartQuest();
		qm.dispose();
        }
}

function end(mode, type, selection) {
	qm.gainExp(200);
	qm.forceCompleteQuest();
	qm.dispose();
}