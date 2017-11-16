/*
 *     This file is part of the MapleSolaxiaV2 Maple Story Server
 *
 * Copyright (C) 2017 RonanLana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools.locks;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author RonanLana
 */
public class MonitoredReentrantReadWriteLock extends ReentrantReadWriteLock {
    public final MonitoredEnums id;
    
    public MonitoredReentrantReadWriteLock(MonitoredEnums id) {
        super();
        this.id = id;
    }
            
    public MonitoredReentrantReadWriteLock(MonitoredEnums id, boolean fair) {
        super(fair);
        this.id = id;
    }
    
    @Override
    public ReadLock readLock() {
        return super.readLock();
    }
    
    @Override
    public WriteLock writeLock() {
        return super.writeLock();
    }
}
