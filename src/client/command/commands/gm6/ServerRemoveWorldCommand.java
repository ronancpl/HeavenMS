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

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;
import net.server.Server;

public class ServerRemoveWorldCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        final MapleCharacter player = c.getPlayer();
        
        final int rwid = Server.getInstance().getWorldsSize() - 1;
        if(rwid <= 0) {
            player.dropMessage(5, "Unable to remove world 0.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(Server.getInstance().removeWorld()) {
                    if(player.isLoggedinWorld()) {
                        player.dropMessage(5, "Successfully removed a world. Current world count: " + Server.getInstance().getWorldsSize() + ".");
                    }
                } else {
                    if(player.isLoggedinWorld()) {
                        if(rwid < 0) {
                            player.dropMessage(5, "No registered worlds to remove.");
                        } else {
                            player.dropMessage(5, "Failed to remove world " + rwid + ". Check if there are people currently playing there.");
                        }
                    }
                }
            }
        }).start();
    }
}
