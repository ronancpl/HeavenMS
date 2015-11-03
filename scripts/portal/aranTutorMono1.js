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
/*
By kevintjuh93
*/
function enter(pi) { 
	pi.blockPortal();
	if (pi.containsAreaInfo(21002, "mo2=o")) {
	    return false;
	}
	pi.playSound("Aran/balloon");
	pi.updateAreaInfo(21002, "mo1=o;mo2=o");
	pi.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/legendBalloon2");
	return true;
}