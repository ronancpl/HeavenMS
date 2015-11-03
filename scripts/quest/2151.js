var status = -1;

function start(mode, type, selection) {
        qm.sendNext("The tree has a strange carving that resembles a scary face.");
        qm.forceCompleteQuest();
        qm.dispose();
}
function end(mode, type, selection) {
        qm.dispose();
}