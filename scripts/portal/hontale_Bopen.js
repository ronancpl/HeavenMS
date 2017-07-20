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
    if (pi.getPlayer().getMapId() == 240050101) {
        var nextMap = 240050102;
        var eim = pi.getPlayer().getEventInstance()
        var target = eim.getMapInstance(nextMap);
        var targetPortal = target.getPortal("sp");
        // only let people through if the eim is ready
        var avail = eim.getProperty("1stageclear");
        if (avail == null) {
            // do nothing; send message to player
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
            return false;
        }else {
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    else if (pi.getPlayer().getMapId() == 240050102) {
        var nextMap = 240050103;
        var eim = pi.getPlayer().getEventInstance()
        var target = eim.getMapInstance(nextMap);
        var targetPortal = target.getPortal("sp");
        // only let people through if the eim is ready
        var avail = eim.getProperty("2stageclear");
        if (avail == null) {
            // do nothing; send message to player
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
            return false;
        }else {
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    else if (pi.getPlayer().getMapId() == 240050103) {
        var nextMap = 240050104;
        var eim = pi.getPlayer().getEventInstance()
        var target = eim.getMapInstance(nextMap);
        var targetPortal = target.getPortal("sp");
        // only let people through if the eim is ready
        var avail = eim.getProperty("3stageclear");
        if (avail == null) {
            // do nothing; send message to player
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
            return false;
        }else {
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    else if (pi.getPlayer().getMapId() == 240050104) {
        var nextMap = 240050105;
        var eim = pi.getPlayer().getEventInstance()
        var target = eim.getMapInstance(nextMap);
        var targetPortal = target.getPortal("sp");
        // only let people through if the eim is ready
        var avail = eim.getProperty("4stageclear");
        if (avail == null) {
            // do nothing; send message to player
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
            return false;
        }else {
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    else if (pi.getPlayer().getMapId() == 240050105) {
        var nextMap = 240050100;
        var eim = pi.getPlayer().getEventInstance()
        var target = eim.getMapInstance(nextMap);
        var targetPortal = target.getPortal("st00");
        
        var avail = eim.getProperty("5stageclear");
        if (avail == null) {
            if (pi.haveItem(4001092) && pi.isEventLeader()) {
                pi.getEventInstance().showClearEffect();
                pi.getPlayer().dropMessage(6, "The leader's key break the seal for a flash...");
                pi.getPlayer().changeMap(target, targetPortal);
                eim.setIntProperty("5stageclear", 1);
                return true;
            } else {
                pi.getPlayer().dropMessage(6, "Horntail\'s Seal is blocking this door. Only the leader with the key can lift this seal.");
                return false;
            }
        }else {
            pi.getPlayer().changeMap(target, targetPortal);
            return true;
        }
    }
    return true;
}
