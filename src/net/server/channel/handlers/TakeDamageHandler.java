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

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.skills.Aran;
import constants.skills.Corsair;

import java.awt.Point;
import java.util.Collections;
import java.util.List;

import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.life.MapleLifeFactory.loseItem;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobAttackInfoFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public final class TakeDamageHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        slea.readInt();
        byte damagefrom = slea.readByte();
        slea.readByte(); //Element
        int damage = slea.readInt();
        int oid = 0, monsteridfrom = 0, pgmr = 0, direction = 0;
        int pos_x = 0, pos_y = 0, fake = 0;
        boolean is_pgmr = false, is_pg = true;
        int mpattack = 0;
        MapleMonster attacker = null;
        final MapleMap map = player.getMap();
        if (damagefrom != -3 && damagefrom != -4) {
        	monsteridfrom = slea.readInt();
	        oid = slea.readInt();
            attacker = (MapleMonster) map.getMapObject(oid);
            List<loseItem> loseItems;
            if (attacker != null) {
                if (attacker.isBuffed(MonsterStatus.NEUTRALISE)) {
                    return;
                }
                if (damage > 0) {
                    loseItems = map.getMonsterById(monsteridfrom).getStats().loseItem();
                    if (loseItems != null) {
                        MapleInventoryType type;
                        final int playerpos = player.getPosition().x;
                        byte d = 1;
                        Point pos = new Point(0, player.getPosition().y);
                        for (loseItem loseItem : loseItems) {
                            type = MapleItemInformationProvider.getInstance().getInventoryType(loseItem.getId());
                            for (byte b = 0; b < loseItem.getX(); b++) {//LOL?
                                if (Randomizer.nextInt(101) >= loseItem.getChance()) {
                                    if (player.haveItem(loseItem.getId())) {
                                        pos.x = (int) (playerpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                                        MapleInventoryManipulator.removeById(c, type, loseItem.getId(), 1, false, false);
                                        map.spawnItemDrop(c.getPlayer(), c.getPlayer(), new Item(loseItem.getId(), (short) 0, (short) 1), map.calcDropPos(pos, player.getPosition()), true, true);
                                        d++;
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                        map.removeMapObject(attacker);
                    }
                }
            } else {
                return;
            }
            direction = slea.readByte();
        }
        if (damagefrom != -1 && damagefrom != -2 && attacker != null) {
            MobAttackInfo attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, damagefrom);
            if (attackInfo != null) {
	            if (attackInfo.isDeadlyAttack()) {
	                mpattack = player.getMp() - 1;
	            }
	            mpattack += attackInfo.getMpBurn();
	            MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
	            if (skill != null && damage > 0) {
	                skill.applyEffect(player, attacker, false);
	            }
	            if (attacker != null) {
	                attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
	                if (player.getBuffedValue(MapleBuffStat.MANA_REFLECTION) != null && damage > 0 && !attacker.isBoss()) {
	                    int jobid = player.getJob().getId();
	                    if (jobid == 212 || jobid == 222 || jobid == 232) {
	                        int id = jobid * 10000 + 1002;
	                        Skill manaReflectSkill = SkillFactory.getSkill(id);
	                        if (player.isBuffFrom(MapleBuffStat.MANA_REFLECTION, manaReflectSkill) && player.getSkillLevel(manaReflectSkill) > 0 && manaReflectSkill.getEffect(player.getSkillLevel(manaReflectSkill)).makeChanceResult()) {
	                            int bouncedamage = (damage * manaReflectSkill.getEffect(player.getSkillLevel(manaReflectSkill)).getX() / 100);
	                            if (bouncedamage > attacker.getMaxHp() / 5) {
	                                bouncedamage = attacker.getMaxHp() / 5;
	                            }
	                            map.damageMonster(player, attacker, bouncedamage);
	                            map.broadcastMessage(player, MaplePacketCreator.damageMonster(oid, bouncedamage), true);
	                            player.getClient().announce(MaplePacketCreator.showOwnBuffEffect(id, 5));
	                            map.broadcastMessage(player, MaplePacketCreator.showBuffeffect(player.getId(), id, 5), false);
	                        }
	                    }
	                }
	            }
	        }
        }
        if (damage == -1) {
            fake = 4020002 + (player.getJob().getId() / 10 - 40) * 100000;
        }
        if (damage == 0) {
            player.getAutobanManager().addMiss();
        } else {
            player.getAutobanManager().resetMisses();
        }
        if (damage > 0 && !player.isHidden()) {
            if (attacker != null && damagefrom == -1 && player.getBuffedValue(MapleBuffStat.POWERGUARD) != null) { // PG works on bosses, but only at half of the rate.
                int bouncedamage = (int) (damage * (player.getBuffedValue(MapleBuffStat.POWERGUARD).doubleValue() / (attacker.isBoss() ? 200 : 100)));
                bouncedamage = Math.min(bouncedamage, attacker.getMaxHp() / 10);
                damage -= bouncedamage;
                map.damageMonster(player, attacker, bouncedamage);
                map.broadcastMessage(player, MaplePacketCreator.damageMonster(oid, bouncedamage), false, true);
                player.checkMonsterAggro(attacker);
            }
            if (attacker != null && damagefrom == -1 && player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null) {
                Skill skill = SkillFactory.getSkill(Aran.BODY_PRESSURE);
                final MapleStatEffect eff = skill.getEffect(player.getSkillLevel(skill));
                if (!attacker.alreadyBuffedStats().contains(MonsterStatus.NEUTRALISE)) {
                    if (!attacker.isBoss() && eff.makeChanceResult()) {
                    	attacker.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.NEUTRALISE, 1), skill, null, false), false, (eff.getDuration()/10) * 2, false);
                    }
                }
            }
            if (damagefrom != -3 && damagefrom != -4) {
                int achilles = 0;
                Skill achilles1 = null;
                int jobid = player.getJob().getId();
                if (jobid < 200 && jobid % 10 == 2) {
                    achilles1 = SkillFactory.getSkill(jobid * 10000 + (jobid == 112 ? 4 : 5));
                    achilles = player.getSkillLevel(achilles1);
                }
                if (achilles != 0 && achilles1 != null) {
                    damage *= (achilles1.getEffect(achilles).getX() / 1000.0);
                }
            }
            Integer mesoguard = player.getBuffedValue(MapleBuffStat.MESOGUARD);
            if (player.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null && mpattack == 0) {
                int mploss = (int) (damage * (player.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0));
                int hploss = damage - mploss;
                if (mploss > player.getMp()) {
                    hploss += mploss - player.getMp();
                    mploss = player.getMp();
                }
                player.addMPHP(-hploss, -mploss);
            } else if (mesoguard != null) {
                damage = Math.round(damage / 2);
                int mesoloss = (int) (damage * (mesoguard.doubleValue() / 100.0));
                if (player.getMeso() < mesoloss) {
                    player.gainMeso(-player.getMeso(), false);
                    player.cancelBuffStats(MapleBuffStat.MESOGUARD);
                } else {
                    player.gainMeso(-mesoloss, false);
                }
                player.addMPHP(-damage, -mpattack);
            } else {
                if (player.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                    if (player.getBuffedValue(MapleBuffStat.MONSTER_RIDING).intValue() == Corsair.BATTLE_SHIP) {
                        player.decreaseBattleshipHp(damage);
                    }
                }
                player.addMPHP(-damage, -mpattack);
            }
        }
        if (!player.isHidden()) {
            map.broadcastMessage(player, MaplePacketCreator.damagePlayer(damagefrom, monsteridfrom, player.getId(), damage, fake, direction, is_pgmr, pgmr, is_pg, oid, pos_x, pos_y), false);
            player.checkBerserk();
        }
        if (map.getId() >= 925020000 && map.getId() < 925030000) {
            player.setDojoEnergy(player.isGM() ? 300 : player.getDojoEnergy() < 300 ? player.getDojoEnergy() + 1 : 0); //Fking gm's
            player.getClient().announce(MaplePacketCreator.getEnergy("energy", player.getDojoEnergy()));
        }
    }
}
