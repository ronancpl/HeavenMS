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
package net.server.channel.worker;

import java.util.Collections;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;

/**
 *
 * @author Ronan
 */
public class FaceExpressionScheduler extends BaseScheduler {
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
