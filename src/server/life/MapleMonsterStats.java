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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.life.MapleLifeFactory.BanishInfo;
import server.life.MapleLifeFactory.loseItem;
import server.life.MapleLifeFactory.selfDestruction;
import tools.Pair;

/**
 * @author Frz
 */
public class MapleMonsterStats {
    public boolean changeable;
    public int exp, hp, mp, level, PADamage, PDDamage, MADamage, MDDamage, dropPeriod, cp, buffToGive = -1, removeAfter;
    public boolean boss, undead, ffaLoot, isExplosiveReward, firstAttack, removeOnMiss;
    public String name;
    public Map<String, Integer> animationTimes = new HashMap<String, Integer>();
    public Map<Element, ElementalEffectiveness> resistance = new HashMap<Element, ElementalEffectiveness>();
    public List<Integer> revives = Collections.emptyList();
    public byte tagColor, tagBgColor;
    public List<Pair<Integer, Integer>> skills = new ArrayList<Pair<Integer, Integer>>();
    public Pair<Integer, Integer> cool = null;
    public BanishInfo banish = null;
    public List<loseItem> loseItem = null;
    public selfDestruction selfDestruction = null;
    public int fixedStance = 0;
    public boolean friendly;

    public void setChange(boolean change) {
        this.changeable = change;
    }

    public boolean isChangeable() {
        return changeable;
    }
    
    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int removeAfter() {
        return removeAfter;
    }

    public void setRemoveAfter(int removeAfter) {
        this.removeAfter = removeAfter;
    }

    public int getDropPeriod() {
        return dropPeriod;
    }

    public void setDropPeriod(int dropPeriod) {
        this.dropPeriod = dropPeriod;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    public boolean isBoss() {
        return boss;
    }

    public void setFfaLoot(boolean ffaLoot) {
        this.ffaLoot = ffaLoot;
    }

    public boolean isFfaLoot() {
        return ffaLoot;
    }

    public void setAnimationTime(String name, int delay) {
        animationTimes.put(name, delay);
    }

    public int getAnimationTime(String name) {
        Integer ret = animationTimes.get(name);
        if (ret == null) {
            return 500;
        }
        return ret.intValue();
    }

    public boolean isMobile() {
        return animationTimes.containsKey("move") || animationTimes.containsKey("fly");
    }

    public List<Integer> getRevives() {
        return revives;
    }

    public void setRevives(List<Integer> revives) {
        this.revives = revives;
    }

    public void setUndead(boolean undead) {
        this.undead = undead;
    }

    public boolean isUndead() {
        return undead;
    }

    public void setEffectiveness(Element e, ElementalEffectiveness ee) {
        resistance.put(e, ee);
    }

    public ElementalEffectiveness getEffectiveness(Element e) {
        ElementalEffectiveness elementalEffectiveness = resistance.get(e);
        if (elementalEffectiveness == null) {
            return ElementalEffectiveness.NORMAL;
        } else {
            return elementalEffectiveness;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getTagColor() {
        return tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = (byte) tagColor;
    }

    public byte getTagBgColor() {
        return tagBgColor;
    }

    public void setTagBgColor(int tagBgColor) {
        this.tagBgColor = (byte) tagBgColor;
    }

    public void setSkills(List<Pair<Integer, Integer>> skills) {
        for (int i = this.skills.size(); i < skills.size(); i++) {
            this.skills.add(null);
        }
        
        for (int i = 0; i < skills.size(); i++) {
            this.skills.set(i, skills.get(i));
        }
    }

    public List<Pair<Integer, Integer>> getSkills() {
        return Collections.unmodifiableList(this.skills);
    }

    public int getNoSkills() {
        return this.skills.size();
    }

    public boolean hasSkill(int skillId, int level) {
        for (Pair<Integer, Integer> skill : skills) {
            if (skill.getLeft() == skillId && skill.getRight() == level) {
                return true;
            }
        }
        return false;
    }

    public void setFirstAttack(boolean firstAttack) {
        this.firstAttack = firstAttack;
    }

    public boolean isFirstAttack() {
        return firstAttack;
    }

    public void setBuffToGive(int buff) {
        this.buffToGive = buff;
    }

    public int getBuffToGive() {
        return buffToGive;
    }

    void removeEffectiveness(Element e) {
        resistance.remove(e);
    }

    public BanishInfo getBanishInfo() {
        return banish;
    }

    public void setBanishInfo(BanishInfo banish) {
        this.banish = banish;
    }

    public int getPADamage() {
        return PADamage;
    }

    public void setPADamage(int PADamage) {
        this.PADamage = PADamage;
    }

    public int getCP() {
        return cp;
    }

    public void setCP(int cp) {
        this.cp = cp;
    }

    public List<loseItem> loseItem() {
        return loseItem;
    }

    public void addLoseItem(loseItem li) {
        if (loseItem == null) {
            loseItem = new LinkedList<loseItem>();
        }
        loseItem.add(li);
    }

    public selfDestruction selfDestruction() {
        return selfDestruction;
    }

    public void setSelfDestruction(selfDestruction sd) {
        this.selfDestruction = sd;
    }
    
    public void setExplosiveReward(boolean isExplosiveReward) {
        this.isExplosiveReward = isExplosiveReward;
    }

    public boolean isExplosiveReward() {
        return isExplosiveReward;
    }

    public void setRemoveOnMiss(boolean removeOnMiss) {
        this.removeOnMiss = removeOnMiss;
    }

    public boolean removeOnMiss() {
        return removeOnMiss;
    }

    public void setCool(Pair<Integer, Integer> cool) {
        this.cool = cool;
    }

    public Pair<Integer, Integer> getCool() {
        return cool;
    }
    
    public int getPDDamage() {
        return PDDamage;
    }
    
    public int getMADamage() {
        return MADamage;
    }
    
    public int getMDDamage() {
        return MDDamage;
    }
    
    public boolean isFriendly() {
        return friendly;
    }
    
    public void setFriendly(boolean value) {
        this.friendly = value;
    }
    
    public void setPDDamage(int PDDamage) {
        this.PDDamage = PDDamage;
    }
    
    public void setMADamage(int MADamage) {
        this.MADamage = MADamage;
    }
    
    public void setMDDamage(int MDDamage) {
        this.MDDamage = MDDamage;
    } 
    
    public int getFixedStance() {
        return this.fixedStance;
    }
    
    public void setFixedStance(int stance) {
        this.fixedStance = stance;
    }
    
    public MapleMonsterStats copy() {
        MapleMonsterStats copy = new MapleMonsterStats();
        try {
            FieldCopyUtil.setFields(this, copy);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Thread.sleep(10000);
            } catch (Exception ex) {
                
            }
            
        }
        
        return copy;
    }
    
    // FieldCopyUtil src: http://www.codesenior.com/en/tutorial/Java-Copy-Fields-From-One-Object-to-Another-Object-with-Reflection
    private static class FieldCopyUtil { // thanks to Codesenior dev team
        private static void setFields(Object from, Object to) {
            Field[] fields = from.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    Field fieldFrom = from.getClass().getDeclaredField(field.getName());
                    Object value = fieldFrom.get(from);
                    to.getClass().getDeclaredField(field.getName()).set(to, value);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
