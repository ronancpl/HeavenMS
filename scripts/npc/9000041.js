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
/* NPC: Donation Box (9000041)
	Victoria Road : Henesys
	
	NPC Bazaar:
        * @author Ronan Lana
*/

var options = ["EQUIP","USE","SET-UP","ETC"];
var name;
var status;
var selectedType = 0;

function numberWithCommas(x) {  // I ain't interessed in finding a way to parse java int to something js will accept through toLocaleString, so be it!
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function start() {
    status = -1;
    action(1, 0, 0); 
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }

    if (status == 0) {
        var selStr = "Hello, I am the #bBazaar NPC#k! Sell to me any item on your inventory you don't need. #rWARNING#b: Make sure you have your items ready to sell at the slots #rAFTER#b the item you have selected to sell.#k Any items #bunder#k the item selected will be sold thoroughly.";
        for (var i = 0; i < options.length; i++)
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        cm.sendSimple(selStr);
    }

    else if (status == 1) {
	selectedType = selection;
        cm.sendGetText("From what item on your #r" + options[selectedType] + "#k inventory do you want to start the transaction?");
    }

    else if (status == 2) {
        name = cm.getText();
	var res = cm.getPlayer().sellAllItemsFromName(selectedType + 1, name);

        if(res > -1) cm.sendOk("Transaction complete! You received #r" + numberWithCommas(res) + " mesos#k from this action.");
	else cm.sendOk("There is no #b'" + name + "'#k in your #b" + options[selectedType] + "#k inventory!");

        cm.dispose();
    }
}