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
        qm.sendNext("How's the training going? Hmmm... Level 70... That's still not much, but you have really made some strides since the first time I met you fresh out of ice. Keep training, and I am sure one day you'll be able to regain your pre-battle form.");
    } else if (status == 1) {
		qm.sendAcceptDecline("But before doing that, I'll need you back in Rein for a bit. #bYour pole arm is reacting strange once again. It looks like it has something it wants to tell you. #kIt might be able to awaken your hidden powers, so please come immediately.");
	} else if (status == 2) {
		qm.sendOk("Anyway, I thought it was really something that a weapon has its own identity, but seriously... this weapon does not stop talking. It first kept on crying because I wasn't really paying attention to its needs, and... ahh, please keep this a secret from the pole arm. I don't think it's a good idea to upset the weapon any further.");
		qm.forceStartQuest();
		qm.dispose();
	}
}