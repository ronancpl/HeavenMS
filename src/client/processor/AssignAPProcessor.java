/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2018 RonanLana (HeavenMS)

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
package client.processor;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ServerConstants;
import constants.skills.BlazeWizard;
import constants.skills.Brawler;
import constants.skills.DawnWarrior;
import constants.skills.Magician;
import constants.skills.Warrior;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author RonanLana (synchronization of AP transaction modules)
 */
public class AssignAPProcessor {
    
    public static void APAutoAssignAction(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr.getRemainingAp() < 1) return;
        
        Collection<Item> equippedC = chr.getInventory(MapleInventoryType.EQUIPPED).list();
        
        c.lockClient();
        try {
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
                if (tempAp < 1) return;

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
                    
                    final MapleClient client = c;
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            client.disconnect(false, false);
                        }
                    });
                    t.start();
                    
                    return;
                }

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
                    if (tempVal < 0 || tempVal > chr.getRemainingAp()) {
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
        } finally {
            c.unlockClient();
        }
    }
    
    private static int getNthHighestStat(List<Short> statList, short rank) {    // ranks from 0
        return(statList.size() <= rank ? 0 : statList.get(rank));
    }
    
    private static int gainStatByType(MapleCharacter chr, MapleStat type, int[] statGain, int gain) {
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
    
    private static MapleStat getQuaternaryStat(MapleJob stance) {
        if(stance != MapleJob.MAGICIAN) return MapleStat.INT;
        return MapleStat.STR;
    }
    
    private static int getAccumulatedStatGain(int[] statGain) {
        int acc = 0;
        
        for(byte i = 0; i < statGain.length; i++) {
            acc += statGain[i];
        }
        
        return acc;
    }
    
    public static boolean APResetAction(MapleClient c, int APFrom, int APTo) {
        c.lockClient();
        try {
            List<Pair<MapleStat, Integer>> statupdate = new ArrayList<>(2);
            MapleCharacter player = c.getPlayer();

            switch (APFrom) {
                case 64: // str
                    if (player.getStr() < 5) {
                        player.message("You don't have the minimum STR required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    player.addStat(1, -1);
                    break;
                case 128: // dex
                    if (player.getDex() < 5) {
                        player.message("You don't have the minimum DEX required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    player.addStat(2, -1);
                    break;
                case 256: // int
                    if (player.getInt() < 5) {
                        player.message("You don't have the minimum INT required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    player.addStat(3, -1);
                    break;
                case 512: // luk
                    if (player.getLuk() < 5) {
                        player.message("You don't have the minimum LUK required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    player.addStat(4, -1);
                    break;
                case 2048: // HP
                    if(ServerConstants.USE_ENFORCE_HPMP_SWAP) {
                        if (APTo != 8192) {
                            player.message("You can only swap HP ability points to MP.");
                            c.announce(MaplePacketCreator.enableActions());
                            return false;
                        }
                    }
                    if (player.getHpMpApUsed() < 1) {
                        player.message("You don't have enough HPMP stat points to spend on AP Reset.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }

                    int hp = player.getMaxHp();
                    int level_ = player.getLevel();

                    boolean canWash_ = true;
                    if (hp < level_ * 14 + 148) {
                        canWash_ = false;
                    }

                    if (!canWash_) {
                        player.message("You don't have the minimum HP pool required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }

                    player.setHpMpApUsed(player.getHpMpApUsed() - 1);
                    int hplose = -takeHp(player.getJob());
                    int nextHp = Math.max(1, player.getHp() + hplose), nextMaxHp = Math.max(50, player.getMaxHp() + hplose);

                    player.setHp(nextHp);
                    player.setMaxHp(nextMaxHp);
                    statupdate.add(new Pair<>(MapleStat.HP, nextHp));
                    statupdate.add(new Pair<>(MapleStat.MAXHP, nextMaxHp));

                    break;
                case 8192: // MP
                    if(ServerConstants.USE_ENFORCE_HPMP_SWAP) {
                        if (APTo != 2048) {
                            player.message("You can only swap MP ability points to HP.");
                            c.announce(MaplePacketCreator.enableActions());
                            return false;
                        }
                    }
                    if (player.getHpMpApUsed() < 1) {
                        player.message("You don't have enough HPMP stat points to spend on AP Reset.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }

                    int mp = player.getMaxMp();
                    int level = player.getLevel();
                    MapleJob job = player.getJob();

                    boolean canWash = true;
                    if (job.isA(MapleJob.SPEARMAN) && mp < 4 * level + 156) {
                        canWash = false;
                    } else if (job.isA(MapleJob.FIGHTER) && mp < 4 * level + 56) {
                        canWash = false;
                    } else if (job.isA(MapleJob.THIEF) && job.getId() % 100 > 0 && mp < level * 14 - 4) {
                        canWash = false;
                    } else if (mp < level * 14 + 148) {
                        canWash = false;
                    }

                    if (!canWash) {
                        player.message("You don't have the minimum MP pool required to swap.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }

                    player.setHpMpApUsed(player.getHpMpApUsed() - 1);
                    int mplose = -takeMp(job);
                    int nextMp = Math.max(0, player.getMp() + mplose), nextMaxMp = Math.max(5, player.getMaxMp() + mplose);

                    player.setMp(nextMp);
                    player.setMaxMp(nextMaxMp);
                    statupdate.add(new Pair<>(MapleStat.MP, nextMp));
                    statupdate.add(new Pair<>(MapleStat.MAXMP, nextMaxMp));

                    break;
                default:
                    c.announce(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, player));
                    return false;
            }

            addStat(c, APTo, true);
            c.announce(MaplePacketCreator.updatePlayerStats(statupdate, true, player));
            return true;
        } finally {
            c.unlockClient();
        }
    }
    
    public static void APAssignAction(MapleClient c, int num) {
        c.lockClient();
        try {
            if (c.getPlayer().getRemainingAp() > 0) {
                if (addStat(c, num, false)) {
                    c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - 1);
                    c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
                }
            }
            c.announce(MaplePacketCreator.enableActions());
        } finally {
            c.unlockClient();
        }
    }
    
    private static boolean addStat(MapleClient c, int apTo, boolean usedAPReset) {
        switch (apTo) {
            case 64: // Str
                if (c.getPlayer().getStr() >= 32767) {
                    return false;
                }
                c.getPlayer().addStat(1, 1);
                break;
            case 128: // Dex
                if (c.getPlayer().getDex() >= 32767) {
                    return false;
                }
                c.getPlayer().addStat(2, 1);
                break;
            case 256: // Int
                if (c.getPlayer().getInt() >= 32767) {
                    return false;
                }
                c.getPlayer().addStat(3, 1);
                break;
            case 512: // Luk
                if (c.getPlayer().getLuk() >= 32767) {
                    return false;
                }
                c.getPlayer().addStat(4, 1);
                break;
            case 2048: // HP
                addHP(c.getPlayer(), addHP(c, usedAPReset));
                break;
            case 8192: // MP
                addMP(c.getPlayer(), addMP(c, usedAPReset));
                break;
            default:
                c.announce(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, c.getPlayer()));
                return false;
        }
        return true;
    }

    private static int addHP(MapleClient c, boolean usedAPReset) {
        MapleCharacter player = c.getPlayer();
        MapleJob job = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpMpApUsed() > 9999 || MaxHP >= 30000) {
            return MaxHP;
        }
        
        return MaxHP + calcHpChange(player, job, usedAPReset);
    }
    
    private static int calcHpChange(MapleCharacter player, MapleJob job, boolean usedAPReset) {
        int MaxHP = 0;
        
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            if(!usedAPReset) {
                Skill increaseHP = SkillFactory.getSkill(job.isA(MapleJob.DAWNWARRIOR1) ? DawnWarrior.MAX_HP_INCREASE : Warrior.IMPROVED_MAXHP);
                int sLvl = player.getSkillLevel(increaseHP);

                if(sLvl > 0)
                    MaxHP += increaseHP.getEffect(sLvl).getY();
            }
            
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if (usedAPReset) {
                    MaxHP += 20;
                } else {
                    MaxHP += Randomizer.rand(18, 22);
                }
            } else {
                MaxHP += 20;
            }
        } else if(job.isA(MapleJob.ARAN1)) {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if (usedAPReset) {
                    MaxHP += 20;
                } else {
                    MaxHP += Randomizer.rand(26, 30);
                }
            } else {
                MaxHP += 28;
            }
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if (usedAPReset) {
                    MaxHP += 6;
                } else {
                    MaxHP += Randomizer.rand(5, 9);
                }
            } else {
                MaxHP += 6;
            }
        } else if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if (usedAPReset) {
                    MaxHP += 16;
                } else {
                    MaxHP += Randomizer.rand(14, 18);
                }
            } else {
                MaxHP += 16;
            }
        } else if(job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1)) {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if (usedAPReset) {
                    MaxHP += 16;
                } else {
                    MaxHP += Randomizer.rand(14, 18);
                }
            } else {
                MaxHP += 16;
            }
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            if(!usedAPReset) {
                Skill increaseHP = SkillFactory.getSkill(Brawler.IMPROVE_MAX_HP);
                int sLvl = player.getSkillLevel(increaseHP);

                if(sLvl > 0)
                    MaxHP += increaseHP.getEffect(sLvl).getY();
            }
            
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if (usedAPReset) {
                    MaxHP += 18;
                } else {
                    MaxHP += Randomizer.rand(16, 20);
                }
            } else {
                MaxHP += 18;
            }
        } else if (usedAPReset) {
            MaxHP += 8;
        } else {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                MaxHP += Randomizer.rand(8, 12);
            } else {
                MaxHP += 10;
            }
        }
        
        return MaxHP;
    }

    private static int addMP(MapleClient c, boolean usedAPReset) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        MapleJob job = player.getJob();
        if (player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        
        return MaxMP + calcMpChange(player, job, usedAPReset);
    }
    
    private static int calcMpChange(MapleCharacter player, MapleJob job, boolean usedAPReset) {
        int MaxMP = 0;
        
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if(!usedAPReset) {
                    MaxMP += (Randomizer.rand(2, 4) + (player.getInt() / 10));
                } else {
                    MaxMP += 2;
                }
            } else {
                MaxMP += 3;
            }
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            if(!usedAPReset) {
                Skill increaseMP = SkillFactory.getSkill(job.isA(MapleJob.BLAZEWIZARD1) ? BlazeWizard.INCREASING_MAX_MP : Magician.IMPROVED_MAX_MP_INCREASE);
                int sLvl = player.getSkillLevel(increaseMP);

                if(sLvl > 0)
                    MaxMP += increaseMP.getEffect(sLvl).getY();
            }
            
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if(!usedAPReset) {
                    MaxMP += (Randomizer.rand(12, 16) + (player.getInt() / 20));
                } else {
                    MaxMP += 18;
                }
            } else {
                MaxMP += 18;
            }
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1)) {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if(!usedAPReset) {
                    MaxMP += (Randomizer.rand(6, 8) + (player.getInt() / 10));
                } else {
                    MaxMP += 10;
                }
            } else {
                MaxMP += 10;
            }
        } else if(job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if(!usedAPReset) {
                    MaxMP += (Randomizer.rand(6, 8) + (player.getInt() / 10));
                } else {
                    MaxMP += 10;
                }
            } else {
                MaxMP += 10;
            }
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if(!usedAPReset) {
                    MaxMP += (Randomizer.rand(7, 9) + (player.getInt() / 10));
                } else {
                    MaxMP += 14;
                }
            } else {
                MaxMP += 14;
            }
        } else {
            if(ServerConstants.USE_RANDOMIZE_HPMP_GAIN) {
                if(!usedAPReset) {
                    MaxMP += (Randomizer.rand(4, 6) + (player.getInt() / 10));
                } else {
                    MaxMP += 6;
                }
            } else {
                MaxMP += 6;
            }
        }
        
        return MaxMP;
    }

    private static void addHP(MapleCharacter player, int MaxHP) {
        MaxHP = Math.min(30000, MaxHP);
        player.setHpMpApUsed(player.getHpMpApUsed() + 1);
        player.setMaxHp(MaxHP);
        player.updateSingleStat(MapleStat.MAXHP, MaxHP);
    }

    private static void addMP(MapleCharacter player, int MaxMP) {
        MaxMP = Math.min(30000, MaxMP);
        player.setHpMpApUsed(player.getHpMpApUsed() + 1);
        player.setMaxMp(MaxMP);
        player.updateSingleStat(MapleStat.MAXMP, MaxMP);
    }
    
    private static int takeHp(MapleJob job) {
        int MaxHP = 0;
        
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            MaxHP += 54;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 10;
        } else if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 20;
        } else if(job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1)) {
            MaxHP += 20;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            MaxHP += 42;
        } else {
            MaxHP += 12;
        }
        
        return MaxHP;
    }
    
    private static int takeMp(MapleJob job) {
        int MaxMP = 0;
        
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            MaxMP += 4;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            MaxMP += 31;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1)) {
            MaxMP += 12;
        } else if(job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxMP += 12;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            MaxMP += 16;
        } else {
            MaxMP += 8;
        }
        
        return MaxMP;
    }
    
}
