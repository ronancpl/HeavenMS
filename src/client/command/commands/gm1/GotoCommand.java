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
import server.MaplePortal;
import server.maps.MapleMap;

import java.util.HashMap;

public class GotoCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        final HashMap<String, Integer> gotomaps = GameConstants.GOTO_MAPS;

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
