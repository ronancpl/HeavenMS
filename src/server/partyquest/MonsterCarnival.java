/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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

package server.partyquest;

import client.MapleCharacter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import server.TimerManager;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author kevintjuh93 - LOST MOTIVATION >=(
 */
public class MonsterCarnival {
    private MonsterCarnivalParty red, blue;
    private MapleMap map;
    private int room;
    private long time = 0;
    private long timeStarted = 0;
    private ScheduledFuture<?> schedule = null;

        public MonsterCarnival(int room, byte channel, MonsterCarnivalParty red1, MonsterCarnivalParty blue1) {
            //this.map = Channel.getInstance(channel).getMapFactory().getMap(980000001 + (room * 100));
            this.room = room;
            this.red = red1;
            this.blue = blue1;
            this.timeStarted = System.currentTimeMillis();
            this.time = 600000;
            map.broadcastMessage(MaplePacketCreator.getClock((int) (time / 1000)));

            for (MapleCharacter chr : red.getMembers())
                chr.setCarnival(this);
            for (MapleCharacter chr : blue.getMembers())
                chr.setCarnival(this);
            
            this.schedule = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    if (red.getTotalCP() > blue.getTotalCP()) {
                        red.setWinner(true);
                        blue.setWinner(false);
                        red.displayMatchResult();
                        blue.displayMatchResult();
                    } else if (blue.getTotalCP() > red.getTotalCP()) {
                        red.setWinner(false);
                        blue.setWinner(true);
                        red.displayMatchResult();
                        blue.displayMatchResult();
                    } else {
                        red.setWinner(false);
                        blue.setWinner(false);
                        red.displayMatchResult();
                        blue.displayMatchResult();
                    }
                    saveResults();
                    warpOut();
                }

            }, time);
           /* if (room == 0) {
                MapleData data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")).getData("Map/Map9" + (980000001 + (room * 100)) + ".img").getChildByPath("monsterCarnival");
                if (data != null) {
                    for (MapleData p : data.getChildByPath("mobGenPos").getChildren()) {
                        MapleData team = p.getChildByPath("team");
                       if (team != null) {
                           if (team.getData().equals(0))
                               redmonsterpoints.add(new Point(MapleDataTool.getInt(p.getChildByPath("x")), MapleDataTool.getInt(p.getChildByPath("y"))));
                           else
                                bluemonsterpoints.add(new Point(MapleDataTool.getInt(p.getChildByPath("x")), MapleDataTool.getInt(p.getChildByPath("y"))));
                        } else
                        monsterpoints.add(new Point(MapleDataTool.getInt(p.getChildByPath("x")), MapleDataTool.getInt(p.getChildByPath("y"))));
                    }
                    for (MapleData p : data.getChildByPath("guardianGenPos").getChildren()) {
                        MapleData team = p.getChildByPath("team");
                       if (team != null) {
                           if (team.getData().equals(0))
                               redreactorpoints.add(new Point(MapleDataTool.getInt(p.getChildByPath("x")), MapleDataTool.getInt(p.getChildByPath("y"))));
                           else
                                bluereactorpoints.add(new Point(MapleDataTool.getInt(p.getChildByPath("x")), MapleDataTool.getInt(p.getChildByPath("y"))));
                        } else
                        reactorpoints.add(new Point(MapleDataTool.getInt(p.getChildByPath("x")), MapleDataTool.getInt(p.getChildByPath("y"))));
                    }
                }
            } */
        }

        public long getTimeLeft() {
            return time - (System.currentTimeMillis() - timeStarted);
        }

        public MonsterCarnivalParty getPartyRed() {
            return red;
        }

        public MonsterCarnivalParty getPartyBlue() {
            return blue;
        }

        public MonsterCarnivalParty oppositeTeam(MonsterCarnivalParty team) {
            if (team == red)
                return blue;
            else
                return red;
        }

        public void playerLeft(MapleCharacter chr) {
            map.broadcastMessage(chr, MaplePacketCreator.leaveCPQ(chr));
        }

        private void warpOut() {
            this.schedule = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    red.warpOut();
                    blue.warpOut();
                }
            }, 12000);
        }

        public int getRoom() {
            return room;
        }

        public void saveResults() {
                Connection con = null;
                try {
                    con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO carnivalresults VALUES (?,?,?,?)");
                    for (MapleCharacter chr : red.getMembers()) {
                        ps.setInt(1, chr.getId());
                        ps.setInt(2, chr.getCP());
                        ps.setInt(3, red.getTotalCP());
                        ps.setInt(4, red.isWinner() ? 1 : 0);
                        ps.execute();
                    }
                    for (MapleCharacter chr : blue.getMembers()) {
                        ps.setInt(1, chr.getId());
                        ps.setInt(2, chr.getCP());
                        ps.setInt(3, blue.getTotalCP());
                        ps.setInt(4, blue.isWinner() ? 1 : 0);
                        ps.execute();
                    }
                    ps.close();
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
        }
}
