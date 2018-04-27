/**
 *
 * @author Arnah, Ronan
 */

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.forceCompleteQuest();

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> has been awarded.");
    qm.earnTitle("<" + medalname + "> has been awarded.");
    qm.dispose();
}

function complete(mode, type, selection) {
    qm.forceCompleteQuest();

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> has been awarded.");
    qm.earnTitle("<" + medalname + "> has been awarded.");
    qm.dispose();
}