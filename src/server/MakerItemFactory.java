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
package server;

import config.YamlConfig;
import constants.inventory.EquipType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import tools.Pair;

/**
 *
 * @author Jay Estrella, Ronan
 */
public class MakerItemFactory {
    private static MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    
    public static MakerItemCreateEntry getItemCreateEntry(int toCreate, int stimulantid, Map<Integer, Short> reagentids) {
        MakerItemCreateEntry makerEntry = ii.getMakerItemEntry(toCreate);
        if(makerEntry.isInvalid()) {
            return makerEntry;
        }
        
          // THEY DECIDED FOR SOME BIZARRE PATTERN ON THE FEE THING, ALMOST RANDOMIZED.
        if(stimulantid != -1) {
            makerEntry.addCost(getMakerStimulantFee(toCreate));
        }
        
        if(!reagentids.isEmpty()) {
            for(Entry<Integer, Short> r : reagentids.entrySet()) {
                makerEntry.addCost((getMakerReagentFee(toCreate, ((r.getKey() % 10) + 1))) * r.getValue());
            }
        }
        
        makerEntry.trimCost();  // "commit" the real cost of the recipe.
        return makerEntry;
    }
    
    public static MakerItemCreateEntry generateLeftoverCrystalEntry(int fromLeftoverid, int crystalId) {
        MakerItemCreateEntry ret = new MakerItemCreateEntry(0, 0, 1);
        ret.addReqItem(fromLeftoverid, 100);
        ret.addGainItem(crystalId, 1);
        return ret;
    }

    public static MakerItemCreateEntry generateDisassemblyCrystalEntry(int fromEquipid, int cost, List<Pair<Integer, Integer>> gains) {     // equipment at specific position already taken
        MakerItemCreateEntry ret = new MakerItemCreateEntry(cost, 0, 1);
        ret.addReqItem(fromEquipid, 1);
        for (Pair<Integer, Integer> p : gains) {
            ret.addGainItem(p.getLeft(), p.getRight());
        }
        return ret;
    }
            
    private static double getMakerStimulantFee(int itemid) {
        if(YamlConfig.config.server.USE_MAKER_FEE_HEURISTICS) {
            EquipType et = EquipType.getEquipTypeById(itemid);
            int eqpLevel = ii.getEquipLevelReq(itemid);

            switch(et) {
                case CAP:
                    return 1145.736246 * Math.exp(0.03336832546 * eqpLevel);

                case LONGCOAT:
                    return 2117.469118 * Math.exp(0.03355349137 * eqpLevel);

                case SHOES:
                    return 1218.624674 * Math.exp(0.0342266043 * eqpLevel);

                case GLOVES:
                    return 2129.531152 * Math.exp(0.03421778102 * eqpLevel);

                case COAT:
                    return 1770.630579 * Math.exp(0.03359768677 * eqpLevel);

                case PANTS:
                    return 1442.98837 * Math.exp(0.03444783295 * eqpLevel);

                case SHIELD:
                    return 6312.40136 * Math.exp(0.0237929527 * eqpLevel);

                default:    // weapons
                    return 4313.581428 * Math.exp(0.03147837094 * eqpLevel);
            }
        } else {
            return 14000;
        }
    }
    
    private static double getMakerReagentFee(int itemid, int reagentLevel) {
        if(YamlConfig.config.server.USE_MAKER_FEE_HEURISTICS) {
            EquipType et = EquipType.getEquipTypeById(itemid);
            int eqpLevel = ii.getEquipLevelReq(itemid);

            switch(et) {
                case CAP:
                    return 5592.01613 * Math.exp(0.02914576018 * eqpLevel) * reagentLevel;

                case LONGCOAT:
                    return 3405.23441 * Math.exp(0.03413001038 * eqpLevel) * reagentLevel;

                case SHOES:
                    return 2115.697484 * Math.exp(0.0354881705 * eqpLevel) * reagentLevel;

                case GLOVES:
                    return 4684.040894 * Math.exp(0.03166500585 * eqpLevel) * reagentLevel;

                case COAT:
                    return 2955.89017 * Math.exp(0.0339948456 * eqpLevel) * reagentLevel;

                case PANTS:
                    return 1774.722181 * Math.exp(0.03854321409 * eqpLevel) * reagentLevel;

                case SHIELD:
                    return 12014.11296 * Math.exp(0.02185471162 * eqpLevel) * reagentLevel;

                default:    // weapons
                    return 4538.650247 * Math.exp(0.0371980303 * eqpLevel) * reagentLevel;
            }
        } else {
            return 8000 * reagentLevel;
        }
    }
    
    public static class MakerItemCreateEntry {
        private int reqLevel, reqMakerLevel;
        private double cost;
        private int reqCost;
        private List<Pair<Integer, Integer>> reqItems = new ArrayList<>(); // itemId / amount
        private List<Pair<Integer, Integer>> gainItems = new ArrayList<>(); // itemId / amount

        public MakerItemCreateEntry(int cost, int reqLevel, int reqMakerLevel) {
            this.cost = cost;
            this.reqLevel = reqLevel;
            this.reqMakerLevel = reqMakerLevel;
        }
        
        public MakerItemCreateEntry(MakerItemCreateEntry mi) {
            this.cost = mi.cost;
            this.reqLevel = mi.reqLevel;
            this.reqMakerLevel = mi.reqMakerLevel;
            
            for(Pair<Integer, Integer> p : mi.reqItems) {
                reqItems.add(p);
            }
            
            for(Pair<Integer, Integer> p : mi.gainItems) {
                gainItems.add(p);
            }
        }

        public List<Pair<Integer, Integer>> getReqItems() {
            return reqItems;
        }
        
        public List<Pair<Integer, Integer>> getGainItems() {
            return gainItems;
        }

        public int getReqLevel() {
            return reqLevel;
        }

        public int getReqSkillLevel() {
            return reqMakerLevel;
        }

        public int getCost() {
            return reqCost;
        }
        
        public void addCost(double amount) {
            cost += amount;
        }

        protected void addReqItem(int itemId, int amount) {
            reqItems.add(new Pair<>(itemId, amount));
        }
        
        protected void addGainItem(int itemId, int amount) {
            gainItems.add(new Pair<>(itemId, amount));
        }
        
        public void trimCost() {
            reqCost = (int) (cost / 1000);
            reqCost *= 1000;
        }
        
        public boolean isInvalid() {    // thanks Rohenn, Wh1SK3Y for noticing some items not getting checked properly
            return reqLevel < 0;
        }
    }
}
