importPackage(Packages.tools);

var Orbis_btf;
var Train_to_Orbis;
var Orbis_docked;
var Ludibrium_btf;
var Train_to_Ludibrium;
var Ludibrium_docked;
var Orbis_Station;
var Ludibrium_Station;

//Time Setting is in millisecond
var closeTime = 60 * 1000; //The time to close the gate
var beginTime = 60 * 1000; //The time to begin the ride
var rideTime = 60 * 1000; //The time that require move to destination

function init() {
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000122);
    Ludibrium_btf = em.getChannelServer().getMapFactory().getMap(220000111);
    Train_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090110);
    Train_to_Ludibrium = em.getChannelServer().getMapFactory().getMap(200090100);
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000121);
    Ludibrium_docked = em.getChannelServer().getMapFactory().getMap(220000110);
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000100);
    Ludibrium_Station = em.getChannelServer().getMapFactory().getMap(220000100);
    scheduleNew();
}

function scheduleNew() {
    Ludibrium_docked.setDocked(true);
    Orbis_docked.setDocked(true);
    Ludibrium_docked.broadcastMessage(MaplePacketCreator.boatPacket(true));
    Orbis_docked.broadcastMessage(MaplePacketCreator.boatPacket(true));
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}



function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    Ludibrium_docked.setDocked(false);
    Orbis_docked.setDocked(false);
    Ludibrium_docked.broadcastMessage(MaplePacketCreator.boatPacket(false));
    Orbis_docked.broadcastMessage(MaplePacketCreator.boatPacket(false));
    em.setProperty("docked","false");
	Orbis_btf.warpEveryone(Train_to_Ludibrium.getId());
	Ludibrium_btf.warpEveryone(Train_to_Orbis.getId());
    em.schedule("arrived", rideTime);
}

function arrived() {
	Train_to_Orbis.warpEveryone(Orbis_Station.getId());
	Train_to_Ludibrium.warpEveryone(Ludibrium_Station.getId());
    scheduleNew();
}

function cancelSchedule() {
}
