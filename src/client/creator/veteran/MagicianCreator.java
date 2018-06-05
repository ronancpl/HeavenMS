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
import client.Skill;
import client.SkillFactory;
import client.creator.CharacterFactory;
import client.creator.CharacterFactoryRecipe;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.skills.Magician;
import server.MapleItemInformationProvider;

/**
 *
 * @author RonanLana
 */
public class MagicianCreator extends CharacterFactory {
        private static int[] equips = {0, 1041041, 0, 1061034, 1072075};
        private static int[] weapons = {1372003, 1382017};
        private static int[] startingHpMp = {405, 729};
        private static int[] mpGain = {0, 40, 80, 118, 156, 194, 230, 266, 302, 336, 370};
    
        private static CharacterFactoryRecipe createRecipe(MapleJob job, int level, int map, int top, int bottom, int shoes, int weapon, int gender, int improveSp) {
                CharacterFactoryRecipe recipe = new CharacterFactoryRecipe(job, level, map, top, bottom, shoes, weapon);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                
                recipe.setInt(20);
                recipe.setRemainingAp(138);
                recipe.setRemainingSp(67);

                recipe.setMaxHp(startingHpMp[0]);
                recipe.setMaxMp(startingHpMp[1] + mpGain[improveSp]);
                
                recipe.setMeso(100000);
                
                if(gender == 0) {
                        giveEquipment(recipe, ii, 1050003);
                }

                for(int i = 1; i < weapons.length; i++) {
                        giveEquipment(recipe, ii, weapons[i]);
                }

                giveItem(recipe, 2000001, 100, MapleInventoryType.USE);
                giveItem(recipe, 2000006, 100, MapleInventoryType.USE);
                giveItem(recipe, 3010000, 1, MapleInventoryType.SETUP);
                
                if(improveSp > 0) {
                        improveSp += 5;
                        recipe.setRemainingSp(recipe.getRemainingSp() - improveSp);

                        int toUseSp = 5;
                        Skill improveMpRec = SkillFactory.getSkill(Magician.IMPROVED_MP_RECOVERY);
                        recipe.addStartingSkillLevel(improveMpRec, toUseSp);
                        improveSp -= toUseSp;

                        if(improveSp > 0) {
                                Skill improveMaxMp = SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
                                recipe.addStartingSkillLevel(improveMaxMp, improveSp);
                        }
                }
                
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
                return createNewCharacter(c, name, face, hair, skin, gender, createRecipe(MapleJob.MAGICIAN, 30, 101000000, equips[gender], equips[2 + gender], equips[4], weapons[0], gender, improveSp));
        }
}
