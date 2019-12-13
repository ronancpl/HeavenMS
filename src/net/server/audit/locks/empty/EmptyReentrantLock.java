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
package net.server.audit.locks.empty;

import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import tools.FilePrinter;

/**
 *
 * @author RonanLana
 */
public class EmptyReentrantLock extends AbstractEmptyLock implements MonitoredReentrantLock {
    private final MonitoredLockType id;
    
    public EmptyReentrantLock(MonitoredLockType type) {
        this.id = type;
    }
    
    @Override
    public void lock() {
        FilePrinter.printError(FilePrinter.DISPOSED_LOCKS, "Captured locking tentative on disposed lock " + id + ":" + printThreadStack(Thread.currentThread().getStackTrace()));
    }
    
    @Override
    public void unlock() {}
    
    @Override
    public boolean tryLock() {
        FilePrinter.printError(FilePrinter.DISPOSED_LOCKS, "Captured try-locking tentative on disposed lock " + id + ":" + printThreadStack(Thread.currentThread().getStackTrace()));
        return false;
    }
    
    @Override
    public MonitoredReentrantLock dispose() {
        return this;
    }
}
