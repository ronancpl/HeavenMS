/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Shati
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Assistant Hairdresser

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var beauty = 0;
var mhair_r = Array(30150, 30170, 30180, 30320, 30330, 30410, 30460, 30680, 30800, 30820, 30900);
var fhair_r = Array(31090, 31190, 31330, 31340, 31400, 31420, 31520, 31620, 31650, 31660, 34000);
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
                if (type == 7) {
                        cm.sendNext("I guess you aren't ready to make the change yet. Let me know when you are!");
                }
                
		cm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendSimple("Hey there! I'm Shatti, and I'm Mazra's apprentice. If you have #bAriant hair style coupon(REG)#k or #bAriant hair color coupon(REG)#k with you, how about allowing me to work on your hair? \r\n#L0#Haircut: #i5150026##t5150026##l\r\n#L1#Dye your hair: #i5151021##t5151021##l");
		} else if (status == 1) {
			if (selection == 0) {
				beauty = 1;
				hairnew = Array();
				if (cm.getChar().getGender() == 0) {
					for(var i = 0; i < mhair_r.length; i++) {
						pushIfItemExists(hairnew, mhair_r[i] + parseInt(cm.getChar().getHair()
 % 10));
					}
				} 
				if (cm.getChar().getGender() == 1) {
					for(var i = 0; i < fhair_r.length; i++) {
						pushIfItemExists(hairnew, fhair_r[i] + parseInt(cm.getChar().getHair()
 % 10));
					}
				}
				cm.sendYesNo("If you use the REG coupon, your hairstyle will be changed to a random new look. You'll also have access to new hairstyles I worked on that's not available for VIP coupons. Would you like to use #bAriant hair style coupon(REG)#k for a fabulous new look?");
			} else if (selection == 1) {
				beauty = 2;
				haircolor = Array();
				var current = parseInt(cm.getChar().getHair()
/10)*10;
				for(var i = 0; i < 8; i++) {
					pushIfItemExists(haircolor, current + i);
				}
				cm.sendYesNo("If you use the regular coupon, your hair color will change to a random new color. Are you sure you want to use #b#t5151021##k and randomly change your hair color?");
			}
		}
		else if (status == 2){
			cm.dispose();
			if (beauty == 1){
				if (cm.haveItem(5150026) == true){
					cm.gainItem(5150026, -1);
					cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
					cm.sendOk("Enjoy your new and improved hairstyle!");
				} else {
					cm.sendNext("I can only change your hairstyle if you bring me the coupon. You didn't forget that, did you?");
				}
			}
			if (beauty == 2){
				if (cm.haveItem(5151021) == true){
					cm.gainItem(5151021, -1);
					cm.setHair(haircolor[Math.floor(Math.random() * haircolor.length)]);
					cm.sendOk("Enjoy your new and improved haircolor!");
				} else {
					cm.sendNext("I can only change your hairstyle if you bring me the coupon. You didn't forget that, did you?");
				}
			}
		}
	}
}