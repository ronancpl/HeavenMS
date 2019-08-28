package net.server.worker;

import client.MapleCharacter;
import constants.ServerConstants;
import net.server.world.World;
import tools.FilePrinter;

import java.util.Collection;

/**
 *
 * @author Shavit
 */
public class TimeoutWorker extends BaseWorker implements Runnable {
    @Override
    public void run() {
        long time = System.currentTimeMillis();
        Collection<MapleCharacter> chars = wserv.getPlayerStorage().getAllCharacters();
        for(MapleCharacter chr : chars) {
            if(time - chr.getClient().getLastPacket() > ServerConstants.TIMEOUT_DURATION) {
                FilePrinter.print(FilePrinter.DCS + chr.getClient().getAccountName(), chr.getName() + " auto-disconnected due to inactivity.");
                chr.getClient().disconnect(true, chr.getCashShop().isOpened());
            }
        }
    }

    public TimeoutWorker(World world) {
        super(world);
    }
}
