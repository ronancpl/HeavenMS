/* @Author SharpAceX
* 5511001.js: Summons Scarlion.
*/

function summonBoss() {
        rm.spawnMonster(9420547,-238,636);
        rm.changeMusic("Bgm09/TimeAttack");
        rm.mapMessage(6, "Beware! The furious Scarlion has shown himself!");
}

function act() {
	if (rm.getReactor().getMap().getMonsterById(9420547) == null) {
		rm.schedule("summonBoss", 3200);
	}
}