var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if(cm.getPlayer().getItemQuantity(1902016, true) > 0) {
        cm.warp(140010210, 0);
    } else {
        cm.sendOk("What is it? If you you're here to waste my time, get lost!");
    }
    
    cm.dispose();
}