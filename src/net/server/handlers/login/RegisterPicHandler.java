package net.server.handlers.login;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;

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
		
        String pic = slea.readMapleAsciiString();
        if (c.getPic() == null || c.getPic().equals("")) {
            c.setPic(pic);
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            String[] socket = Server.getInstance().getIP(c.getWorld(), c.getChannel()).split(":");
            try {
                c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } catch (UnknownHostException e) {
            }
        } else {
            c.getSession().close(true);
        }
    }
}