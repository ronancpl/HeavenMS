/*
 	Author: Kevin
 	Map: Mushroom Castle - Deep Inside Mushroom Forest (106020300)
 	Right Portal
 */
 
 function enter(pi){
 	if (pi.isQuestCompleted(2316)){
 		if (pi.hasItem(2430014)){
 			pi.gainItem(2430014, -1 * pi.getPlayer().getItemQuantity(2430014, false));
 		}
 
 		pi.warp(106020400, 2);
 		return true;
 	}
 
 	return false;
 } 