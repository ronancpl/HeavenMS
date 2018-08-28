/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server.life.positioner;

import constants.ServerConstants;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.server.Server;
import net.server.channel.Channel;
import server.life.MaplePlayerNPC;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

/**
 *
 * @author RonanLana
 * 
 * Note: the podium uses getGroundBelow that in its turn uses inputted posY decremented by 7.
 * Podium system will implement increase-by-7 to negate that behaviour.
 */
public class MaplePlayerNPCPodium {
    private static int getPlatformPosX(int platform) {
        switch(platform) {
            case 0:
                return -50;
                
            case 1:
                return -170;
                
            default:
                return 70;
        }
    }
    
    private static int getPlatformPosY(int platform) {
        switch(platform) {
            case 0:
                return -47;
                
            default:
                return 40;
        }
    }
    
    private static Point calcNextPos(int rank, int step) {
        int podiumPlatform = rank / step;
        int relativePos = (rank % step) + 1;
        
        Point pos = new Point(getPlatformPosX(podiumPlatform) + ((100 * relativePos) / (step + 1)), getPlatformPosY(podiumPlatform));
        return pos;
    }
    
    private static Point rearrangePlayerNpcs(MapleMap map, int newStep, List<MaplePlayerNPC> pnpcs) {
        int i = 0;
        for(MaplePlayerNPC pn : pnpcs) {
            pn.updatePlayerNPCPosition(map, calcNextPos(i, newStep));
            i++;
        }
        
        return calcNextPos(i, newStep);
    }
    
    private static Point reorganizePlayerNpcs(MapleMap map, int newStep, List<MapleMapObject> mmoList) {
        if(!mmoList.isEmpty()) {
            if(ServerConstants.USE_DEBUG) System.out.println("Reorganizing pnpc map, step " + newStep);
            
            List<MaplePlayerNPC> playerNpcs = new ArrayList<>(mmoList.size());
            for(MapleMapObject mmo : mmoList) {
                playerNpcs.add((MaplePlayerNPC) mmo);
            }
            
            Collections.sort(playerNpcs, new Comparator<MaplePlayerNPC>() {
                @Override
                public int compare(MaplePlayerNPC p1, MaplePlayerNPC p2) {
                    return p1.getScriptId() - p2.getScriptId(); // scriptid as playernpc history
                }
            });
            
            for(Channel ch : Server.getInstance().getChannelsFromWorld(map.getWorld())) {
                MapleMap m = ch.getMapFactory().getMap(map.getId());
                
                for(MaplePlayerNPC pn : playerNpcs) {
                    m.removeMapObject(pn);
                    m.broadcastMessage(MaplePacketCreator.removeNPCController(pn.getObjectId()));
                    m.broadcastMessage(MaplePacketCreator.removePlayerNPC(pn.getObjectId()));
                }
            }
            
            Point ret = rearrangePlayerNpcs(map, newStep, playerNpcs);
            
            for(Channel ch : Server.getInstance().getChannelsFromWorld(map.getWorld())) {
                MapleMap m = ch.getMapFactory().getMap(map.getId());
                
                for(MaplePlayerNPC pn : playerNpcs) {
                    m.addPlayerNPCMapObject(pn);
                    m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
                    m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                }
            }
            
            return ret;
        }
        
        return null;
    }
    
    private static int encodePodiumData(int podiumStep, int podiumCount) {
        return (podiumCount * (1 << 5)) + podiumStep;
    }
    
    private static Point getNextPlayerNpcPosition(MapleMap map, int podiumData) {   // automated playernpc position thanks to Ronan
        int podiumStep = podiumData % (1 << 5), podiumCount = (podiumData / (1 << 5));
        
        if(podiumCount >= 3 * podiumStep) {
            if(podiumStep >= ServerConstants.PLAYERNPC_AREA_STEPS) return null;
            
            List<MapleMapObject> mmoList = map.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER_NPC));
            map.getWorldServer().setPlayerNpcMapPodiumData(map.getId(), encodePodiumData(podiumStep + 1, podiumCount + 1));
            return reorganizePlayerNpcs(map, podiumStep + 1, mmoList);
        } else {
            map.getWorldServer().setPlayerNpcMapPodiumData(map.getId(), encodePodiumData(podiumStep, podiumCount + 1));
            return calcNextPos(podiumCount, podiumStep);
        }
    }
    
    public static Point getNextPlayerNpcPosition(MapleMap map) {
        Point pos = getNextPlayerNpcPosition(map, map.getWorldServer().getPlayerNpcMapPodiumData(map.getId()));
        if(pos == null) return null;
        
        return map.getGroundBelow(pos);
    }
}
