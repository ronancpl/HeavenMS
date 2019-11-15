/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

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
package constants.net;

import java.util.Map;
import java.util.HashMap;
import net.opcodes.RecvOpcode;
import net.opcodes.SendOpcode;

/**
 *
 * @author Ronan
 */
public class OpcodeConstants {
    public static Map<Integer, String> sendOpcodeNames = new HashMap<>();
    public static Map<Integer, String> recvOpcodeNames = new HashMap<>();
    
    public static void generateOpcodeNames() {
        for (SendOpcode op : SendOpcode.values()) {
            sendOpcodeNames.put(op.getValue(), op.name());
        }
        
        for (RecvOpcode op : RecvOpcode.values()) {
            recvOpcodeNames.put(op.getValue(), op.name());
        }
    }
    
}
