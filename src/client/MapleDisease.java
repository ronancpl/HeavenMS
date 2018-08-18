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
package client;

import java.util.ArrayList;
import java.util.List;

public enum MapleDisease {
    NULL(0x0),
    SLOW(0x1),
    SEDUCE(0x80),
    FISHABLE(0x100),
    ZOMBIFY(0x4000),
    CONFUSE(0x80000),
    STUN(0x2000000000000L),
    POISON(0x4000000000000L),
    SEAL(0x8000000000000L),
    DARKNESS(0x10000000000000L),
    WEAKEN(0x4000000000000000L),
    CURSE(0x8000000000000000L);
    
    private long i;
    private boolean first;
    
    private MapleDisease(long i) {
        this.i = i;
        this.first = false;
    }

    private MapleDisease(long i, boolean first) {
        this.i = i;
        this.first = first;
    }
    
    public long getValue() {
        return i;
    }

    public boolean isFirst() {
            return first;
    }
    
    public static MapleDisease ordinal(int ord) {
        try {
            return MapleDisease.values()[ord];
        } catch (IndexOutOfBoundsException io) {
            return NULL;
        }
    }
}
