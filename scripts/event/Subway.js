importPackage(Packages.tools);

var KC_Waiting;
var Subway_to_KC;
var KC_docked;
var NLC_Waiting;
var Subway_to_NLC;
var NLC_docked;

//Time Setting is in millisecond
var closeTime =     50 * 1000; //The time to close the gate
var beginTime = 1 * 60 * 1000; //The time to begin the ride
var  rideTime = 4 * 60 * 1000; //The time that require move to destination

function init() {
    closeTime = em.getTransportationTime(closeTime);
    beginTime = em.getTransportationTime(beginTime);
     rideTime = em.getTransportationTime(rideTime);
    
    KC_Waiting = em.getChannelServer().getMapFactory().getMap(600010004);
    NLC_Waiting = em.getChannelServer().getMapFactory().getMap(600010002);
    Subway_to_KC = em.getChannelServer().getMapFactory().getMap(600010003);
    Subway_to_NLC = em.getChannelServer().getMapFactory().getMap(600010005);
    KC_docked = em.getChannelServer().getMapFactory().getMap(103000100);
    NLC_docked = em.getChannelServer().getMapFactory().getMap(600010001);
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
    //sound src: https://www.soundjay.com/transportation/metro-door-close-01.mp3
    KC_docked.broadcastMessage(MaplePacketCreator.playSound("subway/whistle"));
    NLC_docked.broadcastMessage(MaplePacketCreator.playSound("subway/whistle"));
    
    em.setProperty("docked","false");
    KC_Waiting.warpEveryone(Subway_to_NLC.getId());
    NLC_Waiting.warpEveryone(Subway_to_KC.getId());
    em.schedule("arrived", rideTime);
}

function arrived() {
    Subway_to_KC.warpEveryone(KC_docked.getId(), 0);
    Subway_to_NLC.warpEveryone(NLC_docked.getId(), 0);
    scheduleNew();
    
    KC_docked.broadcastMessage(MaplePacketCreator.playSound("subway/whistle"));
    NLC_docked.broadcastMessage(MaplePacketCreator.playSound("subway/whistle"));
}

function cancelSchedule() {}
