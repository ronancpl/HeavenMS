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
import client.MapleJob;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Equip.ScrollResult;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import constants.ItemConstants;
import constants.ServerConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShop;
import server.MapleShopFactory;
import server.TimerManager;
import server.maps.AbstractMapleMapObject;
import server.maps.MaplePlayerShopItem;
import server.maps.MapleMap;
import server.maps.MapleTVEffect;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public final class UseCashItemHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final MapleCharacter player = c.getPlayer();
        if (System.currentTimeMillis() - player.getLastUsedCashItem() < 3000) {
            player.dropMessage(1, "You have used a cash item recently. Wait a moment, then try again.");
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        player.setLastUsedCashItem(System.currentTimeMillis());
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readShort();
        int itemId = slea.readInt();
        int itemType = itemId / 10000;
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(c.getPlayer().getInventory(MapleInventoryType.CASH).findById(itemId).getPosition());
        String medal = "";
        Item medalItem = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
        }
        if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        if (itemType == 505) { // AP/SP reset
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
                List<Pair<MapleStat, Integer>> statupdate = new ArrayList<>(2);
                int APTo = slea.readInt();
                int APFrom = slea.readInt();
                switch (APFrom) {
                    case 64: // str
                        if (player.getStr() < 5) {
                            c.getPlayer().message("You don't have the minimum STR required to swap.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        player.addStat(1, -1);
                        break;
                    case 128: // dex
                        if (player.getDex() < 5) {
                            c.getPlayer().message("You don't have the minimum DEX required to swap.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        player.addStat(2, -1);
                        break;
                    case 256: // int
                        if (player.getInt() < 5) {
                            c.getPlayer().message("You don't have the minimum INT required to swap.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        player.addStat(3, -1);
                        break;
                    case 512: // luk
                        if (player.getLuk() < 5) {
                            c.getPlayer().message("You don't have the minimum LUK required to swap.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        player.addStat(4, -1);
                        break;
                    case 2048: // HP
                        if(ServerConstants.USE_ENFORCE_HPMP_SWAP) {
                            if (APTo != 8192) {
                                c.getPlayer().message("You can only swap HP ability points to MP.");
                                c.announce(MaplePacketCreator.enableActions());
                                return;
                            }
                        }
                        if (player.getHpMpApUsed() < 1) {
                            c.getPlayer().message("You don't have enough HPMP stat points to spend on AP Reset.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                    	}
                        
                        int hp = player.getMaxHp();
                        int level_ = player.getLevel();
                        
                        boolean canWash_ = true;
                        if (hp < level_ * 14 + 148) {
                            canWash_ = false;
                        }
                        
                        if (!canWash_) {
                            c.getPlayer().message("You don't have the minimum HP pool required to swap.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        
                        player.setHpMpApUsed(player.getHpMpApUsed() - 1);
                        int hplose = -DistributeAPHandler.takeHp(player, player.getJob());
                        int nextHp = Math.max(1, player.getHp() + hplose), nextMaxHp = Math.max(50, player.getMaxHp() + hplose);

                        player.setHp(nextHp);
                        player.setMaxHp(nextMaxHp);
                        statupdate.add(new Pair<>(MapleStat.HP, nextHp));
                        statupdate.add(new Pair<>(MapleStat.MAXHP, nextMaxHp));
                        
                        break;
                    case 8192: // MP
                        if(ServerConstants.USE_ENFORCE_HPMP_SWAP) {
                            if (APTo != 2048) {
                                c.getPlayer().message("You can only swap MP ability points to HP.");
                                c.announce(MaplePacketCreator.enableActions());
                                return;
                            }
                        }
                        if (player.getHpMpApUsed() < 1) {
                            c.getPlayer().message("You don't have enough HPMP stat points to spend on AP Reset.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                    	}
                        
                        int mp = player.getMaxMp();
                        int level = player.getLevel();
                        MapleJob job = player.getJob();
                        
                        boolean canWash = true;
                        if (job.isA(MapleJob.SPEARMAN) && mp < 4 * level + 156) {
                            canWash = false;
                        } else if (job.isA(MapleJob.FIGHTER) && mp < 4 * level + 56) {
                            canWash = false;
                        } else if (job.isA(MapleJob.THIEF) && job.getId() % 100 > 0 && mp < level * 14 - 4) {
                            canWash = false;
                        } else if (mp < level * 14 + 148) {
                            canWash = false;
                        }
                        
                        if (!canWash) {
                            c.getPlayer().message("You don't have the minimum MP pool required to swap.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        
                        player.setHpMpApUsed(player.getHpMpApUsed() - 1);
                        int mplose = -DistributeAPHandler.takeMp(player, job);
                        int nextMp = Math.max(0, player.getMp() + mplose), nextMaxMp = Math.max(5, player.getMaxMp() + mplose);

                        player.setMp(nextMp);
                        player.setMaxMp(nextMaxMp);
                        statupdate.add(new Pair<>(MapleStat.MP, nextMp));
                        statupdate.add(new Pair<>(MapleStat.MAXMP, nextMaxMp));
                        
                        break;
                    default:
                        c.announce(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, c.getPlayer()));
                        return;
                }
                DistributeAPHandler.addStat(c, APTo, true);
                c.announce(MaplePacketCreator.updatePlayerStats(statupdate, true, c.getPlayer()));
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
                eq = c.getPlayer().getInventory(type).getItem((short) slea.readInt());
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
                    eq.setExpiration(System.currentTimeMillis() + (period * 60 * 60 * 24 * 1000));
                }

                remove(c, itemId);
            } else if (itemId == 5060002) { // Incubator
                byte inventory2 = (byte) slea.readInt();
                short slot2 = (short) slea.readInt();
                Item item2 = c.getPlayer().getInventory(MapleInventoryType.getByType(inventory2)).getItem(slot2);
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
            switch (itemId / 1000 % 10) {
                case 1: // Megaphone
                    if (player.getLevel() > 9) {
                        player.getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(2, medal + player.getName() + " : " + slea.readMapleAsciiString()));
                    } else {
                        player.dropMessage(1, "You may not use this until you're level 10.");
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
                    if (megassenger) {
                        Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.serverNotice(3, c.getChannel(), medal + player.getName() + " : " + builder.toString(), ear));
                    }
                    if (!MapleTVEffect.isActive()) {
                        new MapleTVEffect(player, victim, messages, tvType);
                        remove(c, itemId);
                    } else {
                        player.dropMessage(1, "MapleTV is already in use.");
                        return;
                    }
                    break;
                case 6: //item megaphone
                    String msg = medal + c.getPlayer().getName() + " : " + slea.readMapleAsciiString();
                    whisper = slea.readByte() == 1;
                    Item item = null;
                    if (slea.readByte() == 1) { //item
                        item = c.getPlayer().getInventory(MapleInventoryType.getByType((byte) slea.readInt())).getItem((short) slea.readInt());
                        if (item == null) //hack
                        {
                            return;
                        } else if (((item.getFlag() & ItemConstants.UNTRADEABLE) == ItemConstants.UNTRADEABLE) || ii.isDropRestricted(item.getItemId())) {
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
                        msg2[i] = medal + c.getPlayer().getName() + " : " + slea.readMapleAsciiString();
                    }
                    whisper = slea.readByte() == 1;
                    Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.getMultiMegaphone(msg2, c.getChannel(), whisper));
                    break;
            }
            remove(c, itemId);
        } else if (itemType == 508) { //graduation banner
            slea.readMapleAsciiString(); // message, separated by 0A for lines
            c.announce(MaplePacketCreator.enableActions());
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
                for (MapleCharacter mChar : c.getPlayer().getMap().getCharacters()) {
                    ii.getItemEffect(ii.getStateChangeItem(itemId)).applyTo(mChar);
                }
            }
            player.getMap().startMapEffect(ii.getMsg(itemId).replaceFirst("%s", c.getPlayer().getName()).replaceFirst("%s", slea.readMapleAsciiString()), itemId);
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
        } else if (itemType == 504) { // vip teleport rock
            String error1 = "Either the player could not be found or you were trying to teleport to an illegal location.";
            boolean vip = slea.readByte() == 1;
            remove(c, itemId);
            if (!vip) {
                int mapId = slea.readInt();
                if (c.getChannelServer().getMapFactory().getMap(mapId).getForcedReturnId() == 999999999) {
                	player.changeMap(c.getChannelServer().getMapFactory().getMap(mapId));
                } else {
                    MapleInventoryManipulator.addById(c, itemId, (short) 1);
                    c.getPlayer().dropMessage(1, error1);
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
        } else if (itemType == 520) {
            player.gainMeso(ii.getMeso(itemId), true, false, true);
            remove(c, itemId);
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 523) {
            int itemid = slea.readInt();
            
            if(!ServerConstants.USE_ENFORCE_OWL_SUGGESTIONS) c.getWorldServer().addOwlItemSearch(itemid);
            player.setOwlSearch(itemid);
            List<Pair<MaplePlayerShopItem, AbstractMapleMapObject>> hmsAvailable = c.getWorldServer().getAvailableItemBundles(itemid);
            if(!hmsAvailable.isEmpty()) remove(c, itemId);
            
            c.announce(MaplePacketCreator.owlOfMinerva(c, itemid, hmsAvailable));
            c.announce(MaplePacketCreator.enableActions());
            
        } else if (itemType == 524) {
            for (byte i = 0; i < 3; i++) {
                MaplePet pet = player.getPet(i);
                if (pet != null) {
                    if (pet.canConsume(itemId)) {
                        pet.gainClosenessFullness(player, 100, 100, 1);
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
            Server.getInstance().broadcastMessage(world, MaplePacketCreator.getAvatarMega(c.getPlayer(), medal, c.getChannel(), itemId, strLines, (slea.readByte() != 0)));
            TimerManager.getInstance().schedule(new Runnable() {
            	@Override
            	public void run() {
            		Server.getInstance().broadcastMessage(world, MaplePacketCreator.byeAvatarMega());
            	}
            }, 1000 * 10);
            remove(c, itemId);
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
            Item item = c.getPlayer().getInventory(type).getItem(slot);
            if (item == null || item.getQuantity() <= 0 || (item.getFlag() & ItemConstants.KARMA) > 0 && ii.isKarmaAble(item.getItemId())) {
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            if (!type.equals(MapleInventoryType.USE)) {
                item.setFlag((byte) ItemConstants.KARMA);
            }

            c.getPlayer().forceUpdateItem(item);
            remove(c, itemId);
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 552) { //DS EGG THING
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 557) {
            slea.readInt();
            int itemSlot = slea.readInt();
            slea.readInt();
            final Equip equip = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) itemSlot);
            if (equip.getVicious() == 2 || c.getPlayer().getInventory(MapleInventoryType.CASH).findById(5570000) == null) {
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
            final Item eitem = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(eSlot);
            
            if (slea.readInt() != 2) {
                return;
            }
            
            final byte uSlot = (byte) slea.readInt();
            final Item uitem = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(uSlot);
            if (eitem == null || uitem == null) {
                return;
            }
            
            Equip toScroll = (Equip) eitem;
            if (toScroll.getUpgradeSlots() < 1) {
                c.getSession().write(MaplePacketCreator.getInventoryFull());
                return;
            }
            
            //should have a check here against PE hacks
            if(itemId / 1000000 != 5) itemId = 0;
            
            c.getPlayer().toggleBlockCashShop();
            
            final int curlevel = toScroll.getLevel();
            c.getSession().write(MaplePacketCreator.sendVegaScroll(0x40));
            
            final Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, uitem.getItemId(), false, itemId, c.getPlayer().isGM());
            c.getSession().write(MaplePacketCreator.sendVegaScroll(scrolled.getLevel() > curlevel ? 0x41 : 0x43));
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

                    client.getSession().write(MaplePacketCreator.enableActions());
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
