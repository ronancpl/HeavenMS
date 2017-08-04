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
package server.events.gm;

import client.MapleCharacter;
import tools.Randomizer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.TimerManager;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 *
 * @author FloppyDisk
 */
public final class MapleOxQuiz {
    private int round = 1;
    private int question = 1;
    private MapleMap map = null;
    private int expGain = 200;
    private static MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));

    public MapleOxQuiz(MapleMap map) {
        this.map = map;
        this.round = Randomizer.nextInt(9);
        this.question = 1;
    }

    private boolean isCorrectAnswer(MapleCharacter chr, int answer) {
        double x = chr.getPosition().getX();
        double y = chr.getPosition().getY();
        if ((x > -234 && y > -26 && answer == 0) || (x < -234 && y > -26 && answer == 1)) {
            chr.dropMessage("Correct!");
            return true;
        }
        return false;
    }

    public void sendQuestion() {
        int gm = 0;
        for (MapleCharacter mc : map.getCharacters()) {
            if (mc.gmLevel() > 1) {
                gm++;
            }
        }
        final int number = gm;
        map.broadcastMessage(MaplePacketCreator.showOXQuiz(round, question, true));
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                map.broadcastMessage(MaplePacketCreator.showOXQuiz(round, question, true));
				List<MapleCharacter> chars = new ArrayList<>(map.getCharacters());
				
                for (MapleCharacter chr : chars) {
                    if (chr != null) // make sure they aren't null... maybe something can happen in 12 seconds.
                    {
                        if (!isCorrectAnswer(chr, getOXAnswer(round, question)) && !chr.isGM()) {
                            chr.changeMap(chr.getMap().getReturnMap());
                        } else {
                            chr.gainExp(expGain, true, true);
                        }
                    }
                }
                //do question
                if ((round == 1 && question == 29) || ((round == 2 || round == 3) && question == 17) || ((round == 4 || round == 8) && question == 12) || (round == 5 && question == 26) || (round == 9 && question == 44) || ((round == 6 || round == 7) && question == 16)) {
                    question = 100;
                } else {
                    question++;
                }
                //send question
                if (map.getCharacters().size() - number <= 2) {
                    map.broadcastMessage(MaplePacketCreator.serverNotice(6, "The event has ended"));
                    map.getPortal("join00").setPortalStatus(true);
                    map.setOx(null);
                    map.setOxQuiz(false);
                    //prizes here
                    return;
                }
                sendQuestion();
            }
        }, 30000); // Time to answer = 30 seconds ( Ox Quiz packet shows a 30 second timer.
    }

    private static int getOXAnswer(int imgdir, int id) {
        return MapleDataTool.getInt(stringData.getData("OXQuiz.img").getChildByPath("" + imgdir + "").getChildByPath("" + id + "").getChildByPath("a"));
    }
}
