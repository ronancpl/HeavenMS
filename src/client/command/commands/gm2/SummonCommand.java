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
        if (victim == null) {//If victim isn't on current channel, loop all channels on current world.
            for (Channel ch : Server.getInstance().getChannelsFromWorld(c.getWorld())) {
                victim = ch.getPlayerStorage().getCharacterByName(params[0]);
                if (victim != null) {
                    break;//We found the person, no need to continue the loop.
                }
            }
        }
        if (victim != null) {
            boolean changingEvent = true;

            if (victim.getEventInstance() != null) {
                if (player.getEventInstance() != null && victim.getEventInstance().getLeaderId() == player.getEventInstance().getLeaderId()) {
                    changingEvent = false;
                } else {
                    victim.getEventInstance().unregisterPlayer(victim);
                }
            }
            //Attempt to join the warpers instance.
            if (player.getEventInstance() != null && changingEvent) {
                if (player.getClient().getChannel() == victim.getClient().getChannel()) {//just in case.. you never know...
                    player.getEventInstance().registerPlayer(victim);
                    victim.changeMap(player.getEventInstance().getMapInstance(player.getMapId()), player.getMap().findClosestPortal(player.getPosition()));
                } else {
                    player.dropMessage("Target isn't on your channel, not able to warp into event instance.");
                }
            } else {//If victim isn't in an event instance or is in the same event instance as the one the caller is, just warp them.
                victim.changeMap(player.getMapId(), player.getMap().findClosestPortal(player.getPosition()));
            }
            if (player.getClient().getChannel() != victim.getClient().getChannel()) {//And then change channel if needed.
                victim.dropMessage("Changing channel, please wait a moment.");
                victim.getClient().changeChannel(player.getClient().getChannel());
            }
        } else {
            player.dropMessage(6, "Unknown player.");
        }
    }
}
