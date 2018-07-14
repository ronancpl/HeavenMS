/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2017 RonanLana

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
/* Pelvis Bebop
	Marriage NPC
 */

importPackage(Packages.constants);
importPackage(Packages.net.server.channel.handlers);
importPackage(Packages.tools);
importPackage(Packages.tools.packets);

var status;
var state;
var eim;
var weddingEventName = "WeddingChapel";
var cathedralWedding = false;
var weddingIndoors;
var weddingBlessingExp = ServerConstants.WEDDING_BLESS_EXP;

function detectPlayerItemid(player) {
    for (var x = 4031357; x <= 4031364; x++) {
        if (player.haveItem(x)) {
            return x;
        }
    }
    
    return -1;
}

function getRingId(boxItemId) {
    return boxItemId == 4031357 ? 1112803 : (boxItemId == 4031359 ? 1112806 : (boxItemId == 4031361 ? 1112807 : (boxItemId == 4031363 ? 1112809 : -1)));
}

function isSuitedForWedding(player, equipped) {
    var baseid = (player.getGender() == 0) ? 1050131 : 1051150;
    
    if(equipped) {
        for(var i = 0; i < 4; i++) {
            if(player.haveItemEquipped(baseid + i)) {
                return true;
            }
        }
    } else {
        for(var i = 0; i < 4; i++) {
            if(player.haveItemWithId(baseid + i, true)) {
                return true;
            }
        }
    }
    
    return false;
}

function getWeddingPreparationStatus(player, partner) {
    if(!player.haveItem(4000313)) return -3;
    if(!partner.haveItem(4000313)) return 3;
    
    if(!isSuitedForWedding(player, true)) return -4;
    if(!isSuitedForWedding(partner, true)) return 4;
    
    var hasEngagement = false;
    for (var x = 4031357; x <= 4031364; x++) {
        if (player.haveItem(x)) {
            hasEngagement = true;
            break;
        }
    }
    if(!hasEngagement) return -1;

    hasEngagement = false;
    for (var x = 4031357; x <= 4031364; x++) {
        if (partner.haveItem(x)) {
            hasEngagement = true;
            break;
        }
    }
    if(!hasEngagement) return -2;

    if(!player.canHold(1112803)) return 1;
    if(!partner.canHold(1112803)) return 2;

    return 0;
}

function giveCoupleBlessings(eim, player, partner) {
    var blessCount = eim.gridSize();
    
    player.gainExp(blessCount * weddingBlessingExp);
    partner.gainExp(blessCount * weddingBlessingExp);
}

function start() {  
    eim = cm.getEventInstance();

    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            if(eim == null) {
                cm.warp(680000000,0);
                cm.dispose();
                return;
            }

            var playerId = cm.getPlayer().getId();
            if(playerId == eim.getIntProperty("groomId") || playerId == eim.getIntProperty("brideId")) {
                var wstg = eim.getIntProperty("weddingStage");

                if(wstg == 2) {
                    cm.sendYesNo("Awhoooooooooosh~, the guests have proclaimed their love to y'all. The time has come baby~, #rshould I make you Husband and Wife#k?");
                    state = 1;
                } else if(wstg == 1) {
                    cm.sendOk("W-whoah wait a bit alright? Your guests are currently giving their love to y'all. Let's shake this place up, baby~~.");
                    cm.dispose();
                } else {
                    cm.sendOk("Wheeeeeeeeeeeeeew! Our festival here is now complete, give a sweet talk to #b#p9201009##k, she will lead you and your folks to the afterparty. Cheers for your love!");
                    cm.dispose();
                }
            } else {
                var wstg = eim.getIntProperty("weddingStage");
                if(wstg == 1) {
                    if(eim.gridCheck(cm.getPlayer()) != -1) {
                        cm.sendOk("Everyone let's shake this place up! Let's rock 'n' roll!!");
                        cm.dispose();
                    } else {
                        if(eim.getIntProperty("guestBlessings") == 1) {
                            cm.sendYesNo("Will you manifest your love to the superstars here present?");
                            state = 0;
                        } else {
                            cm.sendOk("Our superstars are gathered down here. Everyone, let's give them some nice, nicey party~!");
                            cm.dispose();
                        }
                    }
                } else if(wstg == 3) {
                    cm.sendOk("Whooooooo-hoo! The couple's love now are like one super big shiny heart right now! And it shall go on ever after this festival. Please #rget ready for the afterparty#k, baby~. Follow the married couple's lead!");
                    cm.dispose();
                } else {
                    cm.sendOk("It's now guys... Stay with your eyes and ears keened up! They are about to smooch it all over the place!!!");
                    cm.dispose();
                }
            }
        } else if (status == 1) {
            if(state == 0) {    // give player blessings
                eim.gridInsert(cm.getPlayer(), 1);

                if(ServerConstants.WEDDING_BLESSER_SHOWFX) {
                    var target = cm.getPlayer();
                    target.announce(MaplePacketCreator.showSpecialEffect(9));
                    target.getMap().broadcastMessage(target, MaplePacketCreator.showForeignEffect(target.getId(), 9), false);
                } else {
                    var target = eim.getPlayerById(eim.getIntProperty("groomId"));
                    target.announce(MaplePacketCreator.showSpecialEffect(9));
                    target.getMap().broadcastMessage(target, MaplePacketCreator.showForeignEffect(target.getId(), 9), false);
                    
                    target = eim.getPlayerById(eim.getIntProperty("brideId"));
                    target.announce(MaplePacketCreator.showSpecialEffect(9));
                    target.getMap().broadcastMessage(target, MaplePacketCreator.showForeignEffect(target.getId(), 9), false);
                }

                cm.sendOk("Way to go, my friend! Your LOVE has been added to theirs, now in one bigger heart-shaped sentiment that will remain lively in our hearts forever! Who-hoo~!");
                cm.dispose();
            } else {            // couple wants to complete the wedding
                var wstg = eim.getIntProperty("weddingStage");

                if(wstg == 2) {
                    var pid = cm.getPlayer().getPartnerId();
                    if(pid <= 0) {
                        cm.sendOk("Huh~.... Wait wait, did you just break that thing you had right now?? Oh my, what happened?");
                        cm.dispose();
                        return;
                    }

                    var player = cm.getPlayer();
                    var partner = cm.getMap().getCharacterById(cm.getPlayer().getPartnerId());
                    if(partner != null) {
                        state = getWeddingPreparationStatus(player, partner);

                        switch(state) {
                            case 0:
                                var pid = eim.getIntProperty("confirmedVows");
                                if(pid != -1) {
                                    if(pid == player.getId()) {
                                        cm.sendOk("You have already confirmed your vows. All that is left is for your partner to confirm now.");
                                    } else {
                                        eim.setIntProperty("weddingStage", 3);
                                        var cmPartner = partner.getClient().getAbstractPlayerInteraction();

                                        var playerItemId = detectPlayerItemid(player);
                                        var partnerItemId = (playerItemId % 2 == 1) ? playerItemId + 1 : playerItemId - 1;

                                        var marriageRingId = getRingId((playerItemId % 2 == 1) ? playerItemId : partnerItemId);

                                        cm.gainItem(playerItemId, -1);
                                        cmPartner.gainItem(partnerItemId, -1);

                                        RingActionHandler.giveMarriageRings(player, partner, marriageRingId);
                                        player.setMarriageItemId(marriageRingId);
                                        partner.setMarriageItemId(marriageRingId);

                                        //var marriageId = eim.getIntProperty("weddingId");
                                        //player.announce(Wedding.OnMarriageResult(marriageId, player, true));
                                        //partner.announce(Wedding.OnMarriageResult(marriageId, player, true));

                                        giveCoupleBlessings(eim, player, partner);

                                        cm.getMap().dropMessage(6, "Wayne: I'll call it out right now, and it shall go on: you guys are the key of the other's lock, a lace of a pendant. That's it, snog yourselves!");
                                        eim.schedule("showMarriedMsg", 2 * 1000);
                                    }
                                } else {
                                    eim.setIntProperty("confirmedVows", player.getId());
                                    cm.getMap().dropMessage(6, "Wedding Assistant: " + player.getName() + " has confirmed vows! Alright, one step away to make it official. Tighten your seatbelts!");
                                }
                                
                                break;

                            case -1:
                                cm.sendOk("Well, it seems you no longer have the ring/ring box you guys exchanged at the engagement. Awww man~");
                                break;

                            case -2:
                                cm.sendOk("Well, it seems your partner no longer has the ring/ring box you guys exchanged at the engagement. Awww man~");
                                break;

                            case -3:
                                cm.sendOk("Well, it seems you don't have the #r#t4000313##k given at the entrance... Please find it, baby~");
                                break;

                            case -4:
                                cm.sendOk("Aww I know that shucks, but the fashionable wedding clothes does a essential part here. Please wear it before talking to me.");
                                break;

                            case 1:
                                cm.sendOk("Please make an EQUIP slot available to get the marriage ring, will you?");
                                break;

                            case 2:
                                cm.sendOk("Please let your partner know to make an EQUIP slot available to get the marriage ring, will you?");
                                break;

                            case 3:
                                cm.sendOk("Well, it seems your partner don't have the #r#t4000313##k given at the entrance... Please find it, I can't call the finally without it.");
                                break;

                            case 4:
                                cm.sendOk("Aww I know that shucks, but it seems your partner is not using the fashionable wedding clothes. Please tell them to wear it before talking to me.");
                                break;
                        }

                        cm.dispose();
                    } else {
                        cm.sendOk("Oof, is that it that your partner is not here, right now? ... Oh noes, I'm afraid I can't call the finally if your partner is not here.");
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Wheeeeeeeeeeeeew~ You are now #bofficially one couple#k, and a brilliant one. Your moves fitted in outstandingly, congratulations!");
                    cm.dispose();
                }
            }
        }
    }
}