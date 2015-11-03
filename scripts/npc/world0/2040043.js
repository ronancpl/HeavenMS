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
@	Author : Twdtwd
@
@	NPC = Blue Balloon
@	Map = Hidden-Street <Stage 8>
@	NPC MapId = 922010800
@	Function = LPQ - 8 Stage
@
@	Description: Used to find the combo to unlock the next door. Players stand on 5 different crates to guess the combo.
*/

importPackage(Packages.tools);

var status = 0;
var party;
var preamble;
var gaveItems;
var nthtext = "8th";

function start() {
    status = -1;
    action(1, 0, 0);
}

function generateCombo() {
	var countPicked = 0;
	var positions = Array(0,0,0,0,0,0,0,0,0);
	while(countPicked < 5) {
		var picked = Math.floor(Math.random() * positions.length);
		if(positions[picked] == 1) // Don't let it pick one its already picked.
			continue;
			
		positions[picked] = 1;
		countPicked++;
	}
	
	var returnString = "";
	for(var i = 0; i < positions.length; i++) {
		returnString += positions[i];
		if(i != positions.length - 1)
		returnString += ",";
	}
	
	return returnString;
	
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    }else if (mode == 0){
        cm.dispose();
    }else{
        if (mode == 1)
            status++;
        else
            status--;
        var eim = cm.getPlayer().getEventInstance();
        if (status == 0) {
            party = eim.getPlayers();
            preamble = eim.getProperty("leader" + nthtext + "preamble");
            gaveItems = eim.getProperty("leader" + nthtext + "gaveItems");
            if (preamble == null) {
                cm.sendOk("Hi. Welcome to the " + nthtext + " stage.");
                eim.setProperty("leader" + nthtext + "preamble","done");
				eim.setProperty("stage" + nthtext + "combo", generateCombo());
                cm.dispose();
            }else{
                if(!isLeader()){
                    if(gaveItems == null){
                        cm.sendOk("Please tell your #bParty-Leader#k to come talk to me");
                        cm.dispose();
                    }else{
                        cm.sendOk("Hurry, go to the next stage, the portal is open!");
                        cm.dispose();
                    }
                }
                if(gaveItems == null){
					if(cm.getPlayer().getMap().getCharacters().size() != eim.getPlayers().size()) {
						cm.sendOk("Please wait for all of your party members to get here.");
						cm.dispose();
						return;
                    }
                    objset = [0,0,0,0,0,0,0,0,0];
					var playersOnCombo = 0;
					var map = cm.getPlayer().getMap();
                    for (var i = 0; i < party.size(); i++) {
                        for (var y = 0; y < map.getAreas().size(); y++) {
                            if (map.getArea(y).contains(party.get(i).getPosition())) {
								playersOnCombo++;
                                objset[y] = 1;
								//cm.mapMessage(5, "Player found on " + (y + 1));
                                break;
                            }
                        }
                    }
					
					if (playersOnCombo == 5 || cm.getPlayer().gmLevel() > 0) {
                        var combo = eim.getProperty("stage" + nthtext + "combo").split(',');
                        var correctCombo = true;
                        for (i = 0; i < objset.length && correctCombo; i++)
                            if (parseInt(combo[i]) != objset[i]) {
								//cm.mapMessage(5, "Combo failed on " + (i + 1));
                                correctCombo = false;
							}
                        if (correctCombo || cm.getPlayer().gmLevel() > 0) {
                            clear(eim, cm);
                            cm.dispose();
                        } else { // Wrong
							//cm.sendOk(eim.getProperty("stage" + nthtext + "combo"));
                            failstage(eim, cm);
                            cm.dispose();
                        }
                    } else {
                        cm.sendNext("It looks like you haven't found the 5 boxes just yet. Please think of a different combination of boxes Only 5 are allowed to stand on boxes, and if you move it may not count as an answer, so please keep that in mind. Keep going!");
                        cm.dispose();
                    }
                }
            }
        }     
    }
}

function clear(eim, cm) {
    var map = eim.getMapInstance(cm.getPlayer().getMapId());
    map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/clear"));
    map.broadcastMessage(MaplePacketCreator.playSound("Party1/Clear"));
    map.broadcastMessage(MaplePacketCreator.environmentChange("gate", 2));
	
	eim.setProperty("8stageclear","true");
	eim.setProperty("leader" + nthtext + "gaveItems","done");
	cm.givePartyExp("LudiPQ8th");
}

function failstage(eim, cm) {
    var map = eim.getMapInstance(cm.getPlayer().getMapId());
    map.broadcastMessage(MaplePacketCreator.playSound("Party1/Failed"));
    map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/wrong_kor"));
}

function isLeader(){
    if(cm.getParty() == null)
        return false;
    else
        return cm.isLeader();
}