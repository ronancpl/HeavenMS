/*
Dragon nest
*/

function sendToHeaven() {
    rm.spawnNpc(2081008);
    rm.startQuest(100203);
    rm.mapMessage(6, "In a flicker of light the egg has matured and cracked, thus born a radiant baby dragon.");
}

function touch() {
    if(rm.haveItem(4001094) && rm.getReactor().getState() == 0) {
        rm.hitReactor();
        rm.gainItem(4001094, -1);
    }
}

function untouch() {}

function act() {
    sendToHeaven();     // thanks Conrad for pointing out the GMS-like way of Nine Spirit's Nest
}