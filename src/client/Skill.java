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

import java.util.ArrayList;
import java.util.List;
import server.MapleStatEffect;
import server.life.Element;

public class Skill {
    private int id;
    private List<MapleStatEffect> effects = new ArrayList<>();
    private Element element;
    private int animationTime;
    private int job;
    private boolean action;

    public Skill(int id) {
        this.id = id;
        this.job = id / 10000;
    }

    public int getId() {
        return id;
    }

    public MapleStatEffect getEffect(int level) {
        return effects.get(level - 1);
    }

    public int getMaxLevel() {
        return effects.size();
    }

    public boolean isFourthJob() {
        if (job == 2212) {
        	return false;
        }
        if (id == 22170001 || id == 22171003 || id == 22171004 || id == 22181002 || id == 22181003) {
        	return true;
        }
    	return job % 10 == 2;
    }

    public void setElement(Element elem) {
        element = elem;
    }
    
    public Element getElement() {
        return element;
    }

    public int getAnimationTime() {
        return animationTime;
    }
    
    public void setAnimationTime(int time) {
        animationTime = time;
    }
    
    public void incAnimationTime(int time) {
        animationTime += time;
    }

    public boolean isBeginnerSkill() {
        return id % 10000000 < 10000;
    }
    
    public void setAction(boolean act) {
        action = act;
    }

    public boolean getAction() {
        return action;
    }
    
    public void addLevelEffect(MapleStatEffect effect) {
        effects.add(effect);
    }
}