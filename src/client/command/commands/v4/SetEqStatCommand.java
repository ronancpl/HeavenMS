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
package client.command.commands.v4;

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import client.inventory.Equip;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;

public class SetEqStatCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !seteqstat <stat value> [<spdjmp value>]");
            return;
        }

        short newStat = (short) Math.max(0, Integer.parseInt(params[0]));
        short newSpdJmp = params.length >= 2 ? (short) Integer.parseInt(params[1]) : 0;
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        
        for (byte i = 1; i <= equip.getSlotLimit(); i++) {
            try {
                Equip eu = (Equip) equip.getItem(i);
                if (eu == null) continue;

                eu.setWdef(newStat);
                eu.setAcc(newStat);
                eu.setAvoid(newStat);
                eu.setJump(newSpdJmp);
                eu.setMatk(newStat);
                eu.setMdef(newStat);
                eu.setHp(newStat);
                eu.setMp(newStat);
                eu.setSpeed(newSpdJmp);
                eu.setWatk(newStat);
                eu.setDex(newStat);
                eu.setInt(newStat);
                eu.setStr(newStat);
                eu.setLuk(newStat);

                byte flag = eu.getFlag();
                flag |= ItemConstants.UNTRADEABLE;
                eu.setFlag(flag);

                player.forceUpdateItem(eu);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
