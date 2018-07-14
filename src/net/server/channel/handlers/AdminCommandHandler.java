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

import java.util.Arrays;
import java.util.List;

import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;

public final class AdminCommandHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isGM()) {
            return;
        }
        byte mode = slea.readByte();
        String victim;
        MapleCharacter target;
        switch (mode) {
            case 0x00: // Level1~Level8 & Package1~Package2
                int[][] toSpawn = MapleItemInformationProvider.getInstance().getSummonMobs(slea.readInt());
                for (int z = 0; z < toSpawn.length; z++) {
                    int[] toSpawnChild = toSpawn[z];
                    if (Randomizer.nextInt(101) <= toSpawnChild[1]) {
                        c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(toSpawnChild[0]), c.getPlayer().getPosition());
                    }
                }
                c.announce(MaplePacketCreator.enableActions());
                break;
            case 0x01: { // /d (inv)
                byte type = slea.readByte();
                MapleInventory in = c.getPlayer().getInventory(MapleInventoryType.getByType(type));
                for (short i = 1; i <= in.getSlotLimit(); i++) { //TODO What is the point of this loop?
                    if (in.getItem(i) != null) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(type), i, in.getItem(i).getQuantity(), false);
                    }
                    return;
                }
                break;
            }
            case 0x02: // Exp
                c.getPlayer().setExp(slea.readInt());
                break;
            case 0x03: // /ban <name>
            	c.getPlayer().yellowMessage("Please use !ban <IGN> <Reason>");
            	break;
            case 0x04: // /block <name> <duration (in days)> <HACK/BOT/AD/HARASS/CURSE/SCAM/MISCONDUCT/SELL/ICASH/TEMP/GM/IPROGRAM/MEGAPHONE>
            	victim = slea.readMapleAsciiString();
                int type = slea.readByte(); //reason
                int duration = slea.readInt();
                String description = slea.readMapleAsciiString();
                String reason = c.getPlayer().getName() + " used /ban to ban";
                target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
                if (target != null) {
                    String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
                    String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                    reason += readableTargetName + " (IP: " + ip + ")";
                    if (duration == -1) {
                        target.ban(description + " " + reason);
                    } else {
                        target.block(type, duration, description);
                        target.sendPolice(duration, reason, 6000);
                    }
                    c.announce(MaplePacketCreator.getGMEffect(4, (byte) 0));
                } else if (MapleCharacter.ban(victim, reason, false)) {
                    c.announce(MaplePacketCreator.getGMEffect(4, (byte) 0));
                } else {
                    c.announce(MaplePacketCreator.getGMEffect(6, (byte) 1));
                }
                break;
            case 0x10: // /h, information by vana (and tele mode f1) ... hide ofcourse
                c.getPlayer().Hide(slea.readByte() == 1);
                break;
            case 0x11: // Entering a map
                switch (slea.readByte()) {
                    case 0:// /u
                        StringBuilder sb = new StringBuilder("USERS ON THIS MAP: ");
                        for (MapleCharacter mc : c.getPlayer().getMap().getCharacters()) {
                            sb.append(mc.getName());
                            sb.append(" ");
                        }
                        c.getPlayer().message(sb.toString());
                        break;
                    case 12:// /uclip and entering a map
                        break;
                }
                break;
            case 0x12: // Send
                victim = slea.readMapleAsciiString();
                int mapId = slea.readInt();
                c.getChannelServer().getPlayerStorage().getCharacterByName(victim).changeMap(c.getChannelServer().getMapFactory().getMap(mapId));
                break;
            case 0x15: // Kill
                int mobToKill = slea.readInt();
                int amount = slea.readInt();
                List<MapleMapObject> monsterx = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
                for (int x = 0; x < amount; x++) {
                    MapleMonster monster = (MapleMonster) monsterx.get(x);
                    if (monster.getId() == mobToKill) {
                        c.getPlayer().getMap().killMonster(monster, c.getPlayer(), true);
                        //monster.giveExpToCharacter(c.getPlayer(), monster.getExp(), true, 1); already being done
                    }
                }
                break;
            case 0x16: // Questreset
                MapleQuest.getInstance(slea.readShort()).reset(c.getPlayer());
                break;
            case 0x17: // Summon
                int mobId = slea.readInt();
                int quantity = slea.readInt();
                for (int i = 0; i < quantity; i++) {
                    c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), c.getPlayer().getPosition());
                }
                break;
            case 0x18: // Maple & Mobhp
                int mobHp = slea.readInt();
                c.getPlayer().dropMessage("Monsters HP");
                List<MapleMapObject> monsters = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
                for (MapleMapObject mobs : monsters) {
                    MapleMonster monster = (MapleMonster) mobs;
                    if (monster.getId() == mobHp) {
                        c.getPlayer().dropMessage(monster.getName() + ": " + monster.getHp());
                    }
                }
                break;
            case 0x1E: // Warn
                victim = slea.readMapleAsciiString();
                String message = slea.readMapleAsciiString();
                target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
                if (target != null) {
                    target.getClient().announce(MaplePacketCreator.serverNotice(1, message));
                    c.announce(MaplePacketCreator.getGMEffect(0x1E, (byte) 1));
                } else {
                    c.announce(MaplePacketCreator.getGMEffect(0x1E, (byte) 0));
                }
                break;
            case 0x24:// /Artifact Ranking
                break;
            case 0x77: //Testing purpose
                if (slea.available() == 4) {
                    System.out.println(slea.readInt());
                } else if (slea.available() == 2) {
                    System.out.println(slea.readShort());
                }
                break;
            default:
                System.out.println("New GM packet encountered (MODE : " + mode + ": " + slea.toString());
                break;
        }
    }
}
