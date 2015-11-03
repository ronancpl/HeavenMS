function start(ms) {
	ms.playSound("cannonshooter/flying");
    ms.sendDirectionInfo("Effect/Direction4.img/effect/cannonshooter/balloon/0", 9000, 0, 0, 0, -1);
    ms.sendDirectionInfo(1, 1500);
    ms.setDirectionStatus(true);
}