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
package net.server.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Ronan
 */
public class LockCollector {
    
    private static final LockCollector instance = new LockCollector();
    
    public static LockCollector getInstance() {
        return instance;
    }
    
    private Map<Runnable, Integer> disposableLocks = new HashMap<>(200);
    private final Lock lock = new ReentrantLock(true);
    
    public void registerDisposeAction(Runnable r) {
        lock.lock();
        try {
            disposableLocks.put(r, 0);
        } finally {
            lock.unlock();
        }
    }
    
    public void runLockCollector() {
        List<Runnable> toDispose = new ArrayList<>();
        
        lock.lock();
        try {
            for(Entry<Runnable, Integer> e : disposableLocks.entrySet()) {
                Integer eVal = e.getValue();
                if(eVal > 5) {  // updates each 2min
                    toDispose.add(e.getKey());
                } else {
                    disposableLocks.put(e.getKey(), ++eVal);
                }
            }
        } finally {
            lock.unlock();
        }
        
        for(Runnable r : toDispose) {
            r.run();
        }
    }
}
