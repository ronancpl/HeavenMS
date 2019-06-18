/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Vard
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Ariant Plastic Surgery

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var beauty = 0;
var mface_v = Array(20000, 20004, 20005, 20012, 20013, 20031);
var fface_v = Array(21000, 21003, 21006, 21009, 21012, 21024);
var facenew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function pushIfItemsExists(array, itemidList) {
    for (var i = 0; i < itemidList.length; i++) {
        var itemid = itemidList[i];
        
        if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
            array.push(itemid);
        }
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
                        cm.sendSimple("Ah, welcome to the Ariant Plastic Surgery! Would you like to transform your face into something new? With a #b#t5152030##k or a #b#t5152047##k, I can make your face so much better!\r\n#L1#Plastic Surgery: #i5152030##t5152030##l\r\n#L2#Cosmetic Lens: #i5152047##t5152047##l\r\n#L3#One-time Cosmetic Lenses: #i5152101# (any color)#l");
                } else if (status == 1) {
                        if (selection == 1) {
                                beauty = 0;
                            
                                facenew = Array();
                                if (cm.getChar().getGender() == 0) {
                                        for(var i = 0; i < mface_v.length; i++) {
                                                pushIfItemExists(facenew, mface_v[i] + cm.getChar().getFace()
                                                 % 1000 - (cm.getChar().getFace()
                                                 % 100));
                                        }
                                }
                                if (cm.getChar().getGender() == 1) {
                                        for(var i = 0; i < fface.length; i++) {
                                                pushIfItemExists(facenew, fface[i] + cm.getChar().getFace()
                                                 % 1000 - (cm.getChar().getFace()
                                                 % 100));
                                        }
                                }
                                cm.sendStyle("Hmmm... Face of beauty glows even under cover and burning desert. Choose the face you want, and I will pull out my outstanding skill for the great make over.", facenew);
                        } else if (selection == 2) {
                                beauty = 1;
                                
                                if (cm.getPlayer().getGender() == 0) {
                                        var current = cm.getPlayer().getFace()
                                        % 100 + 20000;
                                }
                                if (cm.getPlayer().getGender() == 1) {
                                        var current = cm.getPlayer().getFace()
                                        % 100 + 21000;
                                }
                                colors = Array();
                                pushIfItemsExists(colors, [current , current + 100, current + 300, current + 600, current + 700]);
                                cm.sendStyle("With the utmost finesse matching that of the sparkling sands of the desert that gleefully embraces the rooftop of the Palace, we will make your eyes shine even brighter with the new lenses. Select the one you want to use...", colors);
                        } else if (selection == 3) {
                                beauty = 3;
                                if (cm.getPlayer().getGender() == 0) {
                                        var current = cm.getPlayer().getFace()
                                        % 100 + 20000;
                                }
                                if (cm.getPlayer().getGender() == 1) {
                                        var current = cm.getPlayer().getFace()
                                        % 100 + 21000;
                                }

                                colors = Array();
                                for (var i = 0; i < 8; i++) {
                                        if (cm.haveItem(5152100 + i)) {
                                               pushIfItemExists(colors, current + 100 * i);
                                        }
                                }

                                if (colors.length == 0) {
                                        cm.sendOk("You don't have any One-Time Cosmetic Lens to use.");
                                        cm.dispose();
                                        return;
                                }

                                cm.sendStyle("What kind of lens would you like to wear? Please choose the style of your liking.", colors);
                        }
                } else if (status == 2){
			cm.dispose();
                        
                        if (beauty == 0) {
                                if (cm.haveItem(5152030) == true){
                                        cm.gainItem(5152030, -1);
                                        cm.setFace(facenew[selection]);
                                        cm.sendOk("Enjoy your new and improved face!");
                                } else {
                                        cm.sendNext("Erm... You don't seem to have the exclusive coupon for this hospital. Without the coupon, I'm afraid I can't do it for you.");
                                }
                        } else if (beauty == 1) {
                                if (cm.haveItem(5152047) == true){
                                        cm.gainItem(5152047, -1);
                                        cm.setFace(colors[selection]);
                                        cm.sendOk("Enjoy your new and improved cosmetic lenses!");
                                } else {
                                        cm.sendOk("Hmm ... it looks like you don't have the coupon specifically for this place. Sorry to say this, but without the coupon, there's no plastic surgery for you...");
                                }
                        } else if (beauty == 3){
                                var color = (colors[selection] / 100) % 100 | 0;

                                if (cm.haveItem(5152100 + color)){
                                        cm.gainItem(5152100 + color, -1);
                                        cm.setFace(colors[selection]);
                                        cm.sendOk("Enjoy your new and improved cosmetic lenses!");
                                } else {
                                        cm.sendOk("I'm sorry, but I don't think you have our cosmetic lens coupon with you right now. Without the coupon, I'm afraid I can't do it for you..");
                                }
                        }
		}
	}
}