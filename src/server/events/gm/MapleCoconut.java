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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import server.TimerManager;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 *
 * @author kevintjuh93
 */
//Make them better :)
public class MapleCoconut extends MapleEvent {
       private MapleMap map = null;
       private int MapleScore = 0;
       private int StoryScore = 0;
       private int countBombing = 80;
       private int countFalling = 401;
       private int countStopped = 20;
       private List<MapleCoconuts> coconuts = new LinkedList<MapleCoconuts>();

       public MapleCoconut(MapleMap map) {
           super(1, 50);
           this.map = map;
       }

       public void startEvent() {
           map.startEvent();
           for (int i = 0; i < 506; i++) {
                coconuts.add(new MapleCoconuts(i));
            }
           map.broadcastMessage(MaplePacketCreator.hitCoconut(true, 0, 0));
           setCoconutsHittable(true);
           map.broadcastMessage(MaplePacketCreator.getClock(300));

        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (map.getId() == 109080000) {
                    if (getMapleScore() == getStoryScore()) {
						bonusTime();
                    } else if (getMapleScore() > getStoryScore()) {
                        for (MapleCharacter chr : map.getCharacters()) {
                            if (chr.getTeam() == 0) {
                                chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/victory"));
                                chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Victory"));
                            } else {
                                chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/lose"));
                                chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Failed"));
                            }
                        }
                        warpOut();
                    } else {
                        for (MapleCharacter chr : map.getCharacters()) {
                            if (chr.getTeam() == 1) {
                                chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/victory"));
                                chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Victory"));
                            } else {
                                chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/lose"));
                                chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Failed"));
                            }
                        }
                        warpOut();
                    }
                }
            }
        }, 300000);
       }

       public void bonusTime() {
           map.broadcastMessage(MaplePacketCreator.getClock(120));
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (getMapleScore() == getStoryScore()) {
                    for (MapleCharacter chr : map.getCharacters()) {
                        chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/lose"));
                        chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Failed"));
                    }
                    warpOut();
                } else if (getMapleScore() > getStoryScore()) {
                        for (MapleCharacter chr : map.getCharacters()) {
                            if (chr.getTeam() == 0) {
                                chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/victory"));
                                chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Victory"));
                            } else {
                                chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/lose"));
                                chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Failed"));
                            }
                        }
                        warpOut();
                    } else {
                        for (MapleCharacter chr : map.getCharacters()) {
                            if (chr.getTeam() == 1) {
                                chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/victory"));
                                chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Victory"));
                            } else {
                                chr.getClient().announce(MaplePacketCreator.showEffect("event/coconut/lose"));
                                chr.getClient().announce(MaplePacketCreator.playSound("Coconut/Failed"));
                            }
                        }
                        warpOut();
                    }
            }
        }, 120000);

       }

       public void warpOut() {
          setCoconutsHittable(false);
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
				List<MapleCharacter> chars = new ArrayList<>(map.getCharacters());
				
				for (MapleCharacter chr : chars) {
					if ((getMapleScore() > getStoryScore() && chr.getTeam() == 0) || (getStoryScore() > getMapleScore() && chr.getTeam() == 1)) {
						chr.changeMap(109050000);
					} else {
						chr.changeMap(109050001);
					}
				}
				map.setCoconut(null);
            }
        }, 12000);
       }

       public int getMapleScore() {
           return MapleScore;
       }

       public int getStoryScore() {
           return StoryScore;
       }

       public void addMapleScore() {
           this.MapleScore += 1;
       }

       public void addStoryScore() {
           this.StoryScore += 1;
       }

    public int getBombings() {
        return countBombing;
    }

    public void bombCoconut() {
        countBombing--;
    }

    public int getFalling() {
        return countFalling;
    }

    public void fallCoconut() {
        countFalling--;
    }

    public int getStopped() {
        return countStopped;
    }

    public void stopCoconut() {
        countStopped--;
    }

    public MapleCoconuts getCoconut(int id) {
        return coconuts.get(id);
    }

    public List<MapleCoconuts> getAllCoconuts() {
        return coconuts;
    }

    public void setCoconutsHittable(boolean hittable) {
        for (MapleCoconuts nut : coconuts) {
            nut.setHittable(hittable);
        }
    }
}  