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
import constants.game.GameConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.MonitoredWriteLock;
import net.server.audit.locks.factory.MonitoredReadLockFactory;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.server.audit.locks.factory.MonitoredWriteLockFactory;
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
    protected MonitoredReadLock statRlock;
    protected MonitoredWriteLock statWlock;
    
    protected AbstractMapleCharacterObject() {
        MonitoredReentrantReadWriteLock locks = new MonitoredReentrantReadWriteLock(MonitoredLockType.CHARACTER_STA, true);
        statRlock = MonitoredReadLockFactory.createLock(locks);
        statWlock = MonitoredWriteLockFactory.createLock(locks);
        
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
    
    private static long clampStat(int v, int min, int max) {
        return (v < min) ? min : ((v > max) ? max : v);
    }
    
    private static long calcStatPoolNode(Integer v, int displacement) {
        long r;
        if (v == null) {
            r = -32768;
        } else {
            r = clampStat(v, -32767, 32767);
        }
        
        return ((r & 0x0FFFF) << displacement);
    }
    
    private static long calcStatPoolLong(Integer v1, Integer v2, Integer v3, Integer v4) {
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
        changeHpMpPool(newhp, newmp, null, null, silent);
    }
    
    private void changeHpMpPool(Integer hp, Integer mp, Integer maxhp, Integer maxmp, boolean silent) {
        long hpMpPool = calcStatPoolLong(hp, mp, maxhp, maxmp);
        changeStatPool(hpMpPool, null, null, -1, silent);
    }
    
    public void updateHp(int hp) {
        updateHpMaxHp(hp, null);
    }
    
    public void updateMaxHp(int maxhp) {
        updateHpMaxHp(null, maxhp);
    }
    
    public void updateHpMaxHp(int hp, int maxhp) {
        updateHpMaxHp(Integer.valueOf(hp), Integer.valueOf(maxhp));
    }
    
    private void updateHpMaxHp(Integer hp, Integer maxhp) {
        changeHpMpPool(hp, null, maxhp, null, false);
    }
    
    public void updateMp(int mp) {
        updateMpMaxMp(mp, null);
    }
    
    public void updateMaxMp(int maxmp) {
        updateMpMaxMp(null, maxmp);
    }
    
    public void updateMpMaxMp(int mp, int maxmp) {
        updateMpMaxMp(Integer.valueOf(mp), Integer.valueOf(maxmp));
    }
    
    private void updateMpMaxMp(Integer mp, Integer maxmp) {
        changeHpMpPool(null, mp, null, maxmp, false);
    }
    
    public void updateMaxHpMaxMp(int maxhp, int maxmp) {
        changeHpMpPool(null, null, maxhp, maxmp, false);
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
            changeHpMpPool(null, null, maxhp + hpdelta, maxmp + mpdelta, silent);
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
        return assignStrDexIntLuk(x, null, null, null);
    }
    
    public boolean assignDex(int x) {
        return assignStrDexIntLuk(null, x, null, null);
    }
    
    public boolean assignInt(int x) {
        return assignStrDexIntLuk(null, null, x, null);
    }
    
    public boolean assignLuk(int x) {
        return assignStrDexIntLuk(null, null, null, x);
    }
    
    public boolean assignHP(int deltaHP, int deltaAp) {
        effLock.lock();
        statWlock.lock();
        try {
            if (remainingAp - deltaAp < 0 || hpMpApUsed + deltaAp < 0 || maxhp >= 30000) {
                return false;
            }
            
            long hpMpPool = calcStatPoolLong(null, null, maxhp + deltaHP, maxmp);
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

            long hpMpPool = calcStatPoolLong(null, null, maxhp, maxmp + deltaMP);
            long strDexIntLuk = calcStatPoolLong(str, dex, int_, luk);

            changeStatPool(hpMpPool, strDexIntLuk, null, remainingAp - deltaAp, false);
            setHpMpApUsed(hpMpApUsed + deltaAp);
            return true;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
    
    private static int apAssigned(Integer x) {
        return x != null ? x : 0;
    }
    
    public boolean assignStrDexIntLuk(int deltaStr, int deltaDex, int deltaInt, int deltaLuk) {
        return assignStrDexIntLuk(Integer.valueOf(deltaStr), Integer.valueOf(deltaDex), Integer.valueOf(deltaInt), Integer.valueOf(deltaLuk));
    }
    
    private boolean assignStrDexIntLuk(Integer deltaStr, Integer deltaDex, Integer deltaInt, Integer deltaLuk) {
        effLock.lock();
        statWlock.lock();
        try {
            int apUsed = apAssigned(deltaStr) + apAssigned(deltaDex) + apAssigned(deltaInt) + apAssigned(deltaLuk);
            if (apUsed > remainingAp) {
                return false;
            }

            int newStr = str, newDex = dex, newInt = int_, newLuk = luk;
            if (deltaStr != null) newStr += deltaStr;   // thanks Rohenn for noticing an NPE case after "null" started being used
            if (deltaDex != null) newDex += deltaDex;
            if (deltaInt != null) newInt += deltaInt;
            if (deltaLuk != null) newLuk += deltaLuk;
            
            if (newStr < 4 || newStr > YamlConfig.config.server.MAX_AP) {
                return false;
            }

            if (newDex < 4 || newDex > YamlConfig.config.server.MAX_AP) {
                return false;
            }

            if (newInt < 4 || newInt > YamlConfig.config.server.MAX_AP) {
                return false;
            }

            if (newLuk < 4 || newLuk > YamlConfig.config.server.MAX_AP) {
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
    
    private void changeStrDexIntLuk(Integer str, Integer dex, Integer int_, Integer luk, int remainingAp, boolean silent) {
        long strDexIntLuk = calcStatPoolLong(str, dex, int_, luk);
        changeStatPool(null, strDexIntLuk, null, remainingAp, silent);
    }
    
    private void changeStrDexIntLukSp(Integer str, Integer dex, Integer int_, Integer luk, int remainingAp, int remainingSp, int skillbook, boolean silent) {
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
