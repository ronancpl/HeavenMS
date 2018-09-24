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
package client.command.commands.gm0;

import client.MapleClient;
import client.command.Command;
import constants.ServerConstants;

public class DropLimitCommand extends Command {
    {
        setDescription("");
    }
    
    @Override
    public void execute(MapleClient c, String[] params) {
        int dropCount = c.getPlayer().getMap().getDroppedItemCount();
        if(((float) dropCount) / ServerConstants.ITEM_LIMIT_ON_MAP < 0.75f) {
            c.getPlayer().showHint("Current drop count: #b" + dropCount + "#k / #e" + ServerConstants.ITEM_LIMIT_ON_MAP + "#n", 300);
        } else {
            c.getPlayer().showHint("Current drop count: #r" + dropCount + "#k / #e" + ServerConstants.ITEM_LIMIT_ON_MAP + "#n", 300);
        }

    }
}
