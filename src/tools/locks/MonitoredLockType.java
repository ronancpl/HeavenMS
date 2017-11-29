/*
    This file is part of the HeavenMS (MapleSolaxiaV2) MapleStory Server
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
package tools.locks;

/**
 *
 * @author RonanLana
 */

public enum MonitoredLockType {
    UNDEFINED(-1),
    CHR(0),
    EFF(1),
    PET(2),
    PRT(3),
    CLIENT(4),
    BOOK(5),
    ITEM(6),
    INVENTORY(7),
    SRVHANDLER_IDLE(8),
    SRVHANDLER_TEMP(9),
    BUFF_STORAGE(10),
    PLAYER_STORAGE(11),
    SERVER(12),
    MERCHANT(13),
    CHANNEL(14),
    GUILD(15),
    PARTY(16),
    WORLD_PARTY(17),
    WORLD_OWL(18),
    WORLD_PETS(19),
    WORLD_MOUNTS(20),
    WORLD_PSHOPS(21),
    WORLD_MERCHS(21),
    EIM(22),
    EIM_PARTY(23),
    EIM_SCRIPT(24),
    EM_LOBBY(25),
    EM_QUEUE(26),
    CASHSHOP(27),
    VISITOR_PSHOP(28),
    STORAGE(29),
    MOB_EXT(30),
    MOB(31),
    MOB_STATI(32),
    MOBSKILL_FACTORY(33),
    PORTAL(34),
    VISITOR_MERCH(35),
    MAP_CHRS(36),
    MAP_OBJS(37),
    MAP_FACTORY(38),
    MAP_ITEM(39),
    MAP_BOUNDS(40),
    MINIDUNGEON(41),
    REACTOR(42);
    
    private final int i;
    
    private MonitoredLockType(int val) {
        this.i = val;
    }

    public int getValue() {
        return i;
    }
}
