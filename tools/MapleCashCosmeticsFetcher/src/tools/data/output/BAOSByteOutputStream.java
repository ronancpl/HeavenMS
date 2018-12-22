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
package tools.data.output;

import java.io.ByteArrayOutputStream;

/**
 * Uses a byte array to output a stream of bytes.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 352
 */
class BAOSByteOutputStream implements ByteOutputStream {
    private ByteArrayOutputStream baos;

    /**
     * Class constructor - Wraps the stream around a Java BAOS.
     *
     * @param baos <code>The ByteArrayOutputStream</code> to wrap this around.
     */
    BAOSByteOutputStream(ByteArrayOutputStream baos) {
        super();
        this.baos = baos;
    }

    /**
     * Writes a byte to the stream.
     *
     * @param b The byte to write to the stream.
     * @see tools.data.output.ByteOutputStream#writeByte(byte)
     */
    @Override
    public void writeByte(byte b) {
        baos.write(b);
    }
}
