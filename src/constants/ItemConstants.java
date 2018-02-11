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
package constants;

import client.inventory.MapleInventoryType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jay Estrella
 * @author Ronan
 */
public final class ItemConstants {
    protected static Map<Integer, MapleInventoryType> inventoryTypeCache = new HashMap<>();
    
    public final static int LOCK = 0x01;
    public final static int SPIKES = 0x02;
    public final static int COLD = 0x04;
    public final static int UNTRADEABLE = 0x08;
    public final static int KARMA = 0x10;
    public final static int PET_COME = 0x80;
    public final static int ACCOUNT_SHARING = 0x100;

    public final static boolean EXPIRING_ITEMS = true;

    public static int getFlagByInt(int type) {
        if (type == 128) {
            return PET_COME;
        } else if (type == 256) {
            return ACCOUNT_SHARING;
        }
        return 0;
    }

    public static boolean isThrowingStar(int itemId) {
        return itemId / 10000 == 207;
    }

    public static boolean isBullet(int itemId) {
        return itemId / 10000 == 233;
    }
    
    public static boolean isPotion(int itemId) {
        return itemId / 1000 == 2000;
    }
    
    public static boolean isFood(int itemId) {
        int useType = itemId / 1000;
        return useType == 2022 || useType == 2010 || useType == 2020;
    }
    
    public static boolean isConsumable(int itemId) {
        return isPotion(itemId) || isFood(itemId);
    }

    public static boolean isRechargable(int itemId) {
        return isThrowingStar(itemId) || isBullet(itemId);
    }

    public static boolean isArrowForCrossBow(int itemId) {
        return itemId / 1000 == 2061;
    }

    public static boolean isArrowForBow(int itemId) {
        return itemId / 1000 == 2060;
    }
    
    public static boolean isArrow(int itemId) {
        return isArrowForBow(itemId) || isArrowForCrossBow(itemId);
    }

    public static boolean isPet(int itemId) {
        return itemId / 1000 == 5000;
    }
    
    public static boolean isExpirablePet(int itemId) {
        return ServerConstants.USE_ERASE_PET_ON_EXPIRATION || itemId == 5000054;
    }
    
    public static boolean isNewYearCardEtc(int itemId) { 
        return itemId / 10000 == 430;
    } 
     
    public static boolean isNewYearCardUse(int itemId) { 
        return itemId / 10000 == 216;
    }
    
    public static boolean isAccessory(int itemId) {
        return itemId >= 1110000 && itemId < 1140000;
    }
    
    public static boolean isTownScroll(int itemId) {
        return itemId >= 2030000 && itemId < 2030021;
    }
    
    public static boolean isAntibanishScroll(int itemId) {
        return itemId == 2030100;
    }
    
    public static boolean isCleanSlate(int scrollId) {
        return scrollId > 2048999 && scrollId < 2049004;
    }
    
    public static boolean isFlagModifier(int scrollId, byte flag) {
        if(scrollId == 2041058 && ((flag & ItemConstants.COLD) == ItemConstants.COLD)) return true;
        if(scrollId == 2040727 && ((flag & ItemConstants.SPIKES) == ItemConstants.SPIKES)) return true;
        return false;
    }
    
    public static boolean isChaosScroll(int scrollId) {
    	return scrollId >= 2049100 && scrollId <= 2049103;
    }
    
    public static boolean isRateCoupon(int itemId) {
        int itemType = itemId / 1000;
        return itemType == 5211 || itemType == 5360;
    }
    
    public static boolean isExpCoupon(int couponId) {
        return couponId / 1000 == 5211;
    }
    
    public static boolean isPartyItem(int itemId) {
        return itemId >= 2022430 && itemId <= 2022433;
    }
    
    public static boolean isPartyAllcure(int itemId) {
        return itemId == 2022433;
    }

    public static MapleInventoryType getInventoryType(final int itemId) {
        if (inventoryTypeCache.containsKey(itemId)) {
            return inventoryTypeCache.get(itemId);
        }
        
        MapleInventoryType ret = MapleInventoryType.UNDEFINED;
        
	final byte type = (byte) (itemId / 1000000);
	if (type >= 1 && type <= 5) {
	    ret = MapleInventoryType.getByType(type);
	}
        
        inventoryTypeCache.put(itemId, ret);
        return ret;
    }
    
    public static boolean isMakerReagent(int itemId) {
        return itemId / 10000 == 425;
    }
    
    public static boolean isOverall(int itemId) {
        return itemId / 10000 == 105;
    }

    public static boolean isWeapon(int itemId) {
        return itemId >= 1302000 && itemId < 1492024;
    }
    
    public static boolean isEquipment(int itemId) {
        return itemId < 2000000;
    }
}
