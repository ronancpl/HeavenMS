/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* 
 * @Author Stereo, xQuasar, <<XkelvinchiaX@Kelvin(For make it Fully Working.)>>
 * 
 * Adobis - El Nath: Entrance to Zakum Altar (211042400)
 * 
 * Start of Zakum Bossfight
 */
 
var status;
var minLevel = 50;
var state;
var maxPlayers = 30;
 
 
function start() {
    status = -1;
    action(1, 0, 0);
}
 
function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if ((cm.getPlayer().getLevel() < minLevel)) {
                cm.warp(211042300);
                cm.sendOk("Please come back when you're prepared for the battle. You should not be here yet.");
                cm.dispose();
                return;
            }
            cm.sendSimple("The battle to defeat Zakum begins here. What would you like to do? #b\r\n#L0#Start a new Zakum Battle#l\r\n#L1#Join your group's Zakum Battle#l");
        }
        else if (status == 1) {
            state = selection;
            if (selection == 0)
                cm.sendGetText("In order to start the Zakum Battle, you need to choose a name for your instance.  This is the password that lets your members join, so tell it to everybody who wants to participate in the battle.");
            
            else if (selection == 1)
                cm.sendGetText("In order to join a Zakum Battle, you need to enter the password.  If you don't know what it is, please ask the person leading the battle.");
            
        }
        else if (status == 2) {
            var em = cm.getEventManager("ZakumBattle");
            var passwd = cm.getText();
            if (em == null)
                cm.sendOk("This trial is currently under construction.");
            else {
                if (state == 0) { // Leader
                    if (getEimForString(em,passwd) != null)
                        cm.sendOk("You may not use that password.");
                    else { // start Zakum Battle
                        var eim = em.newInstance("Zakum" + passwd);
                        if(!em.startInstance(eim,cm.getPlayer().getName())) {
                            cm.sendOk("A party in your name is already registered in this instance.");
                            cm.dispose();
                            return;
                        }
                        eim.registerPlayer(cm.getPlayer());
                    }
                }
                if (state == 1) { // Member
                    var eim = getEimForString(em,passwd);
                    if (eim == null)
                        cm.sendOk("There is currently no battle registered under that name.");
                    else {
                        if (eim.getProperty("canEnter").toLowerCase() == "true") {
                            if (eim.getPlayers().size() < maxPlayers)
                                eim.registerPlayer(cm.getPlayer());
                            else
                                cm.sendOk("I'm sorry, but that battle is currently full.  Please wait to join another one.");
                        }
                        else 
                            cm.sendOk("I'm sorry, but that battle is currently in progress.  Please return later.");
                    }
                }
            }
            cm.dispose();
        }
    }
}
function getEimForString(em, name) {
    var stringId = "Zakum" + name;
    return em.getInstance(stringId);
}