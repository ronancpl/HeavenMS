/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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

/**
 * @author: Ronan
 * @npc: Agent Meow
 * @map: 970030000 - Hidden Street - Exclusive Training Center
 * @func: Boss Rush PQ
*/

var status = 0;
var state;
var em = null;

function onRestingSpot() {
    return cm.getMapId() >= 970030001 && cm.getMapId() <= 970030010;
}

function isFinalBossDone() {
    return cm.getMapId() >= 970032700 && cm.getMapId() < 970032800 && cm.getMap().getMonsters().isEmpty();
}

function detectTeamLobby(team) {
    var midLevel = 0;
    
    for(var i = 0; i < team.size(); i++) {
        var player = team.get(i);
        midLevel += player.getLevel();
    }
    midLevel = Math.floor(midLevel / team.size());
    
    var lobby;  // teams low level can be allocated at higher leveled lobbys
    if(midLevel <= 20) lobby = 0;
    else if(midLevel <= 40) lobby = 1;
    else if(midLevel <= 60) lobby = 2;
    else if(midLevel <= 80) lobby = 3;
    else if(midLevel <= 90) lobby = 4;
    else if(midLevel <= 100) lobby = 5;
    else if(midLevel <= 110) lobby = 6;
    else lobby = 7;
        
    return lobby;
}

function start() {
	status = -1;
        state = (cm.getMapId() >= 970030001 && cm.getMapId() <= 970042711) ? (!onRestingSpot() ? (isFinalBossDone() ? 3 : 1) : 2) : 0;
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
                        if(state == 3) {
                                if(cm.getEventInstance().getProperty("clear") == null) {
                                        cm.getEventInstance().clearPQ();
                                        cm.getEventInstance().setProperty("clear", "true");
                                }
                            
                                if(cm.isEventLeader()) {
                                        cm.sendOk("Your party completed such an astounding feat coming this far, #byou have defeated all the bosses#k, congratulations! Now I will be handing your reward as you are being transported out...");
                                }
                                else {
                                        cm.sendOk("For #bdefeating all bosses#k in this instance, congratulations! You will now receive a prize that matches your performance here as I warp you out.");
                                }
                        }
                        else if(state == 2) {
                                if(cm.isEventLeader()) {
                                        if(cm.getPlayer().getEventInstance().isEventTeamTogether()) {
                                                cm.sendYesNo("Is your party ready to proceed to the next stages? Walk through the portal if you think you're done, the time is now.. Now, do you guys REALLY want to proceed?");
                                        }
                                        else {
                                                cm.sendOk("Please wait for your party to reassemble before proceeding.");
                                                cm.dispose();
                                                return;
                                        }
                                }
                                else {
                                        cm.sendOk("Wait for your party leader to give me the signal to proceed. If you're not feeling too well and want to quit, walk through the portal and you will be transported out, and you will receive a prize for coming this far.");
                                        cm.dispose();
                                        return;
                                }
                        } else if(state == 1) {
                                cm.sendYesNo("Do you wish to abandon this instance?");
                        }
                        else {
                                em = cm.getEventManager("BossRushPQ");
                                if(em == null) {
                                        cm.sendOk("The Boss Rush PQ has encountered an error.");
                                        cm.dispose();
                                        return;
                                } else if(cm.isUsingOldPqNpcStyle()) {
                                        action(1, 0, 0);
                                        return;
                                }
                                
                                cm.sendSimple("#e#b<Party Quest: Boss Rush>\r\n#k#n" + em.getProperty("party") + "\r\n\r\nWould you like to collaborate with party members to complete the expedition, or are you brave enough to take it on all by yourself? Have your #bparty leader#k talk to me or make yourself a party.#b\r\n#L0#I want to participate in the party quest.\r\n#L1#I want to find party members.\r\n#L2#I would like to hear more details.");
                        }
                } else if (status == 1) {
                        if(state == 3) {
                                if(!cm.getPlayer().getEventInstance().giveEventReward(cm.getPlayer(), 6)) {
                                        cm.sendOk("Please arrange a slot in all tabs of your inventory beforehand.");
                                        cm.dispose();
                                        return;
                                }
                                
                                cm.warp(970030000);
                                cm.dispose();
                        } else if(state == 2) {
                                var restSpot = ((cm.getMapId() - 1) % 5) + 1;
                                cm.getPlayer().getEventInstance().restartEventTimer(restSpot * 4 * 60000);  // adds (restspot number * 4) minutes
                                cm.getPlayer().getEventInstance().warpEventTeam(970030100 + cm.getEventInstance().getIntProperty("lobby") + (500 * restSpot));
                                
                                cm.dispose();
                        } else if(state == 1) {
                                cm.warp(970030000);
                                cm.dispose();
                        }
                        else {
                                if (selection == 0) {
                                        if (cm.getParty() == null) {
                                                cm.sendOk("You can participate in the party quest only if you are in a party.");
                                                cm.dispose();
                                        } else if(!cm.isLeader()) {
                                                cm.sendOk("Your party leader must talk to me to start this party quest.");
                                                cm.dispose();
                                        } else {
                                                var eli = em.getEligibleParty(cm.getParty());
                                                if(eli.size() > 0) {
                                                        var lobby = detectTeamLobby(eli), i;
                                                        for(i = lobby; i < 8; i++) {
                                                                if(em.startInstance(i, cm.getParty(), cm.getPlayer().getMap(), 1)) break;
                                                        }
                                                        
                                                        if(i == 8) {
                                                                cm.sendOk("Another party has already entered the #rParty Quest#k in this channel. Please try another channel, or wait for the current party to finish.");
                                                        }
                                                }
                                                else {
                                                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. If you're having trouble finding party members, try Party Search.");
                                                }
                                                
                                                cm.dispose();
                                        }
                                } else if (selection == 1) {
                                        cm.sendOk("Try using a Super Megaphone or asking your buddies or guild to join!");
                                        cm.dispose();
                                } else {
                                        cm.sendOk("#e#b<Party Quest: Boss Rush>#k#n\r\nBrave adventurers from all over the places travels here to test their skills and abilities in combat, as they face even more powerful bosses from MapleStory. Join forces with fellow adventurers or face all the burden by yourself and receive all the glory, it is up to you. REWARDS are given accordingly to how far the adventurers reach and extra prizes may are given to a random member of the party, all attributed at the end of an expedition.\r\n\r\nThis instance also supports #bmultiple lobbies for matchmaking several ranges of team levels#k at once: team up with players with lower level if you want better chances to swiftly set up a boss rush for your team.");
                                        cm.dispose();
                                }
                        }
                }
        }
}