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
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import tools.MaplePacketCreator;

public class NpcCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !npc <npcid>");
            return;
        }
        MapleNPC npc = MapleLifeFactory.getNPC(Integer.parseInt(params[0]));
        if (npc != null) {
            npc.setPosition(player.getPosition());
            npc.setCy(player.getPosition().y);
            npc.setRx0(player.getPosition().x + 50);
            npc.setRx1(player.getPosition().x - 50);
            npc.setFh(player.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
            player.getMap().addMapObject(npc);
            player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
        }
    }
}
