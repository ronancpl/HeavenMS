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
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ExpTable;
import constants.ItemConstants;

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
import server.maps.MapleMap;
import server.maps.MapleTVEffect;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public final class UseCashItemHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (System.currentTimeMillis() - player.getLastUsedCashItem() < 3000) {
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
                            return;
                        }
                        player.addStat(1, -1);
                        break;
                    case 128: // dex
                        if (player.getDex() < 5) {
                            return;
                        }
                        player.addStat(2, -1);
                        break;
                    case 256: // int
                        if (player.getInt() < 5) {
                            return;
                        }
                        player.addStat(3, -1);
                        break;
                    case 512: // luk
                        if (player.getLuk() < 5) {
                            return;
                        }
                        player.addStat(4, -1);
                        break;
                    case 2048: // HP
                    	if (APTo != 8192) {
                    		c.announce(MaplePacketCreator.enableActions());
                    		return;
                    	}
                        int hplose = 0;
                        final int jobid = player.getJob().getId();
                        if (jobid == 0 || jobid == 1000 || jobid == 2000 || jobid >= 1200 && jobid <= 1211) { // Beginner
                            hplose -= 12;
                        } else if (jobid >= 100 && jobid <= 132) { // Warrior
                            Skill improvinghplose = SkillFactory.getSkill(1000001);
                            int improvinghploseLevel = c.getPlayer().getSkillLevel(improvinghplose);
                            hplose -= 24;
                            if (improvinghploseLevel >= 1) {
                                hplose -= improvinghplose.getEffect(improvinghploseLevel).getY();
                            }
                        } else if (jobid >= 200 && jobid <= 232) { // Magician
                            hplose -= 10;
                        } else if (jobid >= 500 && jobid <= 522) { // Pirate
                            Skill improvinghplose = SkillFactory.getSkill(5100000);
                            int improvinghploseLevel = c.getPlayer().getSkillLevel(improvinghplose);
                            hplose -= 22;
                            if (improvinghploseLevel > 0) {
                                hplose -= improvinghplose.getEffect(improvinghploseLevel).getY();
                            }
                        } else if (jobid >= 1100 && jobid <= 1111) { // Soul Master
                            Skill improvinghplose = SkillFactory.getSkill(11000000);
                            int improvinghploseLevel = c.getPlayer().getSkillLevel(improvinghplose);
                            hplose -= 27;
                            if (improvinghploseLevel >= 1) {
                                hplose -= improvinghplose.getEffect(improvinghploseLevel).getY();
                            }
                        } else if ((jobid >= 1300 && jobid <= 1311) || (jobid >= 1400 && jobid <= 1411)) { // Wind Breaker and Night Walker
                            hplose -= 17;
                        } else if (jobid >= 300 && jobid <= 322 || jobid >= 400 && jobid <= 422 || jobid >= 2000 && jobid <= 2112) { // Aran
                            hplose -= 20;
                        } else { // GameMaster
                            hplose -= 20;
                        }
                        player.setHp(player.getHp() + hplose);
                        player.setMaxHp(player.getMaxHp() + hplose);
                        statupdate.add(new Pair<>(MapleStat.HP, player.getHp()));
                        statupdate.add(new Pair<>(MapleStat.MAXHP, player.getMaxHp()));
                        break;
                    case 8192: // MP
                    	if (APTo != 2048) {
                    		c.announce(MaplePacketCreator.enableActions());
                    		return;
                    	}
                        int mp = player.getMp();
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
                        if (canWash) {
                            int minmp = 0;
                            if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
                                minmp += 4;
                            } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
                                minmp += 36;
                            } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
                                minmp += 12;
                            } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
                                minmp += 16;
                            } else {
                                minmp += 8;
                            }                       
                            player.setMp(player.getMp() - minmp);
                            player.setMaxMp(player.getMaxMp() - minmp);
                            statupdate.add(new Pair<>(MapleStat.MP, player.getMp()));
                            statupdate.add(new Pair<>(MapleStat.MAXMP, player.getMaxMp()));
                            break;
                        }
                    default:
                        c.announce(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, c.getPlayer()));
                        return;
                }
                DistributeAPHandler.addStat(c, APTo);
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
                    Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(3, c.getChannel(), medal + player.getName() + " : " + slea.readMapleAsciiString(), (slea.readByte() != 0)));
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
                        Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(3, c.getChannel(), medal + player.getName() + " : " + builder.toString(), ear));
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
                        } else if (ii.isDropRestricted(item.getItemId())) { //Lol?
                            player.dropMessage(1, "You cannot trade this item.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                    }
                    Server.getInstance().broadcastMessage(MaplePacketCreator.itemMegaphone(msg, whisper, c.getChannel(), item));
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
                    Server.getInstance().broadcastMessage(MaplePacketCreator.getMultiMegaphone(msg2, c.getChannel(), whisper));
                    break;
            }
            remove(c, itemId);
        } else if (itemType == 508) { //graduation banner
            slea.readMapleAsciiString(); // message, sepearated by 0A for lines
            c.announce(MaplePacketCreator.enableActions());
        } else if (itemType == 509) {
            String sendTo = slea.readMapleAsciiString();
            String msg = slea.readMapleAsciiString();
            try {
                player.sendNote(sendTo, msg, (byte) 0);
            } catch (SQLException e) {
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
            Item item = player.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            String newName = slea.readMapleAsciiString();
            pet.setName(newName);
            pet.saveToDb();
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
                                player.changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
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
        } else if (itemType == 524) {
            for (byte i = 0; i < 3; i++) {
                MaplePet pet = player.getPet(i);
                if (pet != null) {
                    if (pet.canConsume(itemId)) {
                        pet.setFullness(100);
                        if (pet.getCloseness() + 100 > 30000) {
                            pet.setCloseness(30000);
                        } else {
                            pet.gainCloseness(100);
                        }

                        while (pet.getCloseness() >= ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                            pet.setLevel((byte) (pet.getLevel() + 1));
                            byte index = player.getPetIndex(pet);
                            c.announce(MaplePacketCreator.showOwnPetLevelUp(index));
                            player.getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), index));
                        }
                        Item item = player.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
                        player.forceUpdateItem(item);
                        player.getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(player.getId(), i, 1, true), true);
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
            List<String> lines = new LinkedList<>();
            for (int i = 0; i < 4; i++) {
                lines.add(slea.readMapleAsciiString());
            }
            Server.getInstance().broadcastMessage(MaplePacketCreator.getAvatarMega(c.getPlayer(), medal, c.getChannel(), itemId, lines, (slea.readByte() != 0)));
            TimerManager.getInstance().schedule(new Runnable() {
            	@Override
            	public void run() {
            		Server.getInstance().broadcastMessage(MaplePacketCreator.byeAvatarMega());
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
            if (type.equals(MapleInventoryType.USE)) {
                item.setFlag((byte) ItemConstants.SPIKES);
            } else {
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
            c.announce(MaplePacketCreator.enableActions());
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
