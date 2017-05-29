/*
Credits go to Travis of DeanMS ( xKillsAlotx on RaGEZONE)
Item Exchanger for scrolls

Modified by SharpAceX (Alan) for MapleSolaxia
*/

importPackage(Packages.tools);

var status = 0;
var leaf = 4001126;
var chairs = new Array(3010000, 3010001, 3010002, 3010003, 3010004, 3010005, 3010006, 3010007, 3010008, 3010009, 3010010, 3010011, 3010012, 3010013, 3010015, 3010016, 3010017, 3010018, 3010019, 3010022, 3010023, 3010024, 3010025, 3010026, 3010028, 3010040, 3010041, 3010043, 3010045, 3010046, 3010047,3010057,3010058,3010060,3010061,3010062,3010063, 3010064,3010065,3010066,3010067,3010069,3010071,3010072,3010073,3010080,3010081,3010082,3010083, 3010084,3010085,3010097,3010098,3010099,3010101,3010106,3010116,3011000,3012005,3012010,3012011);
var scrolls = new Array(2040603,2044503,2041024,2041025,2044703,2044603,2043303,2040807,2040806,2040006,2040007,2043103,2043203,2043003,2040506,2044403,2040903,2040709,2040710,2040711,2044303,2043803,2040403,2044103,2044203,2044003,2043703);
var weapons = new Array(1302020, 1302030, 1302033, 1302058, 1302064, 1302080, 1312032, 1322054, 1332025, 1332055, 1332056, 1372034, 1382009, 1382012, 1382039, 1402039, 1412011, 1412027, 1422014, 1422029, 1432012, 1432040, 1432046, 1442024, 1442030, 1442051, 1452016, 1452022, 1452045, 1462014, 1462019, 1462040, 1472030, 1472032, 1472055, 1482020, 1482021, 1482022, 1492020, 1492021, 1492022, 1092030, 1092045, 1092046, 1092047);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0)
            cm.dispose();
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple("Hello#b #h ##k, you currently have #b#c4001126# maple leaves.#k \r\nWhat would you like to do?\r\n#k#L1# Trade 1 leaf for 5,000 NX#l\r\n\#L2# Trade 1 leaf for 1 random chair #l\r\n\#L3# Trade 1 leaf for 3 random Maple Weapons #l\r\n\#L4# Trade 1 leaf for 3 Swiss Cheese and Onyx Apples#l\r\n#L5#Trade 1 leaf for a 10 day Hired Merchant#l");
        } else if (status == 1) {
            if (selection == 1) {
                if(cm.haveItem(leaf, 1)) {
					cm.getPlayer().getCashShop().gainCash(1, 5000);
					cm.getPlayer().announce(MaplePacketCreator.earnTitleMessage("You have earned 5,000 NX"));
                    cm.gainItem(leaf, -1);
                    cm.sendOk("Here is your 5,000 NX!");
					cm.logLeaf("5k NX");
                } else {
                    cm.sendOk("Sorry, you don't have a maple leaf!");
				}
                cm.dispose();
            } else if (selection == 2) {
                if(cm.haveItem(leaf, 1)) {
					var chair1 = chairs[Math.floor(Math.random()*chairs.length)];
					if(cm.canHold(chair1)){
						cm.gainItem(chair1);
						cm.gainItem(leaf, -1);
						cm.sendOk("Here is your random chair!");
						cm.logLeaf("Chair ID: " + chair1);
					} else {
						cm.sendOk("Please make sure you have enough space to hold this chair!");
					}
                 } else {
                    cm.sendOk("Sorry, you don't have a maple leaf!");
				}
                cm.dispose();
			} else if (selection == 3) {
                if(cm.haveItem(leaf, 1)) {
					var weapon1 = weapons[Math.floor(Math.random()*weapons.length)];
					var weapon2 = weapons[Math.floor(Math.random()*weapons.length)];
					var weapon3 = weapons[Math.floor(Math.random()*weapons.length)];
					if(!cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP).isFull(3)) {
						cm.gainItem(weapon1, 1, true, true);
						cm.gainItem(weapon2, 1, true, true);
						cm.gainItem(weapon3, 1, true, true);
						cm.gainItem(leaf, -1);
						cm.sendOk("Here are your 3 random weapons!");
						cm.logLeaf("Maple Weapons IDs: " + weapon1 + "," + weapon2 + "," + weapon3);
					} else {
						cm.sendOk("Please make sure you have enough space to hold these weapons!");
					}
                 } else {
                    cm.sendOk("Sorry, you don't have a maple leaf!");
				}
                cm.dispose();
			} else if (selection == 4) {
                if(cm.haveItem(leaf, 1)) {
					var cheese = 2022273;
					var apple = 2022179;
					if(!cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP).isFull(2)){
						cm.gainItem(apple, 3);
						cm.gainItem(cheese, 3);
						cm.gainItem(leaf, -1);
						cm.sendOk("Here are your 3 cheeses and apples!");
						cm.logLeaf("3 cheeses and apples");
					} else {
						cm.sendOk("Please make sure you have enough space to hold these items!");
					}
                 } else {
                    cm.sendOk("Sorry, you don't have a maple leaf!");
				}
                cm.dispose();
            } else if(selection == 5) {
				if(cm.haveItem(leaf, 1)) {
					if(!cm.haveItem(5030000, 1)) {
						if(!cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.CASH).isFull(1)){
							cm.gainItem(5030000, 1, false, true, 1000 * 60 * 60 * 24 * 10);
							cm.gainItem(leaf, -1);
							cm.sendOk("Here is your Hired Merchant!");
							cm.logLeaf("10 day hired merchant");
						} else {
							cm.sendOk("Please make sure you have enough space to hold these items!");
						}
					} else {
						cm.sendOk("I can't give you a merchant if you already have one!");
					}
				} else {
					cm.sendOk("Sorry, you don't have a maple leaf!");
				}
				cm.dispose();
			} else {
                cm.sendOk("Come back later!");
				cm.dispose();
			}
        }
    }
}