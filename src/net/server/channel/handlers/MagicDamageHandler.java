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

import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import constants.ServerConstants;
import constants.skills.Bishop;
import constants.skills.Evan;
import constants.skills.FPArchMage;
import constants.skills.ILArchMage;

public final class MagicDamageHandler extends AbstractDealDamageHandler {
	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter chr = c.getPlayer();
                //chr.setPetLootCd(currentServerTime());

		/*long timeElapsed = currentServerTime() - chr.getAutobanManager().getLastSpam(8);
		if(timeElapsed < 300) {
			AutobanFactory.FAST_ATTACK.alert(chr, "Time: " + timeElapsed);
		}
		chr.getAutobanManager().spam(8);*/

		AttackInfo attack = parseDamage(slea, chr, false, true);
                
		if (chr.getBuffEffect(MapleBuffStat.MORPH) != null) {
			if(chr.getBuffEffect(MapleBuffStat.MORPH).isMorphWithoutAttack()) {
				// How are they attacking when the client won't let them?
				chr.getClient().disconnect(false, false);
				return; 
			}
		}
                
                if (chr.getMap().isDojoMap() && attack.numAttacked > 0) {
                        chr.setDojoEnergy(chr.getDojoEnergy() +  + ServerConstants.DOJO_ENERGY_ATK);
                        c.announce(MaplePacketCreator.getEnergy("energy", chr.getDojoEnergy()));
                }

                int charge = (attack.skill == Evan.FIRE_BREATH || attack.skill == Evan.ICE_BREATH || attack.skill == FPArchMage.BIG_BANG || attack.skill == ILArchMage.BIG_BANG || attack.skill == Bishop.BIG_BANG) ? attack.charge : -1;
                byte[] packet = MaplePacketCreator.magicAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, attack.allDamage, charge, attack.speed, attack.direction, attack.display);
		
		chr.getMap().broadcastMessage(chr, packet, false, true);
		MapleStatEffect effect = attack.getAttackEffect(chr, null);
		Skill skill = SkillFactory.getSkill(attack.skill);
		MapleStatEffect effect_ = skill.getEffect(chr.getSkillLevel(skill));
		if (effect_.getCooldown() > 0) {
			if (chr.skillIsCooling(attack.skill)) {
				return;
			} else {
				c.announce(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
				chr.addCooldown(attack.skill, currentServerTime(), effect_.getCooldown() * 1000);
			}
		}
		applyAttack(attack, chr, effect.getAttackCount());
		Skill eaterSkill = SkillFactory.getSkill((chr.getJob().getId() - (chr.getJob().getId() % 10)) * 10000);// MP Eater, works with right job
		int eaterLevel = chr.getSkillLevel(eaterSkill);
		if (eaterLevel > 0) {
			for (Integer singleDamage : attack.allDamage.keySet()) {
				eaterSkill.getEffect(eaterLevel).applyPassive(chr, chr.getMap().getMapObject(singleDamage), 0);
			}
		}
	}
}
