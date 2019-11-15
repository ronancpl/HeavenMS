package net.server.handlers.login;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.world.World;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import net.server.coordinator.session.MapleSessionCoordinator;
import net.server.coordinator.session.MapleSessionCoordinator.AntiMulticlientResult;
import org.apache.mina.core.session.IoSession;

public class ViewAllCharSelectedWithPicHandler extends AbstractMaplePacketHandler {

    private static int parseAntiMulticlientError(AntiMulticlientResult res) {
        switch (res) {
            case REMOTE_PROCESSING:
                return 10;

            case REMOTE_LOGGEDIN:
                return 7;

            case REMOTE_NO_MATCH:
                return 17;
                
            case COORDINATOR_ERROR:
                return 8;
                
            default:
                return 9;
        }
    }
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        String pic = slea.readMapleAsciiString();
        int charId = slea.readInt();
        slea.readInt(); // please don't let the client choose which world they should login
        
        String macs = slea.readMapleAsciiString();
        String hwid = slea.readMapleAsciiString();
        
        if (!hwid.matches("[0-9A-F]{12}_[0-9A-F]{8}")) {
            c.announce(MaplePacketCreator.getAfterLoginError(17));
            return;
        }
        
        c.updateMacs(macs);
        c.updateHWID(hwid);
        
        if (c.hasBannedMac() || c.hasBannedHWID()) {
            MapleSessionCoordinator.getInstance().closeSession(c.getSession(), true);
            return;
        }
        
        IoSession session = c.getSession();
        
        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            MapleSessionCoordinator.getInstance().closeSession(c.getSession(), true);
            return;
        }
        
        c.setWorld(server.getCharacterWorld(charId));
        World wserv = c.getWorldServer();
        if(wserv == null || wserv.isWorldCapacityFull()) {
            c.announce(MaplePacketCreator.getAfterLoginError(10));
            return;
        }
        
        int channel = Randomizer.rand(1, wserv.getChannelsSize());
        c.setChannel(channel);
        
        if (c.checkPic(pic)) {
            String[] socket = server.getInetSocket(c.getWorld(), c.getChannel());
            if(socket == null) {
                c.announce(MaplePacketCreator.getAfterLoginError(10));
                return;
            }
            
            AntiMulticlientResult res = MapleSessionCoordinator.getInstance().attemptGameSession(session, c.getAccID(), hwid);
            if (res != AntiMulticlientResult.SUCCESS) {
                c.announce(MaplePacketCreator.getAfterLoginError(parseAntiMulticlientError(res)));
                return;
            }
            
            server.unregisterLoginState(c);
            c.setCharacterOnSessionTransitionState(charId);
            
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
