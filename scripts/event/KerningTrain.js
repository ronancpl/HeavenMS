importPackage(Packages.tools);

var returnTo = new Array(103000100, 103000310);
var rideTo = new Array(103000310, 103000100);
var trainRide = new Array(103000301, 103000302);
var myRide;
var returnMap;
var exitMap;
var map;
var timeOnRide = 10; //Seconds
var onRide;

function init() {}

function setup() {
	var eim = em.newInstance("KerningTrain_" + em.getProperty("player"));
	return eim;
}

function afterSetup(eim) {}

function playerEntry(eim, player) {
	if (player.getMapId() == returnTo[0]) {
		myRide = 0;
	} else {
		myRide = 1;
	}
        
	exitMap = eim.getEm().getChannelServer().getMapFactory().getMap(rideTo[myRide]);
        returnMap = eim.getMapFactory().getMap(returnTo[myRide]);
        onRide = eim.getMapFactory().getMap(trainRide[myRide]);
        player.changeMap(onRide, onRide.getPortal(0));
        player.getClient().getSession().write(MaplePacketCreator.getClock(timeOnRide));
        eim.schedule("timeOut", timeOnRide * 1000);
}

function timeOut(eim) {
	end(eim);
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player, success) {
        eim.unregisterPlayer(player);
        player.changeMap(success ? exitMap.getId() : returnMap.getId(), 0);
}

function end(eim) {
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim, party.get(i), true);
        }
        eim.dispose();
}

function playerDisconnected(eim, player) {
        playerExit(eim, player, false);
}

function cancelSchedule() {}

function dispose(eim) {}
