/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

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

public class GiveMesosCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !givems [<playername>] <gainmeso>");
            return;
        }

        String recv_, value_;
        long mesos_ = 0;
        
        if (params.length == 2) {
            recv_ = params[0];
            value_ = params[1];
        } else {
            recv_ = c.getPlayer().getName();
            value_ = params[0];
        }
        
        try {
            mesos_ = Long.parseLong(value_);
            if (mesos_ > Integer.MAX_VALUE) {
                mesos_ = Integer.MAX_VALUE;
            } else if (mesos_ < Integer.MIN_VALUE) {
                mesos_ = Integer.MIN_VALUE;
            }
        } catch (NumberFormatException nfe) {
            if (value_.contentEquals("max")) {  // "max" descriptor suggestion thanks to Vcoc
                mesos_ = Integer.MAX_VALUE;
            } else if (value_.contentEquals("min")) {
                mesos_ = Integer.MIN_VALUE;
            }
        }
        
        MapleCharacter victim = c.getWorldServer().getPlayerStorage().getCharacterByName(recv_);
        if (victim != null) {
            victim.gainMeso((int) mesos_, true);
            player.message("MESO given.");
        } else {
            player.message("Player '" + recv_ + "' could not be found.");
        }
    }
}
