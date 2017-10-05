/*
	Trash Can 3
	Kerning Subway
*/

var status;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection){
	if(mode == -1){
		cm.dispose();
		return;
	}
	else{
		if(mode == 0 && status == 0){
			cm.dispose();
			return;
		}
		else if(mode == 0)
			status--;
		else
			status++;

		if(status == 0) {
			if(cm.isQuestStarted(20710)) {
				if(!cm.hasItem(4032136)) {
					if(cm.canHold(4032136)) {
						cm.gainItem(4032136, 1);
						cm.sendNext("You have found a #b#t4032136##k in the trash can!   #i4032136#");
					} else {
						cm.sendOk("#i4032136# Not enough space in your ETC inventory.");
					}
				} else {
					cm.sendOk("Just a trash can sitting there.");
				}
			} else {
				cm.sendOk("Just a trash can sitting there.");
			}
		} else if(status == 1){
			cm.dispose();
		}
	}
}