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
package mapleskillmakerfetcher;

import java.util.List;

/**
 *
 * @author RonanLana
 */
public class MapleMakerItemEntry {
    public int id = -1;
    public int itemid = -1;
    public int reqLevel = -1;
    public int reqMakerLevel = -1;
    public int reqItem = -1;
    public int reqMeso = -1;
    public int reqEquip = -1;
    public int catalyst = -1;
    public int quantity = -1;
    public int tuc = -1;
    
    public int recipeCount = -1;
    public int recipeItem = -1;
    
    public List<int[]> recipeList = null;
    public List<int[]> randomList = null;
    
    public MapleMakerItemEntry(int id, int itemid, int reqLevel, int reqMakerLevel, int reqItem, int reqMeso, int reqEquip, int catalyst, int quantity, int tuc, int recipeCount, int recipeItem, List<int[]> recipeList, List<int[]> randomList) {
        this.id = id;
        this.itemid = itemid;
        this.reqLevel = reqLevel;
        this.reqMakerLevel = reqMakerLevel;
        this.reqItem = reqItem;
        this.reqMeso = reqMeso;
        this.reqEquip = reqEquip;
        this.catalyst = catalyst;
        this.quantity = quantity;
        this.tuc = tuc;

        this.recipeCount = recipeCount;
        this.recipeItem = recipeItem;

        this.recipeList = recipeList;
        this.randomList = randomList;
    }
}
