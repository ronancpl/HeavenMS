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
import client.Skill;
import client.SkillFactory;
import client.creator.veteran.*;
import client.inventory.Equip;
import client.inventory.Equip.ScrollResult;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import client.inventory.manipulator.MapleInventoryManipulator;
import client.inventory.manipulator.MapleKarmaManipulator;
import client.processor.AssignAPProcessor;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import scripting.npc.NPCScriptManager;
import server.MapleItemInformationProvider;
import server.MapleShop;
import server.MapleShopFactory;
import server.TimerManager;
import server.maps.AbstractMapleMapObject;
import server.maps.MaplePlayerShopItem;
import server.maps.MapleKite;
import server.maps.MapleMap;
import server.maps.MapleTVEffect;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public final class UseCashItemHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final MapleCharacter player = c.getPlayer();
        
        long timeNow = currentServerTime();
        if (timeNow - player.getLastUsedCashItem() < 3000) {
            player.dropMessage(1, "You have used a cash item recently. Wait a moment, then try again.");
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        player.setLastUsedCashItem(timeNow);
        
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readShort();
        int itemId = slea.readInt();
        int itemType = itemId / 10000;
        Item toUse = player.getInventory(MapleInventoryType.CASH).getItem(player.getInventory(MapleInventoryType.CASH).findById(itemId).getPosition());
        String medal = "";
        Item medalItem = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
        }
        if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        if (itemType == 504) { // vip teleport rock
            String error1 = "Either the player could not be found or you were trying to teleport to an illegal location.";
            boolean vip = slea.readByte() == 1;
            remove(c, itemId);
            if (!vip) {
                int mapId = slea.readInt();
                if (c.getChannelServer().getMapFactory().getMap(mapId).getForcedReturnId() == 999999999) {
                	player.changeMap(c.getChannelServer().getMapFactory().getMap(mapId));
                } else {
                    MapleInventoryManipulator.addById(c, itemId, (short) 1);
                    player.dropMessage(1, error1);
                    c.announce(MaplePacketCreator.enableActions());
                }
            } else {
                String name = slea.readMapleAsciiString();
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
                boolean success = false;
                if (victim != null) {
                    MapleMap target = victim.getMap();
                    if (c.getChannelServer().getMapFactory().getMap(victim.getMapId()).getForcedReturnId() == 999999999 || victim.getMapId() < 100000000) {
                        if (victim.gmLevel() <= player.gmLevel()) {
                            if (itemId == 5041000 || victim.getMapId() / player.getMapId() == 1) { //viprock & same continent
                                player.changeMap(target, target.findClosestPlayerSpawnpoint(victim.getPosition()));
                                success = true;
                            } else {
                                player.dropMessage(1, "You cannot teleport between continents with this teleport rock.");
                            }
                        } else {
                            player.dropMessage(1, error1);
                        }
                    } else {
                        player.dropMessage(1, "You cannot teleport to this map.");
                    }
                } else {
                    player.dropMessage(1, "Player could not be found in this channel.");
                }
                if (!success) {
                    MapleInventoryManipulator.addById(c, itemId, (short) 1);
                    c.announce(MaplePacketCreator.enableActions());
                }
            }
        } else if (itemType == 505) { // AP/SP reset
            if(!player.isAlive()) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            if (itemId > 5050000) {
                int SPTo = slea.readInt();
                int SPFrom = slea.readInt();
                Skill skillSPTo = SkillFactory.getSkill(SPTo);
                Skill skillSPFrom = SkillFactory.getSkill(SPFrom);
                byte curLevel = player.getSkillLevel(skillSPTo);
                byte curLevelSPFrom = player.getSkillLevel(skillSPFrom);
                if ((curLevel < skillSPTo.getMaxLevel()) && curLevelSPFrom > 0) {
                    player.changeSkillLevel(skillSPFrom, (byte) (curLevelSPFrom - 1), player.getMasterLevel(skillSPFrom), -1);
                    player.changeSkillLevel(skillSPTo, (byte) (curLevel + 1), player.getMasterLevel(skillSPTo), -1);
                }
            } else {
                int APTo = slea.readInt();
                int APFrom = slea.readInt();
                
                if(!AssignAPProcessor.APResetAction(c, APFrom, APTo)) {
                    return;
                }
            }
            remove(c, itemId);
        } else if (itemType == 506) {
            Item eq = null;
            if (itemId == 5060000) { // Item tag.
                int equipSlot = slea.readShort();
                if (equipSlot == 0) {
                    return;
                }
                eq = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) equipSlot);
                eq.setOwner(player.getName());
            } else if (itemId == 5060001 || itemId == 5061000 || itemId == 5061001 || itemId == 5061002 || itemId == 5061003) { // Sealing lock
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                eq = player.getInventory(type).getItem((short) slea.readInt());
                if (eq == null) { //Check if the type is EQUIPMENT?
                    return;
                }
                byte flag = eq.getFlag();
                flag |= ItemConstants.LOCK;
                if (eq.getExpiration() > -1) {
                    return; //No perma items pls
                }
                eq.setFlag(flag);

                long period = 0;
                if (itemId == 5061000) {
                    period = 7;
                } else if (itemId == 5061001) {
                    period = 30;
                } else if (itemId == 5061002) {
                    period = 90;
                } else if (itemId == 5061003) {
                    period = 365;
                }

                if (period > 0) {
                    eq.setExpiration(currentServerTime() + (period * 60 * 60 * 24 * 1000));
                }

                remove(c, itemId);
            } else if (itemId == 5060002) { // Incubator
                byte inventory2 = (byte) slea.readInt();
                short slot2 = (short) slea.readInt();
                Item item2 = player.getInventory(MapleInventoryType.getByType(inventory2)).getItem(slot2);
                if (item2 == null) // hacking
                {
                    return;
                }
                if (getIncubatedItem(c, itemId)) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(inventory2), slot2, (short) 1, false);
                    remove(c, itemId);
                }
                return;
            }
            slea.readInt(); // time stamp
            if (eq != null) {
                player.forceUpdateItem(eq);
                remove(c, itemId);
            }
        } else if (itemType == 507) {
            boolean whisper;
            switch ((itemId / 1000) % 10) {
                case 1: // Megaphone
                    if (player.getLevel() > 9) {
                        player.getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(2, medal + player.getName() + " : " + slea.readMapleAsciiString()));
                    } else {
                        player.dropMessage(1, "You may not use this until you're level 10.");
                        return;
                    }
                    break;
                case 2: // Super megaphone
                    Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.serverNotice(3, c.getChannel(), medal + player.getName() + " : " + slea.readMapleAsciiString(), (slea.readByte() != 0)));
                    break;
                case 5: // Maple TV
                    int tvType = itemId % 10;
                    boolean megassenger = false;
                    boolean ear = false;
                    MapleCharacter victim = null;
                    if (tvType != 1) {
                        if (tvType >= 3) {
                            megassenger = true;
                            if (tvType == 3) {
                                slea.readByte();
                            }
                            ear = 1 == slea.readByte();
                        } else if (tvType != 2) {
                            slea.readByte();
                        }
                        if (tvType != 4) {
                            victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                        }
                    }
                    List<String> messages = new LinkedList<>();
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < 5; i++) {
                        String message = slea.readMapleAsciiString();
                        if (megassenger) {
                            builder.append(" ").append(message);
                        }
                        messages.add(message);
                    }
                    slea.readInt();
                    
                    if (!MapleTVEffect.broadcastMapleTVIfNotActive(player, victim, messages, tvType)) {
                        player.dropMessage(1, "MapleTV is already in use.");
                        return;
                    }
                    
                    if (megassenger) {
                        Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.serverNotice(3, c.getChannel(), medal + player.getName() + " : " + builder.toString(), ear));
                    }
                    
                    break;
                case 6: //item megaphone
                    String msg = medal + player.getName() + " : " + slea.readMapleAsciiString();
                    whisper = slea.readByte() == 1;
                    Item item = null;
                    if (slea.readByte() == 1) { //item
                        item = player.getInventory(MapleInventoryType.getByType((byte) slea.readInt())).getItem((short) slea.readInt());
                        if (item == null) //hack
                        {
                            return;
                        } else if (item.isUntradeable()) {
                            player.dropMessage(1, "You cannot trade this item.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                    }
                    Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.itemMegaphone(msg, whisper, c.getChannel(), item));
                    break;
                case 7: //triple megaphone
                    int lines = slea.readByte();
                    if (lines < 1 || lines > 3) //hack
                    {
                        return;
                    }
                    String[] msg2 = new String[lines];
                    for (int i = 0; i < lines; i++) {
                        msg2[i] = medal + player.getName() + " : " + slea.readMapleAsciiString();
                    }
                    whisper = slea.readByte() == 1;
                    Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.getMultiMegaphone(msg2, c.getChannel(), whisper));
                    break;
            }
            remove(c, itemId);
        } else if (itemType == 508) {   // graduation banner, thanks to tmskdl12. Also, thanks ratency for first pointing lack of Kite handling
            MapleKite kite = new MapleKite(player, slea.readMapleAsciiString(), itemId);
            
            if (!GameConstants.isFreeMarketRoom(player.getMapId())) {
                player.getMap().spawnKite(kite);
                remove(c, itemId);
            } else {
                c.announce(MaplePacketCreator.sendCannotSpawnKite());
            }
        } else if (itemType == 509) {
            String sendTo = slea.readMapleAsciiString();
            String msg = slea.readMapleAsciiString();
            try {
                player.sendNote(sendTo, msg, (byte) 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            remove(c, itemId);
        } else if (itemType == 510) {
            player.getMap().broadcastMessage(MaplePacketCreator.musicChange("Jukebox/Congratulation"));
            remove(c, itemId);
        } else if (itemType == 512) {
            if (ii.getStateChangeItem(itemId) != 0) {
                for (MapleCharacter mChar : player.getMap().getCharacters()) {
                    ii.getItemEffect(ii.getStateChangeItem(itemId)).applyTo(mChar);
                }
            }
            player.getMap().startMapEffect(ii.getMsg(itemId).replaceFirst("%s", player.getName()).replaceFirst("%s", slea.readMapleAsciiString()), itemId);
            remove(c, itemId);
        } else if (itemType == 517) {
            MaplePet pet = player.getPet(0);
            if (pet == null) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            String newName = slea.readMapleAsciiString();
            pet.setName(newName);
            pet.saveToDb();
            
            Item item = player.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            if (item != null)
                player.forceUpdateItem(item);
            
            player.getMap().broadcastMessage(player, MaplePacketCreator.changePetName(player, newName, 1), true);
            c.announce(MaplePacketCreator.enableActions());
            remove(c, itemId);
        } else if (itemType == 520) {
            player.gainMeso(ii.getMeso(itemId), true, false, true);
            remove(c, itemId);
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 523) {
            int itemid = slea.readInt();
            
            if(!ServerConstants.USE_ENFORCE_ITEM_SUGGESTION) c.getWorldServer().addOwlItemSearch(itemid);
            player.setOwlSearch(itemid);
            List<Pair<MaplePlayerShopItem, AbstractMapleMapObject>> hmsAvailable = c.getWorldServer().getAvailableItemBundles(itemid);
            if(!hmsAvailable.isEmpty()) remove(c, itemId);
            
            c.announce(MaplePacketCreator.owlOfMinerva(c, itemid, hmsAvailable));
            c.announce(MaplePacketCreator.enableActions());
            
        } else if (itemType == 524) {
            for (byte i = 0; i < 3; i++) {
                MaplePet pet = player.getPet(i);
                if (pet != null) {
                    Pair<Integer, Boolean> p = pet.canConsume(itemId);
                    
                    if (p.getRight()) {
                        pet.gainClosenessFullness(player, p.getLeft(), 100, 1);
                        remove(c, itemId);
                        break;
                    }
                } else {
                    break;
                }
            }
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 530) {
            ii.getItemEffect(itemId).applyTo(player);
            remove(c, itemId);
        } else if (itemType == 533) {
            NPCScriptManager.getInstance().start(c, 9010009, null);
        } else if (itemType == 537) {
            player.setChalkboard(slea.readMapleAsciiString());
            player.getMap().broadcastMessage(MaplePacketCreator.useChalkboard(player, false));
            player.getClient().announce(MaplePacketCreator.enableActions());
        } else if (itemType == 539) {
            List<String> strLines = new LinkedList<>();
            for (int i = 0; i < 4; i++) {
                strLines.add(slea.readMapleAsciiString());
            }
            
            final int world = c.getWorld();
            Server.getInstance().broadcastMessage(world, MaplePacketCreator.getAvatarMega(player, medal, c.getChannel(), itemId, strLines, (slea.readByte() != 0)));
            TimerManager.getInstance().schedule(new Runnable() {
            	@Override
            	public void run() {
            		Server.getInstance().broadcastMessage(world, MaplePacketCreator.byeAvatarMega());
            	}
            }, 1000 * 10);
            remove(c, itemId);
        } else if (itemType == 543) {
            if(itemId == 5432000 && !c.gainCharacterSlot()) {
                player.dropMessage(1, "You have already used up all 12 extra character slots.");
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            String name = slea.readMapleAsciiString();
            int face = slea.readInt();
            int hair = slea.readInt();
            int haircolor = slea.readInt();
            int skin = slea.readInt();
            int gender = slea.readInt();
            int jobid = slea.readInt();
            int improveSp = slea.readInt();
            
            int createStatus;
            switch(jobid) {
                case 0:
                    createStatus = WarriorCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
                    break;
                    
                case 1:
                    createStatus = MagicianCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
                    break;
                    
                case 2:
                    createStatus = BowmanCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
                    break;
                    
                case 3:
                    createStatus = ThiefCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
                    break;
                    
                default:
                    createStatus = PirateCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
            }
            
            if(createStatus == 0) {
                c.announce(MaplePacketCreator.sendMapleLifeError(0));   // success!
                
                player.showHint("#bSuccess#k on creation of the new character through the Maple Life card.");
                remove(c, itemId);
            } else {
                if(createStatus == -1) {    // check name
                    c.announce(MaplePacketCreator.sendMapleLifeNameError());
                } else {
                    c.announce(MaplePacketCreator.sendMapleLifeError(-1 * createStatus));
                }
            }
        } else if (itemType == 545) { // MiuMiu's travel store
            if (player.getShop() == null) {
                MapleShop shop = MapleShopFactory.getInstance().getShop(1338);
                if (shop != null) {
                    shop.sendShop(c);
                    remove(c, itemId);
                }
            } else {
                c.announce(MaplePacketCreator.enableActions());
            }
        } else if (itemType == 550) { //Extend item expiration
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 552) {
            MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
            short slot = (short) slea.readInt();
            Item item = player.getInventory(type).getItem(slot);
            if (item == null || item.getQuantity() <= 0 || MapleKarmaManipulator.hasKarmaFlag(item) || !ii.isKarmaAble(item.getItemId())) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            if(MapleKarmaManipulator.hasUsedKarmaFlag(item)) {
                player.dropMessage(6, "Scissors of Karma was already used on this item.");
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            MapleKarmaManipulator.setKarmaFlag(item);
            player.forceUpdateItem(item);
            remove(c, itemId);
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 552) { //DS EGG THING
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 557) {
            slea.readInt();
            int itemSlot = slea.readInt();
            slea.readInt();
            final Equip equip = (Equip) player.getInventory(MapleInventoryType.EQUIP).getItem((short) itemSlot);
            if (equip.getVicious() == 2 || player.getInventory(MapleInventoryType.CASH).findById(5570000) == null) {
                return;
            }
            equip.setVicious(equip.getVicious() + 1);
            equip.setUpgradeSlots(equip.getUpgradeSlots() + 1);
            remove(c, itemId);
            c.announce(MaplePacketCreator.enableActions());
            c.announce(MaplePacketCreator.sendHammerData(equip.getVicious()));
            player.forceUpdateItem(equip);
        } else if (itemType == 561) { //VEGA'S SPELL
            if (slea.readInt() != 1) {
                return;
            }
            
            final byte eSlot = (byte) slea.readInt();
            final Item eitem = player.getInventory(MapleInventoryType.EQUIP).getItem(eSlot);
            
            if (slea.readInt() != 2) {
                return;
            }
            
            final byte uSlot = (byte) slea.readInt();
            final Item uitem = player.getInventory(MapleInventoryType.USE).getItem(uSlot);
            if (eitem == null || uitem == null) {
                return;
            }
            
            Equip toScroll = (Equip) eitem;
            if (toScroll.getUpgradeSlots() < 1) {
                c.announce(MaplePacketCreator.getInventoryFull());
                return;
            }
            
            //should have a check here against PE hacks
            if(itemId / 1000000 != 5) itemId = 0;
            
            player.toggleBlockCashShop();
            
            final int curlevel = toScroll.getLevel();
            c.announce(MaplePacketCreator.sendVegaScroll(0x40));
            
            final Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, uitem.getItemId(), false, itemId, player.isGM());
            c.announce(MaplePacketCreator.sendVegaScroll(scrolled.getLevel() > curlevel ? 0x41 : 0x43));
            //opcodes 0x42, 0x44: "this item cannot be used"; 0x39, 0x45: crashes
            
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, uSlot, (short) 1, false);
            remove(c, itemId);
            
            final MapleClient client = c;
            TimerManager.getInstance().schedule(new Runnable() {
            	@Override
            	public void run() {
                    if(!player.isLoggedin()) return;
                    
                    player.toggleBlockCashShop();
                    
                    final List<ModifyInventory> mods = new ArrayList<>();
                    mods.add(new ModifyInventory(3, scrolled));
                    mods.add(new ModifyInventory(0, scrolled));
                    client.announce(MaplePacketCreator.modifyInventory(true, mods));

                    ScrollResult scrollResult = scrolled.getLevel() > curlevel ? ScrollResult.SUCCESS : ScrollResult.FAIL;
                    player.getMap().broadcastMessage(MaplePacketCreator.getScrollEffect(player.getId(), scrollResult, false));
                    if (eSlot < 0 && (scrollResult == ScrollResult.SUCCESS)) {
                        player.equipChanged();
                    }

                    client.announce(MaplePacketCreator.enableActions());
            	}
            }, 1000 * 3);
        } else {
            System.out.println("NEW CASH ITEM: " + itemType + "\n" + slea.toString());
            c.announce(MaplePacketCreator.enableActions());
        }
    }

    private static void remove(MapleClient c, int itemId) {
        MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
    }

    private static boolean getIncubatedItem(MapleClient c, int id) {
        final int[] ids = {1012070, 1302049, 1302063, 1322027, 2000004, 2000005, 2020013, 2020015, 2040307, 2040509, 2040519, 2040521, 2040533, 2040715, 2040717, 2040810, 2040811, 2070005, 2070006, 4020009,};
        final int[] quantitys = {1, 1, 1, 1, 240, 200, 200, 200, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3};
        int amount = 0;
        for (int i = 0; i < ids.length; i++) {
            if (i == id) {
                amount = quantitys[i];
            }
        }
        if (c.getPlayer().getInventory(MapleInventoryType.getByType((byte) (id / 1000000))).isFull()) {
            return false;
        }
        MapleInventoryManipulator.addById(c, id, (short) amount);
        return true;
    }
}
