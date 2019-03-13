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
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;


/**
    *@author Drago/Dragohe4rt
*/

public final class MonsterCarnivalHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            int tab = slea.readByte();
            int num = slea.readByte();
            int neededCP = 0;
            if (tab == 0) { 
                final List<Pair<Integer, Integer>> mobs = c.getPlayer().getMap().getMobsToSpawn();
                if (num >= mobs.size() || c.getPlayer().getCP() < mobs.get(num).right) {
                    c.announce(MaplePacketCreator.CPQMessage((byte) 1));
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }

                final MapleMonster mob = MapleLifeFactory.getMonster(mobs.get(num).left);
                if (c.getPlayer().getMonsterCarnival() != null) {
                    Point spawnPos = c.getPlayer().getMap().getRandomSP(c.getPlayer().getTeam());
                    if (!c.getPlayer().getMonsterCarnival().canSummon() && c.getPlayer().getTeam() == 0 || !c.getPlayer().getMonsterCarnival().canSummons() && c.getPlayer().getTeam() == 1) {
                        c.announce(MaplePacketCreator.CPQMessage((byte) 2));
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    mob.setPosition(spawnPos);
                    if (c.getPlayer().getTeam() == 0) {
                        c.getPlayer().getMonsterCarnival().summon();
                    } else {
                        c.getPlayer().getMonsterCarnival().summons();
                    }
                        c.getPlayer().getMap().addMonsterSpawn(mob, 1, c.getPlayer().getTeam());
                        c.getSession().write(MaplePacketCreator.enableActions());
                    }
                    neededCP = mobs.get(num).right;
                } else if (tab == 1) { //debuffs
                    final List<Integer> skillid = c.getPlayer().getMap().getSkillIds();
                    if (num >= skillid.size()) {
                        c.getPlayer().dropMessage(5, "Ocorreu um erro.");
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    final MCSkill skil = MapleCarnivalFactory.getInstance().getSkill(skillid.get(num)); //ugh wtf
                    if (skil == null || c.getPlayer().getCP() < skil.cpLoss) {
                        c.announce(MaplePacketCreator.CPQMessage((byte) 1));
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    final MapleDisease dis = skil.getDisease();
                    MapleParty inimigos = c.getPlayer().getParty().getEnemy();
                    if (skil.targetsAll) {
                        int chanceAcerto = 0;
                        if (dis.getDisease() == 121 || dis.getDisease() == 122 || dis.getDisease() == 125 || dis.getDisease() == 126) {
                            chanceAcerto = (int) (Math.random() * 100);
                        }
                        if (chanceAcerto <= 80) {
                            for (MaplePartyCharacter chrS : inimigos.getPartyMembers()) {
                                if (dis == null) {
                                    chrS.getPlayer().dispel();
                                } else {
                                    chrS.getPlayer().giveDebuff(dis, skil.getSkill());
                                }
                                if (!skil.targetsAll) {
                                    break;
                                }
                            }
                        }
                    } else {
                        int amount = inimigos.getMembers().size() - 1;
                        int randd = (int) Math.floor(Math.random() * amount);
                        MapleCharacter chrApp = c.getChannelServer().getPlayerStorage().getCharacterById(inimigos.getMemberByPos(randd).getId());
                        if (chrApp != null && chrApp.getMap().isCPQMap()) {
                            if (dis == null) {
                                chrApp.dispel();
                            } else {
                                chrApp.giveDebuff(dis, skil.getSkill());
                            }
                        }
                    }
                    neededCP = skil.cpLoss;
                    c.getSession().write(MaplePacketCreator.enableActions());
                } else if (tab == 2) { //protectors
                    final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
                    if (skil == null || c.getPlayer().getCP() < skil.cpLoss) {
                        c.announce(MaplePacketCreator.CPQMessage((byte) 1));
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    int success = c.getPlayer().getMap().spawnGuardian(c.getPlayer().getTeam(), num);
                    if (success == -1 || success == 0 || success == 2) {
                        if (success == -1) {
                            c.announce(MaplePacketCreator.CPQMessage((byte) 3));
                        } else if (success == 0) {
                            c.announce(MaplePacketCreator.CPQMessage((byte) 4));
                        } else if (success == 2) {
                            c.announce(MaplePacketCreator.CPQMessage((byte) 3));
                        }
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    } else {
                        neededCP = skil.cpLoss;
                    }
                }
                c.getPlayer().gainCP(-neededCP);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playerSummoned(c.getPlayer().getName(), tab, num));
            }catch (Exception e) {
            e.printStackTrace();
        }
        }

    }
