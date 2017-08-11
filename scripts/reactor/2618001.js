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
/*2618000.js - MagatiaPQ Beaker
 *@author Ronan
 */
 
function hit() {
	var eim = rm.getEventInstance();
        
        var isAlcadno = eim.getIntProperty("isAlcadno");
        var reactname = (isAlcadno == 0) ? "rnj32_out" : "jnr32_out";
        var reactmap = (isAlcadno == 0) ? 926100202 : 926110202;
        
        eim.getMapInstance(reactmap).getReactorByName(reactname).hitReactor(rm.getClient());
}