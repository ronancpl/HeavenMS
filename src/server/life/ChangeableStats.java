/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
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
package server.life;

import constants.GameConstants;

public class ChangeableStats extends OverrideMonsterStats {

    public int watk, matk, wdef, mdef, level;

    public ChangeableStats(MapleMonsterStats stats, OverrideMonsterStats ostats) {
        hp = ostats.getHp();
        exp = ostats.getExp();
        mp = ostats.getMp();
        watk = stats.getPADamage();
        matk = stats.getMADamage();
        wdef = stats.getPDDamage();
        mdef = stats.getMDDamage();
        level = stats.getLevel();
    }

    public ChangeableStats(MapleMonsterStats stats, int newLevel, boolean pqMob) { // here we go i think
        final double mod = (double) newLevel / (double) stats.getLevel();
        final double hpRatio = (double) stats.getHp() / (double) stats.getExp();
        final double pqMod = (pqMob ? 1.5 : 1.0); // god damn
        hp = Math.min((int) Math.round((!stats.isBoss() ? GameConstants.getMonsterHP(newLevel) : (stats.getHp() * mod)) * pqMod), Integer.MAX_VALUE); // right here lol
        exp = Math.min((int) Math.round((!stats.isBoss() ? (GameConstants.getMonsterHP(newLevel) / hpRatio) : (stats.getExp())) * pqMod), Integer.MAX_VALUE);
        mp = Math.min((int) Math.round(stats.getMp() * mod * pqMod), Integer.MAX_VALUE);
        watk = Math.min((int) Math.round(stats.getPADamage() * mod), Integer.MAX_VALUE);
        matk = Math.min((int) Math.round(stats.getMADamage() * mod), Integer.MAX_VALUE);
        wdef = Math.min(Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getPDDamage() * mod)), Integer.MAX_VALUE);
        mdef = Math.min(Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getMDDamage() * mod)), Integer.MAX_VALUE);
        level = newLevel;
    }

    public ChangeableStats(MapleMonsterStats stats, float statModifier, boolean pqMob) {
        this(stats, (int)(statModifier * stats.getLevel()), pqMob);
    }
}
