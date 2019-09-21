package net.server.task;

import client.MapleCharacter;
import config.YamlConfig;
import net.server.world.World;
import tools.FilePrinter;

import java.util.Collection;

/**
 *
 * @author Shavit
 */
public class TimeoutTask extends BaseTask implements Runnable {
    @Override
    public void run() {
        long time = System.currentTimeMillis();
        Collection<MapleCharacter> chars = wserv.getPlayerStorage().getAllCharacters();
        for(MapleCharacter chr : chars) {
            if(time - chr.getClient().getLastPacket() > YamlConfig.config.server.TIMEOUT_DURATION) {
                FilePrinter.print(FilePrinter.DCS + chr.getClient().getAccountName(), chr.getName() + " auto-disconnected due to inactivity.");
                chr.getClient().disconnect(true, chr.getCashShop().isOpened());
            }
        }
    }

    public TimeoutTask(World world) {
        super(world);
    }
}
