var status = -1;

function start(mode, type, selection) {
        qm.sendNext("Some bats seems to accompany this tree wherever it goes. Creepy...");
        qm.forceCompleteQuest();
        qm.dispose();
}
function end(mode, type, selection) {
        qm.dispose();
}