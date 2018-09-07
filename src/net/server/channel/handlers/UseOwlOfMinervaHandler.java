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
package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.MaplePacketCreator;
import tools.Pair;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import constants.GameConstants;

/**
 * @author Ronan
 */

public final class UseOwlOfMinervaHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        List<Pair<Integer, Integer>> owlSearched = c.getWorldServer().getOwlSearchedItems();
        List<Integer> owlLeaderboards;
        
        if(owlSearched.size() < 5) {
            owlLeaderboards = new LinkedList<>();
            for(int i : GameConstants.OWL_DATA) {
                owlLeaderboards.add(i);
            }
        } else {
            Comparator<Pair<Integer, Integer>> comparator = new Comparator<Pair<Integer, Integer>>() {  // descending order
                @Override
                public int compare(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
                    return p2.getRight().compareTo(p1.getRight());
                }
            };
            
            PriorityQueue<Pair<Integer, Integer>> queue = new PriorityQueue<>(Math.max(1, owlSearched.size()), comparator);
            for(Pair<Integer, Integer> p : owlSearched) {
                queue.add(p);
            }
            
            owlLeaderboards = new LinkedList<>();
            for(int i = 0; i < Math.min(owlSearched.size(), 10); i++) {
                owlLeaderboards.add(queue.remove().getLeft());
            }
        }
        
        c.announce(MaplePacketCreator.getOwlOpen(owlLeaderboards));
    }
}