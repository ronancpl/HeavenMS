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
package net.server.channel.handlers;

import net.AbstractMaplePacketHandler;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;

public final class PartyOperationHandler extends AbstractMaplePacketHandler {
    
    public static void leaveParty(MapleParty party, MaplePartyCharacter partyplayer, MapleClient c) {
        World world = c.getWorldServer();
        MapleCharacter player = c.getPlayer();
        
        if (party != null && partyplayer != null) {
            if (partyplayer.getId() == party.getLeaderId()) {
                c.getWorldServer().removeMapPartyMembers(party.getId());

                world.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                if (player.getEventInstance() != null) {
                    player.getEventInstance().disbandParty();
                }
            } else {
                player.getMap().removePartyMember(player);

                world.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                if (player.getEventInstance() != null) {
                    player.getEventInstance().leftParty(player);
                }
            }

            player.setParty(null);
        }
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int operation = slea.readByte();
        MapleCharacter player = c.getPlayer();
        World world = c.getWorldServer();
        MapleParty party = player.getParty();
        MaplePartyCharacter partyplayer = player.getMPC();
        switch (operation) {
            case 1: { // create
               	if(player.getLevel() < 10 && !ServerConstants.USE_PARTY_FOR_STARTERS) {
                    c.announce(MaplePacketCreator.partyStatusMessage(10));
                    return;
            	}
                if (player.getParty() == null) {
                    partyplayer = new MaplePartyCharacter(player);
                    party = world.createParty(partyplayer);
                    player.setParty(party);
                    player.setMPC(partyplayer);
                    player.getMap().addPartyMember(player);
                    player.silentPartyUpdate();
                    c.announce(MaplePacketCreator.partyCreated(partyplayer));
                } else {
                    c.announce(MaplePacketCreator.serverNotice(5, "You can't create a party as you are already in one."));
                }
                break;
            }
            case 2: { // leave/disband
                leaveParty(party, partyplayer, c);
                break;
            }
            case 3: { // join
                int partyid = slea.readInt();
                if (c.getPlayer().getParty() == null) {
                        party = world.getParty(partyid);
                        if (party != null) {
                            if (party.getMembers().size() < 6) {
                                partyplayer = new MaplePartyCharacter(player);
                                player.getMap().addPartyMember(player);
                                
                                world.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                                player.receivePartyMemberHP();
                                player.updatePartyMemberHP();
                            } else {
                                c.announce(MaplePacketCreator.partyStatusMessage(17));
                            }
                        } else {
                            c.announce(MaplePacketCreator.serverNotice(5, "The person you have invited to the party is already in one."));
                        }
                } else {
                    c.announce(MaplePacketCreator.serverNotice(5, "You can't join the party as you are already in one."));
                }
                break;
            }
            case 4: { // invite
                String name = slea.readMapleAsciiString();
                MapleCharacter invited = world.getPlayerStorage().getCharacterByName(name);
                if (invited != null) {
                    if(invited.getLevel() < 10 && (!ServerConstants.USE_PARTY_FOR_STARTERS || player.getLevel() >= 10)) { //min requirement is level 10
                        c.announce(MaplePacketCreator.serverNotice(5, "The player you have invited does not meet the requirements."));
                        return;
                    }
                    if(ServerConstants.USE_PARTY_FOR_STARTERS && invited.getLevel() >= 10 && player.getLevel() < 10) {    //trying to invite high level
                        c.announce(MaplePacketCreator.serverNotice(5, "The player you have invited does not meet the requirements."));
                        return;
                    }
                    
                    if (invited.getParty() == null) {
                        if (player.getParty() == null) {
                            if(player.getLevel() < 10 && !ServerConstants.USE_PARTY_FOR_STARTERS) {
                                c.announce(MaplePacketCreator.partyStatusMessage(10));
                                return;
                            }
                            
                            partyplayer = new MaplePartyCharacter(player);
                            party = world.createParty(partyplayer);
                            player.setParty(party);
                            player.setMPC(partyplayer);
                            player.getMap().addPartyMember(player);
                            c.announce(MaplePacketCreator.partyCreated(partyplayer));
                        }
                        if (party.getMembers().size() < 6) {
                            invited.getClient().announce(MaplePacketCreator.partyInvite(player));
                        } else {
                            c.announce(MaplePacketCreator.partyStatusMessage(17));
                        }
                    } else {
                        c.announce(MaplePacketCreator.partyStatusMessage(16));
                    }
                } else {
                    c.announce(MaplePacketCreator.partyStatusMessage(19));
                }
                break;
            }
            case 5: { // expel
                int cid = slea.readInt();
                if (partyplayer.equals(party.getLeader())) {
                    MaplePartyCharacter expelled = party.getMemberById(cid);
                    if (expelled != null) {
                        MapleCharacter emc = expelled.getPlayer();
                        if(emc != null) {
                            MapleMap map = emc.getMap();
                            if(map != null) map.removePartyMember(emc);
                            
                            EventInstanceManager eim = emc.getEventInstance();
                            if(eim != null) {
                                eim.leftParty(emc);
                            }
                            
                            emc.setParty(null);
                        }
                        
                        world.updateParty(party.getId(), PartyOperation.EXPEL, expelled);
                    }
                }
                break;
            }
            case 6: { // change leader
                int newLeader = slea.readInt();
                MaplePartyCharacter newLeadr = party.getMemberById(newLeader);
                world.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newLeadr);
                break;
            }
        }    
    }
}