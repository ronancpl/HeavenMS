var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.sendOk("I hope for this travel to be a safe one, and that we get to live on a more peaceful place there... Hey, darling, let's go.");
    cm.dispose();
}