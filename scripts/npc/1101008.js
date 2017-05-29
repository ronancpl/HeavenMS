function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status == 0 && mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if(status == 0){
			cm.sendSimple("Wait! You'll figure the stuff out by the time you reach Lv. 10 anyway, but if you absolutely want to prepare beforehand, you may view the following information.\r\n\r\n Tell me, what would you like to know?\r\n#b#L0#About you#l\r\n#L1#Mini Map#l\r\n#L2#Quest Window#l\r\n#L3#Inventory#l\r\n#L4#Regular Attack Hunting#l\r\n#L5#How to Pick Up Items#l\r\n#L6#How to Equip Items#l\r\n#L7#Skill Window#l\r\n#L8#How to Use Quick Slots#l\r\n#L9#How to Break Boxes#l\r\n#L10#How to Sit in a Chair#l\r\n#L11#World Map#l\r\n#L12#Quest Notifications#l\r\n#L13#Enhancing Stats#l\r\n#L14#Who are the Cygnus Knights?#l");
	    } else if(status == 1){
			if(selection == 0){
				cm.sendNext("I serve under Shinsoo, the guardian of Empress Cygnus. My master, Shinsoo, has ordered me to guide everyone who comes to Maple World to join Cygnus Knights. I will be assisting and following you around until you become a Knight or reach Lv. 11. Please let me know if you have any questions.");
		    } else if(selection == 1){
				cm.guideHint(1);
				cm.dispose();
			} else if(selection == 2){
				cm.guideHint(2);
				cm.dispose();
			} else if(selection == 3){
				cm.guideHint(3);
				cm.dispose();
			} else if(selection == 4){
				cm.guideHint(4);
				cm.dispose();
			} else if(selection == 5){
				cm.guideHint(5);
				cm.dispose();
			} else if(selection == 6){
				cm.guideHint(6);
				cm.dispose();
			} else if(selection == 7){
				cm.guideHint(7);
				cm.dispose();
			} else if(selection == 8){
				cm.guideHint(8);
				cm.dispose();
			} else if(selection == 9){
				cm.guideHint(9);
				cm.dispose();
			} else if(selection == 10){
				cm.guideHint(10);
				cm.dispose();
			} else if(selection == 11){
				cm.guideHint(11);
				cm.dispose();
			} else if(selection == 12){
				cm.guideHint(12);
				cm.dispose();
			} else if(selection == 13){
				cm.guideHint(13);
				cm.dispose();				
			} else if(selection == 14){
				cm.sendOk("The Black Mage is trying to revive and conquer our peaceful Maple World. As a response to this threat, Empress Cygnus has formed a knighthood, now known as Cygnus Knights. You can become a Knight when you reach Lv. 10.");
				cm.dispose();
			} 
		}else if(status == 2){
				cm.sendNextPrev("There is no need for you to check this info now. These are basics that you'll pick up as you play. You can always ask me questions that come up after you've reached Lv. 10, so just relax.");
				cm.dispose();
			}
	}
}