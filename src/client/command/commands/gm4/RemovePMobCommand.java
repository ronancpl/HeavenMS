/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.command.commands.gm4;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import jdk.nashorn.internal.objects.NativeArray;
import net.server.Server;
import net.server.channel.Channel;
import server.life.AbstractLoadedMapleLife;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.SpawnPoint;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author asafgb
 */
public class RemovePMobCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax to get the list: !rpmob list");
            player.yellowMessage("Syntax: !rpmob <rowIndex>");
            return;
        }

        if(params[0].equals("list"))
        {
            StringBuilder builder = new StringBuilder();
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM mobs WHERE mid = ?");
                ps.setInt(1, player.getMapId());
                ResultSet rs = ps.executeQuery();
                builder.append("The Perm mobs on that map: \r\n");
                while (rs.next()) {
                    builder.append("Row Index: "+rs.getInt("id"));
                    builder.append("Mob Id: "+rs.getInt("idd"));
                    builder.append("Mob Name: "+ MapleLifeFactory.getMonster(rs.getInt("idd")).getName());
                    builder.append("fh: "+rs.getInt("fh"));
                    builder.append("cy: "+rs.getInt("cy"));
                    builder.append("rx0: "+rs.getInt("rx0"));
                    builder.append("rx1: "+rs.getInt("rx1"));
                    builder.append("x: "+rs.getInt("x"));
                    builder.append("y: "+rs.getInt("y"));  
                    builder.append("mobtime: "+rs.getInt("mobtime"));  
                    builder.append("\r\n");
                }
                ps.close();
                rs.close();
                player.dropMessage(builder.toString());
            } catch (SQLException e) {
                        e.printStackTrace();
            }
        
        }else{
            try
            {
                int DBIndex = Integer.parseInt(params[0]);
                MapleMonster mob = null;
                int Mobtime=-1;
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM mobs WHERE id = ?");
                ps.setInt(1, DBIndex);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    mob=MapleLifeFactory.getMonster(rs.getInt("idd"));
                    mob.setFh(rs.getInt("fh"));
                    mob.setCy(rs.getInt("cy"));
                    mob.setRx0(rs.getInt("rx0"));
                    mob.setRx1(rs.getInt("rx1"));
                    mob.setPosition(new Point(rs.getInt("x"), rs.getInt("y")));
                    Mobtime=rs.getInt("mobtime");
                }
                ps.close();
                rs.close();
                
                if(mob != null)
                {
                    // Delete from DB
                    PreparedStatement ps1 = con.prepareStatement("DELETE FROM mobs WHERE id = ?");
                    ps1.setInt(1, DBIndex);
                    int rs1 = ps1.executeUpdate();
                    ps1.close();
                    
                    
                    

                    for (Channel channel : Server.getInstance().getChannelsFromWorld(player.getWorld())) {
                        SpawnPoint removeMobSpawn = null;
                        MapleMap m = channel.getMapFactory().getMap(player.getMapId());
                        for (SpawnPoint SP : m.GetPermMonsterSpawn()) {
                    
                            if(SP.getMonster().getId() == mob.getId() &&
                               SP.getMobTime() == Mobtime )
                            {
                                removeMobSpawn = SP;
                            }
                            if(removeMobSpawn != null)
                            {
                                player.getMap().GetPermMonsterSpawn().remove(removeMobSpawn);
                                player.getMap().damageMonster(player, removeMobSpawn.getMonster(), Integer.MAX_VALUE);
                            }
                            else
                            {
                                System.out.print("Cant Find that Spawn, Map: "+ player.getMap().getId() + " Mobs DB table Index: "+DBIndex);
                            }
                        }
                    }
                }
                else
                {
                    player.dropMessage("Wrong Index in the database run: !rpMob list");
                }
                
                
            }catch(SQLException e)
            {
                 e.printStackTrace();
            }
            catch(NumberFormatException x)
            {
                x.printStackTrace();
            }
        }
    }
}
