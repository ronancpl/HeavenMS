/** 
Happy - Happy ville 
By Ronan
**/ 
var status = -1;
function start() { 
        if(cm.getMap().getMonsters().size() > 1) {  //reactor as a monster? wtf
                cm.sendOk("Defeat all monsters before talking to me."); 
                cm.dispose();
                return;
        }
    
	action(1, 0, 0); 
} 
function action(mode, type, selection) { 
	if (mode == -1) { 
		cm.dispose(); 
	} else {
                if (status == 0 && mode == 0) { 
			cm.sendOk("Talk to me again when you want to."); 
			cm.dispose(); 
		} 
                if (mode == 1) 
                        status++; 
                else 
                        status--; 
                 
                if (status == 0) { 
                        cm.sendSimple("#b<Raid Quest: Happyville>#k\r\nA raid is nothing but joining many people in an attempt to defeat extremely powerful creatures. Here is no different. Here, everyone can take part in defeating the spawned creature. At the moment the are none, what will you do?\r\n#b\r\n#L0#Spawn Kid Snowman.\r\n#L1#Spawn Lost Rudolph.\r\n#L2#Nothing, just chilling.#k");
                } else if(status == 1) {
                        if(selection == 0) {
                                cm.getMap().spawnMonsterOnGroundBelow(9500317, 1700, 80);
                        } else if(selection == 1) {
                                cm.getMap().spawnMonsterOnGroundBelow(9500320, 1700, 80);
                        } else {
                                cm.sendOk("Fine then.");
                        }
                        
                        cm.dispose();
                }
        }
} 