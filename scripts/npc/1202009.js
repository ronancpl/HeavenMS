var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.sendOk("Up ahead is the #rMirror Cave#k. Only the chosen ones have permission to access that place.");
    cm.dispose();
}