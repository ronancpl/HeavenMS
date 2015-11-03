var status = -1;

function end(mode, type, selection) {
        qm.sendNext("Oh, you brought it back! Thank you so much!");
        
        qm.gainItem(2210006, -1);
        qm.gainExp(7500 * qm.getPlayer().getExpRate());
        qm.gainMeso(30000 * qm.getPlayer().getMesoRate());
        qm.gainFame(3);
        qm.forceCompleteQuest();
        qm.dispose();
}