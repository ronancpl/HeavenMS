var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            qm.sendNext("That tree... I've heard of it before, I even studied its behavior! If I recall correctly, the #bStumpy#k comes alive when the soil deems infertile by some sort of magic, and those stumps who evolves under these conditions starts to drain these suspicious magical sources instead of water and minerals for living, which makes them very threathening to people and villages nearby.");
            qm.forceCompleteQuest();
        } else if (status == 1) {
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
        qm.dispose();
}