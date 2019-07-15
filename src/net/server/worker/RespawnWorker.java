package net.server.worker;

import net.server.Server;
import net.server.channel.Channel;

/**
 * @author Resinate
 */
public class RespawnWorker implements Runnable {
    
    @Override
    public void run() {
        for (Channel ch : Server.getInstance().getAllChannels()) {
            if (!ch.getPlayerStorage().getAllCharacters().isEmpty()) {
                ch.getMapFactory().updateMaps();
            }
        }
    }
}
