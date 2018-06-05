/**
 *
 * @author Arnah, Ronan
 */

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.forceCompleteQuest();

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> is not coded.");
    qm.earnTitle("<" + medalname + "> has been awarded.");
    qm.dispose();
}

function end(mode, type, selection) {
    qm.forceCompleteQuest();

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> is not coded.");
    qm.earnTitle("<" + medalname + "> has been awarded.");
    qm.dispose();
}