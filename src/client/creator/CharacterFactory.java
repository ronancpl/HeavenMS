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
package client.creator;

import client.MapleClient;
import client.MapleCharacter;
import client.MapleSkinColor;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.ServerConstants;
import net.server.Server;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;

/**
 *
 * @author RonanLana
 */
public abstract class CharacterFactory {
        
        protected synchronized static int createNewCharacter(MapleClient c, String name, int face, int hair, int skin, int gender, CharacterFactoryRecipe recipe) {
                if (ServerConstants.COLLECTIVE_CHARSLOT ? c.getAvailableCharacterSlots() <= 0 : c.getAvailableCharacterWorldSlots() <= 0) {
                        return -3;
                }
            
                if (!MapleCharacter.canCreateChar(name)) {
                        return -1;
                }
                
                MapleCharacter newchar = MapleCharacter.getDefault(c);
                newchar.setWorld(c.getWorld());
                newchar.setSkinColor(MapleSkinColor.getById(skin));
                newchar.setGender(gender);
                newchar.setName(name);
                newchar.setHair(hair);
                newchar.setFace(face);
                
                newchar.setLevel(recipe.getLevel());
                newchar.setJob(recipe.getJob());
                newchar.setMapId(recipe.getMap());
                
                MapleInventory equipped = newchar.getInventory(MapleInventoryType.EQUIPPED);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                
                int top = recipe.getTop(), bottom = recipe.getBottom(), shoes = recipe.getShoes(), weapon = recipe.getWeapon();
                
                if(top > 0) {
                    Item eq_top = ii.getEquipById(top);
                    eq_top.setPosition((byte) -5);
                    equipped.addItemFromDB(eq_top);
                }
                
                if(bottom > 0) {
                    Item eq_bottom = ii.getEquipById(bottom);
                    eq_bottom.setPosition((byte) -6);
                    equipped.addItemFromDB(eq_bottom);
                }
                
                if(shoes > 0) {
                    Item eq_shoes = ii.getEquipById(shoes);
                    eq_shoes.setPosition((byte) -7);
                    equipped.addItemFromDB(eq_shoes);
                }
                
                if(weapon > 0) {
                    Item eq_weapon = ii.getEquipById(weapon);
                    eq_weapon.setPosition((byte) -11);
                    equipped.addItemFromDB(eq_weapon.copy());
                }
                
                if (!newchar.insertNewChar(recipe)) {
                        return -2;
                }
                c.announce(MaplePacketCreator.addNewCharEntry(newchar));
                
                Server.getInstance().createCharacterEntry(newchar);
                Server.getInstance().broadcastGMMessage(c.getWorld(), MaplePacketCreator.sendYellowTip("[NEW CHAR]: " + c.getAccountName() + " has created a new character with IGN " + name));
                
                return 0;
        }        
}
