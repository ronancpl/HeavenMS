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
import tools.Pair;

import server.MaplePortal;
import client.MapleCharacter;
import constants.ServerConstants;

/**
 *
 * @author Matze
 * @author Ronan
 */
public class MapleDoor {
    private int ownerId;
    private MapleMap town;
    private MaplePortal townPortal;
    private MapleMap target;
    private Pair<String, Integer> posStatus = null;
    
    private MapleDoorObject townDoor;
    private MapleDoorObject areaDoor;
    
    public MapleDoor(MapleCharacter owner, Point targetPosition) {
        this.ownerId = owner.getId();
        this.target = owner.getMap();
        
        if(target.canDeployDoor(targetPosition)) {
            if(ServerConstants.USE_ENFORCE_MDOOR_POSITION) {
                posStatus = target.getDoorPositionStatus(targetPosition);
            }
            
            if(posStatus == null) {
                this.town = this.target.getReturnMap();
                this.townPortal = getTownDoorPortal(owner.getDoorSlot());

                if(townPortal != null) {
                    this.areaDoor = new MapleDoorObject(ownerId, town, target, townPortal.getId(), targetPosition, townPortal.getPosition());
                    this.townDoor = new MapleDoorObject(ownerId, target, town, -1, townPortal.getPosition(), targetPosition);

                    this.areaDoor.setPairOid(this.townDoor.getObjectId());
                    this.townDoor.setPairOid(this.areaDoor.getObjectId());
                } else {
                    this.ownerId = -1;
                }
            } else {
                this.ownerId = -3;
            }
        } else {
            this.ownerId = -2;
        }
    }
    
    public void updateDoorPortal(MapleCharacter owner) {
        int slot = owner.fetchDoorSlot();
        
        MaplePortal nextTownPortal = getTownDoorPortal(slot);
        if(nextTownPortal != null) {
            townPortal = nextTownPortal;
            areaDoor.update(nextTownPortal.getId(), nextTownPortal.getPosition());
        }
    }
    
    private MaplePortal getTownDoorPortal(int doorid) {
        return town.getDoorPortal(doorid);
    }
    
    public int getOwnerId() {
        return ownerId;
    }

    public MapleDoorObject getTownDoor() {
        return townDoor;
    }
    
    public MapleDoorObject getAreaDoor() {
        return areaDoor;
    }
    
    public MapleMap getTown() {
        return town;
    }

    public MaplePortal getTownPortal() {
        return townPortal;
    }

    public MapleMap getTarget() {
        return target;
    }

    public Pair<String, Integer> getDoorStatus() {
        return posStatus;
    }
}
