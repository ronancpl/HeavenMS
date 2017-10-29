var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.sendOk("We are departing to #bVictoria Island#k briefly. I've heard the #rBlack Mage#k himself cannot take that place on his grasp yet, thanks to #bthe seal that has been casted on that area#k. We pray for their safety, but if fortune does not favor the Heroes, at least we will be safe once we reach the continent.");
    cm.dispose();
}