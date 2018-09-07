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
package net.server.coordinator;

import constants.ServerConstants;
import scripting.event.EventInstanceManager;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Ronan
 */
public class MapleEventRecallCoordinator {
    
    private final static MapleEventRecallCoordinator instance = new MapleEventRecallCoordinator();
    
    public static MapleEventRecallCoordinator getInstance() {
        return instance;
    }
    
    private ConcurrentHashMap<Integer, EventInstanceManager> eventHistory = new ConcurrentHashMap<>();
    
    private static boolean isRecallableEvent(EventInstanceManager eim) {
        return eim != null && !eim.isEventDisposed() && !eim.isEventCleared();
    }
    
    public EventInstanceManager recallEventInstance(int characterId) {
        EventInstanceManager eim = eventHistory.remove(characterId);
        return isRecallableEvent(eim) ? eim : null;
    }
    
    public void storeEventInstance(int characterId, EventInstanceManager eim) {
        if (ServerConstants.USE_ENABLE_RECALL_EVENT && isRecallableEvent(eim)) {
            eventHistory.put(characterId, eim);
        }
    }
    
    public void manageEventInstances() {
        if (!eventHistory.isEmpty()) {
            List<Integer> toRemove = new LinkedList<>();
            
            for (Entry<Integer, EventInstanceManager> eh : eventHistory.entrySet()) {
                if (!isRecallableEvent(eh.getValue())) {
                    toRemove.add(eh.getKey());
                }
            }

            for (Integer r : toRemove) {
                eventHistory.remove(r);
            }
        }
    }
}
