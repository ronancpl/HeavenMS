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

public class HpMpCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        MapleCharacter victim = player;
        int statUpdate = 1;

        if (params.length == 2) {
            victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
            statUpdate = Integer.valueOf(params[1]);
        } else if (params.length == 1) {
            statUpdate = Integer.valueOf(params[0]);
        } else {
            player.yellowMessage("Syntax: !hpmp [<playername>] <value>");
        }

        if (victim != null) {
            victim.updateHpMp(statUpdate);
        } else {
            player.message("Player '" + params[0] + "' could not be found on this world.");
        }
    }
}
