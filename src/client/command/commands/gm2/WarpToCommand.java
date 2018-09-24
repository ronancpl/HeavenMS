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
package client.command.commands.gm2;

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import net.server.Server;
import net.server.channel.Channel;

public class WarpToCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 2) {
            player.yellowMessage("Syntax: !warpto <playername> <mapid>");
            return;
        }

        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim == null) {//If victim isn't on current channel or isnt a character try and find him by loop all channels on current world.
            for (Channel ch : Server.getInstance().getChannelsFromWorld(c.getWorld())) {
                victim = ch.getPlayerStorage().getCharacterByName(params[0]);
                if (victim != null) {
                    break;//We found the person, no need to continue the loop.
                }
            }
        }
        if (victim != null) {//If target isn't null attempt to warp.
            //Remove warper from current event instance.
            if (player.getEventInstance() != null) {
                player.getEventInstance().unregisterPlayer(player);
            }
            //Attempt to join the victims warp instance.
            if (victim.getEventInstance() != null) {
                if (victim.getClient().getChannel() == player.getClient().getChannel()) {//just in case.. you never know...
                    //victim.getEventInstance().registerPlayer(player);
                    player.changeMap(victim.getEventInstance().getMapInstance(victim.getMapId()), victim.getMap().findClosestPortal(victim.getPosition()));
                } else {
                    player.dropMessage(6, "Please change to channel " + victim.getClient().getChannel());
                }
            } else {//If victim isn't in an event instance, just warp them.
                player.changeMap(victim.getMapId(), victim.getMap().findClosestPortal(victim.getPosition()));
            }
            if (player.getClient().getChannel() != victim.getClient().getChannel()) {//And then change channel if needed.
                player.dropMessage("Changing channel, please wait a moment.");
                player.getClient().changeChannel(victim.getClient().getChannel());
            }
        } else {
            player.dropMessage(6, "Unknown player.");
        }
    }
}
