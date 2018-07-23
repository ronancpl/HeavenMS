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
var closeTime = 4 * 60 * 1000; //The time to close the gate
var beginTime = 5 * 60 * 1000; //The time to begin the ride
var  rideTime = 5 * 60 * 1000; //The time that require move to destination

function init() {
    closeTime = em.getTransportationTime(closeTime);
    beginTime = em.getTransportationTime(beginTime);
     rideTime = em.getTransportationTime(rideTime);
    
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
    em.setProperty("docked", "true");
    Orbis_docked.setDocked(true);
    Ludibrium_docked.setDocked(true);
    
    em.setProperty("entry", "true");
    em.schedule("stopEntry", closeTime); //The time to close the gate
    em.schedule("takeoff", beginTime); //The time to begin the ride
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    Orbis_btf.warpEveryone(Train_to_Ludibrium.getId());
    Ludibrium_btf.warpEveryone(Train_to_Orbis.getId());
    Orbis_docked.broadcastShip(false);
    Ludibrium_docked.broadcastShip(false);
    
    em.setProperty("docked","false");
    Orbis_docked.setDocked(false);
    Ludibrium_docked.setDocked(false);
    
    em.schedule("arrived", rideTime); //The time that require move to destination
}

function arrived() {
    Train_to_Orbis.warpEveryone(Orbis_Station.getId(), 0);
    Train_to_Ludibrium.warpEveryone(Ludibrium_Station.getId(), 0);
    Orbis_docked.broadcastShip(true);
    Ludibrium_docked.broadcastShip(true);
    scheduleNew();
}

function cancelSchedule() {}
