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
package server.maps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.maps.MapleReactorStats.StateData;
import tools.Pair;
import tools.StringUtil;

public class MapleReactorFactory {
    private static MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Reactor.wz"));
    private static Map<Integer, MapleReactorStats> reactorStats = new HashMap<Integer, MapleReactorStats>();

    public static MapleReactorStats getReactor(int rid) {
        MapleReactorStats stats = reactorStats.get(Integer.valueOf(rid));
        if (stats == null) {
            int infoId = rid;
            MapleData reactorData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(infoId) + ".img", '0', 11));
            MapleData link = reactorData.getChildByPath("info/link");
            if (link != null) {
                infoId = MapleDataTool.getIntConvert("info/link", reactorData);
                stats = reactorStats.get(Integer.valueOf(infoId));
            }
            MapleData activateOnTouch = reactorData.getChildByPath("info/activateByTouch");
            boolean loadArea = false;
            if (activateOnTouch != null) {
                loadArea = MapleDataTool.getInt("info/activateByTouch", reactorData, 0) != 0;
            }
            if (stats == null) {
                reactorData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(infoId) + ".img", '0', 11));
                MapleData reactorInfoData = reactorData.getChildByPath("0");
                stats = new MapleReactorStats();
                List<StateData> statedatas = new ArrayList<>();
                if (reactorInfoData != null) {
                    boolean areaSet = false;
                    byte i = 0;
                    while (reactorInfoData != null) {
                        MapleData eventData = reactorInfoData.getChildByPath("event");
                        if (eventData != null) {
                            int timeOut = -1;
                            
                            for (MapleData fknexon : eventData.getChildren()) {
                                if (fknexon.getName().equals("timeOut")) {
                                    timeOut = MapleDataTool.getInt(fknexon);
                                } else {
                                    Pair<Integer, Integer> reactItem = null;
                                    int type = MapleDataTool.getIntConvert("type", fknexon);
                                    if (type == 100) { //reactor waits for item
                                        reactItem = new Pair<Integer, Integer>(MapleDataTool.getIntConvert("0", fknexon), MapleDataTool.getIntConvert("1", fknexon));
                                        if (!areaSet || loadArea) { //only set area of effect for item-triggered reactors once
                                            stats.setTL(MapleDataTool.getPoint("lt", fknexon));
                                            stats.setBR(MapleDataTool.getPoint("rb", fknexon));
                                            areaSet = true;
                                        }
                                    }
                                    MapleData activeSkillID = fknexon.getChildByPath("activeSkillID");
                                    List<Integer> skillids = null;
                                    if (activeSkillID != null) {
                                        skillids = new ArrayList<Integer>();
                                        for (MapleData skill : activeSkillID.getChildren()) {
                                            skillids.add(MapleDataTool.getInt(skill));
                                        }
                                    }
                                    byte nextState = (byte) MapleDataTool.getIntConvert("state", fknexon);
                                    statedatas.add(new StateData(type, reactItem, skillids, nextState));
                                }
                            }
                            stats.addState(i, statedatas, timeOut);
                        }
                        i++;
                        reactorInfoData = reactorData.getChildByPath(Byte.toString(i));
                        statedatas = new ArrayList<>();
                    }
                } else //sit there and look pretty; likely a reactor such as Zakum/Papulatus doors that shows if player can enter
                {
                    statedatas.add(new StateData(999, null, null, (byte) 0));
                    stats.addState((byte) 0, statedatas, -1);
                }
                reactorStats.put(Integer.valueOf(infoId), stats);
                if (rid != infoId) {
                    reactorStats.put(Integer.valueOf(rid), stats);
                }
            } else // stats exist at infoId but not rid; add to map
            {
                reactorStats.put(Integer.valueOf(rid), stats);
            }
        }
        return stats;
    }
}
