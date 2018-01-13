/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>
    Copyleft (L) 2017 RonanLana (HeavenMS)

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

import client.MapleClient;
import client.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import constants.ServerConstants;

/**
 *
 * @author Ronan (multi-pot consumption feature)
 */
public final class PetAutoPotHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        MapleCharacter chr = c.getPlayer();
        short maxHp = 0, maxMp = 0;
        
        if(ServerConstants.USE_EQUIPS_ON_AUTOPOT) {
            for(Item i : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).list()) {
                Equip e = (Equip) i;

                maxHp += e.getHp();
                maxMp += e.getMp();
            }
        }
                
        maxHp = (short) Math.min(chr.getMaxHp() + maxHp, 30000);
        maxMp = (short) Math.min(chr.getMaxMp() + maxMp, 30000);
        
        slea.readByte();
        slea.readLong();
        slea.readInt();
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        
        if(toUse != null) {
            MapleStatEffect stat = MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId());
            
            if (toUse.getQuantity() <= 0 || toUse.getItemId() != itemId) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            do {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                stat.applyTo(chr);

                //System.out.println();
                //System.out.println("hp: " + stat.getHp() + " player hp " + c.getPlayer().getHp() + " maxhp " + maxHp);
                //System.out.println("mp: " + stat.getMp() + " player mp " + c.getPlayer().getMp() + " maxmp " + maxMp);
                //System.out.println("redo? " + (((stat.getHp() > 0 && c.getPlayer().getHp() < ServerConstants.PET_AUTOHP_RATIO * maxHp) || (stat.getMp() > 0 && c.getPlayer().getMp() < ServerConstants.PET_AUTOMP_RATIO * maxMp)) && toUse.getQuantity() > 0));
            } while(((stat.getHp() > 0 && chr.getHp() < ServerConstants.PET_AUTOHP_RATIO * maxHp) || (stat.getMp() > 0 && chr.getMp() < ServerConstants.PET_AUTOMP_RATIO * maxMp)) && toUse.getQuantity() > 0);
        }
    }
}
