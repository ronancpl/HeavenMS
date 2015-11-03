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
import net.AbstractMaplePacketHandler;
import server.partyquest.MonsterCarnival;
import server.life.MapleLifeFactory;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public final class MonsterCarnivalHandler extends AbstractMaplePacketHandler{
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        MonsterCarnival carnival = chr.getCarnival();
        int tab = slea.readByte();
        int number = slea.readShort();
        if (carnival != null) {
            if (chr.getCarnivalParty() != carnival.getPartyRed() || chr.getCarnivalParty() != carnival.getPartyBlue()) {
                chr.getMap().broadcastMessage(MaplePacketCreator.leaveCPQ(chr));
                chr.changeMap(980000010);
            }
            if (chr.getCP() > getPrice(tab, number)) {
                if (tab == 0) { //SPAWNING
                    if (chr.getCarnivalParty().canSummon()) {
                        chr.getMap().spawnCPQMonster(MapleLifeFactory.getMonster(getMonster(number)), new Point(1, 1), carnival.oppositeTeam(chr.getCarnivalParty()).getTeam());
                        chr.getCarnivalParty().summon();
                    } else
                        chr.announce(MaplePacketCreator.CPQMessage((byte) 2));

                } else if (tab == 1) {

                } else if (tab == 2) {
                    int rid = 9980000 + chr.getTeam();
                        MapleReactor reactor = new MapleReactor(MapleReactorFactory.getReactor(rid), rid);
                        /*switch (number) {
                            case 0:
                                reactor.setMonsterStatus(tab, MonsterStatus.WEAPON_ATTACK_UP, MobSkillFactory.getMobSkill(150, 1));
                                break;
                            case 1:
                                reactor.setMonsterStatus(tab, MonsterStatus.WEAPON_DEFENSE_UP, MobSkillFactory.getMobSkill(151, 1));
                                break;
                            case 2:
                                reactor.setMonsterStatus(tab, MonsterStatus.MAGIC_ATTACK_UP, MobSkillFactory.getMobSkill(152, 1));
                                break;
                            case 3:
                                reactor.setMonsterStatus(tab, MonsterStatus.MAGIC_DEFENSE_UP, MobSkillFactory.getMobSkill(153, 1));
                                break;
                            case 4:
                                reactor.setMonsterStatus(tab, MonsterStatus.ACC, MobSkillFactory.getMobSkill(154, 1));
                                break;
                            case 5:
                                reactor.setMonsterStatus(tab, MonsterStatus.AVOID, MobSkillFactory.getMobSkill(155, 1));
                                break;
                            case 6:
                                reactor.setMonsterStatus(tab, MonsterStatus.SPEED, MobSkillFactory.getMobSkill(156, 1));
                                break;
                            case 7:
                                reactor.setMonsterStatus(tab, MonsterStatus.WEAPON_IMMUNITY, MobSkillFactory.getMobSkill(140, 1));
                                break;
                            case 8:
                                reactor.setMonsterStatus(tab, MonsterStatus.MAGIC_IMMUNITY, MobSkillFactory.getMobSkill(141, 1));
                                break;
                        } */
                        chr.getMap().spawnReactor(reactor);
                }
            } else {
                chr.getMap().broadcastMessage(MaplePacketCreator.CPQMessage((byte) 1));
            }
        } else {
            chr.announce(MaplePacketCreator.CPQMessage((byte) 5));
        }
        chr.announce(MaplePacketCreator.enableActions());
    }

    public int getMonster(int num) {
        int mid = 0;
        num++;
        switch (num) {
            case 1:
                mid = 9300127;
                break;
            case 2:
                mid = 9300128;
                break;
            case 3:
                mid = 9300129;
                break;
            case 4:
                mid = 9300130;
                break;
            case 5:
                mid = 9300131;
                break;
            case 6:
                mid = 9300132;
                break;
            case 7:
                mid = 9300133;
                break;
            case 8:
                mid = 9300134;
                break;
            case 9:
                mid = 9300135;
                break;
            case 10:
                mid = 9300136;
                break;
        }
        return mid;
    }

    public int getPrice(int num, int tab) {
        int price = 0;
        num++;

        if (tab == 0) {
            switch (num) {
                case 1:
                case 2:
                    price = 7;
                    break;
                case 3:
                case 4:
                    price = 8;
                    break;
                case 5:
                case 6:
                    price = 9;
                    break;
                case 7:
                    price = 10;
                    break;
                case 8:
                    price = 11;
                    break;
                case 9:
                    price = 12;
                    break;
                case 10:
                    price = 30;
                    break;
            }
        } else if (tab == 1) {
            switch (num) {
                case 1:
                    price = 17;
                    break;
                case 2:
                case 4:
                    price = 19;
                    break;
                case 3:
                    price = 12;
                    break;
                case 5:
                    price = 16;
                    break;
                case 6:
                    price = 14;
                    break;
                case 7:
                    price = 22;
                    break;
                case 8:
                    price = 18;
                    break;
            }
        } else {
            switch (num) {
                case 1:
                case 3:
                    price = 17;
                    break;
                case 2:
                case 4:
                case 6:
                    price = 16;
                    break;
                case 5:
                    price = 13;
                    break;
                case 7:
                    price = 12;
                    break;
                case 8:
                case 9:
                    price = 35;
                    break;
            }
        }
        return price;
    }
}
