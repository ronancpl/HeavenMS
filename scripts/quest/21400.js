var status = -1;

function start(mode, type, selection) {
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
        qm.sendAcceptDecline("How is the training going? I know you're busy, but please come to #bRien#k immediately. The #bMaha#k has started to act weird again... But its even weirder now. It's different from before. It's... darker than usual.");
    } else if (status == 1) {
		qm.sendOk("I have a bad feeling about this. Please come back here. I've never seen or herd Maha like this, but I can sense the suffering its going through. #bOnly you, the master of Maha, can do something about it!");
		qm.startQuest();
		qm.dispose();
	}
}