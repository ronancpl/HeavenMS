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

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import server.MapleItemInformationProvider;

public class RechargeCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Item torecharge : c.getPlayer().getInventory(MapleInventoryType.USE).list()) {
            if (ItemConstants.isThrowingStar(torecharge.getItemId())){
                torecharge.setQuantity(ii.getSlotMax(c, torecharge.getItemId()));
                c.getPlayer().forceUpdateItem(torecharge);
            } else if (ItemConstants.isArrow(torecharge.getItemId())){
                torecharge.setQuantity(ii.getSlotMax(c, torecharge.getItemId()));
                c.getPlayer().forceUpdateItem(torecharge);
            } else if (ItemConstants.isBullet(torecharge.getItemId())){
                torecharge.setQuantity(ii.getSlotMax(c, torecharge.getItemId()));
                c.getPlayer().forceUpdateItem(torecharge);
            } else if (ItemConstants.isConsumable(torecharge.getItemId())){
                torecharge.setQuantity(ii.getSlotMax(c, torecharge.getItemId()));
                c.getPlayer().forceUpdateItem(torecharge);
            }
        }
        player.dropMessage(5, "USE Recharged.");
    }
}
