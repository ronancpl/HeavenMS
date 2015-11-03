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
/*
 *@Author RMZero213
 * Ludibrium Maze Party Quest
 * Do not release anywhere other than RaGEZONE. Give credit if used.
 */

var status = 0;
var rewards = new Array(
			1442017, 1, //Blood Snowboard
			1322025, 1, //Emergency Rescue Tube
			1032013, 1, //Red-Hearted Earrings
			1302016, 1, //Yellow Umbrella
			1072263, 1, //Green Strap Shoes
			1032043, 1, //Cecelia's Earrings
			2000005, 1, //Power Elixir
			2000004, 5, //Elixir
			2001001, 5, //Ice Cream Pop
			2001002, 5, //Red Bean Sundae
			2020008, 20, //Fat Sausage
			2020010, 20, //Grape Juice
			2030008, 20, //Coffee Milk
			2030010, 20, //Fruit Milk
			2030009, 20, //Strawberry Milk
			2022000, 50, //Pure Water
			2001000, 50, //Watermelon
			2022019, 50, //Kinoko Ramen (Pig Head)
			2020007, 100, //Dried Squid
			2020006, 100, //Hot Dog Supreme
			2020009, 100, //Orange Juice
			2000006, 100, //Mana Elixir
			2040601, 1, //Scroll for Bottomwear for Def (60%)
			2040605, 1, //Dark Scroll for Bottomwear for DEF 30%
			2040602, 1, //Scroll for Bottomwear for DEF (10%)
			2041027, 1, //Dark Scroll for Cape for Magic Def 30%
			2041028, 1,	//Dark Scroll for Cape for Weapon Def 70%
			2041004, 1,	//Scroll for Cape for Weapon Def 60%
			2041029, 1,	//Dark Scroll for Cape for Weapon Def 30%
			2041017, 1,	//Scroll for Cape for INT 10%
			2041020, 1,	//Scroll for Cape for DEX 10%
			2040008, 1,	//Dark Scroll for Helmet for DEF 70%
			2040001, 1,	//Scroll for Helmet for DEF 60%
			2040009, 1,	//Dark Scroll for Helmet for DEF 30%
			2040002, 1,	//Scroll for Helmet for DEF 10%
			2040504, 1,	//Scroll for Overall Armor for DEF 60%
			2040511, 1, //Dark Scroll for Overall Armor for DEF 30%
			2040505, 1,	//Scroll for Overall Armor for DEF 10%
			2040501, 1, //Scroll for Overall Armor for DEX 60%
			2040904, 1,	//Dark Scroll for Shield for DEF 70%
			2040901, 1,	//Scroll for Shield for DEF 60%
			2040905, 1,	//Dark Scroll for Shield for DEF 30%
			2040902, 1,	//Scroll for Shield for DEF 10%
			2040404, 1, //Dark Scroll for Topwear for DEF 70%
			2040401, 1,	//Scroll for Topwear for DEF 60%
			2040405, 1,	//Dark Scroll for Topwear for DEF 30%
			2040402, 1	//Scroll for Topwear for DEF 10%
			);

function start() {
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection){
    if (mode == -1) {
        cm.dispose();
    }
    if (mode == 0) {
        cm.dispose();
        return;
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
			var index = Math.floor(Math.random()*rewards.length);
			var reward;
			var quantity;
			if (index % 2 == 0){ //The index was an item id
				reward = rewards[index];
				quantity = rewards[index + 1];
			} else {
				reward = rewards[index - 1];
				quantity = rewards[index];
			}
			if(!cm.canHold(reward)){
				cm.sendOk("Please make space in your inventory!");
				return;
			}
			cm.gainItem(reward, quantity);
            var eim = cm.getPlayer().getEventInstance();
            if (eim != null) {
                eim.unregisterPlayer(cm.getPlayer());
            }
            cm.warp(220000000, 0);
			cm.gainItem(4001106, -cm.itemQuantity(4001106))
            cm.dispose();
        }
    }
}