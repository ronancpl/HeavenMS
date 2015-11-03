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
import constants.ExpTable;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.autoban.AutobanManager;
import client.inventory.Item;
import tools.Randomizer;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PetFoodHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        AutobanManager abm = chr.getAutobanManager();
        if (abm.getLastSpam(2) + 500 > System.currentTimeMillis()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        abm.spam(2);
        abm.setTimestamp(1, slea.readInt(), 3);
        if (chr.getNoPets() == 0) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        int previousFullness = 100;
        byte slot = 0;
        MaplePet[] pets = chr.getPets();
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getFullness() < previousFullness) {
                    slot = i;
                    previousFullness = pets[i].getFullness();
                }
            }
        }
        MaplePet pet = chr.getPet(slot);
        short pos = slea.readShort();
        int itemId = slea.readInt();
        Item use = chr.getInventory(MapleInventoryType.USE).getItem(pos);
        if (use == null || (itemId / 10000) != 212 || use.getItemId() != itemId) {
            return;
        }
        boolean gainCloseness = false;
        if (Randomizer.nextInt(101) > 50) {
            gainCloseness = true;
        }
        if (pet.getFullness() < 100) {
            int newFullness = pet.getFullness() + 30;
            if (newFullness > 100) {
                newFullness = 100;
            }
            pet.setFullness(newFullness);
            if (gainCloseness && pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + 1;
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                    pet.setLevel((byte) (pet.getLevel() + 1));
                    c.announce(MaplePacketCreator.showOwnPetLevelUp(chr.getPetIndex(pet)));
                    chr.getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), chr.getPetIndex(pet)));
                }
            }
            chr.getMap().broadcastMessage(MaplePacketCreator.commandResponse(chr.getId(), slot, 0, true));
        } else {
            if (gainCloseness) {
                int newCloseness = pet.getCloseness() - 1;
                if (newCloseness < 0) {
                    newCloseness = 0;
                }
                pet.setCloseness(newCloseness);
                if (pet.getLevel() > 1 && newCloseness < ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                    pet.setLevel((byte) (pet.getLevel() - 1));
                }
            }
            chr.getMap().broadcastMessage(MaplePacketCreator.commandResponse(chr.getId(), slot, 0, false));
        }
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, pos, (short) 1, false);
        
        pet.saveToDb();
        
        Item petz = chr.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
        if (petz == null){ //Not a real fix but fuck it you know?
        	return;
        }
        
        chr.forceUpdateItem(petz);
    }
}
