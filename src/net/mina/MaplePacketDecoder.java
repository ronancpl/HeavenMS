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
package net.mina;

import config.YamlConfig;
import client.MapleClient;
import constants.net.OpcodeConstants;
import net.server.coordinator.session.MapleSessionCoordinator;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import tools.HexTool;
import tools.MapleAESOFB;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.FilePrinter;

public class MaplePacketDecoder extends CumulativeProtocolDecoder {
    private static final String DECODER_STATE_KEY = MaplePacketDecoder.class.getName() + ".STATE";

    private static class DecoderState {
        public int packetlength = -1;
    }

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if(client == null) {
            MapleSessionCoordinator.getInstance().closeSession(session, true);
            return false;
        }
        
        DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            session.setAttribute(DECODER_STATE_KEY, decoderState);
        }
        
        MapleAESOFB rcvdCrypto = client.getReceiveCrypto();
        if (in.remaining() >= 4 && decoderState.packetlength == -1) {
            int packetHeader = in.getInt();
            if (!rcvdCrypto.checkPacket(packetHeader)) {
                MapleSessionCoordinator.getInstance().closeSession(session, true);
                return false;
            }
            decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
        } else if (in.remaining() < 4 && decoderState.packetlength == -1) {
            return false;
        }
        if (in.remaining() >= decoderState.packetlength) {
            byte decryptedPacket[] = new byte[decoderState.packetlength];
            in.get(decryptedPacket, 0, decoderState.packetlength);
            decoderState.packetlength = -1;
            rcvdCrypto.crypt(decryptedPacket);
            MapleCustomEncryption.decryptData(decryptedPacket);
            out.write(decryptedPacket);
            if (YamlConfig.config.server.USE_DEBUG_SHOW_PACKET){ // Atoot's idea: packet traffic log, applied using auto-identation thanks to lrenex
                int packetLen = decryptedPacket.length;
                int pHeader = readFirstShort(decryptedPacket);
                String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                String op = lookupSend(pHeader);
                String Send = "ClientSend:" + op + " [" + pHeaderStr + "] (" + packetLen + ")\r\n";
                if (packetLen <= 3000) {
                    String SendTo = Send + HexTool.toString(decryptedPacket) + "\r\n" + HexTool.toStringFromAscii(decryptedPacket);
                    System.out.println(SendTo);
                    if (op == null) {
                        System.out.println("UnknownPacket:" + SendTo);
                    }
                } else {
                    FilePrinter.print(FilePrinter.PACKET_STREAM + MapleSessionCoordinator.getSessionRemoteAddress(session) + ".txt", HexTool.toString(new byte[]{decryptedPacket[0], decryptedPacket[1]}) + "...");
                }
            }
            return true;
        }
        return false;
    }
    
    private String lookupSend(int val) {
        return OpcodeConstants.recvOpcodeNames.get(val);
    }

    private int readFirstShort(byte[] arr) {
        return new GenericLittleEndianAccessor(new ByteArrayByteStream(arr)).readShort();
    }
}
