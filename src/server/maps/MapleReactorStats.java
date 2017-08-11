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
package server.maps;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.Pair;

/**
 * @author Lerk
 * @author Ronan
 */
public class MapleReactorStats {
    private Point tl;
    private Point br;
    private Map<Byte, List<StateData>> stateInfo = new HashMap<>();
    private Map<Byte, Integer> timeoutInfo = new HashMap<>();

    public void setTL(Point tl) {
        this.tl = tl;
    }

    public void setBR(Point br) {
        this.br = br;
    }

    public Point getTL() {
        return tl;
    }

    public Point getBR() {
        return br;
    }

    public void addState(byte state, List<StateData> data, int timeOut) {
        stateInfo.put(state, data);
        if(timeOut > -1) timeoutInfo.put(state, timeOut);
    }
    
    public int getTimeout(byte state) {
        Integer i = timeoutInfo.get(state);
        return (i == null) ? -1 : i;
    }
    
    public byte getTimeoutState(byte state) {
        return stateInfo.get(state).get(stateInfo.get(state).size() - 1).getNextState();
    }

    public byte getStateSize(byte state) {
        return (byte) stateInfo.get(state).size();
    }

    public byte getNextState(byte state, byte index) {
        if (stateInfo.get(state) == null || stateInfo.get(state).size() < (index + 1)) return -1;
        StateData nextState = stateInfo.get(state).get(index);
        if (nextState != null) {
            return nextState.getNextState();
        } else {
            return -1;
        }
    }

    public List<Integer> getActiveSkills(byte state, byte index) {
        StateData nextState = stateInfo.get(state).get(index);
        if (nextState != null) {
            return nextState.getActiveSkills();
        } else {
            return null;
        }
    }

    public int getType(byte state) {
        List<StateData> list = stateInfo.get(state);
        if (list != null) {
            return list.get(0).getType();
        } else {
            return -1;
        }
    }

    public Pair<Integer, Integer> getReactItem(byte state, byte index) {
        StateData nextState = stateInfo.get(state).get(index);
        if (nextState != null) {
            return nextState.getReactItem();
        } else {
            return null;
        }
    }


    public static class StateData {
        private int type;
        private Pair<Integer, Integer> reactItem;
        private List<Integer> activeSkills;
        private byte nextState;

        public StateData(int type, Pair<Integer, Integer> reactItem, List<Integer> activeSkills, byte nextState) {
            this.type = type;
            this.reactItem = reactItem;
            this.activeSkills = activeSkills;
            this.nextState = nextState;
        }

        private int getType() {
            return type;
        }

        private byte getNextState() {
            return nextState;
        }

        private Pair<Integer, Integer> getReactItem() {
            return reactItem;
        }

        private List<Integer> getActiveSkills() {
            return activeSkills;
        }
    }
}
