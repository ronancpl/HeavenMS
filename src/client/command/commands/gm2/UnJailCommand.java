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

public class UnJailCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !unjail <playername>");
            return;
        }

        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim != null) {
            if (victim.getJailExpirationTimeLeft() <= 0) {
                player.message("This player is already free.");
                return;
            }
            victim.removeJailExpirationTime();
            victim.message("By lack of concrete proof you are now unjailed. Enjoy freedom!");
            player.message(victim.getName() + " was unjailed.");
        } else {
            player.message("Player '" + params[0] + "' could not be found on this channel.");
        }
    }
}
