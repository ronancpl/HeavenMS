/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
package net.server.services.task.channel;

import net.server.services.BaseService;
import config.YamlConfig;
import net.server.audit.locks.MonitoredLockType;
import net.server.services.BaseScheduler;

/**
 *
 * @author Ronan
 */
public class EventService extends BaseService {
    
    private EventScheduler eventSchedulers[] = new EventScheduler[YamlConfig.config.server.CHANNEL_LOCKS];
    
    public EventService() {
        for(int i = 0; i < YamlConfig.config.server.CHANNEL_LOCKS; i++) {
            eventSchedulers[i] = new EventScheduler();
        }
    }
    
    @Override
    public void dispose() {
        for(int i = 0; i < YamlConfig.config.server.CHANNEL_LOCKS; i++) {
            if(eventSchedulers[i] != null) {
                eventSchedulers[i].dispose();
                eventSchedulers[i] = null;
            }
        }
    }
    
    public void registerEventAction(int mapid, Runnable runAction, long delay) {
        eventSchedulers[getChannelSchedulerIndex(mapid)].registerDelayedAction(runAction, delay);
    }
    
    private class EventScheduler extends BaseScheduler {
        
        public EventScheduler() {
            super(MonitoredLockType.CHANNEL_EVENTS);
        }

        public void registerDelayedAction(Runnable runAction, long delay) {
            registerEntry(runAction, runAction, delay);
        }
        
    }
    
}
