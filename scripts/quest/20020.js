/*
	NPC Name: 		Nineheart
	Description: 		Quest - Cygnus movie Intro
*/
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.sendOk("Talk to me after you have decided what you really want to do. Whatever you choose, you will not miss out or lose privileges, so don't take this too seriously...");
        qm.dispose();
    } else {
        if(mode == 0 && type > 0 || selection == 1) {
            qm.sendOk("Talk to me after you have decided what you really want to do. Whatever you choose, you will not miss out or lose privileges, so don't take this too seriously...");
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            qm.sendNext("I can tell you've worked really hard by seeing that you're already at Level 10. I think it's time now for you to break out as a Nobless and officially become the Knight-in-Training. Before doing that, however, I want to ask you one thing. Have you decided which Knight you'd want to beome?");
        } else if (status == 1) {
            qm.sendNextPrev("There isn't a single path to becoming a Knight. In fact, there are five of them laid out for you. It's up to you to choose which path you'd like to take, but it should definitely be something you will not regret. That's why... I am offering to show you what you'll look like once you become a Knight.");
        } else if (status == 2) {
            qm.sendSimple("What do you think? Are you interested in seeing yourself as the leader of the Knights? If you have already decided what kind of Knight you'd like to become, then you won't necessarily have to look at it...\r\n\r\n#b#L0#Show me how I'd look like as the leader of the Knights.#l ..#b#L1#No, I'm okay.");
        } else if (status == 3) {
            qm.sendYesNo("Would you like to see for it yourself right now? A short clip will come out soon. Be prepared for what you are about to witness.");
        } else if (status == 4) {
            qm.forceStartQuest();
            qm.forceCompleteQuest();
            qm.warp(913040100, 0);
            qm.dispose();
        }
    }
}
