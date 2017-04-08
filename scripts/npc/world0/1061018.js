function start() {
    cm.sendYesNo("If you leave now, you'll have to start over. Are you sure you want to leave?");
}

function action(mode, type, selection) {
    if (mode == 1) {
	cm.warp(105100301);
    }
    cm.dispose();
}