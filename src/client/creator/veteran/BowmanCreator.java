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
package client.creator.veteran;

import client.MapleClient;
import client.MapleJob;
import client.creator.CharacterFactory;
import client.creator.CharacterFactoryRecipe;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import server.MapleItemInformationProvider;

/**
 *
 * @author RonanLana
 */
public class BowmanCreator extends CharacterFactory {
        private static int[] equips = {1040067, 1041054, 1060056, 1061050, 1072081};
        private static int[] weapons = {1452005, 1462000};
        private static int[] startingHpMp = {797, 404};
        
        private static CharacterFactoryRecipe createRecipe(MapleJob job, int level, int map, int top, int bottom, int shoes, int weapon) {
                CharacterFactoryRecipe recipe = new CharacterFactoryRecipe(job, level, map, top, bottom, shoes, weapon);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                
                recipe.setDex(25);
                recipe.setRemainingAp(133);
                recipe.setRemainingSp(61);

                recipe.setMaxHp(startingHpMp[0]);
                recipe.setMaxMp(startingHpMp[1]);
                
                recipe.setMeso(100000);

                for(int i = 1; i < weapons.length; i++) {
                        giveEquipment(recipe, ii, weapons[i]);
                }

                giveItem(recipe, 2000002, 100, MapleInventoryType.USE);
                giveItem(recipe, 2000003, 100, MapleInventoryType.USE);
                giveItem(recipe, 3010000, 1, MapleInventoryType.SETUP);
                
                return recipe;
        }
    
        private static void giveEquipment(CharacterFactoryRecipe recipe, MapleItemInformationProvider ii, int equipid) {
                Item nEquip = ii.getEquipById(equipid);
                recipe.addStartingEquipment(nEquip);
        }
        
        private static void giveItem(CharacterFactoryRecipe recipe, int itemid, int quantity, MapleInventoryType itemType) {
                recipe.addStartingItem(itemid, quantity, itemType);
        }
    
        public static int createCharacter(MapleClient c, String name, int face, int hair, int skin, int gender, int improveSp) {
                return createNewCharacter(c, name, face, hair, skin, gender, createRecipe(MapleJob.BOWMAN, 30, 100000000, equips[gender], equips[2 + gender], equips[4], weapons[0]));
        }
}
