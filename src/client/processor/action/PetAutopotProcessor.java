/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2019 RonanLana (HeavenMS)

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
package client.processor.action;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import config.YamlConfig;
import java.util.List;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import tools.MaplePacketCreator;

/**
 *
 * @author Ronan - multi-pot consumption feature
 */
public class PetAutopotProcessor {
    
    private static class AutopotAction {
        
        private MapleClient c;
        private short slot;
        private int itemId;
        
        private Item toUse;
        private List<Item> toUseList;

        private boolean hasHpGain, hasMpGain;
        private int maxHp, maxMp, curHp, curMp;
        private double incHp, incMp;
        
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
        
        public AutopotAction(MapleClient c, short slot, int itemId) {
            this.c = c;
            this.slot = slot;
            this.itemId = itemId;
        }
        
        public void run() {
            MapleClient c = this.c;
            MapleCharacter chr = c.getPlayer();
            if (!chr.isAlive()) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            int useCount = 0, qtyCount = 0;
            MapleStatEffect stat = null;
            
            maxHp = chr.getCurrentMaxHp();
            maxMp = chr.getCurrentMaxMp();

            curHp = chr.getHp();
            curMp = chr.getMp();

            MapleInventory useInv = chr.getInventory(MapleInventoryType.USE);
            useInv.lockInventory();
            try {
                toUse = useInv.getItem(slot);
                if (toUse != null) {
                    if (toUse.getItemId() != itemId) {
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                    }

                    toUseList = null;

                    // from now on, toUse becomes the "cursor" for the current pot being used
                    if (toUse.getQuantity() <= 0) {
                        if (!cursorOnNextAvailablePot(chr)) {
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                    }

                    stat = MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId());
                    hasHpGain = stat.getHp() > 0 || stat.getHpRate() > 0.0;
                    hasMpGain = stat.getMp() > 0 || stat.getMpRate() > 0.0;
                    
                    incHp = stat.getHp();
                    if(incHp <= 0 && hasHpGain) incHp = Math.ceil(maxHp * stat.getHpRate());

                    incMp = stat.getMp();
                    if(incMp <= 0 && hasMpGain) incMp = Math.ceil(maxMp * stat.getMpRate());

                    if (YamlConfig.config.server.USE_COMPULSORY_AUTOPOT) {
                        if (hasHpGain) {
                            double hpRatio = (YamlConfig.config.server.PET_AUTOHP_RATIO * maxHp) - curHp;
                            if (hpRatio > 0.0) {
                                qtyCount = (int) Math.ceil(hpRatio / incHp);
                            }
                        }

                        if (hasMpGain) {
                            double mpRatio = ((YamlConfig.config.server.PET_AUTOMP_RATIO * maxMp) - curMp);
                            if (mpRatio > 0.0) {
                                qtyCount = Math.max(qtyCount, (int) Math.ceil(mpRatio / incMp));
                            }
                        }
                        
                        if (qtyCount < 0) { // thanks Flint, Kevs for noticing an issue where negative counts were getting achieved
                            qtyCount = 0;
                        }
                    } else {
                        qtyCount = 1;   // non-compulsory autopot concept thanks to marcuswoon
                    }

                    while (true) {
                        short qtyToUse = (short) Math.min(qtyCount, toUse.getQuantity());
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, qtyToUse, false);

                        curHp += (incHp * qtyToUse);
                        curMp += (incMp * qtyToUse);

                        useCount += qtyToUse;
                        qtyCount -= qtyToUse;

                        if(toUse.getQuantity() == 0 && qtyCount > 0) {
                            // depleted out the current slot, fetch for more

                            if(!cursorOnNextAvailablePot(chr)) {
                                break;    // no more pots available
                            }
                        } else {
                            break;    // gracefully finished it's job, quit the loop
                        }
                    }
                }
            } finally {
                useInv.unlockInventory();
            }

            if (stat != null) {
                for (int i = 0; i < useCount; i++) {
                    stat.applyTo(chr);
                }
            }

            chr.announce(MaplePacketCreator.enableActions());
        }
    }
    
    public static void runAutopotAction(MapleClient c, short slot, int itemid) {
        AutopotAction action = new AutopotAction(c, slot, itemid);
        action.run();
    }
    
}
