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

import java.awt.Point;

import net.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import constants.ServerConstants;
import constants.skills.Brawler;
import constants.skills.Corsair;
import constants.skills.DarkKnight;
import constants.skills.Hero;
import constants.skills.Paladin;
import constants.skills.Priest;
import constants.skills.SuperGM;


public final class SpecialMoveHandler extends AbstractMaplePacketHandler {
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	MapleCharacter chr = c.getPlayer();
        chr.getAutobanManager().setTimestamp(4, slea.readInt(), 3);
        int skillid = slea.readInt();
        
        /*
        if ((!GameConstants.isPqSkillMap(chr.getMapId()) && GameConstants.isPqSkill(skillid)) || (!chr.isGM() && GameConstants.isGMSkills(skillid)) || (!GameConstants.isInJobTree(skillid, chr.getJob().getId()) && !chr.isGM())) {
        	AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit skills.");
        	FilePrinter.printError(FilePrinter.EXPLOITS + chr.getName() + ".txt", chr.getName() + " tried to use skill " + skillid + " without it being in their job.\r\n");
    		c.disconnect(true, false);
            return;
        }
        */
        
        Point pos = null;
        int __skillLevel = slea.readByte();
        Skill skill = SkillFactory.getSkill(skillid);
        int skillLevel = chr.getSkillLevel(skill);
        if (skillid % 10000000 == 1010 || skillid % 10000000 == 1011) {
            if (chr.getDojoEnergy() < 10000) { // PE hacking or maybe just lagging
                return;
            }
            skillLevel = 1;
            chr.setDojoEnergy(0);
            c.announce(MaplePacketCreator.getEnergy("energy", chr.getDojoEnergy()));
            c.announce(MaplePacketCreator.serverNotice(5, "As you used the secret skill, your energy bar has been reset."));
        }
        if (skillLevel == 0 || skillLevel != __skillLevel) return;
        
        MapleStatEffect effect = skill.getEffect(skillLevel);
        if (effect.getCooldown() > 0) {
            if (chr.skillIsCooling(skillid)) {
                return;
            } else if (skillid != Corsair.BATTLE_SHIP) {
                int cooldownTime = effect.getCooldown();
                if(MapleStatEffect.isHerosWill(skillid) && ServerConstants.USE_FAST_REUSE_HERO_WILL) {
                    cooldownTime /= 60;
                }
                
                c.announce(MaplePacketCreator.skillCooldown(skillid, cooldownTime));
                chr.addCooldown(skillid, currentServerTime(), cooldownTime * 1000);
            }
        }
        if (skillid == Hero.MONSTER_MAGNET || skillid == Paladin.MONSTER_MAGNET || skillid == DarkKnight.MONSTER_MAGNET) { // Monster Magnet
            int num = slea.readInt();
            int mobId;
            byte success;
            for (int i = 0; i < num; i++) {
                mobId = slea.readInt();
                success = slea.readByte();
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.showMagnet(mobId, success), false);
                MapleMonster monster = chr.getMap().getMonsterByOid(mobId);
                if (monster != null) {
                	if (!monster.isBoss()) {
                		monster.switchController(chr, monster.isControllerHasAggro());
                	}
                }
            }
            byte direction = slea.readByte();
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr.getId(), skillid, chr.getSkillLevel(skillid), direction), false);
            c.announce(MaplePacketCreator.enableActions());
            return;
        } else if (skillid == Brawler.MP_RECOVERY) {// MP Recovery
            Skill s = SkillFactory.getSkill(skillid);
            MapleStatEffect ef = s.getEffect(chr.getSkillLevel(s));
            
            int lose = chr.safeAddHP(-1 * (chr.getCurrentMaxHp() / ef.getX()));
            int gain = -lose * (ef.getY() / 100);
            chr.addMP(gain);
        } else if (skillid == SuperGM.HEAL_PLUS_DISPEL) {
            slea.skip(11);
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr.getId(), skillid, chr.getSkillLevel(skillid)), false);
        } else if (skillid % 10000000 == 1004) {
            slea.readShort();
        }
        
        if (slea.available() == 5) {
            pos = new Point(slea.readShort(), slea.readShort());
        }
        if (chr.isAlive()) {
            if (skill.getId() != Priest.MYSTIC_DOOR) {
                skill.getEffect(skillLevel).applyTo(chr, pos);
            } else if(chr.canDoor()) {
                //update door lists
                chr.cancelMagicDoor();
                skill.getEffect(skillLevel).applyTo(chr, pos);
            } else {
                chr.message("Please wait 5 seconds before casting Mystic Door again.");
                c.announce(MaplePacketCreator.enableActions());
            }
        } else {
            c.announce(MaplePacketCreator.enableActions());
        }
    }
}