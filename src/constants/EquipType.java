/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
package constants;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author RonanLana
 */
public enum EquipType {
    UNDEFINED(-1),
    ACCESSORY(0),
    CAP(100),
    CAPE(110),
    COAT(104),
    FACE(2),
    GLOVES(108),
    HAIR(3),
    LONGCOAT(105),
    PANTS(106),
    PET_EQUIP(180),
    PET_EQUIP_FIELD(181),
    PET_EQUIP_LABEL(182),
    PET_EQUIP_QUOTE(183),
    RING(111),
    SHIELD(109),
    SHOES(107),
    TAMING(190),
    TAMING_SADDLE(191),
    SWORD(1302),
    AXE(1312),
    MACE(1322),
    DAGGER(1332),
    WAND(1372),
    STAFF(1382),
    SWORD_2H(1402),
    AXE_2H(1412),
    MACE_2H(1422),
    SPEAR(1432),
    POLEARM(1442),
    BOW(1452),
    CROSSBOW(1462),
    CLAW(1472),
    KNUCKLER(1482),
    PISTOL(1492);

    private final int i;
    private static final Map<Integer, EquipType> map = new HashMap(34);

    private EquipType(int val) {
        this.i = val;
    }

    public int getValue() {
        return i;
    }
    
    static {
        for (EquipType eqEnum : EquipType.values()) {
            map.put(eqEnum.i, eqEnum);
        }
    }
    
    public static EquipType getEquipTypeById(int itemid) {
        EquipType ret;
        int val = itemid / 100000;
        
        if(val == 13 || val == 14) {
            ret = map.get(itemid / 1000);
        } else {
            ret = map.get(itemid / 10000);
        }
        
        return (ret != null) ? ret : EquipType.UNDEFINED;
    }
}