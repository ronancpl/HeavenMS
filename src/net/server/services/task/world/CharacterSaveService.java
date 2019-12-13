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
package net.server.services.task.world;

import net.server.audit.locks.MonitoredLockType;
import net.server.services.BaseScheduler;
import net.server.services.BaseService;

/**
 *
 * @author Ronan
 */
public class CharacterSaveService extends BaseService {
    
    CharacterSaveScheduler chrSaveScheduler = new CharacterSaveScheduler();
    
    @Override
    public void dispose() {
        if(chrSaveScheduler != null) {
            chrSaveScheduler.dispose();
            chrSaveScheduler = null;
        }
    }
    
    public void registerSaveCharacter(int characterId, Runnable runAction) {
        chrSaveScheduler.registerSaveCharacter(characterId, runAction);
    }
    
    private class CharacterSaveScheduler extends BaseScheduler {
        
        public CharacterSaveScheduler() {
            super(MonitoredLockType.WORLD_SAVECHARS);
        }

        public void registerSaveCharacter(Integer characterId, Runnable runAction) {
            registerEntry(characterId, runAction, 0);
        }

        public void unregisterSaveCharacter(Integer characterId) {
            interruptEntry(characterId);
        }
    
    }
    
}
