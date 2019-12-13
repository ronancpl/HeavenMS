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

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.life.AbstractLoadedMapleLife;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MaplePlayerNPC;
import server.life.MaplePlayerNPCFactory;
import scripting.event.EventInstanceManager;
import server.partyquest.GuardianSpawnPoint;
import tools.DatabaseConnection;
import tools.StringUtil;

public class MapleMapFactory {

    private static MapleData nameData;
    private static MapleDataProvider mapSource;
    
    static {
        nameData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Map.img");
        mapSource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz"));
    }
    
    private static void loadLifeFromWz(MapleMap map, MapleData mapData) {
        for (MapleData life : mapData.getChildByPath("life")) {
            life.getName();
            String id = MapleDataTool.getString(life.getChildByPath("id"));
            String type = MapleDataTool.getString(life.getChildByPath("type"));
            int team = MapleDataTool.getInt("team", life, -1);
            if (map.isCPQMap2() && type.equals("m")) {
                if ((Integer.parseInt(life.getName()) % 2) == 0) {
                    team = 0;
                } else {
                    team = 1;
                }
            }
            int cy = MapleDataTool.getInt(life.getChildByPath("cy"));
            MapleData dF = life.getChildByPath("f");
            int f = (dF != null) ? MapleDataTool.getInt(dF) : 0;
            int fh = MapleDataTool.getInt(life.getChildByPath("fh"));
            int rx0 = MapleDataTool.getInt(life.getChildByPath("rx0"));
            int rx1 = MapleDataTool.getInt(life.getChildByPath("rx1"));
            int x = MapleDataTool.getInt(life.getChildByPath("x"));
            int y = MapleDataTool.getInt(life.getChildByPath("y"));
            int hide = MapleDataTool.getInt("hide", life, 0);
            int mobTime = MapleDataTool.getInt("mobTime", life, 0);
            
            loadLifeRaw(map, Integer.parseInt(id), type, cy, f, fh, rx0, rx1, x, y, hide, mobTime, team);
        }
    }

    private static void loadLifeFromDb(MapleMap map) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM plife WHERE map = ? and world = ?");
            ps.setInt(1, map.getId());
            ps.setInt(2, map.getWorld());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("life");
                String type = rs.getString("type");
                int cy = rs.getInt("cy");
                int f = rs.getInt("f");
                int fh = rs.getInt("fh");
                int rx0 = rs.getInt("rx0");
                int rx1 = rs.getInt("rx1");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int hide = rs.getInt("hide");
                int mobTime = rs.getInt("mobtime");
                int team = rs.getInt("team");

                loadLifeRaw(map, id, type, cy, f, fh, rx0, rx1, x, y, hide, mobTime, team);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private static void loadLifeRaw(MapleMap map, int id, String type, int cy, int f, int fh, int rx0, int rx1, int x, int y, int hide, int mobTime, int team) {
        AbstractLoadedMapleLife myLife = loadLife(id, type, cy, f, fh, rx0, rx1, x, y, hide);
        if (myLife instanceof MapleMonster) {
            MapleMonster monster = (MapleMonster) myLife;

            if (mobTime == -1) { //does not respawn, force spawn once
                map.spawnMonster(monster);
            } else {
                map.addMonsterSpawn(monster, mobTime, team);
            }

            //should the map be reseted, use allMonsterSpawn list of monsters to spawn them again
            map.addAllMonsterSpawn(monster, mobTime, team);
        } else {
            map.addMapObject(myLife);
        }
    }

    public static MapleMap loadMapFromWz(int mapid, int world, int channel, EventInstanceManager event) {
        MapleMap map;
        
        String mapName = getMapName(mapid);
        MapleData mapData = mapSource.getData(mapName);    // source.getData issue with giving nulls in rare ocasions found thanks to MedicOP
        MapleData infoData = mapData.getChildByPath("info");

        String link = MapleDataTool.getString(infoData.getChildByPath("link"), "");
        if (!link.equals("")) { //nexon made hundreds of dojo maps so to reduce the size they added links.
            mapName = getMapName(Integer.parseInt(link));
            mapData = mapSource.getData(mapName);
        }
        float monsterRate = 0;
        MapleData mobRate = infoData.getChildByPath("mobRate");
        if (mobRate != null) {
            monsterRate = ((Float) mobRate.getData()).floatValue();
        }
        map = new MapleMap(mapid, world, channel, MapleDataTool.getInt("returnMap", infoData), monsterRate);
        map.setEventInstance(event);

        String onFirstEnter = MapleDataTool.getString(infoData.getChildByPath("onFirstUserEnter"), String.valueOf(mapid));
        map.setOnFirstUserEnter(onFirstEnter.equals("") ? String.valueOf(mapid) : onFirstEnter);

        String onEnter = MapleDataTool.getString(infoData.getChildByPath("onUserEnter"), String.valueOf(mapid));
        map.setOnUserEnter(onEnter.equals("") ? String.valueOf(mapid) : onEnter);

        map.setFieldLimit(MapleDataTool.getInt(infoData.getChildByPath("fieldLimit"), 0));
        map.setMobInterval((short) MapleDataTool.getInt(infoData.getChildByPath("createMobInterval"), 5000));
        MaplePortalFactory portalFactory = new MaplePortalFactory();
        for (MapleData portal : mapData.getChildByPath("portal")) {
            map.addPortal(portalFactory.makePortal(MapleDataTool.getInt(portal.getChildByPath("pt")), portal));
        }
        MapleData timeMob = infoData.getChildByPath("timeMob");
        if (timeMob != null) {
            map.setTimeMob(MapleDataTool.getInt(timeMob.getChildByPath("id")), MapleDataTool.getString(timeMob.getChildByPath("message")));
        }

        int bounds[] = new int[4];
        bounds[0] = MapleDataTool.getInt(infoData.getChildByPath("VRTop"));
        bounds[1] = MapleDataTool.getInt(infoData.getChildByPath("VRBottom"));

        if (bounds[0] == bounds[1]) {    // old-style baked map
            MapleData minimapData = mapData.getChildByPath("miniMap");
            if (minimapData != null) {
                bounds[0] = MapleDataTool.getInt(minimapData.getChildByPath("centerX")) * -1;
                bounds[1] = MapleDataTool.getInt(minimapData.getChildByPath("centerY")) * -1;
                bounds[2] = MapleDataTool.getInt(minimapData.getChildByPath("height"));
                bounds[3] = MapleDataTool.getInt(minimapData.getChildByPath("width"));

                map.setMapPointBoundings(bounds[0], bounds[1], bounds[2], bounds[3]);
            } else {
                int dist = (1 << 18);
                map.setMapPointBoundings(-dist / 2, -dist / 2, dist, dist);
            }
        } else {
            bounds[2] = MapleDataTool.getInt(infoData.getChildByPath("VRLeft"));
            bounds[3] = MapleDataTool.getInt(infoData.getChildByPath("VRRight"));

            map.setMapLineBoundings(bounds[0], bounds[1], bounds[2], bounds[3]);
        }

        List<MapleFoothold> allFootholds = new LinkedList<>();
        Point lBound = new Point();
        Point uBound = new Point();
        for (MapleData footRoot : mapData.getChildByPath("foothold")) {
            for (MapleData footCat : footRoot) {
                for (MapleData footHold : footCat) {
                    int x1 = MapleDataTool.getInt(footHold.getChildByPath("x1"));
                    int y1 = MapleDataTool.getInt(footHold.getChildByPath("y1"));
                    int x2 = MapleDataTool.getInt(footHold.getChildByPath("x2"));
                    int y2 = MapleDataTool.getInt(footHold.getChildByPath("y2"));
                    MapleFoothold fh = new MapleFoothold(new Point(x1, y1), new Point(x2, y2), Integer.parseInt(footHold.getName()));
                    fh.setPrev(MapleDataTool.getInt(footHold.getChildByPath("prev")));
                    fh.setNext(MapleDataTool.getInt(footHold.getChildByPath("next")));
                    if (fh.getX1() < lBound.x) {
                        lBound.x = fh.getX1();
                    }
                    if (fh.getX2() > uBound.x) {
                        uBound.x = fh.getX2();
                    }
                    if (fh.getY1() < lBound.y) {
                        lBound.y = fh.getY1();
                    }
                    if (fh.getY2() > uBound.y) {
                        uBound.y = fh.getY2();
                    }
                    allFootholds.add(fh);
                }
            }
        }
        MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
        for (MapleFoothold fh : allFootholds) {
            fTree.insert(fh);
        }
        map.setFootholds(fTree);
        if (mapData.getChildByPath("area") != null) {
            for (MapleData area : mapData.getChildByPath("area")) {
                int x1 = MapleDataTool.getInt(area.getChildByPath("x1"));
                int y1 = MapleDataTool.getInt(area.getChildByPath("y1"));
                int x2 = MapleDataTool.getInt(area.getChildByPath("x2"));
                int y2 = MapleDataTool.getInt(area.getChildByPath("y2"));
                map.addMapleArea(new Rectangle(x1, y1, (x2 - x1), (y2 - y1)));
            }
        }
        if (mapData.getChildByPath("seat") != null) {
            int seats = mapData.getChildByPath("seat").getChildren().size();
            map.setSeats(seats);
        }
        if (event == null) {
            try {
                Connection con = DatabaseConnection.getConnection();
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE map = ? AND world = ?")) {
                    ps.setInt(1, mapid);
                    ps.setInt(2, world);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            map.addPlayerNPCMapObject(new MaplePlayerNPC(rs));
                        }
                    }
                }
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            List<MaplePlayerNPC> dnpcs = MaplePlayerNPCFactory.getDeveloperNpcsFromMapid(mapid);
            if (dnpcs != null) {
                for (MaplePlayerNPC dnpc : dnpcs) {
                    map.addPlayerNPCMapObject(dnpc);
                }
            }
        }

        loadLifeFromWz(map, mapData);
        loadLifeFromDb(map);

        if (map.isCPQMap()) {
            MapleData mcData = mapData.getChildByPath("monsterCarnival");
            if (mcData != null) {
                map.setDeathCP(MapleDataTool.getIntConvert("deathCP", mcData, 0));
                map.setMaxMobs(MapleDataTool.getIntConvert("mobGenMax", mcData, 20));    // thanks Atoot for noticing CPQ1 bf. 3 and 4 not accepting spawns due to undefined limits, Lame for noticing a need to cap mob spawns even on such undefined limits
                map.setTimeDefault(MapleDataTool.getIntConvert("timeDefault", mcData, 0));
                map.setTimeExpand(MapleDataTool.getIntConvert("timeExpand", mcData, 0));
                map.setMaxReactors(MapleDataTool.getIntConvert("guardianGenMax", mcData, 16));
                MapleData guardianGenData = mcData.getChildByPath("guardianGenPos");
                for (MapleData node : guardianGenData.getChildren()) {
                    GuardianSpawnPoint pt = new GuardianSpawnPoint(new Point(MapleDataTool.getIntConvert("x", node), MapleDataTool.getIntConvert("y", node)));
                    pt.setTeam(MapleDataTool.getIntConvert("team", node, -1));
                    pt.setTaken(false);
                    map.addGuardianSpawnPoint(pt);
                }
                if (mcData.getChildByPath("skill") != null) {
                    for (MapleData area : mcData.getChildByPath("skill")) {
                        map.addSkillId(MapleDataTool.getInt(area));
                    }
                }

                if (mcData.getChildByPath("mob") != null) {
                    for (MapleData area : mcData.getChildByPath("mob")) {
                        map.addMobSpawn(MapleDataTool.getInt(area.getChildByPath("id")), MapleDataTool.getInt(area.getChildByPath("spendCP")));
                    }
                }
            }

        }

        if (mapData.getChildByPath("reactor") != null) {
            for (MapleData reactor : mapData.getChildByPath("reactor")) {
                String id = MapleDataTool.getString(reactor.getChildByPath("id"));
                if (id != null) {
                    MapleReactor newReactor = loadReactor(reactor, id, (byte) MapleDataTool.getInt(reactor.getChildByPath("f"), 0));
                    map.spawnReactor(newReactor);
                }
            }
        }
        
        map.setMapName(loadPlaceName(mapid));
        map.setStreetName(loadStreetName(mapid));
        
        map.setClock(mapData.getChildByPath("clock") != null);
        map.setEverlast(MapleDataTool.getIntConvert("everlast", infoData, 0) != 0); // thanks davidlafriniere for noticing value 0 accounting as true
        map.setTown(MapleDataTool.getIntConvert("town", infoData, 0) != 0);
        map.setHPDec(MapleDataTool.getIntConvert("decHP", infoData, 0));
        map.setHPDecProtect(MapleDataTool.getIntConvert("protectItem", infoData, 0));
        map.setForcedReturnMap(MapleDataTool.getInt(infoData.getChildByPath("forcedReturn"), 999999999));
        map.setBoat(mapData.getChildByPath("shipObj") != null);
        map.setTimeLimit(MapleDataTool.getIntConvert("timeLimit", infoData, -1));
        map.setFieldType(MapleDataTool.getIntConvert("fieldType", infoData, 0));
        map.setMobCapacity(MapleDataTool.getIntConvert("fixedMobCapacity", infoData, 500));//Is there a map that contains more than 500 mobs?
        
        MapleData recData = infoData.getChildByPath("recovery");
        if (recData != null) {
            map.setRecovery(MapleDataTool.getFloat(recData));
        }

        HashMap<Integer, Integer> backTypes = new HashMap<>();
        try {
            for (MapleData layer : mapData.getChildByPath("back")) { // yolo
                int layerNum = Integer.parseInt(layer.getName());
                int btype = MapleDataTool.getInt(layer.getChildByPath("type"), 0);

                backTypes.put(layerNum, btype);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // swallow cause I'm cool
        }

        map.setBackgroundTypes(backTypes);
        map.generateMapDropRangeCache();

        return map;
    }
    
    private static AbstractLoadedMapleLife loadLife(int id, String type, int cy, int f, int fh, int rx0, int rx1, int x, int y, int hide) {
        AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(id, type);
        myLife.setCy(cy);
        myLife.setF(f);
        myLife.setFh(fh);
        myLife.setRx0(rx0);
        myLife.setRx1(rx1);
        myLife.setPosition(new Point(x, y));
        if (hide == 1) {
            myLife.setHide(true);
        }
        return myLife;
    }

    private static MapleReactor loadReactor(MapleData reactor, String id, final byte FacingDirection) {
        MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(Integer.parseInt(id)), Integer.parseInt(id));
        int x = MapleDataTool.getInt(reactor.getChildByPath("x"));
        int y = MapleDataTool.getInt(reactor.getChildByPath("y"));
        myReactor.setFacingDirection(FacingDirection);
        myReactor.setPosition(new Point(x, y));
        myReactor.setDelay(MapleDataTool.getInt(reactor.getChildByPath("reactorTime")) * 1000);
        myReactor.setName(MapleDataTool.getString(reactor.getChildByPath("name"), ""));
        myReactor.resetReactorActions(0);
        return myReactor;
    }

    private static String getMapName(int mapid) {
        String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
        StringBuilder builder = new StringBuilder("Map/Map");
        int area = mapid / 100000000;
        builder.append(area);
        builder.append("/");
        builder.append(mapName);
        builder.append(".img");
        mapName = builder.toString();
        return mapName;
    }

    private static String getMapStringName(int mapid) {
        StringBuilder builder = new StringBuilder();
        if (mapid < 100000000) {
            builder.append("maple");
        } else if (mapid >= 100000000 && mapid < 200000000) {
            builder.append("victoria");
        } else if (mapid >= 200000000 && mapid < 300000000) {
            builder.append("ossyria");
        } else if (mapid >= 300000000 && mapid < 400000000) {
            builder.append("elin");
        } else if (mapid >= 540000000 && mapid < 560000000) {
            builder.append("singapore");
        } else if (mapid >= 600000000 && mapid < 620000000) {
            builder.append("MasteriaGL");
        } else if (mapid >= 677000000 && mapid < 677100000) {
            builder.append("Episode1GL");
        } else if (mapid >= 670000000 && mapid < 682000000) {
            if ((mapid >= 674030000 && mapid < 674040000) || (mapid >= 680100000 && mapid < 680200000)) {
                builder.append("etc");
            } else {
                builder.append("weddingGL");
            }
        } else if (mapid >= 682000000 && mapid < 683000000) {
            builder.append("HalloweenGL");
        } else if (mapid >= 683000000 && mapid < 684000000) {
            builder.append("event");
        } else if (mapid >= 800000000 && mapid < 900000000) {
            if ((mapid >= 889100000 && mapid < 889200000)) {
                builder.append("etc");
            } else {
                builder.append("jp");
            }
        } else {
            builder.append("etc");
        }
        builder.append("/").append(mapid);
        return builder.toString();
    }
    
    public static String loadPlaceName(int mapid) {
        try {
            return MapleDataTool.getString("mapName", nameData.getChildByPath(getMapStringName(mapid)), "");
        } catch (Exception e) {
            return "";
        }
    }
    
    public static String loadStreetName(int mapid) {
        try {
            return MapleDataTool.getString("streetName", nameData.getChildByPath(getMapStringName(mapid)), "");
        } catch (Exception e) {
            return "";
        }
    }
    
}
