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
package server.maps;

/**
 *
 * @author AngelSL
 */
public enum FieldLimit {
    JUMP(0x01),
    MOVEMENTSKILLS(0x02),
    SUMMON(0x04),
    DOOR(0x08),
    CANNOTMIGRATE(0x10),    //change channel, town portal scroll, access cash shop, etc etc
    //NO_NOTES(0x20),
    CANNOTVIPROCK(0x40),
    CANNOTMINIGAME(0x80),
    //SPECIFIC_PORTAL_SCROLL_LIMIT(0x100), // APQ and a couple quest maps have this
    CANNOTUSEMOUNTS(0x200),
    //STAT_CHANGE_ITEM_CONSUME_LIMIT(0x400), // Monster carnival?
    //PARTY_BOSS_CHANGE_LIMIT(0x800), // Monster carnival?
    CANNOTUSEPOTION(0x1000),
    //WEDDING_INVITATION_LIMIT(0x2000), // No notes
    //CASH_WEATHER_CONSUME_LIMIT(0x4000),
    //NO_PET(0x8000), // Ariant colosseum-related?
    //ANTI_MACRO_LIMIT(0x10000), // No notes
    CANNOTJUMPDOWN(0x20000);
    //SUMMON_NPC_LIMIT(0x40000); // Seems to .. disable Rush if 0x2 is set
    
    //......... EVEN MORE LIMITS ............
    //SUMMON_NPC_LIMIT(0x40000),
    //NO_EXP_DECREASE(0x80000),
    //NO_DAMAGE_ON_FALLING(0x100000),
    //PARCEL_OPEN_LIMIT(0x200000),
    //DROP_LIMIT(0x400000),
    //ROCKETBOOSTER_LIMIT(0x800000)     //lol we don't even have mechanics <3
    
    private long i;

    private FieldLimit(long i) {
        this.i = i;
    }

    public long getValue() {
        return i;
    }

    public boolean check(int fieldlimit) {
        return (fieldlimit & i) == i;
    }
}
