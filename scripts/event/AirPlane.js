importPackage(Packages.tools);

//Time Setting is in millisecond
var closeTime = 24 * 1000; //The time to close the gate
var beginTime = 30 * 1000; //The time to begin the ride
var rideTime = 15 * 1000; //The time that require move to destination
var KC_bfd;
var Plane_to_CBD;
var CBD_docked;
var CBD_bfd;
var Plane_to_KC;
var KC_docked;

function init() {
    KC_bfd = em.getChannelServer().getMapFactory().getMap(540010100);
    CBD_bfd = em.getChannelServer().getMapFactory().getMap(540010001);
    Plane_to_CBD = em.getChannelServer().getMapFactory().getMap(540010101);
    Plane_to_KC = em.getChannelServer().getMapFactory().getMap(540010002);
    CBD_docked = em.getChannelServer().getMapFactory().getMap(540010000);
    KC_docked = em.getChannelServer().getMapFactory().getMap(103000000);
    scheduleNew();
}

function scheduleNew() {
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    em.setProperty("docked","false");
    KC_bfd.warpEveryone(Plane_to_CBD.getId());
    CBD_bfd.warpEveryone(Plane_to_KC.getId());
    em.schedule("arrived", rideTime); //The time that require move to destination
}

function arrived() {
    Plane_to_CBD.warpEveryone(CBD_docked.getId(), 0);
    Plane_to_KC.warpEveryone(KC_docked.getId(), 7);
        
    scheduleNew();
}

function cancelSchedule() {
}
