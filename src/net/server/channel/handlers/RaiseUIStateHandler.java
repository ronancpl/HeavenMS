package net.server.channel.handlers;

import client.MapleCharacter.DelayedQuestUpdate;
import client.MapleClient;
import client.MapleQuestStatus;
import net.AbstractMaplePacketHandler;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Xari
 */
public class RaiseUIStateHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int questid = slea.readShort();
        
        if (c.tryacquireClient()) {
            try {
                MapleQuest quest = MapleQuest.getInstance(questid);
                MapleQuestStatus mqs = c.getPlayer().getQuest(quest);
                if (mqs.getStatus() == MapleQuestStatus.Status.NOT_STARTED) {
                    quest.forceStart(c.getPlayer(), 22000);
                    c.getPlayer().updateQuestInfo(quest.getId(), "0"); 
                } else if (mqs.getStatus() == MapleQuestStatus.Status.STARTED) {
                    c.getPlayer().announceUpdateQuest(DelayedQuestUpdate.UPDATE, mqs, false);
                } else {
                    //c.announce(MaplePacketCreator.updateQuestInfo(mqs.getQuestID(), 22000, "0"));
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}