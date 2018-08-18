/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
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
package tools;

import java.util.ArrayList;
import java.util.List;

import net.opcodes.RecvOpcode;
import client.MapleClient;

/**
 * Logs packets to console and file.
 *
 * @author Alan (SharpAceX)
 */

public class MapleLogger {

    public static List<String> monitored = new ArrayList<>();
    public static List<String> ignored = new ArrayList<>();
    
	public static void logRecv(MapleClient c, short packetId, Object message) {
		if (c.getPlayer() == null){
			return;
		}
		if (!monitored.contains(c.getPlayer().getName())){
			return;
		}
		RecvOpcode op = getOpcodeFromValue(packetId);
		if (isRecvBlocked(op)){
			return;
		}
		String packet = op.toString() + "\r\n" + HexTool.toString((byte[]) message);
		FilePrinter.printError(FilePrinter.PACKET_LOGS + c.getAccountName() + "-" + c.getPlayer().getName() + ".txt", packet + "\r\n\r\n");
	}
	
	private static final boolean isRecvBlocked(RecvOpcode op){
		switch(op){
		case MOVE_PLAYER:
		case GENERAL_CHAT:
		case TAKE_DAMAGE:
		case MOVE_PET:
		case MOVE_LIFE:
		case NPC_ACTION:
		case FACE_EXPRESSION:
			return true;
		default:
			return false;
		}
	}
	
	private static final RecvOpcode getOpcodeFromValue(int value){
		for (RecvOpcode op : RecvOpcode.values()){
			if (op.getValue() == value){
				return op;
			}
		}
		return null;
	}
}
