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
   @Author: Ronan
*/
package client.command.commands.gm0;

import client.command.Command;
import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;

public class MapOwnerClaimCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        if (c.tryacquireClient()) {
            try {
                MapleCharacter chr = c.getPlayer();
                
                if (ServerConstants.USE_MAP_OWNERSHIP_SYSTEM) {
                    if (chr.getEventInstance() == null) {
                        if (chr.getMap().unclaimOwnership(chr)) {
                            chr.dropMessage(5, "This lawn is now free real estate.");
                        } else if (chr.getMap().claimOwnership(chr)) {
                            chr.dropMessage(5, "You have leased this lawn for a while, until you leave here or after 1 minute of inactivity.");
                        } else {
                            chr.dropMessage(5, "This lawn has already been leased by another player.");
                        }
                    } else {
                        chr.dropMessage(5, "This lawn cannot be leased.");
                    }
                } else {
                    chr.dropMessage(5, "Feature unavailable.");
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
