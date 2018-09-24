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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import client.MapleCharacter;
import client.MapleDisease;
import client.status.MonsterStatus;
import constants.GameConstants;
import java.util.LinkedList;
import java.util.Map;
import tools.Randomizer;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import tools.ArrayMap;

/**
 *
 * @author Danny (Leifde)
 */
public class MobSkill {
    private int skillId, skillLevel, mpCon;
    private List<Integer> toSummon = new ArrayList<Integer>();
    private int spawnEffect, hp, x, y;
    private long duration, cooltime;
    private float prop;
    private Point lt, rb;
    private int limit;

    public MobSkill(int skillId, int level) {
        this.skillId = skillId;
        this.skillLevel = level;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public void addSummons(List<Integer> toSummon) {
        for (Integer summon : toSummon) {
            this.toSummon.add(summon);
        }
    }

    public void setSpawnEffect(int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCoolTime(long cooltime) {
        this.cooltime = cooltime;
    }

    public void setProp(float prop) {
        this.prop = prop;
    }

    public void setLtRb(Point lt, Point rb) {
        this.lt = lt;
        this.rb = rb;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void applyDelayedEffect(final MapleCharacter player, final MapleMonster monster, final boolean skill, final List<MapleCharacter> banishPlayers, int animationTime) {
        Runnable toRun = new Runnable() {
                            @Override
                            public void run() {
                                if(monster.isAlive()) {
                                    applyEffect(player, monster, skill, banishPlayers);
                                }
                            }
                        };
        
        monster.getMap().getChannelServer().registerOverallAction(monster.getMap().getId(), toRun, animationTime);
    }
    
    public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill, List<MapleCharacter> banishPlayers) {
        MapleDisease disease = null;
        Map<MonsterStatus, Integer> stats = new ArrayMap<MonsterStatus, Integer>();
        List<Integer> reflection = new LinkedList<Integer>();
        switch (skillId) {
            case 100:
            case 110:
            case 150:
                stats.put(MonsterStatus.WEAPON_ATTACK_UP, Integer.valueOf(x));
                break;
            case 101:
            case 111:
            case 151:
                stats.put(MonsterStatus.MAGIC_ATTACK_UP, Integer.valueOf(x));
                break;
            case 102:
            case 112:
            case 152:
                stats.put(MonsterStatus.WEAPON_DEFENSE_UP, Integer.valueOf(x));
                break;
            case 103:
            case 113:
            case 153:
                stats.put(MonsterStatus.MAGIC_DEFENSE_UP, Integer.valueOf(x));
                break;
	    case 114:
		if (lt != null && rb != null && skill) {
		    List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
		    final int hps = (getX() / 1000) * (int) (950 + 1050 * Math.random());
		    for (MapleMapObject mons : objects) {
			((MapleMonster) mons).heal(hps, getY());
		    }
		} else {
		    monster.heal(getX(), getY());
		}
		break;
	    case 120:
                disease = MapleDisease.SEAL;
		break;
	    case 121:
	    	disease = MapleDisease.DARKNESS;
		break;
	    case 122:
	    	disease = MapleDisease.WEAKEN;
		break;
	    case 123:
	    	disease = MapleDisease.STUN;
		break;
	    case 124:
	    	disease = MapleDisease.CURSE;
		break;
	    case 125:
	    	disease = MapleDisease.POISON;
		break;
	    case 126: // Slow
	    	disease = MapleDisease.SLOW;
		break;
	    case 127:
		if (lt != null && rb != null && skill) {
		    for (MapleCharacter character : getPlayersInRange(monster, player)) {
			character.dispel();
		    }
		} else {
		    player.dispel();
		}
		break;
	    case 128: // Seduce
	    	disease = MapleDisease.SEDUCE;
		break;
            case 129: // Banish
                if (lt != null && rb != null && skill) {
                    for (MapleCharacter chr : getPlayersInRange(monster, player)) {
                        banishPlayers.add(chr);
                    }
                } else {
                    banishPlayers.add(player);
                }
                break;
            case 131: // Mist
                monster.getMap().spawnMist(new MapleMist(calculateBoundingBox(monster.getPosition(), true), monster, this), x * 100, false, false, false);
                break;
            case 132:
                disease = MapleDisease.CONFUSE;
                break;
            case 133: // zombify
                disease = MapleDisease.ZOMBIFY;
                break;
            case 140:
                if (makeChanceResult() && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(x));
                }
                break;
            case 141:
                if (makeChanceResult() && !monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) {
                    stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(x));
                }
                break;
	    case 143: // Weapon Reflect
                    stats.put(MonsterStatus.WEAPON_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(x));
                    reflection.add(x);
		break;
	    case 144: // Magic Reflect
                    stats.put(MonsterStatus.MAGIC_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(x));
                    reflection.add(x);
		break;
	    case 145: // Weapon / Magic reflect
                    stats.put(MonsterStatus.WEAPON_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(x));
                    stats.put(MonsterStatus.MAGIC_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(x));
                    reflection.add(x);
                break;
            case 154: // accuracy up
            case 155: // avoid up
            case 156: // speed up
                break;
            case 200: // summon
                if (monster.getMap().getSpawnedMonstersOnMap() < 80) {
                    for (Integer mobId : getSummons()) {
                        MapleMonster toSpawn = MapleLifeFactory.getMonster(mobId);
                        if(toSpawn != null) {
                            if(GameConstants.isBossRush(monster.getMap().getId())) toSpawn.disableDrops();  // no littering on BRPQ pls
                            
                            toSpawn.setPosition(monster.getPosition());
                            int ypos, xpos;
                            xpos = (int) monster.getPosition().getX();
                            ypos = (int) monster.getPosition().getY();
                            switch (mobId) {
                                case 8500003: // Pap bomb high
                                    toSpawn.setFh((int) Math.ceil(Math.random() * 19.0));
                                    ypos = -590;
                                    break;
                                case 8500004: // Pap bomb
                                    xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
                                    if (ypos != -590) {
                                        ypos = (int) monster.getPosition().getY();
                                    }
                                    break;
                                case 8510100: //Pianus bomb
                                    if (Math.ceil(Math.random() * 5) == 1) {
                                        ypos = 78;
                                        xpos = (int) Randomizer.nextInt(5) + (Randomizer.nextInt(2) == 1 ? 180 : 0);
                                    } else {
                                        xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
                                    }
                                    break;          
                            }
                            switch (monster.getMap().getId()) {
                                case 220080001: //Pap map
                                    if (xpos < -890) {
                                        xpos = (int) (Math.ceil(Math.random() * 150) - 890);
                                    } else if (xpos > 230) {
                                        xpos = (int) (230 - Math.ceil(Math.random() * 150));
                                    }
                                    break;
                                case 230040420: // Pianus map
                                    if (xpos < -239) {
                                        xpos = (int) (Math.ceil(Math.random() * 150) - 239);
                                    } else if (xpos > 371) {
                                        xpos = (int) (371 - Math.ceil(Math.random() * 150));
                                    }
                                    break;
                            }
                            toSpawn.setPosition(new Point(xpos, ypos));
                            if (toSpawn.getId() == 8500004) {
                                    monster.getMap().spawnFakeMonster(toSpawn);
                            } else {
                                    monster.getMap().spawnMonsterWithEffect(toSpawn, getSpawnEffect(), toSpawn.getPosition());
                            }
                        }
                    }
                }
                break;
            default:
                System.out.println("Unhandled Mob skill: " + skillId);
                break;
        }
        if (stats.size() > 0) {
            if (lt != null && rb != null && skill) {
                for (MapleMapObject mons : getObjectsInRange(monster, MapleMapObjectType.MONSTER)) {
                    ((MapleMonster) mons).applyMonsterBuff(stats, getX(), getSkillId(), getDuration(), this, reflection);
                }
            } else {
                monster.applyMonsterBuff(stats, getX(), getSkillId(), getDuration(), this, reflection);
            }
        }
        if (disease != null) {
            if (lt != null && rb != null && skill) {
                int i = 0;
                for (MapleCharacter character : getPlayersInRange(monster, player)) {
                    if (!character.isActiveBuffedValue(2321005)) {  // holy shield
                        if (disease.equals(MapleDisease.SEDUCE)) {
                            if (i < 10) {
                                character.giveDebuff(MapleDisease.SEDUCE, this);
                                i++;
                            }
                        } else {
                            character.giveDebuff(disease, this);
                        }
                    }
                }
            } else {
                player.giveDebuff(disease, this);
            }
        }
    }

    private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player) {
        return monster.getMap().getPlayersInRange(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), Collections.singletonList(player));
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getMpCon() {
        return mpCon;
    }

    public List<Integer> getSummons() {
        return Collections.unmodifiableList(toSummon);
    }

    public int getSpawnEffect() {
        return spawnEffect;
    }

    public int getHP() {
        return hp;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getDuration() {
        return duration;
    }

    public long getCoolTime() {
        return cooltime;
    }

    public Point getLt() {
        return lt;
    }

    public Point getRb() {
        return rb;
    }

    public int getLimit() {
        return limit;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        int multiplier = facingLeft ? 1 : -1;
        Point mylt = new Point(lt.x * multiplier + posFrom.x, lt.y + posFrom.y);
        Point myrb = new Point(rb.x * multiplier + posFrom.x, rb.y + posFrom.y);
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
        List<MapleMapObjectType> objectTypes = new ArrayList<MapleMapObjectType>();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInBox(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), objectTypes);
    }
}
