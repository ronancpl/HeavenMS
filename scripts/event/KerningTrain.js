importPackage(Packages.tools);

var returnTo = new Array(103000100, 103000310);
var rideTo = new Array(103000310, 103000100);
var trainRide = new Array(103000301, 103000302);
var myRide;
var returnMap;
var map;
var docked;
var timeOnRide = 10; //Seconds
var onRide;

function init() {
}

function setup() {
	var eim = em.newInstance("KerningTrain_" + em.getProperty("player"));
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
    onRide = eim.getMapFactory().getMap(trainRide[myRide]);
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