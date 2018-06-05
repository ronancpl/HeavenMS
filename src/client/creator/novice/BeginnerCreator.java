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
package client.creator.novice;

import client.MapleClient;
import client.MapleJob;
import client.creator.CharacterFactory;
import client.creator.CharacterFactoryRecipe;
import client.inventory.MapleInventoryType;

/**
 *
 * @author RonanLana
 */
public class BeginnerCreator extends CharacterFactory {
        
        private static CharacterFactoryRecipe createRecipe(MapleJob job, int level, int map, int top, int bottom, int shoes, int weapon) {
                CharacterFactoryRecipe recipe = new CharacterFactoryRecipe(job, level, map, top, bottom, shoes, weapon);
                giveItem(recipe, 4161001, 1, MapleInventoryType.ETC);
                return recipe;
        }
        
        private static void giveItem(CharacterFactoryRecipe recipe, int itemid, int quantity, MapleInventoryType itemType) {
                recipe.addStartingItem(itemid, quantity, itemType);
        }
        
        public static int createCharacter(MapleClient c, String name, int face, int hair, int skin, int top, int bottom, int shoes, int weapon, int gender) {
                int status = createNewCharacter(c, name, face, hair, skin, gender, createRecipe(MapleJob.BEGINNER, 1, 10000, top, bottom, shoes, weapon));
                return status;
        }
}
