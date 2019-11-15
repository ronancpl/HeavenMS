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
package tools;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.MonitoredWriteLock;
import net.server.audit.locks.factory.MonitoredReadLockFactory;
import net.server.audit.locks.factory.MonitoredWriteLockFactory;

/**
 *
 * @author Ronan
 */
public class IntervalBuilder {
        
    private List<Line2D> intervalLimits = new ArrayList<>();
    
    protected MonitoredReadLock intervalRlock;
    protected MonitoredWriteLock intervalWlock;
    
    public IntervalBuilder() {
        MonitoredReentrantReadWriteLock locks = new MonitoredReentrantReadWriteLock(MonitoredLockType.INTERVAL, true);
        intervalRlock = MonitoredReadLockFactory.createLock(locks);
        intervalWlock = MonitoredWriteLockFactory.createLock(locks);
    }

    private void refitOverlappedIntervals(int st, int en, int newFrom, int newTo) {
        List<Line2D> checkLimits = new ArrayList<>(intervalLimits.subList(st, en));

        float newLimitX1, newLimitX2;
        if (!checkLimits.isEmpty()) {
            Line2D firstLimit = checkLimits.get(0);
            Line2D lastLimit = checkLimits.get(checkLimits.size() - 1);

            newLimitX1 = (float) ((newFrom < firstLimit.getX1()) ? newFrom : firstLimit.getX1());
            newLimitX2 = (float) ((newTo > lastLimit.getX2()) ? newTo : lastLimit.getX2());

            for (Line2D limit : checkLimits) {
                intervalLimits.remove(st);
            }
        } else {
            newLimitX1 = newFrom;
            newLimitX2 = newTo;
        }

        intervalLimits.add(st, new Line2D.Float((float) newLimitX1, 0, (float) newLimitX2, 0));
    }

    private int bsearchInterval(int point) {
        int st = 0, en = intervalLimits.size() - 1;

        int mid, idx;
        while (en >= st) {
            idx = (st + en) / 2;
            mid = (int) intervalLimits.get(idx).getX1();

            if (mid == point) {
                return idx;
            } else if (mid < point) {
                st = idx + 1;
            } else {
                en = idx - 1;
            }
        }

        return en;
    }

    public void addInterval(int from, int to) {
        intervalWlock.lock();
        try {
            int st = bsearchInterval(from);
            if (st < 0) {
                st = 0;
            } else if (intervalLimits.get(st).getX2() < from) {
                st += 1;
            }

            int en = bsearchInterval(to);
            if (en < st) en = st - 1;

            refitOverlappedIntervals(st, en + 1, from, to);
        } finally {
            intervalWlock.unlock();
        }
    }

    public boolean inInterval(int point) {
        return inInterval(point, point);
    }
    
    public boolean inInterval(int from, int to) {
        intervalRlock.lock();
        try {
            int idx = bsearchInterval(from);
            return idx >= 0 && to <= intervalLimits.get(idx).getX2();
        } finally {
            intervalRlock.unlock();
        }
    }

    public void clear() {
        intervalWlock.lock();
        try {
            intervalLimits.clear();
        } finally {
            intervalWlock.unlock();
        }
    }
    
}
