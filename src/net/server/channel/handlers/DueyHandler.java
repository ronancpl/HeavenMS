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

import client.MapleClient;
import client.processor.DueyProcessor;
import constants.ServerConstants;

import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DueyHandler extends AbstractMaplePacketHandler {
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	if (!ServerConstants.USE_DUEY){
            c.announce(MaplePacketCreator.enableActions());
            return;
    	}
            
        byte operation = slea.readByte();
        if (operation == DueyProcessor.Actions.TOSERVER_SEND_ITEM.getCode()) {
            byte inventId = slea.readByte();
            short itemPos = slea.readShort();
            short amount = slea.readShort();
            int mesos = slea.readInt();
            String recipient = slea.readMapleAsciiString();
            
            DueyProcessor.dueySendItem(c, inventId, itemPos, amount, mesos, recipient);
        } else if (operation == DueyProcessor.Actions.TOSERVER_REMOVE_PACKAGE.getCode()) {
            int packageid = slea.readInt();
            
            DueyProcessor.dueyRemovePackage(c, packageid);
        } else if (operation == DueyProcessor.Actions.TOSERVER_CLAIM_PACKAGE.getCode()) {
            int packageid = slea.readInt();
            
            DueyProcessor.dueyClaimPackage(c, packageid);
        }
    }
}
