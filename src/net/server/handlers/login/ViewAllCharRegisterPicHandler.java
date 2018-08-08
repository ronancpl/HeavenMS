package net.server.handlers.login;

import client.MapleClient;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.world.World;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ViewAllCharRegisterPicHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        int charId = slea.readInt();
        slea.readInt(); // please don't let the client choose which world they should login
        
        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            c.getSession().close(true);
            return;
        }
        
        c.setWorld(server.getCharacterWorld(charId));
        World wserv = c.getWorldServer();
        if(wserv == null || wserv.isWorldCapacityFull()) {
            c.announce(MaplePacketCreator.getAfterLoginError(10));
            return;
        }
        
        int channel = Randomizer.rand(1, server.getWorld(c.getWorld()).getChannelsSize());
        c.setChannel(channel);
        
        String mac = slea.readMapleAsciiString();
        c.updateMacs(mac);
        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
        }
        
        slea.readMapleAsciiString();
        String pic = slea.readMapleAsciiString();
        c.setPic(pic);
        
        String[] socket = server.getInetSocket(c.getWorld(), channel);
        if (socket == null) {
            c.announce(MaplePacketCreator.getAfterLoginError(10));
            return;
        }
        
        server.unregisterLoginState(c);
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
        server.setCharacteridInTransition((InetSocketAddress) c.getSession().getRemoteAddress(), charId);
        
        try {
            c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
