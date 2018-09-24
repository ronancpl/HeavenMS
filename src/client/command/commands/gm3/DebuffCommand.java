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

import client.MapleDisease;
import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;

import java.util.Arrays;

public class DebuffCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !debuff SLOW|SEDUCE|ZOMBIFY|CONFUSE|STUN|POISON|SEAL|DARKNESS|WEAKEN|CURSE");
            return;
        }

        MapleDisease disease = null;
        MobSkill skill = null;

        switch (params[0].toUpperCase()) {
            case "SLOW":
                disease = MapleDisease.SLOW;
                skill = MobSkillFactory.getMobSkill(126, 7);
                break;

            case "SEDUCE":
                disease = MapleDisease.SEDUCE;
                skill = MobSkillFactory.getMobSkill(128, 7);
                break;

            case "ZOMBIFY":
                disease = MapleDisease.ZOMBIFY;
                skill = MobSkillFactory.getMobSkill(133, 1);
                break;

            case "CONFUSE":
                disease = MapleDisease.CONFUSE;
                skill = MobSkillFactory.getMobSkill(132, 2);
                break;

            case "STUN":
                disease = MapleDisease.STUN;
                skill = MobSkillFactory.getMobSkill(123, 7);
                break;

            case "POISON":
                disease = MapleDisease.POISON;
                skill = MobSkillFactory.getMobSkill(125, 5);
                break;

            case "SEAL":
                disease = MapleDisease.SEAL;
                skill = MobSkillFactory.getMobSkill(120, 1);
                break;

            case "DARKNESS":
                disease = MapleDisease.DARKNESS;
                skill = MobSkillFactory.getMobSkill(121, 1);
                break;

            case "WEAKEN":
                disease = MapleDisease.WEAKEN;
                skill = MobSkillFactory.getMobSkill(122, 1);
                break;

            case "CURSE":
                disease = MapleDisease.CURSE;
                skill = MobSkillFactory.getMobSkill(124, 1);
                break;
        }

        if (disease == null) {
            player.yellowMessage("Syntax: !debuff SLOW|SEDUCE|ZOMBIFY|CONFUSE|STUN|POISON|SEAL|DARKNESS|WEAKEN|CURSE");
            return;
        }

        for (MapleMapObject mmo : player.getMap().getMapObjectsInRange(player.getPosition(), 777777.7, Arrays.asList(MapleMapObjectType.PLAYER))) {
            MapleCharacter chr = (MapleCharacter) mmo;

            if (chr.getId() != player.getId()) {
                chr.giveDebuff(disease, skill);
            }
        }
    }
}
