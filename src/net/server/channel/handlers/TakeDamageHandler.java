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
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.Aran;
import constants.skills.Corsair;

import java.awt.Point;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleStatEffect;
import server.life.MapleLifeFactory.loseItem;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobAttackInfoFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public final class TakeDamageHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        List<MapleCharacter> banishPlayers = new ArrayList<>();
        
        MapleCharacter chr = c.getPlayer();
        slea.readInt();
        byte damagefrom = slea.readByte();
        slea.readByte(); //Element
        int damage = slea.readInt();
        int oid = 0, monsteridfrom = 0, pgmr = 0, direction = 0;
        int pos_x = 0, pos_y = 0, fake = 0;
        boolean is_pgmr = false, is_pg = true, is_deadly = false;
        int mpattack = 0;
        MapleMonster attacker = null;
        final MapleMap map = chr.getMap();
        if (damagefrom != -3 && damagefrom != -4) {
            monsteridfrom = slea.readInt();
	    oid = slea.readInt();
            
            try {
                MapleMapObject mmo = map.getMapObject(oid);
                if(mmo instanceof MapleMonster) {
                    attacker = (MapleMonster) mmo;
                    if(attacker.getId() != monsteridfrom) {
                        attacker = null;
                    }
                }
                
                if (attacker != null) {
                    List<loseItem> loseItems;
                    
                    if (attacker.isBuffed(MonsterStatus.NEUTRALISE)) {
                        return;
                    }
                    if (damage > 0) {
                        MapleMonster assaulter = map.getMonsterById(monsteridfrom);
                        
                        if(assaulter != null) {
                            loseItems = assaulter.getStats().loseItem();
                            if (loseItems != null) {
                                MapleInventoryType type;
                                final int playerpos = chr.getPosition().x;
                                byte d = 1;
                                Point pos = new Point(0, chr.getPosition().y);
                                for (loseItem loseItem : loseItems) {
                                    type = ItemConstants.getInventoryType(loseItem.getId());
                                    for (byte b = 0; b < loseItem.getX(); b++) {//LOL?
                                        if (Randomizer.nextInt(101) >= loseItem.getChance()) {
                                            if (chr.haveItem(loseItem.getId())) {
                                                pos.x = (int) (playerpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                                                MapleInventoryManipulator.removeById(c, type, loseItem.getId(), 1, false, false);
                                                map.spawnItemDrop(chr, chr, new Item(loseItem.getId(), (short) 0, (short) 1), map.calcDropPos(pos, chr.getPosition()), true, true);
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
                    }
                } else {
                    return;
                }
            } catch(ClassCastException e) {
                //this happens due to mob on last map damaging player just before changing maps
                
                e.printStackTrace();
                FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, "Attacker is not a mob-type, rather is a " + map.getMapObject(oid).getClass().getName() + " entity.");
                
                return;
            }
            
            direction = slea.readByte();
        }
        if (damagefrom != -1 && damagefrom != -2 && attacker != null) {
            MobAttackInfo attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, damagefrom);
            if (attackInfo != null) {
	            if (attackInfo.isDeadlyAttack()) {
	                mpattack = chr.getMp() - 1;
                        is_deadly = true;
	            }
	            mpattack += attackInfo.getMpBurn();
	            MobSkill mobSkill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
	            if (mobSkill != null && damage > 0) {
	                mobSkill.applyEffect(chr, attacker, false, banishPlayers);
	            }
	            
                    attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
                    if (chr.getBuffedValue(MapleBuffStat.MANA_REFLECTION) != null && damage > 0 && !attacker.isBoss()) {
                        int jobid = chr.getJob().getId();
                        if (jobid == 212 || jobid == 222 || jobid == 232) {
                            int id = jobid * 10000 + 1002;
                            Skill manaReflectSkill = SkillFactory.getSkill(id);
                            if (chr.isBuffFrom(MapleBuffStat.MANA_REFLECTION, manaReflectSkill) && chr.getSkillLevel(manaReflectSkill) > 0 && manaReflectSkill.getEffect(chr.getSkillLevel(manaReflectSkill)).makeChanceResult()) {
                                int bouncedamage = (damage * manaReflectSkill.getEffect(chr.getSkillLevel(manaReflectSkill)).getX() / 100);
                                if (bouncedamage > attacker.getMaxHp() / 5) {
                                    bouncedamage = attacker.getMaxHp() / 5;
                                }
                                map.damageMonster(chr, attacker, bouncedamage);
                                map.broadcastMessage(chr, MaplePacketCreator.damageMonster(oid, bouncedamage), true);
                                chr.getClient().announce(MaplePacketCreator.showOwnBuffEffect(id, 5));
                                map.broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr.getId(), id, 5), false);
                            }
                        }
                    }
	        }
        }
        if (damage == -1) {
            fake = 4020002 + (chr.getJob().getId() / 10 - 40) * 100000;
        }
        
        //in dojo player cannot use pot, so deadly attacks should be turned off as well
        if(is_deadly && chr.getMap().isDojoMap() && !ServerConstants.USE_DEADLY_DOJO) {
            damage = 0;
            mpattack = 0;
        }
        
        if (damage == 0) {
            chr.getAutobanManager().addMiss();
        } else {
            chr.getAutobanManager().resetMisses();
        }
        if (damage > 0 && !chr.isHidden()) {
            if (attacker != null && damagefrom == -1 && chr.getBuffedValue(MapleBuffStat.POWERGUARD) != null) { // PG works on bosses, but only at half of the rate.
                int bouncedamage = (int) (damage * (chr.getBuffedValue(MapleBuffStat.POWERGUARD).doubleValue() / (attacker.isBoss() ? 200 : 100)));
                bouncedamage = Math.min(bouncedamage, attacker.getMaxHp() / 10);
                damage -= bouncedamage;
                map.damageMonster(chr, attacker, bouncedamage);
                map.broadcastMessage(chr, MaplePacketCreator.damageMonster(oid, bouncedamage), false, true);
                chr.checkMonsterAggro(attacker);
            }
            if (attacker != null && damagefrom == -1 && chr.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null) {
                Skill skill = SkillFactory.getSkill(Aran.BODY_PRESSURE);
                final MapleStatEffect eff = skill.getEffect(chr.getSkillLevel(skill));
                if (!attacker.alreadyBuffedStats().contains(MonsterStatus.NEUTRALISE)) {
                    if (!attacker.isBoss() && eff.makeChanceResult()) {
                    	attacker.applyStatus(chr, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.NEUTRALISE, 1), skill, null, false), false, (eff.getDuration()/10) * 2, false);
                    }
                }
            }
            if (damagefrom != -3 && damagefrom != -4) {
                int achilles = 0;
                Skill achilles1 = null;
                int jobid = chr.getJob().getId();
                if (jobid < 200 && jobid % 10 == 2) {
                    achilles1 = SkillFactory.getSkill(jobid * 10000 + (jobid == 112 ? 4 : 5));
                    achilles = chr.getSkillLevel(achilles1);
                }
                if (achilles != 0 && achilles1 != null) {
                    damage *= (achilles1.getEffect(achilles).getX() / 1000.0);
                }
            }
            Integer mesoguard = chr.getBuffedValue(MapleBuffStat.MESOGUARD);
            if (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null && mpattack == 0) {
                int mploss = (int) (damage * (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0));
                int hploss = damage - mploss;
                
                int curmp = chr.getMp();
                if (mploss > curmp) {
                    hploss += mploss - curmp;
                    mploss = curmp;
                }
                
                chr.addMPHP(-hploss, -mploss);
            } else if (mesoguard != null) {
                damage = Math.round(damage / 2);
                int mesoloss = (int) (damage * (mesoguard.doubleValue() / 100.0));
                if (chr.getMeso() < mesoloss) {
                    chr.gainMeso(-chr.getMeso(), false);
                    chr.cancelBuffStats(MapleBuffStat.MESOGUARD);
                } else {
                    chr.gainMeso(-mesoloss, false);
                }
                chr.addMPHP(-damage, -mpattack);
            } else {
                if (chr.isRidingBattleship()) {
                    chr.decreaseBattleshipHp(damage);
                }
                chr.addMPHP(-damage, -mpattack);
            }
        }
        if (!chr.isHidden()) {
            map.broadcastMessage(chr, MaplePacketCreator.damagePlayer(damagefrom, monsteridfrom, chr.getId(), damage, fake, direction, is_pgmr, pgmr, is_pg, oid, pos_x, pos_y), false);
        } else {
            map.broadcastGMMessage(chr, MaplePacketCreator.damagePlayer(damagefrom, monsteridfrom, chr.getId(), damage, fake, direction, is_pgmr, pgmr, is_pg, oid, pos_x, pos_y), false);
        }
        if (GameConstants.isDojo(map.getId())) {
            chr.setDojoEnergy(chr.getDojoEnergy() + ServerConstants.DOJO_ENERGY_DMG);
            c.announce(MaplePacketCreator.getEnergy("energy", chr.getDojoEnergy()));
        }
        
        for (MapleCharacter player : banishPlayers) {  // chill, if this list ever gets non-empty an attacker does exist, trust me :)
            player.changeMapBanish(attacker.getBanish().getMap(), attacker.getBanish().getPortal(), attacker.getBanish().getMsg());
        }
    }
}
