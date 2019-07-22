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

import java.util.Arrays;

import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public abstract class AbstractAnimatedMapleMapObject extends AbstractMapleMapObject implements AnimatedMapleMapObject {
    
	static {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter((int) getIdleMovementDataLength());
    	mplew.write(1); //movement command count
    	mplew.write(0);
    	mplew.writeShort(-1); //x
    	mplew.writeShort(-1); //y
    	mplew.writeShort(0); //xwobble
    	mplew.writeShort(0); //ywobble
    	mplew.writeShort(0); //fh
    	mplew.write(-1); //stance
    	mplew.writeShort(0); //duration
    	idleMovementPacketData = mplew.getPacket();
	}
	
	private static final byte[] idleMovementPacketData;
	
	private int stance;

    @Override
    public int getStance() {
        return stance;
    }

    @Override
    public void setStance(int stance) {
        this.stance = stance;
    }

    @Override
    public boolean isFacingLeft() {
        return Math.abs(stance) % 2 == 1;
    }
    
    public SeekableLittleEndianAccessor getIdleMovement() {
    	byte[] movementData = Arrays.copyOf(idleMovementPacketData, idleMovementPacketData.length);
    	//seems wasteful to create a whole packet writer when only a few values are changed
    	int x = getPosition().x;
    	int y = getPosition().y;
    	movementData[2] = (byte) (x & 0xFF); //x
    	movementData[3] = (byte) (x >> 8 & 0xFF);
    	movementData[4] = (byte) (y & 0xFF); //y
    	movementData[5] = (byte) (y >> 8 & 0xFF);
    	movementData[12] = (byte) (getStance() & 0xFF);
    	return new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(movementData));
    }
    
    public static long getIdleMovementDataLength() {
    	return 15;
    }
}
