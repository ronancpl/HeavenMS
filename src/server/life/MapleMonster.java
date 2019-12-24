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
package server.life;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamilyEntry;
import client.MapleJob;
import client.Skill;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import config.YamlConfig;
import constants.skills.Crusader;
import constants.skills.FPMage;
import constants.skills.Hermit;
import constants.skills.ILMage;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Priest;
import constants.skills.Shadower;
import constants.skills.WhiteKnight;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.channel.Channel;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import server.TimerManager;
import server.life.MapleLifeFactory.BanishInfo;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import tools.IntervalBuilder;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.server.services.type.ChannelServices;
import net.server.services.task.channel.MobAnimationService;
import net.server.services.task.channel.MobClearSkillService;
import net.server.services.task.channel.MobStatusService;
import net.server.services.task.channel.OverallService;
import net.server.coordinator.world.MapleMonsterAggroCoordinator;
import server.MapleStatEffect;
import server.loot.MapleLootManager;
import server.maps.MapleSummon;

public class MapleMonster extends AbstractLoadedMapleLife {
    
    private ChangeableStats ostats = null;  //unused, v83 WZs offers no support for changeable stats.
    private MapleMonsterStats stats;
    private AtomicInteger hp = new AtomicInteger(1);
    private AtomicLong maxHpPlusHeal = new AtomicLong(1);
    private int mp;
    private WeakReference<MapleCharacter> controller = new WeakReference<>(null);
    private boolean controllerHasAggro, controllerKnowsAboutAggro, controllerHasPuppet;
    private Collection<MonsterListener> listeners = new LinkedList<>();
    private EnumMap<MonsterStatus, MonsterStatusEffect> stati = new EnumMap<>(MonsterStatus.class);
    private ArrayList<MonsterStatus> alreadyBuffed = new ArrayList<>();
    private MapleMap map;
    private int VenomMultiplier = 0;
    private boolean fake = false;
    private boolean dropsDisabled = false;
    private List<Pair<Integer, Integer>> usedSkills = new ArrayList<>();
    private Map<Pair<Integer, Integer>, Integer> skillsUsed = new HashMap<>();
    private Set<Integer> usedAttacks = new HashSet<>();
    private Set<Integer> calledMobOids = null;
    private WeakReference<MapleMonster> callerMob = new WeakReference<>(null);
    private List<Integer> stolenItems = new ArrayList<>(5);
    private int team;
    private int parentMobOid = 0;
    private int spawnEffect = 0;
    private final HashMap<Integer, AtomicLong> takenDamage = new HashMap<>();
    private ScheduledFuture<?> monsterItemDrop = null;
    private Runnable removeAfterAction = null;
    private boolean availablePuppetUpdate = true;

    private MonitoredReentrantLock externalLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_EXT);
    private MonitoredReentrantLock monsterLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB, true);
    private MonitoredReentrantLock statiLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_STATI);
    private MonitoredReentrantLock animationLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_ANI);
    private MonitoredReentrantLock aggroUpdateLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_AGGRO);

    public MapleMonster(int id, MapleMonsterStats stats) {
        super(id);
        initWithStats(stats);
    }

    public MapleMonster(MapleMonster monster) {
        super(monster);
        initWithStats(monster.stats);
    }
    
    public void lockMonster() {
        externalLock.lock();
    }
    
    public void unlockMonster() {
        externalLock.unlock();
    }

    private void initWithStats(MapleMonsterStats baseStats) {
        setStance(5);
        this.stats = baseStats.copy();
        hp.set(stats.getHp());
        mp = stats.getMp();
        
        maxHpPlusHeal.set(hp.get());
    }
    
    public void setSpawnEffect(int effect) {
        spawnEffect = effect;
    }
    
    public int getSpawnEffect() {
        return spawnEffect;
    }

    public void disableDrops() {
        this.dropsDisabled = true;
    }

    public void enableDrops() {
        this.dropsDisabled = false;
    }
    
    public boolean dropsDisabled() {
        return dropsDisabled;
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }
    
    public int getParentMobOid() {
        return parentMobOid;
    }

    public void setParentMobOid(int parentMobId) {
        this.parentMobOid = parentMobId;
    }
    
    public int countAvailableMobSummons(int summonsSize, int skillLimit) {    // limit prop for summons has another conotation, found thanks to MedicOP
        int summonsCount;
        
        Set<Integer> calledOids = this.calledMobOids;
        if(calledOids != null) {
            summonsCount = calledOids.size();
        } else {
            summonsCount = 0;
        }
        
        return Math.min(summonsSize, skillLimit - summonsCount);
    }
    
    public void addSummonedMob(MapleMonster mob) {
        Set<Integer> calledOids = this.calledMobOids;
        if (calledOids == null) {
            calledOids = Collections.synchronizedSet(new HashSet<Integer>());
            this.calledMobOids = calledOids;
        }
        
        calledOids.add(mob.getObjectId());
        mob.setSummonerMob(this);
    }
    
    private void removeSummonedMob(int mobOid) {
        Set<Integer> calledOids = this.calledMobOids;
        if (calledOids != null) {
            calledOids.remove(mobOid);
        }
    }
    
    private void setSummonerMob(MapleMonster mob) {
        this.callerMob = new WeakReference<>(mob);
    }
    
    private void dispatchClearSummons() {
        MapleMonster caller = this.callerMob.get();
        if (caller != null) {
            caller.removeSummonedMob(this.getObjectId());
        }
        
        this.calledMobOids = null;
    }
    
    public void pushRemoveAfterAction(Runnable run) {
        this.removeAfterAction = run;
    }
    
    public Runnable popRemoveAfterAction() {
        Runnable r = this.removeAfterAction;
        this.removeAfterAction = null;
        
        return r;
    }
    
    public int getHp() {
        return hp.get();
    }
    
    public synchronized void addHp(int hp) {
        if (this.hp.get() <= 0) {
            return;
        }
        this.hp.addAndGet(hp);
    }
    
    public synchronized void setStartingHp(int hp) {
        stats.setHp(hp);    // refactored mob stats after non-static HP pool suggestion thanks to twigs
        this.hp.set(hp);
    }

    public int getMaxHp() {
        return stats.getHp();
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public int getMaxMp() {
        return stats.getMp();
    }

    public int getExp() {
        return stats.getExp();
    }

    public int getLevel() {
        return stats.getLevel();
    }

    public int getCP() {
        return stats.getCP();
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getVenomMulti() {
        return this.VenomMultiplier;
    }

    public void setVenomMulti(int multiplier) {
        this.VenomMultiplier = multiplier;
    }

    public MapleMonsterStats getStats() {
        return stats;
    }

    public boolean isBoss() {
        return stats.isBoss();
    }

    public int getAnimationTime(String name) {
        return stats.getAnimationTime(name);
    }

    private List<Integer> getRevives() {
        return stats.getRevives();
    }

    private byte getTagColor() {
        return stats.getTagColor();
    }

    private byte getTagBgColor() {
        return stats.getTagBgColor();
    }

    public void setHpZero() {     // force HP = 0
        applyAndGetHpDamage(Integer.MAX_VALUE, false);
    }
    
    private boolean applyAnimationIfRoaming(int attackPos, MobSkill skill) {   // roam: not casting attack or skill animations
        if (!animationLock.tryLock()) {
            return false;
        }
    
        try {
            long animationTime;
        
            if(skill == null) {
                animationTime = MapleMonsterInformationProvider.getInstance().getMobAttackAnimationTime(this.getId(), attackPos);
            } else {
                animationTime = MapleMonsterInformationProvider.getInstance().getMobSkillAnimationTime(skill);
            }

            if(animationTime > 0) {
                MobAnimationService service = (MobAnimationService) map.getChannelServer().getServiceAccess(ChannelServices.MOB_ANIMATION);
                return service.registerMobOnAnimationEffect(map.getId(), this.hashCode(), animationTime);
            } else {
                return true;
            }
        } finally {
            animationLock.unlock();
        }
    }
    
    public synchronized Integer applyAndGetHpDamage(int delta, boolean stayAlive) {
        int curHp = hp.get();
        if (curHp <= 0) {       // this monster is already dead
            return null;
        }
        
        if(delta >= 0) {
            if (stayAlive) {
                curHp--;
            }
            int trueDamage = Math.min(curHp, delta);
            
            hp.addAndGet(-trueDamage);
            return trueDamage;
        } else {
            int trueHeal = -delta;
            int hp2Heal = curHp + trueHeal;
            int maxHp = getMaxHp();
            
            if (hp2Heal > maxHp) {
                trueHeal -= (hp2Heal - maxHp);
            }
            
            hp.addAndGet(trueHeal);
            return trueHeal;
        }
    }
    
    public synchronized void disposeMapObject() {     // mob is no longer associated with the map it was in
        hp.set(-1);
    }
    
    public void broadcastMobHpBar(MapleCharacter from) {
        if (hasBossHPBar()) {
            from.setPlayerAggro(this.hashCode());
            from.getMap().broadcastBossHpMessage(this, this.hashCode(), makeBossHPBarPacket(), getPosition());
        } else if (!isBoss()) {
            int remainingHP = (int) Math.max(1, hp.get() * 100f / getMaxHp());
            byte[] packet = MaplePacketCreator.showMonsterHP(getObjectId(), remainingHP);
            if (from.getParty() != null) {
                for (MaplePartyCharacter mpc : from.getParty().getMembers()) {
                    MapleCharacter member = from.getMap().getCharacterById(mpc.getId()); // god bless
                    if (member != null) {
                        member.announce(packet.clone()); // clone it just in case of crypto
                    }
                }
            } else {
                from.announce(packet);
            }
        }
    }
    
    public boolean damage(MapleCharacter attacker, int damage, boolean stayAlive) {
        boolean lastHit = false;
        
        this.lockMonster();
        try {
            if (!this.isAlive()) {
                return false;
            }

            /* pyramid not implemented
            Pair<Integer, Integer> cool = this.getStats().getCool();
            if (cool != null) {
                Pyramid pq = (Pyramid) chr.getPartyQuest();
                if (pq != null) {
                    if (damage > 0) {
                        if (damage >= cool.getLeft()) {
                            if ((Math.random() * 100) < cool.getRight()) {
                                pq.cool();
                            } else {
                                pq.kill();
                            }
                        } else {
                            pq.kill();
                        }
                    } else {
                        pq.miss();
                    }
                    killed = true;
                }
            }
            */

            if (damage > 0) {
                this.applyDamage(attacker, damage, stayAlive, false);
                if (!this.isAlive()) {  // monster just died
                    lastHit = true;
                }
            }
        } finally {
            this.unlockMonster();
        }
        
        return lastHit;
    }
    
    /**
     *
     * @param from the player that dealt the damage
     * @param damage
     * @param stayAlive
     */
    private void applyDamage(MapleCharacter from, int damage, boolean stayAlive, boolean fake) {
        Integer trueDamage = applyAndGetHpDamage(damage, stayAlive);
        if (trueDamage == null) {
            return;
        }
        
        if (YamlConfig.config.server.USE_DEBUG) {
            from.dropMessage(5, "Hitted MOB " + this.getId() + ", OID " + this.getObjectId());
        }
        
        if (!fake) {
            dispatchMonsterDamaged(from, trueDamage);
        }
        
        if (!takenDamage.containsKey(from.getId())) {
            takenDamage.put(from.getId(), new AtomicLong(trueDamage));
        } else {
            takenDamage.get(from.getId()).addAndGet(trueDamage);
        }

        broadcastMobHpBar(from);
    }
    
    public void applyFakeDamage(MapleCharacter from, int damage, boolean stayAlive) {
        applyDamage(from, damage, stayAlive, true);
    }
    
    public void heal(int hp, int mp) {
        Integer hpHealed = applyAndGetHpDamage(-hp, false);
        if (hpHealed == null) {
            return;
        }
        
        int mp2Heal = getMp() + mp;
        int maxMp = getMaxMp();
        if (mp2Heal >= maxMp) {
            mp2Heal = maxMp;
        }
        setMp(mp2Heal);
        
        if (hp > 0) {
            getMap().broadcastMessage(MaplePacketCreator.healMonster(getObjectId(), hp, getHp(), getMaxHp()));
        }
        
        maxHpPlusHeal.addAndGet(hpHealed);
        dispatchMonsterHealed(hpHealed);
    }

    public boolean isAttackedBy(MapleCharacter chr) {
        return takenDamage.containsKey(chr.getId());
    }
    
    private static boolean isWhiteExpGain(MapleCharacter chr, Map<Integer, Float> personalRatio, double sdevRatio) {
        Float pr = personalRatio.get(chr.getId());
        if (pr == null) {
            return false;
        }
        
        return pr >= sdevRatio;
    }
    
    private static double calcExperienceStandDevThreshold(List<Float> entryExpRatio, int totalEntries) {
        float avgExpReward = 0.0f;
        for (Float exp : entryExpRatio) {
            avgExpReward += exp;
        }
        
        // thanks Simon (HarborMS) for finding an issue with solo party player gaining yellow EXP when soloing mobs
        avgExpReward /= totalEntries;
        
        float varExpReward = 0.0f;
        for (Float exp : entryExpRatio) {
            varExpReward += Math.pow(exp - avgExpReward, 2);
        }
        varExpReward /= entryExpRatio.size();
        
        return avgExpReward + Math.sqrt(varExpReward);
    }
    
    private void distributePlayerExperience(MapleCharacter chr, float exp, float partyBonusMod, int totalPartyLevel, boolean highestPartyDamager, boolean whiteExpGain, boolean hasPartySharers) {
        float playerExp = (YamlConfig.config.server.EXP_SPLIT_COMMON_MOD * chr.getLevel()) / totalPartyLevel;
        if (highestPartyDamager) playerExp += YamlConfig.config.server.EXP_SPLIT_MVP_MOD;
        
        playerExp *= exp;
        float bonusExp = partyBonusMod * playerExp;
        
        this.giveExpToCharacter(chr, playerExp, bonusExp, whiteExpGain, hasPartySharers);
        giveFamilyRep(chr.getFamilyEntry());
    }
    
    private void distributePartyExperience(Map<MapleCharacter, Long> partyParticipation, float expPerDmg, Set<MapleCharacter> underleveled, Map<Integer, Float> personalRatio, double sdevRatio) {
        IntervalBuilder leechInterval = new IntervalBuilder();
        leechInterval.addInterval(this.getLevel() - YamlConfig.config.server.EXP_SPLIT_LEVEL_INTERVAL, this.getLevel() + YamlConfig.config.server.EXP_SPLIT_LEVEL_INTERVAL);
        
        long maxDamage = 0, partyDamage = 0;
        MapleCharacter participationMvp = null;
        for (Entry<MapleCharacter, Long> e : partyParticipation.entrySet()) {
            long entryDamage = e.getValue();
            partyDamage += entryDamage;
            
            if (maxDamage < entryDamage) {
                maxDamage = entryDamage;
                participationMvp = e.getKey();
            }
            
            // thanks Thora for pointing out leech level limitation
            int chrLevel = e.getKey().getLevel();
            leechInterval.addInterval(chrLevel - YamlConfig.config.server.EXP_SPLIT_LEECH_INTERVAL, chrLevel + YamlConfig.config.server.EXP_SPLIT_LEECH_INTERVAL);
        }
        
        List<MapleCharacter> expMembers = new LinkedList<>();
        int totalPartyLevel = 0;
        
        // thanks G h o s t, Alfred, Vcoc, BHB for poiting out a bug in detecting party members after membership transactions in a party took place
        if (YamlConfig.config.server.USE_ENFORCE_MOB_LEVEL_RANGE) {
            for (MapleCharacter member : partyParticipation.keySet().iterator().next().getPartyMembersOnSameMap()) {
                if (!leechInterval.inInterval(member.getLevel())) {
                    underleveled.add(member);
                    continue;
                }

                totalPartyLevel += member.getLevel();
                expMembers.add(member);
            }
        } else {    // thanks Ari for noticing unused server flag after EXP system overhaul
            for (MapleCharacter member : partyParticipation.keySet().iterator().next().getPartyMembersOnSameMap()) {
                totalPartyLevel += member.getLevel();
                expMembers.add(member);
            }
        }
        
        int membersSize = expMembers.size();
        float participationExp = partyDamage * expPerDmg;
        
        // thanks Crypter for reporting an insufficiency on party exp bonuses
        boolean hasPartySharers = membersSize > 1;
        float partyBonusMod = hasPartySharers ? 0.05f * membersSize : 0.0f;
        
        for (MapleCharacter mc : expMembers) {
            distributePlayerExperience(mc, participationExp, partyBonusMod, totalPartyLevel, mc == participationMvp, isWhiteExpGain(mc, personalRatio, sdevRatio), hasPartySharers);
            giveFamilyRep(mc.getFamilyEntry());
        }
    }
    
    private void distributeExperience(int killerId) {
        if (isAlive()) {
            return;
        }
        
        Map<MapleParty, Map<MapleCharacter, Long>> partyExpDist = new HashMap<>();
        Map<MapleCharacter, Long> soloExpDist = new HashMap<>();
        
        Map<Integer, MapleCharacter> mapPlayers = map.getMapAllPlayers();
        
        int totalEntries = 0;   // counts "participant parties", players who no longer are available in the map is an "independent party"
        for (Entry<Integer, AtomicLong> e : takenDamage.entrySet()) {
            MapleCharacter chr = mapPlayers.get(e.getKey());
            if (chr != null) {
                long damage = e.getValue().longValue();
                
                MapleParty p = chr.getParty();
                if (p != null) {
                    Map<MapleCharacter, Long> partyParticipation = partyExpDist.get(p);
                    if (partyParticipation == null) {
                        partyParticipation = new HashMap<>(6);
                        partyExpDist.put(p, partyParticipation);
                        
                        totalEntries += 1;
                    }
                    
                    partyParticipation.put(chr, damage);
                } else {
                    soloExpDist.put(chr, damage);
                    totalEntries += 1;
                }
            } else {
                totalEntries += 1;
            }
        }
        
        long totalDamage = maxHpPlusHeal.get();
        int mobExp = getExp();
        float expPerDmg = ((float) mobExp) / totalDamage;
        
        Map<Integer, Float> personalRatio = new HashMap<>();
        List<Float> entryExpRatio = new LinkedList<>();
        for (Entry<MapleCharacter, Long> e : soloExpDist.entrySet()) {
            float ratio = ((float) e.getValue()) / totalDamage;
            
            personalRatio.put(e.getKey().getId(), ratio);
            entryExpRatio.add(ratio);
        }
        
        for (Map<MapleCharacter, Long> m : partyExpDist.values()) {
            float ratio = 0.0f;
            for (Entry<MapleCharacter, Long> e : m.entrySet()) {
                float chrRatio = ((float) e.getValue()) / totalDamage;
                
                personalRatio.put(e.getKey().getId(), chrRatio);
                ratio += chrRatio;
            }
            
            entryExpRatio.add(ratio);
        }
        
        double sdevRatio = calcExperienceStandDevThreshold(entryExpRatio, totalEntries);
        
        // GMS-like player and party split calculations found thanks to Russt, KaidaTan, Dusk, AyumiLove - src: https://ayumilovemaple.wordpress.com/maplestory_calculator_formula/
        Set<MapleCharacter> underleveled = new HashSet<>();
        for (Entry<MapleCharacter, Long> chrParticipation : soloExpDist.entrySet()) {
            float exp = chrParticipation.getValue() * expPerDmg;
            MapleCharacter chr = chrParticipation.getKey();
            
            distributePlayerExperience(chr, exp, 0.0f, chr.getLevel(), true, isWhiteExpGain(chr, personalRatio, sdevRatio), false);
        }
        
        for (Map<MapleCharacter, Long> partyParticipation : partyExpDist.values()) {
            distributePartyExperience(partyParticipation, expPerDmg, underleveled, personalRatio, sdevRatio);
        }
        
        EventInstanceManager eim = getMap().getEventInstance();
        if (eim != null) {
            MapleCharacter chr = mapPlayers.get(killerId);
            if (chr != null) {
                eim.monsterKilled(chr, this);
            }
        }
        
        for(MapleCharacter mc : underleveled) {
            mc.showUnderleveledInfo(this);
        }
        
    }
    
    private float getStatusExpMultiplier(MapleCharacter attacker, boolean hasPartySharers) {
        float multiplier = 1.0f;
        
        // thanks Prophecy & Aika for finding out Holy Symbol not being applied on party bonuses
        Integer holySymbol = attacker.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
        if (holySymbol != null) {
            if (YamlConfig.config.server.USE_FULL_HOLY_SYMBOL) { // thanks Mordred, xinyifly, AyumiLove, andy33 for noticing HS hands out 20% of its potential on less than 3 players
                multiplier *= (1.0 + (holySymbol.doubleValue() / 100.0));
            } else {
                multiplier *= (1.0 + (holySymbol.doubleValue() / (hasPartySharers ? 100.0 : 500.0)));
            }
        }

        statiLock.lock();
        try {
            MonsterStatusEffect mse = stati.get(MonsterStatus.SHOWDOWN);
            if (mse != null) {
                multiplier *= (1.0 + (mse.getStati().get(MonsterStatus.SHOWDOWN).doubleValue() / 100.0));
            }
        } finally {
            statiLock.unlock();
        }
        
        return multiplier;
    }
    
    private static int expValueToInteger(double exp) {
        if (exp > Integer.MAX_VALUE) {
            exp = Integer.MAX_VALUE;
        } else if (exp < Integer.MIN_VALUE) {
            exp = Integer.MIN_VALUE;
        }
        
        return (int) Math.round(exp);    // operations on float point are not point-precise... thanks IxianMace for noticing -1 EXP gains
    }
    
    private void giveExpToCharacter(MapleCharacter attacker, Float personalExp, Float partyExp, boolean white, boolean hasPartySharers) {
        if (attacker.isAlive()) {
            if (personalExp != null) {
                personalExp *= getStatusExpMultiplier(attacker, hasPartySharers);
                personalExp *= attacker.getExpRate();
            } else {
                personalExp = 0.0f;
            }
            
            Integer expBonus = attacker.getBuffedValue(MapleBuffStat.EXP_INCREASE);
            if (expBonus != null) {     // exp increase player buff found thanks to HighKey21
                personalExp += expBonus;
            }

            int _personalExp = expValueToInteger(personalExp); // assuming no negative xp here
            
            if (partyExp != null) {
                partyExp *= getStatusExpMultiplier(attacker, hasPartySharers);
                partyExp *= attacker.getExpRate();
                partyExp *= YamlConfig.config.server.PARTY_BONUS_EXP_RATE;
            } else {
                partyExp = 0.0f;
            }
            
            int _partyExp = expValueToInteger(partyExp);
            
            attacker.gainExp(_personalExp, _partyExp, true, false, white);
            attacker.increaseEquipExp(_personalExp);
            attacker.raiseQuestMobCount(getId());
        }
    }
    
    public List<MonsterDropEntry> retrieveRelevantDrops() {
        if (this.getStats().isFriendly()) {     // thanks Conrad for noticing friendly mobs not spawning loots after a recent update
            return MapleMonsterInformationProvider.getInstance().retrieveEffectiveDrop(this.getId());
        }
        
        Map<Integer, MapleCharacter> pchars = map.getMapAllPlayers();
        
        List<MapleCharacter> lootChars = new LinkedList<>();
        for (Integer cid : takenDamage.keySet()) {
            MapleCharacter chr = pchars.get(cid);
            if (chr != null && chr.isLoggedinWorld()) {
                lootChars.add(chr);
            }
        }
        
        return MapleLootManager.retrieveRelevantDrops(this.getId(), lootChars);
    }
    
    public MapleCharacter killBy(final MapleCharacter killer) {
        distributeExperience(killer != null ? killer.getId() : 0);
        
        final Pair<MapleCharacter, Boolean> lastController = aggroRemoveController();
        final List<Integer> toSpawn = this.getRevives();
        if (toSpawn != null) {
            final MapleMap reviveMap = map;
            if (toSpawn.contains(9300216) && reviveMap.getId() > 925000000 && reviveMap.getId() < 926000000) {
                reviveMap.broadcastMessage(MaplePacketCreator.playSound("Dojang/clear"));
                reviveMap.broadcastMessage(MaplePacketCreator.showEffect("dojang/end/clear"));
            }
            Pair<Integer, String> timeMob = reviveMap.getTimeMob();
            if (timeMob != null) {
                if (toSpawn.contains(timeMob.getLeft())) {
                    reviveMap.broadcastMessage(MaplePacketCreator.serverNotice(6, timeMob.getRight()));
                }
            }
            
            if(toSpawn.size() > 0) {
                final EventInstanceManager eim = this.getMap().getEventInstance();
                
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        MapleCharacter controller = lastController.getLeft();
                        boolean aggro = lastController.getRight();
                        
                        for (Integer mid : toSpawn) {
                            final MapleMonster mob = MapleLifeFactory.getMonster(mid);
                            mob.setPosition(getPosition());
                            mob.setFh(getFh());
                            mob.setParentMobOid(getObjectId());
                            
                            if (dropsDisabled()) {
                                mob.disableDrops();
                            }
                            reviveMap.spawnMonster(mob);

                            if (mob.getId() >= 8810010 && mob.getId() <= 8810017 && reviveMap.isHorntailDefeated()) {
                                boolean htKilled = false;
                                MapleMonster ht = reviveMap.getMonsterById(8810018);
                                
                                if(ht != null) {
                                    ht.lockMonster();
                                    try {
                                        htKilled = ht.isAlive();
                                        ht.setHpZero();
                                    } finally {
                                        ht.unlockMonster();
                                    }
                                    
                                    if(htKilled) {
                                        reviveMap.killMonster(ht, killer, true);
                                    }
                                }
                                
                                for(int i = 8810017; i >= 8810010; i--) {
                                    reviveMap.killMonster(reviveMap.getMonsterById(i), killer, true);
                                }
                            } else if (controller != null) {
                                mob.aggroSwitchController(controller, aggro);
                            }
                            
                            if(eim != null) {
                                eim.reviveMonster(mob);
                            }
                        }
                    }
                }, getAnimationTime("die1"));
            }
        } else {  // is this even necessary?
            System.out.println("[CRITICAL LOSS] toSpawn is null for " + this.getName());
        }
        
        MapleCharacter looter = map.getCharacterById(getHighestDamagerId());
        return looter != null ? looter : killer;
    }
    
    public void dropFromFriendlyMonster(long delay) {
        final MapleMonster m = this;
        monsterItemDrop = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (!m.isAlive()) {
                    if (monsterItemDrop != null) {
                        monsterItemDrop.cancel(false);
                    }
                    
                    return;
                }
                
                MapleMap map = m.getMap();
                List<MapleCharacter> chrList = map.getAllPlayers();
                if (!chrList.isEmpty()) {
                    MapleCharacter chr = (MapleCharacter) chrList.get(0);
                    
                    EventInstanceManager eim = map.getEventInstance();
                    if (eim != null) {
                        eim.friendlyItemDrop(m);
                    }
                    
                    map.dropFromFriendlyMonster(chr, m);
                }
            }
        }, delay, delay);
    }
    
    private void dispatchRaiseQuestMobCount() {
        Set<Integer> attackerChrids = takenDamage.keySet();
        if(!attackerChrids.isEmpty()) {
            Map<Integer, MapleCharacter> mapChars = map.getMapPlayers();
            if(!mapChars.isEmpty()) {
                int mobid = getId();
                
                for (Integer chrid : attackerChrids) {
                    MapleCharacter chr = mapChars.get(chrid);

                    if(chr != null && chr.isLoggedinWorld()) {
                        chr.raiseQuestMobCount(mobid);
                    }
                }
            }
        }
    }
    
    public void dispatchMonsterKilled(boolean hasKiller) {
        processMonsterKilled(hasKiller);
        
        EventInstanceManager eim = getMap().getEventInstance();
        if (eim != null) {
            if (!this.getStats().isFriendly()) {
                eim.monsterKilled(this, hasKiller);
            } else {
                eim.friendlyKilled(this, hasKiller);
            }
        }
    }
    
    private synchronized void processMonsterKilled(boolean hasKiller) {
        if(!hasKiller) {    // players won't gain EXP from a mob that has no killer, but a quest count they should
            dispatchRaiseQuestMobCount();
        }
        
        this.aggroClearDamages();
        this.dispatchClearSummons();
        
        MonsterListener[] listenersList;
        statiLock.lock();
        try {
            listenersList = listeners.toArray(new MonsterListener[listeners.size()]);
        } finally {
            statiLock.unlock();
        }
        
        for (MonsterListener listener : listenersList) {
            listener.monsterKilled(getAnimationTime("die1"));
        }
        
        statiLock.lock();
        try {
            stati.clear();
            alreadyBuffed.clear();
            listeners.clear();
        } finally {
            statiLock.unlock();
        }
    }
    
    private void dispatchMonsterDamaged(MapleCharacter from, int trueDmg) {
        MonsterListener[] listenersList;
        statiLock.lock();
        try {
            listenersList = listeners.toArray(new MonsterListener[listeners.size()]);
        } finally {
            statiLock.unlock();
        }
        
        for (MonsterListener listener : listenersList) {
            listener.monsterDamaged(from, trueDmg);
        }
    }
    
    private void dispatchMonsterHealed(int trueHeal) {
        MonsterListener[] listenersList;
        statiLock.lock();
        try {
            listenersList = listeners.toArray(new MonsterListener[listeners.size()]);
        } finally {
            statiLock.unlock();
        }
        
        for (MonsterListener listener : listenersList) {
            listener.monsterHealed(trueHeal);
        }
    }
    
    private void giveFamilyRep(MapleFamilyEntry entry) {
        if(entry != null) {
            int repGain = isBoss() ? YamlConfig.config.server.FAMILY_REP_PER_BOSS_KILL : YamlConfig.config.server.FAMILY_REP_PER_KILL;
            if(getMaxHp() <= 1) repGain = 0; //don't count trash mobs
            entry.giveReputationToSenior(repGain, true);
        }
    }

    public int getHighestDamagerId() {
        int curId = 0;
        long curDmg = 0;

        for (Entry<Integer, AtomicLong> damage : takenDamage.entrySet()) {
            curId = damage.getValue().get() >= curDmg ? damage.getKey() : curId;
            curDmg = damage.getKey() == curId ? damage.getValue().get() : curDmg;
        }

        return curId;
    }

    public boolean isAlive() {
        return this.hp.get() > 0;
    }
    
    public void addListener(MonsterListener listener) {
        statiLock.lock();
        try {
            listeners.add(listener);
        } finally {
            statiLock.unlock();
        }
    }

    public MapleCharacter getController() {
        return controller.get();
    }

    private void setController(MapleCharacter controller) {
        this.controller = new WeakReference<>(controller);
    }
    
    public boolean isControllerHasAggro() {
        return fake ? false : controllerHasAggro;
    }

    private void setControllerHasAggro(boolean controllerHasAggro) {
        if (!fake) {
            this.controllerHasAggro = controllerHasAggro;
        }
    }

    public boolean isControllerKnowsAboutAggro() {
        return fake ? false : controllerKnowsAboutAggro;
    }

    private void setControllerKnowsAboutAggro(boolean controllerKnowsAboutAggro) {
        if (!fake) {
            this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
        }
    }
    
    private void setControllerHasPuppet(boolean controllerHasPuppet) {
        this.controllerHasPuppet = controllerHasPuppet;
    }

    public byte[] makeBossHPBarPacket() {
        return MaplePacketCreator.showBossHP(getId(), getHp(), getMaxHp(), getTagColor(), getTagBgColor());
    }

    public boolean hasBossHPBar() {
        return isBoss() && getTagColor() > 0;
    }
    
    @Override
    public void sendSpawnData(MapleClient client) {
        if (hp.get() <= 0) { // mustn't monsterLock this function
            return;
        }
        if (fake) {
            client.announce(MaplePacketCreator.spawnFakeMonster(this, 0));
        } else {
            client.announce(MaplePacketCreator.spawnMonster(this, false));
        }
        
        if (hasBossHPBar()) {
            client.announceBossHpBar(this, this.hashCode(), makeBossHPBarPacket());
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.killMonster(getObjectId(), false));
        client.announce(MaplePacketCreator.killMonster(getObjectId(), true));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MONSTER;
    }

    public boolean isMobile() {
        return stats.isMobile();
    }

    @Override
    public boolean isFacingLeft() {
        int fixedStance = stats.getFixedStance();    // thanks DimDiDima for noticing inconsistency on some AOE mobskills
        if (fixedStance != 0) {
            return Math.abs(fixedStance) % 2 == 1;
        }
        
        return super.isFacingLeft();
    }
    
    public ElementalEffectiveness getElementalEffectiveness(Element e) {
        statiLock.lock();
        try {
            if (stati.get(MonsterStatus.DOOM) != null) {
                return ElementalEffectiveness.NORMAL; // like blue snails
            }
        } finally {
            statiLock.unlock();
        }
        
        return getMonsterEffectiveness(e);
    }
    
    private ElementalEffectiveness getMonsterEffectiveness(Element e) {
        monsterLock.lock();
        try {
            return stats.getEffectiveness(e);
        } finally {
            monsterLock.unlock();
        }
    }

    private MapleCharacter getActiveController() {
        MapleCharacter chr = getController();
        
        if (chr != null && chr.isLoggedinWorld() && chr.getMap() == this.getMap()) {
            return chr;
        } else {
            return null;
        }
    }
    
    private void broadcastMonsterStatusMessage(byte[] packet) {
        map.broadcastMessage(packet, getPosition());
        
        MapleCharacter chrController = getActiveController();
        if (chrController != null && !chrController.isMapObjectVisible(MapleMonster.this)) {
            chrController.announce(packet);
        }
    }
    
    private int broadcastStatusEffect(final MonsterStatusEffect status) {
        int animationTime = status.getSkill().getAnimationTime();
        byte[] packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), status, null);
        broadcastMonsterStatusMessage(packet);
        
        return animationTime;
    }
    
    public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status, boolean poison, long duration) {
        return applyStatus(from, status, poison, duration, false);
    }

    public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status, boolean poison, long duration, boolean venom) {
        switch (getMonsterEffectiveness(status.getSkill().getElement())) {
            case IMMUNE:
            case STRONG:
            case NEUTRAL:
                return false;
            case NORMAL:
            case WEAK:
                break;
            default: {
                System.out.println("Unknown elemental effectiveness: " + getMonsterEffectiveness(status.getSkill().getElement()));
                return false;
            }
        }

        if (status.getSkill().getId() == FPMage.ELEMENT_COMPOSITION) { // fp compo
            ElementalEffectiveness effectiveness = getMonsterEffectiveness(Element.POISON);
            if (effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) {
                return false;
            }
        } else if (status.getSkill().getId() == ILMage.ELEMENT_COMPOSITION) { // il compo
            ElementalEffectiveness effectiveness = getMonsterEffectiveness(Element.ICE);
            if (effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) {
                return false;
            }
        } else if (status.getSkill().getId() == NightLord.VENOMOUS_STAR || status.getSkill().getId() == Shadower.VENOMOUS_STAB || status.getSkill().getId() == NightWalker.VENOM) {// venom
            if (getMonsterEffectiveness(Element.POISON) == ElementalEffectiveness.WEAK) {
                return false;
            }
        }
        if (poison && hp.get() <= 1) {
            return false;
        }

        final Map<MonsterStatus, Integer> statis = status.getStati();
        if (stats.isBoss()) {
            if (!(statis.containsKey(MonsterStatus.SPEED)
                    && statis.containsKey(MonsterStatus.NINJA_AMBUSH)
                    && statis.containsKey(MonsterStatus.WATK))) {
                return false;
            }
        }

        final Channel ch = map.getChannelServer();
        final int mapid = map.getId();
        if(statis.size() > 0) {
            statiLock.lock();
            try {
                for (MonsterStatus stat : statis.keySet()) {
                    final MonsterStatusEffect oldEffect = stati.get(stat);
                    if (oldEffect != null) {
                        oldEffect.removeActiveStatus(stat);
                        if (oldEffect.getStati().isEmpty()) {
                            MobStatusService service = (MobStatusService) map.getChannelServer().getServiceAccess(ChannelServices.MOB_STATUS);
                            service.interruptMobStatus(mapid, oldEffect);
                        }
                    }
                }
            } finally {
                statiLock.unlock();
            }
        }
        
        final Runnable cancelTask = new Runnable() {

            @Override
            public void run() {
                if (isAlive()) {
                    byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), status.getStati());
                    broadcastMonsterStatusMessage(packet);
                }
                
                statiLock.lock();
                try {
                    for (MonsterStatus stat : status.getStati().keySet()) {
                        stati.remove(stat);
                    }
                } finally {
                    statiLock.unlock();
                }
                
                setVenomMulti(0);
            }
        };
        
        Runnable overtimeAction = null;
        int overtimeDelay = -1;
        
        int animationTime;
        if (poison) {
            int poisonLevel = from.getSkillLevel(status.getSkill());
            int poisonDamage = Math.min(Short.MAX_VALUE, (int) (getMaxHp() / (70.0 - poisonLevel) + 0.999));
            status.setValue(MonsterStatus.POISON, Integer.valueOf(poisonDamage));
            animationTime = broadcastStatusEffect(status);
            
            overtimeAction = new DamageTask(poisonDamage, from, status, 0);
            overtimeDelay = 1000;
        } else if (venom) {
            if (from.getJob() == MapleJob.NIGHTLORD || from.getJob() == MapleJob.SHADOWER || from.getJob().isA(MapleJob.NIGHTWALKER3)) {
                int poisonLevel, matk, jobid = from.getJob().getId();
                int skillid = (jobid == 412 ? NightLord.VENOMOUS_STAR : (jobid == 422 ? Shadower.VENOMOUS_STAB : NightWalker.VENOM));
                poisonLevel = from.getSkillLevel(SkillFactory.getSkill(skillid));
                if (poisonLevel <= 0) {
                    return false;
                }
                matk = SkillFactory.getSkill(skillid).getEffect(poisonLevel).getMatk();
                int luk = from.getLuk();
                int maxDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.2 * luk * matk));
                int minDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.1 * luk * matk));
                int gap = maxDmg - minDmg;
                if (gap == 0) {
                    gap = 1;
                }
                int poisonDamage = 0;
                for (int i = 0; i < getVenomMulti(); i++) {
                    poisonDamage += (Randomizer.nextInt(gap) + minDmg);
                }
                poisonDamage = Math.min(Short.MAX_VALUE, poisonDamage);
                status.setValue(MonsterStatus.VENOMOUS_WEAPON, Integer.valueOf(poisonDamage));
                status.setValue(MonsterStatus.POISON, Integer.valueOf(poisonDamage));
                animationTime = broadcastStatusEffect(status);
                
                overtimeAction = new DamageTask(poisonDamage, from, status, 0);
                overtimeDelay = 1000;
            } else {
                return false;
            }
            /*
        } else if (status.getSkill().getId() == Hermit.SHADOW_WEB || status.getSkill().getId() == NightWalker.SHADOW_WEB) { //Shadow Web
            int webDamage = (int) (getMaxHp() / 50.0 + 0.999);
            status.setValue(MonsterStatus.SHADOW_WEB, Integer.valueOf(webDamage));
            animationTime = broadcastStatusEffect(status);
            
            overtimeAction = new DamageTask(webDamage, from, status, 1);
            overtimeDelay = 3500;
            */
        } else if (status.getSkill().getId() == 4121004 || status.getSkill().getId() == 4221004) { // Ninja Ambush
            final Skill skill = SkillFactory.getSkill(status.getSkill().getId());
            final byte level = from.getSkillLevel(skill);
            final int damage = (int) ((from.getStr() + from.getLuk()) * ((3.7 * skill.getEffect(level).getDamage()) / 100));
            
            status.setValue(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(damage));
            animationTime = broadcastStatusEffect(status);
            
            overtimeAction = new DamageTask(damage, from, status, 2);
            overtimeDelay = 1000;
        } else {
            animationTime = broadcastStatusEffect(status);
        }
        
        statiLock.lock();
        try {
            for (MonsterStatus stat : status.getStati().keySet()) {
                stati.put(stat, status);
                alreadyBuffed.add(stat);
            }
        } finally {
            statiLock.unlock();
        }
        
        MobStatusService service = (MobStatusService) map.getChannelServer().getServiceAccess(ChannelServices.MOB_STATUS);
        service.registerMobStatus(mapid, status, cancelTask, duration + animationTime - 100, overtimeAction, overtimeDelay);
        return true;
    }
    
    public final void dispelSkill(final MobSkill skillId) {
        List<MonsterStatus> toCancel = new ArrayList<MonsterStatus>();
        for (Entry<MonsterStatus, MonsterStatusEffect> effects : stati.entrySet()) {
            MonsterStatusEffect mse = effects.getValue();
            if (mse.getMobSkill() != null && mse.getMobSkill().getSkillId() == skillId.getSkillId()) { //not checking for level.
                toCancel.add(effects.getKey());
            }
        }
        for (MonsterStatus stat : toCancel) {
            debuffMobStat(stat);
        }
    }
    
    public void applyMonsterBuff(final Map<MonsterStatus, Integer> stats, final int x, int skillId, long duration, MobSkill skill, final List<Integer> reflection) {
        final Runnable cancelTask = new Runnable() {

            @Override
            public void run() {
                if (isAlive()) {
                    byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), stats);
                    broadcastMonsterStatusMessage(packet);
                    
                    statiLock.lock();
                    try {
                        for (final MonsterStatus stat : stats.keySet()) {
                            stati.remove(stat);
                        }
                    } finally {
                        statiLock.unlock();
                    }
                }
            }
        };
        final MonsterStatusEffect effect = new MonsterStatusEffect(stats, null, skill, true);
        byte[] packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), effect, reflection);
        broadcastMonsterStatusMessage(packet);
        
        statiLock.lock();
        try {
            for (MonsterStatus stat : stats.keySet()) {
                stati.put(stat, effect);
                alreadyBuffed.add(stat);
            }
        } finally {
            statiLock.unlock();
        }
        
        MobStatusService service = (MobStatusService) map.getChannelServer().getServiceAccess(ChannelServices.MOB_STATUS);
        service.registerMobStatus(map.getId(), effect, cancelTask, duration);
    }
    
    public void refreshMobPosition() {
        resetMobPosition(getPosition());
    }
    
    public void resetMobPosition(Point newPoint) {
        aggroRemoveController();
        
        setPosition(newPoint);
        map.broadcastMessage(MaplePacketCreator.moveMonster(this.getObjectId(), false, -1, 0, 0, 0, this.getPosition(), this.getIdleMovement(), getIdleMovementDataLength()));
        map.moveMonster(this, this.getPosition());
        
        aggroUpdateController();
    }

    private void debuffMobStat(MonsterStatus stat) {
        MonsterStatusEffect oldEffect;
        statiLock.lock();
        try {
            oldEffect = stati.remove(stat);
        } finally {
            statiLock.unlock();
        }
        
        if (oldEffect != null) {
            byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), oldEffect.getStati());
            broadcastMonsterStatusMessage(packet);
        }
    }
    
    public void debuffMob(int skillid) {
        MonsterStatus[] statups = {MonsterStatus.WEAPON_ATTACK_UP, MonsterStatus.WEAPON_DEFENSE_UP, MonsterStatus.MAGIC_ATTACK_UP, MonsterStatus.MAGIC_DEFENSE_UP};
        statiLock.lock();
        try {
            if(skillid == Hermit.SHADOW_MESO) {
                debuffMobStat(statups[1]);
                debuffMobStat(statups[3]);
            } else if(skillid == Priest.DISPEL) {
                for(MonsterStatus ms : statups) {
                    debuffMobStat(ms);
                }
            } else {    // is a crash skill
                int i = (skillid == Crusader.ARMOR_CRASH ? 1 : (skillid == WhiteKnight.MAGIC_CRASH ? 2 : 0));
                debuffMobStat(statups[i]);

                if(YamlConfig.config.server.USE_ANTI_IMMUNITY_CRASH) {
                    if (skillid == Crusader.ARMOR_CRASH) {
                        if(!isBuffed(MonsterStatus.WEAPON_REFLECT)) {
                            debuffMobStat(MonsterStatus.WEAPON_IMMUNITY);
                        }
                        if(!isBuffed(MonsterStatus.MAGIC_REFLECT)) {
                            debuffMobStat(MonsterStatus.MAGIC_IMMUNITY);
                        }
                    } else if (skillid == WhiteKnight.MAGIC_CRASH) {
                        if(!isBuffed(MonsterStatus.MAGIC_REFLECT)) {
                            debuffMobStat(MonsterStatus.MAGIC_IMMUNITY);
                        }
                    } else {
                        if(!isBuffed(MonsterStatus.WEAPON_REFLECT)) {
                            debuffMobStat(MonsterStatus.WEAPON_IMMUNITY);
                        }
                    }
                }
            }
        } finally {
            statiLock.unlock();
        }
    }

    public boolean isBuffed(MonsterStatus status) {
        statiLock.lock();
        try {
            return stati.containsKey(status);
        } finally {
            statiLock.unlock();
        }
    }

    public void setFake(boolean fake) {
        monsterLock.lock();
        try {
            this.fake = fake;
        } finally {
            monsterLock.unlock();
        }
    }

    public boolean isFake() {
        monsterLock.lock();
        try {
            return fake;
        } finally {
            monsterLock.unlock();
        }
    }

    public MapleMap getMap() {
        return map;
    }
    
    public MapleMonsterAggroCoordinator getMapAggroCoordinator() {
        return map.getAggroCoordinator();
    }
    
    public List<Pair<Integer, Integer>> getSkills() {
        return stats.getSkills();
    }

    public boolean hasSkill(int skillId, int level) {
        return stats.hasSkill(skillId, level);
    }
    
    public int getSkillPos(int skillId, int level) {
        int pos = 0;
        for (Pair<Integer, Integer> ms : this.getSkills()) {
            if (ms.getLeft() == skillId && ms.getRight() == level) {
                return pos;
            }
            
            pos++;
        }
        
        return -1;
    }
    
    public boolean canUseSkill(MobSkill toUse, boolean apply) {
        if (toUse == null) {
            return false;
        }
        
        int useSkillid = toUse.getSkillId();
        if (useSkillid >= 143 && useSkillid <= 145) {
            if (this.isBuffed(MonsterStatus.WEAPON_REFLECT) || this.isBuffed(MonsterStatus.MAGIC_REFLECT)) {
                return false;
            }
        }
        
        monsterLock.lock();
        try {
            for (Pair<Integer, Integer> skill : usedSkills) {   // thanks OishiiKawaiiDesu for noticing an issue with mobskill cooldown
                if (skill.getLeft() == useSkillid && skill.getRight() == toUse.getSkillLevel()) {
                    return false;
                }
            }
            
            int mpCon = toUse.getMpCon();
            if (mp < mpCon) {
                return false;
            }
            
            /*
            if (!this.applyAnimationIfRoaming(-1, toUse)) {
                return false;
            }
            */
            
            if (apply) {
                this.usedSkill(toUse);
            }
        } finally {
            monsterLock.unlock();
        }
        
        return true;
    }

    private void usedSkill(MobSkill skill) {
        final int skillId = skill.getSkillId(), level = skill.getSkillLevel();
        long cooltime = skill.getCoolTime();
        
        monsterLock.lock();
        try {
            mp -= skill.getMpCon();
            
            Pair<Integer, Integer> skillKey = new Pair<>(skillId, level);
            this.usedSkills.add(skillKey);
            
            Integer useCount = this.skillsUsed.remove(skillKey);
            if (useCount != null) {
                this.skillsUsed.put(skillKey, useCount + 1);
            } else {
                this.skillsUsed.put(skillKey, 1);
            }
        } finally {
            monsterLock.unlock();
        }
        
        final MapleMonster mons = this;
        MapleMap mmap = mons.getMap();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mons.clearSkill(skillId, level);
            }
        };
        
        MobClearSkillService service = (MobClearSkillService) map.getChannelServer().getServiceAccess(ChannelServices.MOB_CLEAR_SKILL);
        service.registerMobClearSkillAction(mmap.getId(), r, cooltime);
    }

    private void clearSkill(int skillId, int level) {
        monsterLock.lock();
        try {
            int index = -1;
            for (Pair<Integer, Integer> skill : usedSkills) {
                if (skill.getLeft() == skillId && skill.getRight() == level) {
                    index = usedSkills.indexOf(skill);
                    break;
                }
            }
            if (index != -1) {
                usedSkills.remove(index);
            }
        } finally {
            monsterLock.unlock();
        }
    }
    
    public int canUseAttack(int attackPos, boolean isSkill) {
        monsterLock.lock();
        try {
            /*
            if (usedAttacks.contains(attackPos)) {
                return -1;
            }
            */
            
            Pair<Integer, Integer> attackInfo = MapleMonsterInformationProvider.getInstance().getMobAttackInfo(this.getId(), attackPos);
            if (attackInfo == null) {
                return -1;
            }
            
            int mpCon = attackInfo.getLeft();
            if (mp < mpCon) {
                return -1;
            }
            
            /*
            if (!this.applyAnimationIfRoaming(attackPos, null)) {
                return -1;
            }
            */
            
            usedAttack(attackPos, mpCon, attackInfo.getRight());
            return 1;
        } finally {
            monsterLock.unlock();
        }
    }
    
    private void usedAttack(final int attackPos, int mpCon, int cooltime) {
        monsterLock.lock();
        try {
            mp -= mpCon;
            usedAttacks.add(attackPos);

            final MapleMonster mons = this;
            MapleMap mmap = mons.getMap();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mons.clearAttack(attackPos);
                }
            };
            
            MobClearSkillService service = (MobClearSkillService) map.getChannelServer().getServiceAccess(ChannelServices.MOB_CLEAR_SKILL);
            service.registerMobClearSkillAction(mmap.getId(), r, cooltime);
        } finally {
            monsterLock.unlock();
        }
    }
    
    private void clearAttack(int attackPos) {
        monsterLock.lock();
        try {
            usedAttacks.remove(attackPos);
        } finally {
            monsterLock.unlock();
        }
    }
    
    public int getNoSkills() {
        return this.stats.getNoSkills();
    }

    public boolean isFirstAttack() {
        return this.stats.isFirstAttack();
    }

    public int getBuffToGive() {
        return this.stats.getBuffToGive();
    }

    private final class DamageTask implements Runnable {

        private final int dealDamage;
        private final MapleCharacter chr;
        private final MonsterStatusEffect status;
        private final int type;
        private final MapleMap map;

        private DamageTask(int dealDamage, MapleCharacter chr, MonsterStatusEffect status, int type) {
            this.dealDamage = dealDamage;
            this.chr = chr;
            this.status = status;
            this.type = type;
            this.map = chr.getMap();
        }

        @Override
        public void run() {
            int curHp = hp.get();
            if(curHp <= 1) {
                MobStatusService service = (MobStatusService) map.getChannelServer().getServiceAccess(ChannelServices.MOB_STATUS);
                service.interruptMobStatus(map.getId(), status);
                return;
            }
            
            int damage = dealDamage;
            if (damage >= curHp) {
                damage = curHp - 1;
                if (type == 1 || type == 2) {
                    MobStatusService service = (MobStatusService) map.getChannelServer().getServiceAccess(ChannelServices.MOB_STATUS);
                    service.interruptMobStatus(map.getId(), status);
                }
            }
            if (damage > 0) {
                lockMonster();
                try {
                    applyDamage(chr, damage, true, false);
                } finally {
                    unlockMonster();
                }
                
                if (type == 1) {
                    map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
                } else if (type == 2) {
                    if(damage < dealDamage) {    // ninja ambush (type 2) is already displaying DOT to the caster
                        map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
                    }
                }
            }
        }
    }

    public String getName() {
        return stats.getName();
    }

    public void addStolen(int itemId) {
        stolenItems.add(itemId);
    }

    public List<Integer> getStolen() {
        return stolenItems;
    }

    public void setTempEffectiveness(Element e, ElementalEffectiveness ee, long milli) {
        monsterLock.lock();
        try {
            final Element fE = e;
            final ElementalEffectiveness fEE = stats.getEffectiveness(e);
            if (!fEE.equals(ElementalEffectiveness.WEAK)) {
                stats.setEffectiveness(e, ee);
                
                MapleMap mmap = this.getMap();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        monsterLock.lock();
                        try {
                            stats.removeEffectiveness(fE);
                            stats.setEffectiveness(fE, fEE);
                        } finally {
                            monsterLock.unlock();
                        }
                    }
                };
                
                MobClearSkillService service = (MobClearSkillService) mmap.getChannelServer().getServiceAccess(ChannelServices.MOB_CLEAR_SKILL);
                service.registerMobClearSkillAction(mmap.getId(), r, milli);
            }
        } finally {
            monsterLock.unlock();
        }
    }

    public Collection<MonsterStatus> alreadyBuffedStats() {
        statiLock.lock();
        try {
            return Collections.unmodifiableCollection(alreadyBuffed);
        } finally {
            statiLock.unlock();
        }
    }

    public BanishInfo getBanish() {
        return stats.getBanishInfo();
    }

    public void setBoss(boolean boss) {
        this.stats.setBoss(boss);
    }

    public int getDropPeriodTime() {
        return stats.getDropPeriod();
    }

    public int getPADamage() {
        return stats.getPADamage();
    }

    public Map<MonsterStatus, MonsterStatusEffect> getStati() {
        statiLock.lock();
        try {
            return new HashMap<>(stati);
        } finally {
            statiLock.unlock();
        }
    }
    
    public MonsterStatusEffect getStati(MonsterStatus ms) {
        statiLock.lock();
        try {
            return stati.get(ms);
        } finally {
            statiLock.unlock();
        }
    }
    
    // ---- one can always have fun trying these pieces of codes below in-game rofl ----
    
    public final ChangeableStats getChangedStats() {
	return ostats;
    }

    public final int getMobMaxHp() {
        if (ostats != null) {
            return ostats.hp;
        }
        return stats.getHp();
    }
    
    public final void setOverrideStats(final OverrideMonsterStats ostats) {
        this.ostats = new ChangeableStats(stats, ostats);
        this.hp.set(ostats.getHp());
        this.mp = ostats.getMp();
    }
	
    public final void changeLevel(final int newLevel) {
        changeLevel(newLevel, true);
    }

    public final void changeLevel(final int newLevel, boolean pqMob) {
        if (!stats.isChangeable()) {
            return;
        }
        this.ostats = new ChangeableStats(stats, newLevel, pqMob);
        this.hp.set(ostats.getHp());
        this.mp = ostats.getMp();
    }
    
    private float getDifficultyRate(final int difficulty) {
        switch(difficulty) {
            case 6:
                return(7.7f);
            case 5:
                return(5.6f);
            case 4:
                return(3.2f);
            case 3:
                return(2.1f);
            case 2:
                return(1.4f);
        }
        
        return(1.0f);
    }
    
    private void changeLevelByDifficulty(final int difficulty, boolean pqMob) {
        changeLevel((int)(this.getLevel() * getDifficultyRate(difficulty)), pqMob);
    }
    
    public final void changeDifficulty(final int difficulty, boolean pqMob) {
        changeLevelByDifficulty(difficulty, pqMob);
    }
    
    // ---------------------------------------------------------------------------------
    
    private boolean isPuppetInVicinity(MapleSummon summon) {
        return summon.getPosition().distanceSq(this.getPosition()) < 177777;
    }
    
    public boolean isCharacterPuppetInVicinity(MapleCharacter chr) {
        MapleStatEffect mse = chr.getBuffEffect(MapleBuffStat.PUPPET);
        if (mse != null) {
            MapleSummon summon = chr.getSummonByKey(mse.getSourceId());

            // check whether mob is currently under a puppet's field of action or not
            if (summon != null) {
                if (isPuppetInVicinity(summon)) {
                    return true;
                }
            } else {
                map.getAggroCoordinator().removePuppetAggro(chr.getId());
            }
        }
        
        return false;
    }
    
    public boolean isLeadingPuppetInVicinity() {
        MapleCharacter chrController = this.getActiveController();
        
        if (chrController != null) {
            if (this.isCharacterPuppetInVicinity(chrController)) {
                return true;
            }
        }
        
        return false;
    }
    
    private MapleCharacter getNextControllerCandidate() {
        int mincontrolled = Integer.MAX_VALUE;
        MapleCharacter newController = null;
        
        int mincontrolleddead = Integer.MAX_VALUE;
        MapleCharacter newControllerDead = null;
        
        MapleCharacter newControllerWithPuppet = null;
        
        for (MapleCharacter chr : getMap().getAllPlayers()) {
            if (!chr.isHidden()) {
                int ctrlMonsSize = chr.getNumControlledMonsters();

                if (isCharacterPuppetInVicinity(chr)) {
                    newControllerWithPuppet = chr;
                    break;
                } else if (chr.isAlive()) {
                    if (ctrlMonsSize < mincontrolled) {
                        mincontrolled = ctrlMonsSize;
                        newController = chr;
                    }
                } else {
                    if (ctrlMonsSize < mincontrolleddead) {
                        mincontrolleddead = ctrlMonsSize;
                        newControllerDead = chr;
                    }
                }
            }
        }
        
        if (newControllerWithPuppet != null) {
            return newControllerWithPuppet;
        } else if (newController != null) {
            return newController;
        } else {
            return newControllerDead;
        }
    }
    
    /**
     * Removes controllability status from the current controller of this mob.
     * 
     */
    public Pair<MapleCharacter, Boolean> aggroRemoveController() {
        MapleCharacter chrController;
        boolean hadAggro;
        
        aggroUpdateLock.lock();
        try {
            chrController = getActiveController();
            hadAggro = isControllerHasAggro();
            
            this.setController(null);
            this.setControllerHasAggro(false);
            this.setControllerKnowsAboutAggro(false);
        } finally {
            aggroUpdateLock.unlock();
        }
        
        if (chrController != null) { // this can/should only happen when a hidden gm attacks the monster
            if (!this.isFake()) chrController.announce(MaplePacketCreator.stopControllingMonster(this.getObjectId()));
            chrController.stopControllingMonster(this);
        }
        
        return new Pair<>(chrController, hadAggro);
    }
    
    /**
     * Pass over the mob controllability and updates aggro status on the new
     * player controller.
     * 
     */
    public void aggroSwitchController(MapleCharacter newController, boolean immediateAggro) {
        if (aggroUpdateLock.tryLock()) {
            try {
                MapleCharacter prevController = getController();
                if (prevController == newController) {
                    return;
                }
                
                aggroRemoveController();
                if (!(newController != null && newController.isLoggedinWorld() && newController.getMap() == this.getMap())) {
                    return;
                }
                
                this.setController(newController);
                this.setControllerHasAggro(immediateAggro);
                this.setControllerKnowsAboutAggro(false);
                this.setControllerHasPuppet(false);
            } finally {
                aggroUpdateLock.unlock();
            }
            
            this.aggroUpdatePuppetVisibility();
            aggroMonsterControl(newController.getClient(), this, immediateAggro);
            newController.controlMonster(this);
        }
    }
    
    public void aggroAddPuppet(MapleCharacter player) {
        MapleMonsterAggroCoordinator mmac = map.getAggroCoordinator();
        mmac.addPuppetAggro(player);
        
        aggroUpdatePuppetController(player);
        
        if (this.isControllerHasAggro()) {
            this.aggroUpdatePuppetVisibility();
        }
    }
    
    public void aggroRemovePuppet(MapleCharacter player) {
        MapleMonsterAggroCoordinator mmac = map.getAggroCoordinator();
        mmac.removePuppetAggro(player.getId());
        
        aggroUpdatePuppetController(null);
        
        if (this.isControllerHasAggro()) {
            this.aggroUpdatePuppetVisibility();
        }
    }
    
    /**
     * Automagically finds a new controller for the given monster from the chars
     * on the map it is from...
     * 
     */
    public void aggroUpdateController() {
        MapleCharacter chrController = this.getActiveController();
        if (chrController != null && chrController.isAlive()) {
            return;
        }
        
        MapleCharacter newController = getNextControllerCandidate();
        if (newController == null) {    // was a new controller found? (if not no one is on the map)
            return;
        }
        
        this.aggroSwitchController(newController, false);
    }
    
    /**
     * Finds a new controller for the given monster from the chars with deployed
     * puppet nearby on the map it is from...
     * 
     */
    private void aggroUpdatePuppetController(MapleCharacter newController) {
        MapleCharacter chrController = this.getActiveController();
        boolean updateController = false;
        
        if (chrController != null && chrController.isAlive()) {
            if (isCharacterPuppetInVicinity(chrController)) {
                return;
            }
        } else {
            updateController = true;
        }
        
        if (newController == null || !isCharacterPuppetInVicinity(newController)) {
            MapleMonsterAggroCoordinator mmac = map.getAggroCoordinator();
            
            List<Integer> puppetOwners = mmac.getPuppetAggroList();
            List<Integer> toRemovePuppets = new LinkedList<>();
        
            for (Integer cid : puppetOwners) {
                MapleCharacter chr = map.getCharacterById(cid);

                if (chr != null) {
                    if (isCharacterPuppetInVicinity(chr)) {
                        newController = chr;
                        break;
                    }
                } else {
                    toRemovePuppets.add(cid);
                }
            }

            for (Integer cid : toRemovePuppets) {
                mmac.removePuppetAggro(cid);
            }
            
            if (newController == null) {    // was a new controller found? (if not there's no puppet nearby)
                if (updateController) {
                    aggroUpdateController();
                }
                
                return;
            }
        } else if (chrController == newController) {
            this.aggroUpdatePuppetVisibility();
        }
        
        this.aggroSwitchController(newController, this.isControllerHasAggro());
    }
    
    /**
     * Ensures controllability removal of the current player controller, and
     * fetches for any player on the map to start controlling in place.
     * 
     */
    public void aggroRedirectController() {
        this.aggroRemoveController();   // don't care if new controller not found, at least remove current controller
        this.aggroUpdateController();
    }
    
    /**
     * Returns the current aggro status on the specified player, or null if the
     * specified player is currently not this mob's controller.
     * 
     */
    public Boolean aggroMoveLifeUpdate(MapleCharacter player) {
        MapleCharacter chrController = getController();
        if (chrController != null && player.getId() == chrController.getId()) {
            boolean aggro = this.isControllerHasAggro();
            if (aggro) {
                this.setControllerKnowsAboutAggro(true);
            }
            
            return aggro;
        } else {
            return null;
        }
    }
    
    /**
     * Refreshes auto aggro for the player passed as parameter, does nothing if
     * there is already an active controller for this mob.
     * 
     */
    public void aggroAutoAggroUpdate(MapleCharacter player) {
        MapleCharacter chrController = this.getActiveController();
        
        if (chrController == null) {
            this.aggroSwitchController(player, true);
        } else if (chrController.getId() == player.getId()) {
            this.setControllerHasAggro(true);
            if (!YamlConfig.config.server.USE_AUTOAGGRO_NEARBY) {   // thanks Lichtmager for noticing autoaggro not updating the player properly
                aggroMonsterControl(player.getClient(), this, true);
            }
        }
    }
    
    /**
     * Applied damage input for this mob, enough damage taken implies an aggro
     * target update for the attacker shortly.
     * 
     */
    public void aggroMonsterDamage(MapleCharacter attacker, int damage) {
        MapleMonsterAggroCoordinator mmac = this.getMapAggroCoordinator();
        mmac.addAggroDamage(this, attacker.getId(), damage);
        
        MapleCharacter chrController = this.getController();    // aggro based on DPS rather than first-come-first-served, now live after suggestions thanks to MedicOP, Thora, Vcoc
        if (chrController != attacker) {
            if (this.getMapAggroCoordinator().isLeadingCharacterAggro(this, attacker)) {
                this.aggroSwitchController(attacker, true);
            } else {
                this.setControllerHasAggro(true);
                this.aggroUpdatePuppetVisibility();
            }
            
            /*
            For some reason, some mobs loses aggro on controllers if other players also attacks them.
            Maybe Nexon intended to interchange controllers at every attack...
            
            else if (chrController != null) {
                chrController.announce(MaplePacketCreator.stopControllingMonster(this.getObjectId()));
                aggroMonsterControl(chrController.getClient(), this, true);
            }
            */
        } else {
            this.setControllerHasAggro(true);
            this.aggroUpdatePuppetVisibility();
        }
    }
    
    private static void aggroMonsterControl(MapleClient c, MapleMonster mob, boolean immediateAggro) {
        c.announce(MaplePacketCreator.controlMonster(mob, false, immediateAggro));
    }
    
    private void aggroRefreshPuppetVisibility(MapleCharacter chrController, MapleSummon puppet) {
        // lame patch for client to redirect all aggro to the puppet
        
        List<MapleMonster> puppetControlled = new LinkedList<>();
        for (MapleMonster mob : chrController.getControlledMonsters()) {
            if (mob.isPuppetInVicinity(puppet)) {
                puppetControlled.add(mob);
            }
        }
        
        for (MapleMonster mob : puppetControlled) {
            chrController.announce(MaplePacketCreator.stopControllingMonster(mob.getObjectId()));
        }
        chrController.announce(MaplePacketCreator.removeSummon(puppet, false));
        
        MapleClient c = chrController.getClient();
        for (MapleMonster mob : puppetControlled) { // thanks BHB for noticing puppets disrupting mobstatuses for bowmans
            aggroMonsterControl(c, mob, mob.isControllerKnowsAboutAggro());
        }
        chrController.announce(MaplePacketCreator.spawnSummon(puppet, false));
    }
    
    public void aggroUpdatePuppetVisibility() {
        if (!availablePuppetUpdate) {
            return;
        }
        
        availablePuppetUpdate = false;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    MapleCharacter chrController = MapleMonster.this.getActiveController();
                    if (chrController == null) {
                        return;
                    }

                    MapleStatEffect puppetEffect = chrController.getBuffEffect(MapleBuffStat.PUPPET);
                    if (puppetEffect != null) {
                        MapleSummon puppet = chrController.getSummonByKey(puppetEffect.getSourceId());

                        if (puppet != null && isPuppetInVicinity(puppet)) {
                            controllerHasPuppet = true;
                            aggroRefreshPuppetVisibility(chrController, puppet);
                            return;
                        }
                    }

                    if (controllerHasPuppet) {
                        controllerHasPuppet = false;

                        chrController.announce(MaplePacketCreator.stopControllingMonster(MapleMonster.this.getObjectId()));
                        aggroMonsterControl(chrController.getClient(), MapleMonster.this, MapleMonster.this.isControllerHasAggro());
                    }
                } finally {
                    availablePuppetUpdate = true;
                }
            }
        };
        
        // had to schedule this since mob wouldn't stick to puppet aggro who knows why
        OverallService service = (OverallService) this.getMap().getChannelServer().getServiceAccess(ChannelServices.OVERALL);
        service.registerOverallAction(this.getMap().getId(), r, YamlConfig.config.server.UPDATE_INTERVAL);
    }
    
    /**
     * Clears all applied damage input for this mob, doesn't refresh target
     * aggro.
     * 
     */
    public void aggroClearDamages() {
        this.getMapAggroCoordinator().removeAggroEntries(this);
    }

    /**
     * Clears this mob aggro on the current controller.
     * 
     */
    public void aggroResetAggro() {
        aggroUpdateLock.lock();
        try {
            this.setControllerHasAggro(false);
            this.setControllerKnowsAboutAggro(false);
        } finally {
            aggroUpdateLock.unlock();
        }
    }
    
    public final int getRemoveAfter() {
        return stats.removeAfter();
    }
    
    public void dispose() {
        if (monsterItemDrop != null) {
            monsterItemDrop.cancel(false);
        }
        
        this.getMap().dismissRemoveAfter(this);
        disposeLocks();
    }
    
    private void disposeLocks() {
        LockCollector.getInstance().registerDisposeAction(new Runnable() {
            @Override
            public void run() {
                emptyLocks();
            }
        });
    }
    
    private void emptyLocks() {
        externalLock = externalLock.dispose();
        monsterLock = monsterLock.dispose();
        statiLock = statiLock.dispose();
        animationLock = animationLock.dispose();
    }
}