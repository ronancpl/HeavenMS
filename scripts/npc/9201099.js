/**
 *9201098 - Mo
 *@author Ronan
 */
 
function start() {
    if(cm.getQuestStatus(8224) == 2) {
        cm.openShopNPC(9201099);
    } else {
        cm.sendOk("Hm, at who do you think you are looking at?");
    }
    
    cm.dispose();
}
