/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
BossRushPQ - Next Stage
@author Ronan
*/

function enter(pi) {
    if(pi.getMap().getMonsters().isEmpty()) {
        var nextStage;
        
        if(pi.getMapId() % 500 >= 100) nextStage = pi.getMapId() + 100;
        else nextStage = 970030001 + (Math.floor((pi.getMapId() - 970030100) / 500));
        
        pi.playPortalSound(); pi.warp(nextStage);
        return true;
    }
    else {
        pi.getPlayer().dropMessage(6, "Defeat all monsters before proceeding to the next stage.");
        return false;
    }
}