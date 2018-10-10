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
package client.processor;

import client.MapleCharacter;
import java.awt.Point;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.SkillFactory;
import provider.MapleDataTool;
import client.inventory.manipulator.MapleInventoryManipulator;
import java.io.File;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import tools.MaplePacketCreator;

/**
 *
 * @author RonanLana (just added locking on OdinMS' SpawnPetHandler method body)
 */
public class SpawnPetProcessor {
    private static MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
    
    public static void processSpawnPet(MapleClient c, byte slot, boolean lead) {
        if (c.tryacquireClient()) {
            try {
                MapleCharacter chr = c.getPlayer();
                MaplePet pet = chr.getInventory(MapleInventoryType.CASH).getItem(slot).getPet();
                if (pet == null) return;

                int petid = pet.getItemId();
                if (petid == 5000028 || petid == 5000047) //Handles Dragon AND Robos
                {
                    if (chr.haveItem(petid + 1)) {
                        chr.dropMessage(5, "You can't hatch your " + (petid == 5000028 ? "Dragon egg" : "Robo egg") + " if you already have a Baby " + (petid == 5000028 ? "Dragon." : "Robo."));
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                    } else {
                        int evolveid = MapleDataTool.getInt("info/evol1", dataRoot.getData("Pet/" + petid + ".img"));
                        int petId = MaplePet.createPet(evolveid);
                        if (petId == -1) {
                            return;
                        }
                        long expiration = chr.getInventory(MapleInventoryType.CASH).getItem(slot).getExpiration();
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, petid, (short) 1, false, false);
                        MapleInventoryManipulator.addById(c, evolveid, (short) 1, null, petId, expiration);
                        pet.deleteFromDb();

                        c.announce(MaplePacketCreator.enableActions());
                        return;
                    }
                }
                if (chr.getPetIndex(pet) != -1) {
                    chr.unequipPet(pet, true);
                } else {
                    if (chr.getSkillLevel(SkillFactory.getSkill(8)) == 0 && chr.getPet(0) != null) {
                        chr.unequipPet(chr.getPet(0), false);
                    }
                    if (lead) {
                        chr.shiftPetsRight();
                    }
                    Point pos = chr.getPosition();
                    pos.y -= 12;
                    pet.setPos(pos);
                    pet.setFh(chr.getMap().getFootholds().findBelow(pet.getPos()).getId());
                    pet.setStance(0);
                    pet.setSummoned(true);
                    pet.saveToDb();
                    chr.addPet(pet);
                    chr.getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showPet(c.getPlayer(), pet, false, false), true);
                    c.announce(MaplePacketCreator.petStatUpdate(c.getPlayer()));
                    c.announce(MaplePacketCreator.enableActions());

                    chr.commitExcludedItems();
                    chr.getClient().getWorldServer().registerPetHunger(chr, chr.getPetIndex(pet));
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
