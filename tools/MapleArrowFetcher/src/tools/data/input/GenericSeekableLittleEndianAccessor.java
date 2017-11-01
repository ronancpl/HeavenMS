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
package tools.data.input;

import java.io.IOException;

/**
 * Provides an abstract accessor to a generic Little Endian byte stream. This
 * accessor is seekable.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 323
 * @see tools.data.input.GenericLittleEndianAccessor
 */
public class GenericSeekableLittleEndianAccessor extends GenericLittleEndianAccessor implements SeekableLittleEndianAccessor {
    private SeekableInputStreamBytestream bs;

    /**
     * Class constructor
     * Provide a seekable input stream to wrap this object around.
     *
     * @param bs The byte stream to wrap this around.
     */
    public GenericSeekableLittleEndianAccessor(SeekableInputStreamBytestream bs) {
        super(bs);
        this.bs = bs;
    }

    /**
     * Seek the pointer to <code>offset</code>
     *
     * @param offset The offset to seek to.
     * @see tools.data.input.SeekableInputStreamBytestream#seek
     */
    @Override
    public void seek(long offset) {
        try {
            bs.seek(offset);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Seek failed " + e);
        }
    }

    /**
     * Get the current position of the pointer.
     *
     * @return The current position of the pointer as a long integer.
     * @see tools.data.input.SeekableInputStreamBytestream#getPosition
     */
    @Override
    public long getPosition() {
        try {
            return bs.getPosition();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("getPosition failed" + e);
            return -1;
        }
    }

    /**
     * Skip <code>num</code> number of bytes in the stream.
     *
     * @param num The number of bytes to skip.
     */
    @Override
    public void skip(int num) {
        seek(getPosition() + num);
    }
}
