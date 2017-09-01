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
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCharacter.CancelCooldownAction;
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
		MapleCharacter player = c.getPlayer();
                player.setPetLootCd(System.currentTimeMillis());

		/*long timeElapsed = System.currentTimeMillis() - player.getAutobanManager().getLastSpam(8);
		if(timeElapsed < 300) {
			AutobanFactory.FAST_ATTACK.alert(player, "Time: " + timeElapsed);
		}
		player.getAutobanManager().spam(8);*/

		AttackInfo attack = parseDamage(slea, player, false, true);
                
		if (player.getBuffEffect(MapleBuffStat.MORPH) != null) {
			if(player.getBuffEffect(MapleBuffStat.MORPH).isMorphWithoutAttack()) {
				// How are they attacking when the client won't let them?
				player.getClient().disconnect(false, false);
				return; 
			}
		}
                
                if (player.getMap().isDojoMap() && attack.numAttacked > 0) {
                        player.setDojoEnergy(player.getDojoEnergy() +  + ServerConstants.DOJO_ENERGY_ATK);
                        c.announce(MaplePacketCreator.getEnergy("energy", player.getDojoEnergy()));
                }

                int charge = (attack.skill == Evan.FIRE_BREATH || attack.skill == Evan.ICE_BREATH || attack.skill == FPArchMage.BIG_BANG || attack.skill == ILArchMage.BIG_BANG || attack.skill == Bishop.BIG_BANG) ? attack.charge : -1;
                byte[] packet = MaplePacketCreator.magicAttack(player, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, attack.allDamage, charge, attack.speed, attack.direction, attack.display);
		
		player.getMap().broadcastMessage(player, packet, false, true);
		MapleStatEffect effect = attack.getAttackEffect(player, null);
		Skill skill = SkillFactory.getSkill(attack.skill);
		MapleStatEffect effect_ = skill.getEffect(player.getSkillLevel(skill));
		if (effect_.getCooldown() > 0) {
			if (player.skillIsCooling(attack.skill)) {
				return;
			} else {
				c.announce(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
				player.addCooldown(attack.skill, System.currentTimeMillis(), effect_.getCooldown() * 1000, TimerManager.getInstance().schedule(new CancelCooldownAction(player, attack.skill), effect_.getCooldown() * 1000));
			}
		}
		applyAttack(attack, player, effect.getAttackCount());
		Skill eaterSkill = SkillFactory.getSkill((player.getJob().getId() - (player.getJob().getId() % 10)) * 10000);// MP Eater, works with right job
		int eaterLevel = player.getSkillLevel(eaterSkill);
		if (eaterLevel > 0) {
			for (Integer singleDamage : attack.allDamage.keySet()) {
				eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getMapObject(singleDamage), 0);
			}
		}
	}
}
