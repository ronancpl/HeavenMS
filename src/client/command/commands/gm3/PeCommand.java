/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2018 RonanLana

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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm3;

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import net.MaplePacketHandler;
import net.PacketProcessor;
import tools.FilePrinter;
import tools.HexTool;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PeCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        String packet = "";
        try {
            InputStreamReader is = new FileReader("pe.txt");
            Properties packetProps = new Properties();
            packetProps.load(is);
            is.close();
            packet = packetProps.getProperty("pe");
        } catch (IOException ex) {
            ex.printStackTrace();
            player.yellowMessage("Failed to load pe.txt");
            return;

        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(packet));
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(mplew.getPacket()));
        short packetId = slea.readShort();
        final MaplePacketHandler packetHandler = PacketProcessor.getProcessor(0, c.getChannel()).getHandler(packetId);
        if (packetHandler != null && packetHandler.validateState(c)) {
            try {
                player.yellowMessage("Receiving: " + packet);
                packetHandler.handlePacket(slea, c);
            } catch (final Throwable t) {
                FilePrinter.printError(FilePrinter.PACKET_HANDLER + packetHandler.getClass().getName() + ".txt", t, "Error for " + (c.getPlayer() == null ? "" : "player ; " + c.getPlayer() + " on map ; " + c.getPlayer().getMapId() + " - ") + "account ; " + c.getAccountName() + "\r\n" + slea.toString());
            }
        }
    }
}
