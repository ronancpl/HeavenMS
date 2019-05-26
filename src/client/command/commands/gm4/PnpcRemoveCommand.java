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
import client.command.Command;
import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.Pair;

public class PnpcRemoveCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        
        int mapId = player.getMapId();
        int npcId = params.length > 0 ? Integer.parseInt(params[0]) : -1;
        
        Point pos = player.getPosition();
        int xpos = pos.x;
        int ypos = pos.y;
        
        List<Pair<Integer, Pair<Integer, Integer>>> toRemove = new LinkedList<>();
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            
            if (npcId > -1) {
                String select = "SELECT * FROM plife WHERE world = ? AND map = ? AND type LIKE ? AND life = ?";
                ps = con.prepareStatement(select, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ps.setInt(1, player.getWorld());
                ps.setInt(2, mapId);
                ps.setString(3, "n");
                ps.setInt(4, npcId);
            } else {
                String select = "SELECT * FROM plife WHERE world = ? AND map = ? AND type LIKE ? AND x >= ? AND x <= ? AND y >= ? AND y <= ?";
                ps = con.prepareStatement(select, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ps.setInt(1, player.getWorld());
                ps.setInt(2, mapId);
                ps.setString(3, "n");
                ps.setInt(4, xpos - 50);
                ps.setInt(5, xpos + 50);
                ps.setInt(6, ypos - 50);
                ps.setInt(7, ypos + 50);
            }
            
            ResultSet rs = ps.executeQuery();
            while (true) {
                rs.beforeFirst();
                if (!rs.next()) {
                    break;
                }
                
                toRemove.add(new Pair<>(rs.getInt("life"), new Pair<>(rs.getInt("x"), rs.getInt("y"))));
                rs.deleteRow();
            }
            
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            player.dropMessage(5, "Failed to remove pNPC from the database.");
        }
        
        if (!toRemove.isEmpty()) {
            for (Channel ch: player.getWorldServer().getChannels()) {
                MapleMap map = ch.getMapFactory().getMap(mapId);

                for (Pair<Integer, Pair<Integer, Integer>> r : toRemove) {
                    map.destroyNPC(r.getLeft());
                }
            }
        }
        
        player.yellowMessage("Cleared " + toRemove.size() + " pNPC placements.");
    }
}