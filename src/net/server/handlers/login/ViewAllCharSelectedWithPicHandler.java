package net.server.handlers.login;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;

public class ViewAllCharSelectedWithPicHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        String pic = slea.readMapleAsciiString();
        int charId = slea.readInt();
        int world = slea.readInt();//world
        c.setWorld(world);
        int channel = Randomizer.rand(0, Server.getInstance().getWorld(world).getChannels().size());
        c.setChannel(channel);
        String macs = slea.readMapleAsciiString();
        c.updateMacs(macs);

        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
        }
        if (c.checkPic(pic)) {
            Server.getInstance().unregisterLoginState(c);
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);

            String[] socket = Server.getInstance().getIP(c.getWorld(), c.getChannel()).split(":");
            try {
                c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        } else {
            c.announce(MaplePacketCreator.wrongPic());
        }
    }
}
