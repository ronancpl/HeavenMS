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
package client.command.commands.gm0;

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;
import net.server.Server;
import tools.FilePrinter;
import tools.MaplePacketCreator;

public class ReportBugCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();

        if (params.length < 1) {
            player.dropMessage(5, "Message too short and not sent. Please do @bug <bug>");
            return;
        }
        String message = joinStringFrom(params, 0);
        Server.getInstance().broadcastGMMessage(c.getWorld(), MaplePacketCreator.sendYellowTip("[BUG]:" + MapleCharacter.makeMapleReadable(player.getName()) + ": " + message));
        Server.getInstance().broadcastGMMessage(c.getWorld(), MaplePacketCreator.serverNotice(1, message));
        FilePrinter.printError("bug.txt", MapleCharacter.makeMapleReadable(player.getName()) + ": " + message + "\r\n");
        player.dropMessage(5, "Your bug '" + message + "' was submitted successfully to our developers. Thank you!");

    }
}
