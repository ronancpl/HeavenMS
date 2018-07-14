/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2017 RonanLana

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
package net.server.worker;

import java.util.Set;
import net.server.world.World;
import net.server.channel.Channel;
import tools.Pair;

/**
 * @author Ronan
 */
public class WeddingReservationWorker extends BaseWorker implements Runnable {
    
    @Override
    public void run() {
        for(Channel ch : wserv.getChannels()) {
            Pair<Boolean, Pair<Integer, Set<Integer>>> wedding;
            
            wedding = ch.getNextWeddingReservation(true);   // start cathedral
            if(wedding != null) {
                ch.setOngoingWedding(true, wedding.getLeft(), wedding.getRight().getLeft(), wedding.getRight().getRight());
            } else {
                ch.setOngoingWedding(true, null, null, null);
            }
            
            wedding = ch.getNextWeddingReservation(false);  // start chapel
            if(wedding != null) {
                ch.setOngoingWedding(false, wedding.getLeft(), wedding.getRight().getLeft(), wedding.getRight().getRight());
            } else {
                ch.setOngoingWedding(false, null, null, null);
            }
        }
    }
    
    public WeddingReservationWorker(World world) {
        super(world);
    }
}
