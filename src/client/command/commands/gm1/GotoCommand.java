/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm1;

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;
import constants.GameConstants;
import java.util.ArrayList;
import java.util.Collections;
import net.server.Server;
import server.MaplePortal;
import server.maps.FieldLimit;
import server.maps.MapleMap;
import server.maps.MapleMapManager;
import server.maps.MapleMiniDungeonInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GotoCommand extends Command {
    
    {
        setDescription("");
        
        MapleMapManager mapManager = Server.getInstance().getWorlds().get(0).getChannels().get(0).getMapFactory();
        
        List<Entry<String, Integer>> towns = new ArrayList<>(GameConstants.GOTO_TOWNS.entrySet());
        sortGotoEntries(towns);
        for (Map.Entry<String, Integer> e : towns) {
            GOTO_TOWNS_INFO += ("'" + e.getKey() + "' - #b" + (mapManager.getMap(e.getValue()).getMapName()) + "#k\r\n");
        }
        
        List<Entry<String, Integer>> areas = new ArrayList<>(GameConstants.GOTO_AREAS.entrySet());
        sortGotoEntries(areas);
        for (Map.Entry<String, Integer> e : areas) {
            GOTO_AREAS_INFO += ("'" + e.getKey() + "' - #b" + (mapManager.getMap(e.getValue()).getMapName()) + "#k\r\n");
        }
    }
    
    public static String GOTO_TOWNS_INFO = "";
    public static String GOTO_AREAS_INFO = "";
    
    private static void sortGotoEntries(List<Entry<String, Integer>> listEntries) {
        Collections.sort(listEntries, new Comparator<Entry<String, Integer>>() {
            @Override
            public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2)
            {
                return e1.getValue().compareTo(e2.getValue());
            }
        });
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1){
            String sendStr = "Syntax: #b@goto <map name>#k. Available areas:\r\n\r\n#rTowns:#k\r\n" + GOTO_TOWNS_INFO;
            if (player.isGM()) {
                sendStr += ("\r\n#rAreas:#k\r\n" + GOTO_AREAS_INFO);
            }
            
            player.getAbstractPlayerInteraction().npcTalk(9000020, sendStr);
            return;
        }
        
        if (!player.isAlive()) {
            player.dropMessage(1, "This command cannot be used when you're dead.");
            return;
        }

        if (!player.isGM()) {
            if (player.getEventInstance() != null || MapleMiniDungeonInfo.isDungeonMap(player.getMapId()) || FieldLimit.CANNOTMIGRATE.check(player.getMap().getFieldLimit())) {
                player.dropMessage(1, "This command can not be used in this map.");
                return;
            }
        }

        HashMap<String, Integer> gotomaps;
        if (player.isGM()) {
            gotomaps = new HashMap<>(GameConstants.GOTO_AREAS);     // distinct map registry for GM/users suggested thanks to Vcoc
            gotomaps.putAll(GameConstants.GOTO_TOWNS);  // thanks Halcyon for pointing out duplicates on listed entries functionality
        } else {
            gotomaps = GameConstants.GOTO_TOWNS;
        }
        
        if (gotomaps.containsKey(params[0])) {
            MapleMap target = c.getChannelServer().getMapFactory().getMap(gotomaps.get(params[0]));
            
            // expedition issue with this command detected thanks to Masterrulax
            MaplePortal targetPortal = target.getRandomPlayerSpawnpoint();
            player.saveLocationOnWarp();
            player.changeMap(target, targetPortal);
        } else {
            // detailed info on goto available areas suggested thanks to Vcoc
            String sendStr = "Area '#r" + params[0] + "#k' is not available. Available areas:\r\n\r\n#rTowns:#k" + GOTO_TOWNS_INFO;
            if (player.isGM()) {
                sendStr += ("\r\n#rAreas:#k\r\n" + GOTO_AREAS_INFO);
            }
            
            player.getAbstractPlayerInteraction().npcTalk(9000020, sendStr);
        }
    }
}
