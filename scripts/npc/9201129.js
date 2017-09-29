/*
	Demon's Doorway
	Marbas the Demon! Quest
	Victoria Road: The Tree That Grew III
*/

var status;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection){
	if (mode == -1){
		cm.dispose();
	}
	else{
		if (mode == 0 && status ==0){
			cm.dispose();
			return;
		}
		
		if (mode == 1)
			status++;
		else
			status--;

		if (status == 0){
			cm.sendNext("#r\t[Requirements to Enter]\r\n\r\n\t\t1.#k Job must be Magician or Blaze Wizard.\r\n\t\t#r2.#k Must be under level 40.\r\n\t\t#r3.#k Must have #b#t4032495##k.");
		}
		else if (status == 1){
			var jobId = cm.getJobId();

			if ((jobId >= 200 && jobId <= 232) || (jobId >= 1100 && jobId <= 1112)){
				if (cm.getLevel() < 40){
					if (cm.hasItem(4032495)){
						cm.sendYesNo("#kAll conditions have been satisfied. Do you wish to enter?");
					}
					else{
						cm.sendOk("\t\tYou do not have #b#t4032495# #i4032495#");
						cm.dispose();
					}
				}
				else{
					cm.sendOk("\tYour #blevel#k is too high.");
					cm.dispose();
				}
			}
			else{
				cm.sendOk("\tThis is not for you! #rBegone#k you fool!");
				cm.dispose();
			}
		}
		else if (status == 2){
			cm.warp(677000000, 2);

			cm.dispose();
		}
	}
}