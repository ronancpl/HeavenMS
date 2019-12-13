/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

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
package client.command.commands.gm2;

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import server.maps.MapleMap;
import net.server.Server;
import net.server.channel.Channel;

public class SummonCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !warphere <playername>");
            return;
        }

        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim == null) {
            //If victim isn't on current channel, loop all channels on current world.
            
            for (Channel ch : Server.getInstance().getChannelsFromWorld(c.getWorld())) {
                victim = ch.getPlayerStorage().getCharacterByName(params[0]);
                if (victim != null) {
                    break;//We found the person, no need to continue the loop.
                }
            }
        }
        if (victim != null) {
            if (!victim.isLoggedinWorld()) {
                player.dropMessage(6, "Player currently not logged in or unreachable.");
                return;
            }
            
            if (player.getClient().getChannel() != victim.getClient().getChannel()) {//And then change channel if needed.
                victim.dropMessage("Changing channel, please wait a moment.");
                victim.getClient().changeChannel(player.getClient().getChannel());
            }
            
            try {
                for (int i = 0; i < 7; i++) {   // poll for a while until the player reconnects
                    if (victim.isLoggedinWorld()) break;
                    Thread.sleep(1777);
                }
            } catch (InterruptedException e) {}
            
            MapleMap map = player.getMap();
            victim.saveLocationOnWarp();
            victim.forceChangeMap(map, map.findClosestPortal(player.getPosition()));
        } else {
            player.dropMessage(6, "Unknown player.");
        }
    }
}
