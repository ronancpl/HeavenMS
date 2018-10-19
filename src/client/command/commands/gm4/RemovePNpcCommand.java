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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.server.Server;
import net.server.channel.Channel;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.SpawnPoint;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author asafgb
 */
public class RemovePNpcCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
          MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax to get the list: !rpnpc list");
            player.yellowMessage("Syntax: !rpnpc <rowIndex>");
            return;
        }

        if(params[0].equals("list"))
        {
            StringBuilder builder = new StringBuilder();
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM npcs WHERE mid = ?");
                ps.setInt(1, player.getMapId());
                ResultSet rs = ps.executeQuery();
                builder.append("The Perm npcs on that map: \r\n");
                while (rs.next()) {
                    builder.append("Row Index: "+rs.getInt("id"));
                    builder.append("Npc Id: "+rs.getInt("idd"));
                    builder.append("Npc Name: "+ MapleLifeFactory.getNPC(rs.getInt("idd")).getName());
                    builder.append("f: "+rs.getInt("f"));
                    builder.append("fh: "+rs.getInt("fh"));
                    builder.append("cy: "+rs.getInt("cy"));
                    builder.append("rx0: "+rs.getInt("rx0"));
                    builder.append("rx1: "+rs.getInt("rx1"));
                    builder.append("x: "+rs.getInt("x"));
                    builder.append("y: "+rs.getInt("y"));  
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
                MapleNPC npc = null,onpc=null,removeNpc=null;
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM npcs WHERE id = ? and mid = ?");
                ps.setInt(1, DBIndex);
                ps.setInt(2, player.getMapId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    npc=MapleLifeFactory.getNPC(rs.getInt("idd"));
                    npc.setFh(rs.getInt("fh"));
                    npc.setF(rs.getInt("f"));
                    npc.setCy(rs.getInt("cy"));
                    npc.setRx0(rs.getInt("rx0"));
                    npc.setRx1(rs.getInt("rx1"));
                    npc.setPosition(new Point(rs.getInt("x"), rs.getInt("y")));
                }
                ps.close();
                rs.close();
                
                if(npc != null)
                {
                    // Delete from DB
                    PreparedStatement ps1 = con.prepareStatement("DELETE FROM npcs WHERE id = ?");
                    ps1.setInt(1, DBIndex);
                    int rs1 = ps1.executeUpdate();
                    ps1.close();
                    
                       for(Channel channel : Server.getInstance().getChannelsFromWorld(player.getWorld())) {
                        removeNpc=null;
                        MapleMap m = channel.getMapFactory().getMap(player.getMap().getId());
                        List<MapleMapObject> lst= m.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
                        for(MapleMapObject mmo : lst)
                        {
                               onpc=(MapleNPC)mmo;
                               if(onpc.getId()==npc.getId()&&
                                  onpc.getFh()==npc.getFh()&&
                                  onpc.getF()==npc.getF()&&
                                  onpc.getCy()==npc.getCy()&&
                                  onpc.getPosition().x==npc.getPosition().x&&
                                  onpc.getPosition().y==npc.getPosition().y)
                               {
                                   removeNpc=onpc;
                               }
                        }
                         if(removeNpc!=null)
                           {
                               while(m.getNPCById(removeNpc.getId())!=null)
                               {
                                    m.destroyNPC(removeNpc.getId());
                               }       
                               player.dropMessage("The npc Deleted from ch: "+channel.getId());
                           }
                           else
                           {
                               player.dropMessage("Didnt Find that npc at ch: "+channel.getId()+" at map : "+m.getId());
                           }
                      }
                }
                else
                {
                    player.dropMessage("Wrong Index in the database run: !rpnpc list");
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
