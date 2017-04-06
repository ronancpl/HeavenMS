function start() {
    cm.sendNext("You did a great job back there, " + cm.getPlayer().getName() + ", well done. Now I will transport you back to El Nath. Have the pendant in your possession and talk to me when you feel ready to receive the new skill.");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        cm.warp(211000000,"in01");
        cm.dispose();
    }
}