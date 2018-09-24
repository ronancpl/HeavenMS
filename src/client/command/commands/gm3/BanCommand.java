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

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import net.server.Server;
import server.TimerManager;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BanCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 2) {
            player.yellowMessage("Syntax: !ban <IGN> <Reason> (Please be descriptive)");
            return;
        }
        String ign = params[0];
        String reason = joinStringFrom(params, 1);
        MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(ign);
        if (target != null) {
            String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
            String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
            //Ban ip
            PreparedStatement ps = null;
            try {
                Connection con = DatabaseConnection.getConnection();
                if (ip.matches("/[0-9]{1,3}\\..*")) {
                    ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?, ?)");
                    ps.setString(1, ip);
                    ps.setString(2, String.valueOf(target.getClient().getAccID()));

                    ps.executeUpdate();
                    ps.close();
                }

                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                c.getPlayer().message("Error occured while banning IP address");
                c.getPlayer().message(target.getName() + "'s IP was not banned: " + ip);
            }
            target.getClient().banMacs();
            reason = c.getPlayer().getName() + " banned " + readableTargetName + " for " + reason + " (IP: " + ip + ") " + "(MAC: " + c.getMacs() + ")";
            target.ban(reason);
            target.yellowMessage("You have been banned by #b" + c.getPlayer().getName() + " #k.");
            target.yellowMessage("Reason: " + reason);
            c.announce(MaplePacketCreator.getGMEffect(4, (byte) 0));
            final MapleCharacter rip = target;
            TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    rip.getClient().disconnect(false, false);
                }
            }, 5000); //5 Seconds
            Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.serverNotice(6, "[RIP]: " + ign + " has been banned."));
        } else if (MapleCharacter.ban(ign, reason, false)) {
            c.announce(MaplePacketCreator.getGMEffect(4, (byte) 0));
            Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.serverNotice(6, "[RIP]: " + ign + " has been banned."));
        } else {
            c.announce(MaplePacketCreator.getGMEffect(6, (byte) 1));
        }
    }
}
