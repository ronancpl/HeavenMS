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
package client.command.commands.gm3;

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import net.server.Server;
import tools.MapleLogger;
import tools.MaplePacketCreator;

public class IgnoreCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !ignore <ign>");
            return;
        }
        MapleCharacter victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim == null) {
            player.message("Player '" + params[0] + "' could not be found on this world.");
            return;
        }
        boolean monitored_ = MapleLogger.ignored.contains(victim.getName());
        if (monitored_) {
            MapleLogger.ignored.remove(victim.getName());
        } else {
            MapleLogger.ignored.add(victim.getName());
        }
        player.yellowMessage(victim.getName() + " is " + (!monitored_ ? "now being ignored." : "no longer being ignored."));
        String message_ = player.getName() + (!monitored_ ? " has started ignoring " : " has stopped ignoring ") + victim.getName() + ".";
        Server.getInstance().broadcastGMMessage(c.getWorld(), MaplePacketCreator.serverNotice(5, message_));

    }
}
