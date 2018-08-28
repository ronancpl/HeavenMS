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
package client.command.commands.v0;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.command.Command;

public class StatLukCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        int remainingAp = player.getRemainingAp();

        int amount = (params.length > 0) ? Math.min(Integer.parseInt(params[0]), remainingAp) : remainingAp;
        if (amount > 0 && amount <= remainingAp && amount <= 32763) {
            int playerStat = player.getLuk();
            
            if (amount + playerStat <= 32767 && amount + playerStat >= 4) {
                player.setLuk(playerStat + amount);
                player.updateSingleStat(MapleStat.LUK, playerStat);
                player.setRemainingAp(remainingAp - amount);
                player.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
            } else {
                player.dropMessage("Please make sure the stat you are trying to raise is not over 32,767 or under 4.");
            }
        } else {
            player.dropMessage("Please make sure your AP is not over 32,767 and you have enough to distribute.");
        }
    }
}
