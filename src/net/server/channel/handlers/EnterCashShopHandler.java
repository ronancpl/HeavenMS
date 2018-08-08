/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

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
import net.AbstractMaplePacketHandler;
import net.server.Server;
import server.maps.MapleMiniDungeonInfo;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Flav
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            MapleCharacter mc = c.getPlayer();

            if (mc.cannotEnterCashShop()) {
                c.announce(MaplePacketCreator.enableActions());
                return;
                
            }
            
            if(mc.getEventInstance() != null) {
                c.announce(MaplePacketCreator.serverNotice(5, "Entering Cash Shop or MTS are disabled when registered on an event."));
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            if(MapleMiniDungeonInfo.isDungeonMap(mc.getMapId())) {
                c.announce(MaplePacketCreator.serverNotice(5, "Changing channels or entering Cash Shop or MTS are disabled when inside a Mini-Dungeon."));
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            if (mc.getCashShop().isOpened()) {
                return;
            }

            mc.closePlayerInteractions();

            mc.unregisterChairBuff();
            Server.getInstance().getPlayerBuffStorage().addBuffsToStorage(mc.getId(), mc.getAllBuffs());
            Server.getInstance().getPlayerBuffStorage().addDiseasesToStorage(mc.getId(), mc.getAllDiseases());
            mc.setAwayFromChannelWorld();
            mc.notifyMapTransferToPartner(-1);
            mc.cancelAllBuffs(true);
            mc.cancelAllDebuffs();
            mc.cancelBuffExpireTask();
            mc.cancelDiseaseExpireTask();
            mc.cancelSkillCooldownTask();
            mc.cancelExpirationTask();
            
            mc.forfeitExpirableQuests();
            mc.cancelQuestExpirationTask();
            
            c.announce(MaplePacketCreator.openCashShop(c, false));
            c.announce(MaplePacketCreator.showCashInventory(c));
            c.announce(MaplePacketCreator.showGifts(mc.getCashShop().loadGifts()));
            c.announce(MaplePacketCreator.showWishList(mc, false));
            c.announce(MaplePacketCreator.showCash(mc));

            c.getChannelServer().removePlayer(mc);
            mc.getMap().removePlayer(mc);
            mc.getCashShop().open(true);
            mc.saveCharToDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
