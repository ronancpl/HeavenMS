/*
	NPC: Blocked Entrance (portal?)
	MAP: Mushroom Castle - East Castle Tower (106021400)
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
	else if(mode == 0 && status == 0){
		cm.dispose();
		return;
	}
	else if(mode == 0)
		status--;
	else
		status++;


        if(cm.getMapId() == 106021402) {
                if(status == 0){
                        cm.sendSimple("#L0#Enter to fight #bKing Pepe#k and #bYeti Brothers#k.#l\r\n#L1#Enter to fight #bPrime Minister#k.#l");
                }
                else if(status == 1){
                        if(selection == 0){
                                var pepe = cm.getEventManager("KingPepeAndYetis");
                                pepe.setProperty("player", cm.getPlayer().getName());
                                pepe.startInstance(cm.getPlayer());
                                cm.dispose();
                                return;
                        }
                    
                        else if(selection == 1){
                                var pm = cm.getEventManager("MK_PrimeMinister2");
                                pm.setProperty("player", cm.getPlayer().getName());
                                pm.startInstance(cm.getPlayer());
                                
                                cm.dispose();
                                return;
                        }
                }
        } else {
                if(status == 0){
                        cm.sendSimple("#L1#Enter to fight #bKing Pepe#k and #bYeti Brothers#k.#l");
                }
                else if(status == 1){
                        if(selection == 1){
                                var pepe = cm.getEventManager("KingPepeAndYetis");
                                pepe.setProperty("player", cm.getPlayer().getName());
                                pepe.startInstance(cm.getPlayer());
                                cm.dispose();
                                return;
                        }
                }
        }
}