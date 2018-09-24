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

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.MonsterDropEntry;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.LittleEndianAccessor;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleJob;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ServerConstants;
import constants.skills.Aran;
import constants.skills.Assassin;
import constants.skills.Bandit;
import constants.skills.Bishop;
import constants.skills.BlazeWizard;
import constants.skills.Bowmaster;
import constants.skills.Brawler;
import constants.skills.Buccaneer;
import constants.skills.ChiefBandit;
import constants.skills.Cleric;
import constants.skills.Corsair;
import constants.skills.Crossbowman;
import constants.skills.Crusader;
import constants.skills.DawnWarrior;
import constants.skills.DragonKnight;
import constants.skills.Evan;
import constants.skills.FPArchMage;
import constants.skills.FPMage;
import constants.skills.FPWizard;
import constants.skills.Fighter;
import constants.skills.Gunslinger;
import constants.skills.Hermit;
import constants.skills.Hero;
import constants.skills.Hunter;
import constants.skills.ILArchMage;
import constants.skills.ILMage;
import constants.skills.Marauder;
import constants.skills.Marksman;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Outlaw;
import constants.skills.Page;
import constants.skills.Paladin;
import constants.skills.Ranger;
import constants.skills.Rogue;
import constants.skills.Shadower;
import constants.skills.Sniper;
import constants.skills.Spearman;
import constants.skills.SuperGM;
import constants.skills.ThunderBreaker;
import constants.skills.WhiteKnight;
import constants.skills.WindArcher;
import scripting.AbstractPlayerInteraction;

public abstract class AbstractDealDamageHandler extends AbstractMaplePacketHandler {

    public static class AttackInfo {

        public int numAttacked, numDamage, numAttackedAndDamage, skill, skilllevel, stance, direction, rangedirection, charge, display;
        public Map<Integer, List<Integer>> allDamage;
        public boolean ranged, magic;
        public int speed = 4;
        public Point position = new Point();
        public MapleStatEffect getAttackEffect(MapleCharacter chr, Skill theSkill) {
            Skill mySkill = theSkill;
            if (mySkill == null) {
                mySkill = SkillFactory.getSkill(GameConstants.getHiddenSkill(skill));
            }
            
            int skillLevel = chr.getSkillLevel(mySkill);
            if(skillLevel == 0 && GameConstants.isPqSkillMap(chr.getMapId()) && GameConstants.isPqSkill(mySkill.getId())) skillLevel = 1;
            
            if (skillLevel == 0) {
                return null;
            }
            if (display > 80) { //Hmm
                if (!mySkill.getAction()) {
                    AutobanFactory.FAST_ATTACK.autoban(chr, "WZ Edit; adding action to a skill: " + display);
                    return null;
                }
            }
            return mySkill.getEffect(skillLevel);
        }
    }

    protected synchronized void applyAttack(AttackInfo attack, final MapleCharacter player, int attackCount) {
        Skill theSkill = null;
        MapleStatEffect attackEffect = null;
        final int job = player.getJob().getId();
        try {
            if (player.isBanned()) {
                return;
            }
            if (attack.skill != 0) {
                theSkill = SkillFactory.getSkill(GameConstants.getHiddenSkill(attack.skill)); //returns back the skill id if its not a hidden skill so we are gucci
                attackEffect = attack.getAttackEffect(player, theSkill);
                if (attackEffect == null) {
                    player.getClient().announce(MaplePacketCreator.enableActions());
                    return;
                }

                if (player.getMp() < attackEffect.getMpCon()) {
                    AutobanFactory.MPCON.addPoint(player.getAutobanManager(), "Skill: " + attack.skill + "; Player MP: " + player.getMp() + "; MP Needed: " + attackEffect.getMpCon());
                }

                int mobCount = attackEffect.getMobCount();
                if (attack.skill != Cleric.HEAL) {
                    if (player.isAlive()) {
                        if(attack.skill == NightWalker.POISON_BOMB) {// Poison Bomb
                            attackEffect.applyTo(player, new Point(attack.position.x, attack.position.y));
                        } else if(attack.skill != Aran.BODY_PRESSURE) {// prevent BP refreshing
                            attackEffect.applyTo(player);
                            
                            if (attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Page.FINAL_ATTACK_BW || attack.skill == Page.FINAL_ATTACK_SWORD || attack.skill == Fighter.FINAL_ATTACK_SWORD
                                    || attack.skill == Fighter.FINAL_ATTACK_AXE || attack.skill == Spearman.FINAL_ATTACK_SPEAR || attack.skill == Spearman.FINAL_ATTACK_POLEARM || attack.skill == WindArcher.FINAL_ATTACK
                                    || attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Hunter.FINAL_ATTACK || attack.skill == Crossbowman.FINAL_ATTACK) {
                                
                                mobCount = 15;//:(
                            } else if (attack.skill == Aran.HIDDEN_FULL_DOUBLE || attack.skill == Aran.HIDDEN_FULL_TRIPLE || attack.skill == Aran.HIDDEN_OVER_DOUBLE || attack.skill == Aran.HIDDEN_OVER_TRIPLE) {
                                mobCount = 12;
                            }
                        }
                    } else {
                        player.getClient().announce(MaplePacketCreator.enableActions());
                    }
                }
                
                if (attack.numAttacked > mobCount) {
                    AutobanFactory.MOB_COUNT.autoban(player, "Skill: " + attack.skill + "; Count: " + attack.numAttacked + " Max: " + attackEffect.getMobCount());
                    return;
                }
            }
            if (!player.isAlive()) {
                return;
            }

            //WTF IS THIS F3,1
            /*if (attackCount != attack.numDamage && attack.skill != ChiefBandit.MESO_EXPLOSION && attack.skill != NightWalker.VAMPIRE && attack.skill != WindArcher.WIND_SHOT && attack.skill != Aran.COMBO_SMASH && attack.skill != Aran.COMBO_FENRIR && attack.skill != Aran.COMBO_TEMPEST && attack.skill != NightLord.NINJA_AMBUSH && attack.skill != Shadower.NINJA_AMBUSH) {
                return;
            }*/
            
            int totDamage = 0;
            final MapleMap map = player.getMap();

            if (attack.skill == ChiefBandit.MESO_EXPLOSION) {
                int delay = 0;
                for (Integer oned : attack.allDamage.keySet()) {
                    MapleMapObject mapobject = map.getMapObject(oned.intValue());
                    if (mapobject != null && mapobject.getType() == MapleMapObjectType.ITEM) {
                        final MapleMapItem mapitem = (MapleMapItem) mapobject;
                            if (mapitem.getMeso() == 0) { //Maybe it is possible some how?
                                return;
                            }
                            
                            mapitem.lockItem();
                            try {
                                if (mapitem.isPickedUp()) {
                                    return;
                                }
                                TimerManager.getInstance().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        mapitem.lockItem();
                                        try {
                                            if (mapitem.isPickedUp()) {
                                                return;
                                            }
                                            map.pickItemDrop(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 4, 0), mapitem);
                                        } finally {
                                            mapitem.unlockItem();
                                        }
                                    }
                                }, delay);
                                delay += 100;
                            } finally {
                                mapitem.unlockItem();
                            }
                    } else if (mapobject != null && mapobject.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                }
            }
            for (Integer oned : attack.allDamage.keySet()) {
                final MapleMonster monster = map.getMonsterByOid(oned.intValue());
                if (monster != null) { 
                    double distance = player.getPosition().distanceSq(monster.getPosition());
                    double distanceToDetect = 200000.0;
                    
                    if(attack.ranged)
                        distanceToDetect += 400000;
                    
                    if(attack.magic)
                        distanceToDetect += 200000;
                    
                    if(player.getJob().isA(MapleJob.ARAN1))
                        distanceToDetect += 200000; // Arans have extra range over normal warriors.
                    
                    if(attack.skill == Aran.COMBO_SMASH || attack.skill == Aran.BODY_PRESSURE)
                        distanceToDetect += 40000;
                    
                    else if(attack.skill == Bishop.GENESIS || attack.skill == ILArchMage.BLIZZARD || attack.skill == FPArchMage.METEOR_SHOWER)
                        distanceToDetect += 275000;
                    
                    else if(attack.skill == Hero.BRANDISH || attack.skill == DragonKnight.SPEAR_CRUSHER || attack.skill == DragonKnight.POLE_ARM_CRUSHER)
                        distanceToDetect += 40000;
                    
                    else if(attack.skill == DragonKnight.DRAGON_ROAR || attack.skill == SuperGM.SUPER_DRAGON_ROAR)
                        distanceToDetect += 250000;
                    
                    else if(attack.skill == Shadower.BOOMERANG_STEP)
                        distanceToDetect += 60000;
                    
                    if(distance > distanceToDetect) {
                        AutobanFactory.DISTANCE_HACK.alert(player, "Distance Sq to monster: " + distance + " SID: " + attack.skill + " MID: " + monster.getId());
                    }
                    
                    int totDamageToOneMonster = 0;
                    List<Integer> onedList = attack.allDamage.get(oned);
                    for (Integer eachd : onedList) {
                        if(eachd < 0) eachd += Integer.MAX_VALUE;
                        totDamageToOneMonster += eachd;
                    }
                    totDamage += totDamageToOneMonster;
                    player.checkMonsterAggro(monster);
                    if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null && (attack.skill == 0 || attack.skill == Rogue.DOUBLE_STAB || attack.skill == Bandit.SAVAGE_BLOW || attack.skill == ChiefBandit.ASSAULTER || attack.skill == ChiefBandit.BAND_OF_THIEVES || attack.skill == Shadower.ASSASSINATE || attack.skill == Shadower.TAUNT || attack.skill == Shadower.BOOMERANG_STEP)) {
                        Skill pickpocket = SkillFactory.getSkill(ChiefBandit.PICKPOCKET);
                        int picklv = (player.isGM()) ? pickpocket.getMaxLevel() : player.getSkillLevel(pickpocket);
                        if(picklv > 0) {
                            int delay = 0;
                            final int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();
                            for (Integer eachd : onedList) {				
                                eachd += Integer.MAX_VALUE;

                                if (pickpocket.getEffect(picklv).makeChanceResult()) {
                                    final Integer eachdf;
                                    if(eachd < 0)
                                            eachdf = eachd + Integer.MAX_VALUE;
                                    else
                                            eachdf = eachd;

                                    TimerManager.getInstance().schedule(new Runnable() {
                                        @Override
                                        public void run() {
                                            player.getMap().spawnMesoDrop(Math.min((int) Math.max(((double) eachdf / (double) 20000) * (double) maxmeso, (double) 1), maxmeso), new Point((int) (monster.getPosition().getX() + Randomizer.nextInt(100) - 50), (int) (monster.getPosition().getY())), monster, player, true, (byte) 2);
                                        }
                                    }, delay);
                                    delay += 100;
                                }
                            }
                        }
                    } else if (attack.skill == Marauder.ENERGY_DRAIN || attack.skill == ThunderBreaker.ENERGY_DRAIN || attack.skill == NightWalker.VAMPIRE || attack.skill == Assassin.DRAIN) {
                        player.addHP(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skill))).getX() / 100.0), player.getCurrentMaxHp() / 2)));
                    } else if (attack.skill == Bandit.STEAL) {
                        Skill steal = SkillFactory.getSkill(Bandit.STEAL);
                        if (monster.getStolen().size() < 1) { // One steal per mob <3
                            if (steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()) {
                                MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
                                
                                List<Integer> dropPool = mi.retrieveDropPool(monster.getId());
                                if(!dropPool.isEmpty()) {
                                    Integer rndPool = (int) Math.floor(Math.random() * dropPool.get(dropPool.size() - 1));
                                    
                                    int i = 0;
                                    while(rndPool >= dropPool.get(i)) i++;
                                    
                                    List<MonsterDropEntry> toSteal = new ArrayList<>();
                                    toSteal.add(mi.retrieveDrop(monster.getId()).get(i));
                                    
                                    player.getMap().dropItemsFromMonster(toSteal, player, monster);
                                    monster.addStolen(toSteal.get(0).itemId);
                                }
                            }
                        }
                    } else if (attack.skill == FPArchMage.FIRE_DEMON) {
                        monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(FPArchMage.FIRE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(FPArchMage.FIRE_DEMON))).getDuration() * 1000);
                    } else if (attack.skill == ILArchMage.ICE_DEMON) {
                        monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(ILArchMage.ICE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(ILArchMage.ICE_DEMON))).getDuration() * 1000);
                    } else if (attack.skill == Outlaw.HOMING_BEACON || attack.skill == Corsair.BULLSEYE) {
                        player.setMarkedMonster(monster.getObjectId());
                        player.announce(MaplePacketCreator.giveBuff(1, attack.skill, Collections.singletonList(new Pair<>(MapleBuffStat.HOMING_BEACON, monster.getObjectId()))));
                    } else if (attack.skill == Outlaw.FLAME_THROWER) {
                        if (!monster.isBoss()) {
                            Skill type = SkillFactory.getSkill(Outlaw.FLAME_THROWER);
                            if (player.getSkillLevel(type) > 0) {
                                MapleStatEffect DoT = type.getEffect(player.getSkillLevel(type));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, null, false);
                                monster.applyStatus(player, monsterStatusEffect, true, DoT.getDuration(), false);
                            }
                        }
                    }
                    
                    if (job == 2111 || job == 2112) {
                    	if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
                            Skill snowCharge = SkillFactory.getSkill(Aran.SNOW_CHARGE);
                            if (totDamageToOneMonster > 0) {
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getX()), snowCharge, null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getY() * 1000);
                            }
                    	}
                    }
                    if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
                        Skill hamstring = SkillFactory.getSkill(Bowmaster.HAMSTRING);
                        if (hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()) {
                            MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
                        }
                    }
                    if (player.getBuffedValue(MapleBuffStat.SLOW) != null) {
                        Skill slow = SkillFactory.getSkill(Evan.SLOW);
                        if (slow.getEffect(player.getSkillLevel(slow)).makeChanceResult()) {
                            MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, slow.getEffect(player.getSkillLevel(slow)).getX()), slow, null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, slow.getEffect(player.getSkillLevel(slow)).getY() * 60 * 1000);
                        }
                    }
                    if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
                        Skill blind = SkillFactory.getSkill(Marksman.BLIND);
                        if (blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()) {
                            MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
                        }
                    }
                    if (job == 121 || job == 122) {
                        for (int charge = 1211005; charge < 1211007; charge++) {
                            Skill chargeSkill = SkillFactory.getSkill(charge);
                            if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)) {                                
                                if (totDamageToOneMonster > 0) {
                                    if (charge == WhiteKnight.BW_ICE_CHARGE || charge == WhiteKnight.SWORD_ICE_CHARGE) {
                                        monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);	
                                        break;
                                    }
                                    if (charge == WhiteKnight.BW_FIRE_CHARGE || charge == WhiteKnight.SWORD_FIRE_CHARGE) {
                                        monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
                                        break;
                                    }
                                }                              
                            }
                        }
                        if (job == 122) {
                            for (int charge = 1221003; charge < 1221004; charge++) {
                                Skill chargeSkill = SkillFactory.getSkill(charge);
                                if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)) {                                
                                    if (totDamageToOneMonster > 0) {
                                        monster.setTempEffectiveness(Element.HOLY, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                        Skill skill;
                        if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                            skill = SkillFactory.getSkill(21100005);
                            player.addHP(((totDamage * skill.getEffect(player.getSkillLevel(skill)).getX()) / 100));
                        }
                    } else if (job == 412 || job == 422 || job == 1411) {
                        Skill type = SkillFactory.getSkill(player.getJob().getId() == 412 ? 4120005 : (player.getJob().getId() == 1411 ? 14110004 : 4220005));
                        if (player.getSkillLevel(type) > 0) {
                            MapleStatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
                            for (int i = 0; i < attackCount; i++) {
                                if (venomEffect.makeChanceResult()) {
                                    if (monster.getVenomMulti() < 3) {
                                        monster.setVenomMulti((monster.getVenomMulti() + 1));
                                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, null, false);
                                        monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                                    }
                                }
                            }
                        }
                    } else if (job >= 311 && job <= 322) {                   	
                    	if (!monster.isBoss()) {
                            Skill mortalBlow;
                            if (job == 311 || job == 312) {
                                mortalBlow = SkillFactory.getSkill(Ranger.MORTAL_BLOW);
                            } else {
                                mortalBlow = SkillFactory.getSkill(Sniper.MORTAL_BLOW);
                            }
                            if (player.getSkillLevel(mortalBlow) > 0) {	                    		
                                MapleStatEffect mortal = mortalBlow.getEffect(player.getSkillLevel(mortalBlow));
                                if (monster.getHp() <= (monster.getStats().getHp() * mortal.getX()) / 100) {
                                    if (Randomizer.rand(1, 100) <= mortal.getY()) {
                                        monster.getMap().killMonster(monster, player, true);
                                    }
                                }
                            }
                    	}
                    }
                    if (attack.skill != 0) {
                        if (attackEffect.getFixDamage() != -1) {
                            if (totDamageToOneMonster != attackEffect.getFixDamage() && totDamageToOneMonster != 0) {
                                AutobanFactory.FIX_DAMAGE.autoban(player, String.valueOf(totDamageToOneMonster) + " damage");
                            }
                            
                            int threeSnailsId = player.getJobType() * 10000000 + 1000;
                            if(attack.skill == threeSnailsId) {
                                if(ServerConstants.USE_ULTRA_THREE_SNAILS) {
                                    int skillLv = player.getSkillLevel(threeSnailsId);

                                    if(skillLv > 0) {
                                        AbstractPlayerInteraction api = player.getClient().getAbstractPlayerInteraction();

                                        int shellId;
                                        switch(skillLv) {
                                            case 1:
                                                shellId = 4000019;
                                                break;

                                            case 2:
                                                shellId = 4000000;
                                                break;

                                            default:
                                                shellId = 4000016;
                                        }

                                        if(api.haveItem(shellId, 1)) {
                                            api.gainItem(shellId, (short) -1, false);
                                            totDamageToOneMonster *= player.getLevel();
                                        } else {
                                            player.dropMessage(5, "You have ran out of shells to activate the hidden power of Three Snails.");
                                        }
                                    } else {
                                        totDamageToOneMonster = 0;
                                    }
                                }
                            }
                        }
                    }
                    if (totDamageToOneMonster > 0 && attackEffect != null) {
                        Map<MonsterStatus, Integer> attackEffectStati = attackEffect.getMonsterStati();
                        if(!attackEffectStati.isEmpty()) {
                            if (attackEffect.makeChanceResult()) {
                                monster.applyStatus(player, new MonsterStatusEffect(attackEffectStati, theSkill, null, false), attackEffect.isPoison(), attackEffect.getDuration());
                            }
                        }
                    }
                    if (attack.skill == Paladin.HEAVENS_HAMMER) {
                        if(!monster.isBoss()) {
                            damageMonsterWithSkill(player, map, monster, monster.getHp() - 1, attack.skill, 1777);
                        } else {
                            int HHDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Paladin.HEAVENS_HAMMER).getEffect(player.getSkillLevel(SkillFactory.getSkill(Paladin.HEAVENS_HAMMER))).getDamage() / 100));
                            damageMonsterWithSkill(player, map, monster, (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8)), attack.skill, 1777);
                        }
                    } else if (attack.skill == Aran.COMBO_TEMPEST) {
                        if(!monster.isBoss()) {
                            damageMonsterWithSkill(player, map, monster, monster.getHp(), attack.skill, 0);
                        } else {
                            int TmpDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Aran.COMBO_TEMPEST).getEffect(player.getSkillLevel(SkillFactory.getSkill(Aran.COMBO_TEMPEST))).getDamage() / 100));
                            damageMonsterWithSkill(player, map, monster, (int) (Math.floor(Math.random() * (TmpDmg / 5) + TmpDmg * .8)), attack.skill, 0);
                        }
                    } else {
                        if(attack.skill == Aran.BODY_PRESSURE) {
                            map.broadcastMessage(MaplePacketCreator.damageMonster(monster.getObjectId(), totDamageToOneMonster));
                        }
                        
                        map.damageMonster(player, monster, totDamageToOneMonster);
                    }
                    if (monster.isBuffed(MonsterStatus.WEAPON_REFLECT)) {
                        List<Pair<Integer, Integer>> mobSkills = monster.getSkills();
                        
                        for (Pair<Integer, Integer> ms : mobSkills) {
                            if (ms.left == 145) {
                                MobSkill toUse = MobSkillFactory.getMobSkill(ms.left, ms.right);
                                player.addHP(-toUse.getX());
                                map.broadcastMessage(player, MaplePacketCreator.damagePlayer(0, monster.getId(), player.getId(), toUse.getX(), 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
                            }
                        }
                    }                
                    if (monster.isBuffed(MonsterStatus.MAGIC_REFLECT)) {
                        List<Pair<Integer, Integer>> mobSkills = monster.getSkills();
                        
                        for (Pair<Integer, Integer> ms : mobSkills) {
                            if (ms.left == 145) {
                                MobSkill toUse = MobSkillFactory.getMobSkill(ms.left, ms.right);
                                player.addMP(-toUse.getY());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void damageMonsterWithSkill(final MapleCharacter attacker, final MapleMap map, final MapleMonster monster, final int damage, int skillid, int fixedTime) {
        int animationTime;
        
        if(fixedTime == 0) animationTime = SkillFactory.getSkill(skillid).getAnimationTime();
        else animationTime = fixedTime;
        
        if(animationTime > 0) { // be sure to only use LIMITED ATTACKS with animation time here
            TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    map.broadcastMessage(MaplePacketCreator.damageMonster(monster.getObjectId(), damage), monster.getPosition());
                    map.damageMonster(attacker, monster, damage);
                }
            }, animationTime);
        } else {
            map.broadcastMessage(MaplePacketCreator.damageMonster(monster.getObjectId(), damage), monster.getPosition());
            map.damageMonster(attacker, monster, damage);
        }
    }
    
    protected AttackInfo parseDamage(LittleEndianAccessor lea, MapleCharacter chr, boolean ranged, boolean magic) {    
    	//2C 00 00 01 91 A1 12 00 A5 57 62 FC E2 75 99 10 00 47 80 01 04 01 C6 CC 02 DD FF 5F 00
        AttackInfo ret = new AttackInfo();
        lea.readByte();
        ret.numAttackedAndDamage = lea.readByte();
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.allDamage = new HashMap<>();
        ret.skill = lea.readInt();
        ret.ranged = ranged;
        ret.magic = magic;
        
        if (ret.skill > 0) {
            ret.skilllevel = chr.getSkillLevel(ret.skill);
            if(ret.skilllevel == 0 && GameConstants.isPqSkillMap(chr.getMapId()) && GameConstants.isPqSkill(ret.skill)) ret.skilllevel = 1;
        }
        
        if (ret.skill == Evan.ICE_BREATH || ret.skill == Evan.FIRE_BREATH || ret.skill == FPArchMage.BIG_BANG || ret.skill == ILArchMage.BIG_BANG || ret.skill == Bishop.BIG_BANG || ret.skill == Gunslinger.GRENADE || ret.skill == Brawler.CORKSCREW_BLOW || ret.skill == ThunderBreaker.CORKSCREW_BLOW || ret.skill == NightWalker.POISON_BOMB) {
            ret.charge = lea.readInt();
        } else {
            ret.charge = 0;
        }
        
        lea.skip(8);
        ret.display = lea.readByte();
        ret.direction = lea.readByte();
        ret.stance = lea.readByte();
        if (ret.skill == ChiefBandit.MESO_EXPLOSION) {
            if (ret.numAttackedAndDamage == 0) {
                lea.skip(10);
                int bullets = lea.readByte();
                for (int j = 0; j < bullets; j++) {
                    int mesoid = lea.readInt();
                    lea.skip(1);
                    ret.allDamage.put(Integer.valueOf(mesoid), null);
                }
                return ret;
            } else {
                lea.skip(6);
            }
            for (int i = 0; i < ret.numAttacked + 1; i++) {
                int oid = lea.readInt();
                if (i < ret.numAttacked) {
                    lea.skip(12);
                    int bullets = lea.readByte();
                    List<Integer> allDamageNumbers = new ArrayList<>();
                    for (int j = 0; j < bullets; j++) {
                        int damage = lea.readInt();
                        allDamageNumbers.add(Integer.valueOf(damage));
                    }
                    ret.allDamage.put(Integer.valueOf(oid), allDamageNumbers);
                    lea.skip(4);
                } else {
                    int bullets = lea.readByte();
                    for (int j = 0; j < bullets; j++) {
                        int mesoid = lea.readInt();
                        lea.skip(1);
                        ret.allDamage.put(Integer.valueOf(mesoid), null);
                    }
                }
            }
            return ret;
        }
        if (ranged) {
            lea.readByte();
            ret.speed = lea.readByte();
            lea.readByte();
            ret.rangedirection = lea.readByte();
            lea.skip(7);
            if (ret.skill == Bowmaster.HURRICANE || ret.skill == Marksman.PIERCING_ARROW || ret.skill == Corsair.RAPID_FIRE || ret.skill == WindArcher.HURRICANE) {
                lea.skip(4);
            }
        } else {
            lea.readByte();
            ret.speed = lea.readByte();
            lea.skip(4);
        }
        
        // Find the base damage to base futher calculations on.
        // Several skills have their own formula in this section.
        int calcDmgMax = 0;	
        
        if(magic && ret.skill != 0) {
            calcDmgMax = (chr.getTotalMagic() * chr.getTotalMagic() / 1000 + chr.getTotalMagic()) / 30 + chr.getTotalInt() / 200;
        } else if(ret.skill == 4001344 || ret.skill == NightWalker.LUCKY_SEVEN || ret.skill == NightLord.TRIPLE_THROW) {
            calcDmgMax = (chr.getTotalLuk() * 5) * chr.getTotalWatk() / 100;
        } else if(ret.skill == DragonKnight.DRAGON_ROAR) {
            calcDmgMax = (chr.getTotalStr() * 4 + chr.getTotalDex()) * chr.getTotalWatk() / 100;
        } else if(ret.skill == NightLord.VENOMOUS_STAR || ret.skill == Shadower.VENOMOUS_STAB) {
            calcDmgMax = (int) (18.5 * (chr.getTotalStr() + chr.getTotalLuk()) + chr.getTotalDex() * 2) / 100 * chr.calculateMaxBaseDamage(chr.getTotalWatk());
        } else {
            calcDmgMax = chr.calculateMaxBaseDamage(chr.getTotalWatk());
        }

        if(ret.skill != 0) {
            Skill skill = SkillFactory.getSkill(ret.skill);
            MapleStatEffect effect = skill.getEffect(ret.skilllevel);

            if (magic) {
                // Since the skill is magic based, use the magic formula
                if(chr.getJob() == MapleJob.IL_ARCHMAGE || chr.getJob() == MapleJob.IL_MAGE) {
                    int skillLvl = chr.getSkillLevel(ILMage.ELEMENT_AMPLIFICATION);
                    if(skillLvl > 0)
                        calcDmgMax = calcDmgMax * SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                } else if(chr.getJob() == MapleJob.FP_ARCHMAGE || chr.getJob() == MapleJob.FP_MAGE) {
                    int skillLvl = chr.getSkillLevel(FPMage.ELEMENT_AMPLIFICATION);
                    if(skillLvl > 0)
                        calcDmgMax = calcDmgMax * SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                } else if(chr.getJob() == MapleJob.BLAZEWIZARD3 || chr.getJob() == MapleJob.BLAZEWIZARD4) {
                    int skillLvl = chr.getSkillLevel(BlazeWizard.ELEMENT_AMPLIFICATION);
                    if(skillLvl > 0)
                        calcDmgMax = calcDmgMax * SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                } else if(chr.getJob() == MapleJob.EVAN7 || chr.getJob() == MapleJob.EVAN8 || chr.getJob() == MapleJob.EVAN9 || chr.getJob() == MapleJob.EVAN10) {
                    int skillLvl = chr.getSkillLevel(Evan.MAGIC_AMPLIFICATION);
                    if(skillLvl > 0)
                        calcDmgMax = calcDmgMax * SkillFactory.getSkill(Evan.MAGIC_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                }
                
                calcDmgMax *= effect.getMatk();
                if(ret.skill == Cleric.HEAL) {
                    // This formula is still a bit wonky, but it is fairly accurate.
                    calcDmgMax = (int) Math.round((chr.getTotalInt() * 4.8 + chr.getTotalLuk() * 4) * chr.getTotalMagic() / 1000);
                    calcDmgMax = calcDmgMax * effect.getHp() / 100; 
                    
                    ret.speed = 7;
                }
            } else if(ret.skill == Hermit.SHADOW_MESO) {
                // Shadow Meso also has its own formula
                calcDmgMax = effect.getMoneyCon() * 10;
                calcDmgMax = (int) Math.floor(calcDmgMax * 1.5);
            } else {
                // Normal damage formula for skills
                calcDmgMax = calcDmgMax * effect.getDamage() / 100;
            }
        }

        Integer comboBuff = chr.getBuffedValue(MapleBuffStat.COMBO);
        if(comboBuff != null && comboBuff > 0) {
            int oid = chr.isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
            int advcomboid = chr.isCygnus() ? DawnWarrior.ADVANCED_COMBO : Hero.ADVANCED_COMBO;
            
            if(comboBuff > 6) {
                // Advanced Combo
                MapleStatEffect ceffect = SkillFactory.getSkill(advcomboid).getEffect(chr.getSkillLevel(advcomboid));
                calcDmgMax = (int) Math.floor(calcDmgMax * (ceffect.getDamage() + 50) / 100 + 0.20 + (comboBuff - 5) * 0.04);
            } else {
                // Normal Combo
                int skillLv = chr.getSkillLevel(oid);
                if(skillLv <= 0 || chr.isGM()) skillLv = SkillFactory.getSkill(oid).getMaxLevel();
                
                if(skillLv > 0) {
                    MapleStatEffect ceffect = SkillFactory.getSkill(oid).getEffect(skillLv);
                    calcDmgMax = (int) Math.floor(calcDmgMax * (ceffect.getDamage() + 50) / 100 + Math.floor((comboBuff - 1) * (skillLv / 6)) / 100);
                }
            }
            
            if(GameConstants.isFinisherSkill(ret.skill)) {
                // Finisher skills do more damage based on how many orbs the player has.
                int orbs = comboBuff - 1;
                if(orbs == 2) 
                    calcDmgMax *= 1.2;
                else if(orbs == 3)
                    calcDmgMax *= 1.54;
                else if(orbs == 4)
                    calcDmgMax *= 2;
                else if(orbs >= 5)
                    calcDmgMax *= 2.5;
            }
        }
        
        if(chr.getEnergyBar() == 15000) {
            int energycharge = chr.isCygnus() ? ThunderBreaker.ENERGY_CHARGE : Marauder.ENERGY_CHARGE;
            MapleStatEffect ceffect = SkillFactory.getSkill(energycharge).getEffect(chr.getSkillLevel(energycharge));
            calcDmgMax *= ceffect.getDamage() / 100;
        }
        
        if(chr.getMapId() >= 914000000 && chr.getMapId() <= 914000500) {
            calcDmgMax += 80000; // Aran Tutorial.
        }

        boolean canCrit = false;
        if(chr.getJob().isA((MapleJob.BOWMAN)) || chr.getJob().isA(MapleJob.THIEF) || chr.getJob().isA(MapleJob.NIGHTWALKER1) || chr.getJob().isA(MapleJob.WINDARCHER1) || chr.getJob() == MapleJob.ARAN3 || chr.getJob() == MapleJob.ARAN4 || chr.getJob() == MapleJob.MARAUDER || chr.getJob() == MapleJob.BUCCANEER) {
            canCrit = true;
        }
        
        if(chr.getBuffEffect(MapleBuffStat.SHARP_EYES) != null) {
            // Any class that has sharp eyes can crit. Also, since it stacks with normal crit go ahead
            // and calc it in.

            canCrit = true;
            calcDmgMax *= 1.4;
        }
        
        boolean shadowPartner = false;
        if(chr.getBuffEffect(MapleBuffStat.SHADOWPARTNER) != null) {
            shadowPartner = true;
        }	
		
        if(ret.skill != 0) {
            int fixed = ret.getAttackEffect(chr, SkillFactory.getSkill(ret.skill)).getFixDamage();
            if(fixed > 0)
                calcDmgMax = fixed;
        }
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(14);
            List<Integer> allDamageNumbers = new ArrayList<>();
            MapleMonster monster = chr.getMap().getMonsterByOid(oid);
            
            if(chr.getBuffEffect(MapleBuffStat.WK_CHARGE) != null) {
                // Charge, so now we need to check elemental effectiveness
                int sourceID = chr.getBuffSource(MapleBuffStat.WK_CHARGE);
				int level = chr.getBuffedValue(MapleBuffStat.WK_CHARGE);
                if(monster != null) {
                    if(sourceID == WhiteKnight.BW_FIRE_CHARGE || sourceID == WhiteKnight.SWORD_FIRE_CHARGE) {
                        if(monster.getStats().getEffectiveness(Element.FIRE) == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.05 + level * 0.015;
                        }
                    } else if(sourceID == WhiteKnight.BW_ICE_CHARGE || sourceID == WhiteKnight.SWORD_ICE_CHARGE) {
                        if(monster.getStats().getEffectiveness(Element.ICE) == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.05 + level * 0.015;
                        }
                    } else if(sourceID == WhiteKnight.BW_LIT_CHARGE || sourceID == WhiteKnight.SWORD_LIT_CHARGE) {
                        if(monster.getStats().getEffectiveness(Element.LIGHTING) == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.05 + level * 0.015;
                        }
                    } else if(sourceID == Paladin.BW_HOLY_CHARGE || sourceID == Paladin.SWORD_HOLY_CHARGE) {
                        if(monster.getStats().getEffectiveness(Element.HOLY) == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.2 + level * 0.015;
                        }
                    }
                } else {
                    // Since we already know the skill has an elemental attribute, but we dont know if the monster is weak or not, lets
                    // take the safe approach and just assume they are weak.
                    calcDmgMax *= 1.5;
                }
            }
            
            if(ret.skill != 0) {
                    Skill skill = SkillFactory.getSkill(ret.skill);
                    if(skill.getElement() != Element.NEUTRAL && chr.getBuffedValue(MapleBuffStat.ELEMENTAL_RESET) == null) {
                        // The skill has an element effect, so we need to factor that in.
                        if(monster != null) {
                            ElementalEffectiveness eff = monster.getElementalEffectiveness(skill.getElement());
                            if(eff == ElementalEffectiveness.WEAK) {
                                calcDmgMax *= 1.5;
                            } else if(eff == ElementalEffectiveness.STRONG) {
                                //calcDmgMax *= 0.5;
                            }
                        } else {
                            // Since we already know the skill has an elemental attribute, but we dont know if the monster is weak or not, lets
                            // take the safe approach and just assume they are weak.
                            calcDmgMax *= 1.5;
                        }
                    }
                    if(ret.skill == FPWizard.POISON_BREATH || ret.skill == FPMage.POISON_MIST || ret.skill == FPArchMage.FIRE_DEMON || ret.skill == ILArchMage.ICE_DEMON) {
                            if(monster != null) {
                                    // Turns out poison is completely server side, so I don't know why I added this. >.<
                                    //calcDmgMax = monster.getHp() / (70 - chr.getSkillLevel(skill));
                            }
                    } else if(ret.skill == Hermit.SHADOW_WEB) {
                            if(monster != null) {
                                    calcDmgMax = monster.getHp() / (50 - chr.getSkillLevel(skill));
                            }
                    } else if(ret.skill == Hermit.SHADOW_MESO) {
                            if(monster != null) {
                                    monster.debuffMob(Hermit.SHADOW_MESO);
                            }
                    }
            }
            
            for (int j = 0; j < ret.numDamage; j++) {
                    int damage = lea.readInt();
                    int hitDmgMax = calcDmgMax;
                    if(ret.skill == Buccaneer.BARRAGE) {
                        if(j > 3)
                            hitDmgMax *= Math.pow(2, (j - 3));
                    }
                    if(shadowPartner) {
                            // For shadow partner, the second half of the hits only do 50% damage. So calc that
                            // in for the crit effects.
                            if(j >= ret.numDamage / 2) {
                                    hitDmgMax *= 0.5;
                            }
                    }

                    if(ret.skill == Marksman.SNIPE) {
                            damage = 195000 + Randomizer.nextInt(5000);
                            hitDmgMax = 200000;
                    }

                    int maxWithCrit = hitDmgMax;
                    if(canCrit) // They can crit, so up the max.
                            maxWithCrit *= 2;

                    // Warn if the damage is over 1.5x what we calculated above.
                    if(damage > maxWithCrit * 1.5) {
                        AutobanFactory.DAMAGE_HACK.alert(chr, "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
                    }

                    // Add a ab point if its over 5x what we calculated.
                    if(damage > maxWithCrit  * 5) {
                            AutobanFactory.DAMAGE_HACK.addPoint(chr.getAutobanManager(), "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
                    }

                    if (ret.skill == Marksman.SNIPE || (canCrit && damage > hitDmgMax)) {
                            // If the skill is a crit, inverse the damage to make it show up on clients.
                            damage = -Integer.MAX_VALUE + damage - 1;
                    }

                    allDamageNumbers.add(damage);
            }
            if (ret.skill != Corsair.RAPID_FIRE || ret.skill != Aran.HIDDEN_FULL_DOUBLE || ret.skill != Aran.HIDDEN_FULL_TRIPLE || ret.skill != Aran.HIDDEN_OVER_DOUBLE || ret.skill != Aran.HIDDEN_OVER_TRIPLE) {
            	lea.skip(4);
            }
            ret.allDamage.put(Integer.valueOf(oid), allDamageNumbers);
        }
        if (ret.skill == NightWalker.POISON_BOMB) { // Poison Bomb
            lea.skip(4);
            ret.position.setLocation(lea.readShort(), lea.readShort());
        }
        return ret;
    }

    private static int rand(int l, int u) {
        return (int) ((Math.random() * (u - l + 1)) + l);
    }
}
