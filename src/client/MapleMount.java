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
package client;

/**
 * @author PurpleMadness Patrick :O
 */
public class MapleMount {
    private int itemid;
    private int skillid;
    private int tiredness;
    private int exp;
    private int level;
    private MapleCharacter owner;
    private boolean active;

    public MapleMount(MapleCharacter owner, int id, int skillid) {
        this.itemid = id;
        this.skillid = skillid;
        this.tiredness = 0;
        this.level = 1;
        this.exp = 0;
        this.owner = owner;
        active = true;
    }

    public int getItemId() {
        return itemid;
    }

    public int getSkillId() {
        return skillid;
    }

    /**
     * 1902000 - Hog
     * 1902001 - Silver Mane
     * 1902002 - Red Draco
     * 1902005 - Mimiana
     * 1902006 - Mimio
     * 1902007 - Shinjou
     * 1902008 - Frog
     * 1902009 - Ostrich
     * 1902010 - Frog
     * 1902011 - Turtle
     * 1902012 - Yeti
     * @return the id
     */
    public int getId() {
        if (this.itemid < 1903000) {
            return itemid - 1901999;
        }
        return 5;
    }

    public int getTiredness() {
        return tiredness;
    }

    public int getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public void setTiredness(int newtiredness) {
        this.tiredness = newtiredness;
        if (tiredness < 0) {
            tiredness = 0;
        }
    }
    
    public int incrementAndGetTiredness() {
        this.tiredness++;
        return this.tiredness;
    }

    public void setExp(int newexp) {
        this.exp = newexp;
    }

    public void setLevel(int newlevel) {
        this.level = newlevel;
    }

    public void setItemId(int newitemid) {
        this.itemid = newitemid;
    }

    public void setActive(boolean set) {
        this.active = set;
    }

    public boolean isActive() {
        return active;
    }
    
    public void empty() {
        if(owner != null) owner.getClient().getWorldServer().unregisterMountHunger(owner);
        this.owner = null;
    }    
}
