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

import client.MapleJob;
import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;

public class JobCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length == 1) {
            int jobid = Integer.parseInt(params[0]);
            if (jobid < 0 || jobid >= 2200) {
                player.message("Jobid " + jobid + " is not available.");
                return;
            }

            player.changeJob(MapleJob.getById(jobid));
            player.equipChanged();
        } else if (params.length == 2) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(params[0]);

            if (victim != null) {
                int jobid = Integer.parseInt(params[1]);
                if (jobid < 0 || jobid >= 2200) {
                    player.message("Jobid " + jobid + " is not available.");
                    return;
                }

                victim.changeJob(MapleJob.getById(jobid));
                player.equipChanged();
            } else {
                player.message("Player '" + params[0] + "' could not be found on this channel.");
            }
        } else {
            player.message("Syntax: !job <job id> <opt: IGN of another person>");
        }
    }
}
