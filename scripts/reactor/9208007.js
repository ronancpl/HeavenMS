/* 
 * This file is part of the OdinMS Maple Story Server
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
Stage 2: Spear destinations - Guild Quest

@Author Lerk
@Author Ronan
*/

function act() {
        var react = rm.getPlayer().getEventInstance().getMapInstance(990000400).getReactorByName("speargate");
        react.forceHitReactor(react.getState() + 1);
        
        if(react.getState() == 4) {
                var eim = rm.getPlayer().getEventInstance();

                var maps = [990000400, 990000410, 990000420, 990000430, 990000431, 990000440];
                for(var i = 0; i < maps.length; i++) {
                        eim.showClearEffect(false, maps[i]);
                }
                
                rm.getGuild().gainGP(20);
        }
}