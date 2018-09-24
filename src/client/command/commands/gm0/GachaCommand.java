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

import client.command.Command;
import client.MapleClient;
import server.MapleItemInformationProvider;
import server.gachapon.MapleGachapon;
import tools.MaplePacketCreator;

public class GachaCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleGachapon.Gachapon gacha = null;
        String search = joinStringFrom(params,0);
        String gachaName = "";
        String [] names = {"Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa Male", "Showa Spa Female", "New Leaf City", "Nautilus Harbor"};
        int [] ids = {9100100, 9100101, 9100102, 9100103, 9100104, 9100105, 9100106, 9100107, 9100109, 9100117};
        for (int i = 0; i < names.length; i++){
            if (search.equalsIgnoreCase(names[i])){
                gachaName = names[i];
                gacha = MapleGachapon.Gachapon.getByNpcId(ids[i]);
            }
        }
        if (gacha == null){
            c.getPlayer().yellowMessage("Please use @gacha <name> where name corresponds to one of the below:");
            for (String name : names){
                c.getPlayer().yellowMessage(name);
            }
            return;
        }
        String talkStr = "The #b" + gachaName + "#k Gachapon contains the following items.\r\n\r\n";
        for (int i = 0; i < 2; i++){
            for (int id : gacha.getItems(i)){
                talkStr += "-" + MapleItemInformationProvider.getInstance().getName(id) + "\r\n";
            }
        }
        talkStr += "\r\nPlease keep in mind that there are items that are in all gachapons and are not listed here.";
        c.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, talkStr, "00 00", (byte) 0));
    }
}
