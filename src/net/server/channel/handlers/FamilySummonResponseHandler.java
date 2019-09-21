package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamilyEntitlement;
import client.MapleFamilyEntry;
import config.YamlConfig;
import net.AbstractMaplePacketHandler;
import net.server.coordinator.world.MapleInviteCoordinator;
import net.server.coordinator.world.MapleInviteCoordinator.InviteResult;
import net.server.coordinator.world.MapleInviteCoordinator.InviteType;
import net.server.coordinator.world.MapleInviteCoordinator.MapleInviteResult;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class FamilySummonResponseHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if(!YamlConfig.config.server.USE_FAMILY_SYSTEM) return;
        slea.readMapleAsciiString(); //family name
        boolean accept = slea.readByte() != 0;
        MapleInviteResult inviteResult = MapleInviteCoordinator.answerInvite(InviteType.FAMILY_SUMMON, c.getPlayer().getId(), c.getPlayer(), accept);
        if(inviteResult.result == InviteResult.NOT_FOUND) return;
        MapleCharacter inviter = inviteResult.from;
        MapleFamilyEntry inviterEntry = inviter.getFamilyEntry();
        if(inviterEntry == null) return;
        MapleMap map = (MapleMap) inviteResult.params[0];
        if(accept && inviter.getMap() == map) { //cancel if inviter has changed maps
            c.getPlayer().changeMap(map, map.getPortal(0));
        } else {
            inviterEntry.refundEntitlement(MapleFamilyEntitlement.SUMMON_FAMILY);
            inviterEntry.gainReputation(MapleFamilyEntitlement.SUMMON_FAMILY.getRepCost(), false); //refund rep cost if declined
            inviter.announce(MaplePacketCreator.getFamilyInfo(inviterEntry));
            inviter.dropMessage(5, c.getPlayer().getName() + " has denied the summon request.");
        }
    }

}
