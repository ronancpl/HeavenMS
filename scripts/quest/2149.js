var status = -1;

function start(mode, type, selection) {
        qm.sendNext("It is said that a old tree gets alive whenever something sinister disturbs this land... We need a hero that fends our village of that creature!");
        qm.forceCompleteQuest();
        qm.dispose();
}
function end(mode, type, selection) {
        qm.dispose();
}