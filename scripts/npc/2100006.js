/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Mazra
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Hair Salon Owner

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var beauty = 0;
var mhair_v = Array(30150, 30170, 30180, 30320, 30330, 30410, 30460, 30820, 30900);
var fhair_v = Array(31040, 31090, 31190, 31330, 31340, 31400, 31420, 31620, 31660);
var hairnew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
		cm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendSimple("Hahaha... it takes a lot of style and flair for someone to pay attention to his or her hairsyle in a desert. Someone like you...If you have #bAriant hair style coupon(VIP)#k or #bAriant hair color coupon(VIP)#k, I'll give your hair a fresh new look. \r\n#L0#Haircut: #i5150027##t5150027##l\r\n#L1#Dye your hair: #i5151022##t5151022##l");
		} else if (status == 1) {
			if (selection == 0) {
				beauty = 1;
				hairnew = Array();
				if (cm.getChar().getGender() == 0) {
					for(var i = 0; i < mhair_v.length; i++) {
						pushIfItemExists(hairnew, mhair_v[i] + parseInt(cm.getChar().getHair()
 % 10));
					}
				} 
				if (cm.getChar().getGender() == 1) {
					for(var i = 0; i < fhair_v.length; i++) {
						pushIfItemExists(hairnew, fhair_v[i] + parseInt(cm.getChar().getHair()
 % 10));
					}
				}
				cm.sendStyle("Hahaha~all you need is #bAriant hair style coupon(VIP)#k to change up your hairstyle. Choose the new style, and let me do the rest.", hairnew);
			} else if (selection == 1) {
				beauty = 2;
				haircolor = Array();
				var current = parseInt(cm.getChar().getHair()
/10)*10;
				for(var i = 0; i < 8; i++) {
					pushIfItemExists(haircolor, current + i);
				}
				cm.sendStyle("Every once in a while, it doesn't hurt to change up your hair color... it's fun. Allow me, the great Mazra, to dye your hair, so you just bring me #bAriant hair color coupon(VIP)#k, and choose your new hair color.", haircolor);
			}
		}
		else if (status == 2){
			cm.dispose();
			if (beauty == 1){
				if (cm.haveItem(5150027) == true){
					cm.gainItem(5150027, -1);
					cm.setHair(hairnew[selection]);
					cm.sendOk("Enjoy your new and improved hairstyle!");
				} else {
					cm.sendNext("I thought I told you, you need the coupon in order for me to work magic on your hair check again.");
				}
			}
			if (beauty == 2){
				if (cm.haveItem(5151022) == true){
					cm.gainItem(5151022, -1);
					cm.setHair(haircolor[selection]);
					cm.sendOk("Enjoy your new and improved haircolor!");
				} else {
					cm.sendNext("I thought I told you, you need the coupon in order for me to work magic on your hair check again.");
				}
			}
		}
	}
}