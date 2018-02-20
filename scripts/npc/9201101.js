/**
 *9201101 - T-1337
 *@author Ronan
 */
 
function start() {
    if(Packages.server.MapleShopFactory.getInstance().getShop(9201101) != null) {
        cm.openShopNPC(9201101);
    } else {
        cm.sendOk("The patrol in New Leaf City is always ready. No creatures are able to break through to the city.");
    }
    
    cm.dispose();
}
