/*
	QUEST: Where's Violetta?
	NPC: none
*/
importPackage(Packages.server.life);

var status = -1;
var timeLimit = 10; //10 minutes
var eventTimer = 1000 * 60 * timeLimit;
var mobId = 3300008; //Prime Minister

function start(mode, type, selection){
	if(mode == -1 || (mode == 0 && status == 0)){
		qm.dispose();
		return;
	}
	else if(mode == 0)
		status--;
	else
		status++;


	if(status == 0){
		qm.sendAcceptDecline("Please help me!");
	}
	else if(status == 1){
		qm.sendNext("The #bPrime Minister#k is the one who plotted all this! Oh no! Here he comes...");
	}
	else if (status == 2){
		qm.forceStartQuest();
		var eim = qm.getEventInstance();
		eim.startEventTimer(eventTimer);
		qm.getPlayer().getMap().getPortal(1).setPortalState(false);
		qm.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new java.awt.Point(292, 143));
		qm.dispose();
	}
}

function end(mode, type, selection){
	if(mode == -1 || (mode == 0 && status == 0)){
		qm.dispose();
		return;
	}
	else if(mode == 0)
		status--;
	else
		status++;


	if(status == 0){
		qm.sendNext("Hurray! #b#h ##k you defeated the #bPrime Minister#k.");
	}
	else if(status == 1){
		qm.gainExp(15000);
		qm.forceCompleteQuest();

		var eim = qm.getEventInstance();
		qm.getPlayer().getMap().getPortal(1).setPortalState(true);
		eim.stopEventTimer();
		eim.warpEventTeam(106021600);

		eim.dispose();
		qm.dispose();
	}
}