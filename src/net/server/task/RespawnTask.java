package net.server.task;

import net.server.PlayerStorage;
import net.server.Server;
import net.server.channel.Channel;
import server.maps.MapleMapManager;

/**
 * @author Resinate
 */
public class RespawnTask implements Runnable {
    
    @Override
    public void run() {
        for (Channel ch : Server.getInstance().getAllChannels()) {
            PlayerStorage ps = ch.getPlayerStorage();
            if (ps != null) {
                if (!ps.getAllCharacters().isEmpty()) {
                    MapleMapManager mapManager = ch.getMapFactory();
                    if (mapManager != null) {
                        mapManager.updateMaps();
                    }
                }
            }
        }
    }
}
