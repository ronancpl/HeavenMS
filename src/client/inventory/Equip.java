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
package client.inventory;

import client.MapleClient;
import constants.ServerConstants;
import constants.ExpTable;
import java.util.LinkedList;
import java.util.List;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

public class Equip extends Item {

    public static enum ScrollResult {

        FAIL(0), SUCCESS(1), CURSE(2);
        private int value = -1;

        private ScrollResult(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    private static enum StatUpgrade {

        incDEX(0), incSTR(1), incINT(2), incLUK(3),
        incMHP(4), incMMP(5), incPAD(6), incMAD(7),
        incPDD(8), incMDD(9), incEVA(10), incACC(11),
        incSpeed(12), incJump(13), incVicious(14), incSlot(15);
        private int value = -1;

        private StatUpgrade(int value) {
            this.value = value;
        }
    }
    
    private byte upgradeSlots;
    private byte level, flag, itemLevel;
    private short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, vicious;
    private float itemExp;
    private int ringid = -1;
    private boolean wear = false;
    private boolean isUpgradeable, isElemental = false;    // timeless or reverse, or any equip that could levelup on GMS for all effects

    public Equip(int id, short position) {
        this(id, position, 0);
    }

    public Equip(int id, short position, int slots) {
        super(id, position, (short) 1);
        this.upgradeSlots = (byte) slots;
        this.itemExp = 0;
        this.itemLevel = 1;
        
        this.isElemental = (MapleItemInformationProvider.getInstance().getEquipLevel(id, false) > 1);
    }

    @Override
    public Item copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUpgradeSlots());
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.flag = flag;
        ret.vicious = vicious;
        ret.upgradeSlots = upgradeSlots;
        ret.itemLevel = itemLevel;
        ret.itemExp = itemExp;
        ret.level = level;
        ret.log = new LinkedList<>(log);
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        ret.setGiftFrom(getGiftFrom());
        return ret;
    }

    @Override
    public byte getFlag() {
        return flag;
    }

    @Override
    public byte getItemType() {
        return 1;
    }
    
    public byte getUpgradeSlots() {
        return upgradeSlots;
    }

    public short getStr() {
        return str;
    }

    public short getDex() {
        return dex;
    }

    public short getInt() {
        return _int;
    }

    public short getLuk() {
        return luk;
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getWatk() {
        return watk;
    }

    public short getMatk() {
        return matk;
    }

    public short getWdef() {
        return wdef;
    }

    public short getMdef() {
        return mdef;
    }

    public short getAcc() {
        return acc;
    }

    public short getAvoid() {
        return avoid;
    }

    public short getHands() {
        return hands;
    }

    public short getSpeed() {
        return speed;
    }

    public short getJump() {
        return jump;
    }

    public short getVicious() {
        return vicious;
    }

    @Override
    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public void setInt(short _int) {
        this._int = _int;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public void setHp(short hp) {
        this.hp = hp;
    }

    public void setMp(short mp) {
        this.mp = mp;
    }

    public void setWatk(short watk) {
        this.watk = watk;
    }

    public void setMatk(short matk) {
        this.matk = matk;
    }

    public void setWdef(short wdef) {
        this.wdef = wdef;
    }

    public void setMdef(short mdef) {
        this.mdef = mdef;
    }

    public void setAcc(short acc) {
        this.acc = acc;
    }

    public void setAvoid(short avoid) {
        this.avoid = avoid;
    }

    public void setHands(short hands) {
        this.hands = hands;
    }

    public void setSpeed(short speed) {
        this.speed = speed;
    }

    public void setJump(short jump) {
        this.jump = jump;
    }

    public void setVicious(short vicious) {
        this.vicious = vicious;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    private static int getStatModifier(boolean isAttribute) {
        // each set of stat points grants a chance for a bonus stat point upgrade at equip level up.
        
        if(ServerConstants.USE_EQUIPMNT_LVLUP_POWER) {
            if(isAttribute) return 2;
            else return 4;
        }
        else {
            if(isAttribute) return 4;
            else return 16;
        }
    }
    
    private static int randomizeStatUpgrade(int top) {
        int limit = Math.min(top, ServerConstants.MAX_EQUIPMNT_LVLUP_STAT_UP);
        
        int poolCount = (limit * (limit + 1) / 2) + limit;
        int rnd = Randomizer.rand(0, poolCount);
        
        int stat = 0;
        if(rnd >= limit) {
            rnd -= limit;
            stat = 1 + (int)Math.floor((-1 + Math.sqrt((8 * rnd) + 1)) / 2);
        }
        
        return stat;
    }
    
    private void getUnitStatUpgrade(List<Pair<StatUpgrade, Integer>> stats, StatUpgrade name, int curStat, boolean isAttribute) {
        isUpgradeable = true;
        
        int maxUpgrade = randomizeStatUpgrade((int)(1 + (curStat / getStatModifier(isAttribute))));
        if(maxUpgrade == 0) return;
            
        stats.add(new Pair<>(name, maxUpgrade));
    }
    
    private static void getUnitSlotUpgrade(List<Pair<StatUpgrade, Integer>> stats, StatUpgrade name) {
        if(Math.random() < 0.1) {
            stats.add(new Pair<>(name, 1));  // 10% success on getting a slot upgrade.
        }
    }
    
    private void improveDefaultStats(List<Pair<StatUpgrade, Integer>> stats) {
        if(dex > 0) getUnitStatUpgrade(stats, StatUpgrade.incDEX, dex, true);
        if(str > 0) getUnitStatUpgrade(stats, StatUpgrade.incSTR, str, true);
        if(_int > 0) getUnitStatUpgrade(stats, StatUpgrade.incINT,_int, true);
        if(luk > 0) getUnitStatUpgrade(stats, StatUpgrade.incLUK, luk, true);
        if(hp > 0) getUnitStatUpgrade(stats, StatUpgrade.incMHP, hp, false);
        if(mp > 0) getUnitStatUpgrade(stats, StatUpgrade.incMMP, mp, false);
        if(watk > 0) getUnitStatUpgrade(stats, StatUpgrade.incPAD, watk, false);
        if(matk > 0) getUnitStatUpgrade(stats, StatUpgrade.incMAD, matk, false);
        if(wdef > 0) getUnitStatUpgrade(stats, StatUpgrade.incPDD, wdef, false);
        if(mdef > 0) getUnitStatUpgrade(stats, StatUpgrade.incMDD, mdef, false);
        if(avoid > 0) getUnitStatUpgrade(stats, StatUpgrade.incEVA, avoid, false);
        if(acc > 0) getUnitStatUpgrade(stats, StatUpgrade.incACC, acc, false);
        if(speed > 0) getUnitStatUpgrade(stats, StatUpgrade.incSpeed, speed, false);
        if(jump > 0) getUnitStatUpgrade(stats, StatUpgrade.incJump, jump, false);
    }
    
    private void gainLevel(MapleClient c) {
        List<Pair<StatUpgrade, Integer>> stats = new LinkedList<>();
                
        if(isElemental) {
            List<Pair<String, Integer>> elementalStats = MapleItemInformationProvider.getInstance().getItemLevelupStats(getItemId(), itemLevel);
            
            for(Pair<String, Integer> p: elementalStats) {
                if(p.getRight() > 0) stats.add(new Pair<>(StatUpgrade.valueOf(p.getLeft()), p.getRight()));
            }
        }
        
        if(!stats.isEmpty()) {
            if(ServerConstants.USE_EQUIPMNT_LVLUP_SLOTS) {
                if(vicious > 0) getUnitSlotUpgrade(stats, StatUpgrade.incVicious);
                getUnitSlotUpgrade(stats, StatUpgrade.incSlot);
            }
        }
        else {
            isUpgradeable = false;
            
            improveDefaultStats(stats);
            if(ServerConstants.USE_EQUIPMNT_LVLUP_SLOTS) {
                if(vicious > 0) getUnitSlotUpgrade(stats, StatUpgrade.incVicious);
                getUnitSlotUpgrade(stats, StatUpgrade.incSlot);
            }
            
            if(isUpgradeable) {
                while(stats.isEmpty()) {
                    improveDefaultStats(stats);
                    if(ServerConstants.USE_EQUIPMNT_LVLUP_SLOTS) {
                        if(vicious > 0) getUnitSlotUpgrade(stats, StatUpgrade.incVicious);
                        getUnitSlotUpgrade(stats, StatUpgrade.incSlot);
                    }
                }
            }
        }
        
        itemLevel++;
        boolean gotVicious = false, gotSlot = false;
        
        String lvupStr = "'" + MapleItemInformationProvider.getInstance().getName(this.getItemId()) + "' is now level " + itemLevel + "! ";
        String showStr = "#e'" + MapleItemInformationProvider.getInstance().getName(this.getItemId()) + "'#b is now #elevel #r" + itemLevel + "#k#b!";
        
        Integer statUp, maxStat = ServerConstants.MAX_EQUIPMNT_STAT;
        for (Pair<StatUpgrade, Integer> stat : stats) {
            switch (stat.getLeft()) {
                case incDEX:
                    statUp = Math.min(stat.getRight(), maxStat - dex);
                    dex += statUp;
                    lvupStr += "+" + statUp + "DEX ";
                    break;
                case incSTR:
                    statUp = Math.min(stat.getRight(), maxStat - str);
                    str += statUp;
                    lvupStr += "+" + statUp + "STR ";
                    break;
                case incINT:
                    statUp = Math.min(stat.getRight(), maxStat - _int);
                    _int += statUp;
                    lvupStr += "+" + statUp + "INT ";
                    break;
                case incLUK:
                    statUp = Math.min(stat.getRight(), maxStat - luk);
                    luk += statUp;
                    lvupStr += "+" + statUp + "LUK ";
                    break;
                case incMHP:
                    statUp = Math.min(stat.getRight(), maxStat - hp);
                    hp += statUp;
                    lvupStr += "+" + statUp + "HP ";
                    break;
                case incMMP:
                    statUp = Math.min(stat.getRight(), maxStat - mp);
                    mp += statUp;
                    lvupStr += "+" + statUp + "MP ";
                    break;
                case incPAD:
                    statUp = Math.min(stat.getRight(), maxStat - watk);
                    watk += statUp;
                    lvupStr += "+" + statUp + "WATK ";
                    break;
                case incMAD:
                    statUp = Math.min(stat.getRight(), maxStat - matk);
                    matk += statUp;
                    lvupStr += "+" + statUp + "MATK ";
                    break;
                case incPDD:
                    statUp = Math.min(stat.getRight(), maxStat - wdef);
                    wdef += statUp;
                    lvupStr += "+" + statUp + "WDEF ";
                    break;
                case incMDD:
                    statUp = Math.min(stat.getRight(), maxStat - mdef);
                    mdef += statUp;
                    lvupStr += "+" + statUp + "MDEF ";
                    break;
                case incEVA:
                    statUp = Math.min(stat.getRight(), maxStat - avoid);
                    avoid += statUp;
                    lvupStr += "+" + statUp + "AVOID ";
                    break;
                case incACC:
                    statUp = Math.min(stat.getRight(), maxStat - acc);
                    acc += statUp;
                    lvupStr += "+" + statUp + "ACC ";
                    break;
                case incSpeed:
                    statUp = Math.min(stat.getRight(), maxStat - speed);
                    speed += statUp;
                    lvupStr += "+" + statUp + "SPEED ";
                    break;
                case incJump:
                    statUp = Math.min(stat.getRight(), maxStat - jump);
                    jump += statUp;
                    lvupStr += "+" + statUp + "JUMP ";
                    break;
                    
                case incVicious:
                    vicious -= stat.getRight();
                    gotVicious = true;
                    break;
                case incSlot:
                    upgradeSlots += stat.getRight();
                    gotSlot = true;
                    break;
            }
        }
        
        if(gotVicious) {
            //c.getPlayer().dropMessage(6, "A new Vicious Hammer opportunity has been found on the '" + MapleItemInformationProvider.getInstance().getName(getItemId()) + "'!");
            lvupStr += "+VICIOUS ";
        }
        if(gotSlot) {
            //c.getPlayer().dropMessage(6, "A new upgrade slot has been found on the '" + MapleItemInformationProvider.getInstance().getName(getItemId()) + "'!");
            lvupStr += "+UPGSLOT ";
        }
        
        showLevelupMessage(showStr, c); // thanks to Polaris dev team !
        c.getPlayer().dropMessage(6, lvupStr);
        
        c.announce(MaplePacketCreator.showEquipmentLevelUp());
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showForeignEffect(c.getPlayer().getId(), 15));
        c.getPlayer().forceUpdateItem(this);
    }

    public int getItemExp() {
        return (int) itemExp;
    }
    
    private static double normalizedMasteryExp(int reqLevel) {
        // Conversion factor between mob exp and equip exp gain. Through many calculations, the expected for equipment levelup
        // from level 1 to 2 is killing about 100~200 mobs of the same level range, on a 1x EXP rate scenario.
        
        if(reqLevel < 5) {
            return 42;
        } else if(reqLevel >= 78) {
            return Math.max((10413.648 * Math.exp(reqLevel * 0.03275)), 15);
        } else if(reqLevel >= 38) {
            return Math.max(( 4985.818 * Math.exp(reqLevel * 0.02007)), 15);
        } else if(reqLevel >= 18) {
            return Math.max((  248.219 * Math.exp(reqLevel * 0.11093)), 15);
        } else {
            return Math.max(((1334.564 * Math.log(reqLevel)) - 1731.976), 15);
        }
    }
    
    public synchronized void gainItemExp(MapleClient c, int gain) {  // Ronan's Equip Exp gain method
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if(!ii.isUpgradeable(this.getItemId())) {
            return;
        }
        
        int equipMaxLevel = Math.min(30, Math.max(ii.getEquipLevel(this.getItemId(), true), ServerConstants.USE_EQUIPMNT_LVLUP));
        if (itemLevel >= equipMaxLevel) {
            return;
        }
        
        int reqLevel = ii.getEquipLevelReq(this.getItemId());
        
        float masteryModifier = (float)(ServerConstants.EQUIP_EXP_RATE * ExpTable.getExpNeededForLevel(1)) / (float)normalizedMasteryExp(reqLevel);
        float elementModifier = (isElemental) ? 0.85f : 0.6f;
        
        float baseExpGain = gain * elementModifier * masteryModifier;
        
        itemExp += baseExpGain;
        int expNeeded = ExpTable.getEquipExpNeededForLevel(itemLevel);
        
        if(ServerConstants.USE_DEBUG_SHOW_INFO_EQPEXP) System.out.println("'" + ii.getName(this.getItemId()) + "' -> EXP Gain: " + gain + " Mastery: " + masteryModifier + " Base gain: " + baseExpGain + " exp: " + itemExp + " / " + expNeeded + ", Kills TNL: " + expNeeded / (baseExpGain / c.getPlayer().getExpRate()));
        
        if (itemExp >= expNeeded) {
            while(itemExp >= expNeeded) {
                itemExp -= expNeeded;
                gainLevel(c);

                if(itemLevel >= equipMaxLevel) {
                    itemExp = 0.0f;
                    break;
                }
                
                expNeeded = ExpTable.getEquipExpNeededForLevel(itemLevel);
            }
        }
        
        c.getPlayer().forceUpdateItem(this);
        //if(ServerConstants.USE_DEBUG) c.getPlayer().dropMessage("'" + ii.getName(this.getItemId()) + "': " + itemExp + " / " + expNeeded);
    }
    
    private boolean reachedMaxLevel() {
        if (isElemental) {
            if (itemLevel < MapleItemInformationProvider.getInstance().getEquipLevel(getItemId(), true)) {
                return false;
            }
        }
        
        return itemLevel >= ServerConstants.USE_EQUIPMNT_LVLUP;
    }
    
    public String showEquipFeatures(MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if(!ii.isUpgradeable(this.getItemId())) return "";
        
        String eqpName = ii.getName(getItemId());
        String eqpInfo = reachedMaxLevel() ? " #e#rMAX LEVEL#k#n" : (" EXP: #e#b" + (int)itemExp + "#k#n / " + ExpTable.getEquipExpNeededForLevel(itemLevel));
        
        return "'" + eqpName + "' -> LV: #e#b" + itemLevel + "#k#n    " + eqpInfo + "\r\n";
    }

    private static void showLevelupMessage(String msg, MapleClient c) {
        c.getPlayer().showHint(msg, 300);
    }
    
    public void setItemExp(int exp) {
        this.itemExp = exp;
    }

    public void setItemLevel(byte level) {
        this.itemLevel = level;
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public void setUpgradeSlots(int i) {
        this.upgradeSlots = (byte) i;
    }

    public void setVicious(int i) {
        this.vicious = (short) i;
    }

    public int getRingId() {
        return ringid;
    }

    public void setRingId(int id) {
        this.ringid = id;
    }

    public boolean isWearing() {
        return wear;
    }

    public void wear(boolean yes) {
        wear = yes;
    }

    public byte getItemLevel() {
        return itemLevel;
    }
}