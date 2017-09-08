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
import constants.skills.FPMage;
import constants.skills.Hermit;
import constants.skills.ILMage;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Shadower;
import constants.skills.SuperGM;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.TimerManager;
import server.life.MapleLifeFactory.BanishInfo;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

public class MapleMonster extends AbstractLoadedMapleLife {
    private ChangeableStats ostats = null;  //unused, v83 WZs offers no support for changeable stats.
    private MapleMonsterStats stats;
    private int hp, mp;
    private WeakReference<MapleCharacter> controller = new WeakReference<>(null);
    private boolean controllerHasAggro, controllerKnowsAboutAggro;
    private Collection<MonsterListener> listeners = new LinkedList<>();
    private EnumMap<MonsterStatus, MonsterStatusEffect> stati = new EnumMap<>(MonsterStatus.class);
    private ArrayList<MonsterStatus> alreadyBuffed = new ArrayList<MonsterStatus>();
    private MapleMap map;
    private int VenomMultiplier = 0;
    private boolean fake = false;
    private boolean dropsDisabled = false;
    private List<Pair<Integer, Integer>> usedSkills = new ArrayList<>();
    private Map<Pair<Integer, Integer>, Integer> skillsUsed = new HashMap<>();
    private List<Integer> stolenItems = new ArrayList<>();
    private int team;
    private final HashMap<Integer, AtomicInteger> takenDamage = new HashMap<>();

    private ReentrantLock monsterLock = new ReentrantLock();

    public MapleMonster(int id, MapleMonsterStats stats) {
        super(id);
        initWithStats(stats);
    }

    public MapleMonster(MapleMonster monster) {
        super(monster);
        initWithStats(monster.stats);
    }
    
    public void lockMonster() {
        monsterLock.lock();
    }
    
    public void unlockMonster() {
        monsterLock.unlock();
    }

    private void initWithStats(MapleMonsterStats stats) {
        setStance(5);
        this.stats = stats;
        hp = stats.getHp();
        mp = stats.getMp();
    }

    public void disableDrops() {
        this.dropsDisabled = true;
    }

    public boolean dropsDisabled() {
        return dropsDisabled;
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
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

    /**
     *
     * @param from the player that dealt the damage
     * @param damage
     */
    public synchronized void damage(MapleCharacter from, int damage) { // may be pointless synchronization
        if (!isAlive()) {
            return;
        }
        int trueDamage = Math.min(hp, damage); // since magic happens otherwise B^)
        
        if(ServerConstants.USE_DEBUG == true) from.dropMessage(5, "Hitted MOB " + this.getId() + ", OID " + this.getObjectId());
        dispatchMonsterDamaged(from, trueDamage);

        hp -= trueDamage;
        if (!takenDamage.containsKey(from.getId())) {
            takenDamage.put(from.getId(), new AtomicInteger(trueDamage));
        } else {
            takenDamage.get(from.getId()).addAndGet(trueDamage);
        }

        if (hasBossHPBar()) {
            from.setPlayerAggro(this.hashCode());
            from.getMap().broadcastBossHpMessage(this, this.hashCode(), makeBossHPBarPacket(), getPosition());
        } else if (!isBoss()) {
            int remainingHP = (int) Math.max(1, hp * 100f / getMaxHp());
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

    public void heal(int hp, int mp) {
        int hp2Heal = getHp() + hp;
        int mp2Heal = getMp() + mp;
        if (hp2Heal >= getMaxHp()) {
            hp2Heal = getMaxHp();
        }
        if (mp2Heal >= getMaxMp()) {
            mp2Heal = getMaxMp();
        }
        setHp(hp2Heal);
        setMp(mp2Heal);
        getMap().broadcastMessage(MaplePacketCreator.healMonster(getObjectId(), hp));
    }

    public boolean isAttackedBy(MapleCharacter chr) {
        return takenDamage.containsKey(chr.getId());
    }

    private void distributeExperienceToParty(int pid, int exp, int killer, Map<Integer, Integer> expDist) {
        LinkedList<MapleCharacter> members = new LinkedList<>();
        Collection<MapleCharacter> chrs = map.getCharacters();
        
        for (MapleCharacter mc : chrs) {
            if (mc.getPartyId() == pid) {
                members.add(mc);
            }
        }
        
        final int minLevel = getLevel() - 5;

        int partyLevel = 0;
        int leechMinLevel = 0;

        for (MapleCharacter mc : members) {
            if (mc.getLevel() >= minLevel) {
                leechMinLevel = Math.min(mc.getLevel() - 5, minLevel);
            }
        }

        int leechCount = 0;
        for (MapleCharacter mc : members) {
            if (mc.getLevel() >= leechMinLevel) {
                partyLevel += mc.getLevel();
                leechCount++;
            }
        }

        final int mostDamageCid = getHighestDamagerId();

        for (MapleCharacter mc : members) {
            int id = mc.getId();
            int level = mc.getLevel();
            if (expDist.containsKey(id)
                    || level >= leechMinLevel) {
                boolean isKiller = killer == id;
                boolean mostDamage = mostDamageCid == id;
                int xp = (int) (exp * 0.80f * level / partyLevel);
                if (mostDamage) {
                    xp += (exp * 0.20f);
                }
                giveExpToCharacter(mc, xp, isKiller, leechCount);
            }
        }
    }

    public void distributeExperience(int killerId) {
        if (isAlive()) {
            return;
        }
        int exp = getExp();
        int totalHealth = getMaxHp();
        Map<Integer, Integer> expDist = new HashMap<>();
        Map<Integer, Integer> partyExp = new HashMap<>();
        // 80% of pool is split amongst all the damagers
        for (Entry<Integer, AtomicInteger> damage : takenDamage.entrySet()) {
            expDist.put(damage.getKey(), (int) (0.80f * exp * damage.getValue().get() / totalHealth));
        }
        
        Collection<MapleCharacter> chrs = map.getCharacters();
        for (MapleCharacter mc : chrs) {
            if (expDist.containsKey(mc.getId())) {
                boolean isKiller = mc.getId() == killerId;
                int xp = expDist.get(mc.getId());
                if (isKiller) {
                    xp += exp / 5;
                }
                MapleParty p = mc.getParty();
                if (p != null) {
                    int pID = p.getId();
                    int pXP = xp + (partyExp.containsKey(pID) ? partyExp.get(pID) : 0);
                    partyExp.put(pID, pXP);
                } else {
                    giveExpToCharacter(mc, xp, isKiller, 1);
                }
            }
        }
        
        for (Entry<Integer, Integer> party : partyExp.entrySet()) {
            distributeExperienceToParty(party.getKey(), party.getValue(), killerId, expDist);
        }
    }

    public void giveExpToCharacter(MapleCharacter attacker, int exp, boolean isKiller, int numExpSharers) {
        if (isKiller) {
            if (getMap().getEventInstance() != null) {
                getMap().getEventInstance().monsterKilled(attacker, this);
            }
        }
        final int partyModifier = numExpSharers > 1 ? (110 + (5 * (numExpSharers - 2))) : 0;

        int partyExp = 0;

        if (attacker.getHp() > 0) {
            int personalExp = exp * attacker.getExpRate();

            if (exp > 0) {
                if (partyModifier > 0) {
                    partyExp = (int) (personalExp * ServerConstants.PARTY_EXPERIENCE_MOD * partyModifier / 1000f);
                }
                Integer holySymbol = attacker.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
                boolean GMHolySymbol = attacker.getBuffSource(MapleBuffStat.HOLY_SYMBOL) == SuperGM.HOLY_SYMBOL;
                if (holySymbol != null) {
                    if (numExpSharers == 1 && !GMHolySymbol) {
                        personalExp *= 1.0 + (holySymbol.doubleValue() / 500.0);
                    } else {
                        personalExp *= 1.0 + (holySymbol.doubleValue() / 100.0);
                    }
                }
                if (stati.containsKey(MonsterStatus.SHOWDOWN)) {
                    personalExp *= (stati.get(MonsterStatus.SHOWDOWN).getStati().get(MonsterStatus.SHOWDOWN).doubleValue() / 100.0 + 1.0);
                }
            }
            
            attacker.gainExp(personalExp, partyExp, true, false, isKiller);
            attacker.increaseEquipExp(personalExp);
            attacker.mobKilled(getId());
        }
    }

    public MapleCharacter killBy(final MapleCharacter killer) {
        distributeExperience(killer != null ? killer.getId() : 0);

        if (getController() != null) { // this can/should only happen when a hidden gm attacks the monster
            getController().getClient().announce(MaplePacketCreator.stopControllingMonster(this.getObjectId()));
            getController().stopControllingMonster(this);
        }

        final List<Integer> toSpawn = this.getRevives(); // this doesn't work (?)
        if (toSpawn != null) {
            final MapleMap reviveMap = killer.getMap();
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
            
            TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    for (Integer mid : toSpawn) {
                        final MapleMonster mob = MapleLifeFactory.getMonster(mid);
                        mob.setPosition(getPosition());
                        if (dropsDisabled()) {
                            mob.disableDrops();
                        }
                        reviveMap.spawnMonster(mob);
                        
                        if(mob.getId() >= 8810010 && mob.getId() <= 8810017 && reviveMap.isHorntailDefeated()) {
                            for(int i = 8810018; i >= 8810010; i--)
                                reviveMap.killMonster(reviveMap.getMonsterById(i), killer, true);
                        }
                    }
                }
            }, getAnimationTime("die1"));
        }
        else {  // is this even necessary?
            System.out.println("[CRITICAL LOSS] toSpawn is null for " + this.getName());
        }
        
        MapleCharacter looter = map.getCharacterById(getHighestDamagerId());
        return looter != null ? looter : killer;
    }
    
    public void dispatchMonsterKilled() {
        if (getMap().getEventInstance() != null) {
            if (!this.getStats().isFriendly()) {
                getMap().getEventInstance().monsterKilled(this);
            } else {
                getMap().getEventInstance().friendlyKilled(this);
            }
        }
        
        for (MonsterListener listener : listeners.toArray(new MonsterListener[listeners.size()])) {
            listener.monsterKilled(getAnimationTime("die1"));
        }
    }
    
    public void dispatchMonsterDamaged(MapleCharacter from, int trueDmg) {
        for (MonsterListener listener : listeners.toArray(new MonsterListener[listeners.size()])) {
            listener.monsterDamaged(from, trueDmg);
        }
    }

    // should only really be used to determine drop owner
    private int getHighestDamagerId() {
        int curId = 0;
        int curDmg = 0;

        for (Entry<Integer, AtomicInteger> damage : takenDamage.entrySet()) {
            curId = damage.getValue().get() >= curDmg ? damage.getKey() : curId;
            curDmg = damage.getKey() == curId ? damage.getValue().get() : curDmg;
        }

        return curId;
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public MapleCharacter getController() {
        return controller.get();
    }

    public void setController(MapleCharacter controller) {
        this.controller = new WeakReference<>(controller);
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
        listeners.add(listener);
    }

    public boolean isControllerHasAggro() {
        return fake ? false : controllerHasAggro;
    }

    public void setControllerHasAggro(boolean controllerHasAggro) {
        if (fake) {
            return;
        }
        this.controllerHasAggro = controllerHasAggro;
    }

    public boolean isControllerKnowsAboutAggro() {
        return fake ? false : controllerKnowsAboutAggro;
    }

    public void setControllerKnowsAboutAggro(boolean controllerKnowsAboutAggro) {
        if (fake) {
            return;
        }
        this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
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
        if (stati.size() > 0) {
            for (final MonsterStatusEffect mse : this.stati.values()) {
                c.announce(MaplePacketCreator.applyMonsterStatus(getObjectId(), mse, null));
            }
        }
        if (hasBossHPBar()) {
            if (this.getMap().countMonster(8810026) > 0 && this.getMap().getId() == 240060200) {
                this.getMap().killAllMonsters();
                return;
            }
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

    public ElementalEffectiveness getEffectiveness(Element e) {
        if (stati.size() > 0 && stati.get(MonsterStatus.DOOM) != null) {
            return ElementalEffectiveness.NORMAL; // like blue snails
        }
        return stats.getEffectiveness(e);
    }

    public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status, boolean poison, long duration) {
        return applyStatus(from, status, poison, duration, false);
    }

    public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status, boolean poison, long duration, boolean venom) {
        switch (stats.getEffectiveness(status.getSkill().getElement())) {
            case IMMUNE:
            case STRONG:
            case NEUTRAL:
                return false;
            case NORMAL:
            case WEAK:
                break;
            default: {
                System.out.println("Unknown elemental effectiveness: " + stats.getEffectiveness(status.getSkill().getElement()));
                return false;
            }
        }

        if (status.getSkill().getId() == FPMage.ELEMENT_COMPOSITION) { // fp compo
            ElementalEffectiveness effectiveness = stats.getEffectiveness(Element.POISON);
            if (effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) {
                return false;
            }
        } else if (status.getSkill().getId() == ILMage.ELEMENT_COMPOSITION) { // il compo
            ElementalEffectiveness effectiveness = stats.getEffectiveness(Element.ICE);
            if (effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) {
                return false;
            }
        } else if (status.getSkill().getId() == NightLord.VENOMOUS_STAR || status.getSkill().getId() == Shadower.VENOMOUS_STAB || status.getSkill().getId() == NightWalker.VENOM) {// venom
            if (stats.getEffectiveness(Element.POISON) == ElementalEffectiveness.WEAK) {
                return false;
            }
        }
        if (poison && getHp() <= 1) {
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

        for (MonsterStatus stat : statis.keySet()) {
            final MonsterStatusEffect oldEffect = stati.get(stat);
            if (oldEffect != null) {
                oldEffect.removeActiveStatus(stat);
                if (oldEffect.getStati().isEmpty()) {
                    oldEffect.cancelTask();
                    oldEffect.cancelDamageSchedule();
                }
            }
        }

        TimerManager timerManager = TimerManager.getInstance();
        final Runnable cancelTask = new Runnable() {

            @Override
            public void run() {
                if (isAlive()) {
                    byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), status.getStati());
                    map.broadcastMessage(packet, getPosition());
                    if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                        getController().getClient().announce(packet);
                    }
                }
                for (MonsterStatus stat : status.getStati().keySet()) {
                    stati.remove(stat);
                }
                setVenomMulti(0);
                status.cancelDamageSchedule();
            }
        };
        if (poison) {
            int poisonLevel = from.getSkillLevel(status.getSkill());
            int poisonDamage = Math.min(Short.MAX_VALUE, (int) (getMaxHp() / (70.0 - poisonLevel) + 0.999));
            status.setValue(MonsterStatus.POISON, Integer.valueOf(poisonDamage));
            status.setDamageSchedule(timerManager.register(new DamageTask(poisonDamage, from, status, cancelTask, 0), 1000, 1000));
        } else if (venom) {
            if (from.getJob() == MapleJob.NIGHTLORD || from.getJob() == MapleJob.SHADOWER || from.getJob().isA(MapleJob.NIGHTWALKER3)) {
                int poisonLevel, matk, id = from.getJob().getId();
                int skill = (id == 412 ? NightLord.VENOMOUS_STAR : (id == 422 ? Shadower.VENOMOUS_STAB : NightWalker.VENOM));
                poisonLevel = from.getSkillLevel(SkillFactory.getSkill(skill));
                if (poisonLevel <= 0) {
                    return false;
                }
                matk = SkillFactory.getSkill(skill).getEffect(poisonLevel).getMatk();
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
                status.setDamageSchedule(timerManager.register(new DamageTask(poisonDamage, from, status, cancelTask, 0), 1000, 1000));
            } else {
                return false;
            }

        } else if (status.getSkill().getId() == Hermit.SHADOW_WEB || status.getSkill().getId() == NightWalker.SHADOW_WEB) { //Shadow Web
            status.setDamageSchedule(timerManager.schedule(new DamageTask((int) (getMaxHp() / 50.0 + 0.999), from, status, cancelTask, 1), 3500));
        } else if (status.getSkill().getId() == 4121004 || status.getSkill().getId() == 4221004) { // Ninja Ambush
            final Skill skill = SkillFactory.getSkill(status.getSkill().getId());
            final byte level = from.getSkillLevel(skill);
            final int damage = (int) ((from.getStr() + from.getLuk()) * (1.5 + (level * 0.05)) * skill.getEffect(level).getDamage());
            /*if (getHp() - damage <= 1)  { make hp 1 betch
             damage = getHp() - (getHp() - 1);
             }*/

            status.setValue(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(damage));
            status.setDamageSchedule(timerManager.register(new DamageTask(damage, from, status, cancelTask, 2), 1000, 1000));
        }
        for (MonsterStatus stat : status.getStati().keySet()) {
            stati.put(stat, status);
            alreadyBuffed.add(stat);
        }
        int animationTime = status.getSkill().getAnimationTime();
        byte[] packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), status, null);
        map.broadcastMessage(packet, getPosition());
        if (getController() != null && !getController().isMapObjectVisible(this)) {
            getController().getClient().announce(packet);
        }
        status.setCancelTask(timerManager.schedule(cancelTask, duration + animationTime));
        return true;
    }

    public void applyMonsterBuff(final Map<MonsterStatus, Integer> stats, final int x, int skillId, long duration, MobSkill skill, final List<Integer> reflection) {
        TimerManager timerManager = TimerManager.getInstance();
        final Runnable cancelTask = new Runnable() {

            @Override
            public void run() {
                if (isAlive()) {
                    byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), stats);
                    map.broadcastMessage(packet, getPosition());
                    if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                        getController().getClient().announce(packet);
                    }
                    for (final MonsterStatus stat : stats.keySet()) {
                        stati.remove(stat);
                    }
                }
            }
        };
        final MonsterStatusEffect effect = new MonsterStatusEffect(stats, null, skill, true);
        byte[] packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), effect, reflection);
        map.broadcastMessage(packet, getPosition());
        for (MonsterStatus stat : stats.keySet()) {
            stati.put(stat, effect);
            alreadyBuffed.add(stat);
        }
        if (getController() != null && !getController().isMapObjectVisible(this)) {
            getController().getClient().announce(packet);
        }
        effect.setCancelTask(timerManager.schedule(cancelTask, duration));
    }

    public void debuffMob(int skillid) {
        //skillid is not going to be used for now until I get warrior debuff working
        MonsterStatus[] stats = {MonsterStatus.WEAPON_ATTACK_UP, MonsterStatus.WEAPON_DEFENSE_UP, MonsterStatus.MAGIC_ATTACK_UP, MonsterStatus.MAGIC_DEFENSE_UP};
        for (int i = 0; i < stats.length; i++) {
            if (isBuffed(stats[i])) {
                final MonsterStatusEffect oldEffect = stati.get(stats[i]);
                byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), oldEffect.getStati());
                map.broadcastMessage(packet, getPosition());
                if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                    getController().getClient().announce(packet);
                }
                stati.remove(stats);
            }
        }
    }

    public boolean isBuffed(MonsterStatus status) {
        return stati.containsKey(status);
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public boolean isFake() {
        return fake;
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
        for (Pair<Integer, Integer> skill : usedSkills) {
            if (skill.getLeft() == toUse.getSkillId() && skill.getRight() == toUse.getSkillLevel()) {
                return false;
            }
        }
        if (toUse.getLimit() > 0) {
            if (this.skillsUsed.containsKey(new Pair<>(toUse.getSkillId(), toUse.getSkillLevel()))) {
                int times = this.skillsUsed.get(new Pair<>(toUse.getSkillId(), toUse.getSkillLevel()));
                if (times >= toUse.getLimit()) {
                    return false;
                }
            }
        }
        if (toUse.getSkillId() == 200) {
            Collection<MapleMapObject> mmo = getMap().getMapObjects();
            int i = 0;
            for (MapleMapObject mo : mmo) {
                if (mo.getType() == MapleMapObjectType.MONSTER) {
                    i++;
                }
            }
            if (i > 100) {
                return false;
            }
        }
        return true;
    }

    public void usedSkill(final int skillId, final int level, long cooltime) {
        this.usedSkills.add(new Pair<>(skillId, level));
        if (this.skillsUsed.containsKey(new Pair<>(skillId, level))) {
            int times = this.skillsUsed.get(new Pair<>(skillId, level)) + 1;
            this.skillsUsed.remove(new Pair<>(skillId, level));
            this.skillsUsed.put(new Pair<>(skillId, level), times);
        } else {
            this.skillsUsed.put(new Pair<>(skillId, level), 1);
        }
        final MapleMonster mons = this;
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(
                new Runnable() {

                    @Override
                    public void run() {
                        mons.clearSkill(skillId, level);
                    }
                }, cooltime);
    }

    public void clearSkill(int skillId, int level) {
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
        private final Runnable cancelTask;
        private final int type;
        private final MapleMap map;

        private DamageTask(int dealDamage, MapleCharacter chr, MonsterStatusEffect status, Runnable cancelTask, int type) {
            this.dealDamage = dealDamage;
            this.chr = chr;
            this.status = status;
            this.cancelTask = cancelTask;
            this.type = type;
            this.map = chr.getMap();
        }

        @Override
        public void run() {
            int damage = dealDamage;
            if (damage >= hp) {
                damage = hp - 1;
                if (type == 1 || type == 2) {
                    map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
                    cancelTask.run();
                    status.getCancelTask().cancel(false);
                }
            }
            if (hp > 1 && damage > 0) {
                damage(chr, damage);
                if (type == 1) {
                    map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
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
        final Element fE = e;
        final ElementalEffectiveness fEE = stats.getEffectiveness(e);
        if (!stats.getEffectiveness(e).equals(ElementalEffectiveness.WEAK)) {
            stats.setEffectiveness(e, ee);
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    stats.removeEffectiveness(fE);
                    stats.setEffectiveness(fE, fEE);
                }
            }, milli);
        }
    }

    public Collection<MonsterStatus> alreadyBuffedStats() {
        return Collections.unmodifiableCollection(alreadyBuffed);
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
        return stati;
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
        this.hp = ostats.getHp();
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
        this.hp = ostats.getHp();
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
}
