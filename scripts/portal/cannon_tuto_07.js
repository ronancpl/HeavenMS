function enter(pi) {
	pi.setDirectionStatus(true);
	pi.lockUI2();
	pi.spawnNPC(579711, 1096012, -51, -97, 0, true);//A2 01 01 7F D8 08 00 4C B9 10 00 CD FF 9F FF 01 8A 00 9B FF FF FF 01
	pi.setNPCValue(579711, "summon");
	pi.updateInfo("fly", "579711");
	pi.sendDirectionInfo(3, 0);
	pi.sendDirectionInfo(3, 2);
	pi.sendDirectionInfo(4, 0);
	return true;
}