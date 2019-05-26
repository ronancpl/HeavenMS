/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

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
package client.command.commands.gm4;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.server.channel.Channel;
import server.life.MapleLifeFactory;
import server.maps.MapleMap;
import client.command.Command;
import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import server.life.MapleMonster;
import tools.DatabaseConnection;

public class PmobCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !pmob <mobid> [<mobtime>]");
            return;
        }

        // command suggestion thanks to HighKey21, none, bibiko94 (TAYAMO), asafgb
        int mapId = player.getMapId();
        int mobId = Integer.parseInt(params[0]);
        int mobTime = (params.length > 1) ? Integer.parseInt(params[1]) : -1;
        
        Point checkpos = player.getMap().getGroundBelow(player.getPosition());
        int xpos = checkpos.x;
        int ypos = checkpos.y;
        int fh = player.getMap().getFootholds().findBelow(checkpos).getId();
        
        MapleMonster mob = MapleLifeFactory.getMonster(mobId);
        if (mob != null && !mob.getName().equals("MISSINGNO")) {
            mob.setPosition(checkpos);
            mob.setCy(ypos);
            mob.setRx0(xpos + 50);
            mob.setRx1(xpos - 50);
            mob.setFh(fh);
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO plife ( life, f, fh, cy, rx0, rx1, type, x, y, world, map, mobtime, hide ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                ps.setInt(1, mobId);
                ps.setInt(2, 0);
                ps.setInt(3, fh);
                ps.setInt(4, ypos);
                ps.setInt(5, xpos + 50);
                ps.setInt(6, xpos - 50);
                ps.setString(7, "m");
                ps.setInt(8, xpos);
                ps.setInt(9, ypos);
                ps.setInt(10, player.getWorld());
                ps.setInt(11, mapId);
                ps.setInt(12, mobTime);
                ps.setInt(13, 0);
                ps.executeUpdate();
                ps.close();
                con.close();
                
                for (Channel ch: player.getWorldServer().getChannels()) {
                    MapleMap map = ch.getMapFactory().getMap(mapId);
                    map.addMonsterSpawn(mob, mobTime, -1);
                    map.addAllMonsterSpawn(mob, mobTime, -1);
                }
                
                player.yellowMessage("Pmob created.");
            } catch (SQLException e) {
                e.printStackTrace();
                player.dropMessage(5, "Failed to store pmob in the database.");
            }
        } else {
            player.dropMessage(5, "You have entered an invalid mob id.");
        }
    }
}