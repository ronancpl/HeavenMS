package net.server.channel.handlers;

import client.MapleClient;
import client.MapleFamily;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class FamilyPreceptsHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleFamily family = c.getPlayer().getFamily();
        if(family == null) return;
        if(family.getLeader().getChr() != c.getPlayer()) return; //only the leader can set the precepts
        String newPrecepts = slea.readMapleAsciiString();
        if(newPrecepts.length() > 200) return;
        family.setMessage(newPrecepts, true);
        //family.broadcastFamilyInfoUpdate(); //probably don't need to broadcast for this?
        c.announce(MaplePacketCreator.getFamilyInfo(c.getPlayer().getFamilyEntry()));
    }

}
