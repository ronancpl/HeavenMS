/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
package server.events.gm;

/**
 *
 * @author kevintjuh93
 */
public class MapleCoconuts {
	
    private int id;
    private int hits = 0;
    private boolean hittable = false;
    private long hittime = System.currentTimeMillis();

    public MapleCoconuts(int id) {
        this.id = id;
    }

    public void hit() {
        this.hittime = System.currentTimeMillis() + 750;
        hits++;
    }

    public int getHits() {
        return hits;
    }

    public void resetHits() {
        hits = 0;
    }

    public boolean isHittable() {
        return hittable;
    }

    public void setHittable(boolean hittable) {
        this.hittable = hittable;
    }

    public long getHitTime() {
        return hittime;
    }
}
