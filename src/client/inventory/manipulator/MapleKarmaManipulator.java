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
package client.inventory.manipulator;

import constants.ItemConstants;
import client.inventory.Item;

/**
 *
 * @author RonanLana
 */
public class MapleKarmaManipulator {
    private static int getKarmaFlag(Item item) {
        return item.getItemType() == 1 ? ItemConstants.KARMA_EQP : ItemConstants.KARMA_USE;
    }
    
    public static boolean hasKarmaFlag(Item item) {
        int karmaFlag = getKarmaFlag(item);
        return (item.getFlag() & karmaFlag) == karmaFlag;
    }
    
    public static boolean hasUsedKarmaFlag(Item item) {
        int karmaFlag = ItemConstants.KARMA_UNTRADEABLE;
        return (item.getFlag() & karmaFlag) == karmaFlag;
    }
    
    public static void toggleKarmaFlagToUntradeable(Item item) {
        int karmaFlag = getKarmaFlag(item);
        int flag = item.getFlag();
        
        if ((flag & karmaFlag) == karmaFlag) {
            flag ^= karmaFlag;
            flag |= ItemConstants.UNTRADEABLE;
            flag |= ItemConstants.KARMA_UNTRADEABLE;
            
            item.setFlag((byte) flag);
        }
    }
    
    public static void setKarmaFlag(Item item) {
        int karmaFlag = getKarmaFlag(item);
        int flag = item.getFlag();
        
        flag |= karmaFlag;
        item.setFlag((byte) flag);
    }
}
