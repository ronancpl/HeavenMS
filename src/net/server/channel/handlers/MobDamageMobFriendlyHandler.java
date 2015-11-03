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

import net.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;

/**
 *
 * @author Xotic & BubblesDev
 */

public final class MobDamageMobFriendlyHandler extends AbstractMaplePacketHandler {
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int attacker = slea.readInt();
		slea.readInt();
		int damaged = slea.readInt();
		MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(damaged);

		if (monster == null || c.getPlayer().getMap().getMonsterByOid(attacker) == null) {
			return;
		}

		int damage = Randomizer.nextInt(((monster.getMaxHp() / 13 + monster.getPADamage() * 10)) * 2 + 500) / 10; //Beng's formula.
		//  int damage = monster.getStats().getPADamage() + monster.getStats().getPDDamage() - 1;

		if (monster.getId() == 9300061) {
			if (monster.getHp() - damage < 1) {
				monster.getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, "The Moon Bunny went home because he was sick."));
				c.getPlayer().getEventInstance().getMapInstance(monster.getMap().getId()).killFriendlies(monster);
			}
			MapleMap map = c.getPlayer().getEventInstance().getMapInstance(monster.getMap().getId());
			map.addBunnyHit();
		}

		c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.MobDamageMobFriendly(monster, damage), monster.getPosition());
		c.announce(MaplePacketCreator.enableActions());
	}
}