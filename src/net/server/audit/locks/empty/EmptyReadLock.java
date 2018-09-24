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
package net.server.audit.locks.empty;

import constants.ServerConstants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import tools.FilePrinter;

/**
 *
 * @author RonanLana
 */
public class EmptyReadLock implements MonitoredReadLock {
    private final MonitoredLockType id;
    
    public EmptyReadLock(MonitoredLockType type) {
        this.id = type;
    }
    
    private static String printThreadStack(StackTraceElement[] list) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone(ServerConstants.TIMEZONE));
        String df = dateFormat.format(new Date());
        
        String s = "\r\n" + df + "\r\n";
        for(int i = 0; i < list.length; i++) {
            s += ("    " + list[i].toString() + "\r\n");
        }
        s += "----------------------------\r\n\r\n";
        
        return s;
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
    public MonitoredReadLock dispose() {
        return this;
    }
}
