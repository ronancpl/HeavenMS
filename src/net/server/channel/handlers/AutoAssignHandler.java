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
//import java.util.ArrayList;
import java.util.Collection;
//import java.util.List;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Generic
 */
public class AutoAssignHandler extends AbstractMaplePacketHandler {
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        MapleJob stance;
        
        if(ServerConstants.USE_ANOTHER_AUTOASSIGN == true) {
            int eqpStr = 0, eqpDex = 0, eqpLuk = 0;
            int str = 0, dex = 0, luk = 0, int_ = 0;
            
            MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
            Collection<Item> equippedC = iv.list();
            Equip nEquip;

            for (Item item : equippedC) {   //selecting the biggest AP value of each stat from each equipped item.
            	nEquip = (Equip)item;
                if(nEquip.getStr() > eqpStr) eqpStr = nEquip.getStr();
                str += nEquip.getStr();
                
                if(nEquip.getDex() > eqpDex) eqpDex = nEquip.getDex();
                dex += nEquip.getDex();
                
                if(nEquip.getLuk() > eqpLuk) eqpLuk = nEquip.getLuk();
                luk += nEquip.getLuk();
                
                //if(nEquip.getInt() > eqpInt) eqpInt = nEquip.getInt(); //not needed...
                int_ += nEquip.getInt();
            }

            //c.getPlayer().message("----------------------------------------SDL: " + eqpStr + eqpDex + eqpLuk + " BASE STATS --> STR: " + chr.getStr() + " DEX: " + chr.getDex() + " INT: " + chr.getInt() + " LUK: " + chr.getLuk());
            //c.getPlayer().message("SUM EQUIP STATS -> STR: " + str + " DEX: " + dex + " LUK: " + luk + " INT: " + int_);
            
            // ---------- Ronan Lana's AUTOASSIGN -------------
            // This method excels for assigning APs in such a way to cover all equipments AP requirements.
            if (chr.getRemainingAp() < 1) {
                return;
            }
            
            stance = c.getPlayer().getJobStyle();
            int prStat = 0, scStat = 0, trStat = 0, temp, tempAp = chr.getRemainingAp(), CAP;
            
            MapleStat primary, secondary, tertiary = MapleStat.INT;
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
                    
                    if(chr.getStr() >= Math.max(13, (int)(0.4 * chr.getLevel()))) {
                        if(chr.getStr() < 50) {
                            trStat = (chr.getLevel() - 10) - (chr.getStr() + str - eqpStr);
                            if(trStat < 0) trStat = 0;

                            trStat = Math.min(50 - chr.getStr(), trStat);
                            trStat = Math.min(tempAp, trStat);
                            tempAp -= trStat;
                            tertiary = MapleStat.STR;
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
            int total = 0;
            int extras = 0;
            
            total += trStat;
            extras += gainStatByType(chr, tertiary, trStat);
            
            total += scStat;
            extras += gainStatByType(chr, secondary, scStat);
                
            total += prStat;
            extras += gainStatByType(chr, primary, prStat);

            int remainingAp = (chr.getRemainingAp() - total) + extras;
            chr.setRemainingAp(remainingAp);
            chr.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
            c.announce(MaplePacketCreator.enableActions());
            //----------------------------------------------------------------------------------------
            
            c.announce(MaplePacketCreator.serverNotice(1, "Better AP applications detected:\r\nSTR: +" + str + "\r\nDEX: +" + dex + "\r\nINT: +" + int_ + "\r\nLUK: +" + luk));
        }
        else {
            slea.skip(8);
            if (chr.getRemainingAp() < 1) {
                return;
            }
            int total = 0;
            int extras = 0;
            if(slea.available() < 16) {
                AutobanFactory.PACKET_EDIT.alert(chr, "Didn't send full packet for Auto Assign.");
                c.disconnect(false, false);
                return;
            }
            for (int i = 0; i < 2; i++) {
                int type = slea.readInt();
                int tempVal = slea.readInt();
                if (tempVal < 0 || tempVal > c.getPlayer().getRemainingAp()) {
                    return;
                }
                total += tempVal;
                System.out.println(tempVal);
                extras += gainStatByType(chr, MapleStat.getBy5ByteEncoding(type), tempVal);
            }
            int remainingAp = (chr.getRemainingAp() - total) + extras;
            chr.setRemainingAp(remainingAp);
            chr.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
            c.announce(MaplePacketCreator.enableActions());
        }
    }

    private int gainStatByType(MapleCharacter chr, MapleStat type, int gain) {
        int newVal = 0;
        if (type.equals(MapleStat.STR)) {
            newVal = chr.getStr() + gain;
            if (newVal > ServerConstants.MAX_AP) {
                chr.setStr(ServerConstants.MAX_AP);
            } else {
                chr.setStr(newVal);
            }
        } else if (type.equals(MapleStat.INT)) {
            newVal = chr.getInt() + gain;
            if (newVal > ServerConstants.MAX_AP) {
                chr.setInt(ServerConstants.MAX_AP);
            } else {
                chr.setInt(newVal);
            }
        } else if (type.equals(MapleStat.LUK)) {
            newVal = chr.getLuk() + gain;
            if (newVal > ServerConstants.MAX_AP) {
                chr.setLuk(ServerConstants.MAX_AP);
            } else {
                chr.setLuk(newVal);
            }
        } else if (type.equals(MapleStat.DEX)) {
            newVal = chr.getDex() + gain;
            if (newVal > ServerConstants.MAX_AP) {
                chr.setDex(ServerConstants.MAX_AP);
            } else {
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
}
