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

import constants.game.GameConstants;

public enum MapleDisease {
    NULL(0x0),
    SLOW(0x1, 126),
    SEDUCE(0x80, 128),
    FISHABLE(0x100),
    ZOMBIFY(0x4000),
    CONFUSE(0x80000, 132),
    STUN(0x2000000000000L, 123),
    POISON(0x4000000000000L, 125),
    SEAL(0x8000000000000L, 120),
    DARKNESS(0x10000000000000L, 121),
    WEAKEN(0x4000000000000000L, 122),
    CURSE(0x8000000000000000L, 124);
    
    private long i;
    private boolean first;
    private int mobskill;
    
    private MapleDisease(long i) {
        this(i, false, 0);
    }

    private MapleDisease(long i, int skill) {
        this(i, false, skill);
    }
    
    private MapleDisease(long i, boolean first, int skill) {
        this.i = i;
        this.first = first;
        this.mobskill = skill;
    }
    
    public long getValue() {
        return i;
    }

    public boolean isFirst() {
        return first;
    }
    
    public int getDisease() {
        return mobskill;
    }
    
    public static MapleDisease ordinal(int ord) {
        try {
            return MapleDisease.values()[ord];
        } catch (IndexOutOfBoundsException io) {
            return NULL;
        }
    }
    
    public static final MapleDisease getRandom() {
        MapleDisease[] diseases = GameConstants.CPQ_DISEASES;
        return diseases[(int) (Math.random() * diseases.length)];
    }
    
    public static final MapleDisease getBySkill(final int skill) {
        for (MapleDisease d : MapleDisease.values()) {
            if (d.getDisease() == skill && d.getDisease() != 0) {
                return d;
            }
        }
        return null;
    }
    
}