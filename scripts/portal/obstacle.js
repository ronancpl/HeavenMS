/*
 	Author: Ronan
 	Map: Mushroom Castle - Deep Inside Mushroom Forest (106020300)
 	Right Portal
 */
 
 function enter(pi){
 	if (pi.isQuestCompleted(2316)) {
 		if (pi.hasItem(2430014)) {
 			pi.gainItem(2430014, -1 * pi.getPlayer().getItemQuantity(2430014, false));
                        pi.message("You have used the Killer Mushroom Spore to open the way.");
 		}
 
 		pi.playPortalSound(); pi.warp(106020400, 2);
 		return true;
 	} else if (pi.hasItem(2430015)) {
                pi.gainItem(2430015, -1 * pi.getPlayer().getItemQuantity(2430015, false));
                pi.message("You have used the Thorn Remover to clean the way.");
                
                pi.playPortalSound(); pi.warp(106020400, 2);
                return true;
        }

        pi.message("The overgrown vines is blocking the way.");
 	return false;
 } 