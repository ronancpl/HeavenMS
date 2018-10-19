/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.command.commands.gm4;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.server.Server;
import net.server.channel.Channel;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author asafgb
 */
public class PermanentMobCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !permob <mobId> <optinal: mobTime in seconds>");
            return;
        }
        int mobTime = 0;
        int MobId = Integer.parseInt(params[0]);
        if (params.length == 2)
             mobTime = Math.max(Integer.parseInt(params[1]),0);
        
        MapleMonster mob = MapleLifeFactory.getMonster(MobId);
        if (mob != null && !mob.getName().equals("MISSINGNO")) {
            
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO mobs ( idd, f, fh, cy, rx0, rx1, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                ps.setInt(1, MobId);
                ps.setInt(2, 0);
                ps.setInt(3, player.getMap().getFootholds().findBelow(player.getPosition()).getId());
                ps.setInt(4, player.getPosition().y);
                ps.setInt(5, player.getPosition().x + 50);
                ps.setInt(6, player.getPosition().x - 50);
                ps.setInt(7, player.getPosition().x);
                ps.setInt(8, player.getPosition().y);
                ps.setInt(9, player.getMapId());
                ps.setInt(10, mobTime);
                ps.executeUpdate();
            } catch (SQLException e) {
                c.getPlayer().dropMessage("Failed to save MOB to the database"); 
            }
            
            for (Channel channel : Server.getInstance().getChannelsFromWorld(player.getWorld())) {
                mob = MapleLifeFactory.getMonster(MobId);
                mob.setPosition(player.getPosition());
                mob.setCy(player.getPosition().y);
                mob.setRx0(player.getPosition().x + 50);
                mob.setRx1(player.getPosition().x - 50);
                mob.setFh(player.getMap().getFootholds().findBelow(player.getPosition()).getId());
                MapleMap m = channel.getMapFactory().getMap(player.getMapId());
                           m.PermaddMonsterSpawn(mob, mobTime,-1);
            }   
        }
    }
}

