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
package server;

import client.inventory.manipulator.MapleInventoryManipulator;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.server.Server;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.MapleMonster;
import server.maps.FieldLimit;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleDisease;
import client.MapleJob;
import client.MapleMount;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.Aran;
import constants.skills.Assassin;
import constants.skills.Bandit;
import constants.skills.Beginner;
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
import constants.skills.DarkKnight;
import constants.skills.DawnWarrior;
import constants.skills.DragonKnight;
import constants.skills.Evan;
import constants.skills.FPArchMage;
import constants.skills.FPMage;
import constants.skills.FPWizard;
import constants.skills.Fighter;
import constants.skills.GM;
import constants.skills.Gunslinger;
import constants.skills.Hermit;
import constants.skills.Hero;
import constants.skills.Hunter;
import constants.skills.ILArchMage;
import constants.skills.ILMage;
import constants.skills.ILWizard;
import constants.skills.Legend;
import constants.skills.Magician;
import constants.skills.Marauder;
import constants.skills.Marksman;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Noblesse;
import constants.skills.Outlaw;
import constants.skills.Page;
import constants.skills.Paladin;
import constants.skills.Pirate;
import constants.skills.Priest;
import constants.skills.Ranger;
import constants.skills.Rogue;
import constants.skills.Shadower;
import constants.skills.Sniper;
import constants.skills.Spearman;
import constants.skills.SuperGM;
import constants.skills.ThunderBreaker;
import constants.skills.WhiteKnight;
import constants.skills.WindArcher;

/**
 * @author Matze
 * @author Frz
 * @author Ronan
 */
public class MapleStatEffect {

    private short watk, matk, wdef, mdef, acc, avoid, speed, jump;
    private short hp, mp;
    private double hpR, mpR;
    private short mhpRRate, mmpRRate;
    private byte mhpR, mmpR;
    private short mpCon, hpCon;
    private int duration;
    private boolean overTime, repeatEffect;
    private int sourceid;
    private int moveTo;
    private boolean skill;
    private List<Pair<MapleBuffStat, Integer>> statups;
    private Map<MonsterStatus, Integer> monsterStatus;
    private int x, y, mobCount, moneyCon, cooldown, morphId = 0, ghost, fatigue, berserk, booster;
    private double prop;
    private int itemCon, itemConNo;
    private int damage, attackCount, fixdamage;
    private Point lt, rb;
    private byte bulletCount, bulletConsume;
    private byte mapProtection;

    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime) {
        return loadFromData(source, skillid, true, overtime);
    }

    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid) {
        return loadFromData(source, itemid, false, false);
    }

    private static void addBuffStatPairToListIfNotZero(List<Pair<MapleBuffStat, Integer>> list, MapleBuffStat buffstat, Integer val) {
        if (val.intValue() != 0) {
            list.add(new Pair<>(buffstat, val));
        }
    }

    private static byte mapProtection(int sourceid) {
        if(sourceid == 2022001 || sourceid == 2022186) {
            return 1;   //elnath cold
        }
        
        else if(sourceid == 2022040) {
            return 2;   //aqua road underwater
        }
        
        else return 0;
    }
    
    private static MapleStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean overTime) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.duration = MapleDataTool.getIntConvert("time", source, -1);
        ret.hp = (short) MapleDataTool.getInt("hp", source, 0);
        ret.hpR = MapleDataTool.getInt("hpR", source, 0) / 100.0;
        ret.mp = (short) MapleDataTool.getInt("mp", source, 0);
        ret.mpR = MapleDataTool.getInt("mpR", source, 0) / 100.0;
        ret.mpCon = (short) MapleDataTool.getInt("mpCon", source, 0);
        ret.hpCon = (short) MapleDataTool.getInt("hpCon", source, 0);
        int iprop = MapleDataTool.getInt("prop", source, 100);
        ret.prop = iprop / 100.0;
        ret.mobCount = MapleDataTool.getInt("mobCount", source, 1);
        ret.cooldown = MapleDataTool.getInt("cooltime", source, 0);
        ret.morphId = MapleDataTool.getInt("morph", source, 0);
        ret.ghost = MapleDataTool.getInt("ghost", source, 0);
        ret.fatigue = MapleDataTool.getInt("incFatigue", source, 0);
        ret.repeatEffect = MapleDataTool.getInt("repeatEffect", source, 0) > 0;

        ret.sourceid = sourceid;
        ret.skill = skill;
        if (!ret.skill && ret.duration > -1) {
            ret.overTime = true;
        } else {
            ret.duration *= 1000; // items have their times stored in ms, of course
            ret.overTime = overTime;
        }
        
        ArrayList<Pair<MapleBuffStat, Integer>> statups = new ArrayList<>();
        ret.watk = (short) MapleDataTool.getInt("pad", source, 0);
        ret.wdef = (short) MapleDataTool.getInt("pdd", source, 0);
        ret.matk = (short) MapleDataTool.getInt("mad", source, 0);
        ret.mdef = (short) MapleDataTool.getInt("mdd", source, 0);
        ret.acc = (short) MapleDataTool.getIntConvert("acc", source, 0);
        ret.avoid = (short) MapleDataTool.getInt("eva", source, 0);
        
        ret.speed = (short) MapleDataTool.getInt("speed", source, 0);
        ret.jump = (short) MapleDataTool.getInt("jump", source, 0);
        
        ret.mapProtection = mapProtection(sourceid);
        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAP_PROTECTION, Integer.valueOf(ret.mapProtection));
                    
        if (ret.overTime && ret.getSummonMovementType() == null) {
            if(!skill) {
                if(isPyramidBuff(sourceid)) {
                    ret.berserk = MapleDataTool.getInt("berserk", source, 0);
                    ret.booster = MapleDataTool.getInt("booster", source, 0);
                    
                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.BERSERK, Integer.valueOf(ret.berserk));
                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.BOOSTER, Integer.valueOf(ret.booster));
                    
                } else if(isDojoBuff(sourceid) || isHpMpRecovery(sourceid)) {
                    ret.mhpR = (byte) MapleDataTool.getInt("mhpR", source, 0);
                    ret.mhpRRate = (short) (MapleDataTool.getInt("mhpRRate", source, 0) * 100);
                    ret.mmpR = (byte) MapleDataTool.getInt("mmpR", source, 0);
                    ret.mmpRRate = (short) (MapleDataTool.getInt("mmpRRate", source, 0) * 100);

                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.HPREC, Integer.valueOf(ret.mhpR));
                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MPREC, Integer.valueOf(ret.mmpR));
                    
                } else if(isRateCoupon(sourceid)) {
                    switch(MapleDataTool.getInt("expR", source, 0)) {
                        case 1:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_EXP1, 1);
                            break;

                        case 2:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_EXP2, 1);
                            break;

                        case 3:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_EXP3, 1);
                            break;

                        case 4:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_EXP4, 1);
                            break;
                    }

                    switch(MapleDataTool.getInt("drpR", source, 0)) {
                        case 1:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_DRP1, 1);
                            break;

                        case 2:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_DRP2, 1);
                            break;

                        case 3:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_DRP3, 1);
                            break;
                    }
                }
            } else {
                if(isMapChair(sourceid)) {
                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAP_CHAIR, 1);
                } else if((sourceid == Beginner.NIMBLE_FEET || sourceid == Noblesse.NIMBLE_FEET || sourceid == Evan.NIMBLE_FEET || sourceid == Legend.AGILE_BODY) && ServerConstants.USE_ULTRA_NIMBLE_FEET == true) {
                    ret.jump = (short)(ret.speed * 4);
                    ret.speed *= 15;
                }
            }
            
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
        }
        
        MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = (Point) ltd.getData();
            ret.rb = (Point) source.getChildByPath("rb").getData();
            
            if(ServerConstants.USE_MAXRANGE_ECHO_OF_HERO && (sourceid == Beginner.ECHO_OF_HERO || sourceid == Noblesse.ECHO_OF_HERO || sourceid == Legend.ECHO_OF_HERO || sourceid == Evan.ECHO_OF_HERO)) {
                ret.lt = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
                ret.rb = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        }
        
        int x = MapleDataTool.getInt("x", source, 0);
        
        if((sourceid == Beginner.RECOVERY || sourceid == Noblesse.RECOVERY || sourceid == Evan.RECOVERY || sourceid == Legend.RECOVERY) && ServerConstants.USE_ULTRA_RECOVERY == true) {
            x *= 10;
        }
        ret.x = x;
        ret.y = MapleDataTool.getInt("y", source, 0);
        
        ret.damage = MapleDataTool.getIntConvert("damage", source, 100);
        ret.fixdamage = MapleDataTool.getIntConvert("fixdamage", source, -1);
        ret.attackCount = MapleDataTool.getIntConvert("attackCount", source, 1);
        ret.bulletCount = (byte) MapleDataTool.getIntConvert("bulletCount", source, 1);
        ret.bulletConsume = (byte) MapleDataTool.getIntConvert("bulletConsume", source, 0);
        ret.moneyCon = MapleDataTool.getIntConvert("moneyCon", source, 0);
        ret.itemCon = MapleDataTool.getInt("itemCon", source, 0);
        ret.itemConNo = MapleDataTool.getInt("itemConNo", source, 0);
        ret.moveTo = MapleDataTool.getInt("moveTo", source, -1);
        Map<MonsterStatus, Integer> monsterStatus = new ArrayMap<>();
        if (skill) {
            switch (sourceid) {
                // BEGINNER
                case Beginner.RECOVERY:
                case Noblesse.RECOVERY:
                case Legend.RECOVERY:
                case Evan.RECOVERY:
                    statups.add(new Pair<>(MapleBuffStat.RECOVERY, Integer.valueOf(x)));
                    break;
                case Beginner.ECHO_OF_HERO:
                case Noblesse.ECHO_OF_HERO:
                case Legend.ECHO_OF_HERO:
                case Evan.ECHO_OF_HERO:
                    statups.add(new Pair<>(MapleBuffStat.ECHO_OF_HERO, Integer.valueOf(ret.x)));
                    break;
                case Beginner.MONSTER_RIDER:
                case Noblesse.MONSTER_RIDER:
                case Legend.MONSTER_RIDER:
                case Corsair.BATTLE_SHIP:
                case Beginner.SPACESHIP:
                case Noblesse.SPACESHIP:
                case Beginner.YETI_MOUNT1:
                case Beginner.YETI_MOUNT2:
                case Noblesse.YETI_MOUNT1:
                case Noblesse.YETI_MOUNT2:
                case Legend.YETI_MOUNT1:
                case Legend.YETI_MOUNT2:
                case Beginner.WITCH_BROOMSTICK:
                case Noblesse.WITCH_BROOMSTICK:
                case Legend.WITCH_BROOMSTICK:
                case Beginner.BALROG_MOUNT:
                case Noblesse.BALROG_MOUNT:
                case Legend.BALROG_MOUNT:
                    statups.add(new Pair<>(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(sourceid)));
                    break;
                case Beginner.INVINCIBLE_BARRIER:
                case Noblesse.INVINCIBLE_BARRIER:
                case Legend.INVICIBLE_BARRIER:
                case Evan.INVINCIBLE_BARRIER:
                    statups.add(new Pair<>(MapleBuffStat.DIVINE_BODY, Integer.valueOf(1)));
                    break;
                case Fighter.POWER_GUARD:
                case Page.POWER_GUARD:
                    statups.add(new Pair<>(MapleBuffStat.POWERGUARD, Integer.valueOf(x)));
                    break;
                case Spearman.HYPER_BODY:
                case GM.HYPER_BODY:
                case SuperGM.HYPER_BODY:
                    statups.add(new Pair<>(MapleBuffStat.HYPERBODYHP, Integer.valueOf(x)));
                    statups.add(new Pair<>(MapleBuffStat.HYPERBODYMP, Integer.valueOf(ret.y)));
                    break;
                case Crusader.COMBO:
                case DawnWarrior.COMBO:
                    statups.add(new Pair<>(MapleBuffStat.COMBO, Integer.valueOf(1)));
                    break;
                case WhiteKnight.BW_FIRE_CHARGE:
                case WhiteKnight.BW_ICE_CHARGE:
                case WhiteKnight.BW_LIT_CHARGE:
                case WhiteKnight.SWORD_FIRE_CHARGE:
                case WhiteKnight.SWORD_ICE_CHARGE:
                case WhiteKnight.SWORD_LIT_CHARGE:
                case Paladin.BW_HOLY_CHARGE:
                case Paladin.SWORD_HOLY_CHARGE:
                case DawnWarrior.SOUL_CHARGE:
                case ThunderBreaker.LIGHTNING_CHARGE:
                    statups.add(new Pair<>(MapleBuffStat.WK_CHARGE, Integer.valueOf(x)));
                    break;
                case DragonKnight.DRAGON_BLOOD:
                    statups.add(new Pair<>(MapleBuffStat.DRAGONBLOOD, Integer.valueOf(ret.x)));
                    break;
                case Hero.STANCE:
                case Paladin.STANCE:
                case DarkKnight.STANCE:
                case Aran.FREEZE_STANDING:
                    statups.add(new Pair<>(MapleBuffStat.STANCE, Integer.valueOf(iprop)));
                    break;
                case DawnWarrior.FINAL_ATTACK:
                case WindArcher.FINAL_ATTACK:
                    statups.add(new Pair<>(MapleBuffStat.FINALATTACK, Integer.valueOf(x)));
                    break;
                // MAGICIAN
                case Magician.MAGIC_GUARD:
                case BlazeWizard.MAGIC_GUARD:
                case Evan.MAGIC_GUARD:
                    statups.add(new Pair<>(MapleBuffStat.MAGIC_GUARD, Integer.valueOf(x)));
                    break;
                case Cleric.INVINCIBLE:
                    statups.add(new Pair<>(MapleBuffStat.INVINCIBLE, Integer.valueOf(x)));
                    break;
                case Priest.HOLY_SYMBOL:
                case SuperGM.HOLY_SYMBOL:
                    statups.add(new Pair<>(MapleBuffStat.HOLY_SYMBOL, Integer.valueOf(x)));
                    break;
                case FPArchMage.INFINITY:
                case ILArchMage.INFINITY:
                case Bishop.INFINITY:
                    statups.add(new Pair<>(MapleBuffStat.INFINITY, Integer.valueOf(x)));
                    break;
                case FPArchMage.MANA_REFLECTION:
                case ILArchMage.MANA_REFLECTION:
                case Bishop.MANA_REFLECTION:
                    statups.add(new Pair<>(MapleBuffStat.MANA_REFLECTION, Integer.valueOf(1)));
                    break;
                case Bishop.HOLY_SHIELD:
                    statups.add(new Pair<>(MapleBuffStat.HOLY_SHIELD, Integer.valueOf(x)));
                    break;
                case BlazeWizard.ELEMENTAL_RESET:
                case Evan.ELEMENTAL_RESET:
                	statups.add(new Pair<>(MapleBuffStat.ELEMENTAL_RESET, Integer.valueOf(x)));
                	break;
                case Evan.MAGIC_SHIELD:
                    statups.add(new Pair<>(MapleBuffStat.MAGIC_SHIELD, Integer.valueOf(x)));
                    break;
                case Evan.MAGIC_RESISTANCE:
                    statups.add(new Pair<>(MapleBuffStat.MAGIC_RESISTANCE, Integer.valueOf(x)));
                    break;
                case Evan.SLOW:
                	statups.add(new Pair<>(MapleBuffStat.SLOW, Integer.valueOf(x)));
                // BOWMAN
                case Priest.MYSTIC_DOOR:
                case Hunter.SOUL_ARROW:
                case Crossbowman.SOUL_ARROW:
                case WindArcher.SOUL_ARROW:
                    statups.add(new Pair<>(MapleBuffStat.SOULARROW, Integer.valueOf(x)));
                    break;
                case Ranger.PUPPET:
                case Sniper.PUPPET:
                case WindArcher.PUPPET:
                case Outlaw.OCTOPUS:
                case Corsair.WRATH_OF_THE_OCTOPI:
                    statups.add(new Pair<>(MapleBuffStat.PUPPET, Integer.valueOf(1)));
                    break;
                case Bowmaster.CONCENTRATE:
                    statups.add(new Pair<>(MapleBuffStat.CONCENTRATE, x));
                    break;
                case Bowmaster.HAMSTRING:
                    statups.add(new Pair<>(MapleBuffStat.HAMSTRING, Integer.valueOf(x)));
                    monsterStatus.put(MonsterStatus.SPEED, x);
                    break;
                case Marksman.BLIND:
                    statups.add(new Pair<>(MapleBuffStat.BLIND, Integer.valueOf(x)));
                    monsterStatus.put(MonsterStatus.ACC, x);
                    break;
                case Bowmaster.SHARP_EYES:
                case Marksman.SHARP_EYES:
                    statups.add(new Pair<>(MapleBuffStat.SHARP_EYES, Integer.valueOf(ret.x << 8 | ret.y)));
                    break;
                case WindArcher.WIND_WALK:
                    statups.add(new Pair<>(MapleBuffStat.WIND_WALK, Integer.valueOf(x)));
                    break;
                case Rogue.DARK_SIGHT:
                case NightWalker.DARK_SIGHT:
                    statups.add(new Pair<>(MapleBuffStat.DARKSIGHT, Integer.valueOf(x)));
                    break;
                case Hermit.MESO_UP:
                    statups.add(new Pair<>(MapleBuffStat.MESOUP, Integer.valueOf(x)));
                    break;
                case Hermit.SHADOW_PARTNER:
                case NightWalker.SHADOW_PARTNER:
                    statups.add(new Pair<>(MapleBuffStat.SHADOWPARTNER, Integer.valueOf(x)));
                    break;
                case ChiefBandit.MESO_GUARD:
                    statups.add(new Pair<>(MapleBuffStat.MESOGUARD, Integer.valueOf(x)));
                    break;
                case ChiefBandit.PICKPOCKET:
                    statups.add(new Pair<>(MapleBuffStat.PICKPOCKET, Integer.valueOf(x)));
                    break;                
                case NightLord.SHADOW_STARS:
                    statups.add(new Pair<>(MapleBuffStat.SHADOW_CLAW, Integer.valueOf(0)));
                    break;
                // PIRATE
                case Pirate.DASH:
                case ThunderBreaker.DASH:
                case Beginner.SPACE_DASH:
                case Noblesse.SPACE_DASH:
                    statups.add(new Pair<>(MapleBuffStat.DASH2, Integer.valueOf(ret.x)));
                    statups.add(new Pair<>(MapleBuffStat.DASH, Integer.valueOf(ret.y)));
                    break;
                case Corsair.SPEED_INFUSION:
                case Buccaneer.SPEED_INFUSION:
                case ThunderBreaker.SPEED_INFUSION:
                    statups.add(new Pair<>(MapleBuffStat.SPEED_INFUSION, Integer.valueOf(x)));
                    break;
                case Outlaw.HOMING_BEACON:
                case Corsair.BULLSEYE:
                    statups.add(new Pair<>(MapleBuffStat.HOMING_BEACON, Integer.valueOf(x)));
                    break;
                case ThunderBreaker.SPARK:
                    statups.add(new Pair<>(MapleBuffStat.SPARK, Integer.valueOf(x)));
                    break;
                // MULTIPLE
                case Aran.POLEARM_BOOSTER:
                case Fighter.AXE_BOOSTER:
                case Fighter.SWORD_BOOSTER:
                case Page.BW_BOOSTER:
                case Page.SWORD_BOOSTER:
                case Spearman.POLEARM_BOOSTER:
                case Spearman.SPEAR_BOOSTER:
                case Hunter.BOW_BOOSTER:
                case Crossbowman.CROSSBOW_BOOSTER:
                case Assassin.CLAW_BOOSTER:
                case Bandit.DAGGER_BOOSTER:
                case FPMage.SPELL_BOOSTER:
                case ILMage.SPELL_BOOSTER:
                case Brawler.KNUCKLER_BOOSTER:
                case Gunslinger.GUN_BOOSTER:
                case DawnWarrior.SWORD_BOOSTER:
                case BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case NightWalker.CLAW_BOOSTER:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case Evan.MAGIC_BOOSTER:
                case Beginner.POWER_EXPLOSION:
                case Noblesse.POWER_EXPLOSION:
                case Legend.POWER_EXPLOSION:
                    statups.add(new Pair<>(MapleBuffStat.BOOSTER, Integer.valueOf(x)));
                    break;
                case Hero.MAPLE_WARRIOR:
                case Paladin.MAPLE_WARRIOR:
                case DarkKnight.MAPLE_WARRIOR:
                case FPArchMage.MAPLE_WARRIOR:
                case ILArchMage.MAPLE_WARRIOR:
                case Bishop.MAPLE_WARRIOR:
                case Bowmaster.MAPLE_WARRIOR:
                case Marksman.MAPLE_WARRIOR:
                case NightLord.MAPLE_WARRIOR:
                case Shadower.MAPLE_WARRIOR:
                case Corsair.MAPLE_WARRIOR:
                case Buccaneer.MAPLE_WARRIOR:
                case Aran.MAPLE_WARRIOR:
                case Evan.MAPLE_WARRIOR:
                    statups.add(new Pair<>(MapleBuffStat.MAPLE_WARRIOR, Integer.valueOf(ret.x)));
                    break;
                // SUMMON
                case Ranger.SILVER_HAWK:
                case Sniper.GOLDEN_EAGLE:
                    statups.add(new Pair<>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case FPArchMage.ELQUINES:
                case Marksman.FROST_PREY:
                    statups.add(new Pair<>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case Priest.SUMMON_DRAGON:
                case Bowmaster.PHOENIX:
                case ILArchMage.IFRIT:
                case Bishop.BAHAMUT:
                case DarkKnight.BEHOLDER:
                case Outlaw.GAVIOTA:
                case DawnWarrior.SOUL:
                case BlazeWizard.FLAME:
                case WindArcher.STORM:
                case NightWalker.DARKNESS:
                case ThunderBreaker.LIGHTNING:
                case BlazeWizard.IFRIT:
                    statups.add(new Pair<>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    break;
                // ----------------------------- MONSTER STATUS ---------------------------------- //
                case Crusader.ARMOR_CRASH:
                case DragonKnight.POWER_CRASH:
                case WhiteKnight.MAGIC_CRASH:
                    monsterStatus.put(MonsterStatus.SEAL_SKILL, Integer.valueOf(1));
                    break;
                case Rogue.DISORDER:
                    monsterStatus.put(MonsterStatus.WATK, Integer.valueOf(ret.x));
                    monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(ret.y));
                    break;
                case Corsair.HYPNOTIZE:
                    monsterStatus.put(MonsterStatus.INERTMOB, 1);
                    break;
                case NightLord.NINJA_AMBUSH:
                case Shadower.NINJA_AMBUSH:
                    monsterStatus.put(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(ret.damage));
                    break;
                case Page.THREATEN:
                    monsterStatus.put(MonsterStatus.WATK, Integer.valueOf(ret.x));
                    monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(ret.y));
                    break;
                case DragonKnight.DRAGON_ROAR:
                    ret.hpR = -x / 100.0;
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case Crusader.AXE_COMA:
                case Crusader.SWORD_COMA:
                case Crusader.SHOUT:
                case WhiteKnight.CHARGE_BLOW:
                case Hunter.ARROW_BOMB:
                case ChiefBandit.ASSAULTER:
                case Shadower.BOOMERANG_STEP:
                case Brawler.BACK_SPIN_BLOW:
                case Brawler.DOUBLE_UPPERCUT:
                case Buccaneer.DEMOLITION:
                case Buccaneer.SNATCH:
                case Buccaneer.BARRAGE:
                case Gunslinger.BLANK_SHOT:
                case DawnWarrior.COMA:
                case Aran.ROLLING_SPIN:
                case Evan.FIRE_BREATH:
                case Evan.BLAZE:
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case NightLord.TAUNT:
                case Shadower.TAUNT:
                    monsterStatus.put(MonsterStatus.SHOWDOWN, ret.x);
                    monsterStatus.put(MonsterStatus.MDEF, ret.x);
                    monsterStatus.put(MonsterStatus.WDEF, ret.x);
                    break;
                case ILWizard.COLD_BEAM:
                case ILMage.ICE_STRIKE:
                case ILArchMage.BLIZZARD:
                case ILMage.ELEMENT_COMPOSITION:
                case Sniper.BLIZZARD:
                case Outlaw.ICE_SPLITTER:
                case FPArchMage.PARALYZE:
                case Aran.COMBO_TEMPEST:
                case Evan.ICE_BREATH:
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    ret.duration *= 2; // freezing skills are a little strange
                    break;
                case FPWizard.SLOW:
                case ILWizard.SLOW:
                case BlazeWizard.SLOW:
                    monsterStatus.put(MonsterStatus.SPEED, Integer.valueOf(ret.x));
                    break;    
                case FPWizard.POISON_BREATH:
                case FPMage.ELEMENT_COMPOSITION:
                    monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    break;
                case Priest.DOOM:
                    monsterStatus.put(MonsterStatus.DOOM, Integer.valueOf(1));
                    break;
                case ILMage.SEAL:
                case FPMage.SEAL:
                    monsterStatus.put(MonsterStatus.SEAL, Integer.valueOf(1));
                    break;
                case Hermit.SHADOW_WEB: // shadow web
                case NightWalker.SHADOW_WEB:
                    monsterStatus.put(MonsterStatus.SHADOW_WEB, Integer.valueOf(1));
                    break;
                case FPArchMage.FIRE_DEMON:
                case ILArchMage.ICE_DEMON:
                    monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case Evan.PHANTOM_IMPRINT:
                	monsterStatus.put(MonsterStatus.PHANTOM_IMPRINT, Integer.valueOf(x));
                //ARAN
                case Aran.COMBO_ABILITY:
                    statups.add(new Pair<>(MapleBuffStat.ARAN_COMBO, Integer.valueOf(100)));
                    break;
                case Aran.COMBO_BARRIER:
                    statups.add(new Pair<>(MapleBuffStat.COMBO_BARRIER, Integer.valueOf(ret.x)));
                    break;
                case Aran.COMBO_DRAIN:
                    statups.add(new Pair<>(MapleBuffStat.COMBO_DRAIN, Integer.valueOf(ret.x)));
                    break;
                case Aran.SMART_KNOCKBACK:
                    statups.add(new Pair<>(MapleBuffStat.SMART_KNOCKBACK, Integer.valueOf(ret.x)));
                    break;
                case Aran.BODY_PRESSURE:
                    statups.add(new Pair<>(MapleBuffStat.BODY_PRESSURE, Integer.valueOf(ret.x)));
                    break;
                case Aran.SNOW_CHARGE:
                    statups.add(new Pair<>(MapleBuffStat.WK_CHARGE, Integer.valueOf(ret.duration)));
                    break;
                default:
                    break;
            }
        }
        if (ret.isMorph()) {
            statups.add(new Pair<>(MapleBuffStat.MORPH, Integer.valueOf(ret.getMorph())));
        }
        if (ret.ghost > 0 && !skill) {
            statups.add(new Pair<>(MapleBuffStat.GHOST_MORPH, Integer.valueOf(ret.ghost)));
        }
        ret.monsterStatus = monsterStatus;
        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    /**
     * @param applyto
     * @param obj
     * @param attack damage done by the skill
     */
    public void applyPassive(MapleCharacter applyto, MapleMapObject obj, int attack) {
        if (makeChanceResult()) {
            switch (sourceid) { // MP eater
                case FPWizard.MP_EATER:
                case ILWizard.MP_EATER:
                case Cleric.MP_EATER:
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                    MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
                    if (!mob.isBoss()) {
                        int absorbMp = Math.min((int) (mob.getMaxMp() * (getX() / 100.0)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.addMP(absorbMp);
                            applyto.announce(MaplePacketCreator.showOwnBuffEffect(sourceid, 1));
                            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1), false);
                        }
                    }
                    break;
            }
        }
    }

    public boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, false, 1);
    }
    
    public boolean applyTo(MapleCharacter chr, boolean useMaxRange) {
        return applyTo(chr, chr, true, null, useMaxRange, 1);
    }

    public boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos, false, 1);
    }

    // primary: the player caster of the buff
    private boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, boolean useMaxRange, int affectedPlayers) {
        if (skill && (sourceid == GM.HIDE || sourceid == SuperGM.HIDE)) {
            applyto.toggleHide(false);
            return true;
        }
        
        if (primary && isHeal()) {
            affectedPlayers = applyBuff(applyfrom, useMaxRange);
        }
        
        int hpchange = calcHPChange(applyfrom, primary, affectedPlayers);
        int mpchange = calcMPChange(applyfrom, primary);
        if (primary) {
            if (itemConNo != 0) {
                if(!applyto.getClient().getAbstractPlayerInteraction().hasItem(itemCon, itemConNo)) {
                    applyto.announce(MaplePacketCreator.enableActions());
                    return false;
                }
                MapleInventoryManipulator.removeById(applyto.getClient(), ItemConstants.getInventoryType(itemCon), itemCon, itemConNo, false, true);
            }
        } else {
            if (isResurrection()) {
                hpchange = applyto.getCurrentMaxHp();
                applyto.setStance(0);
                
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.removePlayerFromMap(applyto.getId()), false);
                applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.spawnPlayerMapObject(applyto), false);
            }
        }
        
        if (isDispel() && makeChanceResult()) {
            applyto.dispelDebuffs();
        } else if (isCureAllAbnormalStatus()) {
            applyto.dispelDebuff(MapleDisease.SEDUCE);
            applyto.dispelDebuff(MapleDisease.ZOMBIFY);
            applyto.dispelDebuffs();
        } else if (isComboReset()) {
            applyto.setCombo((short) 0);
        }
        /*if (applyfrom.getMp() < getMpCon()) {
         AutobanFactory.MPCON.addPoint(applyfrom.getAutobanManager(), "mpCon hack for skill:" + sourceid + "; Player MP: " + applyto.getMp() + " MP Needed: " + getMpCon());
         } */
        
        if (!applyto.applyHpMpChange(hpCon, hpchange, mpchange)) {
            applyto.announce(MaplePacketCreator.enableActions());
            return false;
        }
        
        if (moveTo != -1) {
            if (moveTo != applyto.getMapId()) {
                MapleMap target;
                MaplePortal pt;
                
                if (moveTo == 999999999) {
                    if(sourceid != 2030100) {
                        target = applyto.getMap().getReturnMap();
                        pt = target.getRandomPlayerSpawnpoint();
                    } else {
                        if(!applyto.canRecoverLastBanish()) return false;
                    
                        Pair<Integer, Integer> lastBanishInfo = applyto.getLastBanishData();
                        target = applyto.getWarpMap(lastBanishInfo.getLeft());
                        pt = target.getPortal(lastBanishInfo.getRight());
                    }
                } else {
                    target = applyto.getClient().getWorldServer().getChannel(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
                    int targetid = target.getId() / 10000000;
                    if (targetid != 60 && applyto.getMapId() / 10000000 != 61 && targetid != applyto.getMapId() / 10000000 && targetid != 21 && targetid != 20 && targetid != 12 && (applyto.getMapId() / 10000000 != 10 && applyto.getMapId() / 10000000 != 12)) {
                    	return false;
                    }
                    
                    pt = target.getRandomPlayerSpawnpoint();
                }
                
                applyto.changeMap(target, pt);
            } else {
                return false;
            }
        }
        if (isShadowClaw()) {
            int projectile = 0;
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            for (int i = 1; i <= use.getSlotLimit(); i++) { // impose order...
                Item item = use.getItem((short) i);
                if (item != null) {
                    if (ItemConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200) {
                        projectile = item.getItemId();
                        break;
                    }
                }
            }
            if (projectile == 0) {
                return false;
            } else {
                MapleInventoryManipulator.removeById(applyto.getClient(), MapleInventoryType.USE, projectile, 200, false, true);
            }
        }
        SummonMovementType summonMovementType = getSummonMovementType();
        if (overTime || isCygnusFA() || summonMovementType != null) {
            if (summonMovementType != null && pos != null) {
                if(summonMovementType.getValue() == summonMovementType.STATIONARY.getValue()) {
                    applyto.cancelBuffStats(MapleBuffStat.PUPPET);
                } else {
                    applyto.cancelBuffStats(MapleBuffStat.SUMMON);
                }
                
                applyto.announce(MaplePacketCreator.enableActions());
            }
            
            applyBuffEffect(applyfrom, applyto, primary);
        }

        if (primary) {
            if (overTime) {
                applyBuff(applyfrom, useMaxRange);
            }

            if (isMonsterBuff()) {
                applyMonsterBuff(applyfrom);
            }
        }
        
        if (this.getFatigue() != 0) {
            applyto.getMount().setTiredness(applyto.getMount().getTiredness() + this.getFatigue());
        }

        if (summonMovementType != null && pos != null) {
            final MapleSummon tosummon = new MapleSummon(applyfrom, sourceid, pos, summonMovementType);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.addSummon(sourceid, tosummon);
            tosummon.addHP(x);
            if (isBeholder()) {
                tosummon.addHP(1);
            }
        }
        if (isMagicDoor() && !FieldLimit.DOOR.check(applyto.getMap().getFieldLimit())) { // Magic Door
            int y = applyto.getFh();
            if (y == 0) {
                    y = applyto.getPosition().y;
            }
            Point doorPosition = new Point(applyto.getPosition().x, y);
            MapleDoor door = new MapleDoor(applyto, doorPosition);
            
            if(door.getOwnerId() >= 0) {
                applyto.applyPartyDoor(door, false);

                door.getTarget().spawnDoor(door.getAreaDoor());
                door.getTown().spawnDoor(door.getTownDoor());
                
                applyto.disableDoorSpawn();
            } else {
                MapleInventoryManipulator.addFromDrop(applyto.getClient(), new Item(4006000, (short) 0, (short) 1), false);
                
                if(door.getOwnerId() == -3) applyto.dropMessage(5, "Mystic Door cannot be cast far from a spawn point. Nearest one is at " + door.getDoorStatus().getRight() + "pts " + door.getDoorStatus().getLeft());
                else if(door.getOwnerId() == -2) applyto.dropMessage(5, "Mystic Door cannot be cast on a slope, try elsewhere.");
                else applyto.dropMessage(5, "There are no door portals available for the town at this moment. Try again later.");
                
                applyto.cancelBuffStats(MapleBuffStat.SOULARROW);  // cancel door buff
            }
        }  else if (isMist()) {
            Rectangle bounds = calculateBoundingBox(sourceid == NightWalker.POISON_BOMB ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
            MapleMist mist = new MapleMist(bounds, applyfrom, this);
            applyfrom.getMap().spawnMist(mist, getDuration(), mist.isPoisonMist(), false, mist.isRecoveryMist());
        } else if(isTimeLeap()) {
            applyto.removeAllCooldownsExcept(Buccaneer.TIME_LEAP, true);
        } else if(isHyperBody() && !primary) {
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.removePlayerFromMap(applyto.getId()), false);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.spawnPlayerMapObject(applyto), false);
        }
        
        return true;
    }

    private int applyBuff(MapleCharacter applyfrom, boolean useMaxRange) {
        int affectedc = 1;
        
        if (isPartyBuff() && (applyfrom.getParty() != null || isGmBuff())) {
            Rectangle bounds = (!useMaxRange) ? calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft()) : new Rectangle(Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2, Integer.MAX_VALUE, Integer.MAX_VALUE);
            List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            List<MapleCharacter> affectedp = new ArrayList<>(affecteds.size());
            for (MapleMapObject affectedmo : affecteds) {
                MapleCharacter affected = (MapleCharacter) affectedmo;
                if (affected != applyfrom && (isGmBuff() || applyfrom.getParty().equals(affected.getParty()))) {
                    if (!isResurrection()) {
                        if (affected.isAlive()) {
                            affectedp.add(affected);
                        }
                    } else {
                        if (!affected.isAlive()) {
                            affectedp.add(affected);
                        }
                    }
                }
            }
            
            affectedc += affectedp.size();   // used for heal
            for (MapleCharacter affected : affectedp) {
                applyTo(applyfrom, affected, false, null, useMaxRange, affectedc);
                affected.announce(MaplePacketCreator.showOwnBuffEffect(sourceid, 2));
                affected.getMap().broadcastMessage(affected, MaplePacketCreator.showBuffeffect(affected.getId(), sourceid, 2), false);
            }
        }
        
        return affectedc;
    }

    private void applyMonsterBuff(MapleCharacter applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
        Skill skill_ = SkillFactory.getSkill(sourceid);
        int i = 0;
        for (MapleMapObject mo : affected) {
            MapleMonster monster = (MapleMonster) mo;
            if (isDispel()) { 
        		monster.debuffMob(skill_.getId());
            } else {
                    if (makeChanceResult()) {
                            monster.applyStatus(applyfrom, new MonsterStatusEffect(getMonsterStati(), skill_, null, false), isPoison(), getDuration());
                            if (isCrash()) {
                                    monster.debuffMob(skill_.getId());
                            }
                    }
            }
            i++;
            if (i >= mobCount) {
                break;
            }
        }
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(-lt.x + posFrom.x, rb.y + posFrom.y);
            mylt = new Point(-rb.x + posFrom.x, lt.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }
    
    public int getBuffLocalDuration() {
        return !ServerConstants.USE_BUFF_EVERLASTING ? duration : Integer.MAX_VALUE;
    }
    
    public void silentApplyBuff(MapleCharacter chr, long localStartTime) {
        int localDuration = getBuffLocalDuration();
        localDuration = alchemistModifyVal(chr, localDuration, false);
        //CancelEffectAction cancelAction = new CancelEffectAction(chr, this, starttime);
        //ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + localDuration) - Server.getInstance().getCurrentTime()));
        
        chr.registerEffect(this, localStartTime, localStartTime + localDuration, true);
        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            final MapleSummon tosummon = new MapleSummon(chr, sourceid, chr.getPosition(), summonMovementType);
            if (!tosummon.isStationary()) {
                chr.addSummon(sourceid, tosummon);
                tosummon.addHP(x);
            }
        }
        if (sourceid == Corsair.BATTLE_SHIP) {
            chr.announceBattleshipHp();
        }
    }

    public final void applyComboBuff(final MapleCharacter applyto, int combo) {
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ARAN_COMBO, combo));
        applyto.announce(MaplePacketCreator.giveBuff(sourceid, 99999, stat));

        final long starttime = Server.getInstance().getCurrentTime();
//	final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
//	final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + 99999) - Server.getInstance().getCurrentTime()));
        applyto.registerEffect(this, starttime, Long.MAX_VALUE, false);
    }
    
    public void updateBuffEffect(MapleCharacter target, List<Pair<MapleBuffStat, Integer>> activeStats, long starttime) {
        int localDuration = getBuffLocalDuration();
        localDuration = alchemistModifyVal(target, localDuration, false);
        
        long leftDuration = (starttime + localDuration) - Server.getInstance().getCurrentTime();
        if(leftDuration > 0) {
            target.announce(MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), (int)leftDuration, activeStats));
        }
    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary) {
        if (!isMonsterRiding() && !isCouponBuff() && !isMysticDoor()) {     // last mystic door already dispelled if it has been used before.
            applyto.cancelEffect(this, true, -1);
        }

        List<Pair<MapleBuffStat, Integer>> localstatups = statups;
        int localDuration = getBuffLocalDuration();
        int localsourceid = sourceid;
        int seconds = localDuration / 1000;
        MapleMount givemount = null;
        if (isMonsterRiding()) {
            int ridingMountId = 0;
            Item mount = applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
            if (mount != null) {
                ridingMountId = mount.getItemId();
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                ridingMountId = 1932000;
            } else if (sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP) {
                ridingMountId = 1932000 + applyto.getSkillLevel(sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1) {
                ridingMountId = 1932003;
            } else if (sourceid == Beginner.YETI_MOUNT2 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Legend.YETI_MOUNT2) {
                ridingMountId = 1932004;
            } else if (sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK) {
                ridingMountId = 1932005;
            } else if (sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT) {
                ridingMountId = 1932010;
            } else {
                if (applyto.getMount() == null) {
                    applyto.mount(ridingMountId, sourceid);
                }
                
                applyto.getClient().getWorldServer().registerMountHunger(applyto);
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                givemount = new MapleMount(applyto, 1932000, sourceid);
            } else if (sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP) {
                givemount = new MapleMount(applyto, 1932000 + applyto.getSkillLevel(sourceid), sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1) {
                givemount = new MapleMount(applyto, 1932003, sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT2 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Legend.YETI_MOUNT2) {
                givemount = new MapleMount(applyto, 1932004, sourceid);
            } else if (sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK) {
                givemount = new MapleMount(applyto, 1932005, sourceid);
            } else if (sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT) {
                givemount = new MapleMount(applyto, 1932010, sourceid);
            } else {
                givemount = applyto.getMount();
            }
            localDuration = sourceid;
            localsourceid = ridingMountId;
            localstatups = Collections.singletonList(new Pair<>(MapleBuffStat.MONSTER_RIDING, 0));
        } else if (isSkillMorph()) {
            for(int i = 0; i < localstatups.size(); i++) {
                if(localstatups.get(i).getLeft().equals(MapleBuffStat.MORPH)) {
                    localstatups.set(i, new Pair<>(MapleBuffStat.MORPH, getMorph(applyto)));
                    break;
                }
            }
        }
        if (primary) {
            localDuration = alchemistModifyVal(applyfrom, localDuration, false);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1, (byte) 3), false);
        }
        if (localstatups.size() > 0) {
            byte[] buff = null;
            byte[] mbuff = null;
            if (getSummonMovementType() == null) {
                buff = MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, localstatups);
            }
            if (isDash()) {
                buff = MaplePacketCreator.givePirateBuff(statups, sourceid, seconds);
                mbuff = MaplePacketCreator.giveForeignPirateBuff(applyto.getId(), sourceid, seconds, localstatups);
            } else if (isInfusion()) {
            	buff = MaplePacketCreator.givePirateBuff(localstatups, sourceid, seconds);
                mbuff = MaplePacketCreator.giveForeignPirateBuff(applyto.getId(), sourceid, seconds, localstatups);
            } else if (isDs()) {
                List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT, 0));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), dsstat);
            } else if (isWw()) {
                List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.WIND_WALK, 0));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), dsstat);
            } else if (isCombo()) {
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), statups);
            } else if (isMonsterRiding()) {
                if (sourceid == Corsair.BATTLE_SHIP) {//hp
                    if (applyto.getBattleshipHp() <= 0) {
                        applyto.resetBattleshipHp();
                    }
                    
                    localstatups = statups;
                }
                
                buff = MaplePacketCreator.giveBuff(localsourceid, localDuration, localstatups);
                mbuff = MaplePacketCreator.showMonsterRiding(applyto.getId(), givemount);
                localDuration = duration;
            } else if (isShadowPartner()) {
                List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.SHADOWPARTNER, 0));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), stat);
            } else if (isSoulArrow()) {
                List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.SOULARROW, 0));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), stat);
            } else if (isEnrage()) {
                applyto.handleOrbconsume();
            } else if (isMorph()) {
                List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.MORPH, Integer.valueOf(getMorph(applyto))));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), stat);
            }
            
            if (buff != null) {
            	if (!hasNoIcon()) { //Thanks flav for such a simple release! :)
                    applyto.announce(buff);
            	} else {
                    System.out.println("<Error> NO buff icon for id " + sourceid);
                }
            }
            
            long starttime = Server.getInstance().getCurrentTime();
            //CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
            //ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, localDuration);
            applyto.registerEffect(this, starttime, starttime + localDuration, false);

            if (mbuff != null) {
                applyto.getMap().broadcastMessage(applyto, mbuff, false);
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                applyto.announceBattleshipHp();
            }
        }
    }

    private int calcHPChange(MapleCharacter applyfrom, boolean primary, int affectedPlayers) {
        int hpchange = 0;
        if (hp != 0) {
            if (!skill) {
                if (primary) {
                    hpchange += alchemistModifyVal(applyfrom, hp, true);
                } else {
                    hpchange += hp;
                }
                if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
                    hpchange /= 2;
                }
            } else { // assumption: this is heal
                float hpHeal = (applyfrom.getCurrentMaxHp() * (float) hp / (100.0f * affectedPlayers));
                hpchange += hpHeal;
                if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
                    hpchange = -hpchange;
                    hpCon = 0;
                }
            }
        }
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getCurrentMaxHp() * hpR) / (applyfrom.hasDisease(MapleDisease.ZOMBIFY) ? 2 : 1);
        }
        if (primary) {
            if (hpCon != 0) {
                hpchange -= hpCon;
            }
        }
        if (isChakra()) {
            hpchange += makeHealHP(getY() / 100.0, applyfrom.getTotalLuk(), 2.3, 3.5);
        } else if (sourceid == SuperGM.HEAL_PLUS_DISPEL) {
            hpchange += applyfrom.getCurrentMaxHp();
        }

        return hpchange;
    }

    private int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(MapleCharacter applyfrom, boolean primary) {
        int mpchange = 0;
        if (mp != 0) {
            if (primary) {
                mpchange += alchemistModifyVal(applyfrom, mp, true);
            } else {
                mpchange += mp;
            }
        }
        if (mpR != 0) {
            mpchange += (int) (applyfrom.getCurrentMaxMp() * mpR);
        }
        if (primary) {
            if (mpCon != 0) {
                double mod = 1.0;
                boolean isAFpMage = applyfrom.getJob().isA(MapleJob.FP_MAGE);
                boolean isCygnus = applyfrom.getJob().isA(MapleJob.BLAZEWIZARD2);
                boolean isEvan = applyfrom.getJob().isA(MapleJob.EVAN7);
                if (isAFpMage || isCygnus || isEvan || applyfrom.getJob().isA(MapleJob.IL_MAGE)) {
                    Skill amp = isAFpMage ? SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION) : (isCygnus ? SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION) : (isEvan ? SkillFactory.getSkill(Evan.MAGIC_AMPLIFICATION) : SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION)));
                    int ampLevel = applyfrom.getSkillLevel(amp);
                    if (ampLevel > 0) {
                        mod = amp.getEffect(ampLevel).getX() / 100.0;
                    }
                }
                mpchange -= mpCon * mod;
                if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                    mpchange = 0;
                } else if (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE) != null) {
                    mpchange -= (int) (mpchange * (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE).doubleValue() / 100));
                }
            }
        }
        if (sourceid == SuperGM.HEAL_PLUS_DISPEL) {
            mpchange += applyfrom.getCurrentMaxMp();
        }

        return mpchange;
    }

    private int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        if (!skill && (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.NIGHTWALKER3))) {
            MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
            if (alchemistEffect != null) {
                return (int) (val * ((withX ? alchemistEffect.getX() : alchemistEffect.getY()) / 100.0));
            }
        }
        return val;
    }

    private MapleStatEffect getAlchemistEffect(MapleCharacter chr) {
        int id = Hermit.ALCHEMIST;
        if (chr.isCygnus()) {
            id = NightWalker.ALCHEMIST;
        }
        int alchemistLevel = chr.getSkillLevel(SkillFactory.getSkill(id));
        return alchemistLevel == 0 ? null : SkillFactory.getSkill(id).getEffect(alchemistLevel);
    }

    private boolean isGmBuff() {
        switch (sourceid) {
            case Beginner.ECHO_OF_HERO:
            case Noblesse.ECHO_OF_HERO:
            case Legend.ECHO_OF_HERO:
            case Evan.ECHO_OF_HERO:	
            case SuperGM.HEAL_PLUS_DISPEL:
            case SuperGM.HASTE:
            case SuperGM.HOLY_SYMBOL:
            case SuperGM.BLESS:
            case SuperGM.RESURRECTION:
            case SuperGM.HYPER_BODY:
                return true;
            default:
                return false;
        }
    }

    private boolean isMonsterBuff() {
        if (!skill) {
            return false;
        }
        switch (sourceid) {
            case Page.THREATEN:
            case FPWizard.SLOW:
            case ILWizard.SLOW:
            case FPMage.SEAL:
            case ILMage.SEAL:
            case Priest.DOOM:
            case Hermit.SHADOW_WEB:
            case NightLord.NINJA_AMBUSH:
            case Shadower.NINJA_AMBUSH:
            case BlazeWizard.SLOW:
            case BlazeWizard.SEAL:
            case NightWalker.SHADOW_WEB:
            case Crusader.ARMOR_CRASH:
            case DragonKnight.POWER_CRASH:
            case WhiteKnight.MAGIC_CRASH:
            case Priest.DISPEL:
            case SuperGM.HEAL_PLUS_DISPEL:
                return true;
        }
        return false;
    }

    private boolean isPartyBuff() {
        if (lt == null || rb == null) {
            return false;
        }
        if ((sourceid >= 1211003 && sourceid <= 1211008) || sourceid == Paladin.SWORD_HOLY_CHARGE || sourceid == Paladin.BW_HOLY_CHARGE || sourceid == DawnWarrior.SOUL_CHARGE) {// wk charges have lt and rb set but are neither player nor monster buffs
            return false;
        }
        return true;
    }

    private boolean isHeal() {
        return sourceid == Cleric.HEAL || sourceid == SuperGM.HEAL_PLUS_DISPEL;
    }

    private boolean isResurrection() {
        return sourceid == Bishop.RESURRECTION || sourceid == GM.RESURRECTION || sourceid == SuperGM.RESURRECTION;
    }

    private boolean isTimeLeap() {
        return sourceid == Buccaneer.TIME_LEAP;
    }

    public boolean isDragonBlood() {
        return skill && sourceid == DragonKnight.DRAGON_BLOOD;
    }

    public boolean isBerserk() {
        return skill && sourceid == DarkKnight.BERSERK;
    }

    public boolean isRecovery() {
        return sourceid == Beginner.RECOVERY || sourceid == Noblesse.RECOVERY || sourceid == Legend.RECOVERY || sourceid == Evan.RECOVERY;
    }
    
    public boolean isMapChair() {
        return sourceid == Beginner.MAP_CHAIR || sourceid == Noblesse.MAP_CHAIR || sourceid == Legend.MAP_CHAIR;
    }
    
    public static boolean isMapChair(int sourceid) {
        return sourceid == Beginner.MAP_CHAIR || sourceid == Noblesse.MAP_CHAIR || sourceid == Legend.MAP_CHAIR;
    }
    
    
    public boolean isDojoBuff() {
        return sourceid >= 2022359 && sourceid <= 2022421;
    }
    
    public static boolean isDojoBuff(int sourceid) {
        return sourceid >= 2022359 && sourceid <= 2022421;
    }
    
    public static boolean isHpMpRecovery(int sourceid) {
        return sourceid == 2022198 || sourceid == 2022337;
    }
    
    public static boolean isPyramidBuff(int sourceid) {
        return sourceid >= 2022585 && sourceid <= 2022617;
    }
    
    public static boolean isRateCoupon(int sourceid) {
        int itemType = sourceid / 1000;
        return itemType == 5211 || itemType == 5360;
    }
    
    private boolean isDs() {
        return skill && (sourceid == Rogue.DARK_SIGHT || sourceid == NightWalker.DARK_SIGHT);
    }
    
    private boolean isWw() {
        return skill && (sourceid == WindArcher.WIND_WALK);
    }

    private boolean isCombo() {
        return skill && (sourceid == Crusader.COMBO || sourceid == DawnWarrior.COMBO);
    }

    private boolean isEnrage() {
        return skill && sourceid == Hero.ENRAGE;
    }

    public boolean isBeholder() {
        return skill && sourceid == DarkKnight.BEHOLDER;
    }

    private boolean isShadowPartner() {
        return skill && (sourceid == Hermit.SHADOW_PARTNER || sourceid == NightWalker.SHADOW_PARTNER);
    }

    private boolean isChakra() {
        return skill && sourceid == ChiefBandit.CHAKRA;
    }

    private boolean isCouponBuff() {
        return isRateCoupon(sourceid);
    }
    
    private boolean isMysticDoor() {
        return skill && sourceid == Priest.MYSTIC_DOOR;
    }
    
    public boolean isMonsterRiding() {
        return skill && (sourceid % 10000000 == 1004 || sourceid == Corsair.BATTLE_SHIP || sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP
                || sourceid == Beginner.YETI_MOUNT1 || sourceid == Beginner.YETI_MOUNT2 || sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Beginner.BALROG_MOUNT
                || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Noblesse.BALROG_MOUNT
                || sourceid == Legend.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT2 || sourceid == Legend.WITCH_BROOMSTICK || sourceid == Legend.BALROG_MOUNT);
    }

    public boolean isMagicDoor() {
        return skill && sourceid == Priest.MYSTIC_DOOR;
    }

    public boolean isPoison() {
        return skill && (sourceid == FPMage.POISON_MIST || sourceid == FPWizard.POISON_BREATH || sourceid == FPMage.ELEMENT_COMPOSITION || sourceid == NightWalker.POISON_BOMB || sourceid == BlazeWizard.FLAME_GEAR);
    }
    
    public boolean isMorph() {
        return morphId > 0;
    }
    
    public boolean isMorphWithoutAttack() {
        return morphId > 0 && morphId < 100; // Every morph item I have found has been under 100, pirate skill transforms start at 1000.
    }

    private boolean isMist() {
        return skill && (sourceid == FPMage.POISON_MIST || sourceid == Shadower.SMOKE_SCREEN || sourceid == BlazeWizard.FLAME_GEAR || sourceid == NightWalker.POISON_BOMB || sourceid == Evan.RECOVERY_AURA);
    }

    private boolean isSoulArrow() {
        return skill && (sourceid == Hunter.SOUL_ARROW || sourceid == Crossbowman.SOUL_ARROW || sourceid == WindArcher.SOUL_ARROW);
    }

    private boolean isShadowClaw() {
        return skill && sourceid == NightLord.SHADOW_STARS;
    }

    private boolean isCrash() {
    	return skill && (sourceid == DragonKnight.POWER_CRASH || sourceid == Crusader.ARMOR_CRASH || sourceid == WhiteKnight.MAGIC_CRASH);
    }
    
    private boolean isDispel() {
        return skill && (sourceid == Priest.DISPEL || sourceid == SuperGM.HEAL_PLUS_DISPEL);
    }

    private boolean isCureAllAbnormalStatus() {
        if (skill) {
            return isHerosWill(sourceid);
        } else if (sourceid == 2022544) return true;
        
        return false;
    }
    
    public static boolean isHerosWill(int skillid) {
        switch(skillid) {
            case Hero.HEROS_WILL:
            case Paladin.HEROS_WILL:
            case DarkKnight.HEROS_WILL:
            case FPArchMage.HEROS_WILL:
            case ILArchMage.HEROS_WILL:
            case Bishop.HEROS_WILL:
            case Bowmaster.HEROS_WILL:
            case Marksman.HEROS_WILL:
            case NightLord.HEROS_WILL:
            case Shadower.HEROS_WILL:
            case Buccaneer.PIRATES_RAGE:
            case Aran.HEROS_WILL:
                return true;

            default:
                return false;
        }
    }

    private boolean isDash() {
        return skill && (sourceid == Pirate.DASH || sourceid == ThunderBreaker.DASH || sourceid == Beginner.SPACE_DASH || sourceid == Noblesse.SPACE_DASH);
    }

    private boolean isSkillMorph() {
        return skill && (sourceid == Buccaneer.SUPER_TRANSFORMATION || sourceid == Marauder.TRANSFORMATION || sourceid == WindArcher.EAGLE_EYE || sourceid == ThunderBreaker.TRANSFORMATION);
    }

    private boolean isInfusion() {
        return skill && (sourceid == Buccaneer.SPEED_INFUSION || sourceid == Corsair.SPEED_INFUSION || sourceid == ThunderBreaker.SPEED_INFUSION);
    }

    private boolean isCygnusFA() {
        return skill && (sourceid == DawnWarrior.FINAL_ATTACK || sourceid == WindArcher.FINAL_ATTACK);
    }
    
    private boolean isHyperBody() {
        return skill && (sourceid == Spearman.HYPER_BODY || sourceid == GM.HYPER_BODY || sourceid == SuperGM.HYPER_BODY);
    }

    private boolean isComboReset() {
        return sourceid == Aran.COMBO_BARRIER || sourceid == Aran.COMBO_DRAIN;
    }

    private int getFatigue() {
        return fatigue;
    }

    private int getMorph() {
        return morphId;
    }

    private int getMorph(MapleCharacter chr) {
        if (morphId % 10 == 0) {
            return morphId + chr.getGender();
        }
        return morphId + 100 * chr.getGender();
    }

    private SummonMovementType getSummonMovementType() {
        if (!skill) {
            return null;
        }
        switch (sourceid) {
            case Ranger.PUPPET:
            case Sniper.PUPPET:
            case WindArcher.PUPPET:
            case Outlaw.OCTOPUS:
            case Corsair.WRATH_OF_THE_OCTOPI:
                return SummonMovementType.STATIONARY;
            case Ranger.SILVER_HAWK:
            case Sniper.GOLDEN_EAGLE:
            case Priest.SUMMON_DRAGON:
            case Marksman.FROST_PREY:
            case Bowmaster.PHOENIX:
            case Outlaw.GAVIOTA:
                return SummonMovementType.CIRCLE_FOLLOW;
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Bishop.BAHAMUT:
            case DawnWarrior.SOUL:
            case BlazeWizard.FLAME:
            case BlazeWizard.IFRIT:
            case WindArcher.STORM:
            case NightWalker.DARKNESS:
            case ThunderBreaker.LIGHTNING:
                return SummonMovementType.FOLLOW;
        }
        return null;
    }

    
    public boolean hasNoIcon() {
        return (sourceid == 3111002 || sourceid == 3211002 || + // puppet, puppet
                sourceid == 3211005 || +                        // golden eagle
                sourceid == 2121005 || sourceid == 2221005 || + // elquines, ifrit
                sourceid == 2321003 || sourceid == 3121006 || + // bahamut, phoenix
                sourceid == 3221005 || sourceid == 3111005 || + // frostprey, silver hawk
                sourceid == 2311006 || sourceid == 5220002 || + // summon dragon, wrath of the octopi
                sourceid == 5211001 || sourceid == 5211002); // octopus, gaviota
    }
    
    public boolean isSkill() {
        return skill;
    }

    public int getSourceId() {
        return sourceid;
    }
    
    public int getBuffSourceId() {
        return skill ? sourceid : -sourceid;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    /*
    private static class CancelEffectAction implements Runnable {

        private MapleStatEffect effect;
        private WeakReference<MapleCharacter> target;
        private long startTime;

        public CancelEffectAction(MapleCharacter target, MapleStatEffect effect, long startTime) {
            this.effect = effect;
            this.target = new WeakReference<>(target);
            this.startTime = startTime;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.cancelEffect(effect, false, startTime);
            }
        }
    }
    */

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }
    
    public double getHpRate() {
        return hpR;
    }

    public double getMpRate() {
        return mpR;
    }
    
    public byte getHpR() {
        return mhpR;
    }

    public byte getMpR() {
        return mmpR;
    }
    
    public short getHpRRate() {
        return mhpRRate;
    }

    public short getMpRRate() {
        return mmpRRate;
    }

    public short getHpCon() {
        return hpCon;
    }

    public short getMpCon() {
        return mpCon;
    }

    public short getMatk() {
        return matk;
    }
    
    public short getWatk() {
        return watk;
    }

    public int getDuration() {
        return duration;
    }

    public List<Pair<MapleBuffStat, Integer>> getStatups() {
        return statups;
    }

    public boolean sameSource(MapleStatEffect effect) {
        return this.sourceid == effect.sourceid && this.skill == effect.skill;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getMobCount() {
        return mobCount;
    }

    public int getFixDamage() {
        return fixdamage;
    }

    public byte getBulletCount() {
        return bulletCount;
    }

    public byte getBulletConsume() {
        return bulletConsume;
    }

    public int getMoneyCon() {
        return moneyCon;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Map<MonsterStatus, Integer> getMonsterStati() {
        return monsterStatus;
    }
}