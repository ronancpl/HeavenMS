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

public enum MapleBuffStat {
    //SLOW(0x1L),
    MORPH(0x2L),
    RECOVERY(0x4L),
    MAPLE_WARRIOR(0x8L),
    STANCE(0x10L),
    SHARP_EYES(0x20L),
    MANA_REFLECTION(0x40L),
    //ALWAYS_RIGHT(0X80L),
    
    //------ bgn EDITED SLOT (was unused before) --------
    MAP_PROTECTION(0x100000000000000L),
    //------ end EDITED SLOT ----------------------------
    
    SHADOW_CLAW(0x100L),
    INFINITY(0x200L),
    HOLY_SHIELD(0x400L),
    HAMSTRING(0x800L),
    BLIND(0x1000L),
    CONCENTRATE(0x2000L),
    HPREC(0x4000L),
    ECHO_OF_HERO(0x8000L),
    MPREC(0x10000L),
    GHOST_MORPH(0x20000L),
    AURA(0x40000L),
    CONFUSE(0x80000L),
    
    // ------ COUPON feature ------
    
    COUPON_EXP1(0x100000L),
    COUPON_EXP2(0x200000L),
    COUPON_EXP3(0x400000L),
    COUPON_EXP4(0x800000L),
    COUPON_DRP1(0x1000000L),
    COUPON_DRP2(0x2000000L),
    COUPON_DRP3(0x4000000L),
    
    // ---- end COUPON feature ----
    
    BERSERK_FURY(0x8000000L),
    DIVINE_BODY(0x10000000L),
    SPARK(0x20000000L),
    MAP_CHAIR(0x40000000L),
    FINALATTACK(0x80000000L),
    WATK(0x100000000L),
    WDEF(0x200000000L),
    MATK(0x400000000L),
    MDEF(0x800000000L),
    ACC(0x1000000000L),
    AVOID(0x2000000000L),
    HANDS(0x4000000000L),
    SHOWDASH(0x4000000000L),
    SPEED(0x8000000000L),
    JUMP(0x10000000000L),
    MAGIC_GUARD(0x20000000000L),
    DARKSIGHT(0x40000000000L),
    BOOSTER(0x80000000000L),
    POWERGUARD(0x100000000000L),
    HYPERBODYHP(0x200000000000L),
    HYPERBODYMP(0x400000000000L),
    INVINCIBLE(0x800000000000L),
    SOULARROW(0x1000000000000L),
    STUN(0x2000000000000L),
    POISON(0x4000000000000L),
    SEAL(0x8000000000000L),
    DARKNESS(0x10000000000000L),
    COMBO(0x20000000000000L),
    SUMMON(0x20000000000000L),
    WK_CHARGE(0x40000000000000L),
    DRAGONBLOOD(0x80000000000000L),
    HOLY_SYMBOL(0x100000000000000L),
    MESOUP(0x200000000000000L),
    SHADOWPARTNER(0x400000000000000L),
    PICKPOCKET(0x800000000000000L),
    PUPPET(0x800000000000000L),
    MESOGUARD(0x1000000000000000L),
    //0x2000000000000000L
    WEAKEN(0x4000000000000000L),
    //THAT GAP
    
    //all incorrect buffstats
    SLOW(0x200000000L, true), 
    ELEMENTAL_RESET(0x200000000L, true), 
    MAGIC_SHIELD(0x400000000L, true), 
    MAGIC_RESISTANCE(0x800000000L, true), 
    // needs Soul Stone
    //end incorrect buffstats
    
    WIND_WALK(0x400000000L, true),
    ARAN_COMBO(0x1000000000L, true),
    COMBO_DRAIN(0x2000000000L, true),
    COMBO_BARRIER(0x4000000000L, true),
    BODY_PRESSURE(0x8000000000L, true),
    SMART_KNOCKBACK(0x10000000000L, true),
    BERSERK(0x20000000000L, true),
    ENERGY_CHARGE(0x4000000000000L, true),
    DASH2(0x8000000000000L, true), // correct (speed)
    DASH(0x10000000000000L, true), // correct (jump)
    MONSTER_RIDING(0x20000000000000L, true),    
    SPEED_INFUSION(0x40000000000000L, true),
    HOMING_BEACON(0x80000000000000L, true);

    private final long i;
    private final boolean isFirst;

    private MapleBuffStat(long i, boolean isFirst) {
        this.i = i;
        this.isFirst = isFirst;
    }

    private MapleBuffStat(long i) {
        this.i = i;
        this.isFirst = false;
    }

    public long getValue() {
        return i;
    }

    public boolean isFirst() {
        return isFirst;
    }
}
