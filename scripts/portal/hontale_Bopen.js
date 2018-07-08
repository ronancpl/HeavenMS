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
/* The five caves
 * @author Jvlaple
 */
function enter(pi) {
    var nextMap;
    var eim;
    var target;
    var targetPortal;
    var avail;
    
    if (pi.getPlayer().getMapId() == 240050101) {
        nextMap = 240050102;
        eim = pi.getPlayer().getEventInstance();
        target = eim.getMapInstance(nextMap);
        targetPortal = target.getPortal("sp");
        // only let people through if the eim is ready
        avail = eim.getProperty("1stageclear");
        if (avail == null) {
            // do nothing; send message to player
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
            return false;
        } else {
            pi.playPortalSound();
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    else if (pi.getPlayer().getMapId() == 240050102) {
        nextMap = 240050103;
        eim = pi.getPlayer().getEventInstance();
        target = eim.getMapInstance(nextMap);
        targetPortal = target.getPortal("sp");
        // only let people through if the eim is ready
        avail = eim.getProperty("2stageclear");
        if (avail == null) {
            // do nothing; send message to player
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
            return false;
        } else {
            pi.playPortalSound();
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    else if (pi.getPlayer().getMapId() == 240050103) {
        nextMap = 240050104;
        eim = pi.getPlayer().getEventInstance();
        target = eim.getMapInstance(nextMap);
        targetPortal = target.getPortal("sp");
        // only let people through if the eim is ready
        avail = eim.getProperty("3stageclear");
        if (avail == null) {
            // do nothing; send message to player
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
            return false;
        } else {
            pi.playPortalSound();
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    else if (pi.getPlayer().getMapId() == 240050104) {
        nextMap = 240050105;
        eim = pi.getPlayer().getEventInstance();
        target = eim.getMapInstance(nextMap);
        targetPortal = target.getPortal("sp");
        // only let people through if the eim is ready
        avail = eim.getProperty("4stageclear");
        if (avail == null) {
            // do nothing; send message to player
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
            return false;
        } else {
            pi.playPortalSound();
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    else if (pi.getPlayer().getMapId() == 240050105) {
        nextMap = 240050100;
        eim = pi.getPlayer().getEventInstance();
        target = eim.getMapInstance(nextMap);
        targetPortal = target.getPortal("st00");
        
        avail = eim.getProperty("5stageclear");
        if (avail == null) {
            if (pi.haveItem(4001092) && pi.isEventLeader()) {
                eim.showClearEffect();
                pi.getPlayer().dropMessage(6, "The leader's key break the seal for a flash...");
                pi.playPortalSound();
                pi.getPlayer().changeMap(target, targetPortal);
                eim.setIntProperty("5stageclear", 1);
                return true;
            } else {
                pi.getPlayer().dropMessage(6, "Horntail\'s Seal is blocking this door. Only the leader with the key can lift this seal.");
                return false;
            }
        } else {
            pi.playPortalSound();
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    return true;
}
