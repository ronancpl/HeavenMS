var status = -1;

function start(mode, type, selection) {
        qm.sendNext("That tree... I've heard of it before, I even studied its behavior! If I recall correctly, the #bStumpy#k comes alive when the soil deems infertile by some sort of magic, and those stumps who evolves under these conditions starts to drain these suspicious magical sources instead of water and minerals for living, which makes them very threathening to people and villages nearby.");
        qm.forceCompleteQuest();
        qm.dispose();
}
function end(mode, type, selection) {
        qm.dispose();
}