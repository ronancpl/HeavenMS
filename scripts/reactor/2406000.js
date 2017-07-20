/*
Dragon nest
*/

function sendToHeaven() {
    rm.destroyNpc(2081008);
    rm.mapMessage(6, "In a flicker of light, Nine Spirit's Little Dragon returns to the place it belongs, high above the skies.");
    rm.getReactor().getMap().resetReactors();
}

function touch() {
    if(rm.haveItem(4001094) && rm.getReactor().getState() == 0) {
        rm.hitReactor();
        rm.gainItem(4001094, -1);
    }
}

function untouch() {}

function act() {
    rm.spawnNpc(2081008);
    rm.schedule("sendToHeaven", 12 * 1000);
}