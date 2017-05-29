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

status = -1;
var sel;
var pickup = -1;

function start() {
    cm.sendSimple("I'm Abdula, and I am a merchant intermediary who deals with rare goods. What do you have for me?#b\r\n#L0#I want to sell goods.\r\n#L1#I want to know about current market prices.\r\n#L2#A merchant intermediary? What is that?");
}

function action(mode, type, selection) {
    status++;
    if(mode != 1){
        if(mode == 0 && status == 0){
            cm.dispose();
            return;
        }else if(mode == 0 && sel == 0 && status == 2){
            cm.sendNext("You don't want to sell it right now? You can sell it later, but remember the Special Items are only valuable for a week.");
            cm.dispose();
            return;
        }else if(mode == 0 && sel == 2)
            status -= 2;
    }
    if(status == 0){
        if(sel == undefined)
            sel = selection;
        if (selection == 0){
            var text = "Let's see what you brought...#b";
            for(var i = 0; i < 5; i++)
                text += "\r\n#L" + i + "##t" + (3994090 + i) + "#";
            cm.sendSimple(text);
        }else if (selection == 1){
            var text = "";
            for(var i = 0; i < 5; i++)
                text += "The current market price for #t" + (i + 3994090) + "# is #rNOT DONE#k mesos\r\n";
            cm.sendNext(text);
            cm.dispose();
        }else
            cm.sendNext("I buy the products at the Maple 7th Day Market and sell them in other towns. I trade memorabilia, spices, taxidermy shark, and more... but no Lazy Daisy's eggs.");
    }else if(status == 1){
        if(sel == 0){
            if(cm.haveItem(3994090 + selection)){
                pickup = 3994090 + selection;
                cm.sendYesNo("The current price is 180 mesos. Would you like to sell it now?"); //Make a price changer by hour.
            }else{
                cm.sendNext("You don't have anything. Stop wasting my time... I'm a busy person.");
                cm.dispose();
            }
        }else
            cm.sendNextPrev("Maple 7th Day Market Sundays are my days off. If you need to see me, you're going to have to come Monday to Friday...");
    }else if(status == 2){
        if(sel == 0)
            cm.sendGetNumber("How many would you like to sell?", 0, 0, 200);
        else{
            cm.sendPrev("Oh, and the prices are subject to change. I can't get the short end of the stick, I have to stay in business! Check back with me frequently, my prices change by the hour!");
        }
    }else if(status == 3){
        if(sel == 0)
            if(selection != 1)
                cm.sendNext("Something's not right. Check again.");
            else{
                cm.sendNext("The transaction has been completed. See you next time.");
                cm.gainMeso(180);
                cm.gainItem(pickup, -1);
            }
        cm.dispose();
    }
}