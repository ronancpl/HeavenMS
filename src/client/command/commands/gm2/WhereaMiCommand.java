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
package client.command.commands.gm2;

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.MapleMapObject;

public class WhereaMiCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        player.yellowMessage("Map ID: " + player.getMap().getId());
        player.yellowMessage("Players on this map:");
        for (MapleMapObject mmo : player.getMap().getPlayers()) {
            MapleCharacter chr = (MapleCharacter) mmo;
            player.dropMessage(5, ">> " + chr.getName() + " - " + chr.getId() + " - Oid: " + chr.getObjectId());
        }
        player.yellowMessage("NPCs on this map:");
        for (MapleMapObject npcs : player.getMap().getMapObjects()) {
            if (npcs instanceof MapleNPC) {
                MapleNPC npc = (MapleNPC) npcs;
                player.dropMessage(5, ">> " + npc.getName() + " - " + npc.getId() + " - Oid: " + npc.getObjectId());
            }
        }
        player.yellowMessage("Monsters on this map:");
        for (MapleMapObject mobs : player.getMap().getMapObjects()) {
            if (mobs instanceof MapleMonster) {
                MapleMonster mob = (MapleMonster) mobs;
                if (mob.isAlive()) {
                    player.dropMessage(5, ">> " + mob.getName() + " - " + mob.getId() + " - Oid: " + mob.getObjectId());
                }
            }
        }
    }
}
