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
package client.command.commands.gm4;

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
                Equip eq = (Equip) equip.getItem(i);
                if (eq == null) continue;

                eq.setWdef(newStat);
                eq.setAcc(newStat);
                eq.setAvoid(newStat);
                eq.setJump(newSpdJmp);
                eq.setMatk(newStat);
                eq.setMdef(newStat);
                eq.setHp(newStat);
                eq.setMp(newStat);
                eq.setSpeed(newSpdJmp);
                eq.setWatk(newStat);
                eq.setDex(newStat);
                eq.setInt(newStat);
                eq.setStr(newStat);
                eq.setLuk(newStat);

                byte flag = eq.getFlag();
                flag |= ItemConstants.UNTRADEABLE;
                eq.setFlag(flag);

                player.forceUpdateItem(eq);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
