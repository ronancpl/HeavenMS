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
/**
 * @author BubblesDev
 * @purpose
 * [x]Full Moon,
 * [/]Summons Moon Bunny,
 * [x]shows animation,
 * [x]makes stirges and stuff appear
 */
importPackage(Packages.tools);

function act() {
    rm.spawnMonster(9300061, 1, 0, 0); // (0, 0) is temp position
    rm.getClient().getMap().startMapEffect("Protect the Moon Bunny that's pounding the mill, and gather up 10 Moon Bunny's Rice Cakes!", 5120016, 7000);
    rm.getClient().getMap().broadcastMessage(MaplePacketCreator.bunnyPacket()); // Protect the Moon Bunny!
    rm.getClient().getMap().broadcastMessage(MaplePacketCreator.showHPQMoon());
    rm.getClient().getMap().showAllMonsters();
    
}