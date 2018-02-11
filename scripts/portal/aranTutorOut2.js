function enter(pi) {
	//lol nexon does this xD
	pi.teachSkill(20000014, 0, -1, -1);
	pi.teachSkill(20000015, 0, -1, -1);
	//nexon sends updatePlayerStats MapleStat.AVAILABLESP 0
	pi.teachSkill(20000014, 1, 0, -1);
	pi.teachSkill(20000015, 1, 0, -1);
	//actually nexon does enableActions here :P
	pi.playPortalSound(); pi.warp(914000210, 1);
	return true;
}