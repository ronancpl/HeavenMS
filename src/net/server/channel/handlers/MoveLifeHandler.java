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
import java.util.LinkedList;
import java.util.List;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
//import server.life.MobAttackInfo;
//import server.life.MobAttackInfoFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
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
 * @author Ronan (HeavenMS)
 */
public final class MoveLifeHandler extends AbstractMovementPacketHandler {
        
	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
                MapleCharacter player = c.getPlayer();
                MapleMap map = player.getMap();
                
		int objectid = slea.readInt();
		short moveid = slea.readShort();
		MapleMapObject mmo = map.getMapObject(objectid);
		if (mmo == null || mmo.getType() != MapleMapObjectType.MONSTER) {
			return;
		}
                
		MapleMonster monster = (MapleMonster) mmo;
                List<MapleCharacter> banishPlayers = new LinkedList<>();
                
		byte pNibbles = slea.readByte();
		byte rawActivity = slea.readByte();
		slea.readByte();
		slea.readByte();
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
                
                boolean currentController = (monster.getController() == player);
                
		MobSkill toUse = null;
                int useSkillId = 0, useSkillLevel = 0;
                
                if (isSkill) {
                        int noSkills = monster.getNoSkills();
                        if (noSkills > 0) {
                                int rndSkill = Randomizer.nextInt(noSkills);

                                Pair<Integer, Integer> skillToUse = monster.getSkills().get(rndSkill);
                                useSkillId = skillToUse.getLeft();
                                useSkillLevel = skillToUse.getRight();
                                toUse = MobSkillFactory.getMobSkill(useSkillId, useSkillLevel);
                        } else {
                                nextMovementCouldBeSkill = false;
                        }
                } else {
                        nextMovementCouldBeSkill = false;
                }
                
                int mobMp;
                if (toUse != null && toUse.getHP() >= (int) (((float) monster.getHp() / monster.getMaxHp()) * 100) && monster.canUseSkill(toUse)) {
                        mobMp = monster.getMp();
                    
                        int animationTime = MapleMonsterInformationProvider.getInstance().getMobSkillAnimationTime(toUse);
                        if(animationTime > 0) {
                                toUse.applyDelayedEffect(player, monster, true, banishPlayers, animationTime);
                        } else {
                                toUse.applyEffect(player, monster, true, banishPlayers);
                        }
                } else {
                        int atkStatus = monster.canUseAttack((attackId - 13) / 2);
                        if (atkStatus < 1) {
                                if (!currentController) {
                                        return;
                                }
                                
                                mobMp = atkStatus < 0 ? 0 : monster.getMp();
                        } else {
                                mobMp = monster.getMp();
                        }
                        
                        useSkillId = 0;
                        useSkillLevel = 0;
                        rawActivity = -1;
                        pOption = 0;
                        
                        toUse = null;
                }
                
		slea.readByte();
		slea.readInt(); // whatever
		short start_x = slea.readShort(); // hmm.. startpos?
		short start_y = slea.readShort(); // hmm...
		Point startPos = new Point(start_x, start_y - 2);
		List<LifeMovementFragment> res = parseMovement(slea);
		if (!currentController) {
			if (monster.isAttackedBy(player)) {
				monster.switchController(player, true);
			} else {
				return;
			}
		} else if (rawActivity == -1 && monster.isControllerKnowsAboutAggro() && !monster.isMobile() && !monster.isFirstAttack()) {
                        monster.setControllerHasAggro(false);
                        monster.setControllerKnowsAboutAggro(false);
		}
                
		boolean aggro = monster.isControllerHasAggro();
                if (toUse != null) {
                        c.announce(MaplePacketCreator.moveMonsterResponse(objectid, moveid, mobMp, aggro, toUse.getSkillId(), toUse.getSkillLevel()));
		} else {
			c.announce(MaplePacketCreator.moveMonsterResponse(objectid, moveid, mobMp, aggro));
		}
                
		if (aggro) {
			monster.setControllerKnowsAboutAggro(true);
		}
                
		if (res != null) {
                        map.broadcastMessage(player, MaplePacketCreator.moveMonster(objectid, nextMovementCouldBeSkill, rawActivity, useSkillId, useSkillLevel, pOption, startPos, res), monster.getPosition());
			updatePosition(res, monster, -2);
			map.moveMonster(monster, monster.getPosition());
		}
                
                for (MapleCharacter chr : banishPlayers) {
                       chr.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
                }
	}

	private static boolean inRangeInclusive(Byte pVal, Integer pMin, Integer pMax) {
		return !(pVal < pMin) || (pVal > pMax);
	}
}