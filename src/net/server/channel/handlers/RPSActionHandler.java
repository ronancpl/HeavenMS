package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import server.minigame.MapleRockPaperScissor;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 15, 2016
 */
public final class RPSActionHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
                MapleCharacter chr = c.getPlayer();
                MapleRockPaperScissor rps = chr.getRPS();
                
                if (c.tryacquireClient()) {
                        try {
                                if(slea.available() == 0 || !chr.getMap().containsNPC(9000019)){
                                        if(rps != null){
                                                rps.dispose(c);
                                        }
                                        return;
                                }
                                final byte mode = slea.readByte();
                                switch (mode){
                                        case 0: // start game
                                        case 5: // retry
                                                if(rps != null){
                                                        rps.reward(c);
                                                }
                                                if(chr.getMeso() >= 1000){
                                                        chr.setRPS(new MapleRockPaperScissor(c, mode));
                                                }else{
                                                        c.announce(MaplePacketCreator.rpsMesoError(-1));
                                                }
                                                break;
                                        case 1: // answer
                                                if(rps == null || !rps.answer(c, slea.readByte())){
                                                        c.announce(MaplePacketCreator.rpsMode((byte) 0x0D));// 13
                                                }
                                                break;
                                        case 2: // time over
                                                if(rps == null || !rps.timeOut(c)){
                                                        c.announce(MaplePacketCreator.rpsMode((byte) 0x0D));
                                                }
                                                break;
                                        case 3: // continue
                                                if(rps == null || !rps.nextRound(c)){
                                                        c.announce(MaplePacketCreator.rpsMode((byte) 0x0D));
                                                }
                                                break;
                                        case 4: // leave
                                                if(rps != null){
                                                        rps.dispose(c);
                                                }else{
                                                        c.announce(MaplePacketCreator.rpsMode((byte) 0x0D));
                                                }
                                                break;
                                }
                        } finally {
                            c.releaseClient();
                        }
                }
	}
}
