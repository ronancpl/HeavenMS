/** 
Roodolph - Happy ville 
By fantier123
**/ 
var status;
function start() { 
        status = 0;
	action(1, 0, 0); 
} 
function action(mode, type, selection) { 
	if (mode == -1) { 
		cm.dispose(); 
	} else { 
		if (mode == 0) { 
			cm.sendOk("Talk to me again when you want to."); 
			cm.dispose(); 
		} 
                if (mode == 1) 
                        status++; 
                else 
                        status--; 
                 
                if (status == 1) { 
                        if (cm.getChar().getMapId() == 209000000) { 
                                cm.sendYesNo("Do you wish to head to where the #bSnow Sprinkler#k is?"); 
                                status = 9; 
                        } else if (cm.getChar().getMapId() == 209080000) { 
                                cm.sendYesNo("Do you wish to return to Happyville?"); 
                                status = 19; 
                        }
                        else {
                                cm.sendOk("You alright?");
                                cm.dispose();
                        }
                } 
                else if (status == 10) { 
                        cm.warp(209080000, 0); 
                        cm.dispose(); 
                } 
                else if (status == 20) { 
                        cm.warp(209000000, 0); 
                        cm.dispose(); 
                }
                
                else {
                        cm.sendOk("You alrighty?");
                        cm.dispose();
                }
        }
} 