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

import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamilyEntitlement;
import client.MapleFamilyEntry;
import config.YamlConfig;
import net.AbstractMaplePacketHandler;
import net.server.coordinator.world.MapleInviteCoordinator;
import net.server.coordinator.world.MapleInviteCoordinator.InviteType;
import server.maps.FieldLimit;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Moogra
 * @author Ubaware
 */
public final class FamilyUseHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if(!YamlConfig.config.server.USE_FAMILY_SYSTEM) {
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
            if(victim != null && victim != c.getPlayer()) {
                if(victim.getFamily() == c.getPlayer().getFamily()) {
                    MapleMap targetMap = victim.getMap();
                    MapleMap ownMap = c.getPlayer().getMap();
                    if(targetMap != null) {
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
                                    && (ownMap.getForcedReturnId() == 999999999 || ownMap.getId() < 100000000) && ownMap.getEventInstance() == null) {
                                
                                if(MapleInviteCoordinator.hasInvite(InviteType.FAMILY_SUMMON, victim.getId())) {
                                    c.announce(MaplePacketCreator.sendFamilyMessage(74, 0));
                                    return;
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
                } else {
                    c.announce(MaplePacketCreator.sendFamilyMessage(67, 0));
                }
            }
        } else if(type == MapleFamilyEntitlement.FAMILY_BONDING) {
            //not implemented
        } else {
            boolean party = false;
            boolean isExp = false;
            float rate = 1.5f;
            int duration = 15;
            do {
                switch(type) {
                case PARTY_EXP_2_30MIN:
                    party = true;
                    isExp = true;
                    type = MapleFamilyEntitlement.SELF_EXP_2_30MIN;
                    continue;
                case PARTY_DROP_2_30MIN:
                    party = true;
                    type = MapleFamilyEntitlement.SELF_DROP_2_30MIN;
                    continue;
                case SELF_DROP_2_30MIN:
                    duration = 30;
                case SELF_DROP_2:
                    rate = 2.0f;
                case SELF_DROP_1_5:
                    break;
                case SELF_EXP_2_30MIN:
                    duration = 30;
                case SELF_EXP_2:
                    rate = 2.0f;
                case SELF_EXP_1_5:
                    isExp = true;
                default:
                    break;
                }
                break;
            } while(true);
            //not implemented
        }
    }
    
    private boolean useEntitlement(MapleFamilyEntry entry, MapleFamilyEntitlement entitlement) {
        if(entry.useEntitlement(entitlement)) {
            entry.gainReputation(-entitlement.getRepCost(), false);
            entry.getChr().announce(MaplePacketCreator.getFamilyInfo(entry));
            return true;
        }
        return false;
    }
}
