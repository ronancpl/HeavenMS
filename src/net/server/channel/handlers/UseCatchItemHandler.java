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
import client.inventory.MapleInventoryType;
import client.autoban.AutobanManager;
import constants.inventory.ItemConstants;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public final class UseCatchItemHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        AutobanManager abm = chr.getAutobanManager();
        slea.readInt();
        abm.setTimestamp(5, Server.getInstance().getCurrentTimestamp(), 4);
        slea.readShort();
        int itemId = slea.readInt();
        int monsterid = slea.readInt();
        
        MapleMonster mob = chr.getMap().getMonsterByOid(monsterid);
        if (chr.getInventory(ItemConstants.getInventoryType(itemId)).countById(itemId) <= 0) {
           return;
        }
        if (mob == null) {
           return;
        }
        switch (itemId) {
            case 2270000:
                if (mob.getId() == 9300101) {
                    chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                    mob.getMap().killMonster(mob, null, false);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                    MapleInventoryManipulator.addById(c, 1902000, (short) 1, "", -1);
                 }
                c.announce(MaplePacketCreator.enableActions());
                break;
            case 2270001:
                if (mob.getId() == 9500197) {
                    if ((abm.getLastSpam(10) + 1000) < currentServerTime()) {
                        if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                            chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                            mob.getMap().killMonster(mob, null, false);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                            MapleInventoryManipulator.addById(c, 4031830, (short) 1, "", -1);
                        } else {
                            abm.spam(10);
                            c.announce(MaplePacketCreator.catchMessage(0));
                        }
                    }
                    c.announce(MaplePacketCreator.enableActions());
                }
                break;
            case 2270002:
                if (mob.getId() == 9300157) {
                    if ((abm.getLastSpam(10) + 800) < currentServerTime()) {
                        if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                            if (chr.canHold(4031868, 1)) {
                                if (Math.random() < 0.5) { // 50% chance
                                    chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                                    mob.getMap().killMonster(mob, null, false);
                                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                                    MapleInventoryManipulator.addById(c, 4031868, (short) 1, "", -1);
                                } else {
                                    chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 0));
                                }
                            } else {
                                chr.dropMessage(5, "Make a ETC slot available before using this item.");
                            }
                            
                            abm.spam(10);
                        } else {
                            c.announce(MaplePacketCreator.catchMessage(0));
                        }
                    }
                    c.announce(MaplePacketCreator.enableActions());
                }
                break;
            case 2270003:
                if (mob.getId() == 9500320) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 4031887, (short) 1, "", -1);
                    } else {
                        c.announce(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.announce(MaplePacketCreator.enableActions());
                break;
            case 2270005:
                if (mob.getId() == 9300187) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 2109001, (short) 1, "", -1);
                    } else {
                        c.announce(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.announce(MaplePacketCreator.enableActions());
                break;
            case 2270006:
                if (mob.getId() == 9300189) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 2109002, (short) 1, "", -1);
                    } else {
                        c.announce(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.announce(MaplePacketCreator.enableActions());
                break;
            case 2270007:
                if (mob.getId() == 9300191) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 2109003, (short) 1, "", -1);
                    } else {
                        c.announce(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.announce(MaplePacketCreator.enableActions());
                break;
            case 2270004:
                if (mob.getId() == 9300175) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                    chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                    mob.getMap().killMonster(mob, null, false);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                    MapleInventoryManipulator.addById(c, 4001169, (short) 1, "", -1);
                    } else {
                    c.announce(MaplePacketCreator.catchMessage(0));
                    }
                }
                c.announce(MaplePacketCreator.enableActions());
                break;
            case 2270008:
                if (mob.getId() == 9500336) {
                    if ((abm.getLastSpam(10) + 3000) < currentServerTime()) {
                        abm.spam(10);
                        chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addById(c, 2022323, (short) 1, "", -1);
                    } else {
                        chr.message("You cannot use the Fishing Net yet.");
                    }
                    c.announce(MaplePacketCreator.enableActions());
                }
                break;
            default:
                // proper Fish catch, thanks to Dragohe4rt
                
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                int itemGanho = ii.getCreateItem(itemId);
                int mobItem = ii.getMobItem(itemId);
                
                if (itemGanho != 0 && mobItem == mob.getId()) {
                    int timeCatch = ii.getUseDelay(itemId);
                    int mobHp = ii.getMobHP(itemId);
                    
                    if (timeCatch != 0 && (abm.getLastSpam(10) + timeCatch) < currentServerTime()) {
                        if (mobHp != 0 && mob.getHp() < ((mob.getMaxHp() / 100) * mobHp)) {
                            chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                            mob.getMap().killMonster(mob, null, false);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                            MapleInventoryManipulator.addById(c, itemGanho, (short) 1, "", -1);
                        } else if (mob.getId() != 9500336) {
                            if (mobHp != 0) {
                                abm.spam(10);
                                c.announce(MaplePacketCreator.catchMessage(0));
                            }
                        } else {
                            chr.message("You cannot use the Fishing Net yet.");
                        }
                    }
                }
                c.announce(MaplePacketCreator.enableActions());
                
                // System.out.println("UseCatchItemHandler: \r\n" + slea.toString());
        }
    }
}
