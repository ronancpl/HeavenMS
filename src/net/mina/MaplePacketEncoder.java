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
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import tools.MapleAESOFB;
import tools.HexTool;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.FilePrinter;

public class MaplePacketEncoder implements ProtocolEncoder {

    @Override
    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

        try {
            if (client.tryacquireEncoder()) {
                try {
                    final MapleAESOFB send_crypto = client.getSendCrypto();
                    final byte[] input = (byte[]) message;
                    if (YamlConfig.config.server.USE_DEBUG_SHOW_PACKET) {
                        int packetLen = input.length;
                        int pHeader = readFirstShort(input);
                        String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                        String op = lookupRecv(pHeader);
                        String Recv = "ServerSend:" + op + " [" + pHeaderStr + "] (" + packetLen + ")\r\n";
                        if (packetLen <= 50000) {
                            String RecvTo = Recv + HexTool.toString(input) + "\r\n" + HexTool.toStringFromAscii(input);
                            System.out.println(RecvTo);
                            if (op == null) {
                                System.out.println("UnknownPacket:" + RecvTo);
                            }
                        } else {
                            FilePrinter.print(FilePrinter.PACKET_STREAM + MapleSessionCoordinator.getSessionRemoteAddress(session) + ".txt", HexTool.toString(new byte[]{input[0], input[1]}) + " ...");
                        }
                    }

                    final byte[] unencrypted = new byte[input.length];
                    System.arraycopy(input, 0, unencrypted, 0, input.length);
                    final byte[] ret = new byte[unencrypted.length + 4];
                    final byte[] header = send_crypto.getPacketHeader(unencrypted.length);
                    MapleCustomEncryption.encryptData(unencrypted);

                    send_crypto.crypt(unencrypted);
                    System.arraycopy(header, 0, ret, 0, 4);
                    System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);

                    out.write(IoBuffer.wrap(ret));
                } finally {
                    client.unlockEncoder();
                }
            }
//            System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
//            out.write(ByteBuffer.wrap(ret));
        } catch (NullPointerException npe) {
            out.write(IoBuffer.wrap(((byte[]) message)));
        }
    }
    
    private String lookupRecv(int val) {
        return OpcodeConstants.sendOpcodeNames.get(val);
    }

    private int readFirstShort(byte[] arr) {
        return new GenericLittleEndianAccessor(new ByteArrayByteStream(arr)).readShort();
    }

    @Override
    public void dispose(IoSession session) throws Exception {}
}