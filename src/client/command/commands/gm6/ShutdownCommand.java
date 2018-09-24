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
package client.command.commands.gm6;

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import net.server.Server;
import net.server.world.World;
import server.TimerManager;

public class ShutdownCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1){
            player.yellowMessage("Syntax: !shutdown [<time>|NOW]");
            return;
        }
        
        int time = 60000;
        if (params[0].equalsIgnoreCase("now")){
            time = 1;
        } else {
            time *= Integer.parseInt(params[0]);
        }

        if (time > 1) {
            int seconds = (int) (time / 1000) % 60;
            int minutes = (int) ((time / (1000 * 60)) % 60);
            int hours = (int) ((time / (1000 * 60 * 60)) % 24);
            int days = (int) ((time / (1000 * 60 * 60 * 24)));

            String strTime = "";
            if (days > 0) strTime += days + " days, ";
            if (hours > 0) strTime += hours + " hours, ";
            strTime += minutes + " minutes, ";
            strTime += seconds + " seconds";

            for (World w : Server.getInstance().getWorlds()) {
                for (MapleCharacter chr : w.getPlayerStorage().getAllCharacters()) {
                    chr.dropMessage("Server is undergoing maintenance process, and will be shutdown in " + strTime + ". Prepare yourself to quit safely in the mean time.");
                }
            }
        }
        
        TimerManager.getInstance().schedule(Server.getInstance().shutdown(false), time);
    }
}
