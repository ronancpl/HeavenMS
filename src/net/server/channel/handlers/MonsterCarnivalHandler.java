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
import client.MapleDisease;
import java.awt.Point;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.partyquest.MapleCarnivalFactory;
import server.partyquest.MapleCarnivalFactory.MCSkill;
import server.partyquest.MonsterCarnival;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;


/**
    *@author Drago (Dragohe4rt)
*/

public final class MonsterCarnivalHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.tryacquireClient()) {
            try {
                try {
                    int tab = slea.readByte();
                    int num = slea.readByte();
                    int neededCP = 0;
                    if (tab == 0) { 
                        final List<Pair<Integer, Integer>> mobs = c.getPlayer().getMap().getMobsToSpawn();
                        if (num >= mobs.size() || c.getPlayer().getCP() < mobs.get(num).right) {
                            c.announce(MaplePacketCreator.CPQMessage((byte) 1));
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }

                        final MapleMonster mob = MapleLifeFactory.getMonster(mobs.get(num).left);
                        MonsterCarnival mcpq = c.getPlayer().getMonsterCarnival();
                        if (mcpq != null) {
                            if (!mcpq.canSummonR() && c.getPlayer().getTeam() == 0 || !mcpq.canSummonB() && c.getPlayer().getTeam() == 1) {
                                c.announce(MaplePacketCreator.CPQMessage((byte) 2));
                                c.announce(MaplePacketCreator.enableActions());
                                return;
                            }
                            
                            if (c.getPlayer().getTeam() == 0) {
                                mcpq.summonR();
                            } else {
                                mcpq.summonB();
                            }

                            Point spawnPos = c.getPlayer().getMap().getRandomSP(c.getPlayer().getTeam());
                            mob.setPosition(spawnPos);

                            c.getPlayer().getMap().addMonsterSpawn(mob, 1, c.getPlayer().getTeam());
                            c.getPlayer().getMap().addAllMonsterSpawn(mob, 1, c.getPlayer().getTeam());
                            c.announce(MaplePacketCreator.enableActions());
                        }

                        neededCP = mobs.get(num).right;
                    } else if (tab == 1) { //debuffs
                        final List<Integer> skillid = c.getPlayer().getMap().getSkillIds();
                        if (num >= skillid.size()) {
                            c.getPlayer().dropMessage(5, "An unexpected error has occurred.");
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        final MCSkill skill = MapleCarnivalFactory.getInstance().getSkill(skillid.get(num)); //ugh wtf
                        if (skill == null || c.getPlayer().getCP() < skill.cpLoss) {
                            c.announce(MaplePacketCreator.CPQMessage((byte) 1));
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        final MapleDisease dis = skill.getDisease();
                        MapleParty enemies = c.getPlayer().getParty().getEnemy();
                        if (skill.targetsAll) {
                            int hitChance = 0;
                            if (dis.getDisease() == 121 || dis.getDisease() == 122 || dis.getDisease() == 125 || dis.getDisease() == 126) {
                                hitChance = (int) (Math.random() * 100);
                            }
                            if (hitChance <= 80) {
                                for (MaplePartyCharacter mpc : enemies.getPartyMembers()) {
                                    MapleCharacter mc = mpc.getPlayer();
                                    if (mc != null) {
                                        if (dis == null) {
                                            mc.dispel();
                                        } else {
                                            mc.giveDebuff(dis, skill.getSkill());
                                        }
                                    }
                                }
                            }
                        } else {
                            int amount = enemies.getMembers().size() - 1;
                            int randd = (int) Math.floor(Math.random() * amount);
                            MapleCharacter chrApp = c.getPlayer().getMap().getCharacterById(enemies.getMemberByPos(randd).getId());
                            if (chrApp != null && chrApp.getMap().isCPQMap()) {
                                if (dis == null) {
                                    chrApp.dispel();
                                } else {
                                    chrApp.giveDebuff(dis, skill.getSkill());
                                }
                            }
                        }
                        neededCP = skill.cpLoss;
                        c.announce(MaplePacketCreator.enableActions());
                    } else if (tab == 2) { //protectors
                        final MCSkill skill = MapleCarnivalFactory.getInstance().getGuardian(num);
                        if (skill == null || c.getPlayer().getCP() < skill.cpLoss) {
                            c.announce(MaplePacketCreator.CPQMessage((byte) 1));
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                        
                        MonsterCarnival mcpq = c.getPlayer().getMonsterCarnival();
                        if (mcpq != null) {
                            if (!mcpq.canGuardianR() && c.getPlayer().getTeam() == 0 || !mcpq.canGuardianB() && c.getPlayer().getTeam() == 1) {
                                c.announce(MaplePacketCreator.CPQMessage((byte) 2));
                                c.announce(MaplePacketCreator.enableActions());
                                return;
                            }

                            int success = c.getPlayer().getMap().spawnGuardian(c.getPlayer().getTeam(), num);
                            if (success != 1) {
                                switch (success) {
                                    case -1:
                                        c.announce(MaplePacketCreator.CPQMessage((byte) 3));
                                        break;

                                    case 0:
                                        c.announce(MaplePacketCreator.CPQMessage((byte) 4));
                                        break;

                                    default:
                                        c.announce(MaplePacketCreator.CPQMessage((byte) 3));
                                }
                                c.announce(MaplePacketCreator.enableActions());
                                return;
                            } else {
                                neededCP = skill.cpLoss;
                            }
                        }
                    }
                    c.getPlayer().gainCP(-neededCP);
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playerSummoned(c.getPlayer().getName(), tab, num));
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
