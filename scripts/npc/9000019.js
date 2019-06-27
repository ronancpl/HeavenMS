var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if(status == 0) {
                        if(cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.ETC).getNumFreeSlot() < 1) {
                                cm.sendNext("Check for a available slot on your ETC inventory.");
                                cm.dispose();
                                return;
                        }
                    
                        cm.getClient().announce(Packages.tools.MaplePacketCreator.openRPSNPC());
                        cm.dispose();
                }
        }
}