package net.server.handlers.login;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import java.net.InetSocketAddress;

public class ViewAllCharSelectedWithPicHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        String pic = slea.readMapleAsciiString();
        int charId = slea.readInt();
        slea.readInt(); // please don't let the client choose which world they should login
        
        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            c.getSession().close(true);
            return;
        }
        
        c.setWorld(server.getCharacterWorld(charId));
        int channel = Randomizer.rand(1, c.getWorldServer().getChannelsSize());
        c.setChannel(channel);
        
        String macs = slea.readMapleAsciiString();
        c.updateMacs(macs);
        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
        }
        
        if (c.checkPic(pic)) {
            if(c.getWorldServer().isWorldCapacityFull()) {
                c.announce(MaplePacketCreator.getAfterLoginError(10));
                return;
            }
            
            server.unregisterLoginState(c);
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            server.setCharacteridInTransition((InetSocketAddress) c.getSession().getRemoteAddress(), charId);

            String[] socket = server.getIP(c.getWorld(), c.getChannel()).split(":");
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
