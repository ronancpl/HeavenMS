package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamilyEntitlement;
import client.MapleFamilyEntry;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.server.coordinator.MapleInviteCoordinator;
import net.server.coordinator.MapleInviteCoordinator.InviteResult;
import net.server.coordinator.MapleInviteCoordinator.InviteType;
import net.server.coordinator.MapleInviteCoordinator.MapleInviteResult;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class FamilySummonResponseHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if(!ServerConstants.USE_FAMILY_SYSTEM) return;
        MapleCharacter inviter = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
        boolean accept = slea.readByte() != 0;
        MapleFamilyEntry inviterEntry = inviter.getFamilyEntry();
        if(inviter == null || inviterEntry == null) return;
        MapleInviteResult inviteResult = MapleInviteCoordinator.answerInvite(InviteType.FAMILY_SUMMON, c.getPlayer().getId(), c.getPlayer(), accept);
        if(inviteResult.result == InviteResult.NOT_FOUND) return;
        if(inviter != inviteResult.from) return;
        MapleMap map = (MapleMap) inviteResult.params[0];
        if(accept && inviter.getMap() == map) { //cancel if inviter has changed maps
            c.getPlayer().changeMap(map, map.getPortal(0));
        } else {
            inviterEntry.refundEntitlement(MapleFamilyEntitlement.SUMMON_FAMILY);
            inviterEntry.gainReputation(MapleFamilyEntitlement.SUMMON_FAMILY.getRepCost()); //refund rep cost if declined
            inviter.announce(MaplePacketCreator.getFamilyInfo(inviterEntry));
            inviter.dropMessage(5, c.getPlayer().getName() + " has denied the summon request.");
        }
    }

}
