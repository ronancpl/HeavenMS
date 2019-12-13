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
import client.MapleCharacter;
import config.YamlConfig;
import java.util.Collections;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.server.services.BaseScheduler;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 *
 * @author Ronan
 */
public class FaceExpressionService extends BaseService {
    
    private FaceExpressionScheduler faceExpressionSchedulers[] = new FaceExpressionScheduler[YamlConfig.config.server.CHANNEL_LOCKS];
    private MonitoredReentrantLock faceLock[] = new MonitoredReentrantLock[YamlConfig.config.server.CHANNEL_LOCKS];
    
    public FaceExpressionService() {
        for(int i = 0; i < YamlConfig.config.server.CHANNEL_LOCKS; i++) {
            faceLock[i] = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHANNEL_FACEEXPRS, true);
            faceExpressionSchedulers[i] = new FaceExpressionScheduler(faceLock[i]);
        }
    }
    
    private void emptyLocks() {
        for(int i = 0; i < YamlConfig.config.server.CHANNEL_LOCKS; i++) {
            faceLock[i] = faceLock[i].dispose();
        }
    }
    
    private void disposeLocks() {
        LockCollector.getInstance().registerDisposeAction(new Runnable() {
            @Override
            public void run() {
                emptyLocks();
            }
        });
    }
    
    @Override
    public void dispose() {
        for(int i = 0; i < YamlConfig.config.server.CHANNEL_LOCKS; i++) {
            if(faceExpressionSchedulers[i] != null) {
                faceExpressionSchedulers[i].dispose();
                faceExpressionSchedulers[i] = null;
            }
        }
        
        disposeLocks();
    }
    
    public void registerFaceExpression(final MapleMap map, final MapleCharacter chr, int emote) {
        int lockid = getChannelSchedulerIndex(map.getId());
        
        Runnable cancelAction = new Runnable() {
            @Override
            public void run() {
                if(chr.isLoggedinWorld()) {
                    map.broadcastMessage(chr, MaplePacketCreator.facialExpression(chr, 0), false);
                }
            }
        };
        
        faceLock[lockid].lock();
        try {
            if(!chr.isLoggedinWorld()) {
                return;
            }
            
            faceExpressionSchedulers[lockid].registerFaceExpression(chr.getId(), cancelAction);
        } finally {
            faceLock[lockid].unlock();
        }
        
        map.broadcastMessage(chr, MaplePacketCreator.facialExpression(chr, emote), false);
    }
    
    public void unregisterFaceExpression(int mapid, MapleCharacter chr) {
        int lockid = getChannelSchedulerIndex(mapid);
        
        faceLock[lockid].lock();
        try {
            faceExpressionSchedulers[lockid].unregisterFaceExpression(chr.getId());
        } finally {
            faceLock[lockid].unlock();
        }
    }
    
    private class FaceExpressionScheduler extends BaseScheduler {
        
        public FaceExpressionScheduler(final MonitoredReentrantLock channelFaceLock) {
            super(MonitoredLockType.CHANNEL_FACESCHDL, Collections.singletonList(channelFaceLock));
        }

        public void registerFaceExpression(Integer characterId, Runnable runAction) {
            registerEntry(characterId, runAction, 5000);
        }

        public void unregisterFaceExpression(Integer characterId) {
            interruptEntry(characterId);
        }
    
    }
    
}
