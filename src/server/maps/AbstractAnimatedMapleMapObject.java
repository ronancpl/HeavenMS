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
package server.maps;

import java.awt.Point;
import java.util.Collections;
import java.util.List;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovementFragment;

public abstract class AbstractAnimatedMapleMapObject extends AbstractMapleMapObject implements AnimatedMapleMapObject {
    private int stance;

    @Override
    public int getStance() {
        return stance;
    }

    @Override
    public void setStance(int stance) {
        this.stance = stance;
    }

    @Override
    public boolean isFacingLeft() {
        return Math.abs(stance) % 2 == 1;
    }
    
    public List<LifeMovementFragment> getIdleMovement() {
        AbsoluteLifeMovement alm = new AbsoluteLifeMovement(0, getPosition(), 0, getStance());
        alm.setPixelsPerSecond(new Point(0, 0));
        
        List<LifeMovementFragment> moveUpdate = Collections.singletonList((LifeMovementFragment) alm);
        return moveUpdate;
    }
}
