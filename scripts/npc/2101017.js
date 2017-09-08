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
	NPC NAME: Cesar (3)
	NPC ID: 2101017
	Author: Vcoc
	Function: AriantPQ
*/

importPackage(Packages.tools);
importPackage(Packages.client);

status = -1;
var sel;

function start() {
    if((cm.getPlayer().getLevel() < 19 || cm.getPlayer().getLevel() > 30) && !cm.getPlayer().isGM()){
        cm.sendNext("You're not between level 20 and 30. Sorry, you may not participate.");
        cm.dispose();
        return;
    }
    if(cm.getPlayer().getMapId() % 10 == 1)
        cm.sendSimple("Do you have a request for me?\r\n#b#L0# Give me #t2270002# and #t2100067#.#l\r\n#L1# What should I do?#l\r\n#L2# Get me out of here.#l");
    else
        cm.sendSimple(cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1) == cm.getPlayer().getName() ? "Would you like to start the match?#b\r\n#b#L3# Ready to enter the Battle Arena!!#l\r\n#L1# I'd like to kick another character.#l\r\n#L2# Get me out of here.#l" : "What do you want?#b\r\n#L2# Get me out of here.#l");
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
    if(cm.getPlayer().getMapId() % 10 == 1){
        if (status == 0){
            if (sel == undefined)
                sel = selection;
            if(sel == 0){
                if(cm.haveItem(2270002))
                    cm.sendNext("You already have #b#t2270002##k.");
            else if(cm.canHold(2270002) && cm.canHold(2100067)){
                if(cm.haveItem(2100067))
                    cm.removeAll(2100067);
                    cm.gainItem(2270002, 50);
                    cm.gainItem(2100067, 5);
                    cm.sendNext("Now lower the HP of the monsters, and use #b#t2270002##k to absorb their power!");
                }else
                    cm.sendNext("Check and see if your Use inventory is full or not");
                cm.dispose();
            }else if(sel == 1)
                cm.sendNext("What do you need to do? You must be new to this. Allow me explain in detail.");
            else
                cm.sendYesNo("Are you sure you want to leave?"); //No GMS like.
        } else if (status == 1){
            if(type == 1){
                cm.removeAll(4031868);
                cm.removeAll(2270002);
                cm.removeAll(2100067);
                cm.warp(980010020);
                cm.dispose();
                return;
            }
            cm.sendNextPrev("It's really simple, actually. You'll receive #b#t2270002##k from me, and your task is to eliminate a set amount of HP from the monster, then use #b#t2270002##k to absorb its monstrous power.");
        } else if (status == 2)
            cm.sendNextPrev("It's simple. If you absorb the power of the monster #b#t2270002##k, then you'll make #b#t4031868##k, which is something Queen Areda loves. The combatant with the most jewels wins the match. It's actually a smart idea to prevent others from absorbing in order to win.");
        else if (status == 3)
            cm.sendNextPrev("One thing. Using #b#t2100067##k, you can steal #b#t4031868##k from your enemies. Warning: #rYou may not use pets for this.#k Understood?!");
        else if (status == 4)
            cm.dispose();
    }else{
        var nextchar = cm.getMap(cm.getPlayer().getMapId()).getCharacters().iterator();
        if(status == 0){
            if (sel == undefined)
                sel = selection;
            if(sel == 1)
                if(cm.getPlayerCount(cm.getPlayer().getMapId()) > 1){
                    var text = "Who would you like to kick from room?"; //Not GMS like text
                    var name;
                    for(var i = 0; nextchar.hasNext(); i++){
                        name = nextchar.next().getName();
                        if(!cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1).equals(name))
                            text += "\r\n#b#L" + i + "#" + name + "#l";
                    }
                    cm.sendSimple(text);
                }else{
                    cm.sendNext("There's no character that can be kicked right now.");
                    cm.dispose();
                }
            else if(sel == 2){
                if(cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1) == cm.getPlayer().getName())
                    cm.sendYesNo("Are you sure you want to leave? You're the leader of the Arena, so if you leave, the whole Battle Arena will close.");
                else
                    cm.sendYesNo("Are you sure you want to leave?"); //No GMS like.
            }else if(sel == 3)
                if(cm.getPlayerCount(cm.getPlayer().getMapId()) > 1)
                    cm.sendYesNo("The room is all set, and no other character may join this Battle Arena. Do you want to start the game right now?");
                else{
                    cm.sendNext("You'll need at least 2 participants inside in order to start the match.");
                    cm.dispose();
                }
        }else if (status == 1){
            if(sel == 1){
                for(var i = 0; nextchar.hasNext(); i++)
                    if(i == selection){
                        nextchar.next().changeMap(cm.getMap(980010000));
                        break;
                    }else
                        nextchar.next();
                cm.sendNext("Player have been kicked out of the Arena."); //Not GMS like
            }else if(sel == 2){
                if(cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1) != cm.getPlayer().getName())
                    cm.warp(980010000);
                else{
                    cm.getPlayer().removeAriantRoom((cm.getPlayer().getMapId() / 100) % 10);
                    cm.mapMessage(6, cm.getPlayer().getName() + " has left the Arena, so the Arena will now close.");
                    cm.warpMap(980010000);
                }
            }else{
                cm.warpMap(cm.getPlayer().getMapId() + 1);
            //}
            //cm.getPlayer().getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(cm.getPlayer().getName(), 0, true));
            }
            cm.dispose();
        }
    }
}
