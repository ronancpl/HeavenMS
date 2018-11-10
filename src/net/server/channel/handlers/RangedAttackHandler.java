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

import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.Aran;
import constants.skills.Buccaneer;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Shadower;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;

public final class RangedAttackHandler extends AbstractDealDamageHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        //chr.setPetLootCd(currentServerTime());
        
        /*long timeElapsed = currentServerTime() - chr.getAutobanManager().getLastSpam(8);
        if(timeElapsed < 300) {
            AutobanFactory.FAST_ATTACK.alert(chr, "Time: " + timeElapsed);
        }
        chr.getAutobanManager().spam(8);*/
		
        AttackInfo attack = parseDamage(slea, chr, true, false);
        
        if (chr.getBuffEffect(MapleBuffStat.MORPH) != null) {
            if(chr.getBuffEffect(MapleBuffStat.MORPH).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                chr.getClient().disconnect(false, false);
                return; 
            }
        }
        
        if (chr.getMap().isDojoMap() && attack.numAttacked > 0) {
            chr.setDojoEnergy(chr.getDojoEnergy() + ServerConstants.DOJO_ENERGY_ATK);
            c.announce(MaplePacketCreator.getEnergy("energy", chr.getDojoEnergy()));
        }
        
        if (attack.skill == Buccaneer.ENERGY_ORB || attack.skill == ThunderBreaker.SPARK || attack.skill == Shadower.TAUNT || attack.skill == NightLord.TAUNT) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, 0, attack.allDamage, attack.speed, attack.direction, attack.display), false);
            applyAttack(attack, chr, 1);
        } else if (attack.skill == ThunderBreaker.SHARK_WAVE && chr.getSkillLevel(ThunderBreaker.SHARK_WAVE) > 0) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, 0, attack.allDamage, attack.speed, attack.direction, attack.display), false);
            applyAttack(attack, chr, 1);
            
            for (int i = 0; i < attack.numAttacked; i++) {
                chr.handleEnergyChargeGain();
            }
        } else if (attack.skill == Aran.COMBO_SMASH || attack.skill == Aran.COMBO_FENRIR || attack.skill == Aran.COMBO_TEMPEST) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, 0, attack.allDamage, attack.speed, attack.direction, attack.display), false);
            if (attack.skill == Aran.COMBO_SMASH && chr.getCombo() >= 30) {
            	chr.setCombo((short) 0);
            	applyAttack(attack, chr, 1);
            } else if (attack.skill == Aran.COMBO_FENRIR && chr.getCombo() >= 100) {
            	chr.setCombo((short) 0);
            	applyAttack(attack, chr, 2);
            } else if (attack.skill == Aran.COMBO_TEMPEST && chr.getCombo() >= 200) {
            	chr.setCombo((short) 0);
                applyAttack(attack, chr, 4);
            }
        } else {
            Item weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
            MapleWeaponType type = MapleItemInformationProvider.getInstance().getWeaponType(weapon.getItemId());
            if (type == MapleWeaponType.NOT_A_WEAPON) {
                return;
            }
            short slot = -1;
            int projectile = 0;
            byte bulletCount = 1;
            MapleStatEffect effect = null;
            if (attack.skill != 0) {
                effect = attack.getAttackEffect(chr, null);
                bulletCount = effect.getBulletCount();
                if (effect.getCooldown() > 0) {
                    c.announce(MaplePacketCreator.skillCooldown(attack.skill, effect.getCooldown()));
                }
                
                if(attack.skill == 4111004) {   // shadow meso
                    bulletCount = 0;
                    
                    int money = effect.getMoneyCon();
                    if (money != 0) {
                        int moneyMod = money / 2;
                        money += Randomizer.nextInt(moneyMod);
                        if (money > chr.getMeso()) {
                            money = chr.getMeso();
                        }
                        chr.gainMeso(-money, false);
                    }
                }
            }
            boolean hasShadowPartner = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null;
            if (hasShadowPartner) {
                bulletCount *= 2;
            }
            MapleInventory inv = chr.getInventory(MapleInventoryType.USE);
            for (short i = 1; i <= inv.getSlotLimit(); i++) {
                Item item = inv.getItem(i);
                if (item != null) {
                    int id = item.getItemId();
                    slot = item.getPosition();
                    
                    boolean bow = ItemConstants.isArrowForBow(id);
                    boolean cbow = ItemConstants.isArrowForCrossBow(id);
                    if (item.getQuantity() >= bulletCount) { //Fixes the bug where you can't use your last arrow.
                        if (type == MapleWeaponType.CLAW && ItemConstants.isThrowingStar(id) && weapon.getItemId() != 1472063) {
                            if (((id == 2070007 || id == 2070018) && chr.getLevel() < 70) || (id == 2070016 && chr.getLevel() < 50)) {
                            } else {	
                                projectile = id;
                                break;
                            }
                        } else if ((type == MapleWeaponType.GUN && ItemConstants.isBullet(id))) {
                            if (id == 2331000 && id == 2332000) {
                                if (chr.getLevel() > 69) {
                                    projectile = id;
                                    break;
                                }
                            } else if (chr.getLevel() > (id % 10) * 20 + 9) {
                                projectile = id;
                                break;
                            }
                        } else if ((type == MapleWeaponType.BOW && bow) || (type == MapleWeaponType.CROSSBOW && cbow) || (weapon.getItemId() == 1472063 && (bow || cbow))) {
                            projectile = id;
                            break;
                        }
                    }
                }
            }            
            boolean soulArrow = chr.getBuffedValue(MapleBuffStat.SOULARROW) != null;
            boolean shadowClaw = chr.getBuffedValue(MapleBuffStat.SHADOW_CLAW) != null;
            if (projectile != 0) {
                if (!soulArrow && !shadowClaw && attack.skill != 11101004 && attack.skill != 15111007 && attack.skill != 14101006) {
                    byte bulletConsume = bulletCount;

                    if (effect != null && effect.getBulletConsume() != 0) {
                        bulletConsume = (byte) (effect.getBulletConsume() * (hasShadowPartner ? 2 : 1));           
                    }

                    if(slot < 0) System.out.println("<ERROR> Projectile to use was unable to be found.");
                    else MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, bulletConsume, false, true);
                }
            }
            
            if (projectile != 0 || soulArrow || attack.skill == 11101004 || attack.skill == 15111007 || attack.skill == 14101006 || attack.skill == 4111004 || attack.skill == 13101005) {
            	int visProjectile = projectile; //visible projectile sent to players
                if (ItemConstants.isThrowingStar(projectile)) {
                    MapleInventory cash = chr.getInventory(MapleInventoryType.CASH);
                    for (int i = 1; i <= cash.getSlotLimit(); i++) { // impose order...
                        Item item = cash.getItem((short) i);
                        if (item != null) {
                            if (item.getItemId() / 1000 == 5021) {
                                visProjectile = item.getItemId();
                                break;
                            }
                        }
                    }
                } else if (soulArrow || attack.skill == 3111004 || attack.skill == 3211004 || attack.skill == 11101004 || attack.skill == 15111007 || attack.skill == 14101006 || attack.skill == 13101005) {
                    visProjectile = 0;
                }
                
                byte[] packet;
                switch (attack.skill) {
                    case 3121004: // Hurricane
                    case 3221001: // Pierce
                    case 5221004: // Rapid Fire
                    case 13111002: // KoC Hurricane
                        packet = MaplePacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.rangedirection, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed, attack.direction, attack.display);
                        break;
                    default:
                        packet = MaplePacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed, attack.direction, attack.display);
                        break;
                }
                chr.getMap().broadcastMessage(chr, packet, false, true);
                
                if (attack.skill != 0) {
                    Skill skill = SkillFactory.getSkill(attack.skill);
                    MapleStatEffect effect_ = skill.getEffect(chr.getSkillLevel(skill));
                    if (effect_.getCooldown() > 0) {
                        if (chr.skillIsCooling(attack.skill)) {
                            return;
                        } else {
                            c.announce(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
                            chr.addCooldown(attack.skill, currentServerTime(), effect_.getCooldown() * 1000);
                        }
                    }
                }
                
                if (chr.getSkillLevel(SkillFactory.getSkill(NightWalker.VANISH)) > 0 && chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && attack.numAttacked > 0 && chr.getBuffSource(MapleBuffStat.DARKSIGHT) != 9101004) {
                    chr.cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
                    chr.cancelBuffStats(MapleBuffStat.DARKSIGHT);
                } else if(chr.getSkillLevel(SkillFactory.getSkill(WindArcher.WIND_WALK)) > 0 && chr.getBuffedValue(MapleBuffStat.WIND_WALK) != null && attack.numAttacked > 0) {
                    chr.cancelEffectFromBuffStat(MapleBuffStat.WIND_WALK);
                    chr.cancelBuffStats(MapleBuffStat.WIND_WALK);
                }
                
                applyAttack(attack, chr, bulletCount);
            }
        }
    }
}