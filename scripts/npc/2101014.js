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
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
	NPC NAME: Cesar (2)
	NPC ID: 2101014
	Author: Vcoc
	Function: AriantPQ
*/

status = -1;
var sel;
empty = [false, false, false];

function start() {
    if((cm.getPlayer().getLevel() < 19 || cm.getPlayer().getLevel() > 30) && !cm.getPlayer().isGM()){
        cm.sendNext("You're not between level 20 and 30. Sorry, you may not participate.");
        cm.dispose();
        return;
    }
    var text = "What do you want?#b";
    for(var i = 0; i < 3; i += 1)
        if (cm.getPlayerCount(980010100 + (i * 100)) > 0)
            if(cm.getPlayerCount(980010101 + (i * 100)) > 0)
                continue;
            else
                text += "\r\n#L" + i + "# Battle Arena " + (i + 1) + "([" + cm.getPlayerCount(980010100 + (i * 100)) + "/" + cm.getPlayer().getAriantSlotsRoom(i) + "]  users: " + cm.getPlayer().getAriantRoomLeaderName(i) + ")#l";
        else{
            empty[i] = true;
            text += "\r\n#L" + i + "# Battle Arena " + (i + 1) + "( Empty )#l";
            if(cm.getPlayer().getAriantRoomLeaderName(i) != "")
                cm.getPlayer().removeAriantRoom(i);
        }
    cm.sendSimple(text + "\r\n#L3# I'd like to know more about the competition.#l");
}

function action(mode, type, selection){
    status++;
    if(mode != 1){
        if(mode == 0 && type == 0)
            status -= 2;
        else{
            cm.dispose();
            return;
        }
    }
    if (status == 0){
        if(sel == undefined)
            sel = selection;
        if(sel == 3)
            cm.sendNext("What do you need to do? You must be new to this. Allow me explain in detail.");
        else{
            if(cm.getPlayer().getAriantRoomLeaderName(sel) != "" && empty[sel])
                empty[sel] = false;
            else if(cm.getPlayer().getAriantRoomLeaderName(sel) != ""){
                cm.warp(980010100 + (sel * 100));
                cm.dispose();
                return;
            }
            if(!empty[sel]){
                cm.sendNext("Another combatant has created the battle arena first. I advise you to either set up a new one, or join the battle arena that's already been set up.");
                cm.dispose();
                return;
            }
            cm.sendGetNumber("Up to how many participants can join in this match? (2~6 ppl)", 0, 2, 6);
        }
    }else if (status == 1){
        if(sel == 3)
            cm.sendNextPrev("It's really simple, actually. You'll receive #b#t2270002##k from me, and your task is to eliminate a set amount of HP from the monster, then use #b#t2270002##k to absorb its monstrous power.");
        else{
            if(cm.getPlayer().getAriantRoomLeaderName(sel) != "" && empty[sel])
                empty[sel] = false;
            if(!empty[sel]){
                cm.sendNext("Another combatant has created the battle arena first. I advise you to either set up a new one, or join the battle arena that's already been set up.");
                cm.dispose();
                return;
            }
            cm.getPlayer().setAriantRoomLeader(sel, cm.getPlayer().getName());
            cm.getPlayer().setAriantSlotRoom(sel, selection);
            cm.warp(980010100 + (sel * 100));
            cm.dispose();
        }
    }else if (status == 2)
        cm.sendNextPrev("It's simple. If you absorb the power of the monster #b#t2270002##k, then you'll make #b#t4031868##k, which is something Queen Areda loves. The combatant with the most jewels wins the match. It's actually a smart idea to prevent others from absorbing in order to win.");
    else if (status == 3)
        cm.sendNextPrev("One thing. Using #b#t2100067##k, you can steal #b#t4031868##k from your enemies. Warning: #rYou may not use pets for this.#k Understood?!");
    else if (status == 4)
        cm.dispose();
}
