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
package net.server.channel.handlers;

import java.util.Collection;

import net.AbstractMaplePacketHandler;
import net.server.world.MapleParty;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import constants.ServerConstants;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;

/**
 *
 * @author XoticStory
 * @author BubblesDev
 */
public class PartySearchStartHandler extends AbstractMaplePacketHandler {
        @Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if(!ServerConstants.USE_PARTY_SEARCH){
			return;
		}
                
		int min = slea.readInt();
		int max = slea.readInt();
		slea.readInt(); // members
		int jobs = slea.readInt();
                
                MapleParty party = c.getPlayer().getParty();
                if(party == null) return;
                
		MapleCharacter chr = c.getPlayer();
		MapleMap map = chr.getMap();
                World world = c.getWorldServer();
                
		Collection<MapleMapObject> mapobjs = map.getPlayers();
                
		for (MapleMapObject mapobj : mapobjs) {
			if (party.getMembers().size() > 5) {
				break;
			}
			if (mapobj instanceof MapleCharacter) {
				MapleCharacter tchar = (MapleCharacter) mapobj;
				int charlvl = tchar.getLevel();
				if (charlvl >= min && charlvl <= max && isValidJob(tchar.getJob(), jobs)) {
					if (tchar.getParty() == null) {
                                                MaplePartyCharacter partyplayer = new MaplePartyCharacter(tchar);
                                                tchar.getMap().addPartyMember(tchar);

                                                world.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                                                tchar.receivePartyMemberHP();
                                                tchar.updatePartyMemberHP();
					}
				}
			}
		}
	}

	private static boolean isValidJob(MapleJob thejob, int jobs) {
		int jobid = thejob.getId();
		if (jobid == 0) {
			return ((jobs & 2) > 0);
		} else if (jobid == 100) {
			return ((jobs & 4) > 0);
		} else if (jobid > 100 && jobid < 113) {
			return ((jobs & 8) > 0);
		} else if (jobid > 110 && jobid < 123) {
			return ((jobs & 16) > 0);
		} else if (jobid > 120 && jobid < 133) {
			return ((jobs & 32) > 0);
		} else if (jobid == 200) {
			return ((jobs & 64) > 0);
		} else if (jobid > 209 && jobid < 213) {
			return ((jobs & 128) > 0);
		} else if (jobid > 219 && jobid < 223) {
			return ((jobs & 256) > 0);
		} else if (jobid > 229 && jobid < 233) {
			return ((jobs & 512) > 0);
		} else if (jobid == 500) {
			return ((jobs & 1024) > 0);
		} else if (jobid > 509 && jobid < 513) {
			return ((jobs & 2048) > 0);
		} else if (jobid > 519 && jobid < 523) {
			return ((jobs & 4096) > 0);
		} else if (jobid == 400) {
			return ((jobs & 8192) > 0);
		} else if (jobid > 400 && jobid < 413) {
			return ((jobs & 16384) > 0);
		} else if (jobid > 419 && jobid < 423) {
			return ((jobs & 32768) > 0);
		} else if (jobid == 300) {
			return ((jobs & 65536) > 0);
		} else if (jobid > 300 && jobid < 313) {
			return ((jobs & 131072) > 0);
		} else if (jobid > 319 && jobid < 323) {
			return ((jobs & 262144) > 0);
		}
		return false;
	}
}