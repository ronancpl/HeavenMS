package net.server.handlers.login;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import java.net.InetSocketAddress;

public final class RegisterPicHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        int charId = slea.readInt();
        
        String macs = slea.readMapleAsciiString();
        String hwid = slea.readMapleAsciiString();	
        c.updateMacs(macs);
        c.updateHWID(hwid);
        if (c.hasBannedMac() || c.hasBannedHWID()) {
            c.getSession().close(true);
            return;
        }
        
        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            c.getSession().close(true);
            return;
        }
		
        String pic = slea.readMapleAsciiString();
        if (c.getPic() == null || c.getPic().equals("")) {
            c.setPic(pic);
            
            c.setWorld(server.getCharacterWorld(charId));
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
            c.getSession().close(true);
        }
    }
}