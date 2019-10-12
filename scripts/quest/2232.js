var status = -1;

function start(mode, type, selection) {
    var familyEntry = qm.getPlayer().getFamilyEntry();
    if (familyEntry != null && familyEntry.getJuniorCount() > 0) {
        qm.forceCompleteQuest();
        qm.gainExp(3000);
        qm.sendNext("Good job!");
    } else {
        qm.sendNext("I see that you have not successfully find a Junior, ok?");
    }
    qm.dispose();
}

function end(mode, type, selection) {
    var familyEntry = qm.getPlayer().getFamilyEntry();
    if (familyEntry != null && familyEntry.getJuniorCount() > 0) {  // script found thanks to kvmba
        qm.forceCompleteQuest();
        qm.gainExp(3000);
        qm.sendNext("Good job!");
    } else {
        qm.sendNext("I see that you have not successfully find a Junior, ok?");
    }
    qm.dispose();
}