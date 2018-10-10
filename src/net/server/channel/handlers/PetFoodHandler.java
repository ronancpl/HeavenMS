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
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.autoban.AutobanManager;
import client.inventory.Item;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PetFoodHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        AutobanManager abm = chr.getAutobanManager();
        if (abm.getLastSpam(2) + 500 > currentServerTime()) {
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
        if(pet == null) return;
        
        short pos = slea.readShort();
        int itemId = slea.readInt();
        
        if (c.tryacquireClient()) {
            try {
                MapleInventory useInv = chr.getInventory(MapleInventoryType.USE);
                useInv.lockInventory();
                try {
                    Item use = useInv.getItem(pos);
                    if (use == null || (itemId / 10000) != 212 || use.getItemId() != itemId || use.getQuantity() < 1) {
                        return;
                    }

                    pet.gainClosenessFullness(chr, (pet.getFullness() <= 75) ? 1 : 0, 30, 1);   // 25+ "emptyness" to get +1 closeness
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, pos, (short) 1, false);
                } finally {
                    useInv.unlockInventory();
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
