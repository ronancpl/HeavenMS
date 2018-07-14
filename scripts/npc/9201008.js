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
/* Assistant Bonnie
	Marriage NPC
 */

var status;
var wid;
var isMarrying;

var cathedralWedding = false;
var weddingEventName = "WeddingChapel";
var weddingEntryTicketCommon = 5251001;
var weddingEntryTicketPremium = 5251002;
var weddingSendTicket = 4031377;
var weddingGuestTicket = 4031406;
var weddingAltarMapid = 680000110;
var weddingIndoors;

function isWeddingIndoors(mapid) {
    return mapid >= 680000100 && mapid <= 680000500;
}

function hasSuitForWedding(player) {
    var baseid = (player.getGender() == 0) ? 1050131 : 1051150;
    
    for(var i = 0; i < 4; i++) {
        if(player.haveItemWithId(baseid + i, true)) {
            return true;
        }
    }
    
    return false;
}

function getMarriageInstance(weddingId) {
    var em = cm.getEventManager(weddingEventName);
    
    for (var iterator = em.getInstances().iterator(); iterator.hasNext();) {
        var eim = iterator.next();
        
        if(eim.getIntProperty("weddingId") == weddingId) {
            return eim;
        }
    }
    
    return null;
}

function hasWeddingRing(player) {
    var rings = [1112806, 1112803, 1112807, 1112809];
    for (var i = 0; i < rings.length; i++) {
        if (player.haveItemWithId(rings[i], true)) {
            return true;
        }
    }
    
    return false;
}

function start() {  
    weddingIndoors = isWeddingIndoors(cm.getMapId());
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
        
        if(!weddingIndoors) {
            var hasEngagement = false;
            for (var x = 4031357; x <= 4031364; x++) {
                if (cm.haveItem(x, 1)) {
                    hasEngagement = true;
                    break;
                }
            }

            if (status == 0) {
                var text = "Welcome to the #bChapel#k! How can I help you?";
                var choice = ["How do I prepare a wedding?", "I have an engagement and want to arrange the wedding", "I am the guest and I'd like to go into the wedding"];
                for (x = 0; x < choice.length; x++) {
                    text += "\r\n#L" + x + "##b" + choice[x] + "#l";
                }
                
                if (cm.haveItem(5251100)) {
                    text += "\r\n#L" + x + "##bMake additional invitation cards#l";
                }
                
                cm.sendSimple(text);
            } else if (status == 1) {
                switch(selection) {
                    case 0:
                        cm.sendOk("Firstly you need to be #bengaged#k to someone. #p9201000# makes the engagement ring. Once attained the engagement status, purchase a #b#t" + weddingEntryTicketCommon + "##k.\r\nShow me your engagement ring and a wedding ticket, and I will book a reservation for you along with #r15 Wedding Tickets#k. Use them to invite your guests into the wedding. They need 1 each to enter.");
                        cm.dispose();
                        break;
                        
                    case 1:
                        if (hasEngagement) {
                            var wserv = cm.getClient().getWorldServer();
                            var cserv = cm.getClient().getChannelServer();
                            var weddingId = wserv.getRelationshipId(cm.getPlayer().getId());

                            if(weddingId > 0) {
                                if(cserv.isWeddingReserved(weddingId)) {    // registration check
                                    var placeTime = cserv.getWeddingReservationTimeLeft(weddingId);
                                    cm.sendOk("Your wedding is set to start at the #r" + placeTime + "#k. Don't be late!");
                                } else {
                                    var partner = wserv.getPlayerStorage().getCharacterById(cm.getPlayer().getPartnerId());
                                    if(partner == null) {
                                        cm.sendOk("Your partner seems to be offline right now... Make sure to get both gathered here when the time comes!");
                                        cm.dispose();
                                        return;
                                    }
                                    
                                    if(hasWeddingRing(cm.getPlayer()) || hasWeddingRing(partner)) {
                                        cm.sendOk("Either you or your partner already has a marriage ring.");
                                        cm.dispose();
                                        return;
                                    }

                                    if(!cm.getMap().equals(partner.getMap())) {
                                        cm.sendOk("Please let your partner come here as well to register the reservation.");
                                        cm.dispose();
                                        return;
                                    }

                                    if(!cm.canHold(weddingSendTicket, 15) || !partner.canHold(weddingSendTicket, 15)) {
                                        cm.sendOk("Either you or your partner doesn't have a free ETC slot for the Wedding tickets! Please make some room before trying to register a reservation.");
                                        cm.dispose();
                                        return;
                                    }

                                    var hasCommon = cm.haveItem(weddingEntryTicketCommon);
                                    var hasPremium = cm.haveItem(weddingEntryTicketPremium);

                                    if(hasCommon || hasPremium) {
                                        var weddingType = (hasPremium ? true : false);

                                        var player = cm.getPlayer();
                                        var resStatus = cserv.pushWeddingReservation(weddingId, cathedralWedding, weddingType, player.getId(), player.getPartnerId());
                                        if(resStatus > 0) {
                                            cm.gainItem((weddingType) ? weddingEntryTicketPremium : weddingEntryTicketCommon, -1);

                                            var expirationTime = cserv.getRelativeWeddingTicketExpireTime(resStatus);
                                            cm.gainItem(weddingSendTicket,15,false,true,expirationTime);
                                            partner.getClient().getAbstractPlayerInteraction().gainItem(weddingSendTicket,15,false,true,expirationTime);

                                            var placeTime = cserv.getWeddingReservationTimeLeft(weddingId);

                                            var wedType = weddingType ? "Premium" : "Regular";
                                            cm.sendOk("You both have received 15 Wedding Tickets, to be given to your guests. #bDouble-click the ticket#k to send it to someone. Invitations can only be sent #rbefore the wedding start time#k. Your #b" + wedType + " wedding#k is set to start at the #r" + placeTime + "#k. Don't be late!");

                                            player.dropMessage(6, "Wedding Assistant: You both have received 15 Wedding Tickets. Invitations can only be sent before the wedding start time. Your " + wedType + " wedding is set to start at the " + placeTime + ". Don't be late!");
                                            partner.dropMessage(6, "Wedding Assistant: You both have received 15 Wedding Tickets. Invitations can only be sent before the wedding start time. Your " + wedType + " wedding is set to start at the " + placeTime + ". Don't be late!");

                                            if(!hasSuitForWedding(player)) {
                                                player.dropMessage(5, "Wedding Assistant: Please purchase a wedding garment before showing up for the ceremony. One can be bought at the Wedding Shop left-most Amoria.");
                                            }

                                            if(!hasSuitForWedding(partner)) {
                                                partner.dropMessage(5, "Wedding Assistant: Please purchase a wedding garment before showing up for the ceremony. One can be bought at the Wedding Shop left-most Amoria.");
                                            }
                                        } else {
                                            cm.sendOk("Your wedding reservation must have been processed recently. Please try again later.");
                                        }
                                    } else {
                                        cm.sendOk("Please have a #b#t" + weddingEntryTicketCommon + "##k available on your CASH inventory before trying to register a reservation.");
                                    }
                                }
                            } else {
                                cm.sendOk("Wedding reservation encountered an error, try again later.");
                            }

                            cm.dispose();
                        } else {
                            cm.sendOk("You do not have an engagement ring.");
                            cm.dispose();
                        }
                        break;
                        
                    case 2:
                        if (cm.haveItem(weddingGuestTicket)) {
                            var cserv = cm.getClient().getChannelServer();

                            wid = cserv.getOngoingWedding(cathedralWedding);
                            if(wid > 0) {
                                if(cserv.isOngoingWeddingGuest(cathedralWedding, cm.getPlayer().getId())) {
                                    var eim = getMarriageInstance(wid);
                                    if(eim != null) {
                                        cm.sendOk("Enjoy the wedding. Don't drop your Gold Maple Leaf or you won't be able to finish the whole wedding.");
                                    } else {
                                        cm.sendOk("Please wait a moment while the couple get ready to enter the Chapel.");
                                        cm.dispose();
                                    }
                                } else {
                                    cm.sendOk("Sorry, but you have not been invited for this wedding.");
                                    cm.dispose();
                                }
                            } else {
                                cm.sendOk("There is no wedding booked right now.");
                                cm.dispose();
                            }
                        } else {
                            cm.sendOk("You do not have a #b#t" + weddingGuestTicket + "##k.");
                            cm.dispose();
                        }
                        break;
                        
                    default:
                        var wserv = cm.getClient().getWorldServer();
                        var cserv = cm.getClient().getChannelServer();
                        var weddingId = wserv.getRelationshipId(cm.getPlayer().getId());

                        var resStatus = cserv.getWeddingReservationStatus(weddingId, cathedralWedding);
                        if(resStatus > 0) {
                            if(cm.canHold(weddingSendTicket, 3)) {
                                cm.gainItem(5251100, -1);

                                var expirationTime = cserv.getRelativeWeddingTicketExpireTime(resStatus);
                                cm.gainItem(weddingSendTicket,3,false,true,expirationTime);
                            } else {
                                cm.sendOk("Please have a free ETC slot available to get more invitations.");
                            }
                        } else {
                            cm.sendOk("You're not currently booked on the Chapel to make additional invitations.");
                        }
                        
                        cm.dispose();
                }
            } else if (status == 2) {   // registering guest
                var eim = getMarriageInstance(wid);

                if(eim != null) {
                    cm.gainItem(weddingGuestTicket, -1);
                    eim.registerPlayer(cm.getPlayer());     //cm.warp(680000210, 0);
                } else {
                    cm.sendOk("The marriage event could not be found.");
                }

                cm.dispose();
            }
        } else {
            if (status == 0) {
                var eim = cm.getEventInstance();
                if(eim == null) {
                    cm.warp(680000000,0);
                    cm.dispose();
                    return;
                }

                isMarrying = (cm.getPlayer().getId() == eim.getIntProperty("groomId") || cm.getPlayer().getId() == eim.getIntProperty("brideId"));

                if(eim.getIntProperty("weddingStage") == 0) {
                    if(!isMarrying) {
                        cm.sendOk("Welcome to the #b#m" + cm.getMapId() + "##k. Please hang around with the groom and bride while the other guests are gathering here.\r\n\r\nWhen the timer reach it's end the couple will head to the altar, at that time you will be allowed to root over them from the #bguests area#k.");
                    } else {
                        cm.sendOk("Welcome to the #b#m" + cm.getMapId() + "##k. Please greet the guests that are already here while the others are coming. When the timer reach it's end the couple will head to the altar.");
                    }

                    cm.dispose();
                } else {
                    cm.sendYesNo("The #bbride and groom#k are already on their way to the altar. Would you like to join them now?");
                }
            } else if (status == 1) {
                cm.warp(weddingAltarMapid,"sp");            
                cm.dispose();
            }
        }
    }
}