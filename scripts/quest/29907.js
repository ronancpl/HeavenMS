
var status = -1;

function start(mode, type, selection) {
	if ((qm.getPlayer().getJob().getId() > 1000 && qm.getPlayer().getJob().getId() < 2000) && qm.getPlayer().getJob().getId() % 100 == 10) {
		qm.forceStartQuest();
	}
	qm.dispose();
}

function end(mode, type, selection) {
	if (qm.canHold(1142067) && !qm.haveItem(1142067) && qm.getPlayer().getJob().getId() > 1000 && qm.getPlayer().getJob().getId() % 100 > 0 && qm.getPlayer().getJob().getId() < 2000) {
		qm.gainItem(1142067,1);
		qm.forceStartQuest();
		qm.forceCompleteQuest();
	}
	qm.dispose();
}