package net.server.channel.handlers;

import client.MapleCharacter.DelayedQuestUpdate;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import net.AbstractMaplePacketHandler;
import scripting.quest.QuestScriptManager;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Xari
 */
public class RaiseUIStateHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int infoNumber = slea.readShort();
        
        if (c.tryacquireClient()) {
            try {
                MapleCharacter chr = c.getPlayer();
                MapleQuest quest = MapleQuest.getInstanceFromInfoNumber(infoNumber);
                MapleQuestStatus mqs = chr.getQuest(quest);
                
                QuestScriptManager.getInstance().raiseOpen(c, (short) infoNumber, mqs.getNpc());
                
                if (mqs.getStatus() == MapleQuestStatus.Status.NOT_STARTED) {
                    quest.forceStart(chr, 22000);
                    c.getAbstractPlayerInteraction().setQuestProgress(quest.getId(), infoNumber, 0);
                } else if (mqs.getStatus() == MapleQuestStatus.Status.STARTED) {
                    chr.announceUpdateQuest(DelayedQuestUpdate.UPDATE, mqs, mqs.getInfoNumber() > 0);
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}