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
function init() {
    em.getChannelServer().getMapFactory().getMap(222020100).resetReactors();
    em.getChannelServer().getMapFactory().getMap(222020200).resetReactors();
    
    scheduleNew();
}

function scheduleNew() {
    em.setProperty("goingUp", "false");
    em.setProperty("goingDown", "true");
    
    em.getChannelServer().getMapFactory().getMap(222020100).resetReactors();
    em.getChannelServer().getMapFactory().getMap(222020200).setReactorState();
    em.schedule("goingUpNow", 50000);
}

function goUp() {
    em.schedule("goingUpNow", 50000); // might be 60
}

function goDown() {
    em.schedule("goingDownNow", 50000); // might be 60
}

function goingUpNow() {
    em.getChannelServer().getMapFactory().getMap(222020110).warpEveryone(222020111);
    em.setProperty("goingUp", "true");
    em.schedule("isUpNow", 55000);
    
    em.getChannelServer().getMapFactory().getMap(222020100).setReactorState();
}

function goingDownNow() {
    em.getChannelServer().getMapFactory().getMap(222020210).warpEveryone(222020211);
    em.setProperty("goingDown", "true");
    em.schedule("isDownNow", 55000);
    
    em.getChannelServer().getMapFactory().getMap(222020200).setReactorState();
}

function isUpNow() {
    em.setProperty("goingDown", "false"); // clear
    em.getChannelServer().getMapFactory().getMap(222020200).resetReactors();
    em.getChannelServer().getMapFactory().getMap(222020111).warpEveryone(222020200, 0);

    goDown();
}

function isDownNow() {
    em.setProperty("goingUp", "false"); // clear
    em.getChannelServer().getMapFactory().getMap(222020100).resetReactors();
    em.getChannelServer().getMapFactory().getMap(222020211).warpEveryone(222020100, 4);
    
    goUp();
}

function cancelSchedule() {
    
}