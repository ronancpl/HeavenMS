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
import constants.skills.Warrior;
import server.MapleItemInformationProvider;

/**
 *
 * @author RonanLana
 */
public class WarriorCreator extends CharacterFactory {
        private static int[] equips = {1040021, 0, 1060016, 0, 1072039};
        private static int[] weapons = {1302008, 1442001, 1422001, 1312005};
        private static int[] startingHpMp = {905, 208};
        private static int[] hpGain = {0, 72, 144, 212, 280, 348, 412, 476, 540, 600, 660};
        
        private static CharacterFactoryRecipe createRecipe(MapleJob job, int level, int map, int top, int bottom, int shoes, int weapon, int gender, int improveSp) {
                CharacterFactoryRecipe recipe = new CharacterFactoryRecipe(job, level, map, top, bottom, shoes, weapon);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                
                recipe.setStr(35);
                recipe.setRemainingAp(123);
                recipe.setRemainingSp(61);

                recipe.setMaxHp(startingHpMp[0] + hpGain[improveSp]);
                recipe.setMaxMp(startingHpMp[1]);
                
                recipe.setMeso(100000);
                
                if(gender == 1) {
                        giveEquipment(recipe, ii, 1051010);
                }

                for(int i = 1; i < weapons.length; i++) {
                        giveEquipment(recipe, ii, weapons[i]);
                }
                
                if(improveSp > 0) {
                        improveSp += 5;
                        recipe.setRemainingSp(recipe.getRemainingSp() - improveSp);

                        int toUseSp = 5;
                        Skill improveHpRec = SkillFactory.getSkill(Warrior.IMPROVED_HPREC);
                        recipe.addStartingSkillLevel(improveHpRec, toUseSp);
                        improveSp -= toUseSp;

                        if(improveSp > 0) {
                                Skill improveMaxHp = SkillFactory.getSkill(Warrior.IMPROVED_MAXHP);
                                recipe.addStartingSkillLevel(improveMaxHp, improveSp);
                        }
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
                return createNewCharacter(c, name, face, hair, skin, gender, createRecipe(MapleJob.WARRIOR, 30, 102000000, equips[gender], equips[2 + gender], equips[4], weapons[0], gender, improveSp));
        }
}
