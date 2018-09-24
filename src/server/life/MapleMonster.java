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
import client.MapleJob;
import client.Skill;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ServerConstants;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.Server;
import net.server.channel.Channel;
import net.server.world.World;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import server.TimerManager;
import server.life.MapleLifeFactory.BanishInfo;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

public class MapleMonster extends AbstractLoadedMapleLife {
    private ChangeableStats ostats = null;  //unused, v83 WZs offers no support for changeable stats.
    private MapleMonsterStats stats;
    private AtomicInteger hp = new AtomicInteger(1);
    private AtomicLong maxHpPlusHeal = new AtomicLong(1);
    private int mp;
    private WeakReference<MapleCharacter> controller = new WeakReference<>(null);
    private boolean controllerHasAggro, controllerKnowsAboutAggro;
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
    private List<Integer> stolenItems = new ArrayList<>();
    private int team;
    private int parentMobOid = 0;
    private final HashMap<Integer, AtomicInteger> takenDamage = new HashMap<>();

    private MonitoredReentrantLock externalLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_EXT);
    private MonitoredReentrantLock monsterLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB, true);
    private MonitoredReentrantLock statiLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_STATI);
    private MonitoredReentrantLock animationLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_ANI);

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

    private void initWithStats(MapleMonsterStats stats) {
        setStance(5);
        this.stats = stats;
        hp.set(stats.getHp());
        mp = stats.getMp();
        
        maxHpPlusHeal.set(hp.get());
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

    public int getHp() {
        return hp.get();
    }
    
    public synchronized void addHp(int hp) {
        if(this.hp.get() <= 0) return;
        this.hp.addAndGet(hp);
    }
    
    public void setStartingHp(int hp) {
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
        if(!animationLock.tryLock()) return false;
    
        try {
            long animationTime;
        
            if(skill == null) {
                animationTime = MapleMonsterInformationProvider.getInstance().getMobAttackAnimationTime(this.getId(), attackPos);
            } else {
                animationTime = MapleMonsterInformationProvider.getInstance().getMobSkillAnimationTime(skill);
            }

            if(animationTime > 0) {
                return map.getChannelServer().registerMobOnAnimationEffect(map.getId(), this.hashCode(), animationTime);
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
            if(stayAlive) curHp--;
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
                this.applyDamage(attacker, damage, stayAlive);
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
    private void applyDamage(MapleCharacter from, int damage, boolean stayAlive) {
        Integer trueDamage = applyAndGetHpDamage(damage, stayAlive);
        if (trueDamage == null) {
            return;
        }
        
        if(ServerConstants.USE_DEBUG) from.dropMessage(5, "Hitted MOB " + this.getId() + ", OID " + this.getObjectId());
        dispatchMonsterDamaged(from, trueDamage);

        if (!takenDamage.containsKey(from.getId())) {
            takenDamage.put(from.getId(), new AtomicInteger(trueDamage));
        } else {
            takenDamage.get(from.getId()).addAndGet(trueDamage);
        }

        broadcastMobHpBar(from);
    }
    
    public void heal(int hp, int mp) {
        Integer hpHealed = applyAndGetHpDamage(-hp, false);
        if(hpHealed == null) return;
        
        int mp2Heal = getMp() + mp;
        int maxMp = getMaxMp();
        if (mp2Heal >= maxMp) {
            mp2Heal = maxMp;
        }
        setMp(mp2Heal);
        
        if(hp > 0) getMap().broadcastMessage(MaplePacketCreator.healMonster(getObjectId(), hp, getHp(), getMaxHp()));
        
        maxHpPlusHeal.addAndGet(hpHealed);
        dispatchMonsterHealed(hpHealed);
    }

    public boolean isAttackedBy(MapleCharacter chr) {
        return takenDamage.containsKey(chr.getId());
    }

    private void distributeExperienceToParty(int pid, float exp, int killer, Set<MapleCharacter> underleveled, int minThresholdLevel) {
        List<MapleCharacter> members = new LinkedList<>();
        MapleCharacter pchar = getMap().getAnyCharacterFromParty(pid);
        if(pchar != null) {
            for(MapleCharacter chr : pchar.getPartyMembersOnSameMap()) {
                members.add(chr);
            }
        } else {
            MapleCharacter chr = getMap().getCharacterById(killer);
            if(chr == null) return;
            
            members.add(chr);
        }
            
        int partyLevel = 0;
        int leechCount = 0;
        for (MapleCharacter mc : members) {
            if (mc.getLevel() >= minThresholdLevel) {    //NO EXP WILL BE GIVEN for those who are underleveled!
                partyLevel += mc.getLevel();
                leechCount++;
            } else {
                underleveled.add(mc);
            }
        }

        final int mostDamageCid = getHighestDamagerId();

        for (MapleCharacter mc : members) {
            int id = mc.getId();
            int level = mc.getLevel();
            if (level >= minThresholdLevel) {
                boolean isKiller = killer == id;
                boolean mostDamage = mostDamageCid == id;
                float xp = ((0.80f * exp * level) / partyLevel);
                if (mostDamage) {
                    xp += (0.20f * exp);
                }
                giveExpToCharacter(mc, xp, isKiller, leechCount);
            }
        }
    }

    private int calcThresholdLevel(boolean isPqMob) {
        if(isPqMob || !ServerConstants.USE_ENFORCE_MOB_LEVEL_RANGE) {
            return 0;
        } else {
            return getLevel() - (!isBoss() ? ServerConstants.MIN_UNDERLEVEL_TO_EXP_GAIN : 2 * ServerConstants.MIN_UNDERLEVEL_TO_EXP_GAIN);
        }
    }
    
    private void distributeExperience(int killerId) {
        if (isAlive()) {
            return;
        }
        
        EventInstanceManager eim = getMap().getEventInstance();
        int minThresholdLevel = calcThresholdLevel(eim != null);
        int exp = getExp();
        long totalHealth = maxHpPlusHeal.get();
        Map<Integer, Float> expDist = new HashMap<>();
        Map<Integer, Float> partyExp = new HashMap<>();
        
        float exp8perHp = (0.8f * exp) / totalHealth;   // 80% of pool is split amongst all the damagers
        float exp2 = (0.2f * exp);                      // 20% of pool goes to the killer or his/her party
        
        for (Entry<Integer, AtomicInteger> damage : takenDamage.entrySet()) {
            expDist.put(damage.getKey(), exp8perHp * damage.getValue().get());
        }
        
        Set<MapleCharacter> underleveled = new HashSet<>();
        Collection<MapleCharacter> mapChrs = map.getCharacters();
        for (MapleCharacter mc : mapChrs) {
            Float mcExp = expDist.remove(mc.getId());
            if (mcExp != null) {
                boolean isKiller = (mc.getId() == killerId);
                if (isKiller) {
                    if (eim != null) {
                        eim.monsterKilled(mc, this);
                    }
                }
                
                float xp = mcExp;
                if (isKiller) {
                    xp += exp2;
                }
                
                MapleParty p = mc.getParty();
                if (p != null) {
                    int pID = p.getId();
                    float pXP = xp + (partyExp.containsKey(pID) ? partyExp.get(pID) : 0);
                    partyExp.put(pID, pXP);
                } else {
                    if(mc.getLevel() >= minThresholdLevel) {
                        //NO EXP WILL BE GIVEN for those who are underleveled!
                        giveExpToCharacter(mc, xp, isKiller, 1);
                    } else {
                        underleveled.add(mc);
                    }
                }
            }
        }
        
        if(!expDist.isEmpty()) {    // locate on world server the partyid of the missing characters
            World wserv = map.getWorldServer();
            
            for (Entry<Integer, Float> ed : expDist.entrySet()) {
                boolean isKiller = (ed.getKey() == killerId);
                float xp = ed.getValue();
                if (isKiller) {
                    xp += exp2;
                }

                Integer pID = wserv.getCharacterPartyid(ed.getKey());
                if (pID != null) {
                    float pXP = xp + (partyExp.containsKey(pID) ? partyExp.get(pID) : 0);
                    partyExp.put(pID, pXP);
                }
            }
        }
        
        for (Entry<Integer, Float> party : partyExp.entrySet()) {
            distributeExperienceToParty(party.getKey(), party.getValue(), killerId, underleveled, minThresholdLevel);
        }
        
        for(MapleCharacter mc : underleveled) {
            mc.showUnderleveledInfo(this);
        }
    }

    private void giveExpToCharacter(MapleCharacter attacker, float exp, boolean isKiller, int numExpSharers) {
        //PARTY BONUS: 2p -> +2% , 3p -> +4% , 4p -> +6% , 5p -> +8% , 6p -> +10%
        final float partyModifier = numExpSharers <= 1 ? 0.0f : 0.02f * (numExpSharers - 1);
        
        int partyExp = 0;
        if (attacker.getHp() > 0) {
            exp *= attacker.getExpRate();
            int personalExp = (int) exp;

            if (exp <= Integer.MAX_VALUE) {  // assuming no negative xp here
                if (partyModifier > 0.0f) {
                    partyExp = (int) (personalExp * partyModifier * ServerConstants.PARTY_BONUS_EXP_RATE);
                }
                Integer holySymbol = attacker.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
                if (holySymbol != null) {
                    personalExp *= 1.0 + (holySymbol.doubleValue() / 100.0);
                }

                statiLock.lock();
                try {
                    if (stati.containsKey(MonsterStatus.SHOWDOWN)) {
                        personalExp *= (stati.get(MonsterStatus.SHOWDOWN).getStati().get(MonsterStatus.SHOWDOWN).doubleValue() / 100.0 + 1.0);
                    }
                } finally {
                    statiLock.unlock();
                }
            } else {
                personalExp = Integer.MAX_VALUE;
            }
            
            attacker.gainExp(personalExp, partyExp, true, false, isKiller);
            attacker.increaseEquipExp(personalExp);
            attacker.updateQuestMobCount(getId());
        }
    }

    public MapleCharacter killBy(final MapleCharacter killer) {
        distributeExperience(killer != null ? killer.getId() : 0);

        MapleCharacter chrController = getController();
        if (chrController != null) { // this can/should only happen when a hidden gm attacks the monster
            chrController.announce(MaplePacketCreator.stopControllingMonster(this.getObjectId()));
            chrController.stopControllingMonster(this);
        }

        final List<Integer> toSpawn = this.getRevives(); // this doesn't work (?)
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

                if (timeMob.getLeft() == 9300338 && (reviveMap.getId() >= 922240100 && reviveMap.getId() <= 922240119)) {
                    if (!reviveMap.containsNPC(9001108)) {
                        MapleNPC npc = MapleLifeFactory.getNPC(9001108);
                        npc.setPosition(new Point(172, 9));
                        npc.setCy(9);
                        npc.setRx0(172 + 50);
                        npc.setRx1(172 - 50);
                        npc.setFh(27);
                        reviveMap.addMapObject(npc);
                        reviveMap.broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                    } else {
                        reviveMap.toggleHiddenNPC(9001108);
                    }
                }
            }
            
            if(toSpawn.size() > 0) {
                final EventInstanceManager eim = this.getMap().getEventInstance();
                
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        for (Integer mid : toSpawn) {
                            final MapleMonster mob = MapleLifeFactory.getMonster(mid);
                            mob.setPosition(getPosition());
                            mob.setFh(getFh());
                            mob.setParentMobOid(getObjectId());
                            
                            if (dropsDisabled()) {
                                mob.disableDrops();
                            }
                            reviveMap.spawnMonster(mob);

                            if(mob.getId() >= 8810010 && mob.getId() <= 8810017 && reviveMap.isHorntailDefeated()) {
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
                                        ht.broadcastMobHpBar(killer);
                                    }
                                }
                                
                                for(int i = 8810017; i >= 8810010; i--)
                                    reviveMap.killMonster(reviveMap.getMonsterById(i), killer, true);
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
    
    private void dispatchUpdateQuestMobCount() {
        Set<Integer> attackerChrids = takenDamage.keySet();
        if(!attackerChrids.isEmpty()) {
            Map<Integer, MapleCharacter> mapChars = map.getMapPlayers();
            if(!mapChars.isEmpty()) {
                int mobid = getId();
                
                for (Integer chrid : attackerChrids) {
                    MapleCharacter chr = mapChars.get(chrid);

                    if(chr != null && chr.isLoggedinWorld()) {
                        chr.updateQuestMobCount(mobid);
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
        if(!hasKiller) {
            dispatchUpdateQuestMobCount();
        }
        
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

    public int getHighestDamagerId() {
        int curId = 0;
        int curDmg = 0;

        for (Entry<Integer, AtomicInteger> damage : takenDamage.entrySet()) {
            curId = damage.getValue().get() >= curDmg ? damage.getKey() : curId;
            curDmg = damage.getKey() == curId ? damage.getValue().get() : curDmg;
        }

        return curId;
    }

    public boolean isAlive() {
        return this.hp.get() > 0;
    }

    public MapleCharacter getController() {
        monsterLock.lock();
        try {
            return controller.get();
        } finally {
            monsterLock.unlock();
        }
    }

    public void setController(MapleCharacter controller) {
        monsterLock.lock();
        try {
            this.controller = new WeakReference<>(controller);
        } finally {
            monsterLock.unlock();
        }
    }

    public void switchController(MapleCharacter newController, boolean immediateAggro) {
        MapleCharacter controllers = getController();
        if (controllers == newController) {
            return;
        }
        if (controllers != null) {
            controllers.stopControllingMonster(this);
            controllers.getClient().announce(MaplePacketCreator.stopControllingMonster(getObjectId()));
        }
        newController.controlMonster(this, immediateAggro);
        setController(newController);
        if (immediateAggro) {
            setControllerHasAggro(true);
        }
        setControllerKnowsAboutAggro(false);
    }

    public void addListener(MonsterListener listener) {
        statiLock.lock();
        try {
            listeners.add(listener);
        } finally {
            statiLock.unlock();
        }
    }

    public boolean isControllerHasAggro() {
        monsterLock.lock();
        try {
            return fake ? false : controllerHasAggro;
        } finally {
            monsterLock.unlock();
        }
    }

    public void setControllerHasAggro(boolean controllerHasAggro) {
        monsterLock.lock();
        try {
            if (fake) {
                return;
            }
            this.controllerHasAggro = controllerHasAggro;
        } finally {
            monsterLock.unlock();
        }
    }

    public boolean isControllerKnowsAboutAggro() {
        monsterLock.lock();
        try {
            return fake ? false : controllerKnowsAboutAggro;
        } finally {
            monsterLock.unlock();
        }
    }

    public void setControllerKnowsAboutAggro(boolean controllerKnowsAboutAggro) {
        monsterLock.lock();
        try {
            if (fake) {
                return;
            }
            this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
        } finally {
            monsterLock.unlock();
        }
    }

    public byte[] makeBossHPBarPacket() {
        return MaplePacketCreator.showBossHP(getId(), getHp(), getMaxHp(), getTagColor(), getTagBgColor());
    }

    public boolean hasBossHPBar() {
        return isBoss() && getTagColor() > 0;
    }
    
    @Override
    public void sendSpawnData(MapleClient c) {
        if (!isAlive()) {
            return;
        }
        if (isFake()) {
            c.announce(MaplePacketCreator.spawnFakeMonster(this, 0));
        } else {
            c.announce(MaplePacketCreator.spawnMonster(this, false));
        }
        statiLock.lock();
        try {
            if (stati.size() > 0) {
                for (final MonsterStatusEffect mse : this.stati.values()) {
                    c.announce(MaplePacketCreator.applyMonsterStatus(getObjectId(), mse, null));
                }
            }
        } finally {
            statiLock.unlock();
        }
        
        if (hasBossHPBar()) {
            c.announceBossHpBar(this, this.hashCode(), makeBossHPBarPacket());
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.killMonster(getObjectId(), false));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MONSTER;
    }

    public boolean isMobile() {
        return stats.isMobile();
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

    private int broadcastStatusEffect(final MonsterStatusEffect status) {
        int animationTime = status.getSkill().getAnimationTime();
        byte[] packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), status, null);
        map.broadcastMessage(packet, getPosition());
        
        MapleCharacter chrController = getController();
        if (chrController != null && !chrController.isMapObjectVisible(this)) {
            chrController.getClient().announce(packet);
        }
        
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
                            ch.interruptMobStatus(mapid, oldEffect);
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
                    map.broadcastMessage(packet, getPosition());
                    
                    MapleCharacter controller = getController();
                    if (controller != null && !controller.isMapObjectVisible(MapleMonster.this)) {
                        controller.getClient().announce(packet);
                    }
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
        
        ch.registerMobStatus(mapid, status, cancelTask, duration + animationTime - 100, overtimeAction, overtimeDelay);
        return true;
    }

    public void applyMonsterBuff(final Map<MonsterStatus, Integer> stats, final int x, int skillId, long duration, MobSkill skill, final List<Integer> reflection) {
        final Runnable cancelTask = new Runnable() {

            @Override
            public void run() {
                if (isAlive()) {
                    byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), stats);
                    map.broadcastMessage(packet, getPosition());
                    
                    MapleCharacter controller = getController();
                    if (controller != null && !controller.isMapObjectVisible(MapleMonster.this)) {
                        controller.getClient().announce(packet);
                    }
                    
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
        map.broadcastMessage(packet, getPosition());
        
        statiLock.lock();
        try {
            for (MonsterStatus stat : stats.keySet()) {
                stati.put(stat, effect);
                alreadyBuffed.add(stat);
            }
        } finally {
            statiLock.unlock();
        }
        
        MapleCharacter controller = getController();
        if (controller != null && !controller.isMapObjectVisible(this)) {
            controller.getClient().announce(packet);
        }
        
        map.getChannelServer().registerMobStatus(map.getId(), effect, cancelTask, duration);
    }

    private void debuffMobStat(MonsterStatus stat) {
        statiLock.lock();
        try {
            if (isBuffed(stat)) {
                final MonsterStatusEffect oldEffect = stati.get(stat);
                byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), oldEffect.getStati());
                map.broadcastMessage(packet, getPosition());

                MapleCharacter chrController = getController();
                if (chrController != null && !chrController.isMapObjectVisible(MapleMonster.this)) {
                    chrController.getClient().announce(packet);
                }
                stati.remove(stat);
            }
        } finally {
            statiLock.unlock();
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

                if(ServerConstants.USE_ANTI_IMMUNITY_CRASH) {
                    if (skillid == Crusader.ARMOR_CRASH) {
                        if(!isBuffed(MonsterStatus.WEAPON_REFLECT)) debuffMobStat(MonsterStatus.WEAPON_IMMUNITY);
                        if(!isBuffed(MonsterStatus.MAGIC_REFLECT)) debuffMobStat(MonsterStatus.MAGIC_IMMUNITY);
                    } else if (skillid == WhiteKnight.MAGIC_CRASH) {
                        if(!isBuffed(MonsterStatus.MAGIC_REFLECT)) debuffMobStat(MonsterStatus.MAGIC_IMMUNITY);
                    } else {
                        if(!isBuffed(MonsterStatus.WEAPON_REFLECT)) debuffMobStat(MonsterStatus.WEAPON_IMMUNITY);
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

    public List<Pair<Integer, Integer>> getSkills() {
        return stats.getSkills();
    }

    public boolean hasSkill(int skillId, int level) {
        return stats.hasSkill(skillId, level);
    }
    
    public boolean canUseSkill(MobSkill toUse) {
        if (toUse == null) {
            return false;
        }
        
        if (toUse.getSkillId() == 200) {
            int i = 0;
            for (MapleMapObject mo : getMap().getMapObjects()) {
                if (mo.getType() == MapleMapObjectType.MONSTER) {
                    i++;
                }
            }
            if (i > 100) {
                return false;
            }
        }
        
        if (toUse.getLimit() > 0) {
            monsterLock.lock();
            try {
                Integer times = this.skillsUsed.get(new Pair<>(toUse.getSkillId(), toUse.getSkillLevel()));
                if (times != null && times >= toUse.getLimit()) {
                    return false;
                }
            } finally {
                monsterLock.unlock();
            }
        }
        
        monsterLock.lock();
        try {
            for (Pair<Integer, Integer> skill : usedSkills) {
                if (skill.getLeft() == toUse.getSkillId() && skill.getRight() == toUse.getSkillLevel()) {
                    return false;
                }
            }
            
            int mpCon = toUse.getMpCon();
            if (mp < mpCon) {
                return false;
            }
            
            if (!this.applyAnimationIfRoaming(-1, toUse)) {
                return false;
            }
            
            this.usedSkill(toUse);
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
            
            this.usedSkills.add(new Pair<>(skillId, level));
            if (this.skillsUsed.containsKey(new Pair<>(skillId, level))) {
                int times = this.skillsUsed.get(new Pair<>(skillId, level)) + 1;
                this.skillsUsed.remove(new Pair<>(skillId, level));
                this.skillsUsed.put(new Pair<>(skillId, level), times);
            } else {
                this.skillsUsed.put(new Pair<>(skillId, level), 1);
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
        
        mmap.getChannelServer().registerMobClearSkillAction(mmap.getId(), r, cooltime);
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
    
    public int canUseAttack(int attackPos) {
        monsterLock.lock();
        try {
            if (usedAttacks.contains(attackPos)) {
                return -1;
            }
            
            Pair<Integer, Integer> attackInfo = MapleMonsterInformationProvider.getInstance().getMobAttackInfo(this.getId(), attackPos);
            if (attackInfo == null) {
                return usedAttacks.isEmpty() ? 0 : -1;
            }
            
            int mpCon = attackInfo.getLeft();
            if (mp < mpCon) {
                return -1;
            }
            
            if (!this.applyAnimationIfRoaming(attackPos, null)) {
                return -1;
            }
            
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

            mmap.getChannelServer().registerMobClearSkillAction(mmap.getId(), r, cooltime);
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
                map.getChannelServer().interruptMobStatus(map.getId(), status);
                return;
            }
            
            int damage = dealDamage;
            if (damage >= curHp) {
                damage = curHp - 1;
                if (type == 1 || type == 2) {
                    map.getChannelServer().interruptMobStatus(map.getId(), status);
                }
            }
            if (damage > 0) {
                lockMonster();
                try {
                    applyDamage(chr, damage, true);
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
                
                mmap.getChannelServer().registerMobClearSkillAction(mmap.getId(), r, milli);
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
            return Collections.unmodifiableMap(stati);
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
            case 6: return(7.7f);
            case 5: return(5.6f);
            case 4: return(3.2f);
            case 3: return(2.1f);
            case 2: return(1.4f);
        }
        
        return(1.0f);
    }
    
    private void changeLevelByDifficulty(final int difficulty, boolean pqMob) {
        changeLevel((int)(this.getLevel() * getDifficultyRate(difficulty)), pqMob);
    }
    
    public final void changeDifficulty(final int difficulty, boolean pqMob) {
        changeLevelByDifficulty(difficulty, pqMob);
    }
    
    public final void disposeLocks() {
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
