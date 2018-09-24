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

import client.MapleStat;
import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import server.MapleItemInformationProvider;

public class FaceCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !face [<playername>] <faceid>");
            return;
        }

        try {
            if (params.length == 1) {
                int itemId = Integer.parseInt(params[0]);
                if (!(itemId >= 20000 && itemId < 22000) || MapleItemInformationProvider.getInstance().getName(itemId) == null) {
                    player.yellowMessage("Face id '" + params[0] + "' does not exist.");
                    return;
                }

                player.setFace(itemId);
                player.updateSingleStat(MapleStat.FACE, itemId);
                player.equipChanged();
            } else {
                int itemId = Integer.parseInt(params[1]);
                if (!(itemId >= 20000 && itemId < 22000) || MapleItemInformationProvider.getInstance().getName(itemId) == null) {
                    player.yellowMessage("Face id '" + params[1] + "' does not exist.");
                }

                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(params[0]);
                if (victim == null) {
                    victim.setFace(itemId);
                    victim.updateSingleStat(MapleStat.FACE, itemId);
                    victim.equipChanged();
                } else {
                    player.yellowMessage("Player '" + params[0] + "' has not been found on this channel.");
                }
            }
        } catch (Exception e) {
        }

    }
}
