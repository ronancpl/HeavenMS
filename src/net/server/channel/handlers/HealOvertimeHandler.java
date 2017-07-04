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
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.MaplePacketCreator;

public final class HealOvertimeHandler extends AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        AutobanManager abm = chr.getAutobanManager();
        int timestamp = slea.readInt();
        abm.setTimestamp(0, timestamp, 3);
        slea.skip(4);
        short healHP = slea.readShort();
        if (healHP != 0) {
            if ((abm.getLastSpam(0) + 1500) > timestamp) AutobanFactory.FAST_HP_HEALING.addPoint(abm, "Fast hp healing");
            
            int abHeal = 140;
            if(chr.getMapId() == 105040401 || chr.getMapId() == 105040402 || chr.getMapId() == 809000101 || chr.getMapId() == 809000201) abHeal += 40; // Sleepywood sauna and showa spa...
            if (healHP > abHeal) {
                AutobanFactory.HIGH_HP_HEALING.autoban(chr, "Healing: " + healHP + "; Max is " + abHeal + ".");
                return;
            }
            chr.addHP(healHP);
            
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showHpHealed(chr.getId(), healHP), false);
            chr.checkBerserk(chr.isHidden());
            abm.spam(0, timestamp);
        }
        short healMP = slea.readShort();
        if (healMP != 0 && healMP < 1000) {
            if ((abm.getLastSpam(1) + 1500) > timestamp) AutobanFactory.FAST_MP_HEALING.addPoint(abm, "Fast mp healing");
            chr.addMP(healMP);
            abm.spam(1, timestamp);
        }
    }
}
