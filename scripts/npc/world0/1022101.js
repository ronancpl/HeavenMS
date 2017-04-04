/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Rooney
	Map Name (Map ID)
	Used to exchange VP for Maple Leaves, and Maple Leaves for rewards.
 */

var itemToUse = 4001126;

var chairs = new Array(3010000, 3010001, 3010002, 3010003, 3010004, 3010005, 3010006, 3010007, 3010008, 3010009, 3010010, 3010011, 3010012, 3010013, 3010015, 3010016, 3010017, 3010018, 3010019, 3010022, 3010023, 3010024, 3010025, 3010026, 3010028, 3010040, 3010041, 3010043, 3010045, 3010046, 3010047,3010057,3010058,3010060,3010061,3010062,3010063, 3010064,3010065,3010066,3010067,3010069,3010071,3010072,3010073,3010080,3010081,3010082,3010083, 3010084,3010085,3010097,3010098,3010099,3010101,3010106,3010116,3011000,3012005,3012010,3012011);
var scrolls = new Array(2040603,2044503,2041024,2041025,2044703,2044603,2043303,2040807,2040806,2040006,2040007,2043103,2043203,2043003,2040506,2044403,2040903,2040709,2040710,2040711,2044303,2043803,2040403,2044103,2044203,2044003,2043703);
var weapons = new Array(1302020, 1302030, 1302033, 1302058, 1302064, 1302080, 1312032, 1322054, 1332025, 1332055, 1332056, 1372034, 1382009, 1382012, 1382039, 1402039, 1412011, 1412027, 1422014, 1422029, 1432012, 1432040, 1432046, 1442024, 1442030, 1442051, 1452016, 1452022, 1452045, 1462014, 1462019, 1462040, 1472030, 1472032, 1472055, 1482020, 1482021, 1482022, 1492020, 1492021, 1492022, 1092030, 1092045, 1092046, 1092047);

var nxAmount = 3000;
var chairAmount = 2;
var weaponAmount = 2;
var buffAmount = 2;
var hiredMerchantLength = 7;

var buff1ID = 2022273;
var buff2ID = 2022179;
var status;
var vp;
var choice;
 
function start() {
	//vp = cm.getClient().getVotePoints();
	//if(vp == null)
		vp = 0;
	
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0 && mode == 1) {
			if(cm.getPlayer().getLevel() < 20) {
				cm.sendOk("Hello, I am the Vote Point exchanger for #rMapleSolaxia#k!\r\n\r\nI am sorry, but I can only exchange Vote Points for players #blevel 20 or over#k.");
				cm.dispose();
				return;
			}
			var outStr = "Hello, I am the Vote Point exchanger for #rMapleSolaxia#k!\r\n";
			outStr += "You currently have #r#c" + itemToUse + "##k #t" + itemToUse + "# and #r" + vp + "#k Vote Points.#b\r\n\r\n";
			outStr += "#L0#I would like to exchange my vote points for Maple Leaves#l\r\n";
			outStr += "#L1#I would like to exchange 1 #t" + itemToUse + "# for "+ nxAmount + " NX Cash#l\r\n";
			outStr += "#L2#I would like to exchange 1 #t" + itemToUse + "# for " + chairAmount + " Random Chair" + (chairAmount > 1 ? "s" : "") + "#l\r\n";
			outStr += "#L3#I would like to exchange 1 #t" + itemToUse + "# for " + weaponAmount + " Maple Weapons#l\r\n";
			outStr += "#L4#I would like to exchange 1 #t" + itemToUse + "# for " + buffAmount + " #t" + buff1ID + "#s and " + buffAmount + " #t" + buff2ID + "#s#l\r\n";
			outStr += "#L5#I would like to exchange 1 #t" + itemToUse + "# for a " + hiredMerchantLength + " Day Hired Merchant#l\r\n";
			cm.sendSimple(outStr);
		} else if(status == 1) {
			choice = selection;
			
			if(selection > 0) {
				if(!cm.haveItem(itemToUse) && vp == 0) {
					cm.sendOk("I'm sorry, but you don't have any #t" +itemToUse + " or Vote Points.");
					cm.dispose();
					return;
				}
			}
			
			if(selection == 0) {
				// Exchange VP for leaves
				if(vp <= 0) {
					cm.sendOk("I'm sorry, but you don't have any Vote Points to exchange!");
					cm.dispose();
					return;
				}
				cm.sendYesNo("Would you like to exchange " + vp + " Vote Point" + (vp > 0 ? "s" : "") + " for " + vp + " #t" + itemToUse + "# " + (vp > 0 ? "s" : "") + "?");
			} else if(selection == 1) {
				// Exchange 1 Leaf for Cash
				cm.sendYesNo("Would you like to exchange 1 #t" + itemToUse + "# for " + nxAmount + " NX Cash?");
			} else if(selection == 2) {
				// Exchange 1 Leaf for Chair
				cm.sendYesNo("Would you like to exchange 1 #t" + itemToUse + "# for " + chairAmount + " Random Chair" + (chairAmount > 1 ? "s" : "") + "?");
			} else if(selection == 3) {
				// Exchange 1 Leaf for Maple Weapons
				cm.sendYesNo("Would you like to exchange 1 #t" + itemToUse + "# for " + weaponAmount + " Random Maple Weapons?");
			} else if(selection == 4) {
				// Exchange 1 Leaf for Apples/Cheese
				cm.sendYesNo("Would you like to exchange 1 #t" + itemToUse + "# for " + buffAmount + " #t" + buff1ID + "# and #t" + buff2ID + "#?");
			} else if(selection == 5) {
				// Echange 1 Leaf for Merchant
				cm.sendYesNo("Would you like to exchange 1 #t" + itemToUse + "# for a " + hiredMerchantLength + " Day Hired Merchant?");
			} else {
				cm.dispose();
			}
		} else if(status == 2) {
			var useVP = false;
			if(!cm.hasItem(itemToUse) && vp > 0)
				useVP = true;
				
			if(choice == 0) {
				// VP Exchange
				if(!cm.canHold(itemToUse)) {
					cm.sendOk("It looks like you don't have enough space in your #rETC#k inventory to hold the #t" + itemToUse + "#" + (vp > 0 ? "s" : "") + ".");
					cm.dispose();
					return;
				}
				
				cm.getClient().useVotePoints(vp);
				cm.gainItem(itemToUse, vp);
				cm.dispose();
			} else if(choice == 1) {
				// Leaf for Cash
				if(useVP)
					cm.getClient().useVotePoints(1);
				else
					cm.gainItem(itemToUse, -1);
					
				cm.getPlayer().getCashShop().gainCash(1, nxAmount);
				cm.getPlayer().announce(Packages.tools.MaplePacketCreator.earnTitleMessage("You have earned " + nxAmount + " NX"));
				cm.logLeaf(nxAmount + " NX");
				cm.dispose();
			} else if(choice == 2) {
				if(!cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.SETUP).isFull(chairAmount)) {
					
					var chairStr = "";
					for(var i = 0; i < chairAmount; i++) {
						var chair = chairs[Math.floor(Math.random() * chairs.length)];
						cm.gainItem(chair, 1, true);
						chairStr += chair + " ";
					}
					
					if(useVP)
						cm.getClient().useVotePoints(1);
					else
						cm.gainItem(itemToUse, -1);
						
					cm.logLeaf("Chair ID: " + chairStr);
					cm.dispose();
				} else {
					cm.sendOk("Please make sure you have enough space to hold the items!");
				}
			} else if(choice == 3) {
				if(!cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP).isFull(weaponAmount)) {
					
					var weaponStr = "";
					for(var i = 0; i < weaponAmount; i++) {
						var weapon = weapons[Math.floor(Math.random() * weapons.length)];
						cm.gainItem(weapon, 1, true, true);
						weaponStr += weapon + " ";
					}
					
					if(useVP)
						cm.getClient().useVotePoints(1);
					else
						cm.gainItem(itemToUse, -1);
						
					cm.logLeaf("Maple Weapon IDs: " + weaponStr);
					cm.dispose();
				} else {
					cm.sendOk("Please make sure you have enough space to hold the items!");
				}
			} else if(choice == 4) {
				if(!cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.USE).isFull(2)) {
					cm.gainItem(buff1ID, buffAmount, true);
					cm.gainItem(buff2ID, buffAmount, true);
					cm.gainItem(itemToUse, -1);
					cm.logLeaf(buffAmount + " cheeses and apples");
					cm.dispose();
				} else {
					cm.sendOk("Please make sure you have enough space to hold the items!");
				}
			} else if(choice == 5) {
				if(!cm.haveItem(5030000, 1)) {
					if(!cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.CASH).isFull(1)){
						cm.gainItem(5030000, 1, false, true, 1000 * 60 * 60 * 24 * hiredMerchantLength);
						
						if(useVP)
							cm.getClient().useVotePoints(1);
						else
							cm.gainItem(itemToUse, -1);
						
						cm.logLeaf(hiredMerchantLength + " day hired merchant");
						cm.dispose();
					} else {
						cm.sendOk("Please make sure you have enough space to hold these items!");
					}
				} else {
					cm.sendOk("I can't give you a merchant if you already have one!");
				}
			}
		} else {
			cm.dispose();
		}
    }
}