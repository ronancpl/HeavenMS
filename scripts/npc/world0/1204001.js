/*
 * NPC : Francis (Doll master)
 * Map : 910510200
 */

var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
    	status++;
    } else {
    	status--;
    }
    if (status == 0) {
    	cm.sendNext("I'm Francis, the Puppeteer of the Black Wings. How dare you disturb my puppets... It really upsets me, but I''ll let it slide this time. If I catch you doing it again though, I swear in the name of the Black Mage, I will make you pay for it.", 9);
    } else if (status == 1) {
    	cm.sendNextPrev("#b(The Black Wings? Huh? Who are they? And how is all this related to the Black Mage? Hm, maybe you should report this info to Tru.)#k", 3);
    } else if (status == 2) {
		cm.startQuest(21760);
		cm.warp(105040200, 3);//104000004 
		cm.dispose();
    }
}