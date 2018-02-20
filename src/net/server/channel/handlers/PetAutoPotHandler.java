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
import tools.Pair;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import constants.ServerConstants;
import java.util.List;

/**
 *
 * @author Ronan (multi-pot consumption feature)
 */
public final class PetAutoPotHandler extends AbstractMaplePacketHandler {
    short slot;
    int itemId;
    Item toUse;
    List<Item> toUseList;
    
    boolean hasHpGain;
    boolean hasMpGain;
    short maxHp;
    short maxMp;
    short incHp;
    short incMp;
    int curHp;
    int curMp;
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        slea.readByte();
        slea.readLong();
        slea.readInt();
        slot = slea.readShort();
        itemId = slea.readInt();
        
        MapleCharacter chr = c.getPlayer();
        toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        
        if(toUse != null) {
            if (toUse.getItemId() != itemId) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            toUseList = null;
            
            // from now on, toUse becomes the "cursor" for the current pot being used
            if(toUse.getQuantity() <= 0) {
                if(!cursorOnNextAvailablePot(chr)) {
                    c.announce(MaplePacketCreator.enableActions());
                    return;
                }
            }
            
            MapleStatEffect stat = MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId());
            hasHpGain = stat.getHp() > 0 || stat.getHpRate() > 0.0;
            hasMpGain = stat.getMp() > 0 || stat.getMpRate() > 0.0;
            
            // contabilize the HP and MP gains from equipments on one's effective MaxHP/MaxMP
            Pair<Short, Short> maxHpMp = calcEffectivePool(chr);
            maxHp = maxHpMp.left;
            maxMp = maxHpMp.right;
            
            incHp = stat.getHp();
            if(incHp <= 0 && hasHpGain) incHp = (short)((maxHp * stat.getHpRate()) / 100.0);
            
            incMp = stat.getMp();
            if(incMp <= 0 && hasMpGain) incMp = (short)((maxMp * stat.getMpRate()) / 100.0);
            
            curHp = chr.getHp();
            curMp = chr.getMp();
            
            //System.out.println("\n-------------------\n");
            while(true) {
                do {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                    stat.applyTo(chr);
                    
                    curHp += incHp;
                    curMp += incMp;

                    //System.out.println();
                    //System.out.println("hp: " + hasHpGain + " player hp " + curHp + " maxhp " + maxHp);
                    //System.out.println("mp: " + hasMpGain + " player mp " + curMp + " maxmp " + maxMp);
                    //System.out.println("redo? " + (shouldReusePot(chr) && toUse.getQuantity() > 0));
                } while(shouldReusePot(chr) && toUse.getQuantity() > 0);

                if(toUse.getQuantity() == 0 && shouldReusePot(chr)) {
                    // depleted out the current slot, fetch for more

                    if(!cursorOnNextAvailablePot(chr)) {
                        break;    // no more pots available
                    }
                } else {
                    break;    // gracefully finished it's job, quit the loop
                }
            }
        }
    }
    
    private boolean cursorOnNextAvailablePot(MapleCharacter chr) {
        if(toUseList == null) {
            toUseList = chr.getInventory(MapleInventoryType.USE).linkedListById(itemId);
        }

        toUse = null;
        while(!toUseList.isEmpty()) {
            Item it = toUseList.remove(0);

            if(it.getQuantity() > 0) {
                toUse = it;
                slot = it.getPosition();

                return true;
            }
        }
        
        return false;
    }
    
    private Pair<Short, Short> calcEffectivePool(MapleCharacter chr) {
        short hp = 0, mp = 0;
        
        if(ServerConstants.USE_EQUIPS_ON_AUTOPOT) {
            for(Item i : chr.getInventory(MapleInventoryType.EQUIPPED).list()) {
                Equip e = (Equip) i;

                hp += e.getHp();
                mp += e.getMp();
            }
        }

        hp = (short) Math.min(chr.getMaxHp() + hp, 30000);
        mp = (short) Math.min(chr.getMaxMp() + mp, 30000);
        
        return new Pair<>(hp, mp);
    }
    
    private boolean shouldReusePot(MapleCharacter chr) {
        return (hasHpGain && curHp < ServerConstants.PET_AUTOHP_RATIO * maxHp) || (hasMpGain && curMp < ServerConstants.PET_AUTOMP_RATIO * maxMp);
    } 
}
