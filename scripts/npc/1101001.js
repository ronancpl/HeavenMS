 /* 
	NPC Name: 		Divine Bird
	Map(s): 		Erev
	Description: 		3rd job KoC Buff
*/
importPackage(Packages.constants);

function start() {
    if (cm.getPlayer().isCygnus() && GameConstants.getJobBranch(cm.getJob()) > 2) {
        cm.useItem(2022458);
    }
    
    cm.sendOk("Don't stop training. Every ounce of your energy is required to protect the world of Maple....");
    cm.dispose();
}