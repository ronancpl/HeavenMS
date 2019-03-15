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

import java.awt.Point;
import java.nio.charset.Charset;
import constants.CharsetConstants.MapleLanguageType;

/**
 * Provides a generic writer of a little-endian sequence of bytes.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public class GenericLittleEndianWriter implements LittleEndianWriter {
    private static Charset ASCII = Charset.forName(MapleLanguageType.LANGUAGE_US.getAscii());
    private ByteOutputStream bos;

    /**
     * Class constructor - Protected to prevent instantiation with no arguments.
     */
    protected GenericLittleEndianWriter() {
        // Blah!
    }

    /**
     * Sets the byte-output stream for this instance of the object.
     *
     * @param bos The new output stream to set.
     */
    void setByteOutputStream(ByteOutputStream bos) {
        this.bos = bos;
    }

    /**
     * Write an array of bytes to the stream.
     *
     * @param b The bytes to write.
     */
    @Override
    public void write(byte[] b) {
        for (int x = 0; x < b.length; x++) {
            bos.writeByte(b[x]);
        }
    }

    /**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(byte b) {
        bos.writeByte(b);
    }

    /**
     * Write a byte in integer form to the stream.
     *
     * @param b The byte as an <code>Integer</code> to write.
     */
    @Override
    public void write(int b) {
        bos.writeByte((byte) b);
    }

    @Override
    public void skip(int b) {
        write(new byte[b]);
    }

    /**
     * Write a short integer to the stream.
     *
     * @param i The short integer to write.
     */
    @Override
    public void writeShort(int i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
    }
    
    /**
     * Writes an integer to the stream.
     *
     * @param i The integer to write.
     */
    @Override
    public void writeInt(int i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
        bos.writeByte((byte) ((i >>> 16) & 0xFF));
        bos.writeByte((byte) ((i >>> 24) & 0xFF));
    }

    /**
     * Writes an ASCII string the the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public void writeAsciiString(String s) {
        write(s.getBytes(ASCII));
    }

    /**
     * Writes a maple-convention ASCII string to the stream.
     *
     * @param s The ASCII string to use maple-convention to write.
     */
    @Override
    public void writeMapleAsciiString(String s) {
        writeShort((short) s.length());
        writeAsciiString(s);
    }

    /**
     * Writes a null-terminated ASCII string to the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public void writeNullTerminatedAsciiString(String s) {
        writeAsciiString(s);
        write(0);
    }

    /**
     * Write a long integer to the stream.
     * @param l The long integer to write.
     */
    @Override
    public void writeLong(long l) {
        bos.writeByte((byte) (l & 0xFF));
        bos.writeByte((byte) ((l >>> 8) & 0xFF));
        bos.writeByte((byte) ((l >>> 16) & 0xFF));
        bos.writeByte((byte) ((l >>> 24) & 0xFF));
        bos.writeByte((byte) ((l >>> 32) & 0xFF));
        bos.writeByte((byte) ((l >>> 40) & 0xFF));
        bos.writeByte((byte) ((l >>> 48) & 0xFF));
        bos.writeByte((byte) ((l >>> 56) & 0xFF));
    }

    /**
     * Writes a 2D 4 byte position information
     *
     * @param s The Point position to write.
     */
    @Override
    public void writePos(Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    /**
     * Writes a boolean true ? 1 : 0
     *
     * @param b The boolean to write.
     */
    @Override
    public void writeBool(final boolean b) {
        write(b ? 1 : 0);
    }
}
