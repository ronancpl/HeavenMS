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

import constants.ServerConstants;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamilyEntitlement;
import client.MapleFamilyEntry;
import net.AbstractMaplePacketHandler;
import net.opcodes.SendOpcode;
import net.server.coordinator.MapleInviteCoordinator;
import net.server.coordinator.MapleInviteCoordinator.InviteType;
import server.maps.FieldLimit;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Moogra
 */
public final class FamilyUseHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if(!ServerConstants.USE_FAMILY_SYSTEM) {
            return;
        }
        MapleFamilyEntitlement type = MapleFamilyEntitlement.values()[slea.readInt()];
        int cost = type.getRepCost();
        MapleFamilyEntry entry = c.getPlayer().getFamilyEntry();
        if(entry.getReputation() < cost || entry.isEntitlementUsed(type)) {
            return; // shouldn't even be able to request it
        }
        c.announce(MaplePacketCreator.getFamilyInfo(entry));
        MapleCharacter victim;
        if(type == MapleFamilyEntitlement.FAMILY_REUINION || type == MapleFamilyEntitlement.SUMMON_FAMILY) {
            victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
            if(victim != null) {
                MapleMap targetMap = victim.getMap();
                MapleMap ownMap = c.getPlayer().getMap();
                if(targetMap != null) { // TODO:more checks for map restrictions/instance
                    if(type == MapleFamilyEntitlement.FAMILY_REUINION) {
                        if(!FieldLimit.CANNOTMIGRATE.check(ownMap.getFieldLimit()) && !FieldLimit.CANNOTVIPROCK.check(targetMap.getFieldLimit())
                                && (targetMap.getForcedReturnId() == 999999999 || targetMap.getId() < 100000000) && targetMap.getEventInstance() == null) {
                            
                            c.getPlayer().changeMap(victim.getMap(), victim.getMap().getPortal(0));
                            useEntitlement(entry, type);
                        } else {
                            c.announce(MaplePacketCreator.sendFamilyMessage(75, 0)); // wrong message, but close enough. (client should check this first anyway)
                            return;
                        }
                    } else {
                        if(!FieldLimit.CANNOTMIGRATE.check(targetMap.getFieldLimit()) && !FieldLimit.CANNOTVIPROCK.check(ownMap.getFieldLimit()) 
                                && (ownMap.getForcedReturnId() == 999999999 || ownMap.getId() < 100000000) && ownMap.getEventInstance() != null) {
                            
                            if(MapleInviteCoordinator.hasInvite(InviteType.FAMILY_SUMMON, victim.getId())) {
                                c.announce(MaplePacketCreator.sendFamilyMessage(74, 0));
                            }
                            MapleInviteCoordinator.createInvite(InviteType.FAMILY_SUMMON, c.getPlayer(), victim, victim.getId(), c.getPlayer().getMap());
                            victim.announce(MaplePacketCreator.sendFamilySummonRequest(c.getPlayer().getFamily().getName(), c.getPlayer().getName()));
                            useEntitlement(entry, type);
                        } else {
                            c.announce(MaplePacketCreator.sendFamilyMessage(75, 0));
                            return;
                        }
                    }
                }
            }
        } else {
            int typeIndex = type.ordinal(); // temporary
            int erate = typeIndex == 3 ? 150 : (typeIndex == 4 || typeIndex == 6 || typeIndex == 8 || typeIndex == 10 ? 200 : 100);
            int drate = typeIndex == 2 ? 150 : (typeIndex == 4 || typeIndex == 5 || typeIndex == 7 || typeIndex == 9 ? 200 : 100);
            if(typeIndex > 8) {
            } else {
                c.announce(useRep(drate == 100 ? 2 : (erate == 100 ? 3 : 4), typeIndex, erate, drate, ((typeIndex > 5 || typeIndex == 4) ? 2 : 1) * 15 * 60 * 1000));
            }
        }
    }
    
    private boolean useEntitlement(MapleFamilyEntry entry, MapleFamilyEntitlement entitlement) {
        if(entry.useEntitlement(entitlement)) {
            entry.gainReputation(-entitlement.getRepCost());
            return true;
        }
        return false;
    }

    /**
     * [65 00][02][08 00 00 00][C8 00 00 00][00 00 00 00][00][40 77 1B 00]
     */
    private static byte[] useRep(int mode, int type, int erate, int drate, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x60);//noty
        mplew.write(mode);
        mplew.writeInt(type);
        if (mode < 4) {
            mplew.writeInt(erate);
            mplew.writeInt(drate);
        }
        mplew.write(0);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    //20 00
    //00 00 00 00
    //00 00 00 00 00 00 00 00
    //80 01
    //00 00 28 00
    //8C 93 3E 00
    //40 0D
    //03 00 14 00
    //8C 93 3E 00
    //40 0D 03 00 00 00 00 00 02
    private static byte[] giveBuff() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
        mplew.writeInt(0);
        mplew.writeLong(0);

        return null;
    }
}
