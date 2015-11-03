function enter(pi) {
	if (pi.getPlayer().getLevel() <= 10 && pi.getPlayer().getJob().getId() == 0) {
		var m = pi.getPlayer().getMap().getId();
		var npcid = 0;
	
		if (m == 120000101) { // Navigation Room, The Nautilus
			npcid = 1090000; // Maybe 1090000?
		} else if (m == 102000003) { // Warrior's Sanctuary
			npcid = 1022000;
		}else if (m == 103000003) { // Thieves' Hideout
			npcid = 1052001;
		}else if (m == 100000201) { // Bowman Instructional School
			npcid = 1012100;
		} else if (m == 101000003) { // Magic Library
			npcid = 1032001;
		}
	
		if (npcid != 0) {
			pi.openNpc(npcid);
			return true;
		}
	}
	return false;
}