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
package net.server.worker;

import net.server.Server;

/**
 * @author Ronan
 * @info   Thread responsible for announcing other players diseases when one enters into a map
 */
public class CharacterDiseaseWorker implements Runnable {
    @Override
    public void run() {
        Server serv = Server.getInstance();
        
        serv.updateCurrentTime();
        serv.runAnnouncePlayerDiseasesSchedule();
    }
}
