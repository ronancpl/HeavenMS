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
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobAttackInfoFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Danny (Leifde)
 * @author ExtremeDevilz
 */
public final class MoveLifeHandler extends AbstractMovementPacketHandler {
	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int objectid = slea.readInt();
		short moveid = slea.readShort();
		MapleMapObject mmo = c.getPlayer().getMap().getMapObject(objectid);
		if (mmo == null || mmo.getType() != MapleMapObjectType.MONSTER) {
			return;
		}
		MapleMonster monster = (MapleMonster) mmo;
		List<LifeMovementFragment> res = null;
                List<MapleCharacter> banishPlayers = new ArrayList<>();
		byte pNibbles = slea.readByte();
		byte rawActivity = slea.readByte();
		byte useSkillId = slea.readByte();
		byte useSkillLevel = slea.readByte();
		short pOption = slea.readShort();
                slea.skip(8);

		if (rawActivity >= 0) {
			rawActivity = (byte) (rawActivity & 0xFF >> 1);
		}

		boolean isAttack = inRangeInclusive(rawActivity, 12, 20);
		boolean isSkill = inRangeInclusive(rawActivity, 21, 25);

		byte attackId = (byte) (isAttack ? rawActivity - 12 : -1);
		boolean nextMovementCouldBeSkill = (pNibbles & 0x0F) != 0;
		boolean pUnk = (pNibbles & 0xF0) != 0;

		int nextCastSkill = useSkillId;
		int nextCastSkillLevel = useSkillLevel;
		
		MobSkill toUse = null;
		
		int percHpLeft = (int) (((float) monster.getHp() / monster.getMaxHp()) * 100);
                
		if (nextMovementCouldBeSkill && monster.getNoSkills() > 0) {
			int Random = Randomizer.nextInt(monster.getNoSkills());
			Pair<Integer, Integer> skillToUse = monster.getSkills().get(Random);
			nextCastSkill = skillToUse.getLeft();
			nextCastSkillLevel = skillToUse.getRight();
			toUse = MobSkillFactory.getMobSkill(nextCastSkill, nextCastSkillLevel);

			if (isSkill || isAttack) {
				if (nextCastSkill != toUse.getSkillId() || nextCastSkillLevel != toUse.getSkillLevel()) {
					//toUse.resetAnticipatedSkill();
					return;
				} else if (toUse.getHP() < percHpLeft) {
					toUse = null;
				} else if (monster.canUseSkill(toUse)) {
					toUse.applyEffect(c.getPlayer(), monster, true, banishPlayers);
					//System.out.println("Applied: " + nextCastSkill + " Level: " + nextCastSkillLevel);
				}
			} else {
				MobAttackInfo mobAttack = MobAttackInfoFactory.getMobAttackInfo(monster, attackId);
				//System.out.println("Attacked");
			}
		}

		slea.readByte();
		slea.readInt(); // whatever
		short start_x = slea.readShort(); // hmm.. startpos?
		short start_y = slea.readShort(); // hmm...
		Point startPos = new Point(start_x, start_y);
		res = parseMovement(slea);
		if (monster.getController() != c.getPlayer()) {
			if (monster.isAttackedBy(c.getPlayer())) {
				monster.switchController(c.getPlayer(), true);
			} else {
				return;
			}
		} else if (rawActivity == -1 && monster.isControllerKnowsAboutAggro() && !monster.isMobile() && !monster.isFirstAttack()) {
			monster.setControllerHasAggro(false);
			monster.setControllerKnowsAboutAggro(false);
		}
		boolean aggro = monster.isControllerHasAggro();
		if (toUse != null) {
			c.announce(MaplePacketCreator.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro, toUse.getSkillId(), toUse.getSkillLevel()));
		} else {
			c.announce(MaplePacketCreator.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro));
		}
		if (aggro) {
			monster.setControllerKnowsAboutAggro(true);
		}
		if (res != null) {
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.moveMonster(objectid, nextMovementCouldBeSkill, rawActivity, useSkillId, useSkillLevel, pOption, startPos, res), monster.getPosition());
			updatePosition(res, monster, -1);
			c.getPlayer().getMap().moveMonster(monster, monster.getPosition());
		}
                
                for (MapleCharacter chr : banishPlayers) {
                       chr.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
                }
	}

	public static boolean inRangeInclusive(Byte pVal, Integer pMin, Integer pMax) {
		return !(pVal < pMin) || (pVal > pMax);
	}
}