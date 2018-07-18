/*
    This file is part of the HeavenMS MapleStory Server
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
package client.processor;

import client.MapleClient;
import client.MapleCharacter;
import client.MapleStat;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import server.maps.MapleMap;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author RonanLana
 */
public class BuybackProcessor {
    
    public static void processBuyback(MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        boolean buyback;
        
        c.lockClient();
        try {
            buyback = !chr.isAlive() && chr.couldBuyback();
        } finally {
            c.unlockClient();
        }

        if(buyback) {
            String jobString;
            switch(chr.getJobStyle()) {
                case WARRIOR:
                    jobString = "warrior";
                    break;

                case MAGICIAN:
                    jobString = "magician";
                    break;

                case BOWMAN:
                    jobString = "bowman";
                    break;

                case THIEF:
                    jobString = "thief";
                    break;

                case BRAWLER:
                case GUNSLINGER:
                    jobString = "pirate";
                    break;

                default:
                    jobString = "beginner";
            }

            chr.setStance(0);

            chr.setHp(chr.getMaxHp());
            chr.setMp(chr.getMaxMp());

            List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<>(2);
            hpmpupdate.add(new Pair<>(MapleStat.HP, Integer.valueOf(chr.getHp())));
            hpmpupdate.add(new Pair<>(MapleStat.MP, Integer.valueOf(chr.getMp())));
            c.announce(MaplePacketCreator.updatePlayerStats(hpmpupdate, true, chr));

            AbsoluteLifeMovement alm = new AbsoluteLifeMovement(0, chr.getPosition(), 0, 0);
            alm.setPixelsPerSecond(new Point(0, 0));
            List<LifeMovementFragment> moveUpdate = Collections.singletonList((LifeMovementFragment) alm);

            MapleMap map = chr.getMap();
            map.broadcastMessage(chr, MaplePacketCreator.movePlayer(c.getPlayer().getId(), moveUpdate), false);

            map.broadcastMessage(MaplePacketCreator.playSound("Buyback/" + jobString));
            map.broadcastMessage(MaplePacketCreator.earnTitleMessage(chr.getName() + " just bought back into the game!"));

            chr.announce(MaplePacketCreator.showBuybackEffect());
            map.broadcastMessage(chr, MaplePacketCreator.showForeignBuybackEffect(chr.getId()), false);
        }
    }
}
