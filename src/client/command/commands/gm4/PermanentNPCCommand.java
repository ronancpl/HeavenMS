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
import java.sql.SQLException;
import net.server.Server;
import net.server.channel.Channel;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author asafgb
 */
public class PermanentNPCCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !permnpc <npcId>");
            return;
        }

       int npcId = Integer.parseInt(params[0]);
        MapleNPC npc = MapleLifeFactory.getNPC(npcId);
        int xpos = player.getPosition().x;
        int ypos = player.getPosition().y;
        int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
        int f;
        //TODO find how to make left and right
        //int f = player.getStance() == 4 ? 1 : 0;
        if(player.getStance() == 4)
            f=1;
        else
            f=0;
        if (npc != null && !npc.getName().equals("MISSINGNO")) {
            
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO npcs ( idd, f, fh, cy, rx0, rx1, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                ps.setInt(1, npcId);
                ps.setInt(2, f);
                ps.setInt(3, fh);
                ps.setInt(4, ypos);
                ps.setInt(5, xpos + 50);
                ps.setInt(6, xpos - 50);
                ps.setInt(7, xpos);
                ps.setInt(8, ypos);
                ps.setInt(9, player.getMapId());
                ps.executeUpdate();
                
                ps.close();
            } catch (SQLException e) {
                c.getPlayer().dropMessage("Failed to save NPC to the database");
            }
            
            
            for (Channel channel : Server.getInstance().getChannelsFromWorld(player.getWorld())) {
                npc = MapleLifeFactory.getNPC(npcId);
                npc.setPosition(player.getPosition());
                npc.setCy(ypos);
                npc.setF(f);
                npc.setRx0(xpos + 50);
                npc.setRx1(xpos - 50);
                npc.setFh(fh);
                npc.setPosition(new Point(player.getPosition().x, player.getPosition().y));
                
                MapleMap m = channel.getMapFactory().getMap(player.getMapId());
                m.addMapObject(npc);
                m.broadcastMessage(player,MaplePacketCreator.spawnNPC(npc));
            }
        } else {
            c.getPlayer().dropMessage("You have entered an invalid Npc-Id");
        }
    }
}
