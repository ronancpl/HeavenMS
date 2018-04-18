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

import constants.ServerConstants;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Generic, Ronan
 */
public class AutoAssignHandler extends AbstractMaplePacketHandler {
    
    private static int getNthHighestStat(List<Short> statList, short rank) {    // ranks from 0
        return(statList.size() <= rank ? 0 : statList.get(rank));
    }
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr.getRemainingAp() < 1) return;
        
        int[] statGain = new int[4];
        int[] statEqpd = new int[4];
        statGain[0] = 0; statGain[1] = 0; statGain[2] = 0; statGain[3] = 0;
        
        slea.skip(8);
        
        if(ServerConstants.USE_SERVER_AUTOASSIGNER) {
            // --------- Ronan Lana's AUTOASSIGNER ---------
            // This method excels for assigning APs in such a way to cover all equipments AP requirements.
            byte opt = slea.readByte();     // useful for pirate autoassigning
            
            int str = 0, dex = 0, luk = 0, int_ = 0;
            List<Short> eqpStrList = new ArrayList<>();
            List<Short> eqpDexList = new ArrayList<>();
            List<Short> eqpLukList = new ArrayList<>();
            
            MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
            Collection<Item> equippedC = iv.list();
            Equip nEquip;

            for (Item item : equippedC) {   //selecting the biggest AP value of each stat from each equipped item.
            	nEquip = (Equip)item;
                if(nEquip.getStr() > 0) eqpStrList.add(nEquip.getStr());
                str += nEquip.getStr();
                
                if(nEquip.getDex() > 0) eqpDexList.add(nEquip.getDex());
                dex += nEquip.getDex();
                
                if(nEquip.getLuk() > 0) eqpLukList.add(nEquip.getLuk());
                luk += nEquip.getLuk();
                
                //if(nEquip.getInt() > 0) eqpIntList.add(nEquip.getInt()); //not needed...
                int_ += nEquip.getInt();
            }
            
            statEqpd[0] = str;
            statEqpd[1] = dex;
            statEqpd[2] = luk;
            statEqpd[3] = int_;
            
            Collections.sort(eqpStrList, Collections.reverseOrder());
            Collections.sort(eqpDexList, Collections.reverseOrder());
            Collections.sort(eqpLukList, Collections.reverseOrder());
            
            //Autoassigner looks up the 1st/2nd placed equips for their stats to calculate the optimal upgrade.
            int eqpStr = getNthHighestStat(eqpStrList, (short) 0) + getNthHighestStat(eqpStrList, (short) 1);
            int eqpDex = getNthHighestStat(eqpDexList, (short) 0) + getNthHighestStat(eqpDexList, (short) 1);
            int eqpLuk = getNthHighestStat(eqpLukList, (short) 0) + getNthHighestStat(eqpLukList, (short) 1);

            //c.getPlayer().message("----------------------------------------");
            //c.getPlayer().message("SDL: s" + eqpStr + " d" + eqpDex + " l" + eqpLuk + " BASE STATS --> STR: " + chr.getStr() + " DEX: " + chr.getDex() + " INT: " + chr.getInt() + " LUK: " + chr.getLuk());
            //c.getPlayer().message("SUM EQUIP STATS -> STR: " + str + " DEX: " + dex + " LUK: " + luk + " INT: " + int_);
            
            MapleJob stance = c.getPlayer().getJobStyle(opt);
            int prStat = 0, scStat = 0, trStat = 0, temp, tempAp = chr.getRemainingAp(), CAP;
            
            MapleStat primary, secondary, tertiary = MapleStat.LUK;
            switch(stance) {
                case MAGICIAN:
                    CAP = 165;
                    scStat = (chr.getLevel() + 3) - (chr.getLuk() + luk - eqpLuk);
                    if(scStat < 0) scStat = 0;
                    scStat = Math.min(scStat, tempAp);
                    
                    if(tempAp > scStat) tempAp -= scStat;
                    else tempAp = 0;
                    
                    prStat = tempAp;
                    int_ = prStat;
                    luk = scStat;
                    str = 0; dex = 0;
                    
                    if(luk + chr.getLuk() > CAP) {
                        temp = luk + chr.getLuk() - CAP;
                        luk -= temp;
                        int_ += temp;
                    }
                    
                    primary = MapleStat.INT;
                    secondary = MapleStat.LUK;
                    tertiary = MapleStat.DEX;
                    
                    break;
                
                case BOWMAN:
                    CAP = 125;
                    scStat = (chr.getLevel() + 5) - (chr.getStr() + str - eqpStr);
                    if(scStat < 0) scStat = 0;
                    scStat = Math.min(scStat, tempAp);
                    
                    if(tempAp > scStat) tempAp -= scStat;
                    else tempAp = 0;
                    
                    prStat = tempAp;
                    dex = prStat;
                    str = scStat;
                    int_ = 0; luk = 0;
                    
                    if(str + chr.getStr() > CAP) {
                        temp = str + chr.getStr() - CAP;
                        str -= temp;
                        dex += temp;
                    }
                    
                    primary = MapleStat.DEX;
                    secondary = MapleStat.STR;
                    
                    break;
                    
                case GUNSLINGER:
                case CROSSBOWMAN:
                    CAP = 120;
                    scStat = chr.getLevel() - (chr.getStr() + str - eqpStr);
                    if(scStat < 0) scStat = 0;
                    scStat = Math.min(scStat, tempAp);
                    
                    if(tempAp > scStat) tempAp -= scStat;
                    else tempAp = 0;
                    
                    prStat = tempAp;
                    dex = prStat;
                    str = scStat;
                    int_ = 0; luk = 0;
                    
                    if(str + chr.getStr() > CAP) {
                        temp = str + chr.getStr() - CAP;
                        str -= temp;
                        dex += temp;
                    }
                    
                    primary = MapleStat.DEX;
                    secondary = MapleStat.STR;
                    
                    break;
                    
                case THIEF:
                    CAP = 160;
                    
                    scStat = 0;
                    if(chr.getDex() < 80) {
                        scStat = (2 * chr.getLevel()) - (chr.getDex() + dex - eqpDex);
                        if(scStat < 0) scStat = 0;
                            
                        scStat = Math.min(80 - chr.getDex(), scStat);
                        scStat = Math.min(tempAp, scStat);
                        tempAp -= scStat;
                    }
                    
                    temp = (chr.getLevel() + 40) - Math.max(80, scStat + chr.getDex() + dex - eqpDex);
                    if(temp < 0) temp = 0;
                    temp = Math.min(tempAp, temp);
                    scStat += temp;
                    tempAp -= temp;
                    
                    // thieves will upgrade STR as well only if a level-based threshold is reached.
                    if(chr.getStr() >= Math.max(13, (int)(0.4 * chr.getLevel()))) {
                        if(chr.getStr() < 50) {
                            trStat = (chr.getLevel() - 10) - (chr.getStr() + str - eqpStr);
                            if(trStat < 0) trStat = 0;

                            trStat = Math.min(50 - chr.getStr(), trStat);
                            trStat = Math.min(tempAp, trStat);
                            tempAp -= trStat;
                        }
                    
                        temp = (20 + (chr.getLevel() / 2)) - Math.max(50, trStat + chr.getStr() + str - eqpStr);
                        if(temp < 0) temp = 0;
                        temp = Math.min(tempAp, temp);
                        trStat += temp;
                        tempAp -= temp;
                    }
                    
                    prStat = tempAp;
                    luk = prStat;
                    dex = scStat;
                    str = trStat;
                    int_ = 0;
                    
                    if(dex + chr.getDex() > CAP) {
                        temp = dex + chr.getDex() - CAP;
                        dex -= temp;
                        luk += temp;
                    }
                    if(str + chr.getStr() > CAP) {
                        temp = str + chr.getStr() - CAP;
                        str -= temp;
                        luk += temp;
                    }
                    
                    primary = MapleStat.LUK;
                    secondary = MapleStat.DEX;
                    tertiary = MapleStat.STR;
                    
                    break;
                    
                case BRAWLER:
                    CAP = 120;
                    
                    scStat = chr.getLevel() - (chr.getDex() + dex - eqpDex);
                    if(scStat < 0) scStat = 0;
                    scStat = Math.min(scStat, tempAp);
                    
                    if(tempAp > scStat) tempAp -= scStat;
                    else tempAp = 0;
                    
                    prStat = tempAp;
                    str = prStat;
                    dex = scStat;
                    int_ = 0; luk = 0;
                    
                    if(dex + chr.getDex() > CAP) {
                        temp = dex + chr.getDex() - CAP;
                        dex -= temp;
                        str += temp;
                    }
                    
                    primary = MapleStat.STR;
                    secondary = MapleStat.DEX;
                    
                    break;
                    
                default:    //warrior, beginner, ...
                    CAP = 80;
                    
                    scStat = ((2 * chr.getLevel()) / 3) - (chr.getDex() + dex - eqpDex);
                    if(scStat < 0) scStat = 0;
                    scStat = Math.min(scStat, tempAp);
                    
                    if(tempAp > scStat) tempAp -= scStat;
                    else tempAp = 0;
                    
                    prStat = tempAp;
                    str = prStat;
                    dex = scStat;
                    int_ = 0; luk = 0;
                    
                    if(dex + chr.getDex() > CAP) {
                        temp = dex + chr.getDex() - CAP;
                        dex -= temp;
                        str += temp;
                    }
                    
                    primary = MapleStat.STR;
                    secondary = MapleStat.DEX;
            }
            
            //-------------------------------------------------------------------------------------
            
            int extras = 0;
            
            extras = gainStatByType(chr, primary, statGain, prStat + extras);
            extras = gainStatByType(chr, secondary, statGain, scStat + extras);
            extras = gainStatByType(chr, tertiary, statGain, trStat + extras);
                        
            if(extras > 0) {    //redistribute surplus in priority order
                extras = gainStatByType(chr, primary, statGain, extras);
                extras = gainStatByType(chr, secondary, statGain, extras);
                extras = gainStatByType(chr, tertiary, statGain, extras);
                gainStatByType(chr, getQuaternaryStat(stance), statGain, extras);
            }
            
            int remainingAp = (chr.getRemainingAp() - getAccumulatedStatGain(statGain));
            chr.setRemainingAp(remainingAp);
            chr.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
            c.announce(MaplePacketCreator.enableActions());
            
            //----------------------------------------------------------------------------------------
            
            c.announce(MaplePacketCreator.serverNotice(1, "Better AP applications detected:\r\nSTR: +" + statGain[0] + "\r\nDEX: +" + statGain[1] + "\r\nINT: +" + statGain[3] + "\r\nLUK: +" + statGain[2]));
        } else {
            if(slea.available() < 16) {
                AutobanFactory.PACKET_EDIT.alert(chr, "Didn't send full packet for Auto Assign.");
                c.disconnect(false, false);
                return;
            }

            MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
            Collection<Item> equippedC = iv.list();
            for (Item item : equippedC) {   //selecting the biggest AP value of each stat from each equipped item.
            	Equip nEquip = (Equip)item;
                
                statEqpd[0] += nEquip.getStr();
                statEqpd[1] += nEquip.getDex();
                statEqpd[2] += nEquip.getLuk();
                statEqpd[3] += nEquip.getInt();
            }
            
            int total = 0;
            int extras = 0;
            for (int i = 0; i < 2; i++) {
                int type = slea.readInt();
                int tempVal = slea.readInt();
                if (tempVal < 0 || tempVal > c.getPlayer().getRemainingAp()) {
                    return;
                }
                total += tempVal;
                extras += gainStatByType(chr, MapleStat.getBy5ByteEncoding(type), statGain, tempVal);
            }
            int remainingAp = (chr.getRemainingAp() - total) + extras;
            chr.setRemainingAp(remainingAp);
            chr.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
            c.announce(MaplePacketCreator.enableActions());
        }
    }

    private int gainStatByType(MapleCharacter chr, MapleStat type, int[] statGain, int gain) {
        if(gain <= 0) return 0;
        
        int newVal = 0;
        if (type.equals(MapleStat.STR)) {
            newVal = chr.getStr() + gain;
            if (newVal > ServerConstants.MAX_AP) {
                statGain[0] += (gain - (newVal - ServerConstants.MAX_AP));
                chr.setStr(ServerConstants.MAX_AP);
            } else {
                statGain[0] += gain;
                chr.setStr(newVal);
            }
        } else if (type.equals(MapleStat.INT)) {
            newVal = chr.getInt() + gain;
            if (newVal > ServerConstants.MAX_AP) {
                statGain[3] += (gain - (newVal - ServerConstants.MAX_AP));
                chr.setInt(ServerConstants.MAX_AP);
            } else {
                statGain[3] += gain;
                chr.setInt(newVal);
            }
        } else if (type.equals(MapleStat.LUK)) {
            newVal = chr.getLuk() + gain;
            if (newVal > ServerConstants.MAX_AP) {
                statGain[2] += (gain - (newVal - ServerConstants.MAX_AP));
                chr.setLuk(ServerConstants.MAX_AP);
            } else {
                statGain[2] += gain;
                chr.setLuk(newVal);
            }
        } else if (type.equals(MapleStat.DEX)) {
            newVal = chr.getDex() + gain;
            if (newVal > ServerConstants.MAX_AP) {
                statGain[1] += (gain - (newVal - ServerConstants.MAX_AP));
                chr.setDex(ServerConstants.MAX_AP);
            } else {
                statGain[1] += gain;
                chr.setDex(newVal);
            }
        }
        
        if (newVal > ServerConstants.MAX_AP) {
            chr.updateSingleStat(type, ServerConstants.MAX_AP);
            return newVal - ServerConstants.MAX_AP;
        }
        chr.updateSingleStat(type, newVal);
        return 0;
    }
    
    private MapleStat getQuaternaryStat(MapleJob stance) {
        if(stance != MapleJob.MAGICIAN) return MapleStat.INT;
        return MapleStat.STR;
    }
    
    private int getAccumulatedStatGain(int[] statGain) {
        int acc = 0;
        
        for(byte i = 0; i < statGain.length; i++) {
            acc += statGain[i];
        }
        
        return acc;
    }
}
