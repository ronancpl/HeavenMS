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

public enum MapleSkinColor {
    NORMAL(0), DARK(1), BLACK(2), PALE(3), BLUE(4), GREEN(5), WHITE(9), PINK(10);
    final int id;

    private MapleSkinColor(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MapleSkinColor getById(int id) {
        for (MapleSkinColor l : MapleSkinColor.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }
}
