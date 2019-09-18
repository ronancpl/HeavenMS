/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
package client;

import config.YamlConfig;
import constants.GameConstants;
import constants.ServerConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.MapleMap;

/**
 *
 * @author RonanLana
 */
public abstract class AbstractMapleCharacterObject extends AbstractAnimatedMapleMapObject {
    protected MapleMap map;
    protected int str, dex, luk, int_, hp, maxhp, mp, maxmp;
    protected int hpMpApUsed, remainingAp;
    protected int[] remainingSp = new int[10];
    protected transient int clientmaxhp, clientmaxmp, localmaxhp = 50, localmaxmp = 5;
    protected float transienthp = Float.NEGATIVE_INFINITY, transientmp = Float.NEGATIVE_INFINITY;
    
    private AbstractCharacterListener listener = null;
    protected Map<MapleStat, Integer> statUpdates = new HashMap<>();
    
    protected Lock effLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_EFF, true);
    protected ReadLock statRlock;
    protected WriteLock statWlock;
    
    protected AbstractMapleCharacterObject() {
        ReentrantReadWriteLock locks = new MonitoredReentrantReadWriteLock(MonitoredLockType.CHARACTER_STA, true);
        statRlock = locks.readLock();
        statWlock = locks.writeLock();
        
        for (int i = 0; i < remainingSp.length; i++) {
            remainingSp[i] = 0;
        }
    }
    
    protected void setListener(AbstractCharacterListener listener) {
        this.listener = listener;
    }
    
    public void setMap(MapleMap map) {
        this.map = map;
    }
    
    public MapleMap getMap() {
        return map;
    }
    
    public int getStr() {
        statRlock.lock();
        try {
            return str;
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getDex() {
        statRlock.lock();
        try {
            return dex;
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getInt() {
        statRlock.lock();
        try {
            return int_;
        } finally {
            statRlock.unlock();
        }
    }

    public int getLuk() {
        statRlock.lock();
        try {
            return luk;
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getRemainingAp() {
        statRlock.lock();
        try {
            return remainingAp;
        } finally {
            statRlock.unlock();
        }
    }
    
    protected int getRemainingSp(int jobid) {
        statRlock.lock();
        try {
            return remainingSp[GameConstants.getSkillBook(jobid)];
        } finally {
            statRlock.unlock();
        }
    }

    public int[] getRemainingSps() {
        statRlock.lock();
        try {
            return Arrays.copyOf(remainingSp, remainingSp.length);
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getHpMpApUsed() {
        statRlock.lock();
        try {
            return hpMpApUsed;
        } finally {
            statRlock.unlock();
        }
    }
    
    public boolean isAlive() {
        statRlock.lock();
        try {
            return hp > 0;
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getHp() {
        statRlock.lock();
        try {
            return hp;
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getMp() {
        statRlock.lock();
        try {
            return mp;
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getMaxHp() {
        statRlock.lock();
        try {
            return maxhp;
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getMaxMp() {
        statRlock.lock();
        try {
            return maxmp;
        } finally {
            statRlock.unlock();
        }
    }
    
    public int getClientMaxHp() {
        return clientmaxhp;
    }
    
    public int getClientMaxMp() {
        return clientmaxmp;
    }
    
    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }
    
    private void setHpMpApUsed(int mpApUsed) {
        this.hpMpApUsed = mpApUsed;
    }
    
    private void dispatchHpChanged(final int oldHp) {
        listener.onHpChanged(oldHp);
    }
    
    private void dispatchHpmpPoolUpdated() {
        listener.onHpmpPoolUpdate();
    }
    
    private void dispatchStatUpdated() {
        listener.onStatUpdate();
    }
    
    private void dispatchStatPoolUpdateAnnounced() {
        listener.onAnnounceStatPoolUpdate();
    }
    
    protected void setHp(int newHp) {
        int oldHp = hp;
        
        int thp = newHp;
        if (thp < 0) {
            thp = 0;
        } else if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        
        if (this.hp != thp) this.transienthp = Float.NEGATIVE_INFINITY;
        this.hp = thp;
        
        dispatchHpChanged(oldHp);
    }

    protected void setMp(int newMp) {
        int tmp = newMp;
        if (tmp < 0) {
            tmp = 0;
        } else if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        
        if (this.mp != tmp) this.transientmp = Float.NEGATIVE_INFINITY;
        this.mp = tmp;
    }
    
    private void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }
    
    private void setRemainingSp(int remainingSp, int skillbook) {
        this.remainingSp[skillbook] = remainingSp;
    }
    
    protected void setMaxHp(int hp_) {
        if (this.maxhp < hp_) this.transienthp = Float.NEGATIVE_INFINITY;
        this.maxhp = hp_;
        this.clientmaxhp = Math.min(30000, hp_);
    }
    
    protected void setMaxMp(int mp_) {
        if (this.maxmp < mp_) this.transientmp = Float.NEGATIVE_INFINITY;
        this.maxmp = mp_;
        this.clientmaxmp = Math.min(30000, mp_);
    }
    
    private static long calcStatPoolNode(long v, int displacement) {
        if (v > Short.MAX_VALUE) {
            v = Short.MAX_VALUE;
        } else if (v < Short.MIN_VALUE) {
            v = Short.MIN_VALUE;
        }
        
        return ((v & 0x0FFFF) << displacement);
    }
    
    private static long calcStatPoolLong(int v1, int v2, int v3, int v4) {
        long ret = 0;
        
        ret |= calcStatPoolNode(v1, 48);
        ret |= calcStatPoolNode(v2, 32);
        ret |= calcStatPoolNode(v3, 16);
        ret |= calcStatPoolNode(v4, 0);
        
        return ret;
    }
    
    private void changeStatPool(Long hpMpPool, Long strDexIntLuk, Long newSp, int newAp, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            statUpdates.clear();
            boolean poolUpdate = false;
            boolean statUpdate = false;

            if (hpMpPool != null) {
                short newHp = (short) (hpMpPool >> 48);
                short newMp = (short) (hpMpPool >> 32);
                short newMaxHp = (short) (hpMpPool >> 16);
                short newMaxMp = (short) (hpMpPool.shortValue());
                
                if (newMaxHp != Short.MIN_VALUE) {
                    if (newMaxHp < 50) {
                        newMaxHp = 50;
                    }

                    poolUpdate = true;
                    setMaxHp(newMaxHp);
                    statUpdates.put(MapleStat.MAXHP, clientmaxhp);
                    statUpdates.put(MapleStat.HP, hp);
                }

                if (newHp != Short.MIN_VALUE) {
                    setHp(newHp);
                    statUpdates.put(MapleStat.HP, hp);
                }

                if (newMaxMp != Short.MIN_VALUE) {
                    if (newMaxMp < 5) {
                        newMaxMp = 5;
                    }

                    poolUpdate = true;
                    setMaxMp(newMaxMp);
                    statUpdates.put(MapleStat.MAXMP, clientmaxmp);
                    statUpdates.put(MapleStat.MP, mp);
                }

                if (newMp != Short.MIN_VALUE) {
                    setMp(newMp);
                    statUpdates.put(MapleStat.MP, mp);
                }
            }

            if (strDexIntLuk != null) {
                short newStr = (short) (strDexIntLuk >> 48);
                short newDex = (short) (strDexIntLuk >> 32);
                short newInt = (short) (strDexIntLuk >> 16);
                short newLuk = (short) (strDexIntLuk.shortValue());

                if (newStr >= 4) {
                    setStr(newStr);
                    statUpdates.put(MapleStat.STR, str);
                }

                if (newDex >= 4) {
                    setDex(newDex);
                    statUpdates.put(MapleStat.DEX, dex);
                }

                if (newInt >= 4) {
                    setInt(newInt);
                    statUpdates.put(MapleStat.INT, int_);
                }

                if (newLuk >= 4) {
                    setLuk(newLuk);
                    statUpdates.put(MapleStat.LUK, luk);
                }

                if (newAp >= 0) {
                    setRemainingAp(newAp);
                    statUpdates.put(MapleStat.AVAILABLEAP, remainingAp);
                }

                statUpdate = true;
            }
            
            if (newSp != null) {
                short sp = (short) (newSp >> 16);
                short skillbook = (short) (newSp.shortValue());
                
                setRemainingSp(sp, skillbook);
                statUpdates.put(MapleStat.AVAILABLESP, remainingSp[skillbook]);
            }

            if (!statUpdates.isEmpty()) {
                if (poolUpdate) {
                    dispatchHpmpPoolUpdated();
                }
                
                if (statUpdate) {
                    dispatchStatUpdated();
                }

                if (!silent) {
                    dispatchStatPoolUpdateAnnounced();
                }
            }
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public void healHpMp() {
        updateHpMp(30000);
    }
    
    public void updateHpMp(int x) {
        updateHpMp(x, x);
    }
    
    public void updateHpMp(int newhp, int newmp) {
        changeHpMp(newhp, newmp, false);
    }
    
    protected void changeHpMp(int newhp, int newmp, boolean silent) {
        changeHpMpPool(newhp, newmp, Short.MIN_VALUE, Short.MIN_VALUE, silent);
    }
    
    private void changeHpMpPool(int hp, int mp, int maxhp, int maxmp, boolean silent) {
        long hpMpPool = calcStatPoolLong(hp, mp, maxhp, maxmp);
        changeStatPool(hpMpPool, null, null, -1, silent);
    }
    
    public void updateHp(int hp) {
        updateHpMaxHp(hp, Short.MIN_VALUE);
    }
    
    public void updateMaxHp(int maxhp) {
        updateHpMaxHp(Short.MIN_VALUE, maxhp);
    }
    
    public void updateHpMaxHp(int hp, int maxhp) {
        changeHpMpPool(hp, Short.MIN_VALUE, maxhp, Short.MIN_VALUE, false);
    }
    
    public void updateMp(int mp) {
        updateMpMaxMp(mp, Short.MIN_VALUE);
    }
    
    public void updateMaxMp(int maxmp) {
        updateMpMaxMp(Short.MIN_VALUE, maxmp);
    }
    
    public void updateMpMaxMp(int mp, int maxmp) {
        changeHpMpPool(Short.MIN_VALUE, mp, Short.MIN_VALUE, maxmp, false);
    }
    
    public void updateMaxHpMaxMp(int maxhp, int maxmp) {
        changeHpMpPool(Short.MIN_VALUE, Short.MIN_VALUE, maxhp, maxmp, false);
    }
    
    protected void enforceMaxHpMp() {
        effLock.lock();
        statWlock.lock();
        try {
            if (mp > localmaxmp || hp > localmaxhp) {
                changeHpMp(hp, mp, false);
            }
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public int safeAddHP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            if (hp + delta <= 0) {
                delta = -hp + 1;
            }

            addHP(delta);
            return delta;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public void addHP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateHp(hp + delta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public void addMP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateMp(mp + delta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void addMPHP(int hpDelta, int mpDelta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateHpMp(hp + hpDelta, mp + mpDelta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    protected void addMaxMPMaxHP(int hpdelta, int mpdelta, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            changeHpMpPool(Short.MIN_VALUE, Short.MIN_VALUE, maxhp + hpdelta, maxmp + mpdelta, silent);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public void addMaxHP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateMaxHp(maxhp + delta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public void addMaxMP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateMaxMp(maxmp + delta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    private void setStr(int str) {
        this.str = str;
    }
    
    private void setDex(int dex) {
        this.dex = dex;
    }
    
    private void setInt(int int_) {
        this.int_ = int_;
    }
    
    private void setLuk(int luk) {
        this.luk = luk;
    }
    
    public boolean assignStr(int x) {
        return assignStrDexIntLuk(x, Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE);
    }
    
    public boolean assignDex(int x) {
        return assignStrDexIntLuk(Short.MIN_VALUE, x, Short.MIN_VALUE, Short.MIN_VALUE);
    }
    
    public boolean assignInt(int x) {
        return assignStrDexIntLuk(Short.MIN_VALUE, Short.MIN_VALUE, x, Short.MIN_VALUE);
    }
    
    public boolean assignLuk(int x) {
        return assignStrDexIntLuk(Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE, x);
    }
    
    public boolean assignHP(int deltaHP, int deltaAp) {
        effLock.lock();
        statWlock.lock();
        try {
            if (remainingAp - deltaAp < 0 || hpMpApUsed + deltaAp < 0 || maxhp >= 30000) {
                return false;
            }
            
            long hpMpPool = calcStatPoolLong(Short.MIN_VALUE, Short.MIN_VALUE, maxhp + deltaHP, maxmp);
            long strDexIntLuk = calcStatPoolLong(str, dex, int_, luk);

            changeStatPool(hpMpPool, strDexIntLuk, null, remainingAp - deltaAp, false);
            setHpMpApUsed(hpMpApUsed + deltaAp);
            return true;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public boolean assignMP(int deltaMP, int deltaAp) {
        effLock.lock();
        statWlock.lock();
        try {
            if (remainingAp - deltaAp < 0 || hpMpApUsed + deltaAp < 0 || maxmp >= 30000) {
                return false;
            }

            long hpMpPool = calcStatPoolLong(Short.MIN_VALUE, Short.MIN_VALUE, maxhp, maxmp + deltaMP);
            long strDexIntLuk = calcStatPoolLong(str, dex, int_, luk);

            changeStatPool(hpMpPool, strDexIntLuk, null, remainingAp - deltaAp, false);
            setHpMpApUsed(hpMpApUsed + deltaAp);
            return true;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    private static int apAssigned(int x) {
        return x != Short.MIN_VALUE ? x : 0;
    }
    
    public boolean assignStrDexIntLuk(int deltaStr, int deltaDex, int deltaInt, int deltaLuk) {
        effLock.lock();
        statWlock.lock();
        try {
            int apUsed = apAssigned(deltaStr) + apAssigned(deltaDex) + apAssigned(deltaInt) + apAssigned(deltaLuk);
            if (apUsed > remainingAp) {
                return false;
            }

            int newStr = str + deltaStr, newDex = dex + deltaDex, newInt = int_ + deltaInt, newLuk = luk + deltaLuk;
            if (newStr < 4 && deltaStr != Short.MIN_VALUE || newStr > YamlConfig.config.server.MAX_AP) {
                return false;
            }

            if (newDex < 4 && deltaDex != Short.MIN_VALUE || newDex > YamlConfig.config.server.MAX_AP) {
                return false;
            }

            if (newInt < 4 && deltaInt != Short.MIN_VALUE || newInt > YamlConfig.config.server.MAX_AP) {
                return false;
            }

            if (newLuk < 4 && deltaLuk != Short.MIN_VALUE || newLuk > YamlConfig.config.server.MAX_AP) {
                return false;
            }

            int newAp = remainingAp - apUsed;
            updateStrDexIntLuk(newStr, newDex, newInt, newLuk, newAp);
            return true;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public void updateStrDexIntLuk(int x) {
        updateStrDexIntLuk(x, x, x, x, -1);
    }
    
    public void changeRemainingAp(int x, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            changeStrDexIntLuk(str, dex, int_, luk, x, silent);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    public void gainAp(int deltaAp, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            changeRemainingAp(Math.max(0, remainingAp + deltaAp), silent);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    protected void updateStrDexIntLuk(int str, int dex, int int_, int luk, int remainingAp) {
        changeStrDexIntLuk(str, dex, int_, luk, remainingAp, false);
    }
    
    private void changeStrDexIntLuk(int str, int dex, int int_, int luk, int remainingAp, boolean silent) {
        long strDexIntLuk = calcStatPoolLong(str, dex, int_, luk);
        changeStatPool(null, strDexIntLuk, null, remainingAp, silent);
    }
    
    private void changeStrDexIntLukSp(int str, int dex, int int_, int luk, int remainingAp, int remainingSp, int skillbook, boolean silent) {
        long strDexIntLuk = calcStatPoolLong(str, dex, int_, luk);
        long sp = calcStatPoolLong(0, 0, remainingSp, skillbook);
        changeStatPool(null, strDexIntLuk, sp, remainingAp, silent);
    }
    
    protected void updateStrDexIntLukSp(int str, int dex, int int_, int luk, int remainingAp, int remainingSp, int skillbook) {
        changeStrDexIntLukSp(str, dex, int_, luk, remainingAp, remainingSp, skillbook, false);
    }
    
    protected void setRemainingSp(int[] sps) {
        effLock.lock();
        statWlock.lock();
        try {
            System.arraycopy(sps, 0, remainingSp, 0, sps.length);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    protected void updateRemainingSp(int remainingSp, int skillbook) {
        changeRemainingSp(remainingSp, skillbook, false);
    }
    
    protected void changeRemainingSp(int remainingSp, int skillbook, boolean silent) {
        long sp = calcStatPoolLong(0, 0, remainingSp, skillbook);
        changeStatPool(null, null, sp, Short.MIN_VALUE, silent);
    }
    
    public void gainSp(int deltaSp, int skillbook, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            changeRemainingSp(Math.max(0, remainingSp[skillbook] + deltaSp), skillbook, silent);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
}
