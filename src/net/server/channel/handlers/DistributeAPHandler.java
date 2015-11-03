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
import constants.skills.BlazeWizard;
import constants.skills.Brawler;
import constants.skills.DawnWarrior;
import constants.skills.Magician;
import constants.skills.Warrior;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DistributeAPHandler extends AbstractMaplePacketHandler {
    private static final int max = 32767;

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        int num = slea.readInt();
        if (c.getPlayer().getRemainingAp() > 0) {
            if (addStat(c, num)) { 
                c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - 1);
                c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
            }
        }
        c.announce(MaplePacketCreator.enableActions());
    }
    
    static boolean addStat(MapleClient c, int apTo) {
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
                addHP(c.getPlayer(), addHP(c));
                break;
            case 8192: // MP
                addMP(c.getPlayer(), addMP(c));
                break;
            default:
                c.announce(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, c.getPlayer()));
                return false;
        }
        return true;
    }

    static int addHP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleJob job = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpMpApUsed() > 9999 || MaxHP >= 30000) {
            return MaxHP;
        }
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            Skill increaseHP = SkillFactory.getSkill(job.isA(MapleJob.DAWNWARRIOR1) ? DawnWarrior.MAX_HP_INCREASE : Warrior.IMPROVED_MAXHP);
            int sLvl = player.getSkillLevel(increaseHP);
            
            if(sLvl > 0)
                MaxHP += increaseHP.getEffect(sLvl).getY();
            
            MaxHP += 20;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 6;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 16;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            Skill increaseHP = SkillFactory.getSkill(Brawler.IMPROVE_MAX_HP);
            int sLvl = player.getSkillLevel(increaseHP);
            
            if(sLvl > 0)
                MaxHP += increaseHP.getEffect(sLvl).getY();
            
            MaxHP += 18;
        } else {
            MaxHP += 8;
        }
        return MaxHP;
    }

    static int addMP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        MapleJob job = player.getJob();
        if (player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            MaxMP += 2;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            Skill increaseMP = SkillFactory.getSkill(job.isA(MapleJob.BLAZEWIZARD1) ? BlazeWizard.INCREASING_MAX_MP : Magician.IMPROVED_MAX_MP_INCREASE);
            int sLvl = player.getSkillLevel(increaseMP);
            
            if(sLvl > 0)
                MaxMP += increaseMP.getEffect(sLvl).getY();
            
            MaxMP += 18;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxMP += 10;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            MaxMP += 14;
        } else {
            MaxMP += 6;
        }
        return MaxMP;
    }

    static void addHP(MapleCharacter player, int MaxHP) {
        MaxHP = Math.min(30000, MaxHP);
        player.setHpMpApUsed(player.getHpMpApUsed() + 1);
        player.setMaxHp(MaxHP);
        player.updateSingleStat(MapleStat.MAXHP, MaxHP);
    }

    static void addMP(MapleCharacter player, int MaxMP) {
        MaxMP = Math.min(30000, MaxMP);
        player.setHpMpApUsed(player.getHpMpApUsed() + 1);
        player.setMaxMp(MaxMP);
        player.updateSingleStat(MapleStat.MAXMP, MaxMP);
    }
}
