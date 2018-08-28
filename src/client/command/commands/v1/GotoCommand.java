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
package client.command.commands.v1;

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;
import server.MaplePortal;
import server.maps.MapleMap;

import java.util.HashMap;

public class GotoCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        final HashMap<String, Integer> gotomaps = new HashMap<String, Integer>();
        gotomaps.put("gmmap", 180000000);
        gotomaps.put("southperry", 60000);
        gotomaps.put("amherst", 1000000);
        gotomaps.put("henesys", 100000000);
        gotomaps.put("ellinia", 101000000);
        gotomaps.put("perion", 102000000);
        gotomaps.put("kerning", 103000000);
        gotomaps.put("lith", 104000000);
        gotomaps.put("sleepywood", 105040300);
        gotomaps.put("florina", 110000000);
        gotomaps.put("nautilus", 120000000);
        gotomaps.put("ereve", 130000000);
        gotomaps.put("rien", 140000000);
        gotomaps.put("orbis", 200000000);
        gotomaps.put("happy", 209000000);
        gotomaps.put("elnath", 211000000);
        gotomaps.put("ludi", 220000000);
        gotomaps.put("aqua", 230000000);
        gotomaps.put("leafre", 240000000);
        gotomaps.put("mulung", 250000000);
        gotomaps.put("herb", 251000000);
        gotomaps.put("omega", 221000000);
        gotomaps.put("korean", 222000000);
        gotomaps.put("ellin", 300000000);
        gotomaps.put("nlc", 600000000);
        gotomaps.put("excavation", 990000000);
        gotomaps.put("pianus", 230040420);
        gotomaps.put("horntail", 240060200);
        gotomaps.put("mushmom", 100000005);
        gotomaps.put("griffey", 240020101);
        gotomaps.put("manon", 240020401);
        gotomaps.put("horseman", 682000001);
        gotomaps.put("balrog", 105090900);
        gotomaps.put("zakum", 211042300);
        gotomaps.put("papu", 220080001);
        gotomaps.put("showa", 801000000);
        gotomaps.put("guild", 200000301);
        gotomaps.put("shrine", 800000000);
        gotomaps.put("skelegon", 240040511);
        gotomaps.put("hpq", 100000200);
        gotomaps.put("ht", 240050400);
        gotomaps.put("ariant", 260000000);
        gotomaps.put("magatia", 261000000);
        gotomaps.put("singapore", 540000000);
        gotomaps.put("keep", 610020006);
        gotomaps.put("amoria", 680000000);
        gotomaps.put("temple", 270000100);
        gotomaps.put("neo", 240070000);
        gotomaps.put("fm", 910000000);

        MapleCharacter player = c.getPlayer();
        if (params.length < 1){
            player.yellowMessage("Syntax: @goto <map name>");
            return;
        }
        if (gotomaps.containsKey(params[0])) {
            MapleMap target = c.getChannelServer().getMapFactory().getMap(gotomaps.get(params[0]));
            MaplePortal targetPortal = target.getPortal(0);
            if (player.getEventInstance() != null) {
                player.getEventInstance().removePlayer(player);
            }
            player.changeMap(target, targetPortal);
        } else {
            player.dropMessage(5, "That map does not exist.");
        }
    }
}
