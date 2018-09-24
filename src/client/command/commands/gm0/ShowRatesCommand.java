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

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;
import constants.ServerConstants;

public class ShowRatesCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        String showMsg = "#eEXP RATE#n" + "\r\n";
        showMsg += "Server EXP Rate: #k" + c.getWorldServer().getExpRate() + "x#k" + "\r\n";
        showMsg += "Player EXP Rate: #k" + player.getRawExpRate() + "x#k" + "\r\n";
        if(player.getCouponExpRate() != 1) showMsg += "Coupon EXP Rate: #k" + player.getCouponExpRate() + "x#k" + "\r\n";
        showMsg += "EXP Rate: #e#b" + player.getExpRate() + "x#k#n" + "\r\n";

        showMsg += "\r\n" + "#eMESO RATE#n" + "\r\n";
        showMsg += "Server MESO Rate: #k" + c.getWorldServer().getMesoRate() + "x#k" + "\r\n";
        showMsg += "Player MESO Rate: #k" + player.getRawMesoRate() + "x#k" + "\r\n";
        if(player.getCouponMesoRate() != 1) showMsg += "Coupon MESO Rate: #k" + player.getCouponMesoRate() + "x#k" + "\r\n";
        showMsg += "MESO Rate: #e#b" + player.getMesoRate() + "x#k#n" + "\r\n";

        showMsg += "\r\n" + "#eDROP RATE#n" + "\r\n";
        showMsg += "Server DROP Rate: #k" + c.getWorldServer().getDropRate() + "x#k" + "\r\n";
        showMsg += "Player DROP Rate: #k" + player.getRawDropRate() + "x#k" + "\r\n";
        if(player.getCouponDropRate() != 1) showMsg += "Coupon DROP Rate: #k" + player.getCouponDropRate() + "x#k" + "\r\n";
        showMsg += "DROP Rate: #e#b" + player.getDropRate() + "x#k#n" + "\r\n";

        if(ServerConstants.USE_QUEST_RATE) {
            showMsg += "\r\n" + "#eQUEST RATE#n" + "\r\n";
            showMsg += "Server QUEST Rate: #e#b" + c.getWorldServer().getQuestRate() + "x#k#n" + "\r\n";
        }
        
        showMsg += "\r\n" + "#eTRAVEL RATE#n" + "\r\n";
        showMsg += "Server TRAVEL Rate: #e#b" + c.getWorldServer().getTravelRate() + "x#k#n" + "\r\n";

        player.showHint(showMsg, 300);
    }
}
