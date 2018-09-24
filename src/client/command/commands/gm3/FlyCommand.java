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

public class FlyCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) { // fly option will become available for any character of that account
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !fly <on/off>");
            return;
        }

        Integer accid = c.getAccID();
        Server srv = Server.getInstance();
        String sendStr = "";
        if (params[0].equalsIgnoreCase("on")) {
            sendStr += "Enabled Fly feature (F1). With fly active, you cannot attack.";
            if (!srv.canFly(accid)) sendStr += " Re-login to take effect.";

            srv.changeFly(c.getAccID(), true);
        } else {
            sendStr += "Disabled Fly feature. You can now attack.";
            if (srv.canFly(accid)) sendStr += " Re-login to take effect.";

            srv.changeFly(c.getAccID(), false);
        }

        player.dropMessage(6, sendStr);
    }
}
