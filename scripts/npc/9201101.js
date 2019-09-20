/**
 *9201101 - T-1337
 *@author Ronan
 */
 
function start() {
    if (Packages.config.YamlConfig.config.server.USE_ENABLE_CUSTOM_NPC_SCRIPT) {
        cm.openShopNPC(9201101);
    } else {
        //cm.sendOk("The patrol in New Leaf City is always ready. No creatures are able to break through to the city.");
        cm.sendDefault();
    }

    cm.dispose();
}
