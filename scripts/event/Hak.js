importPackage(Packages.tools);

var returnTo = new Array(200000141, 250000100);
var rideTo = new Array(250000100, 200000141);
var birdRide = new Array(200090300, 200090310);
var myRide;
var returnMap;
var map;
var docked;
var timeOnRide = 60; //Seconds
var onRide;

function init() {
}

function setup() {
	var eim = em.newInstance("Hak_" + + em.getProperty("player"));
	return eim;
}

function playerEntry(eim, player) {
	if (player.getMapId() == returnTo[0]) {
		myRide = 0;
	} else {
		myRide = 1;
	}
	docked = eim.getEm().getChannelServer().getMapFactory().getMap(rideTo[myRide]);
    returnMap = eim.getMapFactory().getMap(returnTo[myRide]);
    onRide = eim.getMapFactory().getMap(birdRide[myRide]);
    player.changeMap(onRide, onRide.getPortal(0));
    player.getClient().getSession().write(MaplePacketCreator.getClock(timeOnRide));
    eim.schedule("timeOut", timeOnRide * 1000);
}

function timeOut() {
	onRide.warpEveryone(docked.getId());
}




function playerDisconnected(eim, player) {
    return 0;
}

function cancelSchedule() {}

function dispose() {
    em.cancelSchedule();
}