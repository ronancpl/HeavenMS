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
package server.maps;

import constants.ServerConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import scripting.event.EventInstanceManager;
import server.TimerManager;

public class MapleMapManager {

    private int channel, world;
    private EventInstanceManager event;
    
    private Map<Integer, MapleMap> maps = new HashMap<>();
    
    private ScheduledFuture<?> updateTask;
    
    private ReadLock mapsRLock;
    private WriteLock mapsWLock;

    public MapleMapManager(EventInstanceManager eim, int world, int channel) {
        this.world = world;
        this.channel = channel;
        this.event = eim;

        ReentrantReadWriteLock rrwl = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_MANAGER);
        this.mapsRLock = rrwl.readLock();
        this.mapsWLock = rrwl.writeLock();
        
        updateTask = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                updateMaps();
            }
        }, ServerConstants.RESPAWN_INTERVAL);
    }

    public MapleMap resetMap(int mapid) {
        mapsWLock.lock();
        try {
            maps.remove(mapid);
        } finally {
            mapsWLock.unlock();
        }

        return getMap(mapid);
    }

    private synchronized MapleMap loadMapFromWz(int mapid, boolean cache) {
        MapleMap map;

        if (cache) {
            mapsRLock.lock();
            try {
                map = maps.get(mapid);
            } finally {
                mapsRLock.unlock();
            }

            if (map != null) {
                return map;
            }
        }

        map = MapleMapFactory.loadMapFromWz(mapid, world, channel, event);

        if (cache) {
            mapsWLock.lock();
            try {
                maps.put(mapid, map);
            } finally {
                mapsWLock.unlock();
            }
        }

        return map;
    }

    public MapleMap getMap(int mapid) {
        MapleMap map;

        mapsRLock.lock();
        try {
            map = maps.get(mapid);
        } finally {
            mapsRLock.unlock();
        }

        return (map != null) ? map : loadMapFromWz(mapid, true);
    }
    
    public MapleMap getDisposableMap(int mapid) {
        return loadMapFromWz(mapid, false);
    }

    public boolean isMapLoaded(int mapId) {
        mapsRLock.lock();
        try {
            return maps.containsKey(mapId);
        } finally {
            mapsRLock.unlock();
        }
    }

    public Map<Integer, MapleMap> getMaps() {
        mapsRLock.lock();
        try {
            return new HashMap<>(maps);
        } finally {
            mapsRLock.unlock();
        }
    }
    
    private void updateMaps() {
        for (MapleMap map : getMaps().values()) {
            map.respawn();
            map.mobMpRecovery();
        }
    }
    
    public void dispose() {
        if (updateTask != null) {
            updateTask.cancel(false);
            updateTask = null;
        }
        
        for (MapleMap map : getMaps().values()) {
            map.dispose();
        }

        this.event = null;
    }

    public static float getMapRecoveryRate(int mapid) {
        return MapleMapFactory.getMapRecoveryRate(mapid);
    }
}
