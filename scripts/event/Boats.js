importPackage(Packages.client);
importPackage(Packages.tools);
importPackage(Packages.server.life);

//Time Setting is in millisecond
var closeTime = 50 * 1000; //The time to close the gate
var beginTime = 60 * 1000; //The time to begin the ride
var rideTime = 120 * 1000; //The time that require move to destination
var invasionTime = 30 * 1000; //The time that spawn balrog
var Orbis_btf;
var Boat_to_Orbis;
var Orbis_Boat_Cabin;
var Orbis_docked;
var Ellinia_btf;
var Ellinia_Boat_Cabin;
var Ellinia_docked;

function init() {
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000112);
    Ellinia_btf = em.getChannelServer().getMapFactory().getMap(101000301);
    Boat_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090010);
    Boat_to_Ellinia = em.getChannelServer().getMapFactory().getMap(200090000);
    Orbis_Boat_Cabin = em.getChannelServer().getMapFactory().getMap(200090011);
    Ellinia_Boat_Cabin = em.getChannelServer().getMapFactory().getMap(200090001);
    Ellinia_docked = em.getChannelServer().getMapFactory().getMap(101000300);
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000100);
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000111);
    OBoatsetup();
    EBoatsetup();
    scheduleNew();
}

function scheduleNew() {
    Ellinia_docked.setDocked(true);
    Orbis_docked.setDocked(true);
    Ellinia_docked.broadcastMessage(MaplePacketCreator.boatPacket(true));
    Orbis_docked.broadcastMessage(MaplePacketCreator.boatPacket(true));
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.setProperty("haveBalrog","false");
    em.schedule("stopentry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopentry() {
    em.setProperty("entry","false");
    Orbis_Boat_Cabin.resetReactors();
    Ellinia_Boat_Cabin.resetReactors();
}



function takeoff() {
    em.setProperty("docked","false");
	Orbis_btf.warpEveryone(Boat_to_Ellinia.getId());
	Ellinia_btf.warpEveryone(Boat_to_Orbis.getId());
    Ellinia_docked.setDocked(false);
    Orbis_docked.setDocked(false);
    Ellinia_docked.broadcastMessage(MaplePacketCreator.boatPacket(false));
    Orbis_docked.broadcastMessage(MaplePacketCreator.boatPacket(false));
    em.schedule("invasion", invasionTime);
    em.schedule("arrived", rideTime);
}

function arrived() {
	Boat_to_Orbis.warpEveryone(Orbis_Station.getId());
	Orbis_Boat_Cabin.warpEveryone(Orbis_Station.getId());
	Boat_to_Ellinia.warpEveryone(Ellinia_docked.getId());
	Ellinia_Boat_Cabin.warpEveryone(Ellinia_docked.getId());
    Boat_to_Orbis.killAllMonsters();
    Boat_to_Ellinia.killAllMonsters();
    scheduleNew();
}

function invasion() {
    var numspawn = 2;
    if(numspawn > 0) {
        for(var i=0; i < numspawn; i++) {
            Boat_to_Orbis.spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8150000), new java.awt.Point(485, -221));
            Boat_to_Ellinia.spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8150000), new java.awt.Point(-590, -221));
        }
        Boat_to_Orbis.setDocked(true);
        Boat_to_Ellinia.setDocked(true);
        Boat_to_Orbis.broadcastMessage(MaplePacketCreator.boatPacket(true));
        Boat_to_Ellinia.broadcastMessage(MaplePacketCreator.boatPacket(true));
        Boat_to_Orbis.broadcastMessage(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
        Boat_to_Ellinia.broadcastMessage(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
        em.setProperty("haveBalrog","true");
    }
}

function OBoatsetup() {
    em.getChannelServer().getMapFactory().getMap(200090011).getPortal("out00").setScriptName("OBoat1");
    em.getChannelServer().getMapFactory().getMap(200090011).getPortal("out01").setScriptName("OBoat2");
}

function EBoatsetup() {
    em.getChannelServer().getMapFactory().getMap(200090001).getPortal("out00").setScriptName("EBoat1");
    em.getChannelServer().getMapFactory().getMap(200090001).getPortal("out01").setScriptName("EBoat2");
}

function cancelSchedule() {
}