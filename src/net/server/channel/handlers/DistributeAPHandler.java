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

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import constants.ServerConstants;
import constants.skills.BlazeWizard;
import constants.skills.Brawler;
import constants.skills.DawnWarrior;
import constants.skills.Magician;
import constants.skills.Warrior;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DistributeAPHandler extends AbstractMaplePacketHandler {
    private static final int max = 32767;

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        int num = slea.readInt();
        if (c.getPlayer().getRemainingAp() > 0) {
            if (addStat(c, num, false)) {
                c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - 1);
                c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
            }
        }
        c.announce(MaplePacketCreator.enableActions());
    }

    public static boolean addStat(MapleClient c, int apTo, boolean usedAPReset) {
        switch (apTo) {
            case 64: // Str
                if (c.getPlayer().getStr() >= max) {
                    return false;
                }
                c.getPlayer().addStat(1, 1);
                break;
            case 128: // Dex
                if (c.getPlayer().getDex() >= max) {
                    return false;
                }
                c.getPlayer().addStat(2, 1);
                break;
            case 256: // Int
                if (c.getPlayer().getInt() >= max) {
                    return false;
                }
                c.getPlayer().addStat(3, 1);
                break;
            case 512: // Luk
                if (c.getPlayer().getLuk() >= max) {
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
    
    public static int takeHp(MapleCharacter player, MapleJob job) {
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
    
    public static int takeMp(MapleCharacter player, MapleJob job) {
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
