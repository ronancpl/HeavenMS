var status = -1;

function start(mode, type, selection) {
        qm.sendNext("The tree has a scarf upon its branches, I tell you.");
        qm.forceCompleteQuest();
        qm.dispose();
}
function end(mode, type, selection) {
        qm.dispose();
}